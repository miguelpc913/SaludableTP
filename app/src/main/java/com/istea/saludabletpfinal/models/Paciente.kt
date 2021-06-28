package com.istea.saludabletpfinal.models

import java.io.Serializable


data class Paciente(val nombre : String , val apellido : String , val dni : String , val genero :String , val fechaNacimiento : String , val localidad : String,
                    val usuario : String ,  val password : String, val tratamiento : String): Serializable