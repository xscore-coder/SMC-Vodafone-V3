package com.vodafone.smc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vodafone.smc.R
import com.vodafone.smc.data.AppDatabase
import com.vodafone.smc.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goAsync = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminderId = intent.getIntExtra("reminder_id", -1)
                if (reminderId != -1) {
                    val database = AppDatabase.getDatabase(context.applicationContext, this)
                    val dao = database.appDao()
                    val allReminders = dao.getAllReminders().first()
                    val reminder = allReminders.find { it.id == reminderId }
                    
                    if (reminder != null && (reminder.status == "Active" || reminder.status == "Escalated")) {
                        // Check if current hour falls within the allowed daily range (e.g. 7:00 AM to 7:00 PM)
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // 0-23
                        
                        var isInTimeWindow = false
                        if (reminder.startHour <= reminder.endHour) {
                            isInTimeWindow = currentHour >= reminder.startHour && currentHour <= reminder.endHour
                        } else {
                            // Midnight wraparound (e.g. 20 PM to 6 AM)
                            isInTimeWindow = currentHour >= reminder.startHour || currentHour <= reminder.endHour
                        }
                        
                        if (isInTimeWindow) {
                            if (reminder.notificationEnabled) {
                                triggerNotification(context, reminder)
                            }
                            playAlarmSound(context, reminder.soundType, reminder.title)
                            vibrateDevice(context, reminder.vibrationEnabled)
                            
                            try {
                                val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                                    putExtra("reminder_id", reminder.id)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                }
                                context.startActivity(alertIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Log.d("AlarmReceiver", "Alarm for Reminder ID ${reminder.id} suppressed because current hour ($currentHour) is outside allowed window ($reminder.startHour to $reminder.endHour)")
                        }
                        
                        // Reschedule alarm for next interval if reminder is still active
                        val intervalMin = reminder.repeatIntervalMin.coerceAtLeast(1)
                        var nextTriggerMs = System.currentTimeMillis() + (intervalMin * 60 * 1000L)
                        
                        // Ensure the timer strictly works from startHour and ends fully in endHour
                        val calendarNext = Calendar.getInstance()
                        calendarNext.timeInMillis = nextTriggerMs
                        val nextHour = calendarNext.get(Calendar.HOUR_OF_DAY)
                        
                        var nextInWindow = false
                        if (reminder.startHour <= reminder.endHour) {
                            nextInWindow = nextHour >= reminder.startHour && nextHour <= reminder.endHour
                        } else {
                            nextInWindow = nextHour >= reminder.startHour || nextHour <= reminder.endHour
                        }
                        
                        if (!nextInWindow) {
                            val calAdjusted = Calendar.getInstance()
                            calAdjusted.timeInMillis = nextTriggerMs
                            calAdjusted.set(Calendar.MINUTE, 0)
                            calAdjusted.set(Calendar.SECOND, 0)
                            calAdjusted.set(Calendar.MILLISECOND, 0)
                            
                            if (reminder.startHour <= reminder.endHour) {
                                if (nextHour > reminder.endHour) {
                                    calAdjusted.add(Calendar.DAY_OF_YEAR, 1)
                                }
                                calAdjusted.set(Calendar.HOUR_OF_DAY, reminder.startHour)
                            } else {
                                if (nextHour > reminder.endHour && nextHour < reminder.startHour) {
                                    calAdjusted.set(Calendar.HOUR_OF_DAY, reminder.startHour)
                                }
                            }
                            nextTriggerMs = calAdjusted.timeInMillis
                        }
                        
                        val updatedReminder = reminder.copy(dueDate = "TimerTarget:$nextTriggerMs")
                        dao.insertReminder(updatedReminder)
                        
                        AlarmScheduler.scheduleAlarm(context.applicationContext, updatedReminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                goAsync.finish()
            }
        }
    }

    private fun triggerNotification(context: Context, reminder: Reminder) {
        val channelId = "smc_reminders_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMC Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for SMC operational alarm reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, reminder.id, mainIntent, pendingFlags)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.vodafone.smc.R.drawable.ic_no_sim)
            .setContentTitle("🚨 SLA Reminders: ${reminder.title}")
            .setContentText(reminder.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            
        try {
            notificationManager.notify(reminder.id, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAlarmSound(context: Context, soundType: String, reminderTitle: String? = null) {
        try {
            val prefs = context.getSharedPreferences("smc_pref", Context.MODE_PRIVATE)
            val customizedSound = prefs.getString("reminder_sound", "Default") ?: "Default"
            AlertSoundTester.playSound(context, customizedSound, reminderTitle ?: "SLA Alarm triggered")
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val r = RingtoneManager.getRingtone(context, notificationUri)
                r?.play()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun vibrateDevice(context: Context, vibrationEnabled: Boolean) {
        if (!vibrationEnabled) return
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(800, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(800)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
