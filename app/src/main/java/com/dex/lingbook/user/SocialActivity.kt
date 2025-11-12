package com.dex.lingbook.user

import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dex.lingbook.R
import android.app.Dialog

class SocialActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AVATAR_URL = "extra_avatar_url"
    }

    private lateinit var avatarImageView: ImageView
    private lateinit var recyclerPosts: RecyclerView
    private var currentAvatarUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_network)

        // 1. Ánh xạ View
        avatarImageView = findViewById(R.id.avatarImageView)
        recyclerPosts = findViewById(R.id.recyclerPosts)

        // 2. Lấy AVATAR URL TỪ INTENT
        currentAvatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL)

        loadAvatar()
        setupClickListeners()
        setupRecyclerView()
    }

    private fun setupClickListeners() {
        // Gán listener cho khu vực có ID "nhapBai"
        val nhapBaiLayout = findViewById<LinearLayout>(R.id.nhapBai)
        nhapBaiLayout.setOnClickListener {
            showCustomFeatureDevelopingDialog()
        }
    }

    /**
     * Hiển thị hộp thoại thông báo tính năng đang phát triển với layout tùy chỉnh.
     */
    private fun showCustomFeatureDevelopingDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.notification)
        dialog.setCancelable(true)

        // 1. Ánh xạ View trong Dialog
        val btnClose: ImageView = dialog.findViewById(R.id.btnClose)
        val btnPositive: Button = dialog.findViewById(R.id.btnPositive)

        // 2. Gán sự kiện cho nút đóng 'X'
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // 3. Gán sự kiện cho nút "Đồng ý"
        btnPositive.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    private fun loadAvatar() {
        if (!currentAvatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentAvatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.ic_user_placeholder)
        }
    }

    private fun setupRecyclerView() {
        // ...
    }
}