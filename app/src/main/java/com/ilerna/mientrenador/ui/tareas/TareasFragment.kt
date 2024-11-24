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
import com.google.firebase.auth.FirebaseAuth
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.EstiloNatacion
import com.ilerna.mientrenador.ui.data.Tarea
import com.ilerna.mientrenador.utils.TareasUtils

class TareasFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var tareasList: RecyclerView
    private lateinit var Bttn_AgregarTarea: Button
    private lateinit var tareasAdapter: TareasAdapter
    private lateinit var searchViewTareas: SearchView
    private var todasLasTareas: List<Tarea> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tareas, container, false)

        firestore = FirebaseFirestore.getInstance()

        // Vincular elementos del layout
        tareasList = view.findViewById(R.id.recyclerViewTareas)
        Bttn_AgregarTarea = view.findViewById(R.id.Bttn_AgregarTarea)
        searchViewTareas = view.findViewById(R.id.searchViewTareas)

        // Configurar RecyclerView
        tareasList.layoutManager = LinearLayoutManager(requireContext())
        tareasAdapter = TareasAdapter(
            emptyList(),
            { tarea -> editarTarea(tarea) },
            { tarea -> eliminarTarea(tarea) }
        )
        tareasList.adapter = tareasAdapter

        cargarTareas()

        Bttn_AgregarTarea.setOnClickListener { mostrarDialogoAgregarTarea() }

        searchViewTareas.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    tareasAdapter.actualizarTareas(TareasUtils.filtrarTareas(todasLasTareas, it))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tareasAdapter.actualizarTareas(
                    if (newText.isNullOrBlank()) todasLasTareas
                    else TareasUtils.filtrarTareas(todasLasTareas, newText)
                )
                return false
            }
        })

        return view
    }
    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    private fun mostrarDialogoAgregarTarea() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión para agregar tareas", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        val objetivoEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Objetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Descripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Metros)
        val desarrolloSwitch = dialogView.findViewById<Switch>(R.id.S_Desarrollo)
        val cortaSwitch = dialogView.findViewById<Switch>(R.id.S_Corta)
        val checkboxCrol = dialogView.findViewById<CheckBox>(R.id.checkboxCrol)
        val checkboxEspalda = dialogView.findViewById<CheckBox>(R.id.checkboxEspalda)
        val checkboxBraza = dialogView.findViewById<CheckBox>(R.id.checkboxBraza)
        val checkboxMariposa = dialogView.findViewById<CheckBox>(R.id.checkboxMariposa)

        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()
            val metros = metrosEditText.text.toString().toIntOrNull() ?: -1
            val desarrollo = desarrolloSwitch.isChecked
            val corta = cortaSwitch.isChecked

            if (objetivo.isEmpty() || descripcion.isEmpty() || metros == -1) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios y 'Metros' debe ser un número", Toast.LENGTH_SHORT).show()
            } else {
                val estilosSeleccionados = mutableListOf<EstiloNatacion>()
                if (checkboxCrol.isChecked) estilosSeleccionados.add(EstiloNatacion.CROL)
                if (checkboxEspalda.isChecked) estilosSeleccionados.add(EstiloNatacion.ESPALDA)
                if (checkboxBraza.isChecked) estilosSeleccionados.add(EstiloNatacion.BRAZA)
                if (checkboxMariposa.isChecked) estilosSeleccionados.add(EstiloNatacion.MARIPOSA)

                val tarea = Tarea(
                    id = "",
                    objetivo = objetivo,
                    descripcion = descripcion,
                    metros = metros,
                    desarrollo = desarrollo,
                    corta = corta,
                    estilos = estilosSeleccionados
                )

                agregarTarea(user.uid, tarea) // Guardar con el ID del usuario
            }
        }

        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun agregarTarea(userId: String, tarea: Tarea) {
        val tareasCollection = firestore.collection("usuarios").document(userId).collection("tareasUsuario")

        tareasCollection.add(tarea)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                cargarTareas() // Recargar las tareas
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al agregar la tarea: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    private fun editarTarea(tarea: Tarea) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión para editar tareas", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
        builder.setView(dialogView)

        val objetivoEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Objetivo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Descripcion)
        val metrosEditText = dialogView.findViewById<EditText>(R.id.D_E_Text_Metros)
        val desarrolloSwitch = dialogView.findViewById<Switch>(R.id.S_Desarrollo)
        val cortaSwitch = dialogView.findViewById<Switch>(R.id.S_Corta)
        val checkboxCrol = dialogView.findViewById<CheckBox>(R.id.checkboxCrol)
        val checkboxEspalda = dialogView.findViewById<CheckBox>(R.id.checkboxEspalda)
        val checkboxBraza = dialogView.findViewById<CheckBox>(R.id.checkboxBraza)
        val checkboxMariposa = dialogView.findViewById<CheckBox>(R.id.checkboxMariposa)

        objetivoEditText.setText(tarea.objetivo)
        descripcionEditText.setText(tarea.descripcion)
        metrosEditText.setText(tarea.metros.toString())
        desarrolloSwitch.isChecked = tarea.desarrollo
        cortaSwitch.isChecked = tarea.corta

        checkboxCrol.isChecked = tarea.estilos.contains(EstiloNatacion.CROL)
        checkboxEspalda.isChecked = tarea.estilos.contains(EstiloNatacion.ESPALDA)
        checkboxBraza.isChecked = tarea.estilos.contains(EstiloNatacion.BRAZA)
        checkboxMariposa.isChecked = tarea.estilos.contains(EstiloNatacion.MARIPOSA)

        builder.setPositiveButton("Guardar") { _, _ ->
            val objetivo = objetivoEditText.text.toString().trim()
            val descripcion = descripcionEditText.text.toString().trim()
            val metros = metrosEditText.text.toString().trim().toIntOrNull()
            val desarrollo = desarrolloSwitch.isChecked
            val corta = cortaSwitch.isChecked

            if (objetivo.isEmpty() || descripcion.isEmpty() || metros == null) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                val estilosSeleccionados = mutableListOf<EstiloNatacion>()
                if (checkboxCrol.isChecked) estilosSeleccionados.add(EstiloNatacion.CROL)
                if (checkboxEspalda.isChecked) estilosSeleccionados.add(EstiloNatacion.ESPALDA)
                if (checkboxBraza.isChecked) estilosSeleccionados.add(EstiloNatacion.BRAZA)
                if (checkboxMariposa.isChecked) estilosSeleccionados.add(EstiloNatacion.MARIPOSA)

                tarea.objetivo = objetivo
                tarea.descripcion = descripcion
                tarea.metros = metros
                tarea.desarrollo = desarrollo
                tarea.corta = corta
                tarea.estilos = estilosSeleccionados

                firestore.collection("usuarios").document(user.uid).collection("tareasUsuario")
                    .document(tarea.id)
                    .set(tarea)
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

    private fun cargarTareas() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión para ver tareas", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("usuarios").document(user.uid).collection("tareasUsuario").get()
            .addOnSuccessListener { result ->
                todasLasTareas = result.mapNotNull { document ->
                    document.toObject(Tarea::class.java).apply { id = document.id }
                }
                tareasAdapter.actualizarTareas(todasLasTareas)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al cargar las tareas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarTarea(tarea: Tarea) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión para eliminar tareas", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Sí") { _, _ ->
                firestore.collection("usuarios").document(user.uid).collection("tareasUsuario")
                    .document(tarea.id)
                    .delete()
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
