package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.PostCommentData
import java.time.Instant
import java.time.ZoneId

class PostCommentFragmentAdapter(val mContext: Context, val commentList: ArrayList<PostCommentData>):
RecyclerView.Adapter<PostCommentFragmentAdapter.CommentCardViewHolder>(){
    inner class CommentCardViewHolder(view:View):RecyclerView.ViewHolder(view){
        var textViewPostCommentUsername:TextView = view.findViewById(R.id.textViewPostCommentUsername)
        var textViewPostCommentContent:TextView = view.findViewById(R.id.textViewPostCommentContent)
        var textViewPostCommentDate:TextView = view.findViewById(R.id.textViewPostCommentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentCardViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_post_comment,parent,false)
        return CommentCardViewHolder(view)
    }

    override fun getItemCount(): Int {
            return commentList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentCardViewHolder, position: Int) {
        val data = commentList[position]
        val username = data.commentSharerUsername
        val content = data.commentContent
        val date = data.commentDate

        textViewDateSetter(holder,date)
        holder.textViewPostCommentUsername.text = username
        holder.textViewPostCommentContent.text = content



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun textViewDateSetter(holder: CommentCardViewHolder, date:String){
        val formattedDateTime = istanbulDateGetter(date)
        var messageTime = formattedDateTime.substring(11,16)
        var yyyy = formattedDateTime.substring(0,4)
        var mm = formattedDateTime.substring(5,7)
        var dd = formattedDateTime.substring(8,10)
        var messageDate = "$dd/$mm/$yyyy"

        val nowTimeStamp = System.currentTimeMillis().toString()

        val timeDifference = (nowTimeStamp.substring(0, nowTimeStamp.length - 3).toLong() - date.substring(0, date.length - 3).toLong())
        // timedifference saniye cinsinden gelir

        val min = timeDifference/60 // üstünden kaç dakika geçmiş onu gösterir
        val hour = timeDifference/3600 // üstünden kaç saat geçmiş onu gösterir

        if (min >= 0 && min < 60) {
            val text = "$min dakika önce"
            holder.textViewPostCommentDate.text = text
        } else if (hour >= 1 && hour < 24) {
            val text = "$hour saat önce"
            holder.textViewPostCommentDate.text = text
        } else {
            val text =  messageTime + " - " + messageDate
            holder.textViewPostCommentDate.text = text
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun istanbulDateGetter(unixts:String):String{
        val instant = Instant.ofEpochMilli(unixts.toLong())
        val istanbulZoneId = ZoneId.of("Europe/Istanbul")
        val istanbulDateTime = instant.atZone(istanbulZoneId)
        val formattedDateTime = istanbulDateTime.toLocalDateTime().toString()
        return formattedDateTime
    }


}