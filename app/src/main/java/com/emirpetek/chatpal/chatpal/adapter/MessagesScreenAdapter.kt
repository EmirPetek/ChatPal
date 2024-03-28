package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.activity.MessagesScreenActivity
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.MessagesData
import java.time.Instant
import java.time.ZoneId

class MessagesScreenAdapter(private val mContext:Context,private val messageDataList:List<MessagesData>)
    :RecyclerView.Adapter<MessagesScreenAdapter.CardViewObjHolder>() {
    private lateinit var sharedPreferences: SharedPreferences

        companion object{
            private var VIEW_TYPE_SENDER_RIGHT = 1
            private var VIEW_TYPE_RECEIVER_LEFT = 2
            private var a = 0
    }

    inner class CardViewObjHolder(view: View):RecyclerView.ViewHolder(view){

        val textViewCardMessage:TextView = view.findViewById(R.id.textViewCardMessage)
        val textViewCardSendTime:TextView = view.findViewById(R.id.textViewCardSendTime)
        val textViewCardSeenStatus:TextView = view.findViewById(R.id.textViewCardSeenStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewObjHolder {

        if (viewType == VIEW_TYPE_SENDER_RIGHT){
            val view = LayoutInflater.from(mContext).inflate(R.layout.card_message_right, parent, false)
            return CardViewObjHolder(view)
        }else{
            val view = LayoutInflater.from(mContext).inflate(R.layout.card_message_left, parent, false)
            return CardViewObjHolder(view)
        }
    }



    override fun getItemCount(): Int {
        return messageDataList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CardViewObjHolder, position: Int) {
        val messageSender = MessagesScreenActivity.messageReceiver

        val message = messageDataList[position].messagesContent
        val messageStatus = messageDataList[position].messageStatus

        var unixTimestamp = messageDataList[position].sendTime

        // bu 4 satır unixtimestampi istanbul saatine göre ayarlar
        val instant = Instant.ofEpochMilli(unixTimestamp!!.toLong())
        val istanbulZoneId = ZoneId.of("Europe/Istanbul")
        val istanbulDateTime = instant.atZone(istanbulZoneId)
        val formattedDateTime = istanbulDateTime.toLocalDateTime().toString()

        var messageTime = formattedDateTime.substring(11,16)
        var yyyy = formattedDateTime.substring(0,4)
        var mm = formattedDateTime.substring(5,7)
        var dd = formattedDateTime.substring(8,10)
        var messageDate = "$dd-$mm-$yyyy" //formattedDateTime.substring(0,10)


        sharedPreferences = mContext.getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userKeyMain = sharedPreferences.getString("userKey", "")

        holder.textViewCardMessage.text = message
        holder.textViewCardSeenStatus.visibility = View.GONE


         if (messageDataList[position].senderUserKey.equals(userKeyMain)){ // mesajı gönderen ana kullanıcı ise
            holder.textViewCardSeenStatus.text = messageStatus
            holder.textViewCardSendTime.text = messageTime + " " + messageDate
        }else{
            holder.textViewCardSendTime.text = messageDate + " " + messageTime

        }
    }

    override fun getItemViewType(position: Int): Int {

        sharedPreferences = mContext.getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userKeyMain = sharedPreferences.getString("userKey", "")


        if (messageDataList[position].senderUserKey!! == userKeyMain){
            a = VIEW_TYPE_SENDER_RIGHT
            return VIEW_TYPE_SENDER_RIGHT
        }else{
            a = VIEW_TYPE_SENDER_RIGHT
            return VIEW_TYPE_RECEIVER_LEFT
        }
    }
}