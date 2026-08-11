package com.example.testmaster.adapter

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.activities.Attempt_Exam
import com.example.testmaster.activities.CreateMcqTest
import com.example.testmaster.R
import com.example.testmaster.model.CreateQuestions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class HostedTestAdapter(var context: Context,var HostedTestList : List<CreateQuestions>) : RecyclerView.Adapter<HostedTestAdapter.ViewHolder>() {
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostedTestAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_hosted_test,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HostedTestAdapter.ViewHolder, position: Int) {
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        try {
            val hostedDate = originalFormat.parse(HostedTestList[position].hosting_date)
            if (hostedDate != null) {
                val formattedDate = sdf.format(hostedDate)
                holder.host_date.text = formattedDate
            } else {
                holder.host_date.text = "Invalid Date"
            }
        } catch (e: Exception) {
            holder.host_date.text = "Error Formatting Date"
            Log.e("HostedTestAdapter", "Error parsing hosting_date: ${e.message}")
        }

        var timeInMillis = HostedTestList[position].exam_duration?.toLong() ?:0
        val hours = timeInMillis / 1000 / 3600
        val minutes = (timeInMillis / 1000 % 3600) / 60
        val seconds = timeInMillis / 1000 % 60
        holder.exam_duration.text = String.format("%02d:%02d:%02d",hours,minutes,seconds)
        holder.subject_name.text = HostedTestList[position].sub_nm
        holder.exam_Id.text = HostedTestList[position].exam_id
        
        holder.itemView.setOnClickListener {
            showExamDetailsBottomSheet(HostedTestList[position])
        }

        holder.iv_copy.setOnClickListener{
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Exam ID", holder.exam_Id.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context,"Copied!!",Toast.LENGTH_SHORT).show()
        }
        holder.iv_delete.setOnClickListener {
            deleteExam(HostedTestList[position])
        }
    }

    private fun showExamDetailsBottomSheet(exam: CreateQuestions) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_exam_details_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val tvSubject = view.findViewById<TextView>(R.id.bs_subject_name)
        val tvExamId = view.findViewById<TextView>(R.id.bs_exam_id)
        val tvCreatedDate = view.findViewById<TextView>(R.id.bs_created_date)
        val tvTotalQuestions = view.findViewById<TextView>(R.id.bs_total_questions)
        val tvMarking = view.findViewById<TextView>(R.id.bs_marking)
        val tvPassingMark = view.findViewById<TextView>(R.id.bs_passing_mark)
        val tvDuration = view.findViewById<TextView>(R.id.bs_duration)
        val tvTotalMarks = view.findViewById<TextView>(R.id.bs_total_marks)
        val llAvailability = view.findViewById<LinearLayout>(R.id.bs_ll_availability)
        val tvStartTime = view.findViewById<TextView>(R.id.bs_start_time)
        val tvEndTime = view.findViewById<TextView>(R.id.bs_end_time)
        val btnDelete = view.findViewById<Button>(R.id.bs_btn_delete)
        val btnEdit = view.findViewById<Button>(R.id.bs_btn_edit)

        tvSubject.text = exam.sub_nm
        tvExamId.text = "ID: ${exam.exam_id}"

        // Date formatting
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        try {
            val hostedDate = originalFormat.parse(exam.hosting_date)
            tvCreatedDate.text = if (hostedDate != null) sdf.format(hostedDate) else exam.hosting_date
        } catch (e: Exception) {
            tvCreatedDate.text = exam.hosting_date
        }

        val questionCount = exam.questions?.size ?: 0
        tvTotalQuestions.text = questionCount.toString()
        tvMarking.text = "+${exam.pos_mark} / -${exam.neg_mark}"
        tvPassingMark.text = exam.pass_mark
        
        val timeInMillis = exam.exam_duration?.toLong() ?: 0
        val hours = timeInMillis / 1000 / 3600
        val minutes = (timeInMillis / 1000 % 3600) / 60
        val seconds = timeInMillis / 1000 % 60
        tvDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val totalMarks = (exam.pos_mark?.toDoubleOrNull() ?: 0.0) * questionCount
        tvTotalMarks.text = totalMarks.toString()

        if (!exam.start_time.isNullOrEmpty() || !exam.end_time.isNullOrEmpty()) {
            llAvailability.visibility = View.VISIBLE
            tvStartTime.text = "Start: ${exam.start_time}"
            tvEndTime.text = "End: ${exam.end_time}"
        }

        btnDelete.setOnClickListener {
            bottomSheetDialog.dismiss()
            deleteExam(exam)
        }

        btnEdit.setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(context, CreateMcqTest::class.java)
            intent.putExtra("EDIT_EXAM", exam)
            context.startActivity(intent)
        }

        bottomSheetDialog.show()
    }

    private fun deleteExam(exam: CreateQuestions) {
        val sub_nm = exam.sub_nm
        val examid = exam.exam_id
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
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
        dialogMessage.text = "Are you sure you want to delete this exam?"
        dialog.show()
        btnYes.setOnClickListener {
            dialog.dismiss()
            db.collection("CreatedQuestion").document(user).collection("QuestionsDetails").whereEqualTo("exam_id",examid)
                .get().addOnSuccessListener {documents->
                    for(document in documents){
                        db.collection("CreatedQuestion").document(user).collection("QuestionsDetails").document(document.id).delete().addOnSuccessListener {
                            db.collection("Exams").whereEqualTo("exam_id",examid)
                                .get().addOnSuccessListener {documents->
                                    for(exam_document in documents){
                                        db.collection("Exams").document(exam_document.id).delete().addOnSuccessListener {
                                            Toast.makeText(context,"${sub_nm} Question was deleted",Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(context,"${sub_nm} Try again Leter",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }.addOnFailureListener { e ->
                                    Log.d("Firestore", "Error getting documents: $e")
                                }
                        }.addOnFailureListener {
                            Toast.makeText(context,"${sub_nm} Try again Leter",Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting documents: ", e)
                }

        }
        btnNo.setOnClickListener { dialog.dismiss() }
    }

    override fun getItemCount(): Int {
        return HostedTestList.size
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var host_date : TextView
        var exam_duration : TextView
        var subject_name : TextView
        var exam_Id : TextView
        var iv_copy : LinearLayout
        var iv_delete : ImageView
        init {
            host_date = view.findViewById(R.id.host_date)
            exam_duration = view.findViewById(R.id.exam_duration)
            subject_name = view.findViewById(R.id.subject_name)
            exam_Id = view.findViewById(R.id.exam_Id)
            iv_copy = view.findViewById(R.id.iv_copy)
            iv_delete = view.findViewById(R.id.iv_delete)
        }
    }
}