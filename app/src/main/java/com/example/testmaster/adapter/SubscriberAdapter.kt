package com.example.testmaster.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.Subscriber
import com.example.testmaster.util.CustomDialogUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.Locale

class SubscriberAdapter(
    private var subscriberList: MutableList<Subscriber>,
    private val type: String
) : RecyclerView.Adapter<SubscriberAdapter.SubscriberViewHolder>() {

    private val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    class SubscriberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.iv_subscriber_profile)
        val tvName: TextView = itemView.findViewById(R.id.tv_subscriber_name)
        val cvTextPlaceholder: MaterialCardView = itemView.findViewById(R.id.cv_text_placeholder)
        val tvProfilePlaceholder: TextView = itemView.findViewById(R.id.tv_profile_placeholder)
        val btnUnsubscribe: MaterialButton = itemView.findViewById(R.id.btn_unsubscribe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subscriber, parent, false)
        return SubscriberViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriberViewHolder, position: Int) {
        val subscriber = subscriberList[position]
        val name = subscriber.name ?: "Unknown"
        holder.tvName.text = name
        
        if (!subscriber.imageUrl.isNullOrEmpty()) {
            holder.ivProfile.visibility = View.VISIBLE
            holder.cvTextPlaceholder.visibility = View.GONE
            Picasso.get().load(subscriber.imageUrl).placeholder(R.drawable.baseline_person_24).into(holder.ivProfile)
        } else {
            holder.ivProfile.visibility = View.GONE
            holder.cvTextPlaceholder.visibility = View.VISIBLE
            
            val firstLetter = name.take(1).uppercase(Locale.getDefault())
            holder.tvProfilePlaceholder.text = if (firstLetter.isEmpty()) "?" else firstLetter
            
            val colorIndex = Math.abs(name.hashCode()) % colors.size
            holder.cvTextPlaceholder.setCardBackgroundColor(Color.parseColor(colors[colorIndex]))
        }

        if (type == "FOLLOWING") {
            holder.btnUnsubscribe.visibility = View.VISIBLE
            holder.btnUnsubscribe.setOnClickListener {
                showUnfollowConfirmation(holder.itemView.context, subscriber, position)
            }
        } else {
            holder.btnUnsubscribe.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, com.example.testmaster.activities.UserProfileActivity::class.java)
            intent.putExtra("USER_ID", subscriber.uid)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun showUnfollowConfirmation(context: android.content.Context, subscriber: Subscriber, position: Int) {
        CustomDialogUtils.showConfirm(
            activity = context as android.app.Activity,
            title = "Unfollow User",
            message = "Are you sure you want to unfollow ${subscriber.name}?",
            positiveText = "Unfollow",
            negativeText = "Cancel",
            onPositive = {
                unfollowUser(context, subscriber, position)
            }
        )
    }

    private fun unfollowUser(context: android.content.Context, subscriber: Subscriber, position: Int) {
        val currentUid = currentUser?.uid ?: return
        val targetUid = subscriber.uid ?: return

        // 1. Remove from my following list
        val followRef = db.collection("Following").document(currentUid)
            .collection("UserFollowing").document(targetUid)
        
        // 2. Remove from their subscribers list
        val subRef = db.collection("Subscribers").document(targetUid)
            .collection("UserSubscribers").document(currentUid)

        db.runTransaction { transaction ->
            transaction.delete(followRef)
            transaction.delete(subRef)
            transaction.update(db.collection("personalDetails").document(targetUid), "subscribersCount", FieldValue.increment(-1))
        }.addOnSuccessListener {
            if (position < subscriberList.size) {
                subscriberList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, subscriberList.size)
            }
            Toast.makeText(context, "Unfollowed ${subscriber.name}", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to unfollow", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = subscriberList.size
}
