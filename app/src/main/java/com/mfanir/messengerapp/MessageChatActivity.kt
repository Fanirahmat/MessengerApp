package com.mfanir.messengerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mfanir.messengerapp.AdapterClasses.APIService
import com.mfanir.messengerapp.AdapterClasses.ChatsAdapter
import com.mfanir.messengerapp.ModelClasses.Chat
import com.mfanir.messengerapp.ModelClasses.Users
import com.mfanir.messengerapp.Notifications.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    var reference: DatabaseReference? = null
    lateinit var recycler_view_chat: RecyclerView

    var notify = false
    var apiService: APIService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)


        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chat = findViewById(R.id.rv_chat)
        recycler_view_chat.setHasFixedSize(true)

        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd =  true

        recycler_view_chat.layoutManager = linearLayoutManager

        //displaying username and profile receiver
        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                username_receiver.text = user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_receiver)
                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        send_message_btn.setOnClickListener {
            notify = true
            val message = et_text_message.text.toString()
            if (message == "") {
                Toast.makeText(this, "Please write a message", Toast.LENGTH_LONG).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            et_text_message.setText("")
        }

        attach_image_file_btn.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
        }

        seenMessage(userIdVisit)
    }



    private fun sendMessageToUser(senderID: String, receiverID: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val messageKey = ref.push().key
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderID
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverID
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageID"] = messageKey
        ref.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatsListRef = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatsListRef.child("id").setValue(userIdVisit)
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })


                    //implement the push notification using fcm
                    val reference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
                    reference.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(Users::class.java)
                            if (notify) {
                                sendNotification(receiverID, user!!.getUserName(), message)
                            }
                            notify = false
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                }
            }

    }

    private fun sendNotification(receiverID: String?, userName: String?, message: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverID)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
               for (dataSnapshot in p0.children) {
                   val token: Token?  = dataSnapshot.getValue(Token::class.java)
                   val data = Data (
                       firebaseUser!!.uid,
                       R.mipmap.ic_launcher,
                       "$userName: $message",
                       "New Message",
                       userIdVisit
                   )

                   val sender = Sender(data!!, token!!.getToken().toString() )
                    apiService!!.sendNotification(sender).enqueue(object : Callback<MyResponse>{
                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {
                            if (response.code() == 200)  {
                                if (response.body()!!.success != 1) {
                                    Toast.makeText(this@MessageChatActivity, "Failed, Nothing happen.", Toast.LENGTH_LONG).show()

                                }
                            }
                        }

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                            TODO("Not yet implemented")
                        }


                    })
               }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is uploading, please wait .....")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("ChatImages")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dowloadUrl = task.result
                    val url = dowloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageID"] = messageId
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful) {
                                progressBar.dismiss()


                            }
                        }
                }
            }
        }

        //implement the push notification using fcm
        val usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        usersReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users::class.java)
                if (notify) {
                    sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun retrieveMessages(senderID: String, receiverID: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderID) && chat.getSender().equals(receiverID)
                        || chat.getReceiver().equals(receiverID) && chat.getSender().equals(senderID))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    recycler_view_chat.adapter = chatsAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }



        })

    }

    var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
    }
}