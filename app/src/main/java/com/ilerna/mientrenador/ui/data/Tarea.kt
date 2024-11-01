package com.ilerna.mientrenador.ui.data


import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Tarea(
    var id: String = "",
    var objetivo: String = "",
    var descripcion: String = "",
    var metros: Int = 0,
    var desarrollo: Boolean = false,
    var corta: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(objetivo)
        parcel.writeString(descripcion)
        parcel.writeInt(metros)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tarea> {
        override fun createFromParcel(parcel: Parcel): Tarea {
            return Tarea(parcel)
        }

        override fun newArray(size: Int): Array<Tarea?> {
            return arrayOfNulls(size)
        }
    }
}

annotation class Parcelize

