package com.example.login_signin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.login_signin.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class Sign_Up : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Color change of the User Agreement and Privacy Policy
        val fullText = "I've read and agreed to User Agreement  and Privacy Policy"
        val spannable = SpannableString(fullText)
        val termsStart = fullText.indexOf(" User Agreement")
        val termsEnd = termsStart + " User Agreement".length

        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#004643")),
            termsStart, termsEnd,
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

        // Go to the Login Page
        binding.txtBacktoLogin.setOnClickListener {
            val intent = Intent(this@Sign_Up, LoginPage::class.java)
            startActivity(intent)
        }


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()



        binding.btnSignUp.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmpasswordInput.text.toString().trim()
            val isAgreed = binding.singleRadio.isChecked  // Add this

            // check if any field is blank
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("All fields are required!")
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showAlert("Please enter a valid email address.")
            } else if (password.length < 6) {
                showAlert("Password must be at least 6 characters long.")
            } else if (password != confirmPassword) {
                showAlert("Password and Confirm Password do not match.")
            } else if (!isAgreed) {
                showAlert("You must agree to the User Agreement and Privacy Policy.")
            } else {
                // All validations passed, proceed with registration
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            AlertDialog.Builder(this)
                                .setTitle("Success")
                                .setMessage("Registration Successful.")
                                .setPositiveButton("OK") { _, _ ->
                                    startActivity(Intent(this, LoginPage::class.java))
                                    finish()
                                }
                                .show()
                        } else {
                            AlertDialog.Builder(this)
                                .setTitle("Failed")
                                .setMessage("Registration Failed: ${task.exception?.message}")
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
                        Intent(this@Sign_Up, HomePage::class.java).apply {
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
