package com.example.testmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        var iv_home : ImageView = findViewById(R.id.iv_home)
        iv_home.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}