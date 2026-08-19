package com.example.testmaster.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.personalDetail
import com.example.testmaster.model.Subscriber
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.Locale

class UserSearchAdapter(
    private val context: Context,
    private var userList: List<personalDetail>,
    private val userIds: List<String>
) : RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.iv_user_profile)
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvSubscribersCount: TextView = itemView.findViewById(R.id.tv_subscribers_count)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val btnSubscribe: MaterialButton = itemView.findViewById(R.id.btn_subscribe)
        val cvTextPlaceholder: MaterialCardView = itemView.findViewById(R.id.cv_text_placeholder)
        val tvProfilePlaceholder: TextView = itemView.findViewById(R.id.tv_profile_placeholder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val targetUserId = userIds[position]
        val name = user.name ?: "Unknown"

        holder.tvName.text = name
        holder.tvSubscribersCount.text = "${user.subscribersCount} Subscribers"
        holder.tvEmail.text = user.email

        if (!user.imageUrl.isNullOrEmpty()) {
            holder.ivProfile.visibility = View.VISIBLE
            holder.cvTextPlaceholder.visibility = View.GONE
            Picasso.get().load(user.imageUrl).placeholder(R.drawable.baseline_account_circle_24).into(holder.ivProfile)
        } else {
            holder.ivProfile.visibility = View.GONE
            holder.cvTextPlaceholder.visibility = View.VISIBLE
            
            val firstLetter = name.take(1).uppercase(Locale.getDefault())
            holder.tvProfilePlaceholder.text = if (firstLetter.isEmpty()) "?" else firstLetter
            
            val colorIndex = Math.abs(name.hashCode()) % colors.size
            holder.cvTextPlaceholder.setCardBackgroundColor(Color.parseColor(colors[colorIndex]))
        }

        if (currentUser != null && targetUserId == currentUser.uid) {
            holder.btnSubscribe.visibility = View.GONE
        } else {
            holder.btnSubscribe.visibility = View.VISIBLE
            checkSubscriptionStatus(targetUserId, holder.btnSubscribe)
        }

        holder.btnSubscribe.setOnClickListener {
            if (currentUser != null) {
                toggleSubscription(targetUserId, user, holder.btnSubscribe)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, com.example.testmaster.activities.UserProfileActivity::class.java)
            intent.putExtra("USER_ID", targetUserId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    private fun checkSubscriptionStatus(targetUserId: String, btn: MaterialButton) {
        val currentUid = currentUser?.uid ?: return
        db.collection("Following").document(currentUid)
            .collection("UserFollowing").document(targetUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    btn.text = "Subscribed"
                    btn.setIconResource(R.drawable.baseline_playlist_add_check_24)
                } else {
                    btn.text = "Subscribe"
                    btn.setIconResource(0)
                }
            }
    }

    private fun toggleSubscription(targetUserId: String, targetUser: personalDetail, btn: MaterialButton) {
        val currentUid = currentUser?.uid ?: return
        val subRef = db.collection("Subscribers").document(targetUserId)
            .collection("UserSubscribers").document(currentUid)
        val followRef = db.collection("Following").document(currentUid)
            .collection("UserFollowing").document(targetUserId)

        subRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Unsubscribe
                subRef.delete().addOnSuccessListener {
                    followRef.delete().addOnSuccessListener {
                        // Decrement subscribers count
                        db.collection("personalDetails").document(targetUserId)
                            .update("subscribersCount", FieldValue.increment(-1))
                        
                        btn.text = "Subscribe"
                        btn.setIconResource(0)
                        Toast.makeText(context, "Unsubscribed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Subscribe
                db.collection("personalDetails").document(currentUid).get()
                    .addOnSuccessListener { currDoc ->
                        val currName = currDoc.getString("name")
                        val currImage = currDoc.getString("imageUrl")
                        
                        val subscriberData = Subscriber(
                            uid = currentUid,
                            name = currName,
                            imageUrl = currImage
                        )
                        
                        subRef.set(subscriberData).addOnSuccessListener {
                            val followingData = hashMapOf(
                                "uid" to targetUserId,
                                "name" to targetUser.name,
                                "imageUrl" to targetUser.imageUrl
                            )
                            followRef.set(followingData).addOnSuccessListener {
                                db.collection("personalDetails").document(targetUserId)
                                    .update("subscribersCount", FieldValue.increment(1))

                                // Send Notification
                                val appNotification = com.example.testmaster.model.Notification(
                                    title = "New Subscriber!",
                                    message = "$currName has subscribed to you.",
                                    type = "SUBSCRIBE",
                                    fromUserId = currentUid,
                                    fromUserName = currName ?: "",
                                    fromUserImage = currImage ?: ""
                                )
                                com.example.testmaster.util.NotificationHelper.sendNotification(context, targetUserId, appNotification)

                                btn.text = "Subscribed"
                                btn.setIconResource(R.drawable.baseline_playlist_add_check_24)
                                Toast.makeText(context, "Subscribed successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to toggle subscription", Toast.LENGTH_SHORT).show()
            Log.d("TAG", "toggleSubscription: Error fetching subscription status", it)
        }
    }
}
