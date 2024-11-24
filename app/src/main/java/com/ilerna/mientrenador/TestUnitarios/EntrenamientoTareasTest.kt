package com.ilerna.mientrenador.TestUnitarios
import com.ilerna.mientrenador.ui.data.Entrenamiento
import com.ilerna.mientrenador.ui.data.EstiloNatacion
import com.ilerna.mientrenador.ui.data.Tarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntrenamientoTareasTest {

    @Test
    fun testAsociarTarea() {
        val tarea = Tarea(
            id = "1",
            objetivo = "Resistencia",
            descripcion = "Natación continua",
            metros = 500,
            desarrollo = true,
            corta = false,
            estilos = mutableListOf(EstiloNatacion.CROL)
        )

        val entrenamiento = Entrenamiento(
            id = "1",
            nombre = "Entreno Semana 1",
            numeroTareas = 0,
            metrosTotales = 0,
            tareas = mutableListOf()
        )

        entrenamiento.tareas.add(tarea)

        assertEquals(1, entrenamiento.tareas.size)
        assertTrue(entrenamiento.tareas.contains(tarea))
    }
}
