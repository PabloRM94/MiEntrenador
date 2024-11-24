package com.ilerna.mientrenador.TestUnitarios

import com.ilerna.mientrenador.ui.data.Entrenamiento
import com.ilerna.mientrenador.ui.data.EstiloNatacion
import com.ilerna.mientrenador.ui.data.Tarea
import org.junit.Assert.assertEquals
import org.junit.Test

class EntrenamientoTest {

    @Test
    fun testCalcularMetrosTotales() {
        val tarea1 = Tarea(
            id = "1",
            objetivo = "Resistencia",
            descripcion = "Natación continua",
            metros = 500,
            desarrollo = true,
            corta = false,
            estilos = mutableListOf(EstiloNatacion.CROL)
        )
        val tarea2 = Tarea(
            id = "2",
            objetivo = "Técnica",
            descripcion = "Espalda técnica",
            metros = 300,
            desarrollo = true,
            corta = false,
            estilos = mutableListOf(EstiloNatacion.ESPALDA)
        )

        val entrenamiento = Entrenamiento(
            id = "1",
            nombre = "Entreno Semana 1",
            numeroTareas = 2,
            metrosTotales = 0,
            tareas = mutableListOf(tarea1, tarea2)
        )

        val totalMetros = entrenamiento.tareas.sumOf { it.metros }
        assertEquals(800, totalMetros)
    }
}