package com.example.testmaster

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.testmaster.adapter.AnalysisPagerAdapter
import com.example.testmaster.model.AnswerKey
import com.example.testmaster.model.QuestionWithAns

class Analysis_Exam : AppCompatActivity(), FragmentAnalysis.OnQuestionInteractionListener {
    private lateinit var analysisPagerAdapter : AnalysisPagerAdapter
    private lateinit var questionWithAns: List<QuestionWithAns>
    private lateinit var viewPager: ViewPager2
    private lateinit var answerKey: AnswerKey

    lateinit var question_no: TextView
    lateinit var tv_question_duration: TextView
    lateinit var tv_positive_mark: TextView
    lateinit var tv_negative_mark: TextView

    lateinit var drawer_layout : DrawerLayout
    lateinit var gridLayout : GridLayout
    lateinit var ll_previous : LinearLayout
    lateinit var ll_next : LinearLayout

    lateinit var iv_marked: ImageView
    lateinit var iv_report: ImageView
    lateinit var iv_saved: ImageView

    lateinit var marked_count: TextView
    lateinit var attempt_count: TextView
    lateinit var unattempt_count: TextView
    lateinit var unseen_count: TextView

    lateinit var subject_name: TextView
    lateinit var tv_exam_timer: TextView
    lateinit var btn_OpenDrawer: ImageButton
    lateinit var btn_submit: Button

    private lateinit var questionCircles: MutableList<TextView>

    private var examCompletionTime: Long = 0
    lateinit var answer : QuestionWithAns
    lateinit var chosenAnswerList: ArrayList<String>
    lateinit var correctAnswerList: ArrayList<String>
    lateinit var currentChooseAnswerList: ArrayList<String>
    private val handler = Handler(Looper.getMainLooper())


    @SuppressLint("ResourceAsColor", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis_exam)

        question_no = findViewById(R.id.question_no)
        tv_positive_mark = findViewById(R.id.positive_marks)
        tv_negative_mark = findViewById(R.id.negative_marks)
        tv_question_duration = findViewById(R.id.tv_question_duration)
        viewPager = findViewById(R.id.viewPager)

        subject_name = findViewById(R.id.subject_name)
        tv_exam_timer = findViewById(R.id.tv_exam_timer)
        viewPager = findViewById(R.id.viewPager)
        question_no = findViewById(R.id.question_no)
        tv_positive_mark = findViewById(R.id.positive_marks)
        tv_negative_mark = findViewById(R.id.negative_marks)
        btn_OpenDrawer = findViewById(R.id.sumit_quesion_btn)
        iv_marked = findViewById(R.id.iv_marked)
        iv_report = findViewById(R.id.iv_report)
        iv_saved = findViewById(R.id.iv_saved)
        drawer_layout = findViewById(R.id.drawer_layout)
        btn_submit = findViewById(R.id.btn_submit)
        marked_count = findViewById(R.id.marked_count)
        attempt_count = findViewById(R.id.attempt_count)
        unattempt_count = findViewById(R.id.unattempt_count)
        unseen_count = findViewById(R.id.unseen_count)
        ll_previous = findViewById(R.id.ll_previous)
        ll_next = findViewById(R.id.ll_next)
        gridLayout = findViewById<GridLayout>(R.id.grid_layout)

        answerKey = intent.getSerializableExtra("Answer_Key") as AnswerKey
        questionCircles = mutableListOf()
        chosenAnswerList = ArrayList(answerKey.questionsWithAns?.size?:0)
        correctAnswerList = ArrayList(answerKey.questionsWithAns?.size?:0)
        currentChooseAnswerList = ArrayList(answerKey.questionsWithAns?.size?:0)
        repeat(answerKey.questionsWithAns?.size?:0) {
            chosenAnswerList.add(answerKey.questionsWithAns?.get(it)?.choosen_answer ?: "N")
            correctAnswerList.add(answerKey.questionsWithAns?.get(it)?.correct_answer ?: "")
            currentChooseAnswerList.add("")
        }
        questionWithAns = answerKey.questionsWithAns?: emptyList()
        analysisPagerAdapter = AnalysisPagerAdapter(this, questionWithAns)
        viewPager.adapter = analysisPagerAdapter

        setDrawerCircle(questionWithAns.size)
        examCompletionTime = answerKey.exam_complete_duration?.toLong()?:0
        updateTimerText(examCompletionTime)
        subject_name.text = answerKey.sub_nm
        tv_positive_mark.text = "- %.1f".format(answerKey.pos_mark?.toFloat())
        tv_negative_mark.text = "- %.1f".format(answerKey.neg_mark?.toFloat())
        updateQuestionsData()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrollStateChanged(state: Int) {
//                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
//                }
//            }
            override fun onPageSelected(position: Int) {
                restoreSelectedOption()
                updateButtonStates()
                updateQuestionsData()
            }
        })
        ll_next.setOnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }

        ll_previous.setOnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
        }
        btn_OpenDrawer.setOnClickListener{
            drawer_layout.openDrawer(GravityCompat.END)
        }
        btn_submit.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun saveOptionsData() {
        val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? FragmentAnalysis
        currentFragment?.let {
            if(it.selected_option!=null){
                currentChooseAnswerList[viewPager.currentItem] = it.selected_option.toString()
            }
        }
        restoreSelectedOption()
    }
    fun restoreSelectedOption() {
        handler.postDelayed({
            val currentFragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? FragmentAnalysis
            currentFragment?.let {
                if(currentChooseAnswerList[viewPager.currentItem] != "") {
                    if(currentChooseAnswerList[viewPager.currentItem] == correctAnswerList[viewPager.currentItem]){
                        it.selectOption("option_" + correctAnswerList[viewPager.currentItem].toLowerCase())
                    }else if(currentChooseAnswerList[viewPager.currentItem] == chosenAnswerList[viewPager.currentItem]){
                        it.selectWrongOption("option_" + currentChooseAnswerList[viewPager.currentItem].toLowerCase())
                        it.selectOption("option_" + correctAnswerList[viewPager.currentItem].toLowerCase())
                    }else if (currentChooseAnswerList[viewPager.currentItem] != correctAnswerList[viewPager.currentItem] && currentChooseAnswerList[viewPager.currentItem] != chosenAnswerList[viewPager.currentItem]){
                        it.selectWrongOption("option_" + currentChooseAnswerList[viewPager.currentItem].toLowerCase())
                        it.yourChoosenAnswer("option_" + chosenAnswerList[viewPager.currentItem].toLowerCase())
                        it.selectOption("option_" + correctAnswerList[viewPager.currentItem].toLowerCase())
                    }
                }
            }
        }, 100)
    }
    private fun updateButtonStates() {
        ll_previous.isEnabled = viewPager.currentItem > 0
        ll_next.isEnabled = viewPager.currentItem < ((answerKey.questionsWithAns?.size?: 0) - 1)
    }
    private fun setDrawerCircle(circle_size : Int) {
        for (i in 1..circle_size) {
            val questionCircle = TextView(this).apply {
                text = i.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.circle_outline)
                setPadding(20, 20, 20, 20)
                setOnClickListener {
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
            questionCircles.add(questionCircle)
        }
        setAllCircle(circle_size)
        marked_count.text = questionWithAns.filter { it.marked.equals("Y") }.size.toString()
        attempt_count.text = questionWithAns.filter { it.choosen_answer != "N" }.size.toString()
        unattempt_count.text = questionWithAns.filter { it.choosen_answer=="N" && it.question_time!="0" }.size.toString()
        unseen_count.text = questionWithAns.filter { it.question_time=="0" }.size.toString()
    }

    private fun setAllCircle(circle_size: Int) {
        for (i in 0..<circle_size){
            answer = answerKey.questionsWithAns?.get(i)!!
            val questionCircle = questionCircles[i]
            if (answer.choosen_answer != "N" && answer.marked!="Y") {
                questionCircle.setBackgroundResource(R.drawable.attempt_circle_background)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (answer.question_time != "0" && answer.marked!="Y") {
                questionCircle.setBackgroundResource(R.drawable.unattempt_circle_background)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (answer.choosen_answer != "N" && answer.marked=="Y") {
                questionCircle.setBackgroundResource(R.drawable.attempt_with_star)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (answer.choosen_answer != "0" && answer.marked=="Y") {
                questionCircle.setBackgroundResource(R.drawable.unattempt_with_star)
                questionCircle.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                questionCircle.setBackgroundResource(R.drawable.circle_outline)
            }
        }
    }

    private fun updateQuestionsData() {
        answer = answerKey.questionsWithAns?.get(viewPager.currentItem)!!
        question_no.text = "${viewPager.currentItem + 1}"
        if(answer.choosen_answer == answer.correct_answer){
            question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        }else if(answer.choosen_answer == "N"){
            question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.darkgray))
        }else{
            question_no.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        }
        val seconds = (answer.question_time?.toLong()?.div(1000))?.rem(60)
        val minutes = (answer.question_time?.toLong()?.div(1000*60))?.rem(60)
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tv_question_duration.text = timeFormatted

        iv_marked.setColorFilter(ContextCompat.getColor(this, if (answer.marked == "Y") R.color.red else R.color.darkgray))
        iv_report.setColorFilter(ContextCompat.getColor(this, if (answer.report == "Y") R.color.red else R.color.darkgray))
        iv_saved.setColorFilter(ContextCompat.getColor(this, if (answer.saved == "Y") R.color.primary_blue else R.color.darkgray))

    }

    private fun updateTimerText(examCompletionTime : Long) {
        val hours = examCompletionTime / 1000 / 3600
        val minutes = (examCompletionTime / 1000 % 3600) / 60
        val seconds = examCompletionTime / 1000 % 60

        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tv_exam_timer.text = timeFormatted
    }

    override fun onNextQuestion() {
        TODO("Not yet implemented")
    }

    override fun onPreviousQuestion() {
        TODO("Not yet implemented")
    }

    override fun onOptionSelected() {
        saveOptionsData()
    }
}