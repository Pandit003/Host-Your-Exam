package com.example.testmaster.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.SubscriberAdapter
import com.example.testmaster.model.Subscriber
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubscribersActivity : AppCompatActivity() {

    private lateinit var rvSubscribers: RecyclerView
    private lateinit var tvNoSubscribers: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribers)

        val toolbar: MaterialToolbar = findViewById(R.id.subscribers_toolbar)
        rvSubscribers = findViewById(R.id.rv_subscribers)
        tvNoSubscribers = findViewById(R.id.tv_no_subscribers)
        
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        toolbar.setNavigationOnClickListener {
            finish()
        }

        rvSubscribers.layoutManager = LinearLayoutManager(this)
        fetchSubscribers()
    }

    private fun fetchSubscribers() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("Subscribers").document(currentUid)
            .collection("UserSubscribers")
            .get()
            .addOnSuccessListener { documents ->
                val subscriberList = mutableListOf<Subscriber>()
                for (document in documents) {
                    val subscriber = document.toObject(Subscriber::class.java)
                    subscriberList.add(subscriber)
                }

                if (subscriberList.isEmpty()) {
                    tvNoSubscribers.visibility = View.VISIBLE
                    rvSubscribers.visibility = View.GONE
                } else {
                    tvNoSubscribers.visibility = View.GONE
                    rvSubscribers.visibility = View.VISIBLE
                    rvSubscribers.adapter = SubscriberAdapter(subscriberList)
                }
            }
    }
}
