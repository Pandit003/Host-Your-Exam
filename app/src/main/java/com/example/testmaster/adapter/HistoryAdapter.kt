package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.activities.Analysis_Exam
import com.example.testmaster.activities.Attempt_Exam
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(var context: Context,var examDataList : List<AnswerKey>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    lateinit var questionData : CreateQuestions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_exam_history,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        var total_marks = 0
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val attempt_date = originalFormat.parse(examDataList.get(position).attempt_date)
        val sdf = SimpleDateFormat("dd/MM/YYYY", Locale.ENGLISH)

        val formated_date = sdf.format(attempt_date)
        var exam_status = examDataList.get(position).exam_status
        total_marks = (examDataList.get(position).pos_mark?.toInt()?:0) * (examDataList.get(position).questionsWithAns?.size?: 0)
        val mark_scored = examDataList[position].total_score?.toFloatOrNull() ?: 0f
        if(mark_scored > 0){
            holder.pr_markScored.setProgress(mark_scored?.toInt()?:0,true)
            holder.pr_markScored.max=total_marks
        }else{
            holder.pr_markScored.setProgress(0,true)
            holder.pr_markScored.max=0
        }
        holder.pr_markScored.setProgress(mark_scored?.toInt()?:0,true)
        holder.pr_markScored.max=total_marks
        holder.attempt_date.text = formated_date
        holder.hosted_by.text = "Hosted by : "+examDataList.get(position).hosted_by
        holder.subject_name.text = examDataList.get(position).sub_nm
        if(exam_status.equals("C")){
            holder.exam_status.text = "Completed"
            holder.exam_status.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.green_bg)
            )
            holder.exam_status.setTextColor(context.getColor(R.color.greentint))
        }else{
            holder.exam_status.text = "Paused"
            // Use a different badge color if available
            holder.exam_status.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.red_bg)
            )
            holder.exam_status.setTextColor(context.getColor(R.color.redtint))
        }
        holder.tv_exam_mark.text = "$mark_scored/$total_marks"
        holder.no_of_attempt.text = "Attempt #"+examDataList.get(position).no_of_attempt

        // Enhanced Details
        val exam = examDataList[position]
        val questionsCount = exam.questionsWithAns?.size ?: 0
        holder.tv_total_questions.text = "Questions: $questionsCount"

        if (!exam.start_time.isNullOrEmpty() && !exam.end_time.isNullOrEmpty()) {
            holder.tv_availability_range.visibility = View.VISIBLE
            holder.tv_availability_range.text = "Available: ${exam.start_time} - ${exam.end_time}"

            val startTime = parseExamDate(exam.start_time)
            val endTime = parseExamDate(exam.end_time)
            val now = Date()

            if (endTime != null && now.after(endTime)) {
                holder.ll_exam_availability.visibility = View.VISIBLE
                holder.tv_time_left.text = "Time Out"
                holder.tv_time_left.setTextColor(context.getColor(R.color.redtint))
            } else if (startTime != null && now.before(startTime)) {
                holder.ll_exam_availability.visibility = View.VISIBLE
                val diff = startTime.time - now.time
                holder.tv_time_left.text = "Starts in: ${formatDuration(diff)}"
                holder.tv_time_left.setTextColor(context.getColor(R.color.bluetint))
            } else if (endTime != null) {
                holder.ll_exam_availability.visibility = View.VISIBLE
                val diff = endTime.time - now.time
                holder.tv_time_left.text = "Ends in: ${formatDuration(diff)}"
                holder.tv_time_left.setTextColor(context.getColor(R.color.greentint))
            } else {
                holder.ll_exam_availability.visibility = View.GONE
            }
        } else {
            holder.tv_availability_range.visibility = View.GONE
            holder.ll_exam_availability.visibility = View.GONE
        }

        holder.tv_reattempt.setOnClickListener {
            if (!isInternetAvailable(context)) {
                showNoInternetDialog()
            }else {
                val endTime = parseExamDate(examDataList[position].end_time)
                val now = Date()

                if (endTime != null && now.after(endTime)) {
                    CustomDialogUtils.showAlert(
                        activity = context as android.app.Activity,
                        title = "Attention",
                        message = "Exam time is out! You can no longer re-attempt this exam."
                    )
                    return@setOnClickListener
                }

                getReattempQuestion(examDataList[position].exam_id.toString()) {
                    CustomDialogUtils.showConfirm(
                        activity = context as android.app.Activity,
                        title = "Confirmation",
                        message = "Are you sure you want to reattempt this exam?",
                        onPositive = {
                            val intent = Intent(context, Attempt_Exam::class.java)
                            intent.putExtra("examData", questionData)
                            intent.putExtra("Paused_Answer_Key", examDataList[position])
                            context.startActivity(intent)
                        }
                    )

                }
            }
        }
        holder.tv_analysis.setOnClickListener{
            val intent = Intent(context, Analysis_Exam::class.java)
            intent.putExtra("Answer_Key", examDataList.get(position))
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = examDataList.size

    private fun parseExamDate(dateStr: String?): Date? {
        if (dateStr.isNullOrEmpty()) return null
        return try {
            val normalizedDateStr = dateStr.replace("(00:", "(12:")
            val format = SimpleDateFormat("d/M/yyyy (hh:mm a)", Locale.ENGLISH)
            format.parse(normalizedDateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDuration(diff: Long): String {
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        return when {
            hours > 24 -> "${hours / 24}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var tv_exam_mark : TextView
        var tv_reattempt : TextView
        var tv_analysis : TextView
        var attempt_date : TextView
        var hosted_by : TextView
        var subject_name : TextView
        var exam_status : TextView
        var no_of_attempt : TextView
        var pr_markScored : LinearProgressIndicator
        var ll_exam_availability: LinearLayout
        var tv_availability_range: TextView
        var tv_total_questions: TextView
        var tv_time_left: TextView

        init {
            tv_exam_mark=view.findViewById(R.id.tv_exam_mark)
            attempt_date=view.findViewById(R.id.attempt_date)
            hosted_by=view.findViewById(R.id.hosted_by)
            subject_name=view.findViewById(R.id.subject_name)
            exam_status=view.findViewById(R.id.exam_status)
            tv_reattempt =view.findViewById(R.id.tv_reattempt)
            tv_analysis =view.findViewById(R.id.tv_analysis)
            no_of_attempt =view.findViewById(R.id.no_of_attempt)
            pr_markScored =view.findViewById(R.id.pr_markScored)
            ll_exam_availability = view.findViewById(R.id.ll_exam_availability)
            tv_availability_range = view.findViewById(R.id.tv_availability_range)
            tv_total_questions = view.findViewById(R.id.tv_total_questions)
            tv_time_left = view.findViewById(R.id.tv_time_left)
        }
    }
    fun getReattempQuestion(examId: String, onSuccess: () -> Unit) {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        db.collection("Exams")
            .whereEqualTo("exam_id", examId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        questionData = document.toObject(CreateQuestions::class.java)
                    }
                    onSuccess()
                } else {
                    Toast.makeText(context, "This test was deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching exam questions", Toast.LENGTH_SHORT).show()
            }
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetDialog() {
        CustomDialogUtils.showAlert(
            activity = context as android.app.Activity,
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",

        )
    }
}