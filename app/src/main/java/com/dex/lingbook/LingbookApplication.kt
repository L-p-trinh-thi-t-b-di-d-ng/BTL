package com.dex.lingbook

import android.app.Application
import com.cloudinary.android.MediaManager

class LingbookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dyxtb5sar"
        MediaManager.init(this, config)
    }
}