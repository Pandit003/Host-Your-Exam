package com.example.testmaster.model

import java.io.Serializable

data class AnswerKey(
    val candidate_id: String? = null,
    val exam_id: String? = null,
    val sub_nm: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val exam_avl_time: String? = null,
    val exam_duration: String? = null,
    val exam_remaining_time: String? = null,
    val pos_mark: String? = null,
    val neg_mark: String? = null,
    val pass_mark: String? = null,
    val hosting_date: String? = null,
    val hosted_by: String? = null,
    val appear_by: String? = null,
    val attempt_date: String? = null,
    val no_of_attempt: String? = null,
    val exam_complete_duration: String? = null,
    val exam_status: String? = null,
    val total_score: String? = null,
    val correct_question: String? = null,
    val incorrect_question: String? = null,
    val unattempt: String? = null,
    val accuracy: String? = null,
    val questionsWithAns: List<QuestionWithAns>? = null
) : Serializable{
    // Empty constructor for Firebase
    constructor() : this(
        candidate_id = null,
        exam_id = null,
        sub_nm = null,
        start_time = null,
        end_time = null,
        exam_avl_time = null,
        exam_duration = null,
        exam_remaining_time = null,
        pos_mark = null,
        neg_mark = null,
        pass_mark = null,
        hosting_date = null,
        hosted_by = null,
        appear_by = null,
        attempt_date = null,
        no_of_attempt = null,
        exam_complete_duration = null,
        exam_status = null,
        total_score = null,
        correct_question = null,
        incorrect_question = null,
        unattempt = null,
        accuracy = null,
        questionsWithAns = null
    )
}

data class QuestionWithAns(
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
    var report_title: String? = null,
    var report_description: String? = null,
    var saved: String? = null,
    var marked: String? = null
) : Serializable {
    // Empty constructor for Firebase
    constructor() : this(
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
        report_title = null,
        report_description = null,
        saved = null,
        marked = null
    )
}
