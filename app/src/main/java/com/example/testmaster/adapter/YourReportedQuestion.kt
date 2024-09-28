package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.model_reportedQuestion

class YourReportedQuestion(var context : Context, var yourReportedQuestionsList : List<model_reportedQuestion>) : RecyclerView.Adapter<YourReportedQuestion.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourReportedQuestion.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_user_reported,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourReportedQuestion.ViewHolder, position: Int) {
        holder.subject_name.text = yourReportedQuestionsList[position].subject_name
        holder.tv_report_title.text = yourReportedQuestionsList[position].report_title
        holder.tv_report_description.text = "Report description : ${yourReportedQuestionsList[position].report_description}"
        holder.ll_report_question.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.popup_saved_question)
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
            var option_a = dialog.findViewById<LinearLayout>(R.id.option_a)
            var option_b = dialog.findViewById<LinearLayout>(R.id.option_b)
            var option_c = dialog.findViewById<LinearLayout>(R.id.option_c)
            var option_d = dialog.findViewById<LinearLayout>(R.id.option_d)
            var tv_option_a = dialog.findViewById<TextView>(R.id.tv_option_a)
            var tv_option_b = dialog.findViewById<TextView>(R.id.tv_option_b)
            var tv_option_c = dialog.findViewById<TextView>(R.id.tv_option_c)
            var tv_option_d = dialog.findViewById<TextView>(R.id.tv_option_d)
            var tv_question = dialog.findViewById<TextView>(R.id.tv_question)
            var correct_answer = dialog.findViewById<TextView>(R.id.correct_answer)
            var btn_close = dialog.findViewById<Button>(R.id.btn_close)
            tv_question.text = yourReportedQuestionsList[position].question_text
            tv_option_a.text = yourReportedQuestionsList[position].option_a
            tv_option_b.text = yourReportedQuestionsList[position].option_b
            tv_option_c.text = yourReportedQuestionsList[position].option_c
            tv_option_d.text = yourReportedQuestionsList[position].option_d
            var correctAnswer = yourReportedQuestionsList[position].correct_answer
            if (correctAnswer != null && correctAnswer!="N") {
                correct_answer.text = "Correct Answer : $correctAnswer"
                option_a.setBackgroundResource(R.drawable.box_outline)
                option_b.setBackgroundResource(R.drawable.box_outline)
                option_c.setBackgroundResource(R.drawable.box_outline)
                option_d.setBackgroundResource(R.drawable.box_outline)
                when("option_"+correctAnswer.toLowerCase()){
                    "option_a" -> option_a.setBackgroundResource(R.drawable.green_box_outline)
                    "option_b" -> option_b.setBackgroundResource(R.drawable.green_box_outline)
                    "option_c" -> option_c.setBackgroundResource(R.drawable.green_box_outline)
                    "option_d" -> option_d.setBackgroundResource(R.drawable.green_box_outline)
                }
            }
            dialog.setCancelable(false)
            btn_close.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()
        }
    }
    fun correctOption(correctAnswer: String?) {

    }

    override fun getItemCount(): Int {
       return yourReportedQuestionsList.size
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var subject_name : TextView
        var tv_report_title : TextView
        var tv_report_description : TextView
        var ll_report_question : LinearLayout
        init {
            subject_name = view.findViewById(R.id.subject_name)
            tv_report_title = view.findViewById(R.id.tv_report_title)
            tv_report_description = view.findViewById(R.id.tv_report_description)
            ll_report_question = view.findViewById(R.id.ll_report_question)
        }
    }
    }