package com.example.testmaster.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.example.testmaster.R
import com.example.testmaster.model.personalDetail
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvRegister: TextView
    private lateinit var tvResendVerification: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleLogin: Button
    private lateinit var llVerify: LinearLayout
    private lateinit var llRegister: LinearLayout
    private lateinit var pbLogin: ProgressBar
    
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var timer: CountDownTimer? = null

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("GoogleSignIn", "resultCode = ${result.resultCode}")
        Log.d("GoogleSignIn", "RESULT_OK = $RESULT_OK")
        Log.d("GoogleSignIn", "data = ${result.data}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("Login", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
        } else {
            setLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        enableEdgeToEdge()

        // Check if user is already logged in
        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified) {
            navigateToMain()
            return
        }

        // Initialize UI elements
        etEmail = findViewById(R.id.tv_email)
        etPassword = findViewById(R.id.tv_password)
        tvRegister = findViewById(R.id.register)
        tvResendVerification = findViewById(R.id.tv_resend_verification)
        tvCountdown = findViewById(R.id.tv_countdown)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
        pbLogin = findViewById(R.id.pb_login)
        llRegister = findViewById(R.id.ll_register)
        llVerify = findViewById(R.id.ll_verify)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.forgot_password).setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            performEmailLogin()
        }

        btnGoogleLogin.setOnClickListener {
            performGoogleLogin()
        }

        tvResendVerification.setOnClickListener {
            resendVerificationEmail()
        }
    }

    private fun performEmailLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!isConnectedToInternet()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
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

        setLoading(true)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        navigateToMain()
                    } else {
                        setLoading(false)
                        llVerify.visibility = View.VISIBLE
                        Toast.makeText(this, "Please verify your email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun performGoogleLogin() {
        if (!isConnectedToInternet()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        checkIfUserExistsInFirestore(it)
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Google Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfUserExistsInFirestore(user: com.google.firebase.auth.FirebaseUser) {
        db.collection("personalDetails").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    navigateToMain()
                } else {
                    val details = personalDetail(
                        name = user.displayName ?: "User",
                        email = user.email,
                        imageUrl = user.photoUrl?.toString()
                    )
                    db.collection("personalDetails").document(user.uid).set(details)
                        .addOnSuccessListener { navigateToMain() }
                        .addOnFailureListener {
                            setLoading(false)
                            Toast.makeText(this, "Failed to save user info", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error checking user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tvResendVerification.isEnabled = false
                startTimer(60000)
                Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTimer(timeInMillis: Long) {
        timer?.cancel()
        tvCountdown.visibility = View.VISIBLE
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = String.format("%02d:%02d", (millisUntilFinished / 1000) / 60, (millisUntilFinished / 1000) % 60)
            }
            override fun onFinish() {
                tvResendVerification.isEnabled = true
                tvCountdown.visibility = View.GONE
            }
        }.start()
    }

    private fun setLoading(isLoading: Boolean) {
        pbLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        llRegister.isVisible = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
