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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.ExamDetailsAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchExamId : AppCompatActivity() {

    lateinit var rv_exam_data : RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchHistoryAdapter: ArrayAdapter<String>
    var examDataList: MutableList<AnswerKey> = mutableListOf()
    lateinit var user : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_exam_id)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        rv_exam_data = findViewById(R.id.rv_exam_data)

        sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE)

        val root: View = findViewById(R.id.search_root)
        val etSearch: AutoCompleteTextView = findViewById(R.id.et_search)
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
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrEmpty()
                saveSearchQuery(s.toString())
                searchExams(s.toString())
                ivClear.visibility = if (hasText) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                searchExams(query)
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
    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(view: View) {
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
        val maxHistorySize = 10 // Maximum number of search queries to keep
        val searchHistory = sharedPreferences.getStringSet("history", mutableSetOf())?.toMutableSet()

        if (searchHistory != null) {
            // Add the new query if it doesn't already exist
            if (searchHistory.contains(query)) {
                searchHistory.remove(query)
            }
            searchHistory.add(query)

            // Trim the history if it exceeds the maximum size
            if (searchHistory.size > maxHistorySize) {
                val excessCount = searchHistory.size - maxHistorySize
                val iterator = searchHistory.iterator()
                repeat(excessCount) {
                    if (iterator.hasNext()) iterator.next().let { iterator.remove() }
                }
            }

            // Save the updated history
            sharedPreferences.edit().putStringSet("history", searchHistory).apply()
            loadSearchHistory() // Reload the search history to update the dropdown
        }
    }


    fun searchExams(query: String) {
        var exam_data : CreateQuestions
        val db = FirebaseFirestore.getInstance()
        db.collection("Exams")
            .whereEqualTo("exam_id", query)
            .get()
            .addOnSuccessListener { documents ->
                val examList = mutableListOf<CreateQuestions>()
                if (!documents.isEmpty) {
                    // Handle no results found
                    for (document in documents) {
                        exam_data = document.toObject(CreateQuestions::class.java)
                        examList.add(exam_data)
                    }
                    saveSearchQuery(query)
                    db.collection("History").document(user).collection("HistoryDetails").whereEqualTo("exam_id",query)
                        .addSnapshotListener { documents, error ->
                            if (error != null) {
                                Log.w("Firestore", "Listen failed.", error)
                                return@addSnapshotListener
                            }

                            if (documents != null && !documents.isEmpty) {
                                examDataList.clear()  // Clear the list before adding updated data
                                for (document in documents) {
                                    val answerKey = document.toObject(AnswerKey::class.java)
                                    examDataList.add(answerKey)
                                }
                            } else {
                                Log.d("Firestore", "No data found")
                            }
                        }
                    // Initialize the adapter with a list of exams
                    val examDetailsAdapter = ExamDetailsAdapter(this, examList,examDataList)
                    rv_exam_data.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv_exam_data.adapter = examDetailsAdapter
                }else{
                    val examDetailsAdapter = ExamDetailsAdapter(this, examList,examDataList)
                    rv_exam_data.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv_exam_data.adapter = examDetailsAdapter
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching exams", Toast.LENGTH_SHORT).show()
            }
    }
    fun getHistoryList(){
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()


    }
}