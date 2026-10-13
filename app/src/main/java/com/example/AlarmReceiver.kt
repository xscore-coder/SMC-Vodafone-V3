package com.vodafone.smc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vodafone.smc.data.AppDatabase
import com.vodafone.smc.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * BroadcastReceiver responsible exclusively for scheduling and dispatching standard
 * system notifications using the OS default notification sound and channel settings.
 */
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
                        // Check if current hour falls within the allowed daily range
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        
                        var isInTimeWindow = false
                        if (reminder.startHour <= reminder.endHour) {
                            isInTimeWindow = currentHour >= reminder.startHour && currentHour <= reminder.endHour
                        } else {
                            isInTimeWindow = currentHour >= reminder.startHour || currentHour <= reminder.endHour
                        }
                        
                        if (isInTimeWindow) {
                            // Exclusively rely on standard Android notification mechanism with default sound
                            triggerNotification(context, reminder)
                        } else {
                            Log.d("AlarmReceiver", "Notification for Reminder ID ${reminder.id} suppressed because hour ($currentHour) is outside window (${reminder.startHour} to ${reminder.endHour})")
                        }
                        
                        // Reschedule for next interval if still active
                        val intervalMin = reminder.repeatIntervalMin.coerceAtLeast(1)
                        var nextTriggerMs = System.currentTimeMillis() + (intervalMin * 60 * 1000L)
                        
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
        val channelId = "smc_default_alerts_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "SMC Alerts & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Standard system notifications for SMC alerts and operational reminders"
                enableLights(true)
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
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
            .setSmallIcon(R.drawable.ic_no_sim)
            .setContentTitle("SMC Alert: ${reminder.title}")
            .setContentText(reminder.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            
        try {
            notificationManager.notify(reminder.id, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
