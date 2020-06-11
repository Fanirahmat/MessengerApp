package com.mfanir.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mfanir.messengerapp.MessageChatActivity
import com.mfanir.messengerapp.ModelClasses.Chat
import com.mfanir.messengerapp.ModelClasses.Users
import com.mfanir.messengerapp.R
import com.mfanir.messengerapp.VisitUserProfileActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (
    mContext: Context,
    mUsers: List<Users>,
    isChatCheck: Boolean
    ) : RecyclerView.Adapter<UserAdapter.ViewHolder?> ()
{
    private val mContext:Context
    private val mUsers: List<Users>
    private var isChatCheck: Boolean
    var lastMsg: String = ""

    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewgroup, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users? = mUsers[position]

        holder.userNameTxt.text = user!!.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.user_pic).into(holder.profileImageView)

        if (isChatCheck) {
            retrieveLastMessage(user.getUID(), holder.lastMessageTxt)
        } else {
            holder.lastMessageTxt.visibility = View.GONE
        }

        if (isChatCheck) {
            if (user.getStatus() == "online") {
                holder.onlineImage.visibility = View.VISIBLE
                holder.offlineImage.visibility = View.GONE
            } else {
                holder.onlineImage.visibility = View.GONE
                holder.offlineImage.visibility = View.VISIBLE
            }
        } else {
            holder.onlineImage.visibility = View.GONE
            holder.offlineImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message" , // which (position) = 0
                "Visit Profile" // which (position) = 1
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                if (which == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
                if (which == 1) {
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }
    }

    private fun retrieveLastMessage(chatUserId: String?, lastMessageTxt: TextView) {
        lastMsg = "defaultMSg"
        val fireUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for (data in p0.children) {
                    val chat: Chat? = data.getValue(Chat::class.java)
                    if (fireUser != null && chat != null) {
                        if (chat.getReceiver() == fireUser!!.uid && chat.getSender() == chatUserId
                            || chat.getReceiver() == chatUserId && chat.getSender() == fireUser!!.uid) {

                            lastMsg = chat.getMessage()!!
                        }
                    }
                }

                when(lastMsg) {
                    "defaultMSg" -> lastMessageTxt.text = "No Message"
                    "sent you an image" -> lastMessageTxt.text = "Image Sent."
                    else -> lastMessageTxt.text = lastMsg
                }

                lastMsg = "defaultMSg"
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var userNameTxt: TextView = itemView.findViewById(R.id.username)
        var profileImageView: CircleImageView = itemView.findViewById(R.id.profile_image)
        var onlineImage: CircleImageView = itemView.findViewById(R.id.image_online)
        var offlineImage: CircleImageView = itemView.findViewById(R.id.image_offline)
        var lastMessageTxt: TextView = itemView.findViewById(R.id.message_last)
    }

}