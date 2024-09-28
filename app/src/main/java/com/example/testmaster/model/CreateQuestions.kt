package com.example.testmaster.model

import java.io.Serializable

data class CreateQuestions(
    val candidate_id: String? = null,
    val exam_id: String? = null,
    val sub_nm: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val exam_avl_time: String? = null,
    val exam_duration: String? = null,
    val pos_mark: String? = null,
    val neg_mark: String? = null,
    val pass_mark: String? = null,
    val hosting_date: String? = null,
    val hosted_by: String? = null,
    val questions: List<Question>? = null
) : Serializable {
    // Empty constructor for Firebase
    constructor() : this(
        candidate_id = null,
        exam_id = null,
        sub_nm = null,
        start_time = null,
        end_time = null,
        exam_avl_time = null,
        exam_duration = null,
        pos_mark = null,
        neg_mark = null,
        pass_mark = null,
        hosting_date = null,
        hosted_by = null,
        questions = null
    )
}

data class Question(
    val question_no: String? = null,
    val question_text: String? = null,
    val option_a: String? = null,
    val option_b: String? = null,
    val option_c: String? = null,
    val option_d: String? = null,
    val correct_answer: String? = null
) : Serializable {
    // Empty constructor for Firebase
    constructor() : this(
        question_no = null,
        question_text = null,
        option_a = null,
        option_b = null,
        option_c = null,
        option_d = null,
        correct_answer = null
    )
}
