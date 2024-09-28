// QuestionsPagerAdapter.kt
package com.example.testmaster.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.testmaster.FragmentQuestion
import com.example.testmaster.model.Question

class QuestionsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val questions: List<Question>
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentMap: MutableMap<Int, FragmentQuestion> = mutableMapOf()

    override fun getItemCount(): Int = questions.size

    override fun createFragment(position: Int): Fragment {
        val fragment = FragmentQuestion.newInstance(questions[position], position, itemCount)
        fragmentMap[position] = fragment
        return fragment
    }

}
