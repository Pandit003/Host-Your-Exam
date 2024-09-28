package com.example.testmaster

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.testmaster.model.personalDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var et_email : EditText
    private lateinit var et_password : EditText
    private lateinit var et_username : EditText
    private lateinit var btn_register : Button
    private lateinit var signin : TextView
    private lateinit var tv_verify : TextView
    private lateinit var show_password : ImageView
    private lateinit var mAuth : FirebaseAuth
    lateinit var username : String
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        et_email=findViewById<EditText>(R.id.tv_email)
        et_password=findViewById<EditText>(R.id.tv_password)
        et_username=findViewById<EditText>(R.id.tv_username)
        btn_register=findViewById<Button>(R.id.btn_register)
        tv_verify=findViewById<Button>(R.id.tv_verify)
        show_password=findViewById<ImageView>(R.id.show_password)
        tv_verify.visibility = View.GONE
        username = et_username.text.toString()
        signin=findViewById(R.id.signin)
        signin.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        mAuth = FirebaseAuth.getInstance()
        show_password.setOnClickListener {
            if (et_password.transformationMethod == PasswordTransformationMethod.getInstance()) {
                et_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                show_password.setColorFilter(ContextCompat.getColor(this, R.color.black))

            } else {
                et_password.transformationMethod = PasswordTransformationMethod.getInstance()
                show_password.setColorFilter(ContextCompat.getColor(this, R.color.darkgray))
            }
            et_password.setSelection(et_password.text.length)
        }
        btn_register.setOnClickListener {
            btn_register.isEnabled = false
            if (!isConnectedToInternet()) {
                Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show()
                btn_register.isEnabled = true
            }
            else if (et_username.text.toString().isNullOrEmpty()) {
                btn_register.isEnabled = true
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
            else if (et_email.text.toString().isNullOrEmpty()) {
                btn_register.isEnabled = true
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            }
            else if (et_password.text.toString().isNullOrEmpty()) {
                btn_register.isEnabled = true
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("personalDetails")
                    .whereEqualTo("name", et_username.text.toString())
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            Toast.makeText(
                                this,
                                "Username already taken. Try a new one.",
                                Toast.LENGTH_SHORT
                            ).show()
                            btn_register.isEnabled = true
                        } else {
                            // Register the user with email and password
                            mAuth.createUserWithEmailAndPassword(
                                et_email.text.toString(),
                                et_password.text.toString()
                            )
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = mAuth.currentUser
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { emailTask ->
                                                if (emailTask.isSuccessful) {
                                                    tv_verify.visibility = View.VISIBLE
                                                    saveUserDetailsAfterVerification(user?.uid)

                                                    mAuth.signOut()

                                                } else {
                                                    btn_register.isEnabled = true
                                                    Toast.makeText(
                                                        this@RegisterActivity,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        btn_register.isEnabled = true
                                        if (task.exception is FirebaseAuthUserCollisionException) {
                                            Toast.makeText(this@RegisterActivity, "Email already in use. Please use another email.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@RegisterActivity, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        }
                    }
            }
        }
    }
    private fun saveUserDetailsAfterVerification(userId: String?) {
        userId?.let {
            val personalDetail = personalDetail(
                name = et_username.text.toString(),
                email = et_email.text.toString(),
                phone_no = null,
                dob = null
            )

            db.collection("personalDetails").document(it)
                .set(personalDetail)
                .addOnSuccessListener {
                    Toast.makeText(this@RegisterActivity, "Account created and user details saved.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@RegisterActivity, "Failed to store user details.", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}