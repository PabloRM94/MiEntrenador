// TareasUtils.kt
package com.ilerna.mientrenador.utils

import com.ilerna.mientrenador.ui.data.Tarea

object TareasUtils {
    fun filtrarTareas(tareas: List<Tarea>, termino: String): List<Tarea> {
        val terminoLowerCase = termino.lowercase()
        return tareas.filter { tarea ->
            tarea.objetivo.lowercase().contains(terminoLowerCase) ||
                    tarea.descripcion.lowercase().contains(terminoLowerCase) ||
                    tarea.metros.toString().contains(termino)
                    tarea.estilos.any { estilo -> estilo.name.lowercase().contains(terminoLowerCase)}
        }
    }
}
