package com.ilerna.mientrenador.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.*
import com.google.firebase.analytics.*
import com.google.firebase.firestore.*
import com.ilerna.mientrenador.MainActivity
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Usuario

class PerfilFragment : Fragment() {

    private lateinit var nombreEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var clubEditText: EditText
    private lateinit var anosNadandoEditText: EditText
    private lateinit var estiloFavoritoEditText: EditText
    private lateinit var pruebaFavoritaEditText: EditText
    private lateinit var guardarButton: Button
    private lateinit var cerrarSesionButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
        nombreEditText = view.findViewById(R.id.editTextNombre)
        edadEditText = view.findViewById(R.id.editTextEdad)
        clubEditText = view.findViewById(R.id.editTextClub)
        anosNadandoEditText = view.findViewById(R.id.editTextAnosNadando)
        estiloFavoritoEditText = view.findViewById(R.id.editTextEstiloFavorito)
        pruebaFavoritaEditText = view.findViewById(R.id.editTextPruebaFavorita)
        guardarButton = view.findViewById(R.id.guardarButton)
        cerrarSesionButton = view.findViewById(R.id.cerrarSesionButton)

        // Recuperar y mostrar los datos del perfil del usuario
        recuperarPerfilUsuario()

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

    // Recuperar los datos del perfil del usuario desde Firestore
    private fun recuperarPerfilUsuario() {
        val usuarioId = mAuth.currentUser?.uid ?: return

        // Obtener los datos del perfil desde Firestore
        firestore.collection("usuarios").document(usuarioId)
            .get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                if (usuario != null) {
                    nombreEditText.setText(usuario.nombre)
                    edadEditText.setText(usuario.edad?.toString())
                    clubEditText.setText(usuario.club)
                    anosNadandoEditText.setText(usuario.anosNadando?.toString())
                    estiloFavoritoEditText.setText(usuario.estiloFavorito)
                    pruebaFavoritaEditText.setText(usuario.pruebaFavorita)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar el perfil: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Guardar los datos del perfil del usuario en Firestore
    private fun guardarPerfilUsuario() {
        val usuarioId = mAuth.currentUser?.uid ?: return

        val nombre = nombreEditText.text.toString().trim()
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

        // Registrar el evento de cierre de sesión en Firebase Analytics
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "sign_out")
        }


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
