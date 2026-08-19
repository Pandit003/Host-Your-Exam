package com.example.testmaster.activities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.ExamDetailsAdapter
import com.example.testmaster.adapter.UserSearchAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.model.personalDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchExamId : AppCompatActivity() {

    lateinit var rv_exam_data : RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchHistoryAdapter: ArrayAdapter<String>
    var examDataList: MutableList<AnswerKey> = mutableListOf()
    lateinit var user : String

    private val examList = mutableListOf<CreateQuestions>()
    private val userResultList = mutableListOf<personalDetail>()
    private val userResultIds = mutableListOf<String>()

    private var examDetailsAdapter: ExamDetailsAdapter? = null
    private var userSearchAdapter: UserSearchAdapter? = null
    private var concatAdapter: ConcatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_exam_id)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        rv_exam_data = findViewById(R.id.rv_exam_data)
        tvNoResults = findViewById(R.id.tv_no_results)
        rv_exam_data.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE)

        val root: View = findViewById(R.id.search_root)
        val etSearch: AutoCompleteTextView = findViewById(R.id.et_search)
        etSearch.hint = "Search Exam ID or Username"
        val ivClear: ImageView = findViewById(R.id.iv_clear)
        val ivBack: ImageView = findViewById(R.id.iv_back)

        etSearch.requestFocus()
        root.setOnClickListener {
            etSearch.requestFocus()
            showKeyboard(etSearch)
        }

        ivBack.setOnClickListener {
            if (this is Activity) finish()
            else {
                // or call your nav controller popBackStack()
            }
        }
        ivClear.setOnClickListener {
            etSearch.text?.clear()
            ivClear.visibility = View.GONE
            clearResults()
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrEmpty()
                if (hasText) {
                    searchExams(s.toString())
                } else {
                    clearResults()
                }
                ivClear.visibility = if (hasText) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                searchExams(query)
                saveSearchQuery(query)
                hideKeyboard(etSearch)
                true
            } else false
        }
        etSearch?.setOnItemClickListener { parent, view, position, id ->
            val query = parent.getItemAtPosition(position) as String
            searchExams(query)
        }
        loadSearchHistory()
        etSearch?.setAdapter(searchHistoryAdapter)
    }

    private fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = view.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun loadSearchHistory() {
        val searchHistory = sharedPreferences.getStringSet("history", setOf())?.toMutableList()
        searchHistoryAdapter = ArrayAdapter(this, R.layout.dropdown_item, searchHistory ?: listOf())
    }

    private fun saveSearchQuery(query: String) {
        val maxHistorySize = 10 
        val searchHistory = sharedPreferences.getStringSet("history", mutableSetOf())?.toMutableSet()

        if (searchHistory != null) {
            if (searchHistory.contains(query)) {
                searchHistory.remove(query)
            }
            searchHistory.add(query)

            if (searchHistory.size > maxHistorySize) {
                val excessCount = searchHistory.size - maxHistorySize
                val iterator = searchHistory.iterator()
                repeat(excessCount) {
                    if (iterator.hasNext()) iterator.next().let { iterator.remove() }
                }
            }

            sharedPreferences.edit().putStringSet("history", searchHistory).apply()
            loadSearchHistory() 
        }
    }

    private fun clearResults() {
        examList.clear()
        userResultList.clear()
        userResultIds.clear()
        examDataList.clear()
        rv_exam_data.adapter = null
        tvNoResults.visibility = View.GONE
    }

    fun searchExams(query: String) {
        if (query.isEmpty()) {
            clearResults()
            return
        }

        val isNumeric = query.all { it.isDigit() }

        if (isNumeric && query.length >= 6) {
            // Search for Exams
            db.collection("Exams")
                .whereEqualTo("exam_id", query)
                .get()
                .addOnSuccessListener { documents ->
                    examList.clear()
                    userResultList.clear()
                    userResultIds.clear()

                    val tempExamList = mutableListOf<CreateQuestions>()
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val examData = document.toObject(CreateQuestions::class.java)
                            tempExamList.add(examData)
                        }
                    }

                    if (tempExamList.isNotEmpty()) {
                        filterExamsByVisibility(tempExamList, query)
                    } else {
                        updateAdapters()
                    }
                }
        } else {
            // Search for Users
            db.collection("personalDetails")
                .orderBy("name_lowercase")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener { documents ->
                    userResultList.clear()
                    userResultIds.clear()
                    examList.clear()
                    examDataList.clear()

                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val userDetail = document.toObject(personalDetail::class.java)
                            userResultList.add(userDetail)
                            userResultIds.add(document.id)
                        }
                    }
                    updateAdapters()
                }.addOnFailureListener { exception ->
                    Log.e("SearchExamId", "Error occurred while searching for users", exception)
                }
        }
    }

    private fun filterExamsByVisibility(tempList: List<CreateQuestions>, query: String) {
        val filteredList = mutableListOf<CreateQuestions>()
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        var processedCount = 0
        if (tempList.isEmpty()) {
            updateAdapters()
            return
        }

        for (exam in tempList) {
            if (exam.visibility == "Public" || exam.candidate_id == currentUserId) {
                filteredList.add(exam)
                processedCount++
                if (processedCount == tempList.size) {
                    fetchHistoryAndShow(filteredList, query)
                }
            } else {
                // Check if current user is a subscriber of the host
                db.collection("Subscribers").document(exam.candidate_id!!)
                    .collection("UserSubscribers").document(currentUserId)
                    .get()
                    .addOnSuccessListener { subscriberDoc ->
                        if (subscriberDoc.exists()) {
                            filteredList.add(exam)
                        }
                        processedCount++
                        if (processedCount == tempList.size) {
                            fetchHistoryAndShow(filteredList, query)
                        }
                    }
                    .addOnFailureListener {
                        processedCount++
                        if (processedCount == tempList.size) {
                            fetchHistoryAndShow(filteredList, query)
                        }
                    }
            }
        }
    }

    private fun fetchHistoryAndShow(filteredExams: List<CreateQuestions>, query: String) {
        examList.clear()
        examList.addAll(filteredExams)
        
        if (examList.isNotEmpty()) {
            db.collection("History").document(user).collection("HistoryDetails")
                .whereEqualTo("exam_id", query)
                .get()
                .addOnSuccessListener { historyDocs ->
                    examDataList.clear()
                    for (doc in historyDocs) {
                        val answerKey = doc.toObject(AnswerKey::class.java)
                        examDataList.add(answerKey)
                    }
                    updateAdapters()
                }
                .addOnFailureListener {
                    updateAdapters()
                }
        } else {
            updateAdapters()
        }
    }

    private fun updateAdapters() {
        examDetailsAdapter = ExamDetailsAdapter(this, examList, examDataList)
        userSearchAdapter = UserSearchAdapter(this, userResultList, userResultIds)
        
        concatAdapter = ConcatAdapter(userSearchAdapter, examDetailsAdapter)
        rv_exam_data.adapter = concatAdapter

        if (examList.isEmpty() && userResultList.isEmpty()) {
            tvNoResults.visibility = View.VISIBLE
        } else {
            tvNoResults.visibility = View.GONE
        }
    }
}
