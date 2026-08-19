package com.example.testmaster.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var registration: ListenerRegistration? = null

    private lateinit var tvOverallPercentage: TextView
    private lateinit var progressOverall: LinearProgressIndicator
    private lateinit var lineChart: LineChart
    private lateinit var tvExamsCount: TextView
    private lateinit var tvBestScore: TextView
    private lateinit var tvQuestionsCount: TextView
    private lateinit var tvAccuracy: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        tvOverallPercentage = view.findViewById(R.id.tv_overall_percentage)
        progressOverall = view.findViewById(R.id.progress_overall)
        lineChart = view.findViewById(R.id.line_chart)
        tvExamsCount = view.findViewById(R.id.tv_exams_count)
        tvBestScore = view.findViewById(R.id.tv_best_score)
        tvQuestionsCount = view.findViewById(R.id.tv_questions_count)
        tvAccuracy = view.findViewById(R.id.tv_accuracy)

        setupChart()
        fetchProgressData()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registration?.remove()
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            legend.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = resources.getColor(R.color.onSurfaceVariant)
                granularity = 1f
            }

            axisLeft.apply {
                textColor = resources.getColor(R.color.onSurfaceVariant)
                setDrawGridLines(true)
                gridColor = resources.getColor(R.color.emerald)
                axisMinimum = 0f
                axisMaximum = 100f
            }

            axisRight.isEnabled = false
            animateX(1000)
        }
    }

    fun fetchProgressData() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userId = firebaseAuth.currentUser?.uid ?: ""
        if (userId.isEmpty()) return

        registration = db.collection("History").document(userId).collection("HistoryDetails")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("ProgressFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (!isAdded) return@addSnapshotListener

                if (documents != null && !documents.isEmpty) {
                    val historyList = mutableListOf<AnswerKey>()
                    var totalScore = 0f
                    var totalMaxMarks = 0f
                    var highestPercentage = 0f
                    var totalQuestions = 0
                    var totalCorrect = 0

                    for (doc in documents) {
                        val exam = doc.toObject(AnswerKey::class.java)
                        historyList.add(exam)
                        
                        val score = exam.total_score?.toFloatOrNull() ?: 0f
                        val qCount = exam.questionsWithAns?.size ?: 0
                        val posMark = exam.pos_mark?.toFloatOrNull() ?: 1f
                        val maxMark = qCount * posMark
                        
                        totalScore += score
                        totalMaxMarks += maxMark
                        totalQuestions += qCount
                        
                        // Estimating correct answers from score for accuracy
                        // This is an approximation if neg_mark is used
                        val correct = (score / posMark).toInt() 
                        totalCorrect += if (correct > 0) correct else 0

                        if (maxMark > 0) {
                            val percent = (score / maxMark) * 100
                            if (percent > highestPercentage) highestPercentage = percent
                        }
                    }
                    val avgPercentage = if (totalMaxMarks > 0) (totalScore / totalMaxMarks * 100).toInt() else 0
                    val sharedPref = context?.getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    sharedPref?.edit()?.apply {
                        putString("totalExams", documents.size().toString())
                        putString("avgPercentage", avgPercentage.toString())
                        putString("highestPercentage", highestPercentage.toString())
                        apply()
                    }

                    // Sync to Firestore personalDetails
                    val updates = hashMapOf<String, Any>(
                        "totalExams" to documents.size().toString(),
                        "avgPercentage" to avgPercentage.toString(),
                        "highestPercentage" to highestPercentage.toInt().toString()
                    )
                    db.collection("personalDetails").document(userId).update(updates)
                        .addOnFailureListener { e ->
                            Log.e("ProgressFragment", "Failed to sync stats to Firestore", e)
                        }
                    // Sort by date for the chart
                    val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    historyList.sortBy { exam ->
                        exam.attempt_date?.let { sdf.parse(it) } ?: Date(0)
                    }

                    updateUI(historyList, totalScore, totalMaxMarks, highestPercentage, totalQuestions, totalCorrect)
                }
            }
    }

    private fun updateUI(
        historyList: List<AnswerKey>,
        totalScore: Float,
        totalMaxMarks: Float,
        highestPercentage: Float,
        totalQuestions: Int,
        totalCorrect: Int
    ) {
        val avgPercentage = if (totalMaxMarks > 0) (totalScore / totalMaxMarks * 100).toInt() else 0
        
        tvOverallPercentage.text = "$avgPercentage%"
        progressOverall.progress = avgPercentage
        tvExamsCount.text = historyList.size.toString()
        tvBestScore.text = "${highestPercentage.toInt()}%"
        tvQuestionsCount.text = totalQuestions.toString()
        
        val accuracy = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions * 100).toInt() else 0
        tvAccuracy.text = "$accuracy%"

        // Chart Data
        val entries = mutableListOf<Entry>()
        historyList.forEachIndexed { index, exam ->
            val score = exam.total_score?.toFloatOrNull() ?: 0f
            val maxMark = (exam.questionsWithAns?.size ?: 0) * (exam.pos_mark?.toFloatOrNull() ?: 1f)
            val percent = if (maxMark > 0) (score / maxMark) * 100 else 0f
            entries.add(Entry(index.toFloat(), percent))
        }

        val dataSet = LineDataSet(entries, "Performance").apply {
            color = resources.getColor(R.color.emerald)
            setCircleColor(resources.getColor(R.color.emerald))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleColor = resources.getColor(R.color.emerald_bg)
            valueTextSize = 0f
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.emerald)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
}
