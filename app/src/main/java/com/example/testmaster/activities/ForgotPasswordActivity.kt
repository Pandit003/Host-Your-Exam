package com.example.testmaster.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.testmaster.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var toolbar: Toolbar
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        enableEdgeToEdge()
        toolbar = findViewById(R.id.toolbar)
        etEmail = findViewById(R.id.et_email)
        btnResetPassword = findViewById(R.id.btn_reset_password)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnResetPassword.setOnClickListener {
            performPasswordReset()
        }
    }

    private fun performPasswordReset() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }

        btnResetPassword.isEnabled = false
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                btnResetPassword.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
