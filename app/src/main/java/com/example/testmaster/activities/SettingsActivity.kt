package com.example.testmaster.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.testmaster.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        firebaseAuth = FirebaseAuth.getInstance()

        val toolbar: MaterialToolbar = findViewById(R.id.settings_toolbar)
        val themeSwitch: SwitchMaterial = findViewById(R.id.theme_switch)
        val notificationSwitch: SwitchMaterial = findViewById(R.id.notification_switch)
        val btnLogout: MaterialButton = findViewById(R.id.btn_logout_settings)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        
        // Setup Theme Switch
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        themeSwitch.isChecked = isDarkMode
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("DarkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Setup Notification Switch
        val isNotificationEnabled = sharedPref.getBoolean("Notifications", true)
        notificationSwitch.isChecked = isNotificationEnabled
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("Notifications", isChecked).apply()
        }

        // Setup Logout
        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
