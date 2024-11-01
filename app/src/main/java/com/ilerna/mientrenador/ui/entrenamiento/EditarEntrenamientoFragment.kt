package com.ilerna.mientrenador.ui.entrenamiento

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Entrenamiento
import com.ilerna.mientrenador.ui.data.Tarea

class EditarEntrenamientoFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasAdapter: TareasEntrenamientoAdapter
    private lateinit var entrenamiento: Entrenamiento
    private lateinit var tareasList: MutableList<Tarea>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_entrenamiento, container, false)

        firestore = FirebaseFirestore.getInstance()

        val nombreEditText = view.findViewById<EditText>(R.id.editTextNombreEntrenamiento)
        val recyclerViewTareas = view.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        val agregarTareaButton = view.findViewById<Button>(R.id.buttonAgregarTarea)

        // Recuperar el entrenamiento desde los argumentos
        entrenamiento = arguments?.getParcelable("entrenamiento") ?: Entrenamiento()
        tareasList = entrenamiento.tareas.toMutableList()

        nombreEditText.setText(entrenamiento.nombre)

        // Configurar RecyclerView
        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())
        tareasAdapter = TareasEntrenamientoAdapter(tareasList, ::editarTarea, ::eliminarTarea, ::actualizarMetrosTotales, requireContext())
        recyclerViewTareas.adapter = tareasAdapter

        agregarTareaButton.setOnClickListener {
            mostrarBottomSheetSeleccionarTareas()
        }

        view.findViewById<Button>(R.id.buttonGuardarEntrenamiento).setOnClickListener {
            guardarEntrenamiento(nombreEditText.text.toString())
        }

        return view
    }

    private fun editarTarea(tarea: Tarea) {
        // Función para editar una tarea
    }

    private fun eliminarTarea(tarea: Tarea) {
        tareasList.remove(tarea)
        tareasAdapter.actualizarTareas(tareasList)
        actualizarMetrosTotales(requireContext())
    }

    private fun mostrarBottomSheetSeleccionarTareas() {
        val bottomSheet = SeleccionarTareasBottomSheetFragment { tareasSeleccionadas ->
            tareasList.addAll(tareasSeleccionadas)
            tareasAdapter.actualizarTareas(tareasList)
            actualizarMetrosTotales(requireContext())
        }
        bottomSheet.show(parentFragmentManager, "SeleccionarTareas")
    }

    private fun guardarEntrenamiento(nuevoNombre: String) {
        if (nuevoNombre.isNotEmpty()) {
            entrenamiento.nombre = nuevoNombre
            entrenamiento.tareas = tareasList
            entrenamiento.numeroTareas = tareasList.size
            entrenamiento.metrosTotales = tareasList.sumOf { it.metros }

            firestore.collection("entrenamientos").document(entrenamiento.id)
                .set(entrenamiento)
                .addOnSuccessListener {
                    // Navegar de regreso al fragment de entrenamientos
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    // Manejar error al guardar
                }
        } else {
            // Mostrar mensaje de error
        }
    }

    private fun actualizarMetrosTotales(context: Context) {
        // Actualizar el total de metros en la UI si es necesario
    }
}
