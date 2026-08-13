package com.example.testmaster.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.testmaster.R
import com.example.testmaster.model.CreateQuestions
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.Date

class UploadQuestionJsonActivity : AppCompatActivity() {

    private lateinit var etJson: EditText
    private lateinit var tvError: TextView
    private lateinit var btnHostExam: MaterialButton
    private lateinit var btnPaste: ImageButton
    private lateinit var btnInfo: ImageView
    private lateinit var toolbar: Toolbar

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_question_json)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews()
        setupToolbar()
        setupListeners()
        
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
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnPaste.setOnClickListener {
            pasteFromClipboard()
        }

        btnInfo.setOnClickListener {
            showReferenceDialog()
        }

        etJson.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateJson(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnHostExam.setOnClickListener {
            val json = etJson.text.toString()
            if (isValidJson(json)) {
                processHosting(json)
            } else {
                Toast.makeText(this, "Please correct the JSON errors first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setInitialJson() {
        val reference = JsonReference.SAMPLE_JSON
        etJson.setText(reference)
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text
            etJson.setText(text)
            Toast.makeText(this, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Sample JSON copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun validateJson(json: String) {
        if (json.isEmpty()) {
            tvError.visibility = View.GONE
            btnHostExam.isEnabled = false
            return
        }

        try {
            val questions = gson.fromJson(json, CreateQuestions::class.java)
            if (questions.sub_nm.isNullOrEmpty()) {
                showError("Subject name (sub_nm) is required")
            } else if (questions.questions.isNullOrEmpty()) {
                showError("At least one question is required")
            } else {
                tvError.visibility = View.GONE
                btnHostExam.isEnabled = true
            }
        } catch (e: JsonSyntaxException) {
            showError("Invalid JSON: ${e.localizedMessage}")
        } catch (e: Exception) {
            showError("Error: ${e.localizedMessage}")
        }
    }

    private fun isValidJson(json: String): Boolean {
        return try {
            val questions = gson.fromJson(json, CreateQuestions::class.java)
            !questions.sub_nm.isNullOrEmpty() && !questions.questions.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
        btnHostExam.isEnabled = false
    }

    private fun processHosting(json: String) {
        val questions = gson.fromJson(json, CreateQuestions::class.java)
        val user = firebaseAuth.currentUser ?: return
        
        btnHostExam.isEnabled = false
        
        db.collection("personalDetails").document(user.uid).get()
            .addOnSuccessListener { document ->
                val username = document.get("name").toString()
                
                // Complete the model
                val finalQuestions = questions.copy(
                    candidate_id = user.uid,
                    hosting_date = Date().toString(),
                    hosted_by = username
                )
                
                setExamId { newExamId ->
                    val hostedQuestions = finalQuestions.copy(exam_id = newExamId)
                    hostExam(hostedQuestions)
                }
            }
            .addOnFailureListener {
                btnHostExam.isEnabled = true
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
                            if (examId > maxExamId) {
                                maxExamId = examId
                            }
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
                        btnHostExam.isEnabled = true
                        Toast.makeText(this, "Failed to host exam in global list", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                btnHostExam.isEnabled = true
                Toast.makeText(this, "Failed to host exam", Toast.LENGTH_SHORT).show()
            }
    }
}

object JsonReference {
    const val REFERENCE_MESSAGE = "Tip: Copy this reference JSON and provide it to an AI (like Gemini) along with your question PDF or list. Ask the AI to 'Convert these questions into this exact JSON format'. Once converted, paste the result here to host your exam instantly! The JSON must follow the structure of the CreateQuestions model."
    
    const val SAMPLE_JSON = """{
  "sub_nm": "Mathematics Mock",
  "exam_duration": "3600000",
  "pos_mark": "4",
  "neg_mark": "1",
  "pass_mark": "35",
  "questions": [
    {
      "question_no": "1",
      "question_text": "What is 2+2?",
      "option_a": "3",
      "option_b": "4",
      "option_c": "5",
      "option_d": "6",
      "correct_answer": "B"
    }
  ]
}"""
}
