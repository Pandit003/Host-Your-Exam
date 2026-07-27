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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.activities.SearchExamId
import com.example.testmaster.adapter.DisplayItem
import com.example.testmaster.adapter.GenericHorizontalAdapter
import com.example.testmaster.adapter.TestAppear_Adapter
import com.example.testmaster.model.AnswerKey
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

    private lateinit var rvAnnouncements: RecyclerView
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
        
        setupStatCards(view)

        btnJoinExam = view.findViewById(R.id.btn_join_exam)
        btnCreateExam = view.findViewById(R.id.btn_create_exam)
        btnJoinFirst = view.findViewById(R.id.btn_join_first)
        tvViewAll = view.findViewById(R.id.view_all)

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
    }

    private fun setupStatCards(view: View) {
        val totalCard = view.findViewById<View>(R.id.card_total_exams)
        totalCard.findViewById<TextView>(R.id.stat_label).text = "Total Exams"
        totalCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_assignment_24)

        val highestCard = view.findViewById<View>(R.id.card_highest_score)
        highestCard.findViewById<TextView>(R.id.stat_label).text = "Highest Score"
        highestCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_create_24)

        val avgCard = view.findViewById<View>(R.id.card_avg_score)
        avgCard.findViewById<TextView>(R.id.stat_label).text = "Avg Score"
        avgCard.findViewById<ImageView>(R.id.stat_icon).setImageResource(R.drawable.baseline_history_24)
    }

    private fun setupRecyclerViews() {
        // Recent Exams
        testAppearAdapter = TestAppear_Adapter(requireContext(), recentExamAppearList)
        rvTestAppear.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTestAppear.adapter = testAppearAdapter

        // Announcements
        val announcements = listOf(
            DisplayItem("Java Mid Semester", "Starts tomorrow at 10:00 AM"),
            DisplayItem("Python Practice Test", "New set added for practice"),
            DisplayItem("Maintenance Update", "Scheduled for Sunday night")
        )
        rvAnnouncements.adapter = GenericHorizontalAdapter(announcements, R.layout.layout_announcement_card) { v, item ->
            v.findViewById<TextView>(R.id.tv_announcement_title).text = item.title
            v.findViewById<TextView>(R.id.tv_announcement_desc).text = item.desc
        }

        // Explore
        val exploreItems = listOf(
            DisplayItem("Create Exams", "Host your own tests", iconRes = R.drawable.baseline_create_24),
            DisplayItem("Practice Tests", "Prepare with mocks", iconRes = R.drawable.baseline_assignment_24),
            DisplayItem("Leaderboard", "Compete with others", iconRes = R.drawable.baseline_assignment_24),
            DisplayItem("Certificates", "View your rewards", iconRes = R.drawable.baseline_history_24)
        )
        rvExplore.adapter = GenericHorizontalAdapter(exploreItems, R.layout.layout_explore_card) { v, item ->
            v.findViewById<TextView>(R.id.tv_explore_title).text = item.title
            v.findViewById<TextView>(R.id.tv_explore_desc).text = item.desc
            item.iconRes?.let { v.findViewById<ImageView>(R.id.iv_explore_icon).setImageResource(it) }
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
            v.findViewById<TextView>(R.id.tv_feature_title).text = item.title
            v.findViewById<TextView>(R.id.tv_feature_icon).text = item.iconText
        }
    }

    private fun fetchData() {
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
}
