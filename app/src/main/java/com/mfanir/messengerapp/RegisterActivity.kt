package com.mfanir.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this@RegisterActivity, WelcomeActivity::class.java))
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        register_button.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email: String = email_register.text.toString()
        val username: String = username_register.text.toString()
        val password: String = password_register.text.toString()

        if (email == "") {
            Toast.makeText(this, "please write email", Toast.LENGTH_LONG).show()
            //email.error = "please write email"
            //email.requestFocus()
        } else if (username == "") {
            Toast.makeText(this, "please write username", Toast.LENGTH_LONG).show()
            //username.error = "Silahkan isi Username"
            //username.requestFocus()
        } else if (password == "") {
            Toast.makeText(this, "please write password", Toast.LENGTH_LONG).show()
            //password.error = "Silahkan isi Username"
            //password.requestFocus()
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("ok", "createUserWithEmail:success")
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-ce9e6.appspot.com/o/user_pic.png?alt=media&token=5b256f66-3611-4daf-9ab4-d20d8807643c"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-ce9e6.appspot.com/o/644159.jpg?alt=media&token=e4a28c7b-714b-47aa-8143-ea666cf892aa"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    startActivity(Intent(this@RegisterActivity,MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                    finish()
                                }
                            }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Error", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Error Message : " + task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }
}
