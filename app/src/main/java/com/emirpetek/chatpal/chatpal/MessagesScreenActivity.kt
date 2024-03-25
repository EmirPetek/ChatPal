package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.databinding.ActivityMessagesScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MessagesScreenActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMessagesScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter:MessagesScreenAdapter
    private var messagesList:ArrayList<MessagesData> = ArrayList()

    companion object{
        var chatKey = ""
        var messageReceiver = 0
        var userKeyMain = ""
        var userkeyForAnotherUser = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)



        userkeyForAnotherUser = intent.getStringExtra("userKeyAnotherUser").toString()
        var usernameForAnotherUser = intent.getStringExtra("usernameAnotherUser").toString()

        // discoverfragmenttan username geliyor onu userkeye dönüştürür
        findUserKeyFromUsername(usernameForAnotherUser)


        sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        userKeyMain = sharedPreferences.getString("userKey", "").toString()
        var usernameMain = sharedPreferences.getString("usernameMain", "")


        Log.e("messagesScreenActivity keys: ", userkeyForAnotherUser + "iki: " + usernameForAnotherUser)

        // daha önce chat edilmemişse yeni chat satırı oluşturur
        checkerIsChattedBefore(userKeyMain!!,userkeyForAnotherUser, usernameMain!!, usernameForAnotherUser)

        binding.toolbarMessagesScreen.title = usernameForAnotherUser
        setSupportActionBar(binding.toolbarMessagesScreen)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarMessagesScreen.setNavigationOnClickListener {
            //MainActivity.whereComingFrom = "MessagesScreenActivity"
            onBackPressedDispatcher.onBackPressed()
        }

        clickSendButton(userKeyMain, usernameMain, userkeyForAnotherUser, usernameForAnotherUser)


        binding.recyclerViewMessagesScreen.setHasFixedSize(true)
        binding.recyclerViewMessagesScreen.layoutManager = LinearLayoutManager(this)
        adapter = MessagesScreenAdapter(this@MessagesScreenActivity,messagesList)
        binding.recyclerViewMessagesScreen.adapter = adapter

        binding.progressBarMessagesScreenAct.visibility = View.VISIBLE // ilk girişte progressbar çalışır

    }

    private fun updateSeenMessage(chatKeyVal:String,userMainKey: String,userAnotherKey: String){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKeyVal)


        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    for (j in snapshot.children){
                        val msg = j.getValue(MessagesData::class.java)
                        if (msg != null){
                            if ((msg.senderUserKey.equals(userAnotherKey) && msg.receiverUserKey.equals(userMainKey))){

                                val key = j!!.key
                               // Log.e("i ve j", "$i ve $j")
                                val updateInfo = HashMap<String,Any>()
                                updateInfo["messageStatus"] = "Görüldü"
                                ref.child(key!!).updateChildren(updateInfo)
                                getMessage(userMainKey,userAnotherKey,chatKeyVal)
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun clickSendButton(userKeyMain:String,usernameMain:String,userkeyForAnotherUser: String,usernameForAnotherUser: String){
        binding.buttonMessagesScreenSendMessage.setOnClickListener {
            Log.e("chatID messagesscreenactivity: ", chatKey)

            sendMessage(userKeyMain,usernameMain,userkeyForAnotherUser,usernameForAnotherUser)
            getMessage(userKeyMain, userkeyForAnotherUser, chatKey)
            binding.recyclerViewMessagesScreen.scrollToPosition(messagesList.size - 1);
        }
    }

    private fun sendMessage(senderUserKey: String,senderUsername: String,receiverUserKey: String,receiverUsername: String){

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKey)
        val ref2 = database.getReference("chats")

        val currentDate = Date()
        val istanbulTimeZone = TimeZone.getTimeZone("Europe/Istanbul")
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        dateFormat.timeZone = istanbulTimeZone
        //val sendTime = dateFormat.format(currentDate)
        val sendTime = System.currentTimeMillis().toString()

        var messageContent = binding.editTextMessagesScreenMessageContent.text.trim().toString()

        val messageData = MessagesData(chatKey,senderUserKey,receiverUserKey,senderUsername,receiverUsername,messageContent,sendTime,"Gönderildi")
        ref.push().setValue(messageData)

        binding.editTextMessagesScreenMessageContent.text.clear()
        Log.e("chatKey", chatKey)

        val updateLastMessageInfo = HashMap<String,Any>()
        updateLastMessageInfo["lastMessage"] = messageContent
        updateLastMessageInfo["lastMessageTime"] = sendTime
        updateLastMessageInfo["lastMessageSender"] = userKeyMain
        ref2.child(chatKey).updateChildren(updateLastMessageInfo)

    }

    private fun getMessage(userMainKey:String,userAnotherKey:String,chatKeyVal:String){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKeyVal)


        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (i in snapshot.children){
                    val msg = i.getValue(MessagesData::class.java)
                    if (msg != null){
                        if ((msg.senderUserKey.equals(userMainKey) && msg.receiverUserKey.equals(userAnotherKey))
                            ||  msg.senderUserKey.equals(userAnotherKey) && msg.receiverUserKey.equals(userMainKey)){

                        val chatKey = msg.chatKey
                        val senderUserKey = msg.senderUserKey
                        val receiverUserKey = msg.receiverUserKey
                        val senderUsername = msg.senderUsername
                        val receiverUsername = msg.receiverUsername
                        val messagesContent = msg.messagesContent
                        val sendTime = msg.sendTime

                        val messageStatus = msg.messageStatus
                        val obj = MessagesData(chatKey,senderUserKey,receiverUserKey,senderUsername,receiverUsername, messagesContent, sendTime, messageStatus)
                        messagesList.add(obj)
                        binding.recyclerViewMessagesScreen.scrollToPosition(messagesList.size - 1);


                        }
                    }
                }

                binding.progressBarMessagesScreenAct.visibility = View.INVISIBLE // mesajlar gelince progressbar kaybolur

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


    private fun messageReceiverCheck(userMainKey:String,userAnotherKey:String, callback: (Int) -> Unit){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("messages").child(chatKey)

        var returnNumber = 0
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){

                    val user = i.getValue(MessagesData::class.java)
                    if (user != null) {
                        if ((user.senderUserKey.equals(userMainKey) && user.receiverUserKey.equals(userAnotherKey)) ||
                            user.senderUserKey.equals(userAnotherKey) && user.receiverUserKey.equals(userMainKey)){
                            returnNumber = 1 // son mesaj ana kullanıcı tarafından atıldı
                        }
                        if (user.senderUserKey.equals(userAnotherKey) && user.receiverUserKey.equals(userMainKey)){
                            returnNumber = 2 // son mesaj karşı taraftan atıldı
                        }
                    }
                    callback(returnNumber)
                    messageReceiver = returnNumber
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun checkerIsChattedBefore(user1Key:String,user2Key:String,user1Username:String,user2Username:String){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("chats")
        var newChat = ChatData()
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isChattedBefore = false // Daha önce sohbet edilmiş mi kontrolü
                var keyChat = ""
                    for (i in snapshot.children) {

                        val u = i.getValue(ChatData::class.java)
                        if (u != null) {
                            if ((u.user1Key.equals(user1Key) && u.user2Key.equals(user2Key)) || (u.user1Key.equals(user2Key) && u.user2Key.equals(user1Key))) {
                                Log.e("birebir eşleşme: ", "sohbet edilmiş")
                                isChattedBefore = true
                                chatKey = i.key!!
                                keyChat = i.key!!
                                //updateSeenMessage(chatKey, userKeyMain, userkeyForAnotherUser)
                                getMessage(userKeyMain, userkeyForAnotherUser, chatKey)
                                updateSeenMessage(chatKey, userKeyMain, userkeyForAnotherUser)
                                break
                            }
                        }
                    }

                    if (!isChattedBefore){
                        val randomChatID = generateRandomKey()
                        val createdChatDate = System.currentTimeMillis().toString()
                        Log.e("keyChat: ", keyChat)
                        newChat = ChatData(randomChatID,user1Key,user2Key,user1Username,user2Username,createdChatDate,"","","","")
                        ref.push().setValue(newChat)
                        checkerIsChattedBefore(user1Key, user2Key, user1Username, user2Username)
                        Log.e("uzun ifin else kısmı", "sohbet edilmemiş")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun generateRandomKey(): String {
        // Rastgele karakterlerin kullanılacağı karakter kümesi
        val characterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        // Rastgele bir karakter kümesi oluşturmak için SecureRandom kullan
        val secureRandom = SecureRandom()

        // 64 karakterlik rastgele bir anahtar oluştur
        val randomKeyBuilder = StringBuilder()
        for (i in 0..31) {
            val randomIndex = secureRandom.nextInt(characterSet.length)
            val randomChar = characterSet[randomIndex]
            randomKeyBuilder.append(randomChar)
        }
        return randomKeyBuilder.toString()
    }


    private fun findUserKeyFromUsername(username:String){
        val db = FirebaseDatabase.getInstance().getReference("users").orderByChild("username").equalTo(username)
        db.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    Log.e("ukey: ", userkeyForAnotherUser)
                    userkeyForAnotherUser = i.key.toString()
                    Log.e("ukey: ", userkeyForAnotherUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}