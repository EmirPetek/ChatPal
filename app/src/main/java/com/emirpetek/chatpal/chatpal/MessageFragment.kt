package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.databinding.FragmentMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var chatsList:ArrayList<ChatData> = ArrayList()
    private var userStatusList:ArrayList<String> = ArrayList()
    lateinit var adapter:MessageFragmentAdapter
    companion object{
        var userKeyMain = ""
        var usernameMain = ""
        var numUnreadMessageBadge = 0
        var isComingFromMessage = false
    }
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(inflater, container, false);
        val view = binding.root;

        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        userKeyMain = sharedPreferences.getString("userKey", "").toString()
        usernameMain = sharedPreferences.getString("usernameMain", "").toString()

       if (!isComingFromMessage) {
            if (chatsList.size > 0) getUserActivityStatus(chatsList)
            getChats(userKeyMain)
       }

        binding.progressBarMessageFragment.visibility = View.VISIBLE // ilk girişte progressbar çalışır


        binding.recyclerViewChats.setHasFixedSize(true)
        binding.recyclerViewChats.layoutManager = LinearLayoutManager(requireContext())
        adapter = MessageFragmentAdapter(requireContext(),chatsList,userStatusList)
        binding.recyclerViewChats.adapter = adapter


        return view
    }


    override fun onResume() {
        super.onResume()
        if (chatsList.size > 0) getUserActivityStatus(chatsList)
        if (isComingFromMessage) getChats(userKeyMain)
    }

    fun getUserActivityStatus(chatList: ArrayList<ChatData>) {
        val db = FirebaseDatabase.getInstance()
        userStatusList.clear()
        Log.e("size ", chatList.size.toString())

        for (i in 0 until chatList.size) {
            Log.e("user: size ", chatList[i].user2Key!! + chatList[i].user2Username + " " + chatList.size.toString())
            val ref = db.getReference("users").child(chatList[i].user2Key!!)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val activityStatusSnapshot = userSnapshot.getValue(String::class.java)//.child("activityStatus").getValue(String::class.java)
                        userStatusList.add(activityStatusSnapshot.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Database query cancelled: ${error.message}")
                }
            })

            adapter.notifyDataSetChanged()
        }
    }


    private fun getChats(userKey:String){



        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("chats")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsList.clear()
                for (i in snapshot.children){
                    val chat = i.getValue(ChatData::class.java)

                    if (chat != null){

                        if (chat.user1Key.equals(userKey) || chat.user2Key.equals(userKey)){
                            var user1Key = ""
                            var user1Username = ""
                            var user2Key = ""
                            var user2Username = ""
                            if (chat.user1Key.equals(userKey)){
                                user1Key = chat.user1Key.toString()
                                user1Username = chat.user1Username.toString()
                                user2Key = chat.user2Key.toString()
                                user2Username = chat.user2Username.toString()
                            }else{
                                user1Key = chat.user2Key.toString()
                                user1Username = chat.user2Username.toString()
                                user2Key = chat.user1Key.toString()
                                user2Username = chat.user1Username.toString()
                            }


                            val chatID = chat.chatID
                            val lastMessage = chat.lastMessage
                            val lastMessageSender = chat.lastMessageSender
                            val lastMessageTime = chat.lastMessageTime.toString()
                            val chatCreatedDate = chat.chatCreatedDate
                            var totalUnreadMessage = 0
                            val ref2 = database.getReference("messages").child(i.key!!)
                            ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var counterOfUnreadMessage = 0
                                    for (i in snapshot.children) {
                                        val msg = i.getValue(MessagesData::class.java)
                                        if (msg != null) {
                                            if (msg.senderUserKey.equals(user2Key) && msg.receiverUserKey.equals(user1Key) && msg.messageStatus.equals("Gönderildi")
                                            ) {
                                                counterOfUnreadMessage++
                                                //Log.e("ct", counterOfUnreadMessage.toString() + msg.senderUsername + msg.receiverUsername)
                                            }
                                        }
                                    }
                                    totalUnreadMessage += counterOfUnreadMessage
                                    // chatid key vermez. özel key için i.key kullan
                                    val obj = ChatData(chatID, user1Key, user2Key, user1Username, user2Username,chatCreatedDate,
                                        lastMessage,lastMessageTime, totalUnreadMessage.toString(), lastMessageSender)
                                    chatsList.add(obj)
                                    adapter.notifyDataSetChanged()
                                    numUnreadMessageBadge += totalUnreadMessage
                                    updateUnreadMessageCount(numUnreadMessageBadge)
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                        }
                    }
                }

                binding.progressBarMessageFragment.visibility = View.INVISIBLE // veriler gelince progressbar kaybolur

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        numUnreadMessageBadge = 0



    }

    fun updateUnreadMessageCount(messageCount: Int) {
        (activity as MainActivity).setBadgeNumberUnreadMessage(messageCount)
    }

    fun numberOfUnreadMessage(
        userMainKey: String,
        userAnotherKey: String,
        chatKey: String,
        obj: ChatData
    ){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKey)
        //Log.e("ck", chatKey.toString())
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counterOfUnreadMessage = 0
            for (i in snapshot.children) {
                    val msg = i.getValue(MessagesData::class.java)
                    if (msg != null) {
                        if (msg.senderUserKey.equals(userAnotherKey) && msg.receiverUserKey.equals(userMainKey) && msg.messageStatus.equals("Gönderildi")
                        ) {
                            counterOfUnreadMessage++
                            Log.e("ct", counterOfUnreadMessage.toString() + msg.senderUsername + msg.receiverUsername)
                        }
                    }
                    }
                    obj.numberOfNotReadMessage = counterOfUnreadMessage.toString()
                     chatsList.add(obj)


                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }



}