package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.activities.Attempt_Exam
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.util.CustomDialogUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExamDetailsAdapter(var context: Context, var exam_data : List<CreateQuestions>, var examDataList : List<AnswerKey>) : RecyclerView.Adapter<ExamDetailsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamDetailsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_exam_lists,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamDetailsAdapter.ViewHolder, position: Int) {
        if (!exam_data.isEmpty()) {

            val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val date = originalFormat.parse(exam_data.get(position).hosting_date)
            val sdf = SimpleDateFormat("dd/MM/YYYY", Locale.ENGLISH)
            val formated_date = sdf.format(date)
            val questionsSize = exam_data.get(position).questions?.size ?: 0
            val posMark = exam_data.get(position).pos_mark?.toIntOrNull() ?: 0

            val fullMark = questionsSize * posMark
            holder.host_date.setText("$formated_date")
            holder.full_mark.setText("Full Mark : $fullMark")
            holder.user_id.setText(exam_data.get(position).hosted_by)
            holder.subject_name.setText(exam_data.get(position).sub_nm)

            // Enhanced Details
            val exam = exam_data[position]
            val questionsCount = exam.questions?.size ?: 0
            holder.tv_total_questions.text = "Questions: $questionsCount"
            
            if (!exam.start_time.isNullOrEmpty() && !exam.end_time.isNullOrEmpty()) {
                holder.tv_availability_range.visibility = View.VISIBLE
                holder.tv_availability_range.text = "Available: ${exam.start_time} - ${exam.end_time}"
                
                val startTime = parseExamDate(exam.start_time)
                val endTime = parseExamDate(exam.end_time)
                val now = Date()
                
                if (endTime != null && now.after(endTime)) {
                    holder.tv_time_left.visibility = View.VISIBLE
                    holder.tv_time_left.text = "Time Out"
                    holder.tv_time_left.setTextColor(context.getColor(R.color.redtint))
                } else if (startTime != null && now.before(startTime)) {
                    holder.tv_time_left.visibility = View.VISIBLE
                    val diff = startTime.time - now.time
                    holder.tv_time_left.text = "Starts in: ${formatDuration(diff)}"
                    holder.tv_time_left.setTextColor(context.getColor(R.color.bluetint))
                } else if (endTime != null) {
                    holder.tv_time_left.visibility = View.VISIBLE
                    val diff = endTime.time - now.time
                    holder.tv_time_left.text = "Ends in: ${formatDuration(diff)}"
                    holder.tv_time_left.setTextColor(context.getColor(R.color.greentint))
                } else {
                    holder.tv_time_left.visibility = View.GONE
                }
            } else {
                holder.tv_availability_range.visibility = View.GONE
                holder.tv_time_left.visibility = View.GONE
            }

            holder.start_exam_btn.setOnClickListener{
                val exam = exam_data[position]
                val endTime = parseExamDate(exam.end_time)
                val startTime = parseExamDate(exam.start_time)
                val now = Date()

                if (endTime != null && now.after(endTime)) {
                    CustomDialogUtils.showAlert(
                        activity = context as android.app.Activity,
                        title = "Attention",
                        message = "Exam time is out! You can no longer attempt this exam."
                    )
                    return@setOnClickListener
                }

                if (startTime != null && now.before(startTime)) {
                    Toast.makeText(context, "Exam has not started yet!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dialog = Dialog(context)
                dialog.setContentView(R.layout.dialog_box_confirmation)
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                dialog.setCancelable(false)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                val dialogTitle = dialog.findViewById<TextView>(R.id.title)
                val dialogMessage = dialog.findViewById<TextView>(R.id.message)
                val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
                val btnNo = dialog.findViewById<Button>(R.id.btn_no)
                dialogTitle.text = "Confirmation"
                dialogMessage.text = "Are you sure you want to start this exam?"
                dialog.show()
                btnYes.setOnClickListener {
                    dialog.dismiss()
                    val intent = Intent(context, Attempt_Exam::class.java)
                    intent.putExtra("examData", exam_data.get(0))
                    if(!examDataList.isEmpty()){
                        intent.putExtra("Paused_Answer_Key", examDataList.get(position))
                    }
                    context.startActivity(intent)
                }
                btnNo.setOnClickListener { dialog.dismiss() }
            }
        }
    }

    override fun getItemCount(): Int {
        return exam_data.size
    }

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

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var host_date : TextView
        var full_mark : TextView
        var user_id : TextView
        var subject_name : TextView
        var start_exam_btn : Button
        var ll_exam_availability: LinearLayout
        var tv_availability_range: TextView
        var tv_total_questions: TextView
        var tv_time_left: TextView

        init {
            host_date = view.findViewById(R.id.host_date)
            full_mark = view.findViewById(R.id.full_mark)
            user_id = view.findViewById(R.id.user_id)
            subject_name = view.findViewById(R.id.subject_name)
            start_exam_btn = view.findViewById(R.id.start_exam_btn)
            ll_exam_availability = view.findViewById(R.id.ll_exam_availability)
            tv_availability_range = view.findViewById(R.id.tv_availability_range)
            tv_total_questions = view.findViewById(R.id.tv_total_questions)
            tv_time_left = view.findViewById(R.id.tv_time_left)
        }
    }
//    object ExamDataHolder {
//        var examData: List<CreateQuestions>? = null
//    }
}
