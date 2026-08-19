package com.example.testmaster.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testmaster.R
import com.example.testmaster.activities.NotificationActivity
import com.example.testmaster.model.Notification
import com.google.firebase.firestore.FirebaseFirestore

object NotificationHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val CHANNEL_ID = "test_master_notifications"

    /**
     * Saves notification to Firestore.
     * The Firebase Cloud Function will automatically pick this up and send a push notification.
     */
    fun sendNotification(
        context: Context, // Kept for backward compatibility in the app calls
        toUserId: String,
        notification: Notification,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val notificationRef = db.collection("Notifications").document(toUserId)
            .collection("UserNotifications").document()
        
        val finalNotification = notification.copy(id = notificationRef.id)
        
        notificationRef.set(finalNotification)
            .addOnSuccessListener {
                onSuccess()
                Log.d("NotificationHelper", "Notification saved for user: $toUserId")
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e("NotificationHelper", "Failed to save notification", e)
            }
    }

    /**
     * Locally displays a status bar notification (used when app is open)
     */
    fun showSystemNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
