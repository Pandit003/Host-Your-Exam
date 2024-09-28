package com.example.testmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.adapter.SavedQuestionsAdapter
import com.example.testmaster.model.model_savedQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedQuestions : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    private lateinit var rv_saved_quesions : RecyclerView
    private lateinit var iv_home : ImageView
    var SavedQuestionsList : MutableList<model_savedQuestion> = mutableListOf()
    lateinit var savedQuestionsAdapter : SavedQuestionsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_questions)
        rv_saved_quesions = findViewById(R.id.rv_saved_quesions)
        iv_home = findViewById(R.id.iv_home)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        getSavedQuestions()
        savedQuestionsAdapter = SavedQuestionsAdapter(this,SavedQuestionsList)
        iv_home.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
    fun getSavedQuestions() {
        db.collection("SavedQuestion").document(user)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    SavedQuestionsList.clear()

                    // Assuming the document contains a list of saved questions
                    val questionsList = documentSnapshot.get("savedQuestionsList") as? List<Map<String, Any>>

                    if (questionsList != null) {
                        // Convert each map to QuestionWithAns object
                        val questionObjects = questionsList.mapNotNull { questionMap ->
                            try {
                                model_savedQuestion(
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
                        SavedQuestionsList.addAll(questionObjects)
                    }
                    savedQuestionsAdapter.notifyDataSetChanged() // Notify the adapter of the changes
                    rv_saved_quesions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv_saved_quesions.adapter = savedQuestionsAdapter
                } else {
                    Log.d("Firestore", "No document found")
                }
            }
    }


}