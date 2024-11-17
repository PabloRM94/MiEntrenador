package com.ilerna.mientrenador.ui.data


// Clase que representa al usuario con más campos relacionados con su perfil
data class Usuario(
    val email: String? = null,
    val nombre: String? = null,
    val apellidos: String? = null,
    val edad: Int? = null,
    val club: String? = null,
    val anosNadando: Int? = null,
    val estiloFavorito: String? = null,
    val pruebaFavorita: String? = null,
    val contrasena: String? = null
)

