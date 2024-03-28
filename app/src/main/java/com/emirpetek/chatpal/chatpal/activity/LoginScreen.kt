package com.emirpetek.chatpal.chatpal.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.databinding.ActivityLoginScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest

class LoginScreen : AppCompatActivity() {
    private lateinit var binding:ActivityLoginScreenBinding
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        var usernameMain: String? = null
        var userKey: String? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRememberMe()

        binding.textViewSignUp.setOnClickListener {
            val intent = Intent(this, SignUpScreen::class.java)
            startActivity(intent)
        }

        binding.textViewForgetPassword.setOnClickListener {
            Toast.makeText(this, R.string.not_working_feature,Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogin.setOnClickListener {
            loginFunc()
        }
    }

    private fun saveRememberMe(){

        sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if(binding.checkBoxRememberMe.isChecked){
            editor.putString("rememberMe","true")
            editor.apply()
        }
    }

    private fun checkRememberMe() {
        sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences.getString("rememberMe", "false")

        if (rememberMe == "true") {
            // Eğer "Beni Hatırla" işaretliyse, ana ekrana yönlendirir
            MainActivity.whereComingFrom = "checkRememberMe"
            startActivity(Intent(this@LoginScreen, MainActivity::class.java))
            finish()
        }
    }





    private fun loginFunc(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val email = binding.editTextLoginEmail.editText?.text.toString()
        val password = binding.editTextLoginPassword.editText?.text.toString()

        sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (email.isEmpty() || password.isEmpty() || (email.isEmpty() && password.isEmpty())){
            Toast.makeText(applicationContext, R.string.fill_all_places,Toast.LENGTH_SHORT).show()
        }else{
            val query = myRef.orderByChild("email").equalTo(email)
            query.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isVerify = true
                    var pwDb = ""
                    var userKeyValue = ""
                    for (i in snapshot.children){
                        val u = i.getValue(Users::class.java)
                        if (u != null){
                            userKeyValue = i.key!!
                            isVerify = false
                            pwDb = u.password.toString()
                            u.email?.let { Log.e("email from db " , it) }

                            // iki bilgi sharedpreferences ile saklanır.
                            editor.putString("usernameMain", u.username.toString())
                            editor.putString("userKey",i.key!!)
                            editor.apply()
                        }
                    }
                    if (!isVerify) {
                        // sha256 ya göre şifreler
                        val hashedPassword = hashSHA256(password)
                        val hashedPwDb = hashSHA256(pwDb)

                        if (hashedPassword != null && hashedPwDb != null && hashedPassword == hashedPwDb) {
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            Toast.makeText(applicationContext, R.string.login_succesful,Toast.LENGTH_SHORT).show()
                            MainActivity.whereComingFrom = "loginButton"
                            //Log.e("usernameMain ", usernameMain!!)
                            val nowDate = System.currentTimeMillis().toString()


                            val updateInfo = HashMap<String,Any>()
                            updateInfo["activityStatus"] = "online"
                            updateInfo["lastSeen"] = nowDate
                            saveRememberMe()
                            startActivity(intent)

                            finish()
                        }else{
                            Toast.makeText(applicationContext, R.string.wrong_password,Toast.LENGTH_SHORT).show()

                        }

                    }else{
                        Toast.makeText(applicationContext, R.string.wrong_email,Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    fun hashSHA256(input: String): String {
        // MessageDigest objesi oluştur
        val digest = MessageDigest.getInstance("SHA-256")

        // Veriyi byte dizisine çevir
        val bytes = input.toByteArray()

        // Byte dizisini işle
        val hashBytes = digest.digest(bytes)

        // Hash'i hexadecimal stringe çevir
        val hexString = StringBuffer()

        for (byte in hashBytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }

        return hexString.toString()
    }


}