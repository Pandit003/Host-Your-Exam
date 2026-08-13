package com.example.testmaster.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testmaster.R
import com.example.testmaster.model.personalDetail
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvSignin: TextView
    private lateinit var tvVerify: TextView
    
    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val checkHandler = Handler(Looper.getMainLooper())
    private var checkRunnable: Runnable? = null
    private var isUsernameUnique = false
    
    private var defaultBoxStrokeColor: ColorStateList? = null
    private var defaultHintTextColor: ColorStateList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        etEmail = findViewById(R.id.tv_email)
        etPassword = findViewById(R.id.tv_password)
        etUsername = findViewById(R.id.tv_username)
        tilUsername = findViewById(R.id.til_username)
        btnRegister = findViewById(R.id.btn_register)
        tvVerify = findViewById(R.id.tv_verify)
        tvSignin = findViewById(R.id.signin)

        // Capture default colors
//        defaultBoxStrokeColor = tilUsername.boxStrokeColorStateList
        defaultHintTextColor = tilUsername.hintTextColor

        setupUsernameRealTimeCheck()

        tvSignin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            performRegistration()
        }
    }

    private fun setupUsernameRealTimeCheck() {
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkRunnable?.let { checkHandler.removeCallbacks(it) }
                // Reset UI to normal state immediately when user starts typing
                updateUsernameUI(null)
                isUsernameUnique = false
            }

            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isEmpty()) {
                    return
                }

                checkRunnable = Runnable {
                    checkUsernameUniqueness(username)
                }
                checkHandler.postDelayed(checkRunnable!!, 1000)
            }
        })
    }

    private fun checkUsernameUniqueness(username: String) {
        db.collection("personalDetails")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    updateUsernameUI(false) // Taken
                    isUsernameUnique = false
                } else {
                    updateUsernameUI(true) // Available
                    isUsernameUnique = true
                }
            }
            .addOnFailureListener {
                updateUsernameUI(null)
                isUsernameUnique = false
            }
    }

    private fun updateUsernameUI(isAvailable: Boolean?) {
        if (isAvailable == null) {
            // Reset to normal state
            tilUsername.isErrorEnabled = false
            tilUsername.helperText = null
            defaultBoxStrokeColor?.let { tilUsername.setBoxStrokeColorStateList(it) }
            defaultHintTextColor?.let { 
                tilUsername.hintTextColor = it 
                tilUsername.defaultHintTextColor = it
            }
            return
        }

        val colorRes = if (isAvailable) R.color.green else R.color.red
        val colorInt = ContextCompat.getColor(this, colorRes)
        val colorStateList = ColorStateList.valueOf(colorInt)
        
        tilUsername.setBoxStrokeColorStateList(colorStateList)
        tilUsername.defaultHintTextColor = colorStateList
        tilUsername.hintTextColor = colorStateList
        
        if (isAvailable == false) {
            tilUsername.error = "Username already taken"
            tilUsername.isErrorEnabled = true
        } else {
            tilUsername.isErrorEnabled = false
            tilUsername.helperText = "Username available"
        }
    }

    private fun performRegistration() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!isConnectedToInternet()) {
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            return
        }
        if (!isUsernameUnique) {
            Toast.makeText(this, "Please choose a unique username", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }

        setLoading(true)

        // Final check before creation
        db.collection("personalDetails")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    setLoading(false)
                    updateUsernameUI(false)
                    Toast.makeText(this, "Username already taken. Try a new one.", Toast.LENGTH_SHORT).show()
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = mAuth.currentUser
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful) {
                                            tvVerify.visibility = View.VISIBLE
                                            saveUserDetails(user.uid, username, email)
                                            mAuth.signOut()
                                            Toast.makeText(this, "Verification email sent. Please verify to login.", Toast.LENGTH_LONG).show()
                                            setLoading(false)
                                        } else {
                                            setLoading(false)
                                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                setLoading(false)
                                if (task.exception is FirebaseAuthUserCollisionException) {
                                    Toast.makeText(this, "Email already in use.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error checking username uniqueness", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDetails(userId: String, username: String, email: String) {
        val detail = personalDetail(
            name = username,
            name_lowercase = username.lowercase(),
            email = email,
            subscribersCount = 0
        )
        db.collection("personalDetails").document(userId).set(detail)
    }

    private fun setLoading(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        checkRunnable?.let { checkHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}
