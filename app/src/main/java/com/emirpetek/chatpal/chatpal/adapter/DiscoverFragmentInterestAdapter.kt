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


class DiscoverFragmentInterestAdapter(var mContext: Context, var interestsList: ArrayList<String>):
    RecyclerView.Adapter<DiscoverFragmentInterestAdapter.CardViewHolder>(){

        private var CARD_NUMBER_LIMIT_1 = 1
        private var CARD_NUMBER_LIMIT_2 = 2
        private var CARD_NUMBER_LIMIT_3 = 3

        inner class CardViewHolder(view:View):RecyclerView.ViewHolder(view){
            var imageViewInterestsProfileIcon: ImageView = view.findViewById(R.id.imageViewInterestsProfileIcon)
            var textViewInterestsProfile: TextView = view.findViewById(R.id.textViewInterestsProfile)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_interests_profile, parent, false)
        return CardViewHolder(view)     }

    override fun getItemCount(): Int {

        var size = (interestsList.size-1)/3
        if (size > CARD_NUMBER_LIMIT_3){
            if (size == CARD_NUMBER_LIMIT_1) return 1
            else if (size == CARD_NUMBER_LIMIT_2) return 2
            else return 3
        }else{
            return size
        }
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        var newPosition = position*3 + 3


        val iconName = interestsList[newPosition-1] // String olarak drawable kaynağı adını al
        val nicon = iconName.substring(11)
        val resID = mContext.resources.getIdentifier(nicon, "drawable", mContext.packageName)
        if (resID != 0) {
            val draw =  ContextCompat.getDrawable(mContext, resID)
            holder.imageViewInterestsProfileIcon.setImageDrawable(draw)
        }
        holder.textViewInterestsProfile.text = interestsList.get(newPosition-2)

        val element = position + 3
        if (position > 2){

        }else {
            if (element % 3 == 1) {
                //holder.textViewInterestsProfile.text = interestsList.get(element)
                Log.e("el", element.toString())
            }
        }
    }
    fun getDrawable(context: Context, name: String?): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}