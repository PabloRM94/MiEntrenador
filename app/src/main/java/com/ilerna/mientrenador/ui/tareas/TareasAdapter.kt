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

    // Función para filtrar las tareas por objetivo
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    // Funcion para mostrar la tarea
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.bind(tarea, onEditClick, onDeleteClick)
    }

    //  Funcion para mostrar el numero de tareas
    override fun getItemCount(): Int {
        return tareas.size
    }

    // clase para vincular los datos de la tarea con los elementos de la vista
    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val Text_Objetivo: TextView = itemView.findViewById(R.id.Text_Objetivo)
        private val Text_Descripcion: TextView = itemView.findViewById(R.id.Text_Descripcion)
        private val Text_Metros: TextView = itemView.findViewById(R.id.Text_Metros)
        private val Text_Estilos: TextView = itemView.findViewById(R.id.Text_Estilos)
        private val Bttn_Editar: ImageButton = itemView.findViewById(R.id.Bttn_Editar)
        private val Bttn_Eliminar: ImageButton = itemView.findViewById(R.id.Bttn_Eliminar)

        fun bind(tarea: Tarea, onEditClick: (Tarea) -> Unit, onDeleteClick: (Tarea) -> Unit) {
            Text_Objetivo.text = tarea.objetivo
            Text_Descripcion.text = tarea.descripcion
            Text_Metros.text = "${tarea.metros} metros"

            // Mostrar los estilos seleccionados como una cadena de texto
            Text_Estilos.text = tarea.estilos.joinToString(", ") { it.name }

            Bttn_Editar.setOnClickListener { onEditClick(tarea) }
            Bttn_Eliminar.setOnClickListener { onDeleteClick(tarea) }
        }
    }

}

