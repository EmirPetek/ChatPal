package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.InterestsTopicData

class InterestFragmentAdapter(
    val mContext: Context,
    val interestsTopicList: ArrayList<InterestsTopicData>, val dbSelectedItemList: ArrayList<String>
)
    :RecyclerView.Adapter<InterestFragmentAdapter.ParentViewHolder>() {

        inner class ParentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val textViewInterestsTopic:TextView = itemView.findViewById(R.id.textViewInterestsTopic)
            val recyclerViewInterestsElementChild:RecyclerView = itemView.findViewById(R.id.recyclerViewInterestsElementChild)
            val imageViewInterestsTopicIcon:ImageView = itemView.findViewById(R.id.imageViewInterestsTopicIcon)
        }


        override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParentViewHolder {
            val view = LayoutInflater.from(mContext).inflate(R.layout.card_interests_topic_parent,parent,false)
            return ParentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ParentViewHolder,
        position: Int
    ) {
        val parentItem = interestsTopicList[position]
        holder.imageViewInterestsTopicIcon.setImageResource(parentItem.logo)
        holder.textViewInterestsTopic.text = parentItem.title

        holder.recyclerViewInterestsElementChild.setHasFixedSize(true)
        holder.recyclerViewInterestsElementChild.layoutManager = GridLayoutManager(holder.itemView.context, 3)
        val adapter = InterestsFragmentElementAdapter(mContext,parentItem.mList,dbSelectedItemList)
        holder.recyclerViewInterestsElementChild.adapter = adapter
    }

    override fun getItemCount(): Int {
        return interestsTopicList.size
    }

}
