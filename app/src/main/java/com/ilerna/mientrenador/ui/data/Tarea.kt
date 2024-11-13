package com.ilerna.mientrenador.ui.data

import java.util.UUID

data class Tarea(
    var id: String = UUID.randomUUID().toString(),
    var objetivo: String = "",
    var descripcion: String = "",
    var metros: Int = 0,
    var desarrollo: Boolean = false,
    var corta: Boolean = false,
    var estilos: MutableList<EstiloNatacion> = mutableListOf()
)

