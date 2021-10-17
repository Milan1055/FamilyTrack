package com.example.familytrack

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Tried to open login activity!")

            // launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        //stay logged in and don`t sign out after closing the app unless user clicks sign out button
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            val i = Intent(this@RegisterActivity, MapsActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        } else {
            // User is signed out
            Log.d("Register", "onAuthStateChanged:signed_out")
        }


        //make image selector work on register page
        select_image_register.setOnClickListener {
            Log.d("RegisterActivity", "Clicked photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    // Set up profile picture for user
    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check and review selected image
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data !=null) {
            Log.d("RegisterActivity", "Image was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            select_image_register_view.setImageBitmap(bitmap)
         //   val bitmapDrawable = BitmapDrawable(bitmap)
       //     select_image_register_view.setBackgroundDrawable(bitmapDrawable)
            select_image_register.alpha = 0f
        }
    }



    private fun performRegister() {

            val email = email_edittext_register.text.toString()
            val password = password_edittext_register.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter the email and/or password", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            //make it to catch these data
            Log.d("RegisterActivity", "Email is: $email")
            Log.d("RegisterActivity", "Password: $password")


            //Firebase Authentication email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    // else if registration was successful
                    Log.d("RegisterActivity", "Successfully registered an account with the following UID: ${it.result?.user?.uid}")

                    uploadImageToFirebaseStorage()


                }
                .addOnFailureListener { Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }


            //Uploading image to firebase storage
            private fun uploadImageToFirebaseStorage() {
                if (selectedPhotoUri == null) return

                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {
                        Log.d("RegisterActivity", "Image successfully uploaded to Firebase: ${it.metadata?.path}")

                        //Retreived file location of image
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d("RegisterActivity", "File Location: $it")

                            saveUserToFirebaseDatabase(it.toString())
                        }
                    }
                    .addOnFailureListener{
                        Log.d("RegisterActivity", "Saving the image was not successful: ${it.message}")
                    }

            }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "User details have been saved to Firebase Database")

                //enter to app after registering
                val intent = Intent(this, MapsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


                //when back button pressed after signing in, it will exit the app instead of opening register page
               // val intent = Intent(this, LatestMessagesActivity::class.java)
               // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
               // startActivity(intent)

            }
            .addOnFailureListener {
        //        Log.d("RegisterActivity", "Failed to save user to database: ${it.message}")
            }


    }

    }

