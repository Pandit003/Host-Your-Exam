package com.example.testmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.adapter.UserReportedQuestion
import com.example.testmaster.adapter.YourReportedQuestion
import com.example.testmaster.model.model_reportedQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportedQuestion : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    private lateinit var rv_user_reported_quesions : RecyclerView
    private lateinit var rv_your_reported_quesions : RecyclerView
    private lateinit var iv_home : ImageView
    private lateinit var iv_down_user : ImageView
    private lateinit var iv_down_your : ImageView
    private lateinit var ll_down_user : LinearLayout
    private lateinit var ll_down_your : LinearLayout
    var userReportedQuestionsList : MutableList<model_reportedQuestion> = mutableListOf()
    var yourReportedQuestionsList : MutableList<model_reportedQuestion> = mutableListOf()
    lateinit var userReportedQuestionAdapter: UserReportedQuestion
    lateinit var yourReportedQuestionAdapter: YourReportedQuestion
    var isDown_user : Boolean = false
    var isDown_your : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reported_question)
        rv_user_reported_quesions = findViewById(R.id.rv_user_reported_quesions)
        rv_your_reported_quesions = findViewById(R.id.rv_your_reported_quesions)
        iv_home = findViewById(R.id.iv_home)
        iv_down_user = findViewById(R.id.iv_down_user)
        iv_down_your = findViewById(R.id.iv_down_your)
        ll_down_user = findViewById(R.id.ll_down_user)
        ll_down_your = findViewById(R.id.ll_down_your)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        iv_home.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        ll_down_user.setOnClickListener {
            if (!isDown_user){
                isDown_user = !isDown_user
                rv_user_reported_quesions.visibility = View.VISIBLE
                iv_down_user.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
            }else{
                isDown_user = !isDown_user
                rv_user_reported_quesions.visibility = View.GONE
                iv_down_user.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }

        }
        ll_down_your.setOnClickListener {
            if (!isDown_your){
                isDown_your = !isDown_your
                rv_your_reported_quesions.visibility = View.VISIBLE
                iv_down_your.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
            }else{
                isDown_your = !isDown_your
                rv_your_reported_quesions.visibility = View.GONE
                iv_down_your.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }

        }
        getUserReportedQuestions()
        getYourReportedQuestions()
        userReportedQuestionAdapter = UserReportedQuestion(this,userReportedQuestionsList)
        yourReportedQuestionAdapter = YourReportedQuestion(this,yourReportedQuestionsList)
        Log.d("size", "${userReportedQuestionsList.size}, ${yourReportedQuestionsList.size}")
    }
    fun getUserReportedQuestions() {
        db.collection("ReportedQuestion").document(user).collection("Reported Questions").document("user Reported Question")
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    userReportedQuestionsList.clear()

                    val questionsList = documentSnapshot.get("reportQuestionsList") as? List<Map<String, Any>>

                    if (questionsList != null) {
                        // Convert each map to QuestionWithAns object
                        val questionObjects = questionsList.mapNotNull { questionMap ->
                            try {
                                model_reportedQuestion(
                                    id = questionMap["id"] as? String,
                                    subject_name = questionMap["subject_name"] as? String,
                                    question_no = questionMap["question_no"] as? String,
                                    question_text = questionMap["question_text"] as? String,
                                    option_a = questionMap["option_a"] as? String,
                                    option_b = questionMap["option_b"] as? String,
                                    option_c = questionMap["option_c"] as? String,
                                    option_d = questionMap["option_d"] as? String,
                                    choosen_answer = questionMap["choosen_answer"] as? String,
                                    correct_answer = questionMap["correct_answer"] as? String,
                                    question_time = questionMap["question_time"] as? String,
                                    report = questionMap["report"] as? String,
                                    report_title = questionMap["report_title"] as? String,
                                    report_description = questionMap["report_description"] as? String,
                                    saved = questionMap["saved"] as? String,
                                    marked = questionMap["marked"] as? String
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "Firestore",
                                    "Error converting map to QuestionWithAns: ${e.message}"
                                )
                                null
                            }
                        }
                        userReportedQuestionsList.addAll(questionObjects)
                    }
                    userReportedQuestionAdapter.notifyDataSetChanged() // Notify the adapter of the changes
                    rv_user_reported_quesions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv_user_reported_quesions.adapter = userReportedQuestionAdapter
                } else {
                    Log.d("Firestore", "No document found")
                }
            }
    }
    fun getYourReportedQuestions() {
        db.collection("ReportedQuestion").document(user).collection("Report Questions").document("your Reported Question")
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    yourReportedQuestionsList.clear()

                    val questionsList = documentSnapshot.get("reportQuestionsList") as? List<Map<String, Any>>

                    if (questionsList != null) {
                        // Convert each map to QuestionWithAns object
                        val questionObjects = questionsList.mapNotNull { questionMap ->
                            try {
                                model_reportedQuestion(
                                    id = questionMap["id"] as? String,
                                    subject_name = questionMap["subject_name"] as? String,
                                    question_no = questionMap["question_no"] as? String,
                                    question_text = questionMap["question_text"] as? String,
                                    option_a = questionMap["option_a"] as? String,
                                    option_b = questionMap["option_b"] as? String,
                                    option_c = questionMap["option_c"] as? String,
                                    option_d = questionMap["option_d"] as? String,
                                    choosen_answer = questionMap["choosen_answer"] as? String,
                                    correct_answer = questionMap["correct_answer"] as? String,
                                    question_time = questionMap["question_time"] as? String,
                                    report = questionMap["report"] as? String,
                                    report_title = questionMap["report_title"] as? String,
                                    report_description = questionMap["report_description"] as? String,
                                    saved = questionMap["saved"] as? String,
                                    marked = questionMap["marked"] as? String
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "Firestore",
                                    "Error converting map to QuestionWithAns: ${e.message}"
                                )
                                null
                            }
                        }
                        yourReportedQuestionsList.addAll(questionObjects)
                    }
                    yourReportedQuestionAdapter.notifyDataSetChanged() // Notify the adapter of the changes
                    rv_your_reported_quesions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv_your_reported_quesions.adapter = yourReportedQuestionAdapter
                } else {
                    Log.d("Firestore", "No document found")
                }
            }
    }

}