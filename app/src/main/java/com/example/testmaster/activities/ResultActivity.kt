package com.example.testmaster.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.testmaster.R
import com.example.testmaster.fragments.ProgressFragment
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResultActivity : AppCompatActivity() {
    lateinit var btn_analysis : MaterialButton
    lateinit var total_marks : TextView
    lateinit var correct_question : TextView
    lateinit var incorrect_question : TextView
    lateinit var unattempt_question : TextView
    lateinit var accuracy : TextView
    lateinit var percentage : TextView
    lateinit var tv_reattempt : MaterialButton
    lateinit var tv_progress : MaterialButton
    lateinit var examData : CreateQuestions

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        
        val answerKey = intent.getSerializableExtra("Answer_Key") as AnswerKey
        examData = intent.getSerializableExtra("examData") as CreateQuestions

        btn_analysis = findViewById(R.id.btn_analysis)
        total_marks = findViewById(R.id.total_marks)
        percentage = findViewById(R.id.percentage)
        tv_reattempt = findViewById(R.id.tv_reattempt)
        tv_progress = findViewById(R.id.tv_progress)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setTint(getColor(R.color.onPrimary))

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

        btn_analysis.setOnClickListener{
            val intent = Intent(this@ResultActivity,Analysis_Exam::class.java)
            intent.putExtra("Answer_Key", answerKey)
            startActivity(intent)
        }
        tv_reattempt.setOnClickListener{
            val intent = Intent(this@ResultActivity,Attempt_Exam::class.java)
            intent.putExtra("examData",examData)
            startActivity(intent)
            finish()
        }
        tv_progress.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_fragment", "progress")
            startActivity(intent)
            finish()
        }
        ProgressFragment().fetchProgressData()
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

    }

    private fun setupStatCard(id: Int, label: String, tintColor: Int, bgColor: Int) {
        val card = findViewById<MaterialCardView>(id)
        card.findViewById<TextView>(R.id.stat_label).apply {
            text = label
            setTextColor(resources.getColor(tintColor))
        }
        card.findViewById<TextView>(R.id.stat_value).setTextColor(resources.getColor(tintColor))
        card.findViewById<View>(R.id.stat_container).setBackgroundColor(resources.getColor(bgColor))
        card.setStrokeColor(resources.getColor(tintColor))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ProgressFragment()
    }
}