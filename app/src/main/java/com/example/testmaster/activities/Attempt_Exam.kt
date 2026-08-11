package com.example.testmaster.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.testmaster.fragments.FragmentQuestion
import com.example.testmaster.R
import com.example.testmaster.adapter.QuestionsPagerAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.model.Question
import com.example.testmaster.model.QuestionWithAns
import com.example.testmaster.model.model_reportedQuestion
import com.example.testmaster.model.model_savedQuestion
import com.example.testmaster.util.CustomDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date
import java.util.UUID

class Attempt_Exam : AppCompatActivity(), FragmentQuestion.OnQuestionInteractionListener {

    lateinit var drawer_layout : DrawerLayout
    lateinit var gridLayout : GridLayout
    lateinit var iv_marked: ImageView
    lateinit var iv_report: ImageView
    lateinit var iv_saved: ImageView
    lateinit var iv_overflow: ImageView
    lateinit var marked_count: TextView
    lateinit var attempt_count: TextView
    lateinit var unattempt_count: TextView
    lateinit var unseen_count: TextView
    lateinit var ll_icons_container : LinearLayout

    lateinit var subject_name: TextView
    lateinit var tv_exam_timer: TextView
    lateinit var pauseAndResume: ImageView
    lateinit var ll_next: LinearLayout
    lateinit var tv_next: TextView
    lateinit var iv_next_arrow: ImageView
    lateinit var ll_previous: LinearLayout
    lateinit var clear_options: TextView
    lateinit var question_no: TextView
    lateinit var tv_quesion_duration: TextView
    lateinit var tv_positive_mark: TextView
    lateinit var tv_negative_mark: TextView
    lateinit var submit_quesion_btn: ImageButton
    lateinit var btn_submit: Button

    private var timer: CountDownTimer? = null

    private var isTimerRunning = false
    private var isMarked = false
    private var isReported = false
    private var isSaved = false
    private var exam_status = "C"

    private var timeLeftInMillis: Long = 0
    var currentIndex = 0
    private lateinit var questions: List<Question>
    private lateinit var viewPager: ViewPager2
    private lateinit var questionsPagerAdapter: QuestionsPagerAdapter
    lateinit var QuestionWithAnsList: MutableList<QuestionWithAns>
    private lateinit var questionCircles: MutableList<TextView>
    lateinit var chosenAnswerList: ArrayList<String>
    private var questionTimeInMillis: Long = 0
    private var examRemainingTime : Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            questionTimeInMillis += 1000
            val seconds = (questionTimeInMillis / 1000) % 60
            val minutes = (questionTimeInMillis / (1000 * 60)) % 60
            setquesiontimer(seconds, minutes)
            handler.postDelayed(this, 1000)
        }
    }
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    var report_title = ""
    var report_description = ""
    lateinit var userId: String
    lateinit var userName: String
    lateinit var examData: CreateQuestions
    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_exam)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_view)) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, topInset, 0, 0)
            insets
        }
        onBackPressedDispatcher.addCallback(this) {
            showConfirmation()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        val prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
        userName = prefs.getString("name", "") ?: "Unknown"
        pauseAndResume = findViewById(R.id.pauseAndResume)
        subject_name = findViewById(R.id.subject_name)
        tv_exam_timer = findViewById(R.id.tv_exam_timer)
        viewPager = findViewById(R.id.viewPager)
        ll_previous = findViewById(R.id.ll_previous)
        ll_next = findViewById(R.id.ll_next)
        tv_next = findViewById(R.id.tv_next)
        iv_next_arrow = findViewById(R.id.iv_next_arrow)
        clear_options = findViewById(R.id.clear_options)
        question_no = findViewById(R.id.question_no)
        tv_positive_mark = findViewById(R.id.positive_marks)
        tv_negative_mark = findViewById(R.id.negative_marks)
        tv_quesion_duration = findViewById(R.id.tv_question_duration)
        submit_quesion_btn = findViewById(R.id.sumit_quesion_btn)
        iv_marked = findViewById(R.id.iv_marked)
        iv_report = findViewById(R.id.iv_report)
        iv_saved = findViewById(R.id.iv_saved)
        iv_overflow = findViewById<ImageView>(R.id.iv_overflow)
        drawer_layout = findViewById(R.id.drawer_layout)
        btn_submit = findViewById(R.id.btn_submit)

        marked_count = findViewById(R.id.marked_count)
        attempt_count = findViewById(R.id.attempt_count)
        unattempt_count = findViewById(R.id.unattempt_count)
        unseen_count = findViewById(R.id.unseen_count)
        ll_icons_container = findViewById(R.id.ll_icons_container)
        gridLayout = findViewById<GridLayout>(R.id.grid_layout)


        examData = intent.getSerializableExtra("examData") as CreateQuestions
        val pauseAnswerKey = if (intent.hasExtra("Paused_Answer_Key")) {
            intent.getSerializableExtra("Paused_Answer_Key") as AnswerKey
        } else {
            null
        }

        ll_icons_container.viewTreeObserver.addOnGlobalLayoutListener {
            val totalWidth = ll_icons_container.width
            val iconsWidth = iv_marked.width + iv_report.width + iv_saved.width

            if (iconsWidth > totalWidth) {
                // Icons don't fit, hide them and show the overflow menu
                iv_marked.visibility = View.GONE
                iv_report.visibility = View.GONE
                iv_saved.visibility = View.GONE
                iv_overflow.visibility = View.VISIBLE
            } else {
                // Icons fit, show them and hide the overflow
                iv_marked.visibility = View.VISIBLE
                iv_report.visibility = View.VISIBLE
                iv_saved.visibility = View.VISIBLE
                iv_overflow.visibility = View.GONE
            }
        }

        iv_overflow.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.overflow_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_marked -> {
                        isMarked = !isMarked
                        saveCurrentQuestionDetails()
                        true
                    }
                    R.id.menu_report -> {
                        isReported = !isReported
                        saveCurrentQuestionDetails()
                        true
                    }
                    R.id.menu_saved -> {
                        isSaved = !isSaved
                        saveCurrentQuestionDetails()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
        questions = examData.questions ?: emptyList()
        val positive_mark = examData.pos_mark?.toDouble()
        val negative_mark = examData.neg_mark?.toDouble()
        var exam_duration = examData.exam_duration?.toLong()
        chosenAnswerList = ArrayList(questions.size)
        questionCircles = mutableListOf()
        repeat(questions.size) { chosenAnswerList.add("N") }

        QuestionWithAnsList = MutableList(questions.size) { index ->
            QuestionWithAns(
                question_no = questions[index].question_no,
                question_text = questions[index].question_text,
                option_a = questions[index].option_a,
                option_b = questions[index].option_b,
                option_c = questions[index].option_c,
                option_d = questions[index].option_d,
                choosen_answer = "N",
                correct_answer = questions[index].correct_answer,
                question_time = "0",
                report = "N",
                saved = "N",
                marked = "N"
            )
        }
        createQuestionStatusCircle()

        if(pauseAnswerKey?.exam_status.equals("I")){
            repeat(pauseAnswerKey?.questionsWithAns?.size ?: 0) { chosenAnswerList.add("N") }
            pauseAnswerKey?.questionsWithAns?.let {
                QuestionWithAnsList = MutableList(it.size) { index ->
                    QuestionWithAns(
                        question_no = it[index].question_no,
                        question_text = it[index].question_text,
                        option_a = it[index].option_a,
                        option_b = it[index].option_b,
                        option_c = it[index].option_c,
                        option_d = it[index].option_d,
                        choosen_answer = it[index].choosen_answer,
                        correct_answer = it[index].correct_answer,
                        question_time = it[index].question_time,
                        report = it[index].report,
                        saved = it[index].saved,
                        marked = it[index].marked
                    )
                }
            }

            for(i in 0..<(pauseAnswerKey?.questionsWithAns?.size ?: 0)) {
                chosenAnswerList[i] = pauseAnswerKey?.questionsWithAns?.get(i)?.choosen_answer.toString()
                var pre_choseAns = pauseAnswerKey?.questionsWithAns?.get(i)
                var marked = pauseAnswerKey?.questionsWithAns?.get(i)?.marked
                val questionCircle = questionCircles[i]
                if (pre_choseAns?.choosen_answer != "N" && marked != "Y") {
                    question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetint))
                    question_no.setTextColor(ContextCompat.getColor(this, R.color.white))
                    questionCircle.setBackgroundResource(R.drawable.attempt_circle_background)
                    questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else if (pre_choseAns?.question_time != "0" && marked != "Y") {
                    question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
                    questionCircle.setBackgroundResource(R.drawable.unattempt_circle_background)
                    questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else if (pre_choseAns?.choosen_answer != "N" && marked == "Y") {
                    question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetint))
                    question_no.setTextColor(ContextCompat.getColor(this, R.color.white))
                    questionCircle.setBackgroundResource(R.drawable.attempt_with_star)
                    questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else if (pre_choseAns?.choosen_answer != "0" && marked == "Y") {
                    question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
                    questionCircle.setBackgroundResource(R.drawable.unattempt_with_star)
                    questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    questionCircle.setBackgroundResource(R.drawable.unseen_circle_background)
                }
                marked_count.setText(QuestionWithAnsList.filter { it.marked == "Y" }.size.toString())
                attempt_count.setText(QuestionWithAnsList.filter { it.choosen_answer != "N" }.size.toString())
                unattempt_count.setText(QuestionWithAnsList.filter { it.choosen_answer == "N" && it.question_time != "0" }.size.toString())
                unseen_count.setText(QuestionWithAnsList.filter { it.question_time == "0" }.size.toString())
            }
            exam_duration = pauseAnswerKey?.exam_remaining_time?.toLong() ?: 0
        }

        questionsPagerAdapter = QuestionsPagerAdapter(this, questions)
        viewPager.adapter = questionsPagerAdapter
//        subject_name.text = examData?.get(0)?.sub_nm
        subject_name.text = examData.sub_nm
        tv_positive_mark.text = "+ ${positive_mark}"
        tv_negative_mark.text = "- ${negative_mark}"
        question_no.text = "2"

        startTimer(exam_duration ?: 3000)
        handler.post(runnable)

        pauseAndResume.setOnClickListener {
            if (isTimerRunning) {
                handler.removeCallbacks(runnable)
                pauseTimer()
            } else {
                resumeTimer()
                handler.post(runnable)
            }
        }

        ll_next.setOnClickListener {
            if (viewPager.currentItem == questions.size - 1) {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_box_confirmation)
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                dialog.setCancelable(false)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                val dialogTitle = dialog.findViewById<TextView>(R.id.title)
                val dialogMessage = dialog.findViewById<TextView>(R.id.message)
                val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
                val btnNo = dialog.findViewById<Button>(R.id.btn_no)
                dialogTitle.text = "Confirmation"
                dialogMessage.text = "Are you sure you want to submit the exam?"
                dialog.show()
                btnYes.setOnClickListener {
                    dialog.dismiss()
                    submitTest()
                }
                btnNo.setOnClickListener { dialog.dismiss() }
            } else {
                moveToNextQuestion()
            }
        }

        ll_previous.setOnClickListener {
            moveToPreviousQuestion()
        }

        clear_options.setOnClickListener {
            onClearOptions()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            var lastpostion = false

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    if(currentIndex==0 && !lastpostion){
                        saveCurrentQuestionDetails()
                        lastpostion = true
                    }else if (currentIndex!=0){
                        saveCurrentQuestionDetails()
                        resetMarkedAndSavedBtn()
                        lastpostion = false
                    }

                }
            }

            override fun onPageSelected(position: Int) {
                currentIndex = position

                restoreQuestionTime()
                updateButtonStates()
                updateQuestionNumber()
            }
        })

        updateButtonStates()
        iv_marked.setOnClickListener{
            isMarked = !isMarked
            iv_marked.setColorFilter(ContextCompat.getColor(this, if (isMarked) R.color.red else R.color.darkgray))
            saveCurrentQuestionDetails()
        }
        iv_report.setOnClickListener {
            if (isReported) {
                isReported = !isReported
                iv_report.setColorFilter(ContextCompat.getColor(this, if (isReported) R.color.red else R.color.darkgray))
                report_title = ""
                report_description = ""
                saveCurrentQuestionDetails()
            } else {
                report_title = ""
                report_description = ""
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.popup_report_question)
                dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setDimAmount(0.7f)
                val ll_wrong: View = dialog.findViewById(R.id.ll_wrong)
                val ll_error: View = dialog.findViewById(R.id.ll_error)
                val ll_incorrect_ans: View = dialog.findViewById(R.id.ll_incorrect_ans)
                val ll_notvisible: View = dialog.findViewById(R.id.ll_notvisible)
                val ll_spellingmistake: View = dialog.findViewById(R.id.ll_spellingmistake)
                val ll_other: View = dialog.findViewById(R.id.ll_other)
                val et_report_description: EditText =
                    dialog.findViewById(R.id.et_report_description)
                val iv_report_send: ImageView = dialog.findViewById(R.id.iv_report_send)
                val btn_close_report: View = dialog.findViewById(R.id.btn_close_report)
                dialog.setCancelable(false)
                ll_wrong.setOnClickListener {
                    ll_wrong.isSelected = !ll_wrong.isSelected
                    report_title = "Wrong Question"
                }
                ll_error.setOnClickListener {
                    ll_error.isSelected = !ll_error.isSelected
                    report_title = "Grammatical Error"
                }
                ll_incorrect_ans.setOnClickListener {
                    ll_incorrect_ans.isSelected = !ll_incorrect_ans.isSelected
                    report_title = "Incorrect answer options"
                }
                ll_notvisible.setOnClickListener {
                    ll_notvisible.isSelected = !ll_notvisible.isSelected
                    report_title = "Question not visible"
                }
                ll_spellingmistake.setOnClickListener {
                    ll_spellingmistake.isSelected = !ll_spellingmistake.isSelected
                    report_title = "Spelling mistake"
                }
                ll_other.setOnClickListener {
                    ll_other.isSelected = !ll_other.isSelected
                    report_title = "other"
                }
                et_report_description.setOnClickListener {
                    report_description = et_report_description.text.toString()
                }
                iv_report_send.setOnClickListener {
                    report_description = et_report_description.text.toString().trim()
                    val wordCount = report_description.split(" ").filter { it.isNotBlank() }.size
                    if (!ll_wrong.isSelected && !ll_error.isSelected && !ll_incorrect_ans.isSelected && !ll_notvisible.isSelected && !ll_spellingmistake.isSelected && !ll_other.isSelected) {
                        Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    } else if (wordCount < 7) {
                        Toast.makeText(
                            this,
                            "Write at least 7-8 words about the report",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (ll_wrong.isSelected || ll_error.isSelected || ll_incorrect_ans.isSelected || ll_notvisible.isSelected || ll_spellingmistake.isSelected || ll_other.isSelected && wordCount >= 7) {
                        isReported = !isReported
                        iv_report.setColorFilter(
                            ContextCompat.getColor(
                                this,
                                if (isReported) R.color.gold else R.color.darkgray
                            )
                        )
                        saveCurrentQuestionDetails()
                        dialog.dismiss()
                    }
                }
                btn_close_report.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
        iv_saved.setOnClickListener{
            isSaved = !isSaved
            iv_saved.setColorFilter(ContextCompat.getColor(this, if (isSaved) R.color.greentint else R.color.darkgray))
            saveCurrentQuestionDetails()
        }
        submit_quesion_btn.setOnClickListener{
            drawer_layout.openDrawer(GravityCompat.END)
        }
//

        btn_submit.setOnClickListener{
            submitTest()
        }
    }

    private fun submitTest() {
        btn_submit.isEnabled = false
        saveCurrentQuestionDetails()
        val examdata = examData
        val examId = examdata.exam_id
        var correctAnswer = 0
        var unattemptQuestion = 0
        var attemptQuestion = 0
        var wrongAnswer = 0
        val totalQuestions = examdata.questions?.size ?: 0
        val exam_duration_long = examdata.exam_duration?.toLongOrNull() ?: 0L
        val examcompleteDuration = exam_duration_long - timeLeftInMillis
        val examRemainingTime = exam_duration_long - examcompleteDuration
        var incorrectQuestion = 0

        updateNoOfAttempt(examId.toString(), user.toString()) { noOfAttempt, docIdExists ->
            for (i in 0..< (examdata.questions?.size ?: 0)) {
                if (QuestionWithAnsList.get(i).choosen_answer == QuestionWithAnsList.get(i).correct_answer) {
                    correctAnswer += 1
                }
                if (QuestionWithAnsList.get(i).choosen_answer == "N") {
                    unattemptQuestion += 1
                }
                if (QuestionWithAnsList.get(i).choosen_answer != "N") {
                    attemptQuestion += 1
                }
            }
            wrongAnswer = attemptQuestion - correctAnswer
            val posMark = examdata.pos_mark?.toFloatOrNull() ?: 0f
            val negMark = examdata.neg_mark?.toFloatOrNull() ?: 0f
            val correct_marks = posMark * correctAnswer
            var incorrect_marks = negMark * wrongAnswer
            val totalMarks = correct_marks - incorrect_marks
            incorrectQuestion = ((totalQuestions - unattemptQuestion) - correctAnswer)
            val accuracyPercent =
                if (totalQuestions - unattemptQuestion > 0)
                    (correctAnswer.toDouble() / (totalQuestions - unattemptQuestion)) * 100
                else 0.0

            if (user != null) {
                val AnswerKeyObj = AnswerKey(
                    candidate_id = examdata.candidate_id.toString(),
                    exam_id = examdata.exam_id,
                    sub_nm = subject_name.text.toString(),
                    start_time = examdata.start_time,
                    end_time = examdata.end_time,
                    exam_avl_time = examdata.exam_avl_time,
                    exam_duration = examdata.exam_duration,
                    exam_remaining_time = examRemainingTime.toString(),
                    pos_mark = examdata.pos_mark,
                    neg_mark = examdata.neg_mark,
                    pass_mark = examdata.pass_mark,
                    hosting_date = examdata.hosting_date,
                    hosted_by = examdata.hosted_by,
                    appear_by = userName,
                    attempt_date = Date().toString(),
                    no_of_attempt = noOfAttempt,
                    exam_complete_duration = examcompleteDuration.toString(),
                    exam_status = exam_status,
                    total_score = totalMarks.toString(),
                    correct_question = correctAnswer.toString(),
                    incorrect_question = incorrectQuestion.toString(),
                    unattempt = unattemptQuestion.toString(),
                    accuracy = accuracyPercent.toString(),
                    questionsWithAns = QuestionWithAnsList
                )

                val savedList: List<QuestionWithAns> =
                    QuestionWithAnsList.filter { it.saved == "Y" }
                val reportList: List<QuestionWithAns> =
                    QuestionWithAnsList.filter { it.report == "Y" }
                val subjectName = subject_name.text.toString()

                val savedQuestionsList = savedList.map { question ->
                    model_savedQuestion(
                        id = UUID.randomUUID().toString(),
                        subject_name = subjectName,
                        question_no = question.question_no,
                        question_text = question.question_text,
                        option_a = question.option_a,
                        option_b = question.option_b,
                        option_c = question.option_c,
                        option_d = question.option_d,
                        choosen_answer = question.choosen_answer,
                        correct_answer = question.correct_answer,
                        question_time = question.question_time,
                        report = question.report,
                        saved = question.saved,
                        marked = question.marked
                    )
                }
                val reportQuestionsList = reportList.map { question ->
                    model_reportedQuestion(
                        id = UUID.randomUUID().toString(),
                        reported_by_ID = user,
                        examHost_by_ID = AnswerKeyObj.candidate_id,
                        exam_id = AnswerKeyObj.exam_id,
                        subject_name = subjectName,
                        question_no = question.question_no,
                        question_text = question.question_text,
                        option_a = question.option_a,
                        option_b = question.option_b,
                        option_c = question.option_c,
                        option_d = question.option_d,
                        choosen_answer = question.choosen_answer,
                        correct_answer = question.correct_answer,
                        question_time = question.question_time,
                        report = question.report,
                        report_title = question.report_title,
                        report_description = question.report_description,
                        saved = question.saved,
                        marked = question.marked
                    )
                }

                if (docIdExists != null) {
                    db.collection("History").document(user.toString())
                        .collection("HistoryDetails").document(docIdExists)
                        .set(AnswerKeyObj)
                        .addOnSuccessListener {
                            val answerKeyRef =
                                db.collection("History").document(user.toString())
                                    .collection("HistoryDetails").document(docIdExists)

                            if (exam_status == "C") {
                                db.collection("Leaderboard")
                                    .document(examdata.candidate_id.toString())
                                    .collection("LeaderboardDetails").document(user)
                                    .update(
                                        "answerKeyRefs",
                                        FieldValue.arrayUnion(answerKeyRef)
                                    )
                                    .addOnSuccessListener {
                                        handleSavedAndReportedQuestions(
                                            savedQuestionsList,
                                            reportQuestionsList,
                                            AnswerKeyObj
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        db.collection("Leaderboard")
                                            .document(examdata.candidate_id.toString())
                                            .collection("LeaderboardDetails").document(user)
                                            .set(hashMapOf("answerKeyRefs" to listOf(answerKeyRef)))
                                            .addOnSuccessListener {
                                                handleSavedAndReportedQuestions(
                                                    savedQuestionsList,
                                                    reportQuestionsList,
                                                    AnswerKeyObj
                                                )
                                            }
                                    }
                            } else {
                                handleSavedAndReportedQuestions(
                                    savedQuestionsList,
                                    reportQuestionsList,
                                    AnswerKeyObj
                                )
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed To Submit Exam", Toast.LENGTH_LONG).show()
                            btn_submit.isEnabled = true
                        }
                } else {
                    db.collection("History").document(user.toString())
                        .collection("HistoryDetails").add(AnswerKeyObj)
                        .addOnSuccessListener { documentReference ->
                            val answerKeyRef = documentReference
                            if (exam_status == "C") {
                                db.collection("Leaderboard")
                                    .document(examdata.candidate_id.toString())
                                    .collection("LeaderboardDetails").document(user)
                                    .set(hashMapOf("answerKeyRefs" to listOf(answerKeyRef)))
                                    .addOnSuccessListener {
                                        handleSavedAndReportedQuestions(
                                            savedQuestionsList,
                                            reportQuestionsList,
                                            AnswerKeyObj
                                        )
                                    }
                            } else {
                                handleSavedAndReportedQuestions(
                                    savedQuestionsList,
                                    reportQuestionsList,
                                    AnswerKeyObj
                                )
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed To Submit Exam", Toast.LENGTH_LONG).show()
                            btn_submit.isEnabled = true
                        }
                }
            }
        }
    }

    private fun createQuestionStatusCircle(){
        for (i in 1..QuestionWithAnsList.size) {
            val questionCircle = TextView(this).apply {
                text = i.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.circle_outline)
                setPadding(20, 20, 20, 20)
                setOnClickListener {
                    saveCurrentQuestionDetails()
                    viewPager.currentItem = i - 1
                }
            }
            val params = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(10, 10, 10, 10)
            }
            gridLayout.addView(questionCircle, params)
            if(QuestionWithAnsList.size<5) {
                gridLayout.columnCount = QuestionWithAnsList.size
            }
            questionCircles.add(questionCircle)
        }
    }
    private fun updateNoOfAttempt(examId: String, user: String, callback: (String, String?) -> Unit) {
        val query = db.collection("History").document(user)
            .collection("HistoryDetails").whereEqualTo("exam_id",examId).limit(1)
        query.get()
            .addOnSuccessListener  { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val noOfAttempt = querySnapshot.documents[0].getString("no_of_attempt")?.toIntOrNull() ?: 0
                    val docIdExists = querySnapshot.documents[0].id
                    callback(
                        "${noOfAttempt + 1}",
                        docIdExists
                    ) // Return updated attempt and document ID
                }else {
                    Log.d("History", "no history present")
                    callback("1", null) // Error, set attempt to 1 and docId to null
                }
            }.addOnFailureListener {
                callback("1", null) // No exam found, set attempt to 1 and docId to null
            }
    }
    private fun handleSavedAndReportedQuestions(savedQuestionsList: List<model_savedQuestion>, reportQuestionsList: List<model_reportedQuestion>, AnswerKey: AnswerKey?) {
        if (savedQuestionsList.isNotEmpty()) {
            val modelsavedQuestion = mapOf(
                "savedQuestionsList" to FieldValue.arrayUnion(*savedQuestionsList.toTypedArray())
            )
            db.collection("SavedQuestion").document(user.toString())
                .set(modelsavedQuestion, SetOptions.merge())
                .addOnFailureListener {
                    Toast.makeText(this, "Failed To Save Question", Toast.LENGTH_LONG).show()
                    btn_submit.isEnabled = true
                }
        }

        if (reportQuestionsList.isNotEmpty()) {
            val modelreportQuestion = mapOf(
                "reportQuestionsList" to FieldValue.arrayUnion(*reportQuestionsList.toTypedArray())
            )
            db.collection("ReportedQuestion").document(user)
                .collection("Report Questions")
                .document("your Reported Question")
                .set(modelreportQuestion, SetOptions.merge())
                .addOnFailureListener {
                    Toast.makeText(this, "Failed To Report Question", Toast.LENGTH_LONG).show()
                    btn_submit.isEnabled = true
                }
            db.collection("ReportedQuestion").document(AnswerKey?.candidate_id.toString())
                .collection("Reported Questions")
                .document("user Reported Question")
                .set(modelreportQuestion, SetOptions.merge())
                .addOnFailureListener {
                    Toast.makeText(this, "Failed To Send Report Question", Toast.LENGTH_LONG).show()
                    btn_submit.isEnabled = true
                }
        }
        timer?.cancel()
        if(exam_status=="C"){
            Toast.makeText(this, "Exam Submitted", Toast.LENGTH_LONG).show()
            val intent = Intent(this@Attempt_Exam, ResultActivity::class.java)
            intent.putExtra("examData", examData)
            intent.putExtra("Answer_Key", AnswerKey)
            startActivity(intent)
            finish()
        }
    }
    @SuppressLint("ResourceAsColor")
    private fun saveCurrentQuestionDetails() {
        val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? FragmentQuestion
        currentFragment?.let {
            val currentQuestionDetails = it.getQuestionDetails()
            currentQuestionDetails.question_no = (viewPager.currentItem + 1).toString()
            currentQuestionDetails.correct_answer = questions[viewPager.currentItem].correct_answer.toString()
            currentQuestionDetails.question_time = questionTimeInMillis.toString()
            currentQuestionDetails.report = if (isReported) "Y" else "N"
            currentQuestionDetails.report_title = if (isReported) report_title else ""
            currentQuestionDetails.report_description = if (!report_title.equals("")) report_description else ""
            currentQuestionDetails.saved = if (isSaved) "Y" else "N"
            currentQuestionDetails.marked = if (isMarked) "Y" else "N"
            if (it.selected_option != null) {
                if (chosenAnswerList.size > viewPager.currentItem) {
                    chosenAnswerList[viewPager.currentItem] = it.selected_option.toString()
                } else {
                    chosenAnswerList.add(viewPager.currentItem, it.selected_option.toString())
                }
                currentQuestionDetails.choosen_answer = chosenAnswerList[viewPager.currentItem]
            } else {
                if (chosenAnswerList.size > viewPager.currentItem) {
                    currentQuestionDetails.choosen_answer = chosenAnswerList[viewPager.currentItem]
                }
            }
            QuestionWithAnsList[viewPager.currentItem] = currentQuestionDetails

            val questionCircle = questionCircles[viewPager.currentItem]
            if (currentQuestionDetails.choosen_answer != "N" && currentQuestionDetails.marked!="Y") {
                question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetint))
                question_no.setTextColor(ContextCompat.getColor(this, R.color.white))
                questionCircle.setBackgroundResource(R.drawable.attempt_circle_background)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (currentQuestionDetails.question_time != "0" && currentQuestionDetails.marked!="Y") {
                question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
                questionCircle.setBackgroundResource(R.drawable.unattempt_circle_background)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (currentQuestionDetails.choosen_answer != "N" && currentQuestionDetails.marked=="Y") {
                question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetint))
                question_no.setTextColor(ContextCompat.getColor(this, R.color.white))
                questionCircle.setBackgroundResource(R.drawable.attempt_with_star)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (currentQuestionDetails.choosen_answer != "0" && currentQuestionDetails.marked=="Y") {
                question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
                questionCircle.setBackgroundResource(R.drawable.unattempt_with_star)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                questionCircle.setBackgroundResource(R.drawable.unseen_circle_background)
            }
            marked_count.setText(QuestionWithAnsList.filter { it.marked=="Y" }.size.toString())
            attempt_count.setText(QuestionWithAnsList.filter { it.choosen_answer!="N" }.size.toString())
            unattempt_count.setText(QuestionWithAnsList.filter { it.choosen_answer=="N" && it.question_time!="0" }.size.toString())
            unseen_count.setText(QuestionWithAnsList.filter { it.question_time=="0" }.size.toString())
        }
    }


    private fun restoreQuestionTime() {
        val currentQuestionDetails = QuestionWithAnsList[viewPager.currentItem]
        questionTimeInMillis = currentQuestionDetails.question_time?.toLongOrNull() ?: 0L
        handler.removeCallbacks(runnable)
        handler.post(runnable)
        if(currentQuestionDetails.choosen_answer!="N"){
            question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetint))
        }else{
            question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
        }
        isMarked = currentQuestionDetails.marked == "Y"
        iv_marked.setColorFilter(ContextCompat.getColor(this, if (isMarked) R.color.red else R.color.darkgray))

        isReported = currentQuestionDetails.report == "Y"
        iv_report.setColorFilter(ContextCompat.getColor(this, if (isReported) R.color.gold else R.color.darkgray))

        isSaved = currentQuestionDetails.saved == "Y"
        iv_saved.setColorFilter(ContextCompat.getColor(this, if (isSaved) R.color.greentint else R.color.darkgray))

        handler.postDelayed({
            val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? FragmentQuestion
            currentFragment?.restoreSelectedOption(currentQuestionDetails.choosen_answer)
        }, 100)
    }



    private fun startTimer(timeInMillis: Long) {
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                tv_exam_timer.text = "Time's finished!"
                btn_submit.performClick()
            }
        }.start()

        isTimerRunning = true
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        pauseAndResume.setImageResource(R.drawable.baseline_play_circle_outline_24)
    }

    private fun resumeTimer() {
        if (!isTimerRunning) {
            startTimer(timeLeftInMillis)
            pauseAndResume.setImageResource(R.drawable.baseline_pause_circle_outline_24)
        }
    }

    private fun updateTimerText() {
        val hours = timeLeftInMillis / 1000 / 3600
        val minutes = (timeLeftInMillis / 1000 % 3600) / 60
        val seconds = timeLeftInMillis / 1000 % 60

        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tv_exam_timer.text = timeFormatted
    }

    private fun setquesiontimer(seconds : Long, minutes : Long) {
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tv_quesion_duration.text = timeFormatted
    }

    private fun updateButtonStates() {
        ll_previous.isEnabled = viewPager.currentItem > 0
        if (viewPager.currentItem == questions.size - 1) {
            tv_next.text = "Submit"
            iv_next_arrow.visibility = View.GONE
        } else {
            tv_next.text = "Next"
            iv_next_arrow.visibility = View.VISIBLE
        }
    }

    private fun updateQuestionNumber() {
        question_no.text = "${viewPager.currentItem + 1}"
    }

    private fun moveToNextQuestion() {
        if (viewPager.currentItem < (questions.size - 1)) {
            saveCurrentQuestionDetails()
            questionTimeInMillis = 0
            viewPager.currentItem += 1
//            resetMarkedAndSavedBtn()
//            restoreQuestionTime()
        }
    }

    private fun moveToPreviousQuestion() {
        if (viewPager.currentItem > 0) {
//            saveCurrentQuestionDetails()
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
//            resetMarkedAndSavedBtn()
//            restoreQuestionTime()
        }
    }
    fun resetMarkedAndSavedBtn(){
        iv_marked.setColorFilter(ContextCompat.getColor(this, R.color.darkgray))
        isMarked=false

        iv_report.setColorFilter(ContextCompat.getColor(this, R.color.darkgray))
        isReported=false

        iv_saved.setColorFilter(ContextCompat.getColor(this, R.color.darkgray))
        isSaved=false
    }

    override fun onClearOptions() {
        val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? FragmentQuestion
        currentFragment?.clearOption()
        saveCurrentQuestionDetails()

    }

    override fun onNextQuestion() {
        moveToNextQuestion()
    }

    override fun onPreviousQuestion() {
        moveToPreviousQuestion()
    }

    override fun onUpdateQuestionTimer(seconds: Long, minutes: Long) {
        setquesiontimer(seconds, minutes)
    }
    override fun onOptionSelected() {
        saveCurrentQuestionDetails()
    }

    private fun showConfirmation() {
        CustomDialogUtils.showConfirm(
            activity = this,
            title = "Confirmation",
            message = "Are you sure you want to close this exam?",
            onPositive = {
                exam_status = "I"
                btn_submit.performClick()
                finish()
            }
        )
    }
}
