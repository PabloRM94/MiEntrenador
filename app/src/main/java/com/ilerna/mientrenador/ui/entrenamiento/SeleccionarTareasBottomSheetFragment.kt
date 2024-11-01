package com.ilerna.mientrenador.ui.entrenamiento

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.appcompat.widget.SearchView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea

class SeleccionarTareasBottomSheetFragment(
    private val onTareasSeleccionadas: (List<Tarea>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasExistentesAdapter: TareasEntrenamientoAdapter
    private var todasLasTareas: List<Tarea> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seleccionar_tareas, container, false)

        val recyclerViewTareas = view.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        val searchViewTareas = view.findViewById<SearchView>(R.id.searchViewTareas)

        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())
        tareasExistentesAdapter = TareasEntrenamientoAdapter(mutableListOf(), ::onTareaSelected, {}, {}, requireContext())
        recyclerViewTareas.adapter = tareasExistentesAdapter

        // Cargar las tareas existentes desde Firestore
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("tareas").get().addOnSuccessListener { result ->
            todasLasTareas = result.toObjects(Tarea::class.java)
            tareasExistentesAdapter.actualizarTareas(todasLasTareas.toMutableList())
        }

        // Configurar el SearchView para buscar en tiempo real
        searchViewTareas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { buscarTareas(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { buscarTareas(it) }
                return false
            }
        })

        return view
    }

    private fun onTareaSelected(tarea: Tarea) {
        // Aquí puedes definir cualquier acción cuando se selecciona una tarea
    }

    // Función para buscar las tareas por el término de búsqueda (insensible a mayúsculas/minúsculas)
    private fun buscarTareas(termino: String) {
        val terminoLowerCase = termino.lowercase()

        // Filtrar las tareas en base al término de búsqueda (en campos objetivo, descripcion y metros)
        val tareasFiltradas = todasLasTareas.filter { tarea ->
            tarea.objetivo.lowercase().contains(terminoLowerCase) ||
                    tarea.descripcion.lowercase().contains(terminoLowerCase) ||
                    tarea.metros.toString().contains(termino)
        }

        // Actualizar el adaptador con las tareas filtradas
        tareasExistentesAdapter.actualizarTareas(tareasFiltradas.toMutableList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cuando se cierra el BottomSheet, devolver las tareas seleccionadas
        onTareasSeleccionadas(tareasExistentesAdapter.getTareasSeleccionadas())
    }
}
