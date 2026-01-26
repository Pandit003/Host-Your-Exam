package com.example.testmaster.model

import androidx.lifecycle.ViewModel

class ExamViewModel : ViewModel() {
    val questionWithAnsList: MutableList<QuestionWithAns> = mutableListOf()
    val chosenAnswerList: MutableList<String?> = mutableListOf()

    // Initialize lists based on the number of questions
    fun initializeLists(questionCount: Int) {
        for (i in 0 until questionCount) {
            questionWithAnsList.add(QuestionWithAns())
            chosenAnswerList.add(null)
        }
    }
}
