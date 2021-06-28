package com.istea.saludabletpfinal.models
import java.io.Serializable


data class Meal (val Tipo : String , val PlatoPrincipal : String , val PlatoSecundario : String , val Bebida : String , val Postre : String , val Tentacion : String ,
                 val Hambre : Boolean , val Fecha : String ) : Serializable

