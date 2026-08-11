package com.example.testmaster.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R
import com.example.testmaster.activities.AnnouncementDetailsActivity
import com.example.testmaster.model.Announcement

class AnnouncementAdapter(private val announcementList: List<Announcement>) :
    RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_announcement_title)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_announcement_desc)
        val indicator: View = itemView.findViewById(R.id.v_announcement_indicator)
        val bg: LinearLayout = itemView.findViewById(R.id.ll_announcement_bg)
        val icon: ImageView = itemView.findViewById(R.id.iv_announcement_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_announcement_card, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcementList[position]
        holder.tvTitle.text = announcement.title
        
        if (announcement.type == "EXAM") {
            holder.tvDesc.text = "Exam on ${announcement.examDate}"
            holder.icon.setImageResource(R.drawable.baseline_assignment_24)
        } else {
            holder.tvDesc.text = "New message from ${announcement.announcerName}"
            holder.icon.setImageResource(R.drawable.baseline_forum_24)
        }

        val tintColorRes = listOf(R.color.bluetint, R.color.purpletint, R.color.orangetint, R.color.greentint)
        val bgColorRes = listOf(R.color.blue_bg, R.color.purple_bg, R.color.orange_bg, R.color.green_bg)

        // SYSTEM announcements always use blue
        val colorIdx = if (announcement.announcerUid == "SYSTEM") 0 else position % tintColorRes.size
        val context = holder.itemView.context

        holder.bg.setBackgroundColor(context.resources.getColor(bgColorRes[colorIdx]))
        holder.indicator.setBackgroundColor(context.resources.getColor(tintColorRes[colorIdx]))
        holder.icon.setColorFilter(context.resources.getColor(tintColorRes[colorIdx]))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AnnouncementDetailsActivity::class.java)
            intent.putExtra("announcement", announcement)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = announcementList.size
}
