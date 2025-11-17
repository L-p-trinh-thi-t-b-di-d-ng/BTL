package com.dex.lingbook

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class InactivityWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Gọi hàm gửi thông báo
        sendInactivityNotification(applicationContext)
        return Result.success()
    }

    private fun sendInactivityNotification(context: Context) {

        // --- 1. Tạo Intent để mở lại app ---
        val intent = Intent(context, SplashScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // --- 2. Xây dựng thông báo ---
        val builder = NotificationCompat.Builder(context, LingbookApplication.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Đã 24 giờ trôi qua 😡")
            .setContentText("Học đi, người giàu họ khôn lắm, không lấy kẻ ngu đâu 🫵")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // --- 3. Gửi thông báo ---
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }
}