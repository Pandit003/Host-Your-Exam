package com.example.testmaster

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaster.model.personalDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {
    private lateinit var firebaseAuth : FirebaseAuth
    val db = FirebaseFirestore.getInstance()
    lateinit var et_username : TextView
    lateinit var et_email : TextView
    lateinit var et_phone_no : TextView
    lateinit var et_dob : TextView
    lateinit var btn_submit : Button
    lateinit var iv_edit : ImageView
    lateinit var iv_personimage : ImageView
    private lateinit var storageReference: FirebaseStorage
    private val IMAGE_PICK_CODE = 1000
    private var imageUri: Uri? = null
    var name = "N/A"
    var email = "N/A"
    var phone = "N/A"
    var dob = "N/A"
    var imageUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        et_username = findViewById(R.id.et_username)
        et_email = findViewById(R.id.et_email)
        et_phone_no = findViewById(R.id.et_phone_no)
        et_dob = findViewById(R.id.et_dob)
        btn_submit = findViewById(R.id.btn_submit)
        iv_edit = findViewById(R.id.iv_edit)
        iv_personimage = findViewById(R.id.iv_personimage)
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        iv_edit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
        et_dob.setOnClickListener {
            showDatePicker()
        }
        btn_submit.setOnClickListener {
            btn_submit.isEnabled = false
            uploadImageToFirebase()
        }
        if (userId != null) {
            db.collection("personalDetails").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        name = document.getString("name").toString()
                        email = document.getString("email").toString()
                        phone = document.getString("phone_no").toString()
                        dob = document.getString("dob").toString()
                        imageUrl = document.getString("imageUrl").toString()
                        et_username.text = name
                        et_email.text = email
                        if(!phone.equals("null")){
                            et_phone_no.text = phone
                        }
                        if(!dob.equals("null")){
                            et_dob.text = dob
                        }
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(imageUrl).fit()
                                .into(iv_personimage)
                        }
                    }
                }
                .addOnFailureListener {

                }
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)
                et_dob.text = formattedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }
    private fun uploadImageToFirebase() {
        val pd = ProgressDialog(this)
        pd.setMessage("Uploading")
        pd.show()
        if (imageUri != null) {
            val userId = firebaseAuth.currentUser?.uid
            val storageRef = storageReference.reference.child("images/$userId/profile.jpg")

            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val personalDetail = personalDetail(
                            name = et_username.text.toString(),
                            email = et_email.text.toString(),
                            phone_no = et_phone_no.text.toString(),
                            dob = et_dob.text.toString(),
                            imageUrl = imageUrl
                        )

                        db.collection("personalDetails").document(userId!!)
                            .set(personalDetail)
                            .addOnSuccessListener {
                                pd.dismiss()
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                pd.dismiss()
                                btn_submit.isEnabled = true
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                    btn_submit.isEnabled = true
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            pd.dismiss()
            btn_submit.isEnabled = true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            iv_personimage.setImageURI(imageUri)
        }
    }

}