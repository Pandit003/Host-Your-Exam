package com.example.testmaster.model

import java.io.Serializable

data class model_savedQuestion(
    var id: String? = null,
    var subject_name: String? = null,
    var question_no: String? = null,
    var question_text: String? = null,
    var option_a: String? = null,
    var option_b: String? = null,
    var option_c: String? = null,
    var option_d: String? = null,
    var choosen_answer: String? = null,
    var correct_answer: String? = null,
    var question_time: String? = null,
    var report: String? = null,
    var saved: String? = null,
    var marked: String? = null
) : Serializable {
    constructor() : this(
        id = null,
        subject_name = null,
        question_no = null,
        question_text = null,
        option_a = null,
        option_b = null,
        option_c = null,
        option_d = null,
        choosen_answer = null,
        correct_answer = null,
        question_time = null,
        report = null,
        saved = null,
        marked = null
    )
}
