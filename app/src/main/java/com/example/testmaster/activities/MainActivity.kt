package com.example.testmaster.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.testmaster.adapter.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testmaster.fragments.CreateTestFragment
import com.example.testmaster.fragments.HistoryFragment
import com.example.testmaster.fragments.HomeFragment
import com.example.testmaster.fragments.LeaderBoardFragment
import com.example.testmaster.fragments.ProgressFragment
import com.example.testmaster.R
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class MainActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var menuDrawer : ImageButton
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var toolbarTitle: TextView
    lateinit var user_name: TextView
    lateinit var user_email: TextView
    lateinit var view_profile: TextView
    lateinit var iv_userimage: ImageView
    lateinit var viewPager : ViewPager2
    lateinit var search_id : ImageButton
    lateinit var ib_notification : ImageButton
    lateinit var db : FirebaseFirestore

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.getCurrentUser()
        if (currentUser == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else if (!currentUser.isEmailVerified) {
            firebaseAuth.signOut()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerlayout)
        navigationView = findViewById(R.id.navigation_view)
        menuDrawer = findViewById(R.id.menudrawer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        toolbarTitle = findViewById(R.id.toolbar_title)
        search_id = findViewById(R.id.search_id)
        ib_notification = findViewById(R.id.ib_notification)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (!isConnectedToInternet()) {
            showInternetSettingsDialog()
        }
        search_id.setOnClickListener{
            val intent = Intent(this, SearchExamId::class.java)
            startActivity(intent)
        }
        ib_notification.setOnClickListener{
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        viewPager = findViewById(R.id.viewPager)

        val fragmentList = listOf(
            HomeFragment(),
            CreateTestFragment(),
            ProgressFragment(),
            HistoryFragment(),
            LeaderBoardFragment()
        )

        val adapter = ViewPagerAdapter(this, fragmentList)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
                toolbarTitle.text = when (position) {
                    0 -> "Home"
                    1 -> "Create Exam"
                    2 -> "Progress"
                    3 -> "History"
                    4 -> "Leaderboard"
                    else -> "Home"
                }
            }
        })

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_create_exam -> viewPager.currentItem = 1
                R.id.nav_progress -> viewPager.currentItem = 2
                R.id.nav_history -> viewPager.currentItem = 3
                R.id.nav_result -> viewPager.currentItem = 4
            }
            true
        }

        menuDrawer.setOnClickListener(){
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.hosted_exam -> {
                    val intent = Intent(this, HostedTest::class.java)
                    startActivity(intent)
                }
                R.id.menu_post_announcement -> {
                    val intent = Intent(this, PostAnnouncementActivity::class.java)
                    startActivity(intent)
                }
                R.id.menu_saved -> {
                    val intent = Intent(this, SavedQuestions::class.java)
                    startActivity(intent)
                }
                R.id.menu_report -> {
                    val intent = Intent(this, ReportedQuestion::class.java)
                    startActivity(intent)
                }
                R.id.menu_feedback -> {
                    val intent = Intent(this, FeedbackActivity::class.java)
                    startActivity(intent)
                }
                R.id.info -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
        val userId = firebaseAuth.currentUser?.uid
        var username = "N/A"
        var imageUrl = ""
        val headerView = navigationView.getHeaderView(0)
        user_name = headerView.findViewById(R.id.user_name)
        user_email = headerView.findViewById(R.id.user_email)
        view_profile = headerView.findViewById(R.id.view_profile)
        iv_userimage = headerView.findViewById(R.id.iv_userimage)
        view_profile.setOnClickListener{
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        if (userId != null) {
            syncSubscriberCount(userId)
            db.collection("personalDetails").document(userId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.w("TAG", "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        username = documentSnapshot.getString("name").toString()
                        imageUrl = documentSnapshot.getString("imageUrl").toString()
                        user_name.text = username
                        user_email.text = firebaseAuth.currentUser?.email.toString()

                        if(!imageUrl.isNullOrEmpty()){
                            Picasso.get()
                                .load(imageUrl).fit()
                                .into(iv_userimage);
                        }
                    } else {
                        Log.d("TAG", "Current data: null")
                    }
                }
        }
        val openFragment = intent.getStringExtra("open_fragment")
        if (openFragment == "progress") {
            viewPager.currentItem = 2
            bottomNavigationView.selectedItemId = R.id.nav_progress
            toolbarTitle.text = "Progress"
        }
    }
    private fun syncSubscriberCount(userId: String) {
        db.collection("Subscribers").document(userId)
            .collection("UserSubscribers")
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                db.collection("personalDetails").document(userId)
                    .update("subscribersCount", count)
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Failed to sync subscribersCount", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to fetch actual subscribers", e)
            }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem = 0
            drawerLayout.close()
            bottomNavigationView.menu.getItem(0).isChecked = true
            toolbarTitle.text = "Home"
        } else {
            super.onBackPressed()
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Method to show dialog prompting the user to enable internet
    private fun showInternetSettingsDialog() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            positiveText = "Retry",
            negativeText = "Cancel",
            onPositive = {
                this.recreate()
            },
            onNegative = {

            }

        )
    }

}
