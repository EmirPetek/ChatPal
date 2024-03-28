package com.emirpetek.chatpal.chatpal.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.InterestsElementData

class InterestsFragmentElementAdapter(val mContext: Context, val mList: List<InterestsElementData>, val dbSelectedItemList: ArrayList<String>)
    : RecyclerView.Adapter<InterestsFragmentElementAdapter.ChildViewHolder>() {

        companion object{
            var selectedElements : ArrayList<String> = ArrayList()
            lateinit var sharedPreferences:SharedPreferences
        }

    inner class ChildViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val checkBoxInterestFragmentChildElement:CheckBox = itemView.findViewById(R.id.checkBoxInterestFragmentChildElement)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_interests_element_child, parent, false)
        return ChildViewHolder(view)    }

    override fun onBindViewHolder(
        holder: ChildViewHolder,
        position: Int
    ) {
        val childItem = mList[position].title
        val parentItem = mList[position].parentTitle
        val parentIcon = mList[position].parentTitleIcon
        holder.checkBoxInterestFragmentChildElement.text = childItem

        setCheckBoxWithInterestsDataList(dbSelectedItemList,holder,childItem,parentItem,parentIcon)

        holder.checkBoxInterestFragmentChildElement.setOnClickListener {

            val itemFinal = "$parentItem,$childItem,$parentIcon,"
            if (holder.checkBoxInterestFragmentChildElement.isChecked) {

                selectedElements.add(itemFinal)
            }
            if (!holder.checkBoxInterestFragmentChildElement.isChecked) {
                selectedElements.remove(itemFinal)
            }
        }


    }

    fun setCheckBoxWithInterestsDataList(
        interestsDataList: ArrayList<String>,
        holder: ChildViewHolder,
        childItem: String,
        parentItem: String,
        parentIcon:String
    ) {

        for (i in 0..<interestsDataList.size){

            if (i % 3 == 1){
                val index = interestsDataList.get(i)
                if (childItem == index){
                    val finalItem = "$parentItem,$childItem,$parentIcon,"
                    selectedElements.add(finalItem)
                    holder.checkBoxInterestFragmentChildElement.isChecked = true
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

}