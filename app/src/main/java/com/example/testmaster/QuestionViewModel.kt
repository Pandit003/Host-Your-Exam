package com.example.testmaster

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuestionViewModel : ViewModel() {
    val selectedOption = MutableLiveData<String>()

    // Other shared data like question time, marked, reported status
    val questionTime = MutableLiveData<Long>()
    val isMarked = MutableLiveData<Boolean>()
    val isSaved = MutableLiveData<Boolean>()
    val isReported = MutableLiveData<Boolean>()
}
