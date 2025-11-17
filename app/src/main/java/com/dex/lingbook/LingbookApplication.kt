package com.dex.lingbook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.cloudinary.android.MediaManager
import java.util.concurrent.TimeUnit

class LingbookApplication : Application(), LifecycleEventObserver {

    companion object {
        const val CHANNEL_ID = "lingbook_reminders"
        const val INACTIVITY_WORK_NAME = "inactivity_notification_work"
    }

    override fun onCreate() {
        super.onCreate()
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dyxtb5sar"
        MediaManager.init(this, config)

        createNotificationChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // --- CÁC HÀM MỚI ĐỂ XỬ LÝ THÔNG BÁO ---

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                cancelInactivityWork()
            }
            Lifecycle.Event.ON_STOP -> {
                scheduleInactivityWork()
            }
            else -> {
            }
        }
    }

    private fun scheduleInactivityWork() {
        val inactivityWorkRequest = OneTimeWorkRequest.Builder(InactivityWorker::class.java)
            //.setInitialDelay(1, TimeUnit.MINUTES)
            .setInitialDelay(24, TimeUnit.HOURS) // Đặt thời gian trễ là 24 GIỜ
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            INACTIVITY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            inactivityWorkRequest
        )
    }
    private fun cancelInactivityWork() {
        WorkManager.getInstance(this).cancelUniqueWork(INACTIVITY_WORK_NAME)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nhắc nhở học tập"
            val descriptionText = "Kênh thông báo nhắc nhở người dùng học bài"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
