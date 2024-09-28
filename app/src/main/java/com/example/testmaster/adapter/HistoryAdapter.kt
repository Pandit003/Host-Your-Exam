package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.icu.text.CaseMap.Title
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.Analysis_Exam
import com.example.testmaster.Attempt_Exam
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
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
        holder.hosted_by.text = examDataList.get(position).hosted_by
        holder.subject_name.text = examDataList.get(position).sub_nm
        if(exam_status.equals("C")){
            holder.exam_status.text = "Completed"
            holder.exam_status.setBackgroundResource(R.drawable.green_gradient)
        }else{
            holder.exam_status.text = "Incompleted"
            holder.exam_status.setBackgroundResource(R.drawable.red_gradient)
        }
        holder.tv_exam_mark.text = "$mark_scored/$total_marks"
        holder.no_of_attempt.text = examDataList.get(position).no_of_attempt
        holder.tv_reattempt.setOnClickListener {
            getReattempQuestion(examDataList[position].exam_id.toString()) {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.dialog_box_confirmation)
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
                dialog.setCancelable(false)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                val dialogTitle = dialog.findViewById<TextView>(R.id.title)
                val dialogMessage = dialog.findViewById<TextView>(R.id.message)
                val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
                val btnNo = dialog.findViewById<Button>(R.id.btn_no)
                dialogTitle.text = "Confirmation"
                dialogMessage.text = "Are you sure you want to reattempt this exam?"
                btnYes.setOnClickListener {
                    dialog.dismiss()
                    val intent = Intent(context, Attempt_Exam::class.java)
                    intent.putExtra("examData", questionData)
                    intent.putExtra("Paused_Answer_Key", examDataList.get(position)) ////
                    context.startActivity(intent)
                }
                btnNo.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
        }
        holder.tv_analysis.setOnClickListener{
            val intent = Intent(context, Analysis_Exam::class.java)
            intent.putExtra("Answer_Key", examDataList.get(position))
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = examDataList.size
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
}