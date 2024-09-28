package com.example.testmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.adapter.HistoryAdapter
import com.example.testmaster.adapter.TestAppear_Adapter
import com.example.testmaster.model.AnswerKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    var examDataList: MutableList<AnswerKey> = mutableListOf()
    lateinit var rv_exam_history:RecyclerView
    lateinit var adapter: HistoryAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var iv_notfound: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        getHistoryList()
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        getHistoryList()
        rv_exam_history = view.findViewById(R.id.rv_exam_history)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        iv_notfound = view.findViewById(R.id.iv_notfound)
        swipeRefreshLayout.setOnRefreshListener {
            getHistoryList()
        }

        adapter = HistoryAdapter(view.context,examDataList)
        rv_exam_history.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
        rv_exam_history.adapter = adapter
        return view
    }
    fun getHistoryList() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()

        db.collection("History").document(user).collection("HistoryDetails")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    examDataList.clear()  // Clear the list before adding updated data
                    for (document in task.result) {
                        val answerKey = document.toObject(AnswerKey::class.java)
                        examDataList.add(answerKey)
                    }
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    examDataList.sortWith { a, b ->
                        val dateA = a.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        val dateB = b.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        dateB.compareTo(dateA)  // Sort in descending order
                    }
                    adapter.notifyDataSetChanged()
                    rv_exam_history.post {
                        rv_exam_history.invalidate()
                        rv_exam_history.requestFocus()
                        rv_exam_history.requestLayout()
                    }
                    if(examDataList.isEmpty()){
                        iv_notfound.visibility = View.VISIBLE
                    }else{
                        iv_notfound.visibility = View.GONE
                    }
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.exception)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}