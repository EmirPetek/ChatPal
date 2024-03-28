package com.emirpetek.chatpal.chatpal.activity


import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.databinding.ActivitySignUpScreenBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding:ActivitySignUpScreenBinding
    private lateinit var cityAdapter: ArrayAdapter<String>


    companion object {
        private var citySelected: String = ""
        private var gender: String = ""
        private var isGenderChecked = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainCallFunction()
    }

    fun mainCallFunction(){
        birthdaySelecter()
        spinnerSelectCity()
        buttonSignUp()
        returnToLogin()
    }

    private fun buttonSignUp(){
        binding.buttonSignUp.setOnClickListener {
            registerUserChecker()
        }
    }

    private fun returnToLogin(){
        binding.textViewLogin.setOnClickListener {
            var intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
    }

    private fun birthdaySelecter(){
        binding.editTextBirthdate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Geçerli tarihten önceki bir tarihi sınırla (1900 yılı)
        calendar.set(1900, 0, 1)
        val minDate = calendar.timeInMillis

        // Geçerli tarihten sonraki bir tarihi sınırla (gelecek)
        val maxDate = System.currentTimeMillis()

        val datePickerDialog = DatePickerDialog(
            this,
            { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = formatDate(year, monthOfYear, dayOfMonth)
                binding.editTextBirthdate.setText(selectedDate)
            },
            currentYear, currentMonth, currentDay
        )

        // Minimum ve maksimum tarih sınırlamalarını ayarla
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate = maxDate

        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun spinnerSelectCity(){
        val turkishCities = arrayListOf(
            "Adana",
            "Adıyaman",
            "Afyonkarahisar",
            "Ağrı",
            "Aksaray",
            "Amasya",
            "Ankara",
            "Antalya",
            "Ardahan",
            "Artvin",
            "Aydın",
            "Balıkesir",
            "Bartın",
            "Batman",
            "Bayburt",
            "Bilecik",
            "Bingöl",
            "Bitlis",
            "Bolu",
            "Burdur",
            "Bursa",
            "Çanakkale",
            "Çankırı",
            "Çorum",
            "Denizli",
            "Diyarbakır",
            "Düzce",
            "Edirne",
            "Elazığ",
            "Erzincan",
            "Erzurum",
            "Eskişehir",
            "Gaziantep",
            "Giresun",
            "Gümüşhane",
            "Hakkari",
            "Hatay",
            "Iğdır",
            "Isparta",
            "İstanbul",
            "İzmir",
            "Karabük",
            "Karaman",
            "Kars",
            "Kastamonu",
            "Kayseri",
            "Kırıkkale",
            "Kırklareli",
            "Kırşehir",
            "Kilis",
            "Kocaeli",
            "Konya",
            "Kütahya",
            "Malatya",
            "Manisa",
            "Kahramanmaraş",
            "Mardin",
            "Mersin",
            "Muğla",
            "Muş",
            "Nevşehir",
            "Niğde",
            "Ordu",
            "Osmaniye",
            "Rize",
            "Sakarya",
            "Samsun",
            "Siirt",
            "Sinop",
            "Sivas",
            "Şırnak",
            "Tekirdağ",
            "Tokat",
            "Trabzon",
            "Tunceli",
            "Şanlıurfa",
            "Uşak",
            "Van",
            "Yalova",
            "Yozgat",
            "Zonguldak",
        )
        cityAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,turkishCities)
        binding.spinnerSelectCity.adapter = cityAdapter
        binding.spinnerSelectCity.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
                citySelected = turkishCities.get(index)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun registerUserChecker(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val email = binding.editTextSignUpEmail.editText?.text.toString()
        if (!isEmailValid(email)){
            Toast.makeText(applicationContext, R.string.enter_valid_email,Toast.LENGTH_SHORT).show()
        }else{
            val query = myRef.orderByChild("email").equalTo(email)
            query.addListenerForSingleValueEvent (object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var userFound = false
                    for (i in snapshot.children){
                        val u = i.getValue(Users::class.java)
                        if (u != null){
                            val key = i.key
                            userFound = true
                        }
                    }
                    if (!userFound){
                        usernameChecker()
                    }else{
                        Toast.makeText(applicationContext,
                            R.string.already_have_account_to_email,Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun usernameChecker(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val username = binding.editTextUsername.editText?.text.toString()
        if (username.isEmpty()){
            Toast.makeText(applicationContext, R.string.select_username,Toast.LENGTH_SHORT).show()
        }else{
            val query = myRef.orderByChild("username").equalTo(username)
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
                        registerUser()
                        Log.e("username checker db içi ","if durumu")
                    }else{
                        Toast.makeText(applicationContext, R.string.username_matched,Toast.LENGTH_SHORT).show()
                        Log.e("username checker db içi ","else durumu")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun genderChecked(){
        isGenderChecked = true
    }


    private fun registerUser(){

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")

        val password1 = binding.editTextSignUpPassword.editText?.text.toString()
        val passwordRepeat = binding.editTextSignUpPasswordRepeat.editText?.text.toString()
        val email = binding.editTextSignUpEmail.editText?.text.toString()
        val fullname = binding.editTextNameSurname.editText?.text.toString()
        binding.radioGroupSelectGender.setOnCheckedChangeListener { radioGroup, i ->
            val checkedGender: RadioButton? = findViewById(i)
            gender = checkedGender?.text.toString()
            genderChecked()
        }
        val username = binding.editTextUsername.editText?.text.toString()
        val password = binding.editTextSignUpPassword.editText?.text.toString()
        val birthdate = binding.editTextBirthdate.text.toString()

        val registerDate = System.currentTimeMillis().toString()


        if (fullname.isEmpty()){
            Toast.makeText(applicationContext, R.string.enter_name_surname,Toast.LENGTH_SHORT).show()
        }
        else{
            if (!isEmailValid(email)){
                Toast.makeText(applicationContext, R.string.enter_valid_email,Toast.LENGTH_SHORT).show()
            }
            else{
                if (!password1.equals(passwordRepeat) || (password1.isEmpty() || passwordRepeat.isEmpty()
                            || (password1.isEmpty() && passwordRepeat.isEmpty()))){
                    Toast.makeText(applicationContext, R.string.no_matched_password,Toast.LENGTH_SHORT).show()
                }
                else{
                    if (!isGenderChecked){
                        Toast.makeText(applicationContext, R.string.select_gender,Toast.LENGTH_SHORT).show()
                    }
                    else{
                        if (citySelected.isEmpty()){
                            Toast.makeText(applicationContext,"Şehir seçiminde hata",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            if (birthdate.isEmpty()){
                                Toast.makeText(applicationContext, R.string.enter_birthdate,Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val photo = ""
                                val lastSeen = ""
                                val activityStatus = ""
                                val user = Users(fullname,email,password,username,birthdate,
                                    gender,
                                    citySelected,registerDate, photo,lastSeen,activityStatus,"Merhaba","")
                                myRef.push().setValue(user)
                                Toast.makeText(applicationContext, R.string.signup_successful,Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginScreen::class.java))
                                finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

