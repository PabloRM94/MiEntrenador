package com.ilerna.mientrenador.ui.entrenamiento


import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Entrenamiento
import com.ilerna.mientrenador.ui.data.Tarea

class EntrenamientosFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasAdapter: TareasEntrenamientoAdapter
    private lateinit var entrenamientosAdapter: EntrenamientosAdapter
    private var tareasList: MutableList<Tarea> = mutableListOf()
    private var entrenamientosList: MutableList<Entrenamiento> = mutableListOf()
    private var currentEntrenamiento: Entrenamiento = Entrenamiento()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entrenamientos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerViews(view)
        cargarEntrenamientos()

        view.findViewById<Button>(R.id.buttonAgregarTarea).setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        view.findViewById<Button>(R.id.buttonGuardarEntrenamiento).setOnClickListener {
            guardarEntrenamiento()
        }
    }

    // Configurar los RecyclerViews para Tareas y Entrenamientos
    private fun setupRecyclerViews(view: View) {
        val recyclerViewTareas = view.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())
        tareasAdapter = TareasEntrenamientoAdapter(tareasList, ::editarTarea, ::eliminarTarea, ::actualizarMetrosTotales, requireContext())
        recyclerViewTareas.adapter = tareasAdapter

        val recyclerViewEntrenamientos = view.findViewById<RecyclerView>(R.id.recyclerViewEntrenamientos)
        recyclerViewEntrenamientos.layoutManager = LinearLayoutManager(requireContext())
        entrenamientosAdapter = EntrenamientosAdapter(entrenamientosList, ::editarEntrenamiento, ::eliminarEntrenamiento)
        recyclerViewEntrenamientos.adapter = entrenamientosAdapter
    }

    // Mostrar un diálogo para agregar una nueva tarea
    private fun mostrarDialogoAgregarTarea() {
        mostrarDialogoTarea(null) { nuevaTarea ->
            tareasList.add(nuevaTarea)
            tareasAdapter.actualizarTareas(tareasList)
            actualizarMetrosTotales(requireContext())
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

    // Editar una tarea existente
    private fun editarTarea(tarea: Tarea) {
        mostrarDialogoTarea(tarea) { tareaEditada ->
            val index = tareasList.indexOf(tarea)
            if (index != -1) {
                tareasList[index] = tareaEditada
                tareasAdapter.notifyDataSetChanged()
                actualizarMetrosTotales(requireContext())
            }
        }
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

    // Guardar el entrenamiento con las tareas en Firestore
    private fun guardarEntrenamiento() {
        val nombreEntrenamiento = view?.findViewById<EditText>(R.id.editTextNumeroEntrenamiento)?.text.toString().trim()

        if (nombreEntrenamiento.isNotEmpty()) {
            currentEntrenamiento.nombre = nombreEntrenamiento
            currentEntrenamiento.tareas = tareasList
            currentEntrenamiento.numeroTareas = tareasList.size
            currentEntrenamiento.metrosTotales = tareasList.sumOf { it.metros }

            val entrenamientoId = currentEntrenamiento.id.takeIf { it.isNotEmpty() } ?: firestore.collection("entrenamientos").document().id
            currentEntrenamiento.id = entrenamientoId

            firestore.collection("entrenamientos").document(entrenamientoId)
                .set(currentEntrenamiento)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Entrenamiento guardado con éxito", Toast.LENGTH_SHORT).show()
                    cargarEntrenamientos()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al guardar el entrenamiento", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "El nombre del entrenamiento no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    // Editar un entrenamiento
    private fun editarEntrenamiento(entrenamiento: Entrenamiento) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_entrenamiento, null)
        builder.setView(dialogView)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.editTextNombreEntrenamiento)
        val recyclerViewTareas = dialogView.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        val agregarTareaButton = dialogView.findViewById<Button>(R.id.buttonAgregarTarea)

        val tareasMutableList = entrenamiento.tareas.toMutableList()

        val tareasAdapter = TareasEntrenamientoAdapter(tareasMutableList, ::editarTarea, { tarea ->
            tareasMutableList.remove(tarea)
            tareasAdapter.actualizarTareas(tareasMutableList)
            actualizarMetrosTotales(requireContext())
            entrenamiento.tareas = tareasMutableList.toMutableList()
        }, ::actualizarMetrosTotales, requireContext())

        nombreEditText.setText(entrenamiento.nombre)
        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewTareas.adapter = tareasAdapter

        agregarTareaButton.setOnClickListener {
            mostrarDialogoAgregarTareaParaEditar(entrenamiento, tareasAdapter)
        }

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = nombreEditText.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                entrenamiento.nombre = nuevoNombre
                entrenamiento.tareas = tareasAdapter.getTareas().toMutableList()
                entrenamiento.numeroTareas = entrenamiento.tareas.size
                entrenamiento.metrosTotales = entrenamiento.tareas.sumOf { it.metros }

                val entrenamientoId = entrenamiento.id.takeIf { it.isNotEmpty() } ?: firestore.collection("entrenamientos").document().id
                entrenamiento.id = entrenamientoId

                firestore.collection("entrenamientos").document(entrenamiento.id)
                    .set(entrenamiento)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Entrenamiento actualizado", Toast.LENGTH_SHORT).show()
                        cargarEntrenamientos()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al actualizar el entrenamiento", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "El nombre del entrenamiento no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    // Mostrar diálogo para agregar tareas durante la edición
    private fun mostrarDialogoAgregarTareaParaEditar(entrenamiento: Entrenamiento, tareasAdapter: TareasEntrenamientoAdapter) {
        mostrarDialogoTarea(null) { nuevaTarea ->
            entrenamiento.tareas.add(nuevaTarea)
            tareasAdapter.actualizarTareas(entrenamiento.tareas)
        }
    }

    // Eliminar un entrenamiento
    private fun eliminarEntrenamiento(entrenamiento: Entrenamiento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Entrenamiento")
            .setMessage("¿Estás seguro de que deseas eliminar este entrenamiento?")
            .setPositiveButton("Sí") { _, _ ->
                firestore.collection("entrenamientos").document(entrenamiento.id).delete()
                    .addOnSuccessListener {
                        entrenamientosList.remove(entrenamiento)
                        entrenamientosAdapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Entrenamiento eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar el entrenamiento", Toast.LENGTH_SHORT).show()
                    }
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

    // Cargar entrenamientos desde Firestore
    private fun cargarEntrenamientos() {
        firestore.collection("entrenamientos").get()
            .addOnSuccessListener { result ->
                entrenamientosList = result.toObjects(Entrenamiento::class.java)
                entrenamientosAdapter.actualizarEntrenamientos(entrenamientosList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar entrenamientos", Toast.LENGTH_SHORT).show()
            }
    }
}

