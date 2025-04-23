package com.example.login_signin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract.Colors
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login_signin.databinding.ActivityLoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginPage : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val binding:ActivityLoginPageBinding by lazy {
        ActivityLoginPageBinding.inflate(layoutInflater)
    }

//    override fun onStart() {
//        super.onStart()
//
//        // Check if the user is already signed in
//        Handler(Looper.getMainLooper()).postDelayed({
//            val currentUser: FirebaseUser? = auth.currentUser
//            if (currentUser != null) {
//                // User is signed in → go to HomePage
//                startActivity(Intent(this, HomePage::class.java))
//            }
//            finish() // Close SplashActivity so that the user can't navigate back to it
//        }, 2000) // Splash screen delay (2 seconds)
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val fullText = "I've read and agreed to User Agreement  and Privacy Policy"
        val spannable = SpannableString(fullText)


        val termsStart = fullText.indexOf(" User Agreement")
        val termsEnd = termsStart + " User Agreement".length

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#004643")),
            termsStart,termsEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(20, true), // Increase size of "Terms" to 20sp
            termsStart, termsEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#004643")), // Orange
            privacyStart, privacyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(20, true), // Increase size of "Conditions" to 18sp
            privacyStart, privacyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.termsText.text = spannable

        binding.txtCreateAccount.setOnClickListener{
            val intent = Intent(this@LoginPage,Sign_Up::class.java )
            startActivity(intent)
        }
        // Initialize Firebase Auth

        auth = FirebaseAuth.getInstance()

        binding.btnSignin.setOnClickListener{
            val email:String = binding.emailInput.text.toString()
            val password:String = binding.passwordInput.text.toString()
            val isAgreed = binding.singleRadio.isChecked

            if ( email.isEmpty() || password.isEmpty()){
                showAlert("All fields are required!")
            }else if (!isAgreed) {
                showAlert("You must agree to the User Agreement and Privacy Policy.")
            } else{
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // ✅ Show success alert and go to HomePage after user taps OK
                            AlertDialog.Builder(this)
                                .setTitle("Login Successful")
                                .setMessage("Welcome back!")
                                .setPositiveButton("OK") { _, _ ->
                                    startActivity(Intent(this, HomePage::class.java))
                                    finish()
                                }
                                .show()
                        } else {
                            // ❌ Show error if login fails
                            AlertDialog.Builder(this)
                                .setTitle("Login Failed")
                                .setMessage("Authentication Failed: ${task.exception?.message}")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
            }
        }

    // Instantiate a Google sign-in request
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso)

    binding.imgGoogleLogo.setOnClickListener {
        googleSignInClient.signOut()
        startActivityForResult(googleSignInClient.signInIntent, 13)
    }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Corrected condition: Check requestCode (13) and resultCode (RESULT_OK)
        if (requestCode == 13 && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google sign-in failed", e)
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Auto-redirect to HomePage without dialog
                    startActivity(
                        Intent(this@LoginPage, HomePage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    finish()
                } else {
                    Log.d("GoogleSignIn", "Firebase auth failed: ${task.exception?.message}")
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Validation Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}