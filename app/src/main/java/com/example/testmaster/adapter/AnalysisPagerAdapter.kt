package com.example.testmaster.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.testmaster.FragmentAnalysis
import com.example.testmaster.FragmentQuestion
import com.example.testmaster.model.QuestionWithAns

class AnalysisPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val questionswithAnswer: List<QuestionWithAns>
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentMap: MutableMap<Int, FragmentAnalysis> = mutableMapOf()

    override fun getItemCount(): Int = questionswithAnswer.size

    override fun createFragment(position: Int): Fragment {
        val fragment = FragmentAnalysis.newInstance(questionswithAnswer[position], position, itemCount)
        fragmentMap[position] = fragment
        return fragment
    }

}
