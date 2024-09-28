package com.example.testmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.adapter.HostedTestAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HostedTest : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    private lateinit var rv_hosted_test : RecyclerView
    var hostedTestList : MutableList<CreateQuestions> = mutableListOf()
    lateinit var hostedTestAdapter : HostedTestAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var iv_home: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hosted_test)
        rv_hosted_test = findViewById(R.id.rv_hosted_test)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        iv_home = findViewById(R.id.iv_home)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        hostedTestAdapter = HostedTestAdapter(this,hostedTestList)
        iv_home.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        swipeRefreshLayout.setOnRefreshListener {
            getHostedList()
        }
        getHostedList()
    }
    fun getHostedList(){

        db.collection("CreatedQuestion").document(user).collection("QuestionsDetails")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documents != null && !documents.isEmpty) {
                    hostedTestList.clear()  // Clear the list before adding updated data
                    for (document in documents) {
                        val hostedTest = document.toObject(CreateQuestions::class.java)
                        hostedTestList.add(hostedTest)
                    }
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    hostedTestList.sortWith { a, b ->
                        val dateA = originalFormat.parse(a.hosting_date)
                        val dateB = originalFormat.parse(b.hosting_date)
                        dateB.compareTo(dateA)  // Sort in descending order
                    }
                    hostedTestAdapter.notifyDataSetChanged() // Notify the adapter of the changes
                    swipeRefreshLayout.isRefreshing = false
                    rv_hosted_test.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                    rv_hosted_test.adapter = hostedTestAdapter
                } else {
                    Log.d("Firestore", "No data found")
                    swipeRefreshLayout.isRefreshing = false
                }
            }
    }
}