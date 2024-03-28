package com.emirpetek.chatpal.chatpal.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.activity.MainActivity
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.adapter.InterestFragmentAdapter
import com.emirpetek.chatpal.chatpal.adapter.InterestsFragmentElementAdapter
import com.emirpetek.chatpal.chatpal.data.InterestsElementData
import com.emirpetek.chatpal.chatpal.data.InterestsTopicData
import com.emirpetek.chatpal.chatpal.databinding.FragmentInterestsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.StringBuilder


class InterestsFragment : Fragment() {

    private lateinit var binding:FragmentInterestsBinding
    private lateinit var adapter: InterestFragmentAdapter
    private var interestsTopicList:ArrayList<InterestsTopicData> = ArrayList()
    private var dbSelectedItemList:ArrayList<String> = ArrayList()
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInterestsBinding.inflate(inflater, container, false)
        val view = binding.root

        (context as MainActivity).setSupportActionBar(binding.toolbarInterests) // toolbari actionbar olarak algılar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)// geri tuşu
        setHasOptionsMenu(true)// menünün olduğunu bildirir

        binding.toolbarInterests.title = "İlgi Alanları" // başlık değişme
        binding.toolbarInterests.setNavigationOnClickListener {
            Toast.makeText(requireContext(),"geri tıklantı",Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        getSelectedElementFromDatabase()
        InterestsFragmentElementAdapter.selectedElements.clear()


        addTopicDataToList()
        binding.recyclerViewInterestsTopicParent.setHasFixedSize(true)
        binding.recyclerViewInterestsTopicParent.layoutManager = LinearLayoutManager(requireContext())
        adapter = InterestFragmentAdapter(requireContext(),interestsTopicList,dbSelectedItemList)
        binding.recyclerViewInterestsTopicParent.adapter = adapter


        return view

    }

    fun getSelectedElementFromDatabase(){
        InterestsFragmentElementAdapter.sharedPreferences = requireContext().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userKey = InterestsFragmentElementAdapter.sharedPreferences.getString("userKey","").toString()
        val db = FirebaseDatabase.getInstance().getReference("users").child(userKey)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInterests = snapshot.child("userInterests").getValue(String::class.java)
                if (!userInterests.isNullOrEmpty()) {
                   Log.e("userInterests: ", userInterests)

                    dbSelectedItemList.clear()
                    Log.e("ana fragment list bef: ", dbSelectedItemList.toString())
                    convertStringToInterestsElementData(userInterests)
                    Log.e("ana fragment list after: ", dbSelectedItemList.toString())

                    adapter.notifyDataSetChanged()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }



    fun convertStringToInterestsElementData(input: String){
        val part = input.split(",")

        for (x in part){
            dbSelectedItemList.add(x)

        }

    }








    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_interests,menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_save_interests -> {
                getSelectedItems()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getSelectedItems(){
        var size = InterestsFragmentElementAdapter.selectedElements.size
        Log.e("size of selectedElements: ", size.toString())
        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userKey = sharedPreferences.getString("userKey","").toString()

        if (size > 10){
            Toast.makeText(requireContext(),"10 taneden fazla ilgi alanı ekleyemezsiniz.",Toast.LENGTH_SHORT).show()
        }else if (size < 3){
            Toast.makeText(requireContext(),"En az 3 tane ilgi alanı seçiniz.",Toast.LENGTH_SHORT).show()
        }else{
            var stringSelectedElement = StringBuilder()
            for (i in 0..<size){
                val element = InterestsFragmentElementAdapter.selectedElements.get(i).toString()
                Log.e("element: ", element )
                stringSelectedElement.append(element)
            }
            Log.e("final element", stringSelectedElement.toString())
            val db = FirebaseDatabase.getInstance().getReference("users")
            val map : HashMap<String,Any> = HashMap()
            map["userInterests"] = stringSelectedElement.toString()
            db.child(userKey).updateChildren(map)
        }
    }

    private fun addTopicDataToList(){


        val elementItemSport = ArrayList<InterestsElementData>()
        elementItemSport.add(InterestsElementData("Spor","Futbol","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Basketbol","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Yüzme","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Koşu","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Yoga","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Fitness","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Tenis","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Bisiklet","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Voleybol","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Dağcılık","R.drawable.icon_sports"))
        elementItemSport.add(InterestsElementData("Spor","Bilardo","R.drawable.icon_sports"))


        interestsTopicList.add(InterestsTopicData("Spor", R.drawable.icon_sports,elementItemSport))


        val elementItemHobby = ArrayList<InterestsElementData>()
        elementItemHobby.add(InterestsElementData("Hobiler","Resim","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Sinema","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Müzik","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Tiyatro","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Edebiyat","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Fotoğrafçılık","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Heykel","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Dans","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Şiir","R.drawable.icon_hobby"))
        elementItemHobby.add(InterestsElementData("Hobiler","Moda","R.drawable.icon_hobby"))


        interestsTopicList.add(InterestsTopicData("Hobiler", R.drawable.icon_hobby,elementItemHobby))


        val elementItemScience = ArrayList<InterestsElementData>()
        elementItemScience.add(InterestsElementData("Bilim","Bilim","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Teknoloji","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Uzay","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Matematik","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Fizik","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Biyoloji","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Kimya","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Mühendislik","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Yapay Zeka","R.drawable.icon_science"))
        elementItemScience.add(InterestsElementData("Bilim","Robotik","R.drawable.icon_science"))


        interestsTopicList.add(InterestsTopicData("Bilim",
            R.drawable.icon_science,elementItemScience))


        val elementItemTravel = ArrayList<InterestsElementData>()
        elementItemTravel.add(InterestsElementData("Gezi","Seyahat","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Kamp","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Doğa Yürüyüşü","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Trekking","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Plaj Tatili","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Şehir Turu","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Kültürel Geziler","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Macera Tatili","R.drawable.icon_travel_bag"))
        elementItemTravel.add(InterestsElementData("Gezi","Yurtdışı Geziler","R.drawable.icon_travel_bag"))


        interestsTopicList.add(InterestsTopicData("Gezi",
            R.drawable.icon_travel_bag,elementItemTravel))


        val elementItemFood = ArrayList<InterestsElementData>()
        elementItemFood.add(InterestsElementData("Yemek","Mutfak","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Yemek Pişirme","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Tatlı Yapımı","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Kahvaltı Hazırlama","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Tarifler","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Fırın Yemekleri","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Diyet Yemekleri","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Fast Food","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Sushi","R.drawable.icon_food"))
        elementItemFood.add(InterestsElementData("Yemek","Vejetaryen Yemekler","R.drawable.icon_food"))


        interestsTopicList.add(InterestsTopicData("Yemek", R.drawable.icon_food,elementItemFood))

        val elementItemJob = ArrayList<InterestsElementData>()
        elementItemJob.add(InterestsElementData("Meslek","Mühendislik","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Doktor","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Avukat","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Öğretmen","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Mimar","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Hemşire","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Polis","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Programcı","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Satış Temsilcisi","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Pazarlamacı","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Yazılım Geliştirici","R.drawable.icon_job"))
        elementItemJob.add(InterestsElementData("Meslek","Grafik Tasarımcı","R.drawable.icon_job"))


        interestsTopicList.add(InterestsTopicData("Meslek", R.drawable.icon_job,elementItemJob))


    }

}