package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.activity.MessagesScreenActivity
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.FollowData
import com.emirpetek.chatpal.chatpal.data.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendlistAdapter (private val mContext: Context, private val requestList:List<FollowData>)
    : RecyclerView.Adapter<FriendlistAdapter.CardViewViewObjHolder>()
{

    private var userKeyForAnotherUser = ""
    private lateinit var sharedPreferences: SharedPreferences

    inner class CardViewViewObjHolder(view: View):RecyclerView.ViewHolder(view) {

        var card:CardView
        var textViewCardFriendName:TextView
        var imageViewCardManageFriendship:ImageView
        var buttonCardSendMessage: Button

        init {
            card = view.findViewById(R.id.cardViewFriend)
            textViewCardFriendName = view.findViewById(R.id.textViewCardFriendName)
            imageViewCardManageFriendship = view.findViewById(R.id.imageViewCardManageFriendship)
            buttonCardSendMessage = view.findViewById(R.id.buttonCardSendMessage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewViewObjHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_friend_list,parent,false)

        return CardViewViewObjHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(holder: CardViewViewObjHolder, position: Int) {
        var u = requestList[position].followerUserKey!!
        var username = ""

        sharedPreferences = mContext.getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userkey = sharedPreferences.getString("userKey","")

        usernameFinder(u) { usernameForAnotherUser ->

            holder.textViewCardFriendName.text = usernameForAnotherUser
            username = usernameForAnotherUser

            holder.imageViewCardManageFriendship.visibility = View.INVISIBLE


            holder.buttonCardSendMessage.setOnClickListener {
                var intent = Intent(mContext, MessagesScreenActivity::class.java)
                userKeyGetter(username,intent)
                //intent.putExtra("userKeyAnotherUser",userKeyForAnotherUser)
                Log.e("userkeyanotherperson", userKeyForAnotherUser)

            }
        }

    }

    private fun usernameFinder(userKey: String, callback: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").orderByKey().equalTo(userKey)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val u = i.getValue(Users::class.java)
                    if (u != null) {
                        val usernameForAnotherUser = u.username!!
                        callback(usernameForAnotherUser)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Database error: ${error.message}")
            }
        })
    }

    private fun userKeyGetter(username:String,intent: Intent){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").orderByChild("username").equalTo(username)
        var userKey = ""
        myRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        userKey = i.key!!
                        intent.putExtra("userKeyAnotherUser",userKey)
                        intent.putExtra("usernameAnotherUser",username)
                        Log.e("userkeyanotherperson fonk içi", userKey)

                    }
                mContext.startActivity(intent)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}