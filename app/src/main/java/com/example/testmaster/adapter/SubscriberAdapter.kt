package com.example.testmaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.Subscriber
import com.squareup.picasso.Picasso

class SubscriberAdapter(private val subscriberList: List<Subscriber>) :
    RecyclerView.Adapter<SubscriberAdapter.SubscriberViewHolder>() {

    class SubscriberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.iv_subscriber_profile)
        val tvName: TextView = itemView.findViewById(R.id.tv_subscriber_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subscriber, parent, false)
        return SubscriberViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriberViewHolder, position: Int) {
        val subscriber = subscriberList[position]
        holder.tvName.text = subscriber.name
        
        if (!subscriber.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(subscriber.imageUrl).placeholder(R.drawable.baseline_person_24).into(holder.ivProfile)
        } else {
            holder.ivProfile.setImageResource(R.drawable.baseline_person_24)
        }
    }

    override fun getItemCount(): Int = subscriberList.size
}
