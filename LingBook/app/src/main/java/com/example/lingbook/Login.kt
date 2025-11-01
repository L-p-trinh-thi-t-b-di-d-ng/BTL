package com.example.lingbook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import org.json.JSONException

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private lateinit var googleButton: Button
    private lateinit var facebookButton: Button

    // --- Google launcher ---
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                goToProfile()
                            } else {
                                Toast.makeText(this, "Google đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Lỗi Google API: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Đăng nhập Google bị hủy", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(applicationContext)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        googleButton = findViewById(R.id.google)
        facebookButton = findViewById(R.id.facebook)

        // --- Google ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        }

        // --- Facebook ---
        facebookButton.setOnClickListener {
            // Đảm bảo clear session cũ
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut()
            }

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@Login, "Đăng nhập Facebook bị hủy", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@Login, "Lỗi Facebook: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // --- Facebook ---
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val request = GraphRequest.newMeRequest(token) { obj, _ ->
                        try {
                            val name = obj?.optString("name")
                            val email = obj?.optString("email")
                            val photoUrl = obj?.getJSONObject("picture")
                                ?.getJSONObject("data")?.optString("url")

                            goToProfile(name, email, photoUrl)
                        } catch (e: JSONException) {
                            goToProfile()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email,picture.type(large)")
                    request.parameters = parameters
                    request.executeAsync()
                } else {
                    Toast.makeText(this, "Facebook đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToProfile(name: String? = null, email: String? = null, photoUrl: String? = null) {
        val user = auth.currentUser
        val intent = Intent(this, Profile::class.java)
        intent.putExtra("name", name ?: user?.displayName)
        intent.putExtra("email", email ?: user?.email)
        intent.putExtra("photoUrl", photoUrl ?: user?.photoUrl?.toString())
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
