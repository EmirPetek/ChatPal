package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.ZoneId

class HomeFragmentAdapter(var mContext: Context,var postList: ArrayList<Map<String,PostDetailsData>>,val fragment: HomeFragment? = null):
    RecyclerView.Adapter<HomeFragmentAdapter.CardViewObjHolder>() {

        private var sharedPreferences: SharedPreferences = mContext.getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        private val userKey = sharedPreferences.getString("userKey","")!!
        private val username = sharedPreferences.getString("usernameMain","")!!

    class CardViewObjHolder(view: View):RecyclerView.ViewHolder(view){
            var textViewPostUsername:TextView = view.findViewById(R.id.textViewPostUsername)
            var textViewPostDate:TextView = view.findViewById(R.id.textViewPostDate)
            var textViewPostContent:TextView = view.findViewById(R.id.textViewPostContent)
            var imageViewPostMore:ImageView = view.findViewById(R.id.imageViewPostMore)
            var imageButtonPostLike:ImageButton = view.findViewById(R.id.imageButtonPostLike)
            var imageButtonPostComment:ImageButton = view.findViewById(R.id.imageButtonPostComment)
            var textViewPostNumberOfLikes:TextView = view.findViewById(R.id.textViewPostNumberOfLikes)
            var textViewPostNumberOfComment:TextView = view.findViewById(R.id.textViewPostNumberOfComments)

        }


    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): HomeFragmentAdapter.CardViewObjHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_post,parent,false)
        return CardViewObjHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CardViewObjHolder, position: Int) {
        val postKey = postList[position].keys.firstOrNull()!!
        val postDatas = postList[position].values.firstOrNull()!!
        holder.textViewPostUsername.text = postDatas.sharerUserName
        holder.textViewPostContent.text = postDatas.postContent

        val unixTimestamp = postDatas.shareDate

        val formattedDateTime = istanbulDateGetter(unixTimestamp!!)
        var messageTime = formattedDateTime.substring(11,16)
        var yyyy = formattedDateTime.substring(0,4)
        var mm = formattedDateTime.substring(5,7)
        var dd = formattedDateTime.substring(8,10)
        var messageDate = "$dd/$mm/$yyyy"

        val nowTimeStamp = System.currentTimeMillis().toString()

        val timeDifference = (nowTimeStamp.substring(0, nowTimeStamp.length - 3).toLong() - unixTimestamp.substring(0, unixTimestamp.length - 3).toLong())
        // timedifference saniye cinsinden gelir

        val min = timeDifference/60 // üstünden kaç dakika geçmiş onu gösterir
        val hour = timeDifference/3600 // üstünden kaç saat geçmiş onu gösterir
        //Log.e("times: ", "min: $min hour: $hour")
        val bundle = Bundle()
        if (min >= 0 && min < 60) {
            val text = "$min dakika önce"
            holder.textViewPostDate.text = text
            bundle.putString("shareDate",text)

        } else if (hour >= 1 && hour < 24) {
            val text = "$hour saat önce"
            holder.textViewPostDate.text = text
            bundle.putString("shareDate",text)

        } else {
            val text =  messageTime + " - " + messageDate
            holder.textViewPostDate.text = text
            bundle.putString("shareDate",text)

        }

        holder.imageViewPostMore.visibility = View.INVISIBLE

        likeButtonOperations(holder,postKey,userKey,postDatas)


        holder.imageButtonPostComment.setOnClickListener {

            bundle.putString("postKey",postKey)
            bundle.putString("postUsername",postDatas.sharerUserName)
            bundle.putString("postContent",postDatas.postContent)
            if (fragment != null) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_fragmentHome_to_fragmentPostComment,bundle) // yorum sayfasına anasayfadan gidiş
            }
        }

        holder.textViewPostNumberOfLikes.text = postDatas.numberOfLikes.toString()
        holder.textViewPostNumberOfComment.text = postDatas.numberOfComments.toString()

    }

    private fun likeButtonOperations(
        holder: CardViewObjHolder,
        postKey: String,
        userKey: String,
        postDatas: PostDetailsData
    ){
        manageLikeButton(postKey,userKey){ stateLike,likeKey ->

            if (stateLike.equals("like")){ // beğenmiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLike(postKey,userKey,username,holder,likeKey,"unlike",postDatas)
                }
            }else if (stateLike.equals("unlike")){ // beğeniyi geri çekmiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)

                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLike(postKey,userKey,username,holder,likeKey,"like",postDatas)
                }
            }else if (stateLike.equals("no_like") || stateLike.equals("post_no_like")){ // daha önce hiç beğenmemiş veya post hiç beğenilmemiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)

                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLike(postKey,userKey,username,holder,null,"like",postDatas)
                }

            }

        }
    }

    private fun manageLikeButton(postKey: String, userKey: String, callback: (stateLike: String, likeKey: String) -> Unit) {
        Log.e("problemli sınıf userkey: " , userKey)
        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var stateLike = ""
                var likeKey = ""
                if (snapshot.exists()) { // beğenilenler tablosunda ilgili post varsa

                    for (i in snapshot.children) {
                        // Log.e("girilen yer: ", "for dışı ")
                        val likeState = i.getValue(PostLikesData::class.java)!!
                        if (likeState != null) {
                            if (likeState.likerUserKey.equals(userKey)) { // kullanıcı beğenenler arasında var mı?
                                if (likeState.likeState.equals("like")) { // kullanıcı beğenmiş
                                    stateLike = "like"
                                }
                                if (likeState.likeState.equals("unlike")) { // kullanıcı beğeniyi geri çekmiş
                                    stateLike = "unlike"
                                }
                                likeKey = i.key!!
                                break // main kullanıcı postu beğenmişse döngüyü durdurur

                            } else { // kullanıcı hiç beğenmemiş
                                stateLike = "no_like"
                                likeKey = i.key!!

                            }

                        }
                    }
                }else{ //  beğenilenler tablosunda ilgili post yoksa
                    stateLike = "post_no_like"

                }
                callback(stateLike,likeKey) // stateLike'ı geri çağrı aracılığıyla iletilir
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun istanbulDateGetter(unixts:String):String{
        val instant = Instant.ofEpochMilli(unixts!!.toLong())
        val istanbulZoneId = ZoneId.of("Europe/Istanbul")
        val istanbulDateTime = instant.atZone(istanbulZoneId)
        val formattedDateTime = istanbulDateTime.toLocalDateTime().toString()
        return formattedDateTime
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}