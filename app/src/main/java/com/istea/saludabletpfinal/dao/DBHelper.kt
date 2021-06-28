package com.istea.saludabletpfinal.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.istea.saludabletpfinal.models.Meal
import com.istea.saludabletpfinal.models.Paciente
import java.io.Serializable

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME: String = "final.db"
        private val DATABASE_VERSION: Int = 3
        val TABLA_MEAL = "Meal"
        val COLUMN_ID = "id"
        val COLUMN_TIPO = "tipo"
        val COLUMN_PLATO_PRINCIPAL = "plato_principal"
        val COLUMN_PLATO_SECUNDARIO = "plato_secundario"
        val COLUMN_BEBIDA = "bebida"
        val COLUMN_POSTRE = "postre"
        val COLUMN_TENTACION = "tentacion"
        val COLUMN_HAMBRE = "hambre"
        val COLUNM_FECHA = "fecha"
        val COLUMN_USERID = "user_id"

        val TABLA_USER = "User"
        val COLUMN_USER_ID = "id"
        val COLUMN_NOMBRE = "nombre"
        val COLUMN_APELLIDO= "apellido"
        val COLUMN_DNI = "dni"
        val COLUMN_SEXO = "sexo"
        val COLUMN_FECHA_NACIMIENTO = "fecha_de_nacimiento"
        val COLUMN_LOCALIDAD= "localidad"
        val COLUMN_USER = "username"
        val COLUMN_PASSWORD= "password"
        val COLUMN_TRATAMIENTO = "tratamiento"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // las tablas en nuestra base de datos
//        val CREATE_MEAL_TABLE = ("CREATE TABLE " +
//                TABLA_MEAL + "("
//                + COLUMN_ID + " INTEGER PRIMARY KEY,"
//                + COLUMN_TIPO + " TEXT,"
//                + COLUMN_PLATO_PRINCIPAL + " TEXT,"
//                + COLUMN_PLATO_SECUNDARIO + " TEXT,"
//                + COLUMN_BEBIDA + " TEXT,"
//                + COLUMN_POSTRE + " TEXT,"
//                + COLUMN_TENTACION + " TEXT,"
//                + COLUMN_HAMBRE + " INTEGER,"
//                + COLUNM_FECHA + " TEXT,"
//                + COLUMN_USERID + " TEXT" + ")")

        val CREATE_MEAL_TABLE = ("CREATE TABLE " +
                TABLA_MEAL + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TIPO + " TEXT,"
                + COLUMN_PLATO_PRINCIPAL + " TEXT,"
                + COLUMN_PLATO_SECUNDARIO + " TEXT,"
                + COLUMN_BEBIDA + " TEXT,"
                + COLUMN_POSTRE + " TEXT,"
                + COLUMN_TENTACION + " TEXT,"
                + COLUMN_HAMBRE + " INTEGER,"
                + COLUNM_FECHA + " TEXT,"
                + COLUMN_USERID + " REFERENCES User(ID)" + ");")

        val CREATE_USER_TABLE = ("CREATE TABLE " +
                TABLA_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NOMBRE + " TEXT,"
                + COLUMN_APELLIDO + " TEXT,"
                + COLUMN_DNI + " TEXT,"
                + COLUMN_SEXO + " TEXT,"
                + COLUMN_FECHA_NACIMIENTO + " TEXT,"
                + COLUMN_LOCALIDAD + " TEXT,"
                + COLUMN_USER + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_TRATAMIENTO + " TEXT" + ");")
        db?.execSQL(CREATE_USER_TABLE);
        db?.execSQL(CREATE_MEAL_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        if (oldVersion != newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS " + TABLA_MEAL)
            db?.execSQL("DROP TABLE IF EXISTS " + TABLA_USER)
            onCreate(db)
        }
    }

    fun guardarMeal(meal: Meal , paciente: Paciente) {
        val db = this.writableDatabase
        val values = ContentValues()
        val pacienteId : Int = findForPacienteId(paciente.usuario , paciente.password)
        if(pacienteId > -1) {
            values.put(COLUMN_TIPO, meal.Tipo)
            values.put(COLUMN_PLATO_PRINCIPAL, meal.PlatoPrincipal)
            values.put(COLUMN_PLATO_SECUNDARIO, meal.PlatoSecundario)
            values.put(COLUMN_BEBIDA, meal.Bebida)
            values.put(COLUMN_POSTRE, meal.Postre)
            values.put(COLUMN_TENTACION, meal.Tentacion)
            values.put(COLUMN_HAMBRE, meal.Hambre)
            values.put(COLUNM_FECHA, meal.Fecha)
            values.put(COLUMN_USERID, pacienteId)
            db.insert(TABLA_MEAL, null, values)
        }
    }

    fun deleteMeals() {
        val db = this.writableDatabase
        db.delete(TABLA_MEAL , null , null);
    }

    fun obtenerMeal(): ArrayList<Meal> {
        val query = "SELECT * FROM " + TABLA_MEAL
        val listaMeal: ArrayList<Meal> = ArrayList<Meal>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {

            do {
                val tipo = cursor.getString(cursor.getColumnIndex("tipo"))
                val plato_principal = cursor.getString(cursor.getColumnIndex("plato_principal"))
                val plato_secundario = cursor.getString(cursor.getColumnIndex("plato_secundario"))
                val bebida = cursor.getString(cursor.getColumnIndex("bebida"))
                val postre = cursor.getString(cursor.getColumnIndex("postre"))
                val tentacion = cursor.getString(cursor.getColumnIndex("tentacion"))
                val hambre = cursor.getString(cursor.getColumnIndex("hambre"))
                val Fecha = cursor.getString(cursor.getColumnIndex("fecha"))
                val hambreBool = hambre.toInt() > 0
                listaMeal.add(Meal(tipo, plato_principal, plato_secundario, bebida, postre, tentacion, hambreBool, Fecha))
            } while (cursor.moveToNext())
        }

        return listaMeal
    }

    fun obtenerMealBasedOnPaciente (paciente: Paciente) : ArrayList<Meal>{
        val pacienteId : Int = findForPacienteId(paciente.usuario , paciente.password)
        val listaMeal: ArrayList<Meal> = ArrayList<Meal>()
        if(pacienteId > -1) {
            val query = "SELECT * FROM $TABLA_MEAL WHERE $COLUMN_USERID= $pacienteId"
            val db = this.readableDatabase
            val cursor: Cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val tipo = cursor.getString(cursor.getColumnIndex("tipo"))
                    val plato_principal = cursor.getString(cursor.getColumnIndex("plato_principal"))
                    val plato_secundario = cursor.getString(cursor.getColumnIndex("plato_secundario"))
                    val bebida = cursor.getString(cursor.getColumnIndex("bebida"))
                    val postre = cursor.getString(cursor.getColumnIndex("postre"))
                    val tentacion = cursor.getString(cursor.getColumnIndex("tentacion"))
                    val hambre = cursor.getString(cursor.getColumnIndex("hambre"))
                    val Fecha = cursor.getString(cursor.getColumnIndex("fecha"))
                    val hambreBool = hambre.toInt() > 0
                    listaMeal.add(Meal(tipo, plato_principal, plato_secundario, bebida, postre, tentacion, hambreBool, Fecha))
                } while (cursor.moveToNext())
            }
        }
        return listaMeal;
    }

    fun createPaciente(paciente: Paciente) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NOMBRE, paciente.nombre)
        values.put(COLUMN_APELLIDO, paciente.apellido)
        values.put(COLUMN_DNI, paciente.dni)
        values.put(COLUMN_SEXO, paciente.genero)
        values.put(COLUMN_FECHA_NACIMIENTO, paciente.fechaNacimiento)
        values.put(COLUMN_LOCALIDAD, paciente.localidad)
        values.put(COLUMN_USER, paciente.usuario)
        values.put(COLUMN_PASSWORD, paciente.password)
        values.put(COLUMN_TRATAMIENTO, paciente.tratamiento)

        db.insert(TABLA_USER, null, values)
    }

    fun findForPacienteId(username : String , password : String ) : Int {
        val query = "SELECT * FROM $TABLA_USER WHERE $COLUMN_USER='$username' and $COLUMN_PASSWORD = '$password'"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var id : Int = -1
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getString(cursor.getColumnIndex(COLUMN_ID)).toInt()
            } while (cursor.moveToNext())
        }
        return id;
    }

    fun checkIfPacienteExistInDB(username : String , password : String ) : Boolean {
        val query = "SELECT * FROM $TABLA_USER WHERE $COLUMN_USER='$username' and $COLUMN_PASSWORD = '$password'"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var found : Boolean = cursor.moveToFirst();
        return found;
    }

    fun findPacienteInLocalDB (username : String , password : String ) : ArrayList<Paciente>{
        val query = "SELECT * FROM $TABLA_USER WHERE $COLUMN_USER='$username' and $COLUMN_PASSWORD = '$password'"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        val listaPaciente = ArrayList<Paciente>()
        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndex(COLUMN_NOMBRE))
                val apellido = cursor.getString(cursor.getColumnIndex(COLUMN_APELLIDO))
                val dni = cursor.getString(cursor.getColumnIndex(COLUMN_DNI))
                val sexo = cursor.getString(cursor.getColumnIndex(COLUMN_SEXO))
                val fecha = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_NACIMIENTO))
                val localidad = cursor.getString(cursor.getColumnIndex(COLUMN_LOCALIDAD))
                val username = cursor.getString(cursor.getColumnIndex(COLUMN_USER))
                val password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD))
                val tratamiento = cursor.getString(cursor.getColumnIndex(COLUMN_TRATAMIENTO))
                val paciente = Paciente(nombre , apellido , dni , sexo , fecha , localidad , username , password , tratamiento)
                listaPaciente.add(paciente)
            } while (cursor.moveToNext())
        }

        return listaPaciente
    }

    fun obtenerPacientes () : ArrayList<Paciente>{
        val query = "SELECT * FROM $TABLA_USER"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        val listaPaciente = ArrayList<Paciente>()
        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndex(COLUMN_NOMBRE))
                val apellido = cursor.getString(cursor.getColumnIndex(COLUMN_APELLIDO))
                val dni = cursor.getString(cursor.getColumnIndex(COLUMN_DNI))
                val sexo = cursor.getString(cursor.getColumnIndex(COLUMN_SEXO))
                val fecha = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_NACIMIENTO))
                val localidad = cursor.getString(cursor.getColumnIndex(COLUMN_LOCALIDAD))
                val username = cursor.getString(cursor.getColumnIndex(COLUMN_USER))
                val password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD))
                val tratamiento = cursor.getString(cursor.getColumnIndex(COLUMN_TRATAMIENTO))
                val paciente = Paciente(nombre , apellido , dni , sexo , fecha , localidad , username , password , tratamiento)
                listaPaciente.add(paciente)
            } while (cursor.moveToNext())
        }

        return listaPaciente
    }
}