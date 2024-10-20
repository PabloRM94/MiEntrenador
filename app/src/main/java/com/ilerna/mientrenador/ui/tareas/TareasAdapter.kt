package com.ilerna.mientrenador.ui.tareas


import android.view.*
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea


class TareasAdapter(
    private var tareas: List<Tarea>,
    private val onEditClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit
) : RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {

    // Función para actualizar la lista de tareas y notificar al adaptador
    fun actualizarTareas(nuevasTareas: List<Tarea>) {
        tareas = nuevasTareas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.bind(tarea, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int {
        return tareas.size
    }

    // ViewHolder que contiene la lógica para mostrar y gestionar cada tarea
    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a los elementos del layout (objetivo, descripción, metros, botones)
        private val objetivoTextView: TextView = itemView.findViewById(R.id.textViewObjetivo)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val metrosTextView: TextView = itemView.findViewById(R.id.textViewMetros)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEditar)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonEliminar)

        // Función para enlazar la tarea con los datos del layout y asignar los eventos de clic
        fun bind(tarea: Tarea, onEditClick: (Tarea) -> Unit, onDeleteClick: (Tarea) -> Unit) {
            // Mostrar los datos de la tarea
            objetivoTextView.text = tarea.objetivo
            descripcionTextView.text = tarea.descripcion
            metrosTextView.text = "${tarea.metros} metros"

            // Asignar los clics a los botones de editar y eliminar
            editButton.setOnClickListener { onEditClick(tarea) }
            deleteButton.setOnClickListener { onDeleteClick(tarea) }
        }
    }
}

