package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DiscoverFragmentAdapter(var mContext:Context,var userList:ArrayList<UsersWithUserKey>):
    RecyclerView.Adapter<DiscoverFragmentAdapter.CardViewObjHolder>() {

        companion object{
            lateinit var interestList : ArrayList<String>
        }
    class CardViewObjHolder(view: View):RecyclerView.ViewHolder(view) {
         var textViewDiscoverUsername:TextView = view.findViewById(R.id.textViewDiscoverUsername)
         var textViewDiscoverUserAge:TextView = view.findViewById(R.id.textViewDiscoverUserAge)
         var buttonDiscoverSendMessage:Button = view.findViewById(R.id.buttonDiscoverSendMessage)
         var textViewDiscoverAboutMe:TextView = view.findViewById(R.id.textViewDiscoverAboutMe)
         var imageViewDiscoverProfilePhoto:ImageView = view.findViewById(R.id.imageViewDiscoverProfilePhoto)
         var recyclerViewDiscoverUserInterests:RecyclerView = view.findViewById(R.id.recyclerViewDiscoverUserInterests)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewObjHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.card_people_list_big,parent,false)
        return CardViewObjHolder(view)

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CardViewObjHolder, position: Int) {
        val pos = userList.elementAt(position)
        val username = pos.user!!.username
        val userKey = pos.userKey
        val userBirthdate = pos.user.birthdate!!
        val userAge = ProfileFragment().calculateAge(userBirthdate)
        val userAboutMe = pos.user.userAboutMe
        val userInterest = pos.user.userInterests!!

        holder.textViewDiscoverUsername.text = username
        holder.textViewDiscoverUserAge.text = userAge.toString()
        Log.e("aboutme discover: ", "$userAboutMe ${userAboutMe.isNullOrEmpty()}")

        if (userAboutMe.isNullOrEmpty() ) {
            holder.textViewDiscoverAboutMe.text = "Merhaba"
        } else {
            holder.textViewDiscoverAboutMe.text = userAboutMe
        }

        rvInterest(holder,userInterest)

        holder.imageViewDiscoverProfilePhoto.setOnClickListener {
            val intent = Intent(mContext, UserScreenActivity::class.java)
            intent.putExtra("key",userKey)
            intent.putExtra("username",username)
            mContext.startActivity(intent)
        }

        holder.buttonDiscoverSendMessage.setOnClickListener {
            val intent = Intent(mContext,MessagesScreenActivity::class.java)
            intent.putExtra("userKeyAnotherUser",userKey)
            intent.putExtra("usernameAnotherUser",username)
            mContext.startActivity(intent)
        }

    }

    private fun rvInterest(holder: CardViewObjHolder,userInterest:String){
        if (userInterest.isNotEmpty()){
            interestList = ArrayList()

            holder.recyclerViewDiscoverUserInterests.setHasFixedSize(true)

            // string olarak gelen veri , ile ayrılarak arrayliste çevrilir
            val part = userInterest.split(",")
            for (x in part){
                interestList.add(x)
            }
            val size = getInterestListSize(interestList)
            holder.recyclerViewDiscoverUserInterests.layoutManager = GridLayoutManager(mContext,size)
            var adapter = DiscoverFragmentInterestAdapter(mContext,interestList)
            holder.recyclerViewDiscoverUserInterests.adapter = adapter

            adapter.notifyDataSetChanged()
        }
    }

    private fun getInterestListSize(interestsList:ArrayList<String>):Int{
        var size = (interestsList.size-1)/3
        if (size > 3){
            if (size == 1) return 1
            else if (size == 2) return 2
            else return 3
        }else{
            return size
        }
    }
}