package com.ilerna.mientrenador.ui.tareas

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.firestore.*
import androidx.appcompat.widget.SearchView
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea

class TareasFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasList: RecyclerView
    private lateinit var agregarTareaButton: Button
    private lateinit var tareasAdapter: TareasAdapter
    private lateinit var searchViewTareas: SearchView
    private var todasLasTareas: List<Tarea> = emptyList()  // Lista para almacenar todas las tareas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tareas, container, false)

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Vincular elementos del layout
        tareasList = view.findViewById(R.id.recyclerViewTareas)
        agregarTareaButton = view.findViewById(R.id.buttonAgregarTarea)
        searchViewTareas = view.findViewById(R.id.searchViewTareas)

        // Configurar RecyclerView
        tareasList.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador con los métodos para editar y eliminar tareas
        tareasAdapter = TareasAdapter(emptyList(),
            { tarea -> editarTarea(tarea) },  // Función para editar
            { tarea -> eliminarTarea(tarea) }  // Función para eliminar
        )
        tareasList.adapter = tareasAdapter

        // Cargar las tareas desde Firestore
        cargarTareas()

        // Botón para agregar nueva tarea
        agregarTareaButton.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }


        // Configurar el SearchView para buscar en tiempo real
        searchViewTareas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    buscarTareas(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // Si el término está vacío, restauramos todas las tareas
                    tareasAdapter.actualizarTareas(todasLasTareas)
                } else {
                    buscarTareas(newText)
                }
                return false
            }
        })

        return view
    }

    // Función para buscar las tareas por el término de búsqueda (insensible a mayúsculas/minúsculas)
    fun buscarTareas(termino: String) {
        val terminoLowerCase = termino.lowercase()

        // Filtrar las tareas en base al término de búsqueda (en campos objetivo, descripcion y metros)
        val tareasFiltradas = todasLasTareas.filter { tarea ->
            tarea.objetivo.lowercase().contains(terminoLowerCase) ||  // Comparar objetivo ignorando mayúsculas
                    tarea.descripcion.lowercase().contains(terminoLowerCase) ||  // Comparar descripcion ignorando mayúsculas
                    tarea.metros.toString().contains(termino)  // Para el campo metros, buscamos la coincidencia exacta del número
        }

        // Actualizar el adaptador con las tareas filtradas
        tareasAdapter.actualizarTareas(tareasFiltradas)
    }

    // Función para cargar todas las tareas desde Firestore y almacenarlas en `todasLasTareas`
    private fun cargarTareas() {
        firestore.collection("tareas").get()
            .addOnSuccessListener { result ->
                // Guardamos todas las tareas cargadas
                todasLasTareas = result.mapNotNull { document ->
                    document.toObject(Tarea::class.java).apply { id = document.id }
                }

                // Actualizamos el adaptador para mostrar todas las tareas
                tareasAdapter.actualizarTareas(todasLasTareas)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al cargar las tareas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    // Mostrar un diálogo para agregar una nueva tarea
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun mostrarDialogoAgregarTarea() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        // Referencias a los elementos del layout
        val objetivoEditText = dialogView.findViewById<EditText>(R.id.editTextObjetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.editTextMetros)
        val desarrolloSwitch = dialogView.findViewById<Switch>(R.id.switchDesarrollo)
        val cortaSwitch = dialogView.findViewById<Switch>(R.id.switchCorta)

        // Al hacer clic en el botón de guardar
        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()

            // Convertir el texto de metros a un número entero
            val metros = metrosEditText.text.toString().toIntOrNull() ?: -1 // Valor por defecto si no se puede convertir

            val desarrollo = desarrolloSwitch.isChecked // Booleano correcto
            val corta = cortaSwitch.isChecked // Booleano correcto

            // Validar que todos los campos obligatorios estén completos y en el formato correcto
            if (objetivo.isEmpty() || descripcion.isEmpty() || metros == -1) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios y 'Metros' debe ser un número", Toast.LENGTH_SHORT).show()
            } else {
                // Llamar a la función para agregar la tarea, pasando los valores correctos
                agregarTarea(Tarea("", objetivo, descripcion, metros, desarrollo, corta))
            }
        }

        // Botón de cancelar
        builder.setNegativeButton("Cancelar", null)

        // Mostrar el diálogo
        val dialog = builder.create()
        dialog.show()
    }



    // Función para agregar una nueva tarea a Firestore
    fun agregarTarea(tarea: Tarea) {
        firestore.collection("tareas").add(tarea)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                cargarTareas() // Recargar las tareas
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al agregar la tarea: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun editarTarea(tarea: Tarea) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        val objetivoEditText = dialogView.findViewById<EditText>(R.id.editTextObjetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.editTextMetros)
        val desarrolloSwitch = dialogView.findViewById<Switch>(R.id.switchDesarrollo)
        val cortaSwitch = dialogView.findViewById<Switch>(R.id.switchCorta)

        // Llenar los campos con los datos actuales de la tarea
        objetivoEditText.setText(tarea.objetivo)
        descripcionEditText.setText(tarea.descripcion)
        metrosEditText.setText(tarea.metros.toString())
        desarrolloSwitch.isChecked = tarea.desarrollo
        cortaSwitch.isChecked = tarea.corta

        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()
            val metros = metrosEditText.text.toString().trim().toIntOrNull()
            val desarrollo = desarrolloSwitch.isChecked
            val corta = cortaSwitch.isChecked

            if (objetivo.isEmpty() || descripcion.isEmpty() || metros == null) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                // Actualizar la tarea en Firestore
                tarea.objetivo = objetivo
                tarea.descripcion = descripcion
                tarea.metros = metros
                tarea.desarrollo = desarrollo
                tarea.corta = corta

                firestore.collection("tareas").document(tarea.id).set(tarea)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show()
                        cargarTareas() // Recargar las tareas
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Error al actualizar la tarea: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()
    }


    fun eliminarTarea(tarea: Tarea) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Sí") { _, _ ->
                firestore.collection("tareas").document(tarea.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show()
                        cargarTareas() // Recargar las tareas
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Error al eliminar la tarea: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

}
