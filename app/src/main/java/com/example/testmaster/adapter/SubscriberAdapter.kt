package com.example.testmaster.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.Subscriber
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import java.util.Locale

class SubscriberAdapter(private val subscriberList: List<Subscriber>) :
    RecyclerView.Adapter<SubscriberAdapter.SubscriberViewHolder>() {

    private val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722")

    class SubscriberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.iv_subscriber_profile)
        val tvName: TextView = itemView.findViewById(R.id.tv_subscriber_name)
        val cvTextPlaceholder: MaterialCardView = itemView.findViewById(R.id.cv_text_placeholder)
        val tvProfilePlaceholder: TextView = itemView.findViewById(R.id.tv_profile_placeholder)
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
    }

    override fun getItemCount(): Int = subscriberList.size
}
