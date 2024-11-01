package com.ilerna.mientrenador.ui.data

import android.os.Parcel
import android.os.Parcelable

data class Entrenamiento(
    var id: String = "",
    var nombre: String = "",
    var tareas: List<Tarea> = emptyList(),
    var numeroTareas: Int = 0,
    var metrosTotales: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        TODO("tareas"),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nombre)
        parcel.writeInt(numeroTareas)
        parcel.writeInt(metrosTotales)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Entrenamiento> {
        override fun createFromParcel(parcel: Parcel): Entrenamiento {
            return Entrenamiento(parcel)
        }

        override fun newArray(size: Int): Array<Entrenamiento?> {
            return arrayOfNulls(size)
        }
    }
}

