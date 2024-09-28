package com.example.testmaster

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testmaster.model.QuestionWithAns

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAnalysis.newInstance] factory method to
 * create an instance of this fragment.
 */
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
            selected_option = "A"
            setCorrectAnswer()
            listener?.onOptionSelected()
        }
        option_b.setOnClickListener{
            selected_option = "B"
            setCorrectAnswer()
            listener?.onOptionSelected()

        }
        option_c.setOnClickListener{
            selected_option = "C"
            setCorrectAnswer()
            listener?.onOptionSelected()

        }
        option_d.setOnClickListener{
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
        option_a.setBackgroundResource(R.drawable.box_outline)
        option_b.setBackgroundResource(R.drawable.box_outline)
        option_c.setBackgroundResource(R.drawable.box_outline)
        option_d.setBackgroundResource(R.drawable.box_outline)
        selected_option = "N"
    }
    fun selectOption(option : String){
        when(option){
            "option_a" -> option_a.setBackgroundResource(R.drawable.green_box_outline)
            "option_b" -> option_b.setBackgroundResource(R.drawable.green_box_outline)
            "option_c" -> option_c.setBackgroundResource(R.drawable.green_box_outline)
            "option_d" -> option_d.setBackgroundResource(R.drawable.green_box_outline)
        }
    }
    fun selectWrongOption(option : String){
        when(option){
            "option_a" -> option_a.setBackgroundResource(R.drawable.red_box_outline)
            "option_b" -> option_b.setBackgroundResource(R.drawable.red_box_outline)
            "option_c" -> option_c.setBackgroundResource(R.drawable.red_box_outline)
            "option_d" -> option_d.setBackgroundResource(R.drawable.red_box_outline)
        }
    }
    fun yourChoosenAnswer(option : String){
        when(option){
            "option_a" -> option_a.setBackgroundResource(R.drawable.blue_box_outline)
            "option_b" -> option_b.setBackgroundResource(R.drawable.blue_box_outline)
            "option_c" -> option_c.setBackgroundResource(R.drawable.blue_box_outline)
            "option_d" -> option_d.setBackgroundResource(R.drawable.blue_box_outline)
        }
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