package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class LeaderBoardAdapter(var context: Context, var leaderBoardList : List<AnswerKey>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_student_leaderboard,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderBoardAdapter.ViewHolder, position: Int) {
        val db = FirebaseFirestore.getInstance()
        if(leaderBoardList[position].exam_id!=null){
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val attempt_date = originalFormat.parse(leaderBoardList.get(position).attempt_date.toString())
        val sdf = SimpleDateFormat("dd/MM/YYYY", Locale.ENGLISH)
        val timeInMillis = leaderBoardList.get(position).exam_complete_duration?.toLong()
        val hours = (timeInMillis?.div(1000) ?: 0) / 3600
        val minutes = ((timeInMillis?.div(1000) ?: 0) % 3600) / 60
        val seconds = (timeInMillis?.div(1000) ?: 0) % 60
        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        val formated_date = sdf.format(attempt_date!!)
        val total_marks = (leaderBoardList[position].pos_mark?.toInt()?:0) * (leaderBoardList[position].questionsWithAns?.size?: 0)
        val mark_scored = leaderBoardList[position].total_score?.toFloatOrNull() ?: 0f

        holder.attempt_date.text = formated_date.toString()
        holder.completedIn_time.text = timeFormatted
        holder.appear_by.text = leaderBoardList[position].appear_by
        holder.subject_name.text = leaderBoardList[position].sub_nm
        holder.tv_exam_mark.text = "$mark_scored/$total_marks"
        if(mark_scored > 0){
            holder.pr_markScored.setProgress(mark_scored.toInt()?:0,true)
            holder.pr_markScored.max=total_marks
        }else{
            holder.pr_markScored.setProgress(0,true)
            holder.pr_markScored.max=0
        }
        holder.pr_markScored.setProgress(mark_scored.toInt()?:0,true)
        holder.pr_markScored.max=total_marks
        if(leaderBoardList.get(position).pass_mark?.toDouble()!! <= mark_scored.toDouble()){
            holder.tv_result_status.text = "PASS"
            holder.ll_result_status.setBackgroundResource(R.drawable.green_left_corner_background)
        }else {
            holder.tv_result_status.text = "FAIL"
            holder.ll_result_status.setBackgroundResource(R.drawable.red_left_corner_background)
        }

            holder.tv_view_info.setOnClickListener {
                var dialog = Dialog(context)
                dialog.setContentView(R.layout.popup_leadeboard_details)
                dialog.window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                dialog.setCancelable(false)
                var cancel_button : Button = dialog.findViewById(R.id.cancel_button)
                var tv_username : TextView = dialog.findViewById(R.id.tv_username)
                var tv_email : TextView = dialog.findViewById(R.id.tv_email)
                var total_examAnd_percentage : TextView = dialog.findViewById(R.id.total_examAnd_percentage)
                var iv_stu_img : ImageView = dialog.findViewById(R.id.iv_stu_img)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                var id = ""
                var totalExamMark: Float = 0.0f
                var totalMarkScored: Float = 0.0f
                var imageUrl = ""
                var name = ""
                var email = ""
                // First fetch the document ID based on the name
                db.collection("personalDetails")
                    .whereEqualTo("name", leaderBoardList[position].appear_by).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot != null && !querySnapshot.isEmpty) {
                            for (document in querySnapshot.documents) {
                                id = document.id.toString() // Set the document ID
                                imageUrl = document.getString("imageUrl").toString()
                                name = document.getString("name").toString()
                                email = document.getString("email").toString()
                            }
                            tv_username.text = name
                            tv_email.text = email
                            if (imageUrl != null && imageUrl.isNotEmpty()) {
                                Picasso.get()
                                    .load(imageUrl).fit()
                                    .into(iv_stu_img)
                            }
                            db.collection("History").document(id).collection("HistoryDetails")
                                .addSnapshotListener { documents, error ->
                                    if (error != null) {
                                        Log.w("Firestore", "Listen failed.", error)
                                        return@addSnapshotListener
                                    }
                                    if (documents != null && !documents.isEmpty) {
                                        val tempList = mutableListOf<AnswerKey>()
                                        for (document in documents) {
                                            val answerKey = document.toObject(AnswerKey::class.java)
                                            tempList.add(answerKey)
                                            totalMarkScored += answerKey.total_score?.toFloat() ?: 0f
                                            totalExamMark += answerKey.pos_mark?.toFloat()
                                                ?.times((answerKey.questionsWithAns?.size ?: 0)) ?: 0f
                                        }
                                        val percentage = (totalMarkScored / totalExamMark) * 100
                                        val total_exam = tempList.size
                                        total_examAnd_percentage.text = "( Total Exam $total_exam / Average ${percentage.toInt()}% )"
                                    }
                                }
                        } else {
                            Toast.makeText(context, "No user found with the name", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                    }
                dialog.show()
                cancel_button.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }else{
            Toast.makeText(context,"No Data Found",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = leaderBoardList.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var tv_result_status : TextView
        var tv_view_info : TextView
        var attempt_date : TextView
        var completedIn_time : TextView
        var appear_by : TextView
        var subject_name : TextView
        var pr_markScored : LinearProgressIndicator
        var tv_exam_mark : TextView
        var ll_result_status : LinearLayout
        init {
            tv_result_status = view.findViewById(R.id.tv_result_status)
            tv_view_info = view.findViewById(R.id.tv_view_info)
            attempt_date = view.findViewById(R.id.attempt_date)
            completedIn_time = view.findViewById(R.id.completedIn_time)
            appear_by = view.findViewById(R.id.appear_by)
            subject_name = view.findViewById(R.id.subject_name)
            pr_markScored = view.findViewById(R.id.pr_markScored)
            tv_exam_mark = view.findViewById(R.id.tv_exam_mark)
            ll_result_status = view.findViewById(R.id.ll_result_status)
        }
    }
}