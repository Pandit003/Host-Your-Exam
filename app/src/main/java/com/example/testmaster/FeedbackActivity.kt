package com.example.testmaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FeedbackActivity : AppCompatActivity() {
    private lateinit var firebaseAuth : FirebaseAuth
    val db = FirebaseFirestore.getInstance()
    lateinit var et_subject : TextView
    lateinit var et_msg : TextView
    lateinit var btn_feedback_submit : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        et_subject = findViewById(R.id.et_subject)
        et_msg = findViewById(R.id.et_msg)
        btn_feedback_submit = findViewById(R.id.btn_feedback_submit)
        firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        btn_feedback_submit.setOnClickListener {
            if(et_subject.text.toString().equals("")){
                Toast.makeText(this, "Please enter feedback subject", Toast.LENGTH_SHORT).show()
            }else if(et_msg.text.toString().equals("")){
                Toast.makeText(this, "Please enter feedback message", Toast.LENGTH_SHORT).show()
            }else {
                if (userId != null) {
                    val feedbackentry = hashMapOf(
                        "submited_by" to userId,
                        "suject" to et_subject.text.toString(),
                        "message" to et_msg.text.toString()
                    )
                    db.collection("feedBack").document(userId.toString())
                        .collection("Feedback Details")
                        .add(feedbackentry)
                        .addOnSuccessListener { document ->
                            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT)
                                .show()
                            onBackPressed()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Unable to submit your feedback! Try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }
}