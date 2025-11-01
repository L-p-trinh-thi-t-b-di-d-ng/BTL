package com.example.lingbook

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    private lateinit var avatarImage: ImageView
    private lateinit var nameField: TextView
    private lateinit var emailField: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    Glide.with(this).load(it).into(avatarImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        avatarImage = findViewById(R.id.avatarImage)
        nameField = findViewById(R.id.name)
        emailField = findViewById(R.id.email)
        logoutButton = findViewById(R.id.logout)
        backButton = findViewById(R.id.back)

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")

        nameField.text = name ?: "Không có tên"
        emailField.text = email ?: "Không có email"

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this).load(photoUrl).into(avatarImage)
        }

        val auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, gso)

        logoutButton.setOnClickListener {
            auth.signOut()
            googleClient.signOut()
            LoginManager.getInstance().logOut()

            val intent = Intent(this, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
