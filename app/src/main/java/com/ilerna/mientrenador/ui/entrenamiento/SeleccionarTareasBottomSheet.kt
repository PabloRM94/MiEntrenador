package com.ilerna.mientrenador.ui.entrenamiento

import android.content.Context
import android.widget.Button
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea
import androidx.appcompat.widget.SearchView

class SeleccionarTareasBottomSheet(private val onTareasSeleccionadas: (List<Tarea>) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasAdapter: TareasEntrenamientoAdapter
    private var tareasList: MutableList<Tarea> = mutableListOf()
    private var todasLasTareas: List<Tarea> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_seleccionar_tareas_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTareasExistentes)
        val searchView = view.findViewById<SearchView>(R.id.searchViewTareas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        tareasAdapter = TareasEntrenamientoAdapter(mutableListOf(), ::editarTarea, ::eliminarTarea, ::actualizarMetrosTotales, requireContext())
        recyclerView.adapter = tareasAdapter

        // Cargar tareas desde Firestore
        firestore.collection("tareas").get().addOnSuccessListener { result ->
            todasLasTareas = result.toObjects(Tarea::class.java)
            tareasAdapter.actualizarTareas(todasLasTareas.toMutableList())
        }

        // Configurar búsqueda en tiempo real
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { buscarTareas(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { buscarTareas(it) }
                return false
            }
        })

        view.findViewById<Button>(R.id.buttonConfirmarSeleccion).setOnClickListener {
            val tareasSeleccionadas = tareasAdapter.getTareasSeleccionadas()
            onTareasSeleccionadas(tareasSeleccionadas)
            dismiss() // Cierra el BottomSheet
        }
    }

    private fun buscarTareas(termino: String) {
        val terminoLowerCase = termino.lowercase()
        val tareasFiltradas = todasLasTareas.filter { tarea ->
            tarea.objetivo.lowercase().contains(terminoLowerCase) || tarea.descripcion.lowercase().contains(terminoLowerCase)
        }
        tareasAdapter.actualizarTareas(tareasFiltradas.toMutableList())
    }
    // Editar una tarea existente
    private fun editarTarea(tarea: Tarea) {
        mostrarDialogoTarea(tarea) { tareaEditada ->
            val index = tareasList.indexOf(tarea)
            if (index != -1) {
                tareasList[index] = tareaEditada
                tareasAdapter.notifyDataSetChanged() // Forzar actualización inmediata
                actualizarMetrosTotales(requireContext())
            }
        }
    }
    // Mostrar un diálogo reutilizable para agregar/editar una tarea
    private fun mostrarDialogoTarea(tarea: Tarea?, onGuardar: (Tarea) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        val objetivoEditText = dialogView.findViewById<EditText>(R.id.editTextObjetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.editTextMetros)

        tarea?.let {
            objetivoEditText.setText(it.objetivo)
            descripcionEditText.setText(it.descripcion)
            metrosEditText.setText(it.metros.toString())
        }

        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()
            val metros = metrosEditText.text.toString().toIntOrNull() ?: 0

            if (objetivo.isNotEmpty() && descripcion.isNotEmpty()) {
                onGuardar(Tarea(objetivo = objetivo, descripcion = descripcion, metros = metros))
            } else {
                Toast.makeText(requireContext(), "Los campos de tarea son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    // Eliminar una tarea
    private fun eliminarTarea(tarea: Tarea) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Sí") { _, _ ->
                tareasList.remove(tarea)
                tareasAdapter.actualizarTareas(tareasList)
                actualizarMetrosTotales(requireContext())
                Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }
    // Actualizar metros totales
    private fun actualizarMetrosTotales(context: Context) {
        val totalMetros = tareasList.sumOf { it.metros }
        val textViewSumaTotalMetros = view?.findViewById<TextView>(R.id.textViewSumaTotalMetros)
        textViewSumaTotalMetros?.text = context.getString(R.string.total_metros, totalMetros)
    }
}
