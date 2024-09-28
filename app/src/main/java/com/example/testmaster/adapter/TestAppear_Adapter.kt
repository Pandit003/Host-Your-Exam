package com.example.testmaster.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.google.android.material.progressindicator.LinearProgressIndicator

class TestAppear_Adapter(var context: Context, var recentExamApearList : List<AnswerKey>) : RecyclerView.Adapter<TestAppear_Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestAppear_Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_test_appear,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestAppear_Adapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        var total_marks = (recentExamApearList.get(position).pos_mark?.toInt()?:0) * (recentExamApearList.get(position).questionsWithAns?.size?: 0)
        val mark_scored = recentExamApearList[position].total_score?.toFloatOrNull() ?: 0f
        holder.markScored.text="$mark_scored/$total_marks"
        holder.hosted_by.text = "Hoster : ${recentExamApearList.get(position).hosted_by}"
//        holder.hosted_by.text = "Rohit Pandit"
        if(recentExamApearList.get(position).exam_status=="C"){
            holder.exam_status.text = "Completed"
        }else{
            holder.exam_status.text = "Pause"
        }
        holder.pr_markScored.setProgress(mark_scored.toInt(),true)
        holder.pr_markScored.max=total_marks
        holder.subject_name.text="Sub : ${recentExamApearList.get(position).sub_nm}"
    }

    override fun getItemCount() = recentExamApearList.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var markScored : TextView
        var hosted_by : TextView
        var subject_name : TextView
        var exam_status : TextView
        var pr_markScored : LinearProgressIndicator
        init {
            markScored=view.findViewById(R.id.tv_mark_scored)
            hosted_by=view.findViewById(R.id.hosted_by)
            subject_name=view.findViewById(R.id.subject_name)
            exam_status=view.findViewById(R.id.exam_status)
            pr_markScored = view.findViewById(R.id.pr_markScored)
        }
    }
}