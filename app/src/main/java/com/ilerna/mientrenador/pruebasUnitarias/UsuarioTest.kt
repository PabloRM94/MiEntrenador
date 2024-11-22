package com.ilerna.mientrenador.pruebasUnitarias


import com.ilerna.mientrenador.ui.data.Usuario
import org.junit.Assert.assertEquals
import org.junit.Test

class UsuarioTest {

    @Test
    fun testUsuarioNombreCompleto() {
        val usuario = Usuario(
            email = "test@test.com",
            nombre = "Juan",
            apellidos = "Pérez",
            edad = 30,
            club = "Club Natación",
            anosNadando = 5,
            estiloFavorito = "Crol",
            pruebaFavorita = "100m Libre"
        )
        assertEquals("Juan Pérez", "${usuario.nombre} ${usuario.apellidos}")
    }
}
