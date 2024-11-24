package com.ilerna.mientrenador.ui.entrenamiento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Entrenamiento

class EntrenamientosAdapter(
    private var entrenamientos: List<Entrenamiento>,
    private val onEditClick: (Entrenamiento) -> Unit,
    private val onDeleteClick: (Entrenamiento) -> Unit
) : RecyclerView.Adapter<EntrenamientosAdapter.EntrenamientoViewHolder>() {

    // Clase para el adaptador de Entrenamientos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntrenamientoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entrenamiento, parent, false)
        return EntrenamientoViewHolder(view)
    }

    // Crear un nuevo ViewHolder
    override fun onBindViewHolder(holder: EntrenamientoViewHolder, position: Int) {
        val entrenamiento = entrenamientos[position]
        holder.bind(entrenamiento, onEditClick, onDeleteClick)
    }

    // Obtener el número de elementos en la lista
    override fun getItemCount(): Int {
        return entrenamientos.size
    }

    // Actualizar la lista de entrenamientos
    fun actualizarEntrenamientos(nuevosEntrenamientos: List<Entrenamiento>) {
        entrenamientos = nuevosEntrenamientos
        notifyDataSetChanged()
    }

    // Clase para el ViewHolder de Entrenamiento
    class EntrenamientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombreEntrenamiento)
        private val numeroTareasTextView: TextView = itemView.findViewById(R.id.textViewNumeroTareas)
        private val metrosTotalesTextView: TextView = itemView.findViewById(R.id.textViewMetrosTotales)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEditarEntrenamiento)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonEliminarEntrenamiento)

        fun bind(
            entrenamiento: Entrenamiento,
            onEditClick: (Entrenamiento) -> Unit,
            onDeleteClick: (Entrenamiento) -> Unit
        ) {
            nombreTextView.text = entrenamiento.nombre
            numeroTareasTextView.text = "${entrenamiento.numeroTareas} tareas"
            metrosTotalesTextView.text = "${entrenamiento.metrosTotales} metros"

            editButton.setOnClickListener { onEditClick(entrenamiento) }
            deleteButton.setOnClickListener { onDeleteClick(entrenamiento) }
        }
    }
}
