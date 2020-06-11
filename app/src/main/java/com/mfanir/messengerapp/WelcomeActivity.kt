package com.mfanir.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    var  firebaseUser: FirebaseUser? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        register_welcome_btn.setOnClickListener {
            startActivity(Intent(this@WelcomeActivity,RegisterActivity::class.java))
        }

        login_welcome_btn.setOnClickListener {
            startActivity(Intent(this@WelcomeActivity,LoginActivity::class.java))
        }

    }

    override fun onStart() {
        super.onStart()

        firebaseUser= FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            startActivity(Intent(this@WelcomeActivity,MainActivity::class.java))
            finish()
        }
    }
}
