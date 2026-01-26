package com.example.testmaster.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.testmaster.activities.MainActivity
import com.example.testmaster.R

class AboutActivity : AppCompatActivity() {
    lateinit var iv_home : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        iv_home = findViewById(R.id.iv_home)
        iv_home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}