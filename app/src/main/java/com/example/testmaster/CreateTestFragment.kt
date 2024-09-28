package com.example.testmaster

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateTestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateTestFragment : Fragment() {
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
    lateinit var ll_create_mcq : LinearLayout
    lateinit var ll_create_One_word : LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_create_test, container, false)
        // Inflate the layout for this fragment
        ll_create_mcq = view.findViewById(R.id.ll_create_mcq)
        ll_create_One_word = view.findViewById(R.id.ll_create_One_word)

        ll_create_mcq.setOnClickListener(){
            val intent = Intent(view.context,CreateMcqTest::class.java)
            startActivity(intent)
        }
        ll_create_One_word.setOnClickListener {
            val dialog = Dialog(it.context)
            dialog.setContentView(R.layout.dialog_box_attention)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            val dialogTitle = dialog.findViewById<TextView>(R.id.title)
            val dialogDescription = dialog.findViewById<TextView>(R.id.message)
            val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
            dialogTitle.text = "Alert!!"
            dialogDescription.text = "This feature is currently unavailable"
            btnYes.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Create_Test_Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateTestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}