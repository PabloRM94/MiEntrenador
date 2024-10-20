package com.ilerna.mientrenador.ui.entrenamiento


import android.content.Context
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea


class TareasEntrenamientoAdapter(
    private var tareas: MutableList<Tarea>,
    private val onEditClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit,
    private val actualizarMetrosTotales: (Context) -> Unit,
    private val contexto: Context
) : RecyclerView.Adapter<TareasEntrenamientoAdapter.TareaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.bind(tarea, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = tareas.size

    // Actualizar la lista de tareas en el adaptador
    fun actualizarTareas(nuevaLista: MutableList<Tarea>) {
        tareas = nuevaLista
        notifyDataSetChanged()
    }

    // Eliminar tarea con confirmación
    fun eliminarTarea(tarea: Tarea) {
        val builder = AlertDialog.Builder(contexto)
        builder.setTitle("Eliminar Tarea")
        builder.setMessage("¿Estás seguro de que deseas eliminar esta tarea?")

        builder.setPositiveButton("Sí") { _, _ ->
            // Elimina la tarea y actualiza el adaptador
            tareas.remove(tarea)
            notifyDataSetChanged()
            actualizarMetrosTotales(contexto)
            Toast.makeText(contexto, "Tarea eliminada", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No", null)
        builder.create().show()
    }

    fun getTareas(): MutableList<Tarea> {
        return tareas
    }

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val objetivoTextView: TextView = itemView.findViewById(R.id.textViewObjetivo)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val metrosTextView: TextView = itemView.findViewById(R.id.textViewMetros)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEditar)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonEliminar)

        fun bind(tarea: Tarea, onEditClick: (Tarea) -> Unit, onDeleteClick: (Tarea) -> Unit) {
            objetivoTextView.text = tarea.objetivo
            descripcionTextView.text = tarea.descripcion
            metrosTextView.text = "${tarea.metros} metros"
            editButton.setOnClickListener { onEditClick(tarea) }
            deleteButton.setOnClickListener { onDeleteClick(tarea) }
        }
    }
}


