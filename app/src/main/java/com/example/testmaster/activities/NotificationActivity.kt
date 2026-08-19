package com.example.testmaster.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.NotificationAdapter
import com.example.testmaster.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private lateinit var ivHome: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationList = mutableListOf<Notification>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        ivHome = findViewById(R.id.iv_home)
        rvNotifications = findViewById(R.id.rv_notifications)
        llEmpty = findViewById(R.id.ll_empty_notifications)

        ivHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(this, notificationList) { notification ->
            markAsRead(notification)
        }
        rvNotifications.adapter = adapter

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Notifications").document(uid)
            .collection("UserNotifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                notificationList.clear()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val notification = doc.toObject(Notification::class.java)
                        notificationList.add(notification)
                    }
                }

                if (notificationList.isEmpty()) {
                    llEmpty.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                } else {
                    llEmpty.visibility = View.GONE
                    rvNotifications.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun markAsRead(notification: Notification) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Notifications").document(uid)
            .collection("UserNotifications").document(notification.id)
            .update("read", true)
    }
}
