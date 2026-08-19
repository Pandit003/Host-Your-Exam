package com.example.testmaster.activities

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.HostedTestAdapter
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.model.Subscriber
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class UserProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivUserImg: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvSubscribersCount: TextView
    private lateinit var tvHostedExamsCount: TextView
    private lateinit var rvHostedExams: RecyclerView
    private lateinit var tvNoExams: TextView
    private lateinit var btnSubscribe: MaterialButton
    private lateinit var cvSubscribeInfo: View

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String? = null
    private var currentUserName: String? = null
    private var currentUserImage: String? = null
    
    private val hostedExamsList = mutableListOf<CreateQuestions>()
    private lateinit var adapter: HostedTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            finish()
            return
        }

        bindViews()
        setupRecyclerView()
        
        fetchCurrentUserDetails()
        fetchUserDetails()
        fetchHostedExams()
        checkSubscriptionStatus()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        ivUserImg = findViewById(R.id.iv_user_img)
        tvUsername = findViewById(R.id.tv_username)
        tvSubscribersCount = findViewById(R.id.tv_subscribers_count)
        tvHostedExamsCount = findViewById(R.id.tv_hosted_exams_count)
        rvHostedExams = findViewById(R.id.rv_hosted_exams)
        tvNoExams = findViewById(R.id.tv_no_exams)
        btnSubscribe = findViewById(R.id.btn_subscribe)
        cvSubscribeInfo = findViewById(R.id.cv_subscribe_info)

        toolbar.setNavigationOnClickListener { finish() }

        if (userId == auth.currentUser?.uid) {
            btnSubscribe.visibility = View.GONE
            cvSubscribeInfo.visibility = View.GONE
        } else {
            btnSubscribe.visibility = View.VISIBLE
            btnSubscribe.setOnClickListener { toggleSubscription() }
        }
    }

    private fun setupRecyclerView() {
        adapter = HostedTestAdapter(this, hostedExamsList, isOwner = (userId == auth.currentUser?.uid))
        rvHostedExams.layoutManager = LinearLayoutManager(this)
        rvHostedExams.adapter = adapter
    }

    private fun fetchCurrentUserDetails() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("personalDetails").document(currentUid).get()
            .addOnSuccessListener { doc ->
                currentUserName = doc.getString("name")
                currentUserImage = doc.getString("imageUrl")
            }
    }

    private fun fetchUserDetails() {
        db.collection("personalDetails").document(userId!!).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val name = doc.getString("name") ?: "Unknown"
                tvUsername.text = name
                tvSubscribersCount.text = (doc.getLong("subscribersCount") ?: 0).toString()
                
                val imageUrl = doc.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.baseline_account_circle_24).into(ivUserImg)
                }
            }
        }
    }

    private fun fetchHostedExams() {
        db.collection("CreatedQuestion").document(userId!!).collection("QuestionsDetails")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                
                hostedExamsList.clear()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val exam = doc.toObject(CreateQuestions::class.java)
                        hostedExamsList.add(exam)
                    }
                }
                
                tvHostedExamsCount.text = hostedExamsList.size.toString()
                
                if (hostedExamsList.isEmpty()) {
                    tvNoExams.visibility = View.VISIBLE
                    rvHostedExams.visibility = View.GONE
                } else {
                    tvNoExams.visibility = View.GONE
                    rvHostedExams.visibility = View.VISIBLE
                    
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    hostedExamsList.sortByDescending { 
                        try { it.hosting_date?.let { date -> originalFormat.parse(date) } } catch (e: Exception) { null } 
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun checkSubscriptionStatus() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("Following").document(currentUid)
            .collection("UserFollowing").document(userId!!)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    btnSubscribe.text = "Subscribed"
                    btnSubscribe.setIconResource(R.drawable.baseline_playlist_add_check_24)
                    btnSubscribe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surfaceVariant))
                    btnSubscribe.setTextColor(ContextCompat.getColor(this, R.color.onSurfaceVariant))
                    cvSubscribeInfo.visibility = View.GONE
                } else {
                    btnSubscribe.text = "Subscribe"
                    btnSubscribe.setIconResource(0)
                    btnSubscribe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bluetint))
                    btnSubscribe.setTextColor(Color.WHITE)
                    if (userId != auth.currentUser?.uid) {
                        cvSubscribeInfo.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun toggleSubscription() {
        val currentUid = auth.currentUser?.uid ?: return
        val targetUid = userId ?: return
        
        val subRef = db.collection("Subscribers").document(targetUid)
            .collection("UserSubscribers").document(currentUid)
        val followRef = db.collection("Following").document(currentUid)
            .collection("UserFollowing").document(targetUid)

        subRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Unsubscribe
                db.runTransaction { transaction ->
                    transaction.delete(subRef)
                    transaction.delete(followRef)
                    transaction.update(db.collection("personalDetails").document(targetUid), "subscribersCount", FieldValue.increment(-1))
                }.addOnSuccessListener {
                    Toast.makeText(this, "Unsubscribed", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Subscribe
                val subscriberData = Subscriber(uid = currentUid, name = currentUserName, imageUrl = currentUserImage)
                val followingData = hashMapOf("uid" to targetUid, "name" to tvUsername.text.toString(), "imageUrl" to "") // Image will be synced by function
                
                db.runTransaction { transaction ->
                    transaction.set(subRef, subscriberData)
                    transaction.set(followRef, followingData)
                    transaction.update(db.collection("personalDetails").document(targetUid), "subscribersCount", FieldValue.increment(1))
                }.addOnSuccessListener {
                    // Send Notification
                    val appNotification = com.example.testmaster.model.Notification(
                        title = "New Subscriber!",
                        message = "$currentUserName has subscribed to you.",
                        type = "SUBSCRIBE",
                        fromUserId = currentUid,
                        fromUserName = currentUserName ?: "",
                        fromUserImage = currentUserImage ?: ""
                    )
                    com.example.testmaster.util.NotificationHelper.sendNotification(this, targetUid, appNotification)
                    Toast.makeText(this, "Subscribed successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
