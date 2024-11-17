package com.ilerna.mientrenador.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.*
import com.google.firebase.analytics.*
import com.google.firebase.firestore.*
import com.ilerna.mientrenador.MainActivity
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Usuario

class PerfilFragment : Fragment() {
    private lateinit var textViewTitulo: TextView
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var clubEditText: EditText
    private lateinit var anosNadandoEditText: EditText
    private lateinit var estiloFavoritoEditText: EditText
    private lateinit var pruebaFavoritaEditText: EditText
    private lateinit var guardarButton: Button
    private lateinit var cerrarSesionButton: Button
    private lateinit var botonPerfil :Button
    private lateinit var seccionPerfil : LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializar Firebase Auth, Firestore y Firebase Analytics
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // Firestore para recuperar y guardar datos
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        // Vincular elementos del layout
        textViewTitulo = view.findViewById(R.id.textViewTitulo)
        nombreEditText = view.findViewById(R.id.editTextNombre)
        apellidoEditText = view.findViewById(R.id.editTextApellido)
        edadEditText = view.findViewById(R.id.editTextEdad)
        clubEditText = view.findViewById(R.id.editTextClub)
        anosNadandoEditText = view.findViewById(R.id.editTextAnosNadando)
        estiloFavoritoEditText = view.findViewById(R.id.editTextEstiloFavorito)
        pruebaFavoritaEditText = view.findViewById(R.id.editTextPruebaFavorita)
        seccionPerfil = view.findViewById(R.id.seccionPerfil)
        guardarButton = view.findViewById(R.id.guardarButton)
        cerrarSesionButton = view.findViewById(R.id.cerrarSesionButton)
        val navController = findNavController()

        // Recuperar y mostrar los datos del perfil del usuario
        recuperarPerfilUsuario()

        //Boton mostrar Perfil
        view.findViewById<Button>(R.id.botonPerfil).setOnClickListener {
            Visibilidad(seccionPerfil)
        }
        // Botón para ir al fragmento de entrenamientos
        view.findViewById<Button>(R.id.btnEntrenamientos).setOnClickListener {
            navController.navigate(R.id.action_nav_perfil_to_nav_entrenamientos)
        }

        // Botón para ir al fragmento de tareas
        view.findViewById<Button>(R.id.btnTareas).setOnClickListener {
            navController.navigate(R.id.action_nav_perfil_to_nav_Tareas)
        }

        // Manejar el botón para guardar los cambios en el perfil
        guardarButton.setOnClickListener {
            guardarPerfilUsuario()
        }

        // Manejar el botón para cerrar sesión
        cerrarSesionButton.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }

        return view
    }

    // Función para alternar la visibilidad de un LinearLayout
    private fun Visibilidad (section: LinearLayout) {
        section.visibility = if (section.visibility == View.GONE) View.VISIBLE else View.GONE
    }
    // Recuperar los datos del perfil del usuario desde Firestore
    private fun recuperarPerfilUsuario() {
        val usuarioId = mAuth.currentUser?.uid ?: return

        // Obtener los datos del perfil desde Firestore
        firestore.collection("usuarios").document(usuarioId)
            .get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)

                if (usuario != null) {
                    textViewTitulo.text = "Perfil de ${usuario.nombre}"
                    nombreEditText.setText(usuario.nombre)
                    apellidoEditText.setText(usuario.apellidos)
                    edadEditText.setText(usuario.edad?.toString())
                    clubEditText.setText(usuario.club)
                    anosNadandoEditText.setText(usuario.anosNadando?.toString())
                    estiloFavoritoEditText.setText(usuario.estiloFavorito)
                    pruebaFavoritaEditText.setText(usuario.pruebaFavorita)


                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar el perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error al cargar el perfil: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        nombreEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textViewTitulo.text =
                    "Perfil de ${s.toString()}"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Guardar los datos del perfil del usuario en Firestore
    private fun guardarPerfilUsuario() {
        val usuarioId = mAuth.currentUser?.uid ?: return
        val nombre = nombreEditText.text.toString().trim()
        val apellidos = apellidoEditText.text.toString().trim()
        val edad = edadEditText.text.toString().trim()
        val club = clubEditText.text.toString().trim()
        val anosNadando = anosNadandoEditText.text.toString().trim()
        val estiloFavorito = estiloFavoritoEditText.text.toString().trim()
        val pruebaFavorita = pruebaFavoritaEditText.text.toString().trim()

        // Validar los datos antes de guardar
        if (nombre.isEmpty() || edad.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y Edad son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el objeto Usuario actualizado
        val usuario = Usuario(
            email = mAuth.currentUser?.email,
            nombre = nombre,
            apellidos = apellidos,
            edad = edad.toInt(),
            club = club,
            anosNadando = if (anosNadando.isNotEmpty()) anosNadando.toInt() else null,
            estiloFavorito = estiloFavorito,
            pruebaFavorita = pruebaFavorita
        )

        // Guardar los datos en Firestore
        firestore.collection("usuarios").document(usuarioId)
            .set(usuario)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()

                // Registrar el evento de actualización de perfil en Firebase Analytics
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.ITEM_NAME, "perfil_actualizado")
                }
                firebaseAnalytics.logEvent("perfil_guardado", bundle)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar el perfil: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Mostrar un AlertDialog para confirmar el cierre de sesión
    private fun mostrarDialogoCerrarSesion() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar Sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Sí") { _, _ ->
            cerrarSesion()
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()
    }

    // Función para cerrar sesión del usuario
    private fun cerrarSesion() {
        mAuth.signOut()

        // Mostrar un mensaje de despedida y redirigir al inicio de sesión
        AlertDialog.Builder(requireContext())
            .setTitle("Adiós")
            .setMessage("Gracias por usar nuestra app. ¡Hasta pronto!")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .create()
            .show()
    }

}
