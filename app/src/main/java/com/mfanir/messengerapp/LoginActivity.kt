package com.mfanir.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this@LoginActivity, WelcomeActivity::class.java))
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        login_button.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

        if (email == "") {
            Toast.makeText(this, "please write email", Toast.LENGTH_LONG).show()
        } else if (password == "") {
            Toast.makeText(this, "please write password", Toast.LENGTH_LONG).show()
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(Intent(this@LoginActivity,MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Error", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Error Message : " + task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT).show()
                        // ...
                    }

                    // ...
                }
        }
    }


}
