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
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.Attempt_Exam
import com.example.testmaster.R
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import java.text.SimpleDateFormat
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
            holder.start_exam_btn.setOnClickListener{
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
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var host_date : TextView
        var full_mark : TextView
        var user_id : TextView
        var subject_name : TextView
        var start_exam_btn : Button
        init {
            host_date = view.findViewById(R.id.host_date)
            full_mark = view.findViewById(R.id.full_mark)
            user_id = view.findViewById(R.id.user_id)
            subject_name = view.findViewById(R.id.subject_name)
            start_exam_btn = view.findViewById(R.id.start_exam_btn)
        }
    }
//    object ExamDataHolder {
//        var examData: List<CreateQuestions>? = null
//    }
}
