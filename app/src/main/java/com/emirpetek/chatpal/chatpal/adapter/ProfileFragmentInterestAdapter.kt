package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.R

class ProfileFragmentInterestAdapter(var mContext: Context, var interestsList: ArrayList<String>):
    RecyclerView.Adapter<ProfileFragmentInterestAdapter.CardViewHolder>(){
        inner class CardViewHolder(view: View):RecyclerView.ViewHolder(view){

            var imageViewInterestsProfileIcon:ImageView = view.findViewById(R.id.imageViewInterestsProfileIcon)
            var textViewInterestsProfile:TextView = view.findViewById(R.id.textViewInterestsProfile)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_interests_profile, parent, false)
        return CardViewHolder(view)    }

    override fun getItemCount(): Int {
        var counter = 0
        for (i in 0..<interestsList.size){

            if (i % 3 == 1){
                counter++
            }
        }
        return (interestsList.size-1)/3
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        Log.e("item count",itemCount.toString())
        Log.e("position number",position.toString())

        var newPosition = position*3 + 3
        holder.textViewInterestsProfile.text = interestsList.get(newPosition-2)

        val iconName = interestsList[newPosition-1] // String olarak drawable kaynağı adını al
        val nicon = iconName.substring(11)
        val resID = mContext.resources.getIdentifier(nicon, "drawable", mContext.packageName)
        if (resID != 0) {
            val draw =  ContextCompat.getDrawable(mContext, resID)
            holder.imageViewInterestsProfileIcon.setImageDrawable(draw)

        }

    }


}
