package com.example.testmaster.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.R
import com.example.testmaster.adapter.HostedTestAdapter
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.util.CustomDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class HostedTest : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    private lateinit var rv_hosted_test : RecyclerView
    var hostedTestList : MutableList<CreateQuestions> = mutableListOf()
    lateinit var hostedTestAdapter : HostedTestAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hosted_test)
        rv_hosted_test = findViewById(R.id.rv_hosted_test)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        hostedTestAdapter = HostedTestAdapter(this,hostedTestList)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Your Hosted Exams"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setTint(getColor(R.color.onPrimary))
        swipeRefreshLayout.setOnRefreshListener {
            getHostedList()
        }
        getHostedList()
    }
    fun getHostedList(){
        if (!isInternetAvailable(this)) {
            showNoInternetDialog()
            return
        }
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
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetDialog() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            positiveText = "Retry",
            negativeText = "Exit",
            onPositive = {
                getHostedList()
            },
            onNegative = {
                finish()
            }

        )
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}