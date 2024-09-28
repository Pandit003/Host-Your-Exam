package com.example.testmaster.adapter

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.model_savedQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SavedQuestionsAdapter(var context : Context, var savedQuestionsList: List<model_savedQuestion>) : RecyclerView.Adapter<SavedQuestionsAdapter.ViewHolder>() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    lateinit var option_a : LinearLayout
    lateinit var option_b : LinearLayout
    lateinit var option_c : LinearLayout
    lateinit var option_d : LinearLayout
    lateinit var tv_question : TextView
    lateinit var tv_option_a : TextView
    lateinit var tv_option_b : TextView
    lateinit var tv_option_c : TextView
    lateinit var tv_option_d : TextView
    lateinit var correct_answer : TextView
    lateinit var btn_close : Button
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedQuestionsAdapter.ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_saved_questions,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedQuestionsAdapter.ViewHolder, position: Int) {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        holder.question.text = "Question : ${savedQuestionsList[position].question_text}"
        holder.subject_name.text = savedQuestionsList[position].subject_name
        holder.iv_delete.setOnClickListener {
            deleteQuestionById(savedQuestionsList[position].id, position)
        }
        holder.ll_save_question.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.popup_saved_question)
            dialog.window?.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
            option_a = dialog.findViewById(R.id.option_a)
            option_b = dialog.findViewById(R.id.option_b)
            option_c = dialog.findViewById(R.id.option_c)
            option_d = dialog.findViewById(R.id.option_d)
            tv_option_a = dialog.findViewById(R.id.tv_option_a)
            tv_option_b = dialog.findViewById(R.id.tv_option_b)
            tv_option_c = dialog.findViewById(R.id.tv_option_c)
            tv_option_d = dialog.findViewById(R.id.tv_option_d)
            tv_question = dialog.findViewById(R.id.tv_question)
            correct_answer = dialog.findViewById(R.id.correct_answer)
            btn_close = dialog.findViewById(R.id.btn_close)
            tv_question.text = savedQuestionsList[position].question_text
            tv_option_a.text = savedQuestionsList[position].option_a
            tv_option_b.text = savedQuestionsList[position].option_b
            tv_option_c.text = savedQuestionsList[position].option_c
            tv_option_d.text = savedQuestionsList[position].option_d
            correctOption(savedQuestionsList[position].correct_answer)
            dialog.setCancelable(false)
            btn_close.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
       return savedQuestionsList.size
    }
    private fun deleteQuestionById(id: String?, position: Int) {
        if (id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Query for the specific document based on exam_id
        db.collection("SavedQuestion").document(user)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val questionsList = documentSnapshot.get("savedQuestionsList") as? List<Map<String, Any>>

                    if (questionsList != null) {
                        // Find the specific question to delete
                        val questionToDelete = questionsList.find { it["id"] == id }
                        if (questionToDelete != null) {
                            // Remove the question from the Firestore document
                            db.collection("SavedQuestion").document(user)
                                .update("savedQuestionsList", FieldValue.arrayRemove(questionToDelete))
                                .addOnSuccessListener {
                                    // Remove the question from the local list
                                    if (position >= 0 && position < savedQuestionsList.size) {
//                                        savedQuestionsList.removeAt(position)
                                        notifyItemRemoved(position)
                                        Toast.makeText(context, "Question Unsaved", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error deleting document", e)
                                    Toast.makeText(context, "Error deleting question", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Question not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.d("Firestore", "No document found")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting document", e)
            }
    }
    fun correctOption(correctAnswer: String?) {
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
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var subject_name : TextView
        var iv_delete : ImageView
        var question : TextView
        var ll_save_question : LinearLayout
        init {
            subject_name = view.findViewById(R.id.subject_name)
            iv_delete = view.findViewById(R.id.iv_delete)
            question = view.findViewById(R.id.question)
            ll_save_question = view.findViewById(R.id.ll_save_question)
        }
    }
}