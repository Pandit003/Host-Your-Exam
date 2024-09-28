package com.example.testmaster

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.testmaster.adapter.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.provider.Settings



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
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
            val intent = Intent(this,SearchExamId::class.java)
            startActivity(intent)
        }
        ib_notification.setOnClickListener{
            startActivity(Intent(this,NotificationActivity::class.java))
        }

//        loadFragment(HomeFragment(), "Home")

        viewPager = findViewById(R.id.viewPager)

        val fragmentList = listOf(
            HomeFragment(),
            CreateTestFragment(),
            HistoryFragment(),
            LeaderBoardFragment()
        )

        val adapter = ViewPagerAdapter(this, fragmentList)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
                toolbarTitle.text = when (position) {
                    0 -> "Home"
                    1 -> "Create Exam"
                    2 -> "History"
                    3 -> "Leaderboard"
                    else -> "Home"
                }
            }
        })

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_create_exam -> viewPager.currentItem = 1
                R.id.nav_history -> viewPager.currentItem = 2
                R.id.nav_result -> viewPager.currentItem = 3
            }
            true
        }

        menuDrawer.setOnClickListener(){
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    firebaseAuth.signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                R.id.hosted_exam -> {
                    val intent = Intent(this, HostedTest::class.java)
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
            startActivity(Intent(this,EditProfileActivity::class.java))
        }
        if (userId != null) {
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

    private fun loadFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
        toolbarTitle.text = title
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
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("It looks like you have no internet connection. Please turn on Wi-Fi or mobile data to continue.")
            .setPositiveButton("Settings") { dialogInterface: DialogInterface, _: Int ->
                // Open network settings
                startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                // Exit the app or do nothing
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }

}