package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "otp_recv_channel"
    private const val CHANNEL_NAME = "OTP Notifications"

    fun showOtpNotification(context: Context, otp: String, phone: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for incoming OTPs"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("OTP RECV 💥💣")
                .setContentText("OTP: $otp ($phone)")
                .setStyle(NotificationCompat.BigTextStyle().bigText("OTP: $otp\nPhone: $phone\nStatus: Auto-Copied to Clipboard!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (_: Exception) {}
    }
}
