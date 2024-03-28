package com.emirpetek.chatpal.chatpal.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.emirpetek.chatpal.chatpal.activity.LoginScreen
import com.emirpetek.chatpal.chatpal.activity.MainActivity
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.databinding.FragmentProfileSettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class ProfileSettingsFragment : Fragment() {

    private lateinit var binding:FragmentProfileSettingsBinding
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileSettingsBinding.inflate(inflater,container,false)
        val view = binding.root

        val bundle = arguments
        val userkey = bundle?.getString("userkey").toString()

        setToolbar()
        logoutOperations()
        setUserDetails(userkey)
        privacyAndPolicy()
        aboutApplication()
        return view
    }

    fun aboutApplication(){
        binding.textViewShowAboutApplication.setOnClickListener {
            val client = OkHttpClient()
            val url = "https://emirpetek.com/chatPal/about.html"

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    (context as? MainActivity)?.runOnUiThread {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(responseBody)
                        builder.setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }
            })
        }
    }

    fun privacyAndPolicy() {
        binding.textViewProfileSettingsSeeAboutPrivacyPolicy.setOnClickListener {
            val client = OkHttpClient()
            val url = "https://emirpetek.com/chatPal/privacy.html"

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    (context as? MainActivity)?.runOnUiThread {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(responseBody)
                        builder.setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }
            })
        }
    }

    fun setUserDetails(userkey:String){
        val db = FirebaseDatabase.getInstance().getReference("users").orderByChild(userkey)
        db.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val user = i.getValue(Users::class.java)
                    if (user != null && i.key.equals(userkey)){
                        binding.textViewSettingsChangePersonalDetails.setOnClickListener {
                            alertSetUserDetails(userkey, user.fullname!!, user.username!!, user.email!!, user.birthdate!!, user.password!! )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun alertSetUserDetails(userkey:String, nameSurname:String, username:String, email:String, birthdate:String, password:String){
        val bindAlert = layoutInflater.inflate(R.layout.alert_user_details, null)
        val editTextAlertUserDetailsNameSurname = bindAlert.findViewById<EditText>(R.id.editTextAlertUserDetailsNameSurname)
        val editTextAlertUserDetailsUsername = bindAlert.findViewById<EditText>(R.id.editTextAlertUserDetailsUsername)
        val editTextAlertUserDetailsEmail = bindAlert.findViewById<EditText>(R.id.editTextAlertUserDetailsEmail)
        val editTextAlertUserDetailsBirthdate = bindAlert.findViewById<EditText>(R.id.editTextAlertUserDetailsBirthdate)
        val editTextAlertUserDetailsPassword = bindAlert.findViewById<EditText>(R.id.editTextAlertUserDetailsPassword)

        editTextAlertUserDetailsNameSurname.setText(nameSurname)
        editTextAlertUserDetailsUsername.setText(username)
        editTextAlertUserDetailsEmail.setText(email)
        editTextAlertUserDetailsBirthdate.setText(birthdate)
        editTextAlertUserDetailsPassword.setText(password)


        val ad = AlertDialog.Builder(requireContext())
        ad.setTitle("Bilgilerini Değiştir")
        ad.setIcon(R.drawable.baseline_create_24)
        ad.setView(bindAlert)


        ad.setPositiveButton("Kaydet"){dialogInterface:DialogInterface, i : Int ->
            val editTextAlertNameSurnameString = editTextAlertUserDetailsNameSurname.text.toString()
            val editTextAlertUserDetailsUsernameString = editTextAlertUserDetailsUsername.text.toString()
            val editTextAlertUserDetailsEmailString = editTextAlertUserDetailsEmail.text.toString()
            val editTextAlertUserDetailsBirthdateString = editTextAlertUserDetailsBirthdate.text.toString()
            val editTextAlertUserDetailsPasswordString = editTextAlertUserDetailsPassword.text.toString()
            if (editTextAlertUserDetailsUsernameString != username) {
                usernameChecker(userkey,editTextAlertNameSurnameString,editTextAlertUserDetailsUsernameString,editTextAlertUserDetailsEmailString,editTextAlertUserDetailsBirthdateString,editTextAlertUserDetailsPasswordString)
            }else{
                validBirthdateChecker(userkey,editTextAlertNameSurnameString,editTextAlertUserDetailsUsernameString,editTextAlertUserDetailsEmailString,editTextAlertUserDetailsBirthdateString,editTextAlertUserDetailsPasswordString)
            }
        }

        ad.setNegativeButton("İptal"){dialogInterface:DialogInterface, i : Int ->

        }

        ad.show()
    }

    fun updateUserDetails(userkey:String, nameSurname:String, username:String, email:String, birthdate:String, password:String){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val updateInfo = HashMap<String,Any>()
        updateInfo["fullname"] = nameSurname
        updateInfo["username"] = username
        updateInfo["email"] = email
        updateInfo["birthdate"] = birthdate
        updateInfo["password"] = password
        ref.child(userkey).updateChildren(updateInfo)
    }


    fun logoutOperations(){
        binding.cardViewLogout.setOnClickListener {
            sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()
            editor.putString("rememberMe","false")
            editor.putString("usernameMain", "")
            editor.putString("userKey","")
            editor.apply()


            startActivity(Intent(requireActivity(), LoginScreen::class.java))
            requireActivity().finish()
            Toast.makeText(requireContext(),"Oturum sonlandırıldı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setToolbar(){
        val bottomView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomView.visibility = View.GONE

        (context as MainActivity).setSupportActionBar(binding.toolbarProfileSettings) // toolbari actionbar olarak algılar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)// geri tuşu

        binding.toolbarProfileSettings.setNavigationOnClickListener {
            findNavController().popBackStack()
            val bottomView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomView.visibility = View.VISIBLE
        }
    }

    private fun usernameChecker(userkey:String,editTextAlertNameSurnameString:String,editTextAlertUserDetailsUsernameString:String,editTextAlertUserDetailsEmailString:String,
                                editTextAlertUserDetailsBirthdateString:String,editTextAlertUserDetailsPasswordString:String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        if (editTextAlertUserDetailsUsernameString.isEmpty()){
            Toast.makeText(requireContext(),"Kullanıcı adı alanı boş olamaz",Toast.LENGTH_SHORT).show()
        }else{
            val query = myRef.orderByChild("username").equalTo(editTextAlertUserDetailsUsernameString)
            query.addListenerForSingleValueEvent (object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isUsernameMatched = false
                    for (i in snapshot.children){
                        val u = i.getValue(Users::class.java)
                        if (u != null){
                            val key = i.key
                            isUsernameMatched = true
                            u.username?.let { Log.e("username from db " , it) }
                        }
                    }
                    if (!isUsernameMatched) {
                        validBirthdateChecker(userkey,editTextAlertNameSurnameString,editTextAlertUserDetailsUsernameString,editTextAlertUserDetailsEmailString,editTextAlertUserDetailsBirthdateString,editTextAlertUserDetailsPasswordString)
                        Log.e("username checker db içi ","if durumu")
                    }else{
                        Toast.makeText(requireContext(), R.string.username_matched,Toast.LENGTH_SHORT).show()
                        Log.e("username checker db içi ","else durumu")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun validBirthdateChecker(userkey:String,editTextAlertNameSurnameString:String,editTextAlertUserDetailsUsernameString:String,editTextAlertUserDetailsEmailString:String,
                                      editTextAlertUserDetailsBirthdateString:String,editTextAlertUserDetailsPasswordString:String){
        val datePattern = """(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\d{4}""".toRegex()

        if (editTextAlertUserDetailsBirthdateString.matches(datePattern)) {
            val parts = editTextAlertUserDetailsBirthdateString.split("-")
            val day = parts[0].toInt()
            val month = parts[1].toInt()

            if (day in 1..31 && month in 1..12) {
                // Girdi, geçerli bir tarih formatına ve gün/ay sınırlarına uyuyor
                updateUserDetails(userkey,editTextAlertNameSurnameString,editTextAlertUserDetailsUsernameString,editTextAlertUserDetailsEmailString,editTextAlertUserDetailsBirthdateString,editTextAlertUserDetailsPasswordString)
            } else {
                // Girdi, gün/ay sınırlarına uygun değil
                Toast.makeText(requireContext(), "Doğum tarihi genel tarih formatına uygun değil.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Girdi, genel tarih formatına uymuyor
            Toast.makeText(requireContext(), "Doğum tarihi genel tarih formatına uygun değil.", Toast.LENGTH_SHORT).show()
        }
    }



}