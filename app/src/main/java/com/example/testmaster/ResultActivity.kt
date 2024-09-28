package com.example.testmaster

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testmaster.adapter.ExamDetailsAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResultActivity : AppCompatActivity() {
    lateinit var btn_analysis : TextView
    lateinit var total_marks : TextView
    lateinit var correct_question : TextView
    lateinit var incorrect_question : TextView
    lateinit var unattempt_question : TextView
    lateinit var accuracy : TextView
    lateinit var percentage : TextView
    lateinit var tv_reattempt : TextView
    lateinit var ib_home : ImageButton
    lateinit var examData : CreateQuestions

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val answerKey = intent.getSerializableExtra("Answer_Key") as AnswerKey
        btn_analysis = findViewById(R.id.btn_analysis)
        total_marks = findViewById(R.id.total_marks)
        correct_question = findViewById(R.id.correct_question)
        incorrect_question = findViewById(R.id.incorrect_question)
        unattempt_question = findViewById(R.id.unattempt_question)
        accuracy = findViewById(R.id.accuracy)
        percentage = findViewById(R.id.percentage)
        tv_reattempt = findViewById(R.id.tv_reattempt)
        ib_home = findViewById(R.id.ib_home)

        val accuracyPercent = ((answerKey.total_score?.toDouble() ?: 0.0) /
                (answerKey.questionsWithAns?.size?.toDouble() ?: 1.0).times(answerKey.pos_mark?.toDouble()?:1.0)) * 100

        total_marks.text = ": ${answerKey.total_score}"
        correct_question.text = ": ${answerKey.correct_question}"
        incorrect_question.text = ": ${answerKey.incorrect_question}"
        unattempt_question.text = ": ${answerKey.unattempt}"
        accuracy.text = ": "+String.format("%.2f", answerKey.accuracy?.toFloat()) + "%"
        percentage.text = ": "+String.format("%.2f", accuracyPercent.toFloat()) + "%"

        btn_analysis.setOnClickListener{
            val intent = Intent(this@ResultActivity,Analysis_Exam::class.java)
            intent.putExtra("Answer_Key", answerKey)
            startActivity(intent)
        }
        ib_home.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        db.collection("Exams")
            .whereEqualTo("exam_id", answerKey.exam_id)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    examData = documents.documents[0].toObject(CreateQuestions::class.java)!!
                } else {
                    Toast.makeText(this, "No exam data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Unable To Take Re-attempt", Toast.LENGTH_SHORT).show()
            }
        tv_reattempt.setOnClickListener{
            val intent = Intent(this,Attempt_Exam::class.java)
            intent.putExtra("examData",examData)
            startActivity(intent)
            finish()
        }
    }
}