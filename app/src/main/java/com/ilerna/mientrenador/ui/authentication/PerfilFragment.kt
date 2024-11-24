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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.*
import com.google.firebase.analytics.*
import com.google.firebase.firestore.*
import com.ilerna.mientrenador.MainActivity
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Usuario

class PerfilFragment : Fragment() {
    private lateinit var NombreUsuario: TextView
    private lateinit var E_Text_Nombre: EditText
    private lateinit var E_text_Apellidos: EditText
    private lateinit var E_Text_Edad: EditText
    private lateinit var E_Text_Club: EditText
    private lateinit var E_Text_A_Nadando: EditText
    private lateinit var E_Text_Est_Fav: EditText
    private lateinit var E_Text_Prub_Fav: EditText
    private lateinit var Bttn_Guardar: Button
    private lateinit var Bttn_CerrarSesion: Button
    private lateinit var LinLay_Perfil : LinearLayout
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
        NombreUsuario = view.findViewById(R.id.NombreUsuario)
        E_Text_Nombre = view.findViewById(R.id.E_Text_Nombre)
        E_text_Apellidos = view.findViewById(R.id.E_text_Apellidos)
        E_Text_Edad = view.findViewById(R.id.E_Text_Edad)
        E_Text_Club = view.findViewById(R.id.E_Text_Club)
        E_Text_A_Nadando = view.findViewById(R.id.E_Text_A_Nadando)
        E_Text_Est_Fav = view.findViewById(R.id.E_Text_Est_Fav)
        E_Text_Prub_Fav = view.findViewById(R.id.E_Text_Prub_Fav)
        LinLay_Perfil = view.findViewById(R.id.LinLay_Perfil)
        Bttn_Guardar = view.findViewById(R.id.Bttn_Guardar)
        Bttn_CerrarSesion = view.findViewById(R.id.Bttn_CerrarSesion)
        val navController = findNavController()

        // Recuperar y mostrar los datos del perfil del usuario
        recuperarPerfilUsuario()

        //Boton mostrar Perfil
        view.findViewById<Button>(R.id.botonPerfil).setOnClickListener {
            Visibilidad(LinLay_Perfil)
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
        Bttn_Guardar.setOnClickListener {
            guardarPerfilUsuario()
        }

        // Manejar el botón para cerrar sesión
        Bttn_CerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }

        return view
    }

    // Función para alternar la visibilidad del LinearLayout
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
                    NombreUsuario.text = "Perfil de ${usuario.nombre}"
                    E_Text_Nombre.setText(usuario.nombre)
                    E_text_Apellidos.setText(usuario.apellidos)
                    E_Text_Edad.setText(usuario.edad?.toString())
                    E_Text_Club.setText(usuario.club)
                    E_Text_A_Nadando.setText(usuario.anosNadando?.toString())
                    E_Text_Est_Fav.setText(usuario.estiloFavorito)
                    E_Text_Prub_Fav.setText(usuario.pruebaFavorita)


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

        E_Text_Nombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                NombreUsuario.text =
                    "Perfil de ${s.toString()}"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Guardar los datos del perfil del usuario en Firestore
    private fun guardarPerfilUsuario() {
        val usuarioId = mAuth.currentUser?.uid ?: return
        val nombre = E_Text_Nombre.text.toString().trim()
        val apellidos = E_text_Apellidos.text.toString().trim()
        val edad = E_Text_Edad.text.toString().trim()
        val club = E_Text_Club.text.toString().trim()
        val anosNadando = E_Text_A_Nadando.text.toString().trim()
        val estiloFavorito = E_Text_Est_Fav.text.toString().trim()
        val pruebaFavorita = E_Text_Prub_Fav.text.toString().trim()

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



}
