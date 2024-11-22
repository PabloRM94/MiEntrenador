package com.ilerna.mientrenador.pruebasUnitarias

import com.ilerna.mientrenador.ui.data.EstiloNatacion
import com.ilerna.mientrenador.ui.data.Tarea
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class TareaTest {

    @Test
    fun testAgregarEstilos() {
        val tarea = Tarea(
            id = "1",
            objetivo = "Técnica",
            descripcion = "Mejorar mariposa",
            metros = 200,
            desarrollo = true,
            corta = false,
            estilos = mutableListOf()
        )

        // Agregar estilos
        tarea.estilos.add(EstiloNatacion.MARIPOSA)
        tarea.estilos.add(EstiloNatacion.CROL)

        assertEquals(2, tarea.estilos.size)
        assertTrue(tarea.estilos.contains(EstiloNatacion.MARIPOSA))
    }
}
