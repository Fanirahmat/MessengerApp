package com.mfanir.messengerapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.mfanir.messengerapp.AdapterClasses.UserAdapter
import com.mfanir.messengerapp.ModelClasses.Chatlist
import com.mfanir.messengerapp.ModelClasses.Users
import com.mfanir.messengerapp.Notifications.Token

import com.mfanir.messengerapp.R

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var userChatList: List<Chatlist>? = null
    lateinit var rv_chatlist: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chats, container, false)

        rv_chatlist = view.findViewById(R.id.rv_chatlist)
        rv_chatlist.setHasFixedSize(true)
        rv_chatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userChatList = ArrayList()

        val ref  = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (userChatList as ArrayList).clear()
                for (dataSnapshot in p0.children) {
                    val chatlist = dataSnapshot.getValue(Chatlist::class.java)
                    (userChatList as ArrayList).add(chatlist!!)

                }
                retrieveChatList()
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList() {
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()
                for (dataSnapshot in p0.children) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    for (eachChatList in userChatList!!) {
                        if (user!!.getUID().equals(eachChatList.getId())) {
                            (mUsers as ArrayList).add(user)
                        }


                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>),true)
                rv_chatlist.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}
