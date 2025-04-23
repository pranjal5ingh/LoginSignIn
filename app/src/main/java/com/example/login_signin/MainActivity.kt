package com.example.login_signin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


       // Close SplashScreen to prevent going back
        // Check the user's authentication status after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser: FirebaseUser? = auth.currentUser
            // If user is logged in, direct them to the HomePage
            if (currentUser != null) {
                startActivity(Intent(this@MainActivity, HomePage::class.java))

            } else {
                // If no user is logged in, direct them to the LoginPage
                startActivity(Intent(this@MainActivity, LoginPage::class.java))
            }
            finish()
        }, 2000) // Delay for 2 seconds to show the splash screen
    }
}
