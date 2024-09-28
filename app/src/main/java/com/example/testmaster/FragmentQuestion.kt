package com.example.testmaster

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.testmaster.model.Question
import com.example.testmaster.model.QuestionWithAns


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentQuestion.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentQuestion : Fragment() {

    private var listener: OnQuestionInteractionListener? = null


    private var questionTimeInMillis: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnQuestionInteractionListener")
        }
    }
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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

    var selected_option : String? = null

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

        val questions = arguments?.getSerializable("question") as? Question

        tv_question.setText(questions?.question_text)
        tv_option_a.setText(questions?.option_a)
        tv_option_b.setText(questions?.option_b)
        tv_option_c.setText(questions?.option_c)
        tv_option_d.setText(questions?.option_d)

        option_a.setOnClickListener{
            selectOption("option_a")
            selected_option = "A"
            listener?.onOptionSelected()
        }
        option_b.setOnClickListener{
            selectOption("option_b")
            selected_option = "B"
            listener?.onOptionSelected()
        }
        option_c.setOnClickListener{
            selectOption("option_c")
            selected_option = "C"
            listener?.onOptionSelected()
        }
        option_d.setOnClickListener{
            selectOption("option_d")
            selected_option = "D"
            listener?.onOptionSelected()
        }

        return view
    }


    interface OnQuestionInteractionListener {
        fun onClearOptions()
        fun onNextQuestion()
        fun onPreviousQuestion()
        fun onUpdateQuestionTimer(seconds: Long, minutes: Long)
        fun onOptionSelected()
    }

    fun getQuestionDetails(): QuestionWithAns {
        return QuestionWithAns(
            question_text = tv_question.text.toString(),
            option_a = tv_option_a.text.toString(),
            option_b = tv_option_b.text.toString(),
            option_c = tv_option_c.text.toString(),
            option_d = tv_option_d.text.toString(),
//            choosen_answer = selected_option
        )
    }

    fun clearOption(){
        option_a.setBackgroundResource(R.drawable.box_outline)
        option_b.setBackgroundResource(R.drawable.box_outline)
        option_c.setBackgroundResource(R.drawable.box_outline)
        option_d.setBackgroundResource(R.drawable.box_outline)
        selected_option = "N"
    }
    fun selectOption(option : String){
        option_a.setBackgroundResource(R.drawable.box_outline)
        option_b.setBackgroundResource(R.drawable.box_outline)
        option_c.setBackgroundResource(R.drawable.box_outline)
        option_d.setBackgroundResource(R.drawable.box_outline)
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
        fun newInstance(question: Question, position: Int, totalQuestions: Int): FragmentQuestion {
            val fragment = FragmentQuestion()
            val args = Bundle()
            args.putSerializable("question", question)
            args.putInt("position", position)
            args.putInt("totalQuestions", totalQuestions)
            fragment.arguments = args
            return fragment
        }
    }
}