package com.example.testmaster.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testmaster.R
import com.example.testmaster.model.QuestionWithAns

class FragmentAnalysis : Fragment() {

    private var listener: OnQuestionInteractionListener? = null



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnQuestionInteractionListener")
        }
    }

    lateinit var option_a : LinearLayout
    lateinit var option_b : LinearLayout
    lateinit var option_c : LinearLayout
    lateinit var option_d : LinearLayout
    lateinit var tv_question : TextView
    lateinit var tv_option_a : TextView
    lateinit var tv_option_b : TextView
    lateinit var tv_option_c : TextView
    lateinit var tv_option_d : TextView
    lateinit var choosenAnswer : String
    lateinit var correctAnswer : String

    var selected_option : String? = null
    var position : Int = 0
    lateinit var questions : QuestionWithAns
    lateinit var tv_option_a_indicator : TextView
    lateinit var tv_option_b_indicator : TextView
    lateinit var tv_option_c_indicator : TextView
    lateinit var tv_option_d_indicator : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)
        // Inflate the layout for this fragment
        option_a = view.findViewById(R.id.option_a)
        option_b = view.findViewById(R.id.option_b)
        option_c = view.findViewById(R.id.option_c)
        option_d = view.findViewById(R.id.option_d)
        tv_option_a = view.findViewById(R.id.tv_option_a)
        tv_option_b = view.findViewById(R.id.tv_option_b)
        tv_option_c = view.findViewById(R.id.tv_option_c)
        tv_option_d = view.findViewById(R.id.tv_option_d)
        tv_question = view.findViewById(R.id.tv_question)

        tv_option_a_indicator = view.findViewById(R.id.tv_option_a_indicator)
        tv_option_b_indicator = view.findViewById(R.id.tv_option_b_indicator)
        tv_option_c_indicator = view.findViewById(R.id.tv_option_c_indicator)
        tv_option_d_indicator = view.findViewById(R.id.tv_option_d_indicator)

        questions = (arguments?.getSerializable("question") as? QuestionWithAns)!!
        questions = (arguments?.getSerializable("question") as? QuestionWithAns)!!
        var totalQuestions = (arguments?.getInt("totalQuestions"))?:0
        position = (arguments?.getInt("position"))?:0
        choosenAnswer = questions.choosen_answer.toString()
        correctAnswer = questions.correct_answer.toString()


        if(questions == null){
            Toast.makeText(view.context,"Unable To Take Re-attempt Exam",Toast.LENGTH_SHORT).show()
        }

        tv_question.setText(questions.question_text)
        tv_option_a.setText(questions.option_a)
        tv_option_b.setText(questions.option_b)
        tv_option_c.setText(questions.option_c)
        tv_option_d.setText(questions.option_d)

        option_a.setOnClickListener{
            clearOption()
            selected_option = "A"
            setCorrectAnswer()
            listener?.onOptionSelected()
        }
        option_b.setOnClickListener{
            clearOption()
            selected_option = "B"
            setCorrectAnswer()
            listener?.onOptionSelected()

        }
        option_c.setOnClickListener{
            clearOption()
            selected_option = "C"
            setCorrectAnswer()
            listener?.onOptionSelected()

        }
        option_d.setOnClickListener{
            clearOption()
            selected_option = "D"
            setCorrectAnswer()
            listener?.onOptionSelected()
        }
        return view
    }
    fun setCorrectAnswer(){
        if(selected_option == correctAnswer){
            selectOption("option_" + correctAnswer.toLowerCase())
        }else if(selected_option == choosenAnswer){
            selectWrongOption("option_" + selected_option?.toLowerCase())
            selectOption("option_" + correctAnswer.toLowerCase())
        }else if (selected_option != correctAnswer && selected_option != choosenAnswer){
            selectWrongOption("option_" + selected_option?.toLowerCase())
            yourChoosenAnswer("option_" + choosenAnswer.toLowerCase())
            selectOption("option_" + correctAnswer.toLowerCase())
        }

    }


    interface OnQuestionInteractionListener {
        fun onNextQuestion()
        fun onPreviousQuestion()
        fun onOptionSelected()
    }

    fun clearOption(){
        resetOption(option_a, tv_option_a, tv_option_a_indicator)
        resetOption(option_b, tv_option_b, tv_option_b_indicator)
        resetOption(option_c, tv_option_c, tv_option_c_indicator)
        resetOption(option_d, tv_option_d, tv_option_d_indicator)
        selected_option = "N"
    }

    private fun resetOption(layout: LinearLayout, text: TextView, indicator: TextView) {
        layout.setBackgroundResource(R.drawable.bg_pill_surface_variant)
        layout.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.surfaceVariant))
        text.setTextColor(resources.getColor(R.color.onSurface))
        indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.surface))
        indicator.setTextColor(resources.getColor(R.color.onSurface))
    }

    fun selectOption(option : String){
        when(option){
            "option_a" -> applyOptionStyle(option_a, tv_option_a, tv_option_a_indicator, R.color.green_bg, R.color.greentint)
            "option_b" -> applyOptionStyle(option_b, tv_option_b, tv_option_b_indicator, R.color.green_bg, R.color.greentint)
            "option_c" -> applyOptionStyle(option_c, tv_option_c, tv_option_c_indicator, R.color.green_bg, R.color.greentint)
            "option_d" -> applyOptionStyle(option_d, tv_option_d, tv_option_d_indicator, R.color.green_bg, R.color.greentint)
        }
    }

    fun selectWrongOption(option : String){
        when(option){
            "option_a" -> applyOptionStyle(option_a, tv_option_a, tv_option_a_indicator, R.color.red_bg, R.color.redtint)
            "option_b" -> applyOptionStyle(option_b, tv_option_b, tv_option_b_indicator, R.color.red_bg, R.color.redtint)
            "option_c" -> applyOptionStyle(option_c, tv_option_c, tv_option_c_indicator, R.color.red_bg, R.color.redtint)
            "option_d" -> applyOptionStyle(option_d, tv_option_d, tv_option_d_indicator, R.color.red_bg, R.color.redtint)
        }
    }

    fun yourChoosenAnswer(option : String){
        when(option){
            "option_a" -> applyOptionStyle(option_a, tv_option_a, tv_option_a_indicator, R.color.blue_bg, R.color.bluetint)
            "option_b" -> applyOptionStyle(option_b, tv_option_b, tv_option_b_indicator, R.color.blue_bg, R.color.bluetint)
            "option_c" -> applyOptionStyle(option_c, tv_option_c, tv_option_c_indicator, R.color.blue_bg, R.color.bluetint)
            "option_d" -> applyOptionStyle(option_d, tv_option_d, tv_option_d_indicator, R.color.blue_bg, R.color.bluetint)
        }
    }

    private fun applyOptionStyle(layout: LinearLayout, text: TextView, indicator: TextView, bgColor: Int, tintColor: Int) {
        layout.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(bgColor))
        text.setTextColor(resources.getColor(tintColor))
        indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(resources.getColor(tintColor))
        indicator.setTextColor(resources.getColor(R.color.white))
    }
    fun restoreSelectedOption(choosenAnswer: String?) {
        if (choosenAnswer != null && choosenAnswer!="N") {
            selectOption("option_"+choosenAnswer.toLowerCase())
        }else{
            clearOption()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentQuestion.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(questionwithans: QuestionWithAns, position: Int, totalQuestions: Int): FragmentAnalysis {
            val fragment = FragmentAnalysis()
            val args = Bundle()
            args.putSerializable("question", questionwithans)
            args.putInt("position", position)
            args.putInt("totalQuestions", totalQuestions)
            fragment.arguments = args
            return fragment
        }
    }
}