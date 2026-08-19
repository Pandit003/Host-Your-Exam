package com.example.testmaster.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.testmaster.R
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.model.Question
import com.example.testmaster.util.CustomDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.Toolbar
import java.util.*


class CreateMcqTest : AppCompatActivity() {
    private lateinit var set_time_switch: SwitchCompat
    private lateinit var ll_set_time: LinearLayout
    private lateinit var set_start_time: LinearLayout
    private lateinit var set_end_time: LinearLayout
    private lateinit var ll_total_time: LinearLayout
    private lateinit var ll_next: LinearLayout
    private lateinit var ll_previous: LinearLayout
    private lateinit var tv_clear_text: TextView
    private lateinit var tv_start_time: TextView
    private lateinit var tv_end_time: TextView
    private lateinit var tv_total_time: TextView
    private lateinit var tv_exm_hrs: TextView
    private lateinit var tv_exm_min: TextView
    private lateinit var currentCalendar: Calendar
    private lateinit var startCalendar: Calendar
    private lateinit var endCalendar: Calendar
    private lateinit var spinner_answer: Spinner
    private lateinit var np_exam_duration_hrs: NumberPicker
    private lateinit var np_exam_duration_mins: NumberPicker
    private lateinit var firebaseAuth: FirebaseAuth

    lateinit var tv_question_no : TextView
    lateinit var subject_name : EditText
    lateinit var main_question : EditText
    lateinit var opt_a : EditText
    lateinit var opt_b : EditText
    lateinit var opt_c : EditText
    lateinit var opt_d : EditText
    lateinit var et_pos_mark : EditText
    lateinit var et_neg_mark : EditText
    lateinit var et_pass_mark : EditText
    lateinit var host_btn : Button
    lateinit var db : FirebaseFirestore

    private lateinit var rgVisibility: RadioGroup
    private lateinit var rbPublic: RadioButton
    private lateinit var rbSubscribers: RadioButton

    val questionList = mutableListOf<Question>()

    var count=0
    var question_count = 1
    var exam_duration_hrs = 0
    var exam_duration_min = 0
    var isLastquestion : Boolean = false

    private var isEditMode = false
    private var examToEdit: CreateQuestions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_mcq_test)
        onBackPressedDispatcher.addCallback(this) {
            showConfirmation()
        }
        val spinnerOption = arrayOf("Select The Answer","A","B","C","D")

        currentCalendar = Calendar.getInstance()
        startCalendar = Calendar.getInstance()
        endCalendar = Calendar.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = firebaseAuth.currentUser?.uid

        set_time_switch = findViewById(R.id.set_time_switch)
        ll_set_time = findViewById(R.id.ll_set_time)
        set_start_time = findViewById(R.id.set_start_time)
        set_end_time = findViewById(R.id.set_end_time)
        ll_total_time = findViewById(R.id.ll_total_time)
        tv_start_time = findViewById(R.id.tv_start_time)
        tv_end_time = findViewById(R.id.tv_end_time)
        tv_total_time = findViewById(R.id.tv_total_time)
        spinner_answer = findViewById(R.id.spinner_answer)
        np_exam_duration_hrs = findViewById(R.id.np_exam_duration_hrs)
        np_exam_duration_mins = findViewById(R.id.np_exam_duration_mins)
        tv_exm_hrs = findViewById(R.id.tv_exm_hrs)
        tv_exm_min = findViewById(R.id.tv_exm_min)
        ll_next = findViewById(R.id.ll_next)
        ll_previous = findViewById(R.id.ll_previous)
        tv_clear_text = findViewById(R.id.tv_clear_text)

        tv_question_no = findViewById(R.id.tv_question_no)
        subject_name = findViewById(R.id.subject_name)
        main_question = findViewById(R.id.main_question)
        opt_a = findViewById(R.id.opt_a)
        opt_b = findViewById(R.id.opt_b)
        opt_c = findViewById(R.id.opt_c)
        opt_d = findViewById(R.id.opt_d)
        et_pos_mark = findViewById(R.id.et_pos_mark)
        et_neg_mark = findViewById(R.id.et_neg_mark)
        et_pass_mark = findViewById(R.id.et_pass_mark)
        host_btn = findViewById(R.id.host_btn)
        
        rgVisibility = findViewById(R.id.rg_visibility)
        rbPublic = findViewById(R.id.rb_public)
        rbSubscribers = findViewById(R.id.rb_subscribers)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setTint(getColor(R.color.onPrimary))

        np_exam_duration_hrs.minValue=0
        np_exam_duration_hrs.maxValue=4

        np_exam_duration_hrs.setOnValueChangedListener{_,_,newValue->
            tv_exm_hrs.text = newValue.toString()+" hours"
            exam_duration_hrs = newValue*60*60*1000
        }

        np_exam_duration_mins.minValue=0
        np_exam_duration_mins.maxValue=59

        np_exam_duration_mins.setOnValueChangedListener{_,_,newValue->
            tv_exm_min.text = newValue.toString()+" min"
            exam_duration_min = newValue*60*1000
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,spinnerOption)
        spinner_answer.setAdapter(adapter)

        // Check for Edit Mode
        if (intent.hasExtra("EDIT_EXAM")) {
            isEditMode = true
            examToEdit = intent.getSerializableExtra("EDIT_EXAM") as CreateQuestions
            setupEditMode()
        }

        set_end_time.isEnabled = false
        set_end_time.alpha = 0.5f

        set_start_time.setOnClickListener {
            setDateAndTime(tv_start_time)
        }

        set_end_time.setOnClickListener {
            setDateAndTime(tv_end_time)
        }

        updateLayoutVisibility(set_time_switch.isChecked)

        set_time_switch.setOnCheckedChangeListener { _, isChecked ->
            updateLayoutVisibility(isChecked)
        }

        tv_clear_text.setOnClickListener{
            main_question.setText("")
            opt_a.setText("")
            opt_b.setText("")
            opt_c.setText("")
            opt_d.setText("")
        }
        ll_previous.setOnClickListener{
            if(count>0) {
                count--
                var pre_questions: Question = questionList.get(count)
                if (!questionList.isEmpty()) {
                    tv_question_no.setText("Q" + pre_questions.question_no + ". ")
                    main_question.setText(pre_questions.question_text)
                    opt_a.setText(pre_questions.option_a)
                    opt_b.setText(pre_questions.option_b)
                    opt_c.setText(pre_questions.option_c)
                    opt_d.setText(pre_questions.option_d)
                    val position = spinnerOption.indexOf(pre_questions.correct_answer)
                    spinner_answer.setSelection(position)
                }
                if (count<=0){
                    ll_previous.visibility = View.GONE
                }
            }
        }
        ll_next.setOnClickListener {
            // Save or update the current question before moving to the next one
            if(main_question.text.toString().isEmpty()){
                Toast.makeText(this,"Enter The Question",Toast.LENGTH_LONG).show()
            }else if(opt_a.text.toString().isEmpty()){
                Toast.makeText(this,"Enter The Option A",Toast.LENGTH_LONG).show()
            }else if(opt_b.text.toString().isEmpty()){
                Toast.makeText(this,"Enter The Option B",Toast.LENGTH_LONG).show()
            }else if(opt_c.text.toString().isEmpty()){
                Toast.makeText(this,"Enter The Option C",Toast.LENGTH_LONG).show()
            }else if(opt_d.text.toString().isEmpty()){
                Toast.makeText(this,"Enter The Option D",Toast.LENGTH_LONG).show()
            }else if(spinner_answer.selectedItem.equals("Select The Answer")){
                Toast.makeText(this,"Please Select The Answer",Toast.LENGTH_LONG).show()
            }else {
                if (questionList.size > count) {
                    // Update the existing question
                    questionList[count] = Question(
                        question_no = tv_question_no.text.toString().removePrefix("Q")
                            .removeSuffix(". "),
                        question_text = main_question.text.toString(),
                        option_a = opt_a.text.toString(),
                        option_b = opt_b.text.toString(),
                        option_c = opt_c.text.toString(),
                        option_d = opt_d.text.toString(),
                        correct_answer = spinner_answer.selectedItem.toString()
                    )
                } else {
                    // Add a new question to the list
                    questionList.add(
                        Question(
                            question_no = question_count.toString(),
                            question_text = main_question.text.toString(),
                            option_a = opt_a.text.toString(),
                            option_b = opt_b.text.toString(),
                            option_c = opt_c.text.toString(),
                            option_d = opt_d.text.toString(),
                            correct_answer = spinner_answer.selectedItem.toString()
                        )
                    )
                    question_count++
                }

                // Move to the next question or display an empty form for a new question
                count++
                if (count < questionList.size) {
                    val next_question: Question = questionList[count]
                    tv_question_no.setText("Q" + next_question.question_no + ". ")
                    main_question.setText(next_question.question_text)
                    opt_a.setText(next_question.option_a)
                    opt_b.setText(next_question.option_b)
                    opt_c.setText(next_question.option_c)
                    opt_d.setText(next_question.option_d)
                    val position = spinnerOption.indexOf(next_question.correct_answer)
                    spinner_answer.setSelection(position)
                    isLastquestion = true
                } else {
                    tv_question_no.setText("Q$question_count. ")
                    main_question.setText("")
                    opt_a.setText("")
                    opt_b.setText("")
                    opt_c.setText("")
                    opt_d.setText("")
                    spinner_answer.setSelection(0)
                    isLastquestion = false
                }

                ll_previous.visibility = View.VISIBLE
            }
        }

        host_btn.setOnClickListener {
            if (!isInternetAvailable(this)) {
                showNoInternetDialog()
                return@setOnClickListener
            }
            // Disable the button to prevent multiple clicks
            try {
                host_btn.isEnabled = false
                var posMark = et_pos_mark.text.toString().toDoubleOrNull() ?: 0.0
//            var negMark = et_neg_mark.text.toString().toDoubleOrNull() ?: 0.0
                var passMark = et_pass_mark.text.toString().toDoubleOrNull() ?: 0.0

                if (subject_name.text.isEmpty()) {
                    Toast.makeText(this, "Enter The Subject Name", Toast.LENGTH_LONG).show()
                    host_btn.isEnabled = true  // Re-enable the button
                } else if (posMark <= 0) {
                    Toast.makeText(this, "Enter The Positive Mark", Toast.LENGTH_LONG).show()
                    host_btn.isEnabled = true  // Re-enable the button
                } else if (et_neg_mark.text.isEmpty() || et_neg_mark.text.equals(".")) {
                    Toast.makeText(this, "Enter The Negative Mark", Toast.LENGTH_LONG).show()
                    host_btn.isEnabled = true  // Re-enable the button
                } else if (passMark <= 0) {
                    Toast.makeText(this, "Enter The Passing Mark", Toast.LENGTH_LONG).show()
                    host_btn.isEnabled = true  // Re-enable the button
                } else if (questionList.isEmpty()) {
                    Toast.makeText(this, "Enter The Questions With Options", Toast.LENGTH_LONG)
                        .show()
                    host_btn.isEnabled = true  // Re-enable the button
                } else {
                    if (set_time_switch.isChecked) {
                        if (tv_start_time.text.isEmpty()) {
                            Toast.makeText(this, "Set The Exam Start Time", Toast.LENGTH_LONG)
                                .show()
                            host_btn.isEnabled = true  // Re-enable the button
                            return@setOnClickListener
                        } else if (tv_end_time.text.isEmpty()) {
                            Toast.makeText(this, "Set The Exam End Time", Toast.LENGTH_LONG).show()
                            host_btn.isEnabled = true  // Re-enable the button
                            return@setOnClickListener
                        } else if (exam_duration_min <= 0 && exam_duration_hrs <= 0) {
                            Toast.makeText(this, "Set The Exam Duration", Toast.LENGTH_LONG).show()
                            host_btn.isEnabled = true  // Re-enable the button
                            return@setOnClickListener
                        }
                    }
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        var username = firebaseAuth.currentUser?.email.toString()
                        val visibility = if (rbPublic.isChecked) "Public" else "Subscribers"
                        db.collection("personalDetails").document(userId).get()
                            .addOnSuccessListener { document ->
                                username = document.get("name").toString()
                                val create_questions = CreateQuestions(
                                    candidate_id = user.toString(),
                                    exam_id = if (isEditMode) examToEdit?.exam_id else "",
                                    sub_nm = subject_name.text.toString(),
                                    start_time = tv_start_time.text.toString(),
                                    end_time = tv_end_time.text.toString(),
                                    exam_avl_time = tv_total_time.text.toString(),
                                    exam_duration = (exam_duration_hrs + exam_duration_min).toString(),
                                    pos_mark = et_pos_mark.text.toString(),
                                    neg_mark = et_neg_mark.text.toString(),
                                    pass_mark = et_pass_mark.text.toString(),
                                    hosting_date = if (isEditMode) examToEdit?.hosting_date else Date().toString(),
                                    hosted_by = username,
                                    visibility = visibility,
                                    questions = questionList
                                )

                                if (isEditMode) {
                                    updateExam(create_questions)
                                } else {
                                    hostExam(create_questions, username)
                                }
                            }
                    }
                }
            }catch (e: Exception) {
                Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                host_btn.isEnabled = true  // Re-enable the button
            }
        }
    }

    private fun hostExam(create_questions: CreateQuestions, username: String) {
        val user = firebaseAuth.currentUser?.uid.toString()
        db.collection("CreatedQuestion").document(user)
            .collection("QuestionsDetails").add(create_questions)
            .addOnSuccessListener { documentReference ->
                setExamId(user) { newExamId ->
                    documentReference.update("exam_id", newExamId)
                        .addOnSuccessListener {
                            db.collection("Exams")
                                .add(create_questions)
                                .addOnSuccessListener { examDocRef ->
                                    examDocRef.update("exam_id", newExamId)
                                    notifySubscribersOfExam(user, username, create_questions.sub_nm ?: "", newExamId)
                                    Toast.makeText(this, "Exam Created", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, HostedTest::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to insert data", Toast.LENGTH_LONG).show()
                                    host_btn.isEnabled = true
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to insert data", Toast.LENGTH_LONG).show()
                            host_btn.isEnabled = true
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to insert the data", Toast.LENGTH_LONG).show()
                host_btn.isEnabled = true
            }
    }

    private fun updateExam(create_questions: CreateQuestions) {
        val user = firebaseAuth.currentUser?.uid.toString()
        val examId = create_questions.exam_id ?: return

        // Update in CreatedQuestion
        db.collection("CreatedQuestion").document(user)
            .collection("QuestionsDetails").whereEqualTo("exam_id", examId)
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("CreatedQuestion").document(user)
                        .collection("QuestionsDetails").document(document.id)
                        .set(create_questions)
                }
                
                // Update in Exams
                db.collection("Exams").whereEqualTo("exam_id", examId)
                    .get().addOnSuccessListener { examDocs ->
                        for (doc in examDocs) {
                            db.collection("Exams").document(doc.id).set(create_questions)
                        }
                        Toast.makeText(this, "Exam Updated Successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, HostedTest::class.java))
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update exam", Toast.LENGTH_LONG).show()
                host_btn.isEnabled = true
            }
    }

    private fun setupEditMode() {
        val exam = examToEdit ?: return
        
        host_btn.text = "SUBMIT"
        host_btn.isEnabled = true
        subject_name.setText(exam.sub_nm)
        et_pos_mark.setText(exam.pos_mark)
        et_neg_mark.setText(exam.neg_mark)
        et_pass_mark.setText(exam.pass_mark)

        if (exam.visibility == "Subscribers") {
            rbSubscribers.isChecked = true
        } else {
            rbPublic.isChecked = true
        }
        
        if (!exam.start_time.isNullOrEmpty()) {
            set_time_switch.isChecked = true
            tv_start_time.text = exam.start_time
            tv_end_time.text = exam.end_time
            tv_total_time.text = exam.exam_avl_time
        }
        
        val durationMillis = exam.exam_duration?.toLongOrNull() ?: 0
        val totalMins = durationMillis / (60 * 1000)
        val hrs = (totalMins / 60).toInt()
        val mins = (totalMins % 60).toInt()
        
        np_exam_duration_hrs.value = hrs
        np_exam_duration_mins.value = mins
        tv_exm_hrs.text = "$hrs hours"
        tv_exm_min.text = "$mins min"
        exam_duration_hrs = hrs * 60 * 60 * 1000
        exam_duration_min = mins * 60 * 1000

        exam.questions?.let {
            questionList.addAll(it)
            if (questionList.isNotEmpty()) {
                val firstQ = questionList[0]
                tv_question_no.text = "Q${firstQ.question_no}. "
                main_question.setText(firstQ.question_text)
                opt_a.setText(firstQ.option_a)
                opt_b.setText(firstQ.option_b)
                opt_c.setText(firstQ.option_c)
                opt_d.setText(firstQ.option_d)
                
                val spinnerOption = arrayOf("Select The Answer","A","B","C","D")
                val position = spinnerOption.indexOf(firstQ.correct_answer)
                spinner_answer.post {
                    if (position >= 0) {
                        spinner_answer.setSelection(position)
                    }
                }
                
                question_count = questionList.size + 1
            }
        }
    }

    private fun updateLayoutVisibility(isSwitchChecked: Boolean) {
        ll_set_time.visibility = if (isSwitchChecked) View.VISIBLE else View.GONE
        ll_total_time.visibility = if (isSwitchChecked) View.VISIBLE else View.GONE
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
                            textView.text = formattedDateTime

                            if (textView == tv_start_time) {
                                startCalendar = selectedDate
                                set_end_time.isEnabled = true
                                set_end_time.alpha = 1.0f
                                tv_end_time.text=""
                                // Set the minimum date for end date picker to start date
                                endCalendar = startCalendar.clone() as Calendar

                            } else if (textView == tv_end_time) {
                                if (selectedDate.timeInMillis < startCalendar.timeInMillis) {
                                    Toast.makeText(
                                        this,
                                        "End date cannot be before start date.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    endCalendar = selectedDate
                                    calculateDifference()
                                }
                            }
                        }
                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    false // false for 12-hour format
                )
                timepickerDialog.show()
                timepickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.onPrimary))
                timepickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.onPrimary))
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        if (textView == tv_end_time && startCalendar.timeInMillis > currentCalendar.timeInMillis) {
            datePickerDialog.datePicker.minDate = startCalendar.timeInMillis
        } else {
            datePickerDialog.datePicker.minDate = currentCalendar.timeInMillis
        }

        datePickerDialog.show()
        datePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.onPrimary))
        datePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.onPrimary))

    }


    private fun calculateDifference() {
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis

        if (diffInMillis < 0) {
            Toast.makeText(this, "End time cannot be before start time.", Toast.LENGTH_LONG).show()
            return
        }

        val days = diffInMillis / (24 * 60 * 60 * 1000)
        val hours = (diffInMillis / (60 * 60 * 1000)) % 24
        val minutes = (diffInMillis / (60 * 1000)) % 60

        tv_total_time.text = "$days days, $hours hours, $minutes minutes"
    }

    fun setExamId(user: String, callback: (String) -> Unit) {
        db.collection("Exams")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    var maxExamId = 253465 // Default value if no documents are found
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            val examIdString = document.getString("exam_id")
                            val examId = examIdString?.toIntOrNull() ?: continue
                            if (examId > maxExamId) {
                                maxExamId = examId
                            }
                        }
                        maxExamId += 1
                    }
                    callback(maxExamId.toString())
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.exception)
                    callback("253465")
                }
            }
    }

    private fun showConfirmation() {
        val title = if (isEditMode) "Discard Changes?" else "Exit?"
        val message = if (isEditMode) 
            "Are you sure you want to discard the changes you've made to this exam?" 
            else "Are you sure you want to exit? All unsaved data will be lost."

        CustomDialogUtils.showConfirm(
            activity = this,
            title = title,
            message = message,
            onPositive = {
                finish()
            }
        )
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetDialog() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            positiveText = "Retry",
            negativeText = "Exit",
            onPositive = {
                host_btn.performClick()
            },

        )
    }

    private fun notifySubscribersOfExam(
        uid: String,
        name: String,
        subject: String,
        examId: String
    ) {
        db.collection("personalDetails").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val imageUrl = userDoc.getString("imageUrl") ?: ""
                db.collection("Subscribers").document(uid)
                    .collection("UserSubscribers")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val subscriberId = document.id
                            val notification = com.example.testmaster.model.Notification(
                                title = "New Exam Hosted!",
                                message = "$name has hosted a new exam on $subject.",
                                type = "EXAM_HOST",
                                fromUserId = uid,
                                fromUserName = name,
                                fromUserImage = imageUrl,
                                targetId = examId
                            )
                            com.example.testmaster.util.NotificationHelper.sendNotification(this@CreateMcqTest, subscriberId, notification)
                        }
                    }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
