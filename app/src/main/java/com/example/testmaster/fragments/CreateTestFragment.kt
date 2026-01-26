package com.example.testmaster.fragments

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
import com.example.testmaster.R
import com.example.testmaster.activities.CreateMcqTest

class CreateTestFragment : Fragment() {
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
            val intent = Intent(view.context, CreateMcqTest::class.java)
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
}