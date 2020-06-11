package com.mfanir.messengerapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mfanir.messengerapp.ModelClasses.Users

import com.mfanir.messengerapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    var refUsers: DatabaseReference? = null
    var fireUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        fireUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(fireUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("UserImages")

        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val users: Users? = p0.getValue(Users::class.java)

                    if (context != null) {
                        username_settings.text = users!!.getUserName()
                        Picasso.get().load(users.getProfile()).into(profile_image_settings)
                        Picasso.get().load(users.getCover()).into(cover_image_settings)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        view.profile_image_settings.setOnClickListener {
            coverChecker = "profile_image"
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            coverChecker = "cover_image"
            pickImage()
        }

        view.set_fb.setOnClickListener {
            socialChecker = "facebook"
            setSocialLink()
        }

        view.set_ig.setOnClickListener {
            socialChecker = "instagram"
            setSocialLink()

        }
        view.set_web.setOnClickListener {
            socialChecker = "website"
            setSocialLink()
        }

        return view
    }

    private fun setSocialLink() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val et = EditText(context)

        if (socialChecker == "website") {
            builder.setTitle("Write URL : ")
            et.hint = "e.g www.google.com"
        } else {
            builder.setTitle("Write Username Account : ")
            et.hint = "e.g abc123"
        }

        builder.setView(et)
        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, which ->
            val str = et.text.toString()
            if (str == "") {
                Toast.makeText(context,"Please write something...", Toast.LENGTH_LONG).show()
            } else {
                saveSocialLink(str)
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()

    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()
        when (socialChecker) {
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" -> {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        refUsers!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context,"Updated Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Toast.makeText(context,"uploading .....", Toast.LENGTH_LONG).show()
            uploadImageToDB()
        }
    }

    private fun uploadImageToDB() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait .....")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }

                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dowloadUrl = task.result
                    val url = dowloadUrl.toString()

                    if (coverChecker == "cover_image") {
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        refUsers!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    } else {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        refUsers!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }

}
