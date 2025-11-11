package com.dex.lingbook

import android.app.Application
import com.cloudinary.android.MediaManager

class LingbookApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Khởi tạo Cloudinary
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dyxtb5sar"
        // Bạn có thể thêm các cấu hình khác nếu cần

        MediaManager.init(this, config)
    }
}