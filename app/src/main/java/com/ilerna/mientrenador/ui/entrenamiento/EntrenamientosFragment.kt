package com.ilerna.mientrenador.ui.entrenamiento


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SearchView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Entrenamiento
import com.ilerna.mientrenador.ui.data.EstiloNatacion
import com.ilerna.mientrenador.ui.data.Tarea
import com.ilerna.mientrenador.utils.TareasUtils

import java.util.UUID

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
            mostrarOpcionesTareas()
        }

        view.findViewById<Button>(R.id.buttonGuardarEntrenamiento).setOnClickListener {
            guardarEntrenamiento()
        }
    }

    // Configurar los RecyclerViews para Tareas y Entrenamientos
    private fun setupRecyclerViews(view: View) {
        val recyclerViewTareas = view.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        recyclerViewTareas.layoutManager = LinearLayoutManager(requireContext())
        tareasAdapter = TareasEntrenamientoAdapter(tareasList, ::editarTarea, ::eliminarTarea)
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
    // Mostrar las opciones: seleccionar tareas existentes o agregar nuevas tareas
    private fun mostrarOpcionesTareas() {
        val opciones = arrayOf("Seleccionar tareas existentes", "Crear nueva tarea")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones de tareas")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoSeleccionarTareas()  // Seleccionar tareas de Firestore
                    1 -> mostrarDialogoAgregarTarea()       // Crear una nueva tarea
                }
            }
            .show()
    }
    @SuppressLint("MissingInflatedId")
    private fun mostrarDialogoSeleccionarTareas() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_seleccionar_tareas, null)
        builder.setView(dialogView)

        val recyclerViewTareasExistentes = dialogView.findViewById<RecyclerView>(R.id.recyclerViewTareasEntrenamiento)
        recyclerViewTareasExistentes.layoutManager = LinearLayoutManager(requireContext())

        val searchViewTareas = dialogView.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchViewTareas)
        val tareasExistentesAdapter = TareasEntrenamientoAdapter(mutableListOf(), ::editarTarea, ::eliminarTarea)
        recyclerViewTareasExistentes.adapter = tareasExistentesAdapter

        // Cargar las tareas existentes desde Firestore
        firestore.collection("tareas").get().addOnSuccessListener { result ->
            val tareasExistentes = result.toObjects(Tarea::class.java)
            tareasExistentesAdapter.actualizarTareas(tareasExistentes.toMutableList())

            // Configurar búsqueda en tiempo real
            searchViewTareas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        tareasExistentesAdapter.actualizarTareas(TareasUtils.filtrarTareas(tareasExistentes, it))
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    tareasExistentesAdapter.actualizarTareas(
                        if (newText.isNullOrBlank()) tareasExistentes
                        else TareasUtils.filtrarTareas(tareasExistentes, newText)
                    )
                    return false
                }
            })
        }

        builder.setPositiveButton("Añadir") { _, _ ->
            // Añadir las tareas seleccionadas al entrenamiento actual
            val tareasSeleccionadas = tareasExistentesAdapter.getTareasSeleccionadas()
            tareasList.addAll(tareasSeleccionadas)
            tareasAdapter.actualizarTareas(tareasList)
            actualizarMetrosTotales(requireContext())
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    // Mostrar un diálogo reutilizable para agregar/editar una tarea
    private fun mostrarDialogoTarea(tarea: Tarea?, onGuardar: (Tarea) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        val objetivoEditText = dialogView.findViewById<EditText>(R.id.editTextObjetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.editTextMetros)

        // Checkboxes para los estilos de natación
        val checkboxCrol = dialogView.findViewById<CheckBox>(R.id.checkboxCrol)
        val checkboxEspalda = dialogView.findViewById<CheckBox>(R.id.checkboxEspalda)
        val checkboxBraza = dialogView.findViewById<CheckBox>(R.id.checkboxBraza)
        val checkboxMariposa = dialogView.findViewById<CheckBox>(R.id.checkboxMariposa)

        // Si estamos editando una tarea existente, llenar los campos y preseleccionar los estilos
        tarea?.let {
            objetivoEditText.setText(it.objetivo)
            descripcionEditText.setText(it.descripcion)
            metrosEditText.setText(it.metros.toString())

            checkboxCrol.isChecked = it.estilos.contains(EstiloNatacion.CROL)
            checkboxEspalda.isChecked = it.estilos.contains(EstiloNatacion.ESPALDA)
            checkboxBraza.isChecked = it.estilos.contains(EstiloNatacion.BRAZA)
            checkboxMariposa.isChecked = it.estilos.contains(EstiloNatacion.MARIPOSA)
        }

        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()
            val metros = metrosEditText.text.toString().toIntOrNull() ?: 0

            if (objetivo.isNotEmpty() && descripcion.isNotEmpty()) {
                // Obtener los estilos seleccionados
                val estilosSeleccionados = mutableListOf<EstiloNatacion>()
                if (checkboxCrol.isChecked) estilosSeleccionados.add(EstiloNatacion.CROL)
                if (checkboxEspalda.isChecked) estilosSeleccionados.add(EstiloNatacion.ESPALDA)
                if (checkboxBraza.isChecked) estilosSeleccionados.add(EstiloNatacion.BRAZA)
                if (checkboxMariposa.isChecked) estilosSeleccionados.add(EstiloNatacion.MARIPOSA)

                // Crear o actualizar la tarea con los estilos seleccionados
                onGuardar(
                    Tarea(
                        id = tarea?.id ?: UUID.randomUUID().toString(),
                        objetivo = objetivo,
                        descripcion = descripcion,
                        metros = metros,
                        estilos = estilosSeleccionados
                    )
                )
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
                tareasAdapter.actualizarTarea(tareaEditada)
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
        // Configurar el entrenamiento actual con el seleccionado
        currentEntrenamiento = entrenamiento

        // Establecer el nombre del entrenamiento en el EditText
        val editTextNombreEntrenamiento = view?.findViewById<EditText>(R.id.editTextNumeroEntrenamiento)
        editTextNombreEntrenamiento?.setText(entrenamiento.nombre)

        // Actualizar la lista de tareas en el RecyclerView de tareas
        tareasList = entrenamiento.tareas.toMutableList()
        tareasAdapter.actualizarTareas(tareasList)
        actualizarMetrosTotales(requireContext())
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

