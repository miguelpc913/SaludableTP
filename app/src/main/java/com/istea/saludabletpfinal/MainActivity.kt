package com.istea.saludabletpfinal

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.istea.saludabletpfinal.dao.DBHelper
import com.istea.saludabletpfinal.databinding.ActivityMainBinding
import com.istea.saludabletpfinal.models.Paciente
import org.json.JSONObject
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onResume() {
        super.onResume()
        MyApplication.setContext(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);
        MyApplication.setContext(this)
        MyApplication.checkForInternetConnectivity()
        if (!MyApplication.listening) {
            MyApplication.listenForInternetConnectivity()
        }

        database = Firebase.database.reference
        binding.CreateAccountButton.setOnClickListener {
            if (MyApplication.connected) {
                val intent = Intent(this , SignInActivity::class.java)
                startActivity(intent);
            }else{
                Toast.makeText(this , "Estas usando el modo offline, no puedes crear cuentas nuevas" , Toast.LENGTH_SHORT).show()
            }
        }
        binding.LoginButton.setOnClickListener {
            val username = binding.LoginUsuario.text.toString();
            val password = binding.LoginPassword.text.toString();
            if(username.isNotBlank() && password.isNotBlank()) {
                if (MyApplication.connected) {
                    logInWithFirebase(username, password)
                } else {
                    //Look into local DB
                    logInLocally(username, password)
                }
            }else{
                Toast.makeText(this , "Por favor ingrese los datos para loggearse" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logInWithFirebase(username : String , password : String){
        val db = DBHelper(this,null)
        val intent = Intent(this , MealActivity::class.java);
        database.child("users/$username").get().addOnSuccessListener {
            val objectDataSnapshot = it.getValue(Any::class.java)
            val json = Gson().toJson(objectDataSnapshot)
            var paciente = Gson().fromJson(json, Paciente::class.java)
            if (paciente.password.equals(password)) {
                intent.putExtra("pacientedLoggedIn", paciente);
                if(!db.checkIfPacienteExistInDB(paciente.usuario , paciente.password)) {
                    db.createPaciente(paciente)
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Usuario no existente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logInLocally(username : String , password : String){
        val db = DBHelper(this,null)
        val intent = Intent(this , MealActivity::class.java);
        if(db.checkIfPacienteExistInDB(username , password)){
            val pacientes = db.findPacienteInLocalDB(username , password)
            if(pacientes.count() == 1 ){
                intent.putExtra("pacientedLoggedIn", pacientes[0]);
                startActivity(intent);
            }
        }else{
            Toast.makeText(this, "No existe un usuario con esas credenciales", Toast.LENGTH_SHORT).show()
        }
    }
}


class MyApplication: Application() {


    companion object {
        private lateinit var context: Context
        var connected : Boolean = false;
        private lateinit var database: DatabaseReference
        var listening : Boolean = false;

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun listenForInternetConnectivity() {
            listening = true;
            val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    Toast.makeText(context , "Network access is available" , Toast.LENGTH_SHORT).show()
                    syncWithFirebase()
                }

                override fun onLost(network: Network) {
                    var builder = AlertDialog.Builder(context)
                    builder.setTitle("Modo Offline activado, no se encontro una red disponible")
                    builder.setMessage("Solo podra loggearse con cuentas anteriormente usadas, los nuevos alimentos registrados se syncearan cuando se vuelva a tener una red disponible (No se podran crear cuentas nuevas).")
                    builder.setPositiveButton("Estoy de acuerdo") { dialogInterface, i ->
                        connected = false
                        Toast.makeText(context , "Modo Offline activado" , Toast.LENGTH_SHORT).show()
                    }

                    builder.setNegativeButton("Prefiero no usar la app") { dialogInterface, i ->
                        exitProcess(0);
                    }

                    val alerta: AlertDialog = builder.create()
                    alerta.show()
                }
            }

            val connectivityManager = context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
        }

        fun checkForInternetConnectivity(){
            if(isNetworkAvailable()){
                Toast.makeText(context , "Network access is available" , Toast.LENGTH_SHORT).show()
                syncWithFirebase()
            }else{
                Toast.makeText(context , "Network access is NOT available" , Toast.LENGTH_SHORT).show()
                connected = false
            }
        }

        fun syncWithFirebase(){
            if(!connected){
                val db = DBHelper(context,null)
                val listOfPacientes= db.obtenerPacientes();
                listOfPacientes.forEach { paciente ->
                    val listOfMeals = db.obtenerMealBasedOnPaciente(paciente)
                    if(listOfMeals.count() > 0) {
                        database = FirebaseDatabase.getInstance().getReference("users/${paciente.usuario}/meals")
                        listOfMeals.forEach { meal ->
                            database.push().setValue(meal).addOnSuccessListener {
                                Toast.makeText(context, "Meals synceados con firebase", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                db.deleteMeals();
                connected = true
            }
        }

        fun isNetworkAvailable(): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw      = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    //for other device how are able to connect with Ethernet
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    //for check internet over Bluetooth
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    else -> false
                }
            } else {
                return connectivityManager.activeNetworkInfo?.isConnected ?: false
            }
        }

        fun setContext(con: Context) {
            context=con
        }

    }
}

