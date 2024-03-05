package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.ZoneId

class MessageFragmentAdapter(
    private val mContext: Context,
    private val chatsList: ArrayList<ChatData>,
    private val userStatusList: ArrayList<String>
)
    : RecyclerView.Adapter<MessageFragmentAdapter.CardViewObjHolder>() {


        class CardViewObjHolder(view: View):RecyclerView.ViewHolder(view) {
            var cardViewMessageList:CardView = view.findViewById(R.id.cardViewMessageList)
            var textViewMessageListFriendUsername:TextView = view.findViewById(R.id.textViewMessageListFriendUsername)
            var textViewMessageListLastMessage:TextView = view.findViewById(R.id.textViewMessageListLastMessage)
            var textViewMessagesListUnreadMessageNumber:TextView = view.findViewById(R.id.textViewMessagesListUnreadMessageNumber)
            var textViewMessageLastMessageTime:TextView = view.findViewById(R.id.textViewMessageLastMessageTime)
            var imageViewUserActivity:ImageView = view.findViewById(R.id.imageViewUserActivity)
        }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): CardViewObjHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_message_list,parent,false)
        return CardViewObjHolder(view)
    }

    override fun getItemCount(): Int {
        return chatsList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CardViewObjHolder, position: Int) {
        var chatsListNew = chatsList.sortedByDescending { it.lastMessageTime }

        val pos = chatsListNew[position]
        val user1Key = pos.user1Key
        var user1Username = pos.user1Username
        val user2Key = pos.user2Key
        var user2Username = pos.user2Username
        var chatID = pos.chatID
        var lastMessage = pos.lastMessage
        var numberOfNotReadMessage = pos.numberOfNotReadMessage
        var lastMessageSender = pos.lastMessageSender

        var lastMessageTime = ""

        if (lastMessage.equals("")){
            lastMessageTime = ""
        }else{
            val instant = Instant.ofEpochMilli(pos.lastMessageTime!!.toLong())
            val istanbulZoneId = ZoneId.of("Europe/Istanbul")
            val istanbulDateTime = instant.atZone(istanbulZoneId)
            val formattedDateTime = istanbulDateTime.toLocalDateTime().toString()

            var messageTime = formattedDateTime.substring(11,16)
            var yyyy = formattedDateTime.substring(0,4)
            var mm = formattedDateTime.substring(5,7)
            var dd = formattedDateTime.substring(8,10)
            var messageDate = "$dd-$mm-$yyyy"
            lastMessageTime = "$messageTime $messageDate"
        }

        if (lastMessageSender.equals(user2Key)){
            holder.textViewMessageListLastMessage.setTextColor(R.color.logo_background_blue)
        }



        holder.textViewMessageListFriendUsername.text = user2Username
        holder.textViewMessageLastMessageTime.text = lastMessageTime
        if (lastMessage!!.length > 30){
            holder.textViewMessageListLastMessage.text = "$lastMessage..."
        }else{
            holder.textViewMessageListLastMessage.text = lastMessage

        }

        if (numberOfNotReadMessage.equals("0")){
            holder.textViewMessagesListUnreadMessageNumber.visibility = View.INVISIBLE
        }else{
            holder.textViewMessagesListUnreadMessageNumber.text = numberOfNotReadMessage
        }

        holder.cardViewMessageList.setOnClickListener {
            Log.e("messagesfragmeentadapter: ", "mesaj cardı tıklandı")
            var intent = Intent(mContext,MessagesScreenActivity::class.java)
            intent.putExtra("userKeyAnotherUser",user2Key)
            intent.putExtra("usernameAnotherUser",user2Username)
            mContext.startActivity(intent)
        }

        holder.imageViewUserActivity.visibility = View.INVISIBLE


    }

    fun numberOfUnreadMessage(holder: CardViewObjHolder, userMainKey:String, userAnotherKey:String, chatKey:String){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKey)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counterOfUnreadMessage = 0
                for (i in snapshot.children){
                    for (j in snapshot.children){
                        val msg = j.getValue(MessagesData::class.java)
                        if (msg != null){
                            if ((msg.senderUserKey.equals(userAnotherKey) && msg.receiverUserKey.equals(userMainKey))){ //&& msg.messageStatus.equals("Görüldü")){
                                counterOfUnreadMessage++
                            }
                        }
                    }
                }
                notifyDataSetChanged()
                holder.textViewMessagesListUnreadMessageNumber.text = counterOfUnreadMessage.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}