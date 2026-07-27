package com.example.testmaster.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class TestAppear_Adapter(var context: Context, var recentExamApearList : List<AnswerKey>) : RecyclerView.Adapter<TestAppear_Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestAppear_Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_test_appear,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestAppear_Adapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val exam = recentExamApearList[position]
        val totalQuestions = exam.questionsWithAns?.size ?: 0
        val posMark = exam.pos_mark?.toFloatOrNull() ?: 0f
        val total_marks = (posMark * totalQuestions).toInt()
        val mark_scored = exam.total_score?.toFloatOrNull() ?: 0f
        
        holder.markScored.text = "$mark_scored/$total_marks"
        holder.hosted_by.text = "Hosted by ${exam.hosted_by ?: "Unknown"}"
        
        if(exam.exam_status == "C"){
            holder.exam_status.text = "Completed"
            holder.exam_status.setBackgroundResource(R.drawable.bg_status_badge)
            holder.exam_status.setTextColor(context.getColor(R.color.success_text))
        } else {
            holder.exam_status.text = "Paused"
            // Use a different badge color if available
            holder.exam_status.setBackgroundResource(R.drawable.bg_status_badge)
            holder.exam_status.setTextColor(context.getColor(R.color.warning_text))
        }
        
        val progress = if (total_marks > 0) (mark_scored / total_marks * 100).toInt() else 0
        holder.pr_markScored.setProgress(progress, true)
        holder.subject_name.text = exam.sub_nm ?: "Untitled Exam"
        
        holder.btnViewResult.setOnClickListener {
            // Existing logic to view result
        }
    }

    override fun getItemCount() = recentExamApearList.size
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var markScored : TextView = view.findViewById(R.id.tv_mark_scored)
        var hosted_by : TextView = view.findViewById(R.id.hosted_by)
        var subject_name : TextView = view.findViewById(R.id.subject_name)
        var exam_status : TextView = view.findViewById(R.id.exam_status)
        var pr_markScored : LinearProgressIndicator = view.findViewById(R.id.pr_markScored)
        var btnViewResult : MaterialButton = view.findViewById(R.id.btn_view_result)
    }
}