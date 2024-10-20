package com.ilerna.mientrenador.ui.tareas

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Tarea

class TareasFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasList: RecyclerView
    private lateinit var agregarTareaButton: Button
    private lateinit var editarTareaButton: Button
    private lateinit var tareasAdapter: TareasAdapter

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
        editarTareaButton = view.findViewById(R.id.buttonEditarTarea)

        // Configurar RecyclerView
        tareasList.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador con los métodos para editar y eliminar tareas
        tareasAdapter = TareasAdapter(emptyList(),
            { tarea -> editarTarea(tarea) }, // Función para editar
            { tarea -> eliminarTarea(tarea) } // Función para eliminar
        )
        tareasList.adapter = tareasAdapter

        // Cargar las tareas desde Firestore
        cargarTareas()

        // Botón para agregar nueva tarea
        agregarTareaButton.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        // Botón para editar/eliminar tareas
        editarTareaButton.setOnClickListener {
            mostrarDialogoEditarTarea()
        }

        return view
    }

    // Función para cargar todas las tareas desde Firestore
    private fun cargarTareas() {
        firestore.collection("tareas").get()
            .addOnSuccessListener { result ->
                val tareas = result.mapNotNull { document ->
                    document.toObject(Tarea::class.java).apply { id = document.id }
                }
                tareasAdapter.actualizarTareas(tareas)
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

    // Mostrar un diálogo para editar o eliminar tareas
    @SuppressLint("MissingInflatedId")
    fun mostrarDialogoEditarTarea() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_tarea, null)
        builder.setView(dialogView)

        val listaTareasRecyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewEditarTareas)
        listaTareasRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Cargar las tareas y mostrarlas en el diálogo
        firestore.collection("tareas").get()
            .addOnSuccessListener { result ->
                val tareas = result.mapNotNull { document ->
                    document.toObject(Tarea::class.java).apply { id = document.id }
                }
                val adapter = EditarTareasAdapter(tareas, { tarea -> editarTarea(tarea) }, { tarea -> eliminarTarea(tarea) })
                listaTareasRecyclerView.adapter = adapter
            }

        builder.setNegativeButton("Cerrar", null)

        val dialog = builder.create()
        dialog.show()
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
