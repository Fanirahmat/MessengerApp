package com.mfanir.messengerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {

    private var iv: ImageView? = null
    private var imageUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        iv = findViewById(R.id.image_viewer)
        imageUrl = intent.getStringExtra("url")

        Picasso.get().load(imageUrl).into(iv)

    }
}
