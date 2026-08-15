package com.example.testmaster.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.activities.SearchExamId
import com.example.testmaster.adapter.DisplayItem
import com.example.testmaster.adapter.GenericHorizontalAdapter
import com.example.testmaster.adapter.TestAppear_Adapter
import com.example.testmaster.adapter.AnnouncementAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.Announcement
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var user: String
    
    private lateinit var rvTestAppear: RecyclerView
    private lateinit var testAppearAdapter: TestAppear_Adapter
    private var recentExamAppearList: MutableList<AnswerKey> = mutableListOf()
    
    private lateinit var layoutStats: LinearLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var llTestAppearSection: LinearLayout
    
    private lateinit var tvTotalExams: TextView
    private lateinit var tvHighestScore: TextView
    private lateinit var tvAvgScore: TextView
    
    private lateinit var btnJoinExam: MaterialButton
    private lateinit var btnCreateExam: MaterialButton
    private lateinit var btnJoinFirst: MaterialButton
    private lateinit var tvViewAll: TextView

    private lateinit var cvSubscribers: View
    private lateinit var tvSubscriberCount: TextView

    private lateinit var rvAnnouncements: RecyclerView
    private var announcementList: MutableList<Announcement> = mutableListOf()
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    private lateinit var rvExplore: RecyclerView
    private lateinit var rvFeatures: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        bindViews(view)
        setupClickListeners()
        setupRecyclerViews()
        
        seedAppAnnouncements()
        fetchData()
        
        return view
    }

    private fun bindViews(view: View) {
        rvTestAppear = view.findViewById(R.id.rv_testApear)
        layoutStats = view.findViewById(R.id.layout_stats)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        llTestAppearSection = view.findViewById(R.id.ll_test_appear)
        
        tvTotalExams = view.findViewById<View>(R.id.card_total_exams).findViewById(R.id.stat_value)
        tvHighestScore = view.findViewById<View>(R.id.card_highest_score).findViewById(R.id.stat_value)
        tvAvgScore = view.findViewById<View>(R.id.card_avg_score).findViewById(R.id.stat_value)

        tvTotalExams.setTextColor(resources.getColor(R.color.bluetint))
        tvHighestScore.setTextColor(resources.getColor(R.color.orangetint))
        tvAvgScore.setTextColor(resources.getColor(R.color.greentint))

        setupStatCards(view)

        btnJoinExam = view.findViewById(R.id.btn_join_exam)
        btnCreateExam = view.findViewById(R.id.btn_create_exam)
        btnJoinFirst = view.findViewById(R.id.btn_join_first)
        tvViewAll = view.findViewById(R.id.view_all)

        cvSubscribers = view.findViewById(R.id.cv_subscribers)
        tvSubscriberCount = view.findViewById(R.id.tv_subscriber_count)

        rvAnnouncements = view.findViewById(R.id.rv_announcements)
        rvExplore = view.findViewById(R.id.rv_explore)
        rvFeatures = view.findViewById(R.id.rv_features)
    }

    private fun setupClickListeners() {
        val joinExamAction = View.OnClickListener {
            startActivity(Intent(requireContext(), SearchExamId::class.java))
        }
        btnJoinExam.setOnClickListener(joinExamAction)
        btnJoinFirst.setOnClickListener(joinExamAction)
        
        btnCreateExam.setOnClickListener {
            // Trigger navigation to Create Exam tab if parent is MainActivity
            activity?.let {
                if (it is com.example.testmaster.activities.MainActivity) {
                    it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.nav_create_exam
                }
            }
        }
        
        tvViewAll.setOnClickListener {
            activity?.let {
                if (it is com.example.testmaster.activities.MainActivity) {
                    it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.nav_history
                }
            }
        }

        cvSubscribers.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.testmaster.activities.SubscribersActivity::class.java))
        }
    }

    private fun setupStatCards(view: View) {
        val totalCard = view.findViewById<View>(R.id.card_total_exams)
        totalCard.findViewById<TextView>(R.id.stat_label).text = "Total Exams"
        totalCard.findViewById<TextView>(R.id.stat_label).setTextColor(resources.getColor(R.color.bluetint))
        totalCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_assignment_24)
        totalCard.findViewById<ImageView>(R.id.stat_icon).setColorFilter(ContextCompat.getColor(
            requireContext(), R.color.bluetint))

        val highestCard = view.findViewById<View>(R.id.card_highest_score)
        highestCard.findViewById<TextView>(R.id.stat_label).text = "Highest Score"
        highestCard.findViewById<TextView>(R.id.stat_label).setTextColor(resources.getColor(R.color.orangetint))
        highestCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_create_24)
        highestCard.findViewById<ImageView>(R.id.stat_icon).setColorFilter(ContextCompat.getColor(requireContext(), R.color.orangetint))

        val avgCard = view.findViewById<View>(R.id.card_avg_score)
        avgCard.findViewById<TextView>(R.id.stat_label).text = "Avg Score"
        avgCard.findViewById<TextView>(R.id.stat_label).setTextColor(resources.getColor(R.color.greentint))
        avgCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_history_24)
        avgCard.findViewById<ImageView>(R.id.stat_icon).setColorFilter(ContextCompat.getColor(requireContext(), R.color.greentint))

        val totalBg = view.findViewById<View>(R.id.card_total_exams)
        totalBg.findViewById<LinearLayout>(R.id.state_bg).setBackgroundColor(resources.getColor(R.color.blue_bg))
        val highestBg = view.findViewById<View>(R.id.card_highest_score)
        highestBg.findViewById<LinearLayout>(R.id.state_bg).setBackgroundColor(resources.getColor(R.color.orange_bg))
        val avgBg = view.findViewById<View>(R.id.card_avg_score)
        avgBg.findViewById<LinearLayout>(R.id.state_bg).setBackgroundColor(resources.getColor(R.color.green_bg))

    }

    private fun setupRecyclerViews() {
        // Recent Exams
        testAppearAdapter = TestAppear_Adapter(requireContext(), recentExamAppearList)
        rvTestAppear.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTestAppear.adapter = testAppearAdapter

        // Announcements
        announcementAdapter = AnnouncementAdapter(announcementList,"")
        rvAnnouncements.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvAnnouncements.adapter = announcementAdapter

        val tintColors = listOf(R.color.bluetint, R.color.purpletint, R.color.orangetint, R.color.greentint)
        val bgColors = listOf(R.color.blue_bg, R.color.purple_bg, R.color.orange_bg, R.color.green_bg)

        // Explore
        val exploreItems = listOf(
            DisplayItem("Create Exams", "Host your own tests", iconRes = R.drawable.baseline_create_24),
            DisplayItem("Practice Tests", "Prepare with mocks", iconRes = R.drawable.baseline_assignment_24),
            DisplayItem("Leaderboard", "Compete with others", iconRes = R.drawable.baseline_assignment_24),
            DisplayItem("Certificates", "View your rewards", iconRes = R.drawable.baseline_history_24)
        )

        rvExplore.adapter = GenericHorizontalAdapter(exploreItems, R.layout.layout_explore_card) { v, item ->
            val pos = exploreItems.indexOf(item)
            val colorIdx = pos % tintColors.size
            
            v.findViewById<TextView>(R.id.tv_explore_title).text = item.title
            v.findViewById<TextView>(R.id.tv_explore_desc).text = item.desc
            item.iconRes?.let { v.findViewById<ImageView>(R.id.iv_explore_icon).setImageResource(it) }
            
            // Apply colors
            v.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cv_icon_container)
                .setCardBackgroundColor(resources.getColor(bgColors[colorIdx]))
            v.findViewById<ImageView>(R.id.iv_explore_icon)
                .setColorFilter(resources.getColor(tintColors[colorIdx]))
            v.findViewById<LinearLayout>(R.id.ll_explore_container)
                .setBackgroundColor(resources.getColor(bgColors[colorIdx]))
        }

        // Features
        val featureItems = listOf(
            DisplayItem("Easy Hosting", iconText = "📚"),
            DisplayItem("Instant Results", iconText = "⚡"),
            DisplayItem("Performance Tracking", iconText = "📊"),
            DisplayItem("Secure Exam", iconText = "🔒"),
            DisplayItem("Teacher Friendly", iconText = "👨‍🏫")
        )
        rvFeatures.adapter = GenericHorizontalAdapter(featureItems, R.layout.layout_feature_card) { v, item ->
            val pos = featureItems.indexOf(item)
            val colorIdx = pos % tintColors.size

            v.findViewById<TextView>(R.id.tv_feature_title).text = item.title
            v.findViewById<TextView>(R.id.tv_feature_icon).text = item.iconText

            // Apply colors
            val rootCard = v.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cv_feature_root)
            rootCard.setCardBackgroundColor(resources.getColor(bgColors[colorIdx]))
            rootCard.setStrokeColor(android.content.res.ColorStateList.valueOf(resources.getColor(tintColors[colorIdx])))
            v.findViewById<TextView>(R.id.tv_feature_title).setTextColor(resources.getColor(tintColors[colorIdx]))
        }
    }

    private fun fetchData() {
        fetchSubscriberCount()
        fetchAnnouncements()
        db.collection("History").document(user).collection("HistoryDetails")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("HomeFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documents != null && !documents.isEmpty) {
                    recentExamAppearList.clear()
                    var totalScoreSum = 0f
                    var totalMaxMarkSum = 0f
                    var highestPercentage = 0f
                    
                    val tempList = mutableListOf<AnswerKey>()
                    for (document in documents) {
                        val answerKey = document.toObject(AnswerKey::class.java)
                        if (answerKey != null) {
                            tempList.add(answerKey)
                            
                            val score = answerKey.total_score?.toFloatOrNull() ?: 0f
                            val maxMark = (answerKey.pos_mark?.toFloatOrNull() ?: 0f) * (answerKey.questionsWithAns?.size ?: 0)
                            
                            if (maxMark > 0) {
                                val percentage = (score / maxMark) * 100
                                if (percentage > highestPercentage) highestPercentage = percentage
                                totalScoreSum += score
                                totalMaxMarkSum += maxMark
                            }
                        }
                    }

                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    tempList.sortWith { a, b ->
                        val dateA = a.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        val dateB = b.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        dateB.compareTo(dateA)
                    }

                    recentExamAppearList.addAll(tempList.take(5))
                    
                    tvTotalExams.text = tempList.size.toString()
                    tvHighestScore.text = "${highestPercentage.toInt()}%"
                    val avgPercentage = if (totalMaxMarkSum > 0) (totalScoreSum / totalMaxMarkSum * 100).toInt() else 0
                    tvAvgScore.text = "$avgPercentage%"
                    
                    layoutStats.visibility = View.VISIBLE
                    llTestAppearSection.visibility = View.VISIBLE
                    layoutEmptyState.visibility = View.GONE
                    testAppearAdapter.notifyDataSetChanged()
                } else {
                    layoutStats.visibility = View.GONE
                    llTestAppearSection.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                }
            }
    }

    private fun fetchSubscriberCount() {
        db.collection("Subscribers").document(user)
            .collection("UserSubscribers")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("HomeFragment", "Listen for subscribers failed.", error)
                    return@addSnapshotListener
                }
                tvSubscriberCount.text = snapshots?.size()?.toString() ?: "0"
            }
    }

    private fun fetchAnnouncements() {
        db.collection("Following").document(user).collection("UserFollowing")
            .addSnapshotListener { followingSnapshots, error ->
                if (error != null || followingSnapshots == null) return@addSnapshotListener
                
                val followedUids = followingSnapshots.documents.mapNotNull { it.id }.toMutableList()
                // Always include SYSTEM announcements
                followedUids.add("SYSTEM")

                db.collection("Announcements")
                    .whereIn("announcerUid", followedUids)
                    .addSnapshotListener { announcementSnapshots, aError ->
                        if (aError != null || announcementSnapshots == null) return@addSnapshotListener
                        
                        announcementList.clear()
                        for (doc in announcementSnapshots) {
                            val announcement = doc.toObject(Announcement::class.java)
                            announcementList.add(announcement)
                        }
                        // Sort by date (descending) - assuming announcementDate is dd MMM yyyy
                        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        announcementList.sortByDescending { it.announcementDate?.let { d -> format.parse(d) } ?: Date(0) }
                        
                        announcementAdapter.notifyDataSetChanged()
                    }
            }
    }

    private fun seedAppAnnouncements() {
        val systemRef = db.collection("Announcements").whereEqualTo("announcerUid", "SYSTEM").limit(1)
        systemRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                // Seed initial announcements
                val a1 = Announcement(
                    id = "sys_1",
                    announcerUid = "SYSTEM",
                    announcerName = "Test Master App",
                    title = "Welcome to Test Master!",
                    description = "Stay updated with your teachers and ace your exams with our platform. More features coming soon!",
                    announcementDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                    type = "MESSAGE"
                )
                val a2 = Announcement(
                    id = "sys_2",
                    announcerUid = "SYSTEM",
                    announcerName = "Admin",
                    title = "Mock Exam Practice",
                    description = "Join the weekly mock exam to test your skills in Java and Python. High scorers will be featured on the leaderboard!",
                    examDate = "15 Aug 2024",
                    announcementDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                    duration = "90",
                    noOfQuestions = "60",
                    markingPattern = "+4, -1",
                    type = "EXAM"
                )
                db.collection("Announcements").document(a1.id!!).set(a1)
                db.collection("Announcements").document(a2.id!!).set(a2)
            }
        }
    }
}
