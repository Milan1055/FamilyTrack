package com.example.familytrack

import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contact_page.*

class ContactPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_page)


        //create populated email button

        imageView_contact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)

            intent.type = "text/html"

            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("milan.karacson@gmail.com"))

           // intent.putExtra(Intent.EXTRA_CC, arrayOf<String>(""))

            intent.putExtra(Intent.EXTRA_SUBJECT, "FamilyTrack - Query")

            intent.putExtra(Intent.EXTRA_TEXT, "Hello there, I am writing regarding the FamilyTrack application because...")

            startActivity(intent)


        }


    }
}