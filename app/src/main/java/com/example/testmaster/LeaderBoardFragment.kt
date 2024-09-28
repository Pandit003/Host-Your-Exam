package com.example.testmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.Carousel.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testmaster.adapter.LeaderBoardAdapter
import com.example.testmaster.model.AnswerKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LeaderBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeaderBoardFragment : Fragment() {
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
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var rv_exam_leaderboard : RecyclerView
    var leaderBoardList : MutableList<AnswerKey> = mutableListOf()
    var newleaderBoardList : MutableList<AnswerKey> = mutableListOf()
    private lateinit var adapter: LeaderBoardAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var iv_notfound: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    override fun onResume() {
        getLeaderBoardList()
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        getLeaderBoardList()
        rv_exam_leaderboard = view.findViewById(R.id.rv_exam_leaderboard)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        iv_notfound = view.findViewById(R.id.iv_notfound)
        swipeRefreshLayout.setOnRefreshListener {
            getLeaderBoardList()
        }
        adapter = LeaderBoardAdapter(view.context,leaderBoardList)
        rv_exam_leaderboard.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
        rv_exam_leaderboard.adapter = adapter
        return view
    }
    fun getLeaderBoardList() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        leaderBoardList.clear() // Ensure the list is cleared before fetching data

        db.collection("Leaderboard").document(user)
            .collection("LeaderboardDetails")
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val tempList = mutableListOf<AnswerKey>() // Temporary list to collect data
                    for (document in documents.documents) {
                        val answerKeyRefs = document.get("answerKeyRefs") as? List<DocumentReference>
                        if (answerKeyRefs != null) {
                            for (answerKeyRef in answerKeyRefs) {
                                answerKeyRef.get().addOnSuccessListener { answerKeyDocument ->
                                    if (answerKeyDocument.exists()) {
                                        val answerKey = answerKeyDocument.toObject(AnswerKey::class.java)
                                        if (answerKey != null) {
                                            tempList.add(answerKey) // Add to temporary list
                                        }
                                        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                                        tempList.sortWith { a, b ->
                                            val dateA = a.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                                            val dateB = b.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                                            dateB.compareTo(dateA)
                                        }
                                        leaderBoardList.clear() // Clear the main list before updating
                                        leaderBoardList.addAll(tempList) // Add all items from tempList to leaderBoardList
                                        adapter.notifyDataSetChanged() // Notify adapter of data changes
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Failed to retrieve AnswerKey data", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()

                swipeRefreshLayout.isRefreshing = false
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DownloadFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LeaderBoardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}