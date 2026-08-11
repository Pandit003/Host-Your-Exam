package com.example.testmaster.util

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.testmaster.R

object CustomDialogUtils {

    fun showAlert(
        activity: Activity,
        title: String,
        message: String,
        positiveText: String = "OK",
        cancelable: Boolean = false,
        onPositive: (() -> Unit)? = null
    ) {
        if (activity.isFinishing) return

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_box_confirmation)
        dialog.setCancelable(cancelable)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))

        val tvTitle = dialog.findViewById<TextView>(R.id.title)
        val tvMessage = dialog.findViewById<TextView>(R.id.message)
        val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialog.findViewById<Button>(R.id.btn_no)

        tvTitle.text = title
        tvMessage.text = message

        btnYes.text = positiveText
        btnNo.visibility = View.GONE   // ALERT = single button

        btnYes.setOnClickListener {
            dialog.dismiss()
            onPositive?.invoke()
        }

        dialog.show()
    }

    fun showConfirm(
        activity: Activity,
        title: String,
        message: String,
        positiveText: String = "Yes",
        negativeText: String = "No",
        cancelable: Boolean = false,
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ) {
        if (activity.isFinishing) return

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_box_confirmation)
        dialog.setCancelable(cancelable)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))

        val tvTitle = dialog.findViewById<TextView>(R.id.title)
        val tvMessage = dialog.findViewById<TextView>(R.id.message)
        val btnYes = dialog.findViewById<Button>(R.id.btn_yes)
        val btnNo = dialog.findViewById<Button>(R.id.btn_no)

        tvTitle.text = title
        tvMessage.text = message

        btnYes.text = positiveText
        btnNo.text = negativeText
        btnNo.visibility = View.VISIBLE

        btnYes.setOnClickListener {
            dialog.dismiss()
            onPositive.invoke()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
            onNegative?.invoke()
        }

        dialog.show()
    }

    fun showCustom(
        activity: Activity,
        layoutRes: Int,
        cancelable: Boolean = true
    ): Dialog {
        if (activity.isFinishing) throw IllegalStateException("Activity is finishing")

        val dialog = Dialog(activity)
        dialog.setContentView(layoutRes)
        dialog.setCancelable(cancelable)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
        return dialog
    }
}
