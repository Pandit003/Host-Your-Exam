package com.example.testmaster.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaster.R
import com.example.testmaster.model.Announcement
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnnouncementDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_details)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val tvTitle: TextView = findViewById(R.id.tv_title)
        val tvAnnouncer: TextView = findViewById(R.id.tv_announcer)
        val tvExamDate: TextView = findViewById(R.id.tv_exam_date)
        val tvDuration: TextView = findViewById(R.id.tv_duration)
        val tvQuestions: TextView = findViewById(R.id.tv_questions)
        val tvMarking: TextView = findViewById(R.id.tv_marking)
        val tvDescription: TextView = findViewById(R.id.tv_description)
        val tvPostDate: TextView = findViewById(R.id.tv_announcement_date)
        val gridSpecs: GridLayout = findViewById(R.id.grid_exam_specs)
        val vSeparator: View = findViewById(R.id.v_separator_exam)
        val llActions: LinearLayout = findViewById(R.id.ll_actions)
        val btnDelete: MaterialButton = findViewById(R.id.btn_delete)
        val btnEdit: MaterialButton = findViewById(R.id.btn_edit)

        toolbar.setNavigationOnClickListener { finish() }

        val announcement = intent.getSerializableExtra("announcement") as? Announcement
        if (announcement != null) {
            tvTitle.text = announcement.title
            tvAnnouncer.text = "by ${announcement.announcerName}"
            tvDescription.text = announcement.description
            tvPostDate.text = "Posted on ${announcement.announcementDate}"

            if (announcement.type == "EXAM") {
                vSeparator.visibility = View.VISIBLE
                gridSpecs.visibility = View.VISIBLE
                tvExamDate.text = announcement.examDate
                tvDuration.text = "${announcement.duration} mins"
                tvQuestions.text = announcement.noOfQuestions
                tvMarking.text = announcement.markingPattern
            } else {
                vSeparator.visibility = View.GONE
                gridSpecs.visibility = View.GONE
            }

            // Show actions if the current user is the announcer
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            if (announcement.announcerUid == currentUid) {
                llActions.visibility = View.VISIBLE
            }

            btnDelete.setOnClickListener {
                CustomDialogUtils.showConfirm(
                    this,
                    "Delete Announcement",
                    "Are you sure you want to delete this announcement?",
                    "Delete",
                    "Cancel",
                    onPositive = {
                        announcement.id?.let { id ->
                            FirebaseFirestore.getInstance().collection("Announcements")
                                .document(id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }

            btnEdit.setOnClickListener {
                val intent = Intent(this, CreateAnnouncementActivity::class.java)
                intent.putExtra("edit_announcement", announcement)
                startActivity(intent)
                finish()
            }
        }
    }
}
