package com.example.testmaster.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.testmaster.R
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {
    private lateinit var firebaseAuth : FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var et_username : TextInputEditText
    private lateinit var til_username : TextInputLayout
    private lateinit var et_email : TextInputEditText
    private lateinit var et_phone_no : TextInputEditText
    private lateinit var et_dob : TextInputEditText
    private lateinit var btn_submit : Button
    private lateinit var iv_edit : ImageView
    private lateinit var iv_personimage : ImageView
    private lateinit var storageReference: FirebaseStorage
    private val IMAGE_PICK_CODE = 1000
    private var imageUri: Uri? = null
    
    private var originalName = ""
    private var email = ""
    private var imageUrl = ""
    private var currentSubscribersCount = 0

    private var isUsernameUnique = true
    private val checkHandler = Handler(Looper.getMainLooper())
    private var checkRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        til_username = findViewById(R.id.til_username)
        et_username = findViewById(R.id.et_username)
        et_email = findViewById(R.id.et_email)
        et_phone_no = findViewById(R.id.et_phone_no)
        et_dob = findViewById(R.id.et_dob)
        btn_submit = findViewById(R.id.btn_submit)
        iv_edit = findViewById(R.id.iv_edit)
        iv_personimage = findViewById(R.id.iv_personimage)
        
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.onPrimary))
        
        val userId = firebaseAuth.currentUser?.uid
        
        iv_edit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
        
        til_username.setEndIconOnClickListener {
            et_username.isEnabled = true
            et_username.requestFocus()
            et_username.setSelection(et_username.text?.length ?: 0)
            setupUsernameRealTimeCheck()
            Toast.makeText(this, "You can now edit your username", Toast.LENGTH_SHORT).show()
        }
        

        et_dob.setOnClickListener {
            showDatePicker()
        }
        
        btn_submit.setOnClickListener {
            if (!isInternetAvailable(this)) {
                showNoInternetDialog()
            } else if (et_username.text.toString().trim().isEmpty()) {
                et_username.error = "Username is required"
            } else if (!isUsernameUnique) {
                Toast.makeText(this, "Please choose a unique username", Toast.LENGTH_SHORT).show()
            } else {
                btn_submit.isEnabled = false
                updateProfile()
            }
        }
        
        if (userId != null) {
            db.collection("personalDetails").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        originalName = document.getString("name") ?: ""
                        email = document.getString("email") ?: ""
                        val phone = document.getString("phone_no") ?: ""
                        val dob = document.getString("dob") ?: ""
                        imageUrl = document.getString("imageUrl") ?: ""
                        currentSubscribersCount = document.getLong("subscribersCount")?.toInt() ?: 0
                        
                        et_username.setText(originalName)
                        et_email.setText(email)
                        
                        if (phone != "null" && phone.isNotEmpty()) {
                            et_phone_no.setText(phone)
                        }
                        if (dob != "null" && dob.isNotEmpty()) {
                            et_dob.setText(dob)
                        }
                        if (imageUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(imageUrl).fit()
                                .into(iv_personimage)
                        }
                    }
                }
        }
    }

    private fun setupUsernameRealTimeCheck() {
        et_username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkRunnable?.let { checkHandler.removeCallbacks(it) }
            }

            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isEmpty()) {
                    updateUsernameUI(null)
                    isUsernameUnique = false
                    return
                }
                
                if (username == originalName) {
                    updateUsernameUI(true)
                    isUsernameUnique = true
                    return
                }

                checkRunnable = Runnable {
                    checkUsernameUniqueness(username)
                }
                checkHandler.postDelayed(checkRunnable!!, 1000)
            }
        })
    }

    private fun checkUsernameUniqueness(username: String) {
        db.collection("personalDetails")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    updateUsernameUI(false)
                    isUsernameUnique = false
                } else {
                    updateUsernameUI(true)
                    isUsernameUnique = true
                }
            }
            .addOnFailureListener {
                updateUsernameUI(null)
                isUsernameUnique = false
            }
    }

    private fun updateUsernameUI(isAvailable: Boolean?) {
        val color = when (isAvailable) {
            true -> ContextCompat.getColor(this, R.color.green)
            false -> ContextCompat.getColor(this, R.color.red)
            else -> ContextCompat.getColor(this, R.color.darkgray)
        }
        
        val colorStateList = ColorStateList.valueOf(color)
        til_username.setBoxStrokeColorStateList(colorStateList)
        tilUsernameToHintColor(colorStateList)
        
        if (isAvailable == false) {
            til_username.error = "Username already taken"
            til_username.isErrorEnabled = true
        } else if (isAvailable == true) {
            til_username.isErrorEnabled = false
            til_username.helperText = "Username available"
        } else {
            til_username.isErrorEnabled = false
            til_username.helperText = null
        }
    }

    private fun tilUsernameToHintColor(colorStateList: ColorStateList) {
        til_username.defaultHintTextColor = colorStateList
        til_username.hintTextColor = colorStateList
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)
                et_dob.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateProfile() {
        val pd = ProgressDialog(this)
        pd.setMessage("Updating Profile...")
        pd.setCancelable(false)
        pd.show()

        val userId = firebaseAuth.currentUser?.uid ?: return
        
        if (imageUri != null) {
            val storageRef = storageReference.reference.child("images/$userId/profile.jpg")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProfileToFirestore(uri.toString(), pd)
                    }
                }
                .addOnFailureListener {
                    pd.dismiss()
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    btn_submit.isEnabled = true
                }
        } else {
            saveProfileToFirestore(imageUrl, pd)
        }
    }

    private fun saveProfileToFirestore(newImageUrl: String, pd: ProgressDialog) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        val updates = hashMapOf<String, Any?>(
            "name" to et_username.text.toString().trim(),
            "name_lowercase" to et_username.text.toString().trim().lowercase(),
            "email" to et_email.text.toString().trim(),
            "phone_no" to et_phone_no.text.toString().trim(),
            "dob" to et_dob.text.toString().trim(),
            "imageUrl" to newImageUrl
        )

        db.collection("personalDetails").document(userId)
            .update(updates)
            .addOnSuccessListener {
                pd.dismiss()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                originalName = et_username.text.toString().trim()
                finish()
            }
            .addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                btn_submit.isEnabled = true
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val sourceUri = data?.data
            if (sourceUri != null) {
                startCrop(sourceUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            imageUri = UCrop.getOutput(data!!)
            iv_personimage.setImageURI(imageUri)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("UCrop", "Crop error: $cropError")
            Toast.makeText(this, "Crop failed: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false)
            // Optional: Set colors to match your app theme
            setToolbarColor(ContextCompat.getColor(this@EditProfileActivity, R.color.primary))
            setStatusBarColor(ContextCompat.getColor(this@EditProfileActivity, R.color.primary))
            setToolbarWidgetColor(ContextCompat.getColor(this@EditProfileActivity, R.color.onPrimary))
            setActiveControlsWidgetColor(ContextCompat.getColor(this@EditProfileActivity, R.color.bluetint))
        }

        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f) // Force 1:1 ratio
            .withMaxResultSize(1000, 1000)
            .withOptions(options)
            .start(this)
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
                btn_submit.performClick()
            },
            onNegative = {
                finish()
            }
        )
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    override fun onDestroy() {
        checkRunnable?.let { checkHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}
