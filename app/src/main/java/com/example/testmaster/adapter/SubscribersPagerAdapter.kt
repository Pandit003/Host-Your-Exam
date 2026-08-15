package com.example.testmaster.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.testmaster.fragments.SubscribersListFragment

class SubscribersPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SubscribersListFragment.newInstance("FOLLOWERS")
            else -> SubscribersListFragment.newInstance("FOLLOWING")
        }
    }
}
