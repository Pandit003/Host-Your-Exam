package com.example.testmaster.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.adapter.SubscriberAdapter
import com.example.testmaster.model.Subscriber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubscribersListFragment : Fragment() {

    private lateinit var rvSubscribers: RecyclerView
    private lateinit var tvNoData: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var type: String? = null

    companion object {
        fun newInstance(type: String) = SubscribersListFragment().apply {
            arguments = Bundle().apply {
                putString("TYPE", type)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("TYPE")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_subscribers_list, container, false)
        rvSubscribers = view.findViewById(R.id.rv_subscribers_list)
        tvNoData = view.findViewById(R.id.tv_no_data)
        
        rvSubscribers.layoutManager = LinearLayoutManager(context)
        fetchData()
        
        return view
    }

    private fun fetchData() {
        val uid = auth.currentUser?.uid ?: return
        val collectionPath = if (type == "FOLLOWERS") {
            db.collection("Subscribers").document(uid).collection("UserSubscribers")
        } else {
            db.collection("Following").document(uid).collection("UserFollowing")
        }

        collectionPath.get().addOnSuccessListener { documents ->
            val list = mutableListOf<Subscriber>()
            for (doc in documents) {
                val subscriber = Subscriber(
                    uid = doc.id,
                    name = doc.getString("name"),
                    imageUrl = doc.getString("imageUrl")
                )
                list.add(subscriber)
            }

            if (list.isEmpty()) {
                tvNoData.visibility = View.VISIBLE
                rvSubscribers.visibility = View.GONE
                tvNoData.text = if (type == "FOLLOWERS") "You don't have any followers yet." else "You are not following anyone yet."
            } else {
                tvNoData.visibility = View.GONE
                rvSubscribers.visibility = View.VISIBLE
                rvSubscribers.adapter = SubscriberAdapter(list, type ?: "FOLLOWERS")
            }
        }
    }
}
