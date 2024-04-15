package com.emirpetek.chatpal.chatpal.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.adapter.DiscoverFragmentAdapter
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.data.UsersWithUserKey
import com.emirpetek.chatpal.chatpal.databinding.FragmentDiscoverBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DiscoverFragment : Fragment() {

    private lateinit var binding:FragmentDiscoverBinding
    private var userList:ArrayList<UsersWithUserKey> = ArrayList()
    private lateinit var adapter: DiscoverFragmentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentDiscoverBinding.inflate(inflater,container,false);
        val view = binding.root;

        binding.toolbarDiscoverFragment.setTitle(R.string.discover)
        binding.toolbarDiscoverFragment.setTitleTextColor(Color.BLACK)

        getDiscoverUser()

        userList.shuffled()
        binding.recyclerViewDiscoverUsers.setHasFixedSize(true)
        binding.recyclerViewDiscoverUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = DiscoverFragmentAdapter(requireContext(),userList)
        binding.recyclerViewDiscoverUsers.adapter = adapter

        binding.progressBarFragmentDiscover.visibility = View.VISIBLE // ilk girişte pb oluşturur


        binding.recyclerViewDiscoverUsers.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        val position = parent.getChildAdapterPosition(view)
                        if (position != 0) {
                            outRect.left = 20 // Kartların arasında 16dp boşluk bırakır
                        }
                    }
                })

        val snapHelper:SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewDiscoverUsers)

        return view
    }


    private fun getDiscoverUser(){
        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference("users")
        userList.clear()
        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val mainUserKey = sharedPreferences.getString("userKey","")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val user = i.getValue(Users::class.java)
                    if (user != null){
                        if (i.key.equals(mainUserKey)){
                            continue
                        }else{
                            val userKey = i.key
                            var userAllData = UsersWithUserKey(userKey,user)
                            userList.add(userAllData)
                        }
                    }
                }
                userList.shuffle()
                adapter.notifyDataSetChanged()
                binding.progressBarFragmentDiscover.visibility = View.GONE // veriler geldikten sonra pb kaybolur

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}

