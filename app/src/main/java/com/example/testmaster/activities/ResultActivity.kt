package com.example.testmaster.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.testmaster.R
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
        percentage = findViewById(R.id.percentage)
        tv_reattempt = findViewById(R.id.tv_reattempt)
        ib_home = findViewById(R.id.ib_home)

        // Bind included layout stats
        val cardCorrect = findViewById<View>(R.id.card_correct)
        cardCorrect.findViewById<TextView>(R.id.stat_label).text = "Correct"
        correct_question = cardCorrect.findViewById(R.id.stat_value)

        val cardIncorrect = findViewById<View>(R.id.card_incorrect)
        cardIncorrect.findViewById<TextView>(R.id.stat_label).text = "Incorrect"
        incorrect_question = cardIncorrect.findViewById(R.id.stat_value)

        val cardUnattempted = findViewById<View>(R.id.card_unattempted)
        cardUnattempted.findViewById<TextView>(R.id.stat_label).text = "Unattempted"
        unattempt_question = cardUnattempted.findViewById(R.id.stat_value)

        val cardAccuracy = findViewById<View>(R.id.card_accuracy)
        cardAccuracy.findViewById<TextView>(R.id.stat_label).text = "Accuracy"
        accuracy = cardAccuracy.findViewById(R.id.stat_value)

        val accuracyPercent = ((answerKey.total_score?.toDouble() ?: 0.0) /
                ((answerKey.questionsWithAns?.size?.toDouble() ?: 1.0) * (answerKey.pos_mark?.toDouble() ?: 1.0))) * 100

        applyColorfulTheme(accuracyPercent.toFloat())

        total_marks.text = "${answerKey.total_score}"
        correct_question.text = "${answerKey.correct_question}"
        incorrect_question.text = "${answerKey.incorrect_question}"
        unattempt_question.text = "${answerKey.unattempt}"
        accuracy.text = String.format("%.2f", answerKey.accuracy?.toFloat()) + "%"
        percentage.text = String.format("%.2f", accuracyPercent.toFloat()) + "%"
    }

    private fun applyColorfulTheme(percent: Float) {
        val themeColor = when {
            percent >= 80 -> R.color.greentint
            percent >= 60 -> R.color.emerald
            percent >= 40 -> R.color.bluetint
            else -> R.color.orangetint
        }

        val themeBg = when {
            percent >= 80 -> R.color.green_bg
            percent >= 60 -> R.color.emerald_bg
            percent >= 40 -> R.color.blue_bg
            else -> R.color.orange_bg
        }

        val banner = findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_score_banner)
        banner.setCardBackgroundColor(resources.getColor(themeBg))
        banner.setStrokeColor(resources.getColor(themeColor))
        total_marks.setTextColor(resources.getColor(themeColor))

        // Stats cards
        setupStatCard(R.id.card_correct, "Correct", R.color.greentint, R.color.green_bg)
        setupStatCard(R.id.card_incorrect, "Incorrect", R.color.redtint, R.color.red_bg)
        setupStatCard(R.id.card_unattempted, "Unattempted", R.color.purpletint, R.color.purple_bg)
        setupStatCard(R.id.card_accuracy, "Accuracy", themeColor, themeBg)

        // Main buttons
        val btnAnalysis = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_analysis)
        btnAnalysis.setBackgroundColor(resources.getColor(themeColor))
        btnAnalysis.setTextColor(resources.getColor(R.color.white))
        btnAnalysis.setIconTintResource(R.color.white)

        val btnReattempt = findViewById<com.google.android.material.button.MaterialButton>(R.id.tv_reattempt)
        btnReattempt.setStrokeColorResource(themeColor)
        btnReattempt.setTextColor(resources.getColor(themeColor))
    }

    private fun setupStatCard(id: Int, label: String, tintColor: Int, bgColor: Int) {
        val card = findViewById<View>(id)
        card.findViewById<TextView>(R.id.stat_label).apply {
            text = label
            setTextColor(resources.getColor(tintColor))
        }
        card.findViewById<TextView>(R.id.stat_value).setTextColor(resources.getColor(tintColor))
        card.findViewById<View>(R.id.stat_container).setBackgroundColor(resources.getColor(bgColor))
        card.findViewById<com.google.android.material.card.MaterialCardView>(R.id.stat_card_root).setStrokeColor(resources.getColor(tintColor))
    }
}