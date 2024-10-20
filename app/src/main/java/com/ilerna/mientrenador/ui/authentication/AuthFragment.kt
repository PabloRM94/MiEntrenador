package com.ilerna.mientrenador.ui.authentication

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.*
import com.google.firebase.analytics.*
import com.google.firebase.firestore.*
import com.ilerna.mientrenador.R
import com.ilerna.mientrenador.ui.data.Usuario

class AuthFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        // Inicializar Firebase Auth, Firestore y Firebase Analytics
        mAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        firestore = FirebaseFirestore.getInstance() // Firestore para guardar datos

        // Vincular elementos del XML
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = view.findViewById(R.id.text_pass)
        loginButton = view.findViewById(R.id.Accederbutton)
        signupButton = view.findViewById(R.id.singupButton)

        // Manejar el botón de registro
        signupButton.setOnClickListener { registrarUsuario() }

        // Manejar el botón de login
        loginButton.setOnClickListener { accederUsuario() }

        return view
    }

    // Función para registrar al usuario con correo y contraseña
    private fun registrarUsuario() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Por favor, introduce una contraseña"
            return
        }

        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(
                        requireContext(),
                        "Usuario registrado: ${user?.email}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Registrar evento de registro en Firebase Analytics
                    val bundle = Bundle().apply {
                        putString(FirebaseAnalytics.Param.METHOD, "email")
                    }
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                    // Mostrar formulario de perfil
                    mostrarFormularioPerfil(user, password, false)  // false -> nuevo perfil
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error en el registro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Función para acceder al usuario con correo y contraseña
    private fun accederUsuario() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Por favor, introduce una contraseña"
            return
        }

        // Iniciar sesión en Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(
                        requireContext(),
                        "Acceso concedido: ${user?.email}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Registrar evento de inicio de sesión en Firebase Analytics
                    val bundle = Bundle().apply {
                        putString(FirebaseAnalytics.Param.METHOD, "email")
                    }
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)

                    // Navegar al fragmento de perfil
                    val navController = findNavController()
                    navController.navigate(R.id.nav_perfil)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al acceder: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Mostrar formulario para completar o editar el perfil del usuario
    private fun mostrarFormularioPerfil(user: FirebaseUser?, contrasena: String, esEdicion: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_completar_perfil, null)
        builder.setView(dialogView)

        // Vinculación de los campos del formulario
        val nombreEditText: EditText = dialogView.findViewById(R.id.editTextNombre)
        val edadEditText: EditText = dialogView.findViewById(R.id.editTextEdad)
        val clubEditText: EditText = dialogView.findViewById(R.id.editTextClub)
        val anosNadandoEditText: EditText = dialogView.findViewById(R.id.editTextAnosNadando)
        val estiloFavoritoEditText: EditText = dialogView.findViewById(R.id.editTextEstiloFavorito)
        val pruebaFavoritaEditText: EditText = dialogView.findViewById(R.id.editTextPruebaFavorita)
        val contrasenaEditText: EditText = dialogView.findViewById(R.id.editTextContrasena)
        val mostrarContrasenaCheckBox: CheckBox = dialogView.findViewById(R.id.mostrarContrasenaCheckBox)

        // Mostrar/ocultar la contraseña
        contrasenaEditText.setText(contrasena)
        mostrarContrasenaCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                contrasenaEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                contrasenaEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        // Guardar los datos del perfil en Firestore al confirmar el formulario
        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = nombreEditText.text.toString().trim()
            val edad = edadEditText.text.toString().trim()
            val club = clubEditText.text.toString().trim()
            val anosNadando = anosNadandoEditText.text.toString().trim()
            val estiloFavorito = estiloFavoritoEditText.text.toString().trim()
            val pruebaFavorita = pruebaFavoritaEditText.text.toString().trim()

            if (!validarCamposPerfil(nombre, edad, club, anosNadando)) {
                return@setPositiveButton
            }

            // Guardar los datos del usuario en Firebase Firestore
            val usuarioId = user?.uid ?: return@setPositiveButton
            val usuario = Usuario(
                email = user.email,
                nombre = nombre,
                edad = edad.toInt(),
                club = club,
                anosNadando = if (anosNadando.isNotEmpty()) anosNadando.toInt() else null,
                estiloFavorito = estiloFavorito,
                pruebaFavorita = pruebaFavorita,
                contrasena = contrasena
            )

            // Guardar los datos en Firestore en la colección "usuarios"
            firestore.collection("usuarios").document(usuarioId)
                .set(usuario)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Perfil guardado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar el perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    // Validar los campos del perfil antes de guardar
    private fun validarCamposPerfil(
        nombre: String,
        edad: String,
        club: String,
        anosNadando: String
    ): Boolean {
        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        if (edad.isEmpty() || !edad.matches("\\d+".toRegex())) {
            Toast.makeText(requireContext(), "Introduce una edad válida", Toast.LENGTH_SHORT).show()
            return false
        }
        if (anosNadando.isNotEmpty() && !anosNadando.matches("\\d+".toRegex())) {
            Toast.makeText(requireContext(), "Introduce un valor numérico válido para los años nadando", Toast.LENGTH_SHORT).show()
            return false
        }
        if (club.isEmpty()) {
            Toast.makeText(requireContext(), "El club es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

