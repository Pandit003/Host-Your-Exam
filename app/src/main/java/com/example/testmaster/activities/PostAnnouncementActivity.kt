package com.example.testmaster.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.AnnouncementAdapter
import com.example.testmaster.model.Announcement
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostAnnouncementActivity : AppCompatActivity() {

    private lateinit var rvMyAnnouncements: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private lateinit var fabAnnounce: ExtendedFloatingActionButton
    private lateinit var toolbar: MaterialToolbar
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val announcementList = mutableListOf<Announcement>()
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_announcement)

        toolbar = findViewById(R.id.toolbar)
        rvMyAnnouncements = findViewById(R.id.rv_my_announcements)
        llEmpty = findViewById(R.id.ll_empty_announcements)
        fabAnnounce = findViewById(R.id.fab_announce)

        toolbar.setNavigationOnClickListener { finish() }

        rvMyAnnouncements.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementAdapter(announcementList,"community")
        rvMyAnnouncements.adapter = adapter

        fabAnnounce.setOnClickListener {
            startActivity(Intent(this, CreateAnnouncementActivity::class.java))
        }

        fetchMyAnnouncements()
    }

    private fun fetchMyAnnouncements() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Announcements")
            .whereEqualTo("announcerUid", uid)
            .orderBy("announcementDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                announcementList.clear()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val announcement = doc.toObject(Announcement::class.java)
                        announcementList.add(announcement)
                    }
                }

                if (announcementList.isEmpty()) {
                    llEmpty.visibility = View.VISIBLE
                    rvMyAnnouncements.visibility = View.GONE
                } else {
                    llEmpty.visibility = View.GONE
                    rvMyAnnouncements.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
