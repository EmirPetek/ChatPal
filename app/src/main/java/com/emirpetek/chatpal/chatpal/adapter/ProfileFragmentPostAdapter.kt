package com.emirpetek.chatpal.chatpal.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.emirpetek.chatpal.chatpal.fragment.ProfileFragment
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.PostDetailsData
import com.emirpetek.chatpal.chatpal.data.PostLikesData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.ZoneId

class ProfileFragmentPostAdapter(
    val mContext: Context,
    val postList: ArrayList<Map<String, PostDetailsData>>,
    val fragment: ProfileFragment? = null
) :
    RecyclerView.Adapter<ProfileFragmentPostAdapter.CardViewObjHolder>(){

        inner class CardViewObjHolder(view: View):RecyclerView.ViewHolder(view){
            var textViewPostUsername: TextView = view.findViewById(R.id.textViewPostUsername)
            var textViewPostDate: TextView = view.findViewById(R.id.textViewPostDate)
            var textViewPostContent: TextView = view.findViewById(R.id.textViewPostContent)
            var imageViewPostMore: ImageView = view.findViewById(R.id.imageViewPostMore)
            var imageButtonPostLike: ImageButton = view.findViewById(R.id.imageButtonPostLike)
            var imageButtonPostComment: ImageButton = view.findViewById(R.id.imageButtonPostComment)
            var textViewPostNumberOfLikes: TextView = view.findViewById(R.id.textViewPostNumberOfLikes)
            var textViewPostNumberOfComment: TextView = view.findViewById(R.id.textViewPostNumberOfComments)
            var cardViewPost:CardView = view.findViewById(R.id.cardViewPost)
            var linearLayoutCardPost:LinearLayout = view.findViewById(R.id.linearLayoutCardPost)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewObjHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_post, parent, false)
        return CardViewObjHolder(view)      }

    override fun getItemCount(): Int {
            return postList.size
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CardViewObjHolder, position: Int) {
        val postKey = postList[position].keys.firstOrNull()!!
        val postDatas = postList[position].values.firstOrNull()!!
        val userKey = postDatas.sharerUserKey
        val username = postDatas.sharerUserName
        holder.textViewPostUsername.text = postDatas.sharerUserName
        holder.textViewPostContent.text = postDatas.postContent
        holder.textViewPostContent.maxWidth = 800

        val unixTimestamp = postDatas.shareDate!!
        val bundle = Bundle()

        postTimeBinder(holder,unixTimestamp,bundle)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(16,4,16,0)
        holder.linearLayoutCardPost.layoutParams = layoutParams

        likeButtonOperations(holder,postKey,userKey!!,postDatas,username!!)


        holder.imageButtonPostComment.setOnClickListener {
            bundle.putString("postKey",postKey)
            bundle.putString("postUsername",postDatas.sharerUserName)
            bundle.putString("postContent",postDatas.postContent)
            if (fragment != null) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_fragmentProfile_to_FragmentPostComment,bundle)
            }
        }

        holder.textViewPostNumberOfLikes.text = postDatas.numberOfLikes.toString()
        holder.textViewPostNumberOfComment.text = postDatas.numberOfComments.toString()
        holder.imageViewPostMore.setOnClickListener {
            val popUp = PopupMenu(mContext, holder.imageViewPostMore)
            popUp.menuInflater.inflate(R.menu.manage_post, popUp.menu)
            popUp.setOnMenuItemClickListener { item ->

                when (item.itemId) {
                    R.id.action_delete_post -> {
                        deletePostOperation(holder,postKey)
                        true
                    }
                    R.id.action_update_post -> {
                        updatePostOperation(postDatas.postContent.toString(),postKey)
                        true
                    }
                    else -> false
                }
            }
            popUp.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun postTimeBinder(holder: CardViewObjHolder, time:String, bundle: Bundle){
        val formattedDateTime = istanbulDateGetter(time)
        var messageTime = formattedDateTime.substring(11,16)
        var yyyy = formattedDateTime.substring(0,4)
        var mm = formattedDateTime.substring(5,7)
        var dd = formattedDateTime.substring(8,10)
        var messageDate = "$dd/$mm/$yyyy"

        val nowTimeStamp = System.currentTimeMillis().toString()

        val timeDifference = (nowTimeStamp.substring(0, nowTimeStamp.length - 3).toLong() - time.substring(0, time.length - 3).toLong())
        // timedifference saniye cinsinden gelir

        val min = timeDifference/60 // üstünden kaç dakika geçmiş onu gösterir
        val hour = timeDifference/3600 // üstünden kaç saat geçmiş onu gösterir
        //Log.e("times: ", "min: $min hour: $hour")

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
    }

    fun updatePostOperation(postContent: String,postKey: String){
        val bindAlert = LayoutInflater.from(mContext).inflate(R.layout.alert_update_post,null)
        val editTextUpdatePost = bindAlert.findViewById<EditText>(R.id.editTextUpdatePost)

        editTextUpdatePost.setText(postContent)

        val ad = AlertDialog.Builder(mContext)
        ad.setTitle(R.string.update_comment)
        //ad.setMessage(R.string.enter_looking_for_username)
        ad.setIcon(R.drawable.baseline_people_24)
        ad.setView(bindAlert)

        ad.setPositiveButton(R.string.update){ dialog: DialogInterface, i: Int ->
            val updatedContent = editTextUpdatePost.text.toString()


            val updateInfo = HashMap<String, Any>()
            updateInfo["postContent"] = updatedContent
            //updateInfo["shareDate"] = System.currentTimeMillis().toString()
            val myRef = FirebaseDatabase.getInstance().getReference("posts")
            myRef.child(postKey).updateChildren(updateInfo)
        }
        ad.setNegativeButton(R.string.cancel){ dialog: DialogInterface, i : Int ->

        }

        ad.show()
    }

    fun deletePostOperation(holder: CardViewObjHolder, postKey: String){
        // Snackbar'ı göster
        Snackbar.make(holder.imageViewPostMore, "Post Silinsin mi?", Snackbar.LENGTH_LONG)
            .setAction("Evet") {
                // Evet'e tıklandığında yapılacak işlemler
                val updateInfo = HashMap<String, Any>()
                updateInfo["deleteState"] = "passive"
                updateInfo["deleteDate"] = System.currentTimeMillis().toString()
                val myRef = FirebaseDatabase.getInstance().getReference("posts")
                myRef.child(postKey).updateChildren(updateInfo)
            }.show()
    }

    private fun likeButtonOperations(
        holder: CardViewObjHolder,
        postKey: String,
        userKey: String,
        postDatas: PostDetailsData,
        username:String
    ){
        manageLikeButton(postKey,userKey){ stateLike,likeKey ->

            if (stateLike.equals("like")){ // beğenmiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)

                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLikeProfileFragment(postKey,userKey,username,holder,likeKey,"unlike",postDatas)
                }
            }else if (stateLike.equals("unlike")){ // beğeniyi geri çekmiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)

                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLikeProfileFragment(postKey,userKey,username,holder,likeKey,"like",postDatas)
                }
            }else if (stateLike.equals("no_like") || stateLike.equals("post_no_like")){ // daha önce hiç beğenmemiş veya post hiç beğenilmemiş
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)

                holder.imageButtonPostLike.setOnClickListener {
                    PostLikesData().togglePostLikeProfileFragment(postKey,userKey,username,holder,null,"like",postDatas)
                }

            }

        }
    }

    private fun manageLikeButton(postKey: String, userKey: String, callback: (stateLike: String, likeKey: String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey)//.child("likerUserKey").equalTo(userKey)
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
                                break

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

}