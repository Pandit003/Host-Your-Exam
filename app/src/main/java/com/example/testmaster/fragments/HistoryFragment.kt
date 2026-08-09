package com.example.testmaster.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.R
import com.example.testmaster.adapter.HistoryAdapter
import com.example.testmaster.model.AnswerKey
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    var examDataList: MutableList<AnswerKey> = mutableListOf()
    lateinit var rv_exam_history:RecyclerView
    lateinit var adapter: HistoryAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var iv_notfound: LinearLayout

    override fun onResume() {
        getHistoryList()
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        getHistoryList()
        rv_exam_history = view.findViewById(R.id.rv_exam_history)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        iv_notfound = view.findViewById(R.id.iv_notfound)
        val appBarLayout: AppBarLayout = view.findViewById(R.id.appBarLayout)
        val headerCard: View = view.findViewById(R.id.cardHeader)
        swipeRefreshLayout.setOnRefreshListener {
            getHistoryList()
        }

        adapter = HistoryAdapter(view.context,examDataList)
        rv_exam_history.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
        rv_exam_history.adapter = adapter

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
            // Total distance the app bar can collapse
            val maxScroll = appBar.totalScrollRange.toFloat()

            if (maxScroll != 0f) {
                // Calculates percentage scrolled: 1.0 = fully visible, 0.0 = fully collapsed
                val percentage = (maxScroll + verticalOffset) / maxScroll

                // Dynamically adjust alpha based on scroll distance
                headerCard.alpha = percentage
            }
        })
        return view
    }
    fun getHistoryList() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        db.collection("History").document(user).collection("HistoryDetails")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    examDataList.clear()  // Clear the list before adding updated data
                    for (document in task.result) {
                        val answerKey = document.toObject(AnswerKey::class.java)
                        examDataList.add(answerKey)
                    }
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    examDataList.sortWith { a, b ->
                        val dateA = a.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        val dateB = b.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        dateB.compareTo(dateA)  // Sort in descending order
                    }
                    adapter.notifyDataSetChanged()
                    rv_exam_history.post {
                        rv_exam_history.invalidate()
                        rv_exam_history.requestFocus()
                        rv_exam_history.requestLayout()
                    }
                    if(examDataList.isEmpty()){
                        iv_notfound.visibility = View.VISIBLE
                    }else{
                        iv_notfound.visibility = View.GONE
                    }
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.exception)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
    }

}