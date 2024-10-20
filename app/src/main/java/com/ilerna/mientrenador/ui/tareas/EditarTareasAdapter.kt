package com.ilerna.mientrenador.ui.tareas


import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea
class EditarTareasAdapter(
    private var tareas: List<Tarea>,
    private val onEditClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit
) : RecyclerView.Adapter<EditarTareasAdapter.EditarTareaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditarTareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return EditarTareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditarTareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.bind(tarea, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int {
        return tareas.size
    }

    class EditarTareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val objetivoTextView: TextView = itemView.findViewById(R.id.textViewObjetivo)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val metrosTextView: TextView = itemView.findViewById(R.id.textViewMetros)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEditar)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonEliminar)

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

