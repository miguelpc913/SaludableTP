package com.istea.saludabletpfinal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.istea.saludabletpfinal.dao.DBHelper
import com.istea.saludabletpfinal.databinding.ActivitySignInBinding
import com.istea.saludabletpfinal.models.Paciente
import java.util.*

class SignInActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        MyApplication.setContext(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignInBinding.inflate(layoutInflater);
        setContentView(binding.root);
        binding.SignInDatePicker.setOnClickListener{
            clickDatePicker(binding)
        }

        binding.CrearCuenta.setOnClickListener {
            if(MyApplication.connected) {
                var paciente = createUser(binding);
                if (pacienteAllFieldsChosen(paciente)) {
                    addPacienteToFirebaseDatabase(paciente);
                } else {
                    Toast.makeText(this, "Por favor complete el formulario para crear un usuario", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "No puede crear el usuario sin acceso a red", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun addPacienteToFirebaseDatabase(paciente : Paciente){
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(paciente.usuario).get().addOnSuccessListener {
            if (!it.exists()) {
                database.child(paciente.usuario).setValue(paciente).addOnSuccessListener {
                    Toast.makeText(this, "Usuario guardado en firebase", Toast.LENGTH_LONG).show()
                    val db = DBHelper(this,null)
                    db.createPaciente(paciente)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent);
                }
            }else{
                Toast.makeText(this, "Ya existe una persona con ese usuario, por favor cambie el usuario", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createUser (binding: ActivitySignInBinding) : Paciente{
        val Nombre : String  = binding.SignInNombre.text.toString()
        val Apellido : String = binding.SignInApellido.text.toString();
        val DNI : String = binding.SignInDNI.text.toString();
        val SexoRadioGroup : RadioGroup =  binding.SignInGeneroGroup;
        val GeneroId :Int = SexoRadioGroup.checkedRadioButtonId;
        var Genero : String = ""
        if(GeneroId !== -1){
            val GeneroRadio : RadioButton = findViewById(GeneroId);
            Genero = GeneroRadio.text.toString();
        }
        val FechaNacimiento : String = binding.SignInDate.text.toString();
        val Localidad : String = binding.SignInLocalidad.text.toString();
        val Usuario : String = binding.SignInUsuario.text.toString();
        val Password : String = binding.SignInPassword.text.toString();
        val TratamientoRadioGroup : RadioGroup =  binding.SignInTratamientoGroup;
        val TratamientoId : Int = TratamientoRadioGroup.checkedRadioButtonId;
        var Tratamiento : String = ""
        if(TratamientoId !== -1){
            val TratamientoRadio : RadioButton = findViewById(TratamientoId);
            Tratamiento = TratamientoRadio.text.toString();
        }

        return Paciente(Nombre , Apellido , DNI , Genero , FechaNacimiento , Localidad , Usuario , Password , Tratamiento)
    }

    private fun clickDatePicker(binding: ActivitySignInBinding){

        val myCalendar = Calendar.getInstance();
        val year = myCalendar.get(Calendar.YEAR);
        val month = myCalendar.get(Calendar.MONTH);
        val day = myCalendar.get(Calendar.DAY_OF_MONTH);

        val datePickerDialog = DatePickerDialog(this ,
            DatePickerDialog.OnDateSetListener{
                    _ , selectedYear, selectedMonth, selectedDay ->
                val newSelectedMonth = selectedMonth + 1 ;
                val selectedDate = "$selectedDay/$newSelectedMonth/$selectedYear";
                binding.SignInDate.setText(selectedDate)
            } ,
            year ,
            month ,
            day)

        datePickerDialog.datePicker.maxDate = Date().time - 86400000;
        datePickerDialog.show();
    }

    private fun pacienteAllFieldsChosen(paciente: Paciente) : Boolean{
        return paciente.nombre.isNotBlank() && paciente.apellido.isNotBlank()
                && paciente.dni.isNotBlank() && paciente.genero.isNotBlank()
                && paciente.fechaNacimiento.isNotBlank() && paciente.localidad.isNotBlank()
                && paciente.usuario.isNotBlank() && paciente.password.isNotBlank()
                && paciente.tratamiento.isNotBlank()
    }

}