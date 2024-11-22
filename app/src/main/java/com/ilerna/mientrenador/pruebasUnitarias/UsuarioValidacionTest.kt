package com.ilerna.mientrenador.pruebasUnitarias

import com.ilerna.mientrenador.ui.data.Usuario
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioValidacionTest {

    @Test
    fun testDatosCompletos() {
        val usuario = Usuario(
            email = "test@test.com",
            nombre = "Luis",
            apellidos = "Martínez",
            edad = 25,
            club = "Club Avanzados",
            anosNadando = 7,
            estiloFavorito = "Crol",
            pruebaFavorita = "200m Libre"
        )

        assertNotNull(usuario.email)
        assertNotNull(usuario.nombre)
        assertNotNull(usuario.apellidos)
        assertTrue(usuario.edad!! > 0)
        assertNotNull(usuario.club)
    }
}
