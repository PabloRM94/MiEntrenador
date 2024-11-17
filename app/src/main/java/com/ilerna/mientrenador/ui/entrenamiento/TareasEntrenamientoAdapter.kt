package com.ilerna.mientrenador.ui.entrenamiento



import android.annotation.SuppressLint
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea

class TareasEntrenamientoAdapter(
    private var tareas: MutableList<Tarea>,
    private val onEditClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit,
) : RecyclerView.Adapter<TareasEntrenamientoAdapter.TareaViewHolder>() {

    // Mantiene un conjunto de tareas seleccionadas
    private val tareasSeleccionadas: MutableSet<Tarea> = mutableSetOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    // Actualizar la lista de tareas en el adaptador
    fun actualizarTareas(nuevaLista: List<Tarea>) {
        tareas = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    // Actualizar una tarea específica
    fun actualizarTarea(tarea: Tarea) {
        val index = tareas.indexOfFirst { it.id == tarea.id }
        if (index != -1) {
            tareas[index] = tarea
            notifyItemChanged(index)
        }
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.bind(tarea, onEditClick, onDeleteClick)

        // Cambiar el fondo del ítem si está seleccionado o no
        holder.itemView.setBackgroundColor(
            if (tareasSeleccionadas.contains(tarea))
                ContextCompat.getColor(holder.itemView.context, R.color.yellow_700)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
        )

        // Controlar la selección del ítem
        holder.itemView.setOnClickListener {
            if (tareasSeleccionadas.contains(tarea)) {
                tareasSeleccionadas.remove(tarea)  // Deseleccionar
            } else {
                tareasSeleccionadas.add(tarea)  // Seleccionar
            }
            notifyItemChanged(position)  // Refrescar el ítem para mostrar cambios de selección
        }
    }

    override fun getItemCount(): Int = tareas.size

    // Devuelve la lista de tareas seleccionadas
    fun getTareasSeleccionadas(): List<Tarea> {
        return tareasSeleccionadas.toList()
    }

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val objetivoTextView: TextView = itemView.findViewById(R.id.textViewObjetivo)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val metrosTextView: TextView = itemView.findViewById(R.id.textViewMetros)
        private val estilosTextView: TextView = itemView.findViewById(R.id.textViewEstilos)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEditar)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonEliminar)

        fun bind(tarea: Tarea, onEditClick: (Tarea) -> Unit, onDeleteClick: (Tarea) -> Unit) {
            objetivoTextView.text = tarea.objetivo
            descripcionTextView.text = tarea.descripcion
            metrosTextView.text = "${tarea.metros} metros"

            estilosTextView.text = tarea.estilos.joinToString(", ") { it.name }

            editButton.setOnClickListener { onEditClick(tarea) }
            deleteButton.setOnClickListener { onDeleteClick(tarea) }
        }
    }
}
