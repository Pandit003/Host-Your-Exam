package com.example.testmaster.adapter

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.model.Notification
import com.squareup.picasso.Picasso

class NotificationAdapter(
    private val context: Context,
    private val notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: LinearLayout = itemView.findViewById(R.id.ll_notification_root)
        val ivSender: ImageView = itemView.findViewById(R.id.iv_sender)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val unreadIndicator: View = itemView.findViewById(R.id.unread_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        
        val timeAgo = DateUtils.getRelativeTimeSpanString(
            notification.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvTime.text = timeAgo

        if (notification.fromUserImage.isNotEmpty()) {
            Picasso.get().load(notification.fromUserImage)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(holder.ivSender)
        } else {
            holder.ivSender.setImageResource(R.drawable.baseline_account_circle_24)
        }

        if (notification.isRead) {
            holder.unreadIndicator.visibility = View.GONE
            holder.root.setBackgroundColor(Color.TRANSPARENT)
        } else {
            holder.unreadIndicator.visibility = View.VISIBLE
            holder.root.setBackgroundColor(Color.parseColor("#10000000")) // Very light grey
        }

        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size
}
