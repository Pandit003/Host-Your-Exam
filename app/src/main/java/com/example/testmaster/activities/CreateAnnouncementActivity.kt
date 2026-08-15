package com.example.testmaster.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaster.R
import com.example.testmaster.model.Announcement
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateAnnouncementActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedDate: Calendar = Calendar.getInstance()

    private lateinit var etTitle: EditText
    private lateinit var etExamDate: EditText
    private lateinit var etDuration: EditText
    private lateinit var etQuestions: EditText
    private lateinit var etMarking: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnPost: MaterialButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var llExamFields: LinearLayout
    private lateinit var currentCalendar: Calendar
    private var currentType = "EXAM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_announcement)

        toolbar = findViewById(R.id.toolbar)
        etTitle = findViewById(R.id.et_title)
        etExamDate = findViewById(R.id.et_exam_date)
        etDuration = findViewById(R.id.et_duration)
        etQuestions = findViewById(R.id.et_questions)
        etMarking = findViewById(R.id.et_marking)
        etDescription = findViewById(R.id.et_description)
        btnPost = findViewById(R.id.btn_post)
        tabLayout = findViewById(R.id.tab_layout)
        llExamFields = findViewById(R.id.ll_exam_fields)
        currentCalendar = Calendar.getInstance()

        toolbar.setNavigationOnClickListener { finish() }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentType = "EXAM"
                        llExamFields.visibility = View.VISIBLE
                    }
                    1 -> {
                        currentType = "MESSAGE"
                        llExamFields.visibility = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        etExamDate.setOnClickListener {
            showDatePicker()
        }

        btnPost.setOnClickListener {
            postAnnouncement()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }

                val timepickerDialog = TimePickerDialog(
                    this,
                    { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
                        selectedDate.set(Calendar.MINUTE, selectedMinute)

                        if (selectedDate.timeInMillis < currentCalendar.timeInMillis) {
                            Toast.makeText(
                                this,
                                "The selected time is in the past.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val formattedDateTime = "${selectedDate.get(Calendar.DAY_OF_MONTH)}/" +
                                    "${selectedDate.get(Calendar.MONTH) + 1}/${selectedDate.get(Calendar.YEAR)} " +
                                    "(${String.format("%02d:%02d %s", selectedHour % 12, selectedMinute, if (selectedHour >= 12) "PM" else "AM")})"
                            etExamDate.setText(formattedDateTime)
                        }
                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    false // false for 12-hour format
                )
                timepickerDialog.show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = currentCalendar.timeInMillis
        datePickerDialog.show()
    }

    private fun postAnnouncement() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val examDate = etExamDate.text.toString().trim()
        val duration = etDuration.text.toString().trim()
        val questions = etQuestions.text.toString().trim()
        val marking = etMarking.text.toString().trim()

        if (currentType == "EXAM" && examDate.isEmpty()) {
            Toast.makeText(this, "Please select an exam date", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser ?: return
        
        db.collection("personalDetails").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Teacher"
                val id = db.collection("Announcements").document().id
                
                val announcement = Announcement(
                    id = id,
                    announcerUid = user.uid,
                    announcerName = name,
                    title = title,
                    description = description,
                    examDate = if (currentType == "EXAM") examDate else null,
                    announcementDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                    duration = if (currentType == "EXAM") duration else null,
                    noOfQuestions = if (currentType == "EXAM") questions else null,
                    markingPattern = if (currentType == "EXAM") marking else null,
                    type = currentType
                )

                db.collection("Announcements").document(id).set(announcement)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Posted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
