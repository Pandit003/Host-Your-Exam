package com.example.testmaster.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.testmaster.R
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.model.Question
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UploadQuestionJsonActivity : AppCompatActivity() {

    private lateinit var etJson: EditText
    private lateinit var tvError: TextView
    private lateinit var btnHostExam: MaterialButton
    private lateinit var btnPaste: ImageButton
    private lateinit var btnInfo: ImageView
    private lateinit var toolbar: Toolbar

    private lateinit var etSubjectName: EditText
    private lateinit var rgVisibility: RadioGroup
    private lateinit var rbPublic: RadioButton
    private lateinit var rbSubscribers: RadioButton
    
    private lateinit var setTimeSwitch: SwitchCompat
    private lateinit var llSetTime: LinearLayout
    private lateinit var llTotalTimeSection: LinearLayout
    private lateinit var setStartTime: LinearLayout
    private lateinit var setEndTime: LinearLayout
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvTotalTime: TextView

    private lateinit var npDurationHrs: NumberPicker
    private lateinit var npDurationMins: NumberPicker
    private lateinit var tvExmHrs: TextView
    private lateinit var tvExmMin: TextView

    private lateinit var etPosMark: EditText
    private lateinit var etNegMark: EditText
    private lateinit var etPassMark: EditText

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val gson = Gson()

    private var currentCalendar = Calendar.getInstance()
    private var startCalendar = Calendar.getInstance()
    private var endCalendar = Calendar.getInstance()
    private var examDurationHrs = 0
    private var examDurationMin = 0

    private var hasUserInteractedWithJson = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_question_json)

        onBackPressedDispatcher.addCallback(this) {
            showConfirmation()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews()
        setupToolbar()
        setupListeners()
        setupPickers()
        
        // Show initial reference
        setInitialJson()
    }

    private fun initViews() {
        etJson = findViewById(R.id.et_json)
        tvError = findViewById(R.id.tv_error)
        btnHostExam = findViewById(R.id.btn_host_exam)
        btnPaste = findViewById(R.id.btn_paste)
        btnInfo = findViewById(R.id.btn_info)
        toolbar = findViewById(R.id.toolbar)

        etSubjectName = findViewById(R.id.subject_name)
        rgVisibility = findViewById(R.id.rg_visibility)
        rbPublic = findViewById(R.id.rb_public)
        rbSubscribers = findViewById(R.id.rb_subscribers)
        
        setTimeSwitch = findViewById(R.id.set_time_switch)
        llSetTime = findViewById(R.id.ll_set_time)
        llTotalTimeSection = findViewById(R.id.ll_total_time)
        setStartTime = findViewById(R.id.set_start_time)
        setEndTime = findViewById(R.id.set_end_time)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        tvTotalTime = findViewById(R.id.tv_total_time)

        npDurationHrs = findViewById(R.id.np_exam_duration_hrs)
        npDurationMins = findViewById(R.id.np_exam_duration_mins)
        tvExmHrs = findViewById(R.id.tv_exm_hrs)
        tvExmMin = findViewById(R.id.tv_exm_min)

        etPosMark = findViewById(R.id.et_pos_mark)
        etNegMark = findViewById(R.id.et_neg_mark)
        etPassMark = findViewById(R.id.et_pass_mark)

        setEndTime.isEnabled = false
        setEndTime.alpha = 0.5f
        
        // Host button dimmed and disabled by default
        setHostButtonEnabled(false)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(getColor(R.color.onPrimary))
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupPickers() {
        npDurationHrs.minValue = 0
        npDurationHrs.maxValue = 4
        npDurationHrs.setOnValueChangedListener { _, _, newValue ->
            tvExmHrs.text = newValue.toString() + " hours"
            examDurationHrs = newValue * 60 * 60 * 1000
            updateHostButtonState()
        }

        npDurationMins.minValue = 0
        npDurationMins.maxValue = 59
        npDurationMins.setOnValueChangedListener { _, _, newValue ->
            tvExmMin.text = newValue.toString() + " min"
            examDurationMin = newValue * 60 * 1000
            updateHostButtonState()
        }
    }

    private fun setupListeners() {
        btnPaste.setOnClickListener { 
            hasUserInteractedWithJson = true
            pasteFromClipboard() 
        }
        btnInfo.setOnClickListener { showReferenceDialog() }

        setTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            llSetTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            llTotalTimeSection.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateHostButtonState()
        }

        setStartTime.setOnClickListener { setDateAndTime(tvStartTime) }
        setEndTime.setOnClickListener { setDateAndTime(tvEndTime) }

        val commonTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etJson.hasFocus()) hasUserInteractedWithJson = true
                updateHostButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etJson.addTextChangedListener(commonTextWatcher)
        etSubjectName.addTextChangedListener(commonTextWatcher)
        etPosMark.addTextChangedListener(commonTextWatcher)
        etNegMark.addTextChangedListener(commonTextWatcher)
        etPassMark.addTextChangedListener(commonTextWatcher)

        btnHostExam.setOnClickListener {
            val json = etJson.text.toString()
            if (validateInputs() && isValidJson(json)) {
                processHosting(json)
            }
        }
    }

    private fun updateHostButtonState() {
        val json = etJson.text.toString()
        val isJsonValid = isValidJson(json)
        val areInputsFilled = etSubjectName.text.isNotBlank() && 
                             etPosMark.text.isNotBlank() && 
                             etPassMark.text.isNotBlank() && 
                             (examDurationHrs + examDurationMin > 0) &&
                             (!setTimeSwitch.isChecked || (tvStartTime.text.isNotBlank() && tvEndTime.text.isNotBlank()))
        
        setHostButtonEnabled(isJsonValid && areInputsFilled)
        
        // Show error message only if the user has entered/modified some JSON and it's invalid
        if (hasUserInteractedWithJson && !isJsonValid && json.isNotBlank()) {
            validateJson(json)
        } else {
            tvError.visibility = View.GONE
        }
    }

    private fun setHostButtonEnabled(enabled: Boolean) {
        btnHostExam.isEnabled = enabled
        if (enabled) {
            btnHostExam.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bluetint))
            btnHostExam.alpha = 1.0f
        } else {
            // Light grey color for disabled state as requested
            btnHostExam.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            btnHostExam.alpha = 0.5f
        }
    }

    private fun setDateAndTime(textView: TextView) {
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

                        if (selectedDate.timeInMillis < Calendar.getInstance().timeInMillis) {
                            Toast.makeText(this, "The selected time is in the past.", Toast.LENGTH_LONG).show()
                        } else {
                            val formattedDateTime = "${selectedDate.get(Calendar.DAY_OF_MONTH)}/" +
                                    "${selectedDate.get(Calendar.MONTH) + 1}/${selectedDate.get(Calendar.YEAR)} " +
                                    "(${String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour % 12, selectedMinute, if (selectedHour >= 12) "PM" else "AM")})"
                            textView.text = formattedDateTime

                            if (textView == tvStartTime) {
                                startCalendar = selectedDate
                                setEndTime.isEnabled = true
                                setEndTime.alpha = 1.0f
                                tvEndTime.text = ""
                            } else if (textView == tvEndTime) {
                                if (selectedDate.timeInMillis < startCalendar.timeInMillis) {
                                    Toast.makeText(this, "End date cannot be before start date.", Toast.LENGTH_LONG).show()
                                } else {
                                    endCalendar = selectedDate
                                    calculateDifference()
                                }
                            }
                            updateHostButtonState()
                        }
                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    false
                )
                timepickerDialog.show()
                timepickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.onPrimary))
                timepickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.onPrimary))
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.onPrimary))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.onPrimary))
    }

    private fun calculateDifference() {
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val days = diffInMillis / (24 * 60 * 60 * 1000)
        val hours = (diffInMillis / (60 * 60 * 1000)) % 24
        val minutes = (diffInMillis / (60 * 1000)) % 60
        tvTotalTime.text = "$days days, $hours hours, $minutes minutes"
    }

    private fun validateInputs(): Boolean {
        if (etSubjectName.text.isEmpty()) {
            Toast.makeText(this, "Enter The Subject Name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (setTimeSwitch.isChecked) {
            if (tvStartTime.text.isEmpty() || tvEndTime.text.isEmpty()) {
                Toast.makeText(this, "Set The Exam Start and End Time", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        if (examDurationHrs + examDurationMin <= 0) {
            Toast.makeText(this, "Set The Exam Duration", Toast.LENGTH_SHORT).show()
            return false
        }
        val posMark = etPosMark.text.toString().toDoubleOrNull() ?: 0.0
        val passMark = etPassMark.text.toString().toDoubleOrNull() ?: 0.0
        
        if (posMark <= 0) {
            Toast.makeText(this, "Enter The Positive Mark", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etNegMark.text.isEmpty()) {
            Toast.makeText(this, "Enter The Negative Mark", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passMark <= 0) {
            Toast.makeText(this, "Enter The Passing Mark", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun setInitialJson() {
        etJson.setText(JsonReference.SAMPLE_JSON)
        tvError.visibility = View.GONE 
        hasUserInteractedWithJson = false
        setHostButtonEnabled(false)
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            etJson.setText(clip.getItemAt(0).text)
            Toast.makeText(this, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReferenceDialog() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "JSON Structure Reference",
            message = JsonReference.REFERENCE_MESSAGE,
            positiveText = "Got it",
            negativeText = "Copy Sample",
            onPositive = {},
            onNegative = {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Sample JSON", JsonReference.SAMPLE_JSON)
                clipboard.setPrimaryClip(clip)
            }
        )
    }

    private fun validateJson(json: String) {
        if (json.isBlank()) {
            tvError.visibility = View.GONE
            return
        }
        try {
            val type = object : TypeToken<List<Question>>() {}.type
            val questions: List<Question>? = gson.fromJson(json, type)
            if (questions == null || questions.isEmpty()) {
                showError("At least one question is required in the array")
            } else {
                val missingFields = checkMissingFields(questions)
                if (missingFields.isNotEmpty()) {
                    showError(missingFields)
                } else {
                    tvError.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            showError("Invalid JSON format. Please check your structure.")
        }
    }

    private fun checkMissingFields(questions: List<Question>): String {
        for ((index, q) in questions.withIndex()) {
            val qNum = q.question_no ?: (index + 1).toString()
            if (q.question_no.isNullOrBlank()) return "Question ${index + 1}: 'question_no' is missing"
            if (q.question_text.isNullOrBlank()) return "Question $qNum: 'question_text' is missing"
            if (q.option_a.isNullOrBlank()) return "Question $qNum: 'option_a' is missing"
            if (q.option_b.isNullOrBlank()) return "Question $qNum: 'option_b' is missing"
            if (q.option_c.isNullOrBlank()) return "Question $qNum: 'option_c' is missing"
            if (q.option_d.isNullOrBlank()) return "Question $qNum: 'option_d' is missing"
            
            val ans = q.correct_answer?.trim()?.uppercase() ?: ""
            if (ans.isEmpty()) return "Question $qNum: 'correct_answer' is missing"
            // Strict ABCD validation as requested
            if (ans !in listOf("A", "B", "C", "D")) {
                return "Question $qNum: 'correct_answer' must be A, B, C, or D"
            }
        }
        return ""
    }

    private fun isValidJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<Question>>() {}.type
            val questions: List<Question>? = gson.fromJson(json, type)
            questions != null && questions.isNotEmpty() && checkMissingFields(questions).isEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun processHosting(json: String) {
        val type = object : TypeToken<List<Question>>() {}.type
        val rawQuestions: List<Question> = gson.fromJson(json, type)
        // Normalize correct_answer to uppercase
        val questionsList = rawQuestions.map { 
            it.copy(correct_answer = it.correct_answer?.trim()?.uppercase()) 
        }

        val user = firebaseAuth.currentUser ?: return
        
        setHostButtonEnabled(false)

        db.collection("personalDetails").document(user.uid).get()
            .addOnSuccessListener { document ->
                val username = document.get("name").toString()
                
                val finalQuestions = CreateQuestions(
                    candidate_id = user.uid,
                    sub_nm = etSubjectName.text.toString(),
                    visibility = if (rbPublic.isChecked) "Public" else "Subscribers",
                    start_time = if (setTimeSwitch.isChecked) tvStartTime.text.toString() else "",
                    end_time = if (setTimeSwitch.isChecked) tvEndTime.text.toString() else "",
                    exam_avl_time = if (setTimeSwitch.isChecked) tvTotalTime.text.toString() else "",
                    exam_duration = (examDurationHrs + examDurationMin).toString(),
                    pos_mark = etPosMark.text.toString(),
                    neg_mark = etNegMark.text.toString(),
                    pass_mark = etPassMark.text.toString(),
                    hosting_date = Date().toString(),
                    hosted_by = username,
                    questions = questionsList
                )
                
                setExamId { newExamId ->
                    val hostedQuestions = finalQuestions.copy(exam_id = newExamId)
                    hostExam(hostedQuestions)
                }
            }
            .addOnFailureListener {
                setHostButtonEnabled(true)
                Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setExamId(callback: (String) -> Unit) {
        db.collection("Exams")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    var maxExamId = 253465
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            val examIdString = document.getString("exam_id")
                            val examId = examIdString?.toIntOrNull() ?: continue
                            if (examId > maxExamId) maxExamId = examId
                        }
                        maxExamId += 1
                    }
                    callback(maxExamId.toString())
                } else {
                    callback("253465")
                }
            }
    }

    private fun hostExam(questions: CreateQuestions) {
        val user = firebaseAuth.currentUser?.uid.toString()
        db.collection("CreatedQuestion").document(user)
            .collection("QuestionsDetails").add(questions)
            .addOnSuccessListener {
                db.collection("Exams")
                    .add(questions)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Exam Hosted Successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, HostedTest::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        setHostButtonEnabled(true)
                        Toast.makeText(this, "Failed to host exam globally", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                setHostButtonEnabled(true)
                Toast.makeText(this, "Failed to host exam locally", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmation() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "Exit?",
            message = "Are you sure you want to exit? All unsaved data will be lost.",
            onPositive = {
                finish()
            }
        )
    }
}

object JsonReference {
    const val REFERENCE_MESSAGE = "Tip: Copy this reference JSON and provide it to an AI (like Gemini) along with your question PDF or list. Ask the AI to 'Convert these questions into this exact JSON format'. Once converted, paste the result here to host your exam instantly! The JSON must follow the given structure."
    
    const val SAMPLE_JSON = """[
  {
    "question_no": "1",
    "question_text": "",
    "option_a": "",
    "option_b": "",
    "option_c": "",
    "option_d": "",
    "correct_answer": "A"
  },
  {
    "question_no": "2",
    "question_text": "",
    "option_a": "",
    "option_b": "",
    "option_c": "",
    "option_d": "",
    "correct_answer": "B"
  }
]"""
}
