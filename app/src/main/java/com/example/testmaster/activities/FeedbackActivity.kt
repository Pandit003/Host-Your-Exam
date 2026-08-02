package com.example.testmaster.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.testmaster.R
import com.example.testmaster.util.CustomDialogUtils
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setTint(getColor(R.color.onPrimary))

        val userId = firebaseAuth.currentUser?.uid
        btn_feedback_submit.setOnClickListener {
            if (!isInternetAvailable(this)) {
                showNoInternetDialog()
            }else if(et_subject.text.toString().equals("")){
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
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetDialog() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            positiveText = "Retry",
            negativeText = "Exit",
            onPositive = {
                btn_feedback_submit.performClick()
            },
            onNegative = {
                finish()
            }

        )
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}