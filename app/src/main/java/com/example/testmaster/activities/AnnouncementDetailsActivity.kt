package com.example.testmaster.activities

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaster.R
import com.example.testmaster.model.Announcement
import com.google.android.material.appbar.MaterialToolbar

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
        }
    }
}
