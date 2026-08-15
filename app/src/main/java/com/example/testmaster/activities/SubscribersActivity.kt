package com.example.testmaster.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaster.R
import com.example.testmaster.adapter.SubscribersPagerAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class SubscribersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribers)

        val toolbar: MaterialToolbar = findViewById(R.id.subscribers_toolbar)
        val tabLayout: TabLayout = findViewById(R.id.subscribers_tabs)
        val viewPager: ViewPager2 = findViewById(R.id.subscribers_viewpager)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val adapter = SubscribersPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Followers" else "Following"
        }.attach()
    }
}
