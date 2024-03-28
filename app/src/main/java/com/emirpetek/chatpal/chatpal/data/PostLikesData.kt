package com.emirpetek.chatpal.chatpal.data

import android.util.Log
import com.emirpetek.chatpal.chatpal.fragment.HomeFragment
import com.emirpetek.chatpal.chatpal.adapter.HomeFragmentAdapter
import com.emirpetek.chatpal.chatpal.adapter.ProfileFragmentPostAdapter
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.adapter.UserScreenActivityPostAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PostLikesData(
    val postKey:String? = null, val likerUserKey:String? = null, val likerUsername:String? = null,
    val likeDate:String? = null,val likeState:String? = null, val likeReturnDate:String? = null) {

    fun togglePostLike(
        postKey: String?,
        likerUserKey: String?,
        likerUsername: String?,
        holder: HomeFragmentAdapter.CardViewObjHolder,
        likeKey: String?,  // Eğer varsa beğeni anahtarı
        likeState: String?,  // Eğer varsa beğeni durumu
        postData: PostDetailsData
    ) {

        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey!!)
        val likeDate = System.currentTimeMillis().toString()

        if (likeState == "no_like" || likeKey == null) {
            // Eğer beğeni anahtarı veya durumu yoksa, yeni bir beğeni ekle
            val like = PostLikesData(postKey, likerUserKey, likerUsername, likeDate, "like", "")
            ref.push().setValue(like)
            holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
            val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
            holder.textViewPostNumberOfLikes.text = (num + 1).toString()//postData.numberOfLikes?.plus(1)).toString()
        } else {
            // Eğer beğeni anahtarı ve durumu varsa, beğeni durumunu güncelle
            val refLike = ref.child(likeKey)
            val likeReturnDate = System.currentTimeMillis().toString()
            val updateLikeState = mapOf("likeState" to likeState, "likeReturnDate" to likeReturnDate)
            refLike.updateChildren(updateLikeState)
            if (likeState == "like") {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num + 1).toString()//(postData.numberOfLikes?.plus(1)).toString()
            } else {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num - 1).toString()//(postData.numberOfLikes?.minus(1)).toString()
            }
        }
    }

    fun togglePostLikeProfileFragment(
        postKey: String?,
        likerUserKey: String?,
        likerUsername: String?,
        holder: ProfileFragmentPostAdapter.CardViewObjHolder,
        likeKey: String?,  // Eğer varsa beğeni anahtarı
        likeState: String?,  // Eğer varsa beğeni durumu
        postData: PostDetailsData
    ) {

        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey!!)
        val likeDate = System.currentTimeMillis().toString()

        if (likeState == "no_like" || likeKey == null) {
            // Eğer beğeni anahtarı veya durumu yoksa, yeni bir beğeni ekle
            val like = PostLikesData(postKey, likerUserKey, likerUsername, likeDate, "like", "")
            ref.push().setValue(like)
            holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
            val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
            holder.textViewPostNumberOfLikes.text = (num + 1).toString()//postData.numberOfLikes?.plus(1)).toString()
        } else {
            // Eğer beğeni anahtarı ve durumu varsa, beğeni durumunu güncelle
            val refLike = ref.child(likeKey)
            val likeReturnDate = System.currentTimeMillis().toString()
            val updateLikeState = mapOf("likeState" to likeState, "likeReturnDate" to likeReturnDate)
            refLike.updateChildren(updateLikeState)
            if (likeState == "like") {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num + 1).toString()//(postData.numberOfLikes?.plus(1)).toString()
            } else {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num - 1).toString()//(postData.numberOfLikes?.minus(1)).toString()
            }
        }
    }

    fun togglePostLikeUserScreenActivity(
        postKey: String?,
        likerUserKey: String?,
        likerUsername: String?,
        holder: UserScreenActivityPostAdapter.CardViewObjHolder,
        likeKey: String?,  // Eğer varsa beğeni anahtarı
        likeState: String?,  // Eğer varsa beğeni durumu
        postData: PostDetailsData
    ) {

        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey!!)
        val likeDate = System.currentTimeMillis().toString()

        if (likeState == "no_like" || likeKey == null) {
            // Eğer beğeni anahtarı veya durumu yoksa, yeni bir beğeni ekle
            val like = PostLikesData(postKey, likerUserKey, likerUsername, likeDate, "like", "")
            ref.push().setValue(like)
            holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
            val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
            holder.textViewPostNumberOfLikes.text = (num + 1).toString()//postData.numberOfLikes?.plus(1)).toString()
        } else {
            // Eğer beğeni anahtarı ve durumu varsa, beğeni durumunu güncelle
            val refLike = ref.child(likeKey)
            val likeReturnDate = System.currentTimeMillis().toString()
            val updateLikeState = mapOf("likeState" to likeState, "likeReturnDate" to likeReturnDate)
            refLike.updateChildren(updateLikeState)
            if (likeState == "like") {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num + 1).toString()//(postData.numberOfLikes?.plus(1)).toString()
            } else {
                holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)
                val num = holder.textViewPostNumberOfLikes.text.toString().toInt()
                holder.textViewPostNumberOfLikes.text = (num - 1).toString()//(postData.numberOfLikes?.minus(1)).toString()
            }
        }
    }


    fun likePost(
        postKey: String?,
        likerUserKey: String?,
        likerUsername: String?,
        holder: HomeFragmentAdapter.CardViewObjHolder,
        postData: PostDetailsData
    ){
        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey!!)
        val likeDate = System.currentTimeMillis().toString()
        val like = PostLikesData(postKey,likerUserKey,likerUsername,likeDate,"like","")
        ref.push().setValue(like)
        var likes = holder.textViewPostNumberOfLikes.text.toString().toInt()
        if (likeState.equals("like")){
            likes++
            Log.e("likes: ", "$likes likefonk")
            holder.textViewPostNumberOfLikes.text = likes.toString()
        }
        if (likeState.equals("unlike")){
            likes--
            Log.e("likes: ", "$likes likefonk")
            holder.textViewPostNumberOfLikes.text = likes.toString()
        }

        holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_24)
    }

    fun unlikePost(
        postKey: String?,
        likerUserKey: String?,
        holder: HomeFragmentAdapter.CardViewObjHolder,
        likeKey: String?,
        likeState: String?,
        postData: PostDetailsData
    ){
        val ref = FirebaseDatabase.getInstance().getReference("postLikes").child(postKey!!)
        val likeReturnDate = System.currentTimeMillis().toString()

        val updateLikeState = HashMap<String,Any>()
        updateLikeState["likeState"] = likeState!!
        updateLikeState["likeReturnDate"] = likeReturnDate
        ref.child(likeKey!!).updateChildren(updateLikeState)
        HomeFragment().updateLikeNumber(postKey)
        holder.textViewPostNumberOfLikes.text = postData.numberOfLikes.toString()

        holder.imageButtonPostLike.setImageResource(R.drawable.baseline_favorite_border_24)
    }



}



