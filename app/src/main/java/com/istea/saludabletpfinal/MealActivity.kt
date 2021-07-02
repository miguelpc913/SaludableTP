package com.istea.saludabletpfinal

import android.app.AlertDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.istea.saludabletpfinal.dao.DBHelper
import com.istea.saludabletpfinal.databinding.ActivityMealBinding
import com.istea.saludabletpfinal.models.Meal
import com.istea.saludabletpfinal.models.Paciente
import org.w3c.dom.Text
import java.time.LocalDateTime

class MealActivity : AppCompatActivity() {
    var createdPostreLayout : Boolean = false;
    var isTentacionTextRendered : Boolean = false;
    var createdPostreText : Boolean = false;
    private lateinit var paciente: Paciente;
    private lateinit var database: DatabaseReference

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        MyApplication.setContext(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMealBinding.inflate(layoutInflater);
        setContentView(binding.root);
        MyApplication.setContext(this)
        val paciente : Paciente = intent.getSerializableExtra("pacientedLoggedIn") as Paciente;
        binding.MealTypeGroup.setOnCheckedChangeListener { group, checkedId -> MealTypeChangeEvent(checkedId , binding) }
        binding.TentacionGroup.setOnCheckedChangeListener { group, checkedId -> TentacionChangeEvent(checkedId , binding) }

        binding.MealSubmitInfo.setOnClickListener {
            val meal = buildMeal(binding)
            if(isMealInfoValid(meal)){
                saveMeal(meal , paciente);
            }else{
                Toast.makeText(this, "Por favor llena los campos necesarios", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveMeal(meal: Meal , paciente: Paciente){
        if(MyApplication.connected){
            database = FirebaseDatabase.getInstance().getReference("users/${paciente.usuario}/meals")
            database.push().setValue(meal).addOnSuccessListener {
                Toast.makeText(this, "Meal guardado en firebase", Toast.LENGTH_LONG).show()
            }
        }else{
            val db = DBHelper(this,null)
            db.guardarMeal(meal , paciente)
            Toast.makeText(this, "Meal guardado localmente", Toast.LENGTH_LONG).show()
        }

        finish();
        intent.putExtra("pacientedLoggedIn", paciente);
        startActivity(intent);
    }

    private fun MealTypeChangeEvent(checkedId : Int , binding : ActivityMealBinding) {
        if(checkedId !== null && checkedId !== -1) {
            val MealRadio: RadioButton = findViewById(checkedId)
            val MealType: String = MealRadio.text.toString().toLowerCase();
            val PostreLayoutContainer: LinearLayout = binding.PostreLayoutOptions;
            if (shouldAddPostreLayout(MealType)) {
                val Postreoptions: View = LayoutInflater.from(this).inflate(R.layout.postre_options_layout, null)
                PostreLayoutContainer.addView(Postreoptions)
                createdPostreLayout = true
                PostreOptionsBindEvents(Postreoptions)
            } else if (shouldRemovePostreLayout(MealType)) {
                PostreLayoutContainer.removeViewAt(0)
                createdPostreLayout = false
            }
        }
    }

    private fun PostreOptionsBindEvents(Postreoptions : View){
        Postreoptions.findViewById<RadioGroup>(R.id.MealPostreGroup).setOnCheckedChangeListener{ group , checkedId ->
            run {
                if(checkedId !== null && checkedId !== -1) {
                    val PostreRadio: RadioButton = findViewById(checkedId)
                    val isPostre: Boolean = PostreRadio.text.toString().toLowerCase().equals("si");
                    val PostreTextContainer = Postreoptions.findViewById<LinearLayout>(R.id.MealLayoutPostre);
                    if (isPostre && !createdPostreText) {
                        val PostreText: View = LayoutInflater.from(this).inflate(R.layout.meal_postre_layout, null);
                        PostreTextContainer.addView(PostreText)
                        createdPostreText = true;
                    }else if(createdPostreText && !isPostre){
                        PostreTextContainer.removeViewAt(0)
                        createdPostreText = false;
                    }
                }
            }
        }
    }

    private fun TentacionChangeEvent(checkedId : Int , binding : ActivityMealBinding){
        if(checkedId !== null && checkedId !== -1) {
            val TentacionRadio: RadioButton = findViewById(checkedId)
            val IsTentacion: Boolean = TentacionRadio.text.toString().toLowerCase().equals("si");
            if (IsTentacion) {
                val TentacionText = LayoutInflater.from(this).inflate(R.layout.tentacion_texto_layout, null)
                binding.TentacionTextoContainer.addView(TentacionText)
                isTentacionTextRendered = true
            } else if (isTentacionTextRendered) {
                binding.TentacionTextoContainer.removeViewAt(0)
                isTentacionTextRendered = false
            }
        }
    }

    private fun buildMeal(binding : ActivityMealBinding) : Meal{
        val MealId : Int = binding.MealTypeGroup.checkedRadioButtonId;
        var MealType = ""
        if(MealId !== -1){
            val MealRadio : RadioButton = findViewById(MealId)
            MealType  = MealRadio.text.toString()
        }
        val PlatoPrincipal : String = binding.MealPlatoPrincipal.text.toString();
        val PlatoSecundario : String = binding.MealPlatoSecundario.text.toString()
        val Bebida : String = binding.MealBebida.text.toString();
        val TeniaPostre : Boolean = if( MealType.toLowerCase().equals("almuerzo") || MealType.toLowerCase().equals("cena") )  findViewById<RadioButton>(R.id.MealPostreSi).isChecked else false
        val Postre : String = if (TeniaPostre) findViewById<TextView>(R.id.MealPostre).text.toString()  else ""
        val TeniaTentacion : Boolean = binding.TentacionSi.isChecked
        val Tentacion : String = if(TeniaTentacion) findViewById<TextView>(R.id.MealTentacion).text.toString() else ""
        val TeniaHambre : Boolean = binding.MealHambreSi.isChecked
        val currentDateTime = LocalDateTime.now().toString()
        val meal = Meal(MealType , PlatoPrincipal , PlatoSecundario , Bebida , Postre , Tentacion , TeniaHambre , currentDateTime)
        return meal;
    }

    private fun isMealInfoValid(meal : Meal) : Boolean{
        return meal.Tipo.isNotBlank() && meal.PlatoPrincipal.isNotBlank()
    }


    private fun shouldAddPostreLayout(MealType : String) : Boolean{
        return (MealType.equals("almuerzo") || MealType.equals("cena")) && !createdPostreLayout
    }

    private fun shouldRemovePostreLayout(MealType : String) : Boolean{
        return (MealType.equals("merienda") || MealType.equals("desayuno")) && createdPostreLayout
    }
}