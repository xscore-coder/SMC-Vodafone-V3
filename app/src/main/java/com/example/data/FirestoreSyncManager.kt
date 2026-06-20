package com.vodafone.smc.data

import android.content.Context
import android.util.Log

class FirestoreSyncManager(private val context: Context) {
    private var firestoreInstance: Any? = null
    private var isInitialized = false

    init {
        try {
            // Attempt to load and instantiate FirebaseFirestore via reflection
            // to prevent crashes if the Google Services JSON is not configured
            val firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore")
            val getInstanceMethod = firestoreClass.getMethod("getInstance")
            firestoreInstance = getInstanceMethod.invoke(null)
            isInitialized = firestoreInstance != null
            Log.d("FirestoreSyncManager", "Firebase Firestore initialized successfully: $isInitialized")
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Firebase Firestore is not compiled or google-services.json is missing.", e)
            isInitialized = false
        }
    }

    fun isFirebaseConfigured(): Boolean {
        return isInitialized
    }

    fun uploadReminder(reminder: Reminder) {
        val db = firestoreInstance ?: return
        try {
            val data = hashMapOf(
                "reminderId" to reminder.id,
                "title" to reminder.title,
                "description" to reminder.description,
                "dueDate" to reminder.dueDate,
                "escalateAfterMin" to reminder.escalateAfterMin,
                "priority" to reminder.priority,
                "status" to reminder.status,
                "createdAt" to reminder.createdAt,
                "startHour" to reminder.startHour,
                "endHour" to reminder.endHour,
                "repeatIntervalMin" to reminder.repeatIntervalMin,
                "soundType" to reminder.soundType,
                "vibrationEnabled" to reminder.vibrationEnabled,
                "notificationEnabled" to reminder.notificationEnabled
            )
            
            val collectionMethod = db.javaClass.getMethod("collection", String::class.java)
            val collectionReference = collectionMethod.invoke(db, "reminders")
            
            val documentMethod = collectionReference.javaClass.getMethod("document", String::class.java)
            val documentReference = documentMethod.invoke(collectionReference, reminder.id.toString())
            
            val setMethod = documentReference.javaClass.getMethod("set", Any::class.java)
            setMethod.invoke(documentReference, data)
            Log.d("FirestoreSync", "Queued reminder upload to Firestore for ID ${reminder.id}")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Failed to upload reminder via Firestore", e)
        }
    }

    fun uploadITTask(task: ITTask) {
        val db = firestoreInstance ?: return
        try {
            val data = hashMapOf(
                "taskId" to task.id,
                "title" to task.title,
                "description" to task.description,
                "priority" to task.priority,
                "dueDate" to task.dueDate,
                "status" to task.status,
                "isRecurring" to task.isRecurring,
                "createdAt" to task.createdAt
            )
            
            val collectionMethod = db.javaClass.getMethod("collection", String::class.java)
            val collectionReference = collectionMethod.invoke(db, "it_tasks")
            
            val documentMethod = collectionReference.javaClass.getMethod("document", String::class.java)
            val documentReference = documentMethod.invoke(collectionReference, task.id.toString())
            
            val setMethod = documentReference.javaClass.getMethod("set", Any::class.java)
            setMethod.invoke(documentReference, data)
            Log.d("FirestoreSync", "Queued IT task upload to Firestore for ID ${task.id}")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Failed to upload task via Firestore", e)
        }
    }

    fun deleteReminder(id: Int) {
        val db = firestoreInstance ?: return
        try {
            val collectionMethod = db.javaClass.getMethod("collection", String::class.java)
            val collectionReference = collectionMethod.invoke(db, "reminders")
            
            val documentMethod = collectionReference.javaClass.getMethod("document", String::class.java)
            val documentReference = documentMethod.invoke(collectionReference, id.toString())
            
            val deleteMethod = documentReference.javaClass.getMethod("delete")
            deleteMethod.invoke(documentReference)
            Log.d("FirestoreSync", "Queued reminder deletion from Firestore for ID $id")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Failed to delete reminder via Firestore", e)
        }
    }

    fun deleteITTask(id: Int) {
        val db = firestoreInstance ?: return
        try {
            val collectionMethod = db.javaClass.getMethod("collection", String::class.java)
            val collectionReference = collectionMethod.invoke(db, "it_tasks")
            
            val documentMethod = collectionReference.javaClass.getMethod("document", String::class.java)
            val documentReference = documentMethod.invoke(collectionReference, id.toString())
            
            val deleteMethod = documentReference.javaClass.getMethod("delete")
            deleteMethod.invoke(documentReference)
            Log.d("FirestoreSync", "Queued IT task deletion from Firestore for ID $id")
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Failed to delete task via Firestore", e)
        }
    }
}
