package com.example.testmaster

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white))
        loadSearchHistory()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView  // Safe cast to SearchView


        searchView?.queryHint = "Search Exam Id e.g. 111111"


        // Customize SearchView
        searchView?.setBackgroundColor(resources.getColor(android.R.color.transparent))

        // Find and customize the SearchAutoComplete
        val searchAutoComplete = searchView?.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete?.setTextColor(resources.getColor(android.R.color.white))
        searchAutoComplete?.setHintTextColor(resources.getColor(android.R.color.white))

        searchAutoComplete?.setAdapter(searchHistoryAdapter)

        searchAutoComplete?.setOnItemClickListener { parent, view, position, id ->
            val query = parent.getItemAtPosition(position) as String
            searchView.setQuery(query, true)  // Set the query and submit it
        }

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    saveSearchQuery(it)
                    searchExams(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchHistoryAdapter.filter.filter(newText)
                    searchExams(it)
                }
                return false
            }
        })
        searchItem?.expandActionView()
        searchView?.requestFocus()

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                // Do something when the search view is expanded
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                onBackPressed()
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
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