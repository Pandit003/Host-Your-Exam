package com.example.testmaster.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.R
import com.example.testmaster.adapter.AnnouncementAdapter
import com.example.testmaster.model.Announcement
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityFeedActivity : AppCompatActivity() {

    private lateinit var rvCommunityFeed: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: MaterialToolbar
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val announcementList = mutableListOf<Announcement>()
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_feed)

        toolbar = findViewById(R.id.toolbar)
        rvCommunityFeed = findViewById(R.id.rv_community_feed)
        llEmpty = findViewById(R.id.ll_empty_feed)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        toolbar.setNavigationOnClickListener { finish() }

        rvCommunityFeed.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementAdapter(announcementList,"community")
        rvCommunityFeed.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            fetchCommunityFeed()
        }

        fetchCommunityFeed()
    }

    private fun fetchCommunityFeed() {
        val user = auth.currentUser?.uid ?: return
        swipeRefresh.isRefreshing = true

        db.collection("Following").document(user).collection("UserFollowing")
            .get()
            .addOnSuccessListener { followingSnapshots ->
                val followedUids = followingSnapshots.documents.mapNotNull { it.id }.toMutableList()
                
                // Always include SYSTEM announcements
                followedUids.add("SYSTEM")

                if (followedUids.isEmpty()) {
                    updateUI()
                    return@addOnSuccessListener
                }

                // Firestore 'whereIn' supports up to 10 elements in some versions/configs, 
                // but usually it's 30 now. If followed count is huge, this might need chunking.
                // For now, fetching all and sorting.
                db.collection("Announcements")
                    .whereIn("announcerUid", followedUids)
                    .get()
                    .addOnSuccessListener { announcementSnapshots ->
                        announcementList.clear()
                        for (doc in announcementSnapshots) {
                            val announcement = doc.toObject(Announcement::class.java)
                            announcementList.add(announcement)
                        }
                        
                        // Sort by date (descending)
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        announcementList.sortByDescending { it.announcementDate?.let { d -> 
                            try { format.parse(d) } catch (e: Exception) { Date(0) } 
                        } ?: Date(0) }
                        
                        updateUI()
                    }
                    .addOnFailureListener {
                        updateUI()
                    }
            }
            .addOnFailureListener {
                updateUI()
            }
    }

    private fun updateUI() {
        swipeRefresh.isRefreshing = false
        if (announcementList.isEmpty()) {
            llEmpty.visibility = View.VISIBLE
            rvCommunityFeed.visibility = View.GONE
        } else {
            llEmpty.visibility = View.GONE
            rvCommunityFeed.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }
}
