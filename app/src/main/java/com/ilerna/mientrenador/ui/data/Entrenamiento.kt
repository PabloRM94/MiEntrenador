package com.ilerna.mientrenador.ui.data

import java.util.UUID

data class Entrenamiento(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var numeroTareas: Int = 0,
    var metrosTotales: Int = 0,
    var tareas: MutableList<Tarea> = mutableListOf()  // Lista de tareas asociadas al entrenamiento
)

