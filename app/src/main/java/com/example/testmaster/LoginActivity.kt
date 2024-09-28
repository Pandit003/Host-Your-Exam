package com.example.testmaster

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.LineRadarDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var et_email: EditText
    private lateinit var et_password: EditText
    private lateinit var register: TextView
    private lateinit var tv_resend_verification: TextView
    private lateinit var tv_countdown: TextView
    private lateinit var btn_login: Button
    private lateinit var show_password : ImageView
    private lateinit var ll_verify : LinearLayout
    val db = FirebaseFirestore.getInstance()
    private var timer: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        et_email = findViewById(R.id.tv_email)
        et_password = findViewById(R.id.tv_password)
        register = findViewById(R.id.register)
        tv_resend_verification = findViewById(R.id.tv_resend_verification)
        tv_countdown = findViewById(R.id.tv_countdown)
        btn_login = findViewById(R.id.btn_login)
        show_password=findViewById<ImageView>(R.id.show_password)
        ll_verify=findViewById<LinearLayout>(R.id.ll_verify)
        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        handler.post(runnable)
        val firebaseAuth = FirebaseAuth.getInstance()
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
        val forgot_password: TextView = findViewById(R.id.forgot_password)
        forgot_password.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        tv_resend_verification.setOnClickListener {
            val user = firebaseAuth.currentUser
            if (user != null && user.isEmailVerified){
                Toast.makeText(this, "Already verified", Toast.LENGTH_SHORT).show()
            }else if (et_email.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            } else if (et_password.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show()
            }else {
                tv_resend_verification.isClickable = false
                tv_resend_verification.setTextColor(resources.getColor(R.color.primary_blue, null))
                val user = firebaseAuth.currentUser
                user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        tv_countdown.visibility = View.VISIBLE
                        startTimer(60000)
                        Toast.makeText(
                            this,
                            "Verification email sent. Please check your inbox.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        tv_resend_verification.isClickable = true
                        tv_resend_verification.setTextColor(resources.getColor(R.color.blue, null))
                        Toast.makeText(
                            this,
                            "Failed to send verification email: ${emailTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        btn_login.setOnClickListener {
            btn_login.isEnabled = false
            if (!isConnectedToInternet()) {
                btn_login.isEnabled = true
                Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show()
            } else if (et_email.text.toString().isNullOrEmpty()) {
                btn_login.isEnabled = true
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            } else if (et_password.text.toString().isNullOrEmpty()) {
                btn_login.isEnabled = true
                Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(
                    et_email.text.toString(),
                    et_password.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null && user.isEmailVerified){
                            Toast.makeText(this, "Login sucessfully", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }else{
                            btn_login.isEnabled = true
                            ll_verify.visibility = View.VISIBLE
                            Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        btn_login.isEnabled = true
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid user and password",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("Login", "Authentication failed: ${task.exception?.message}")
                    }
                }
            }
        }
    }
    private fun startTimer(timeInMillis: Long) {
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) % 60
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                tv_countdown.text = timeFormatted
            }

            override fun onFinish() {
                tv_resend_verification.isClickable = true
                tv_resend_verification.setTextColor(resources.getColor(R.color.blue, null))
                tv_countdown.visibility = View.GONE
            }
        }.start()
    }
    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}