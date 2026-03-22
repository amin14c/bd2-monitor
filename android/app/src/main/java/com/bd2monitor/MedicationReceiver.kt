package com.bd2monitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MedicationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nameAr = intent.getStringExtra("medNameAr") ?: ""
        val nameFr = intent.getStringExtra("medNameFr") ?: ""
        val dosage = intent.getStringExtra("dosage") ?: ""

        val channelId = "medication_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيه الدواء / Rappel médicament",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 موعد الدواء / Heure du médicament")
            .setContentText("$nameAr / $nameFr — $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
