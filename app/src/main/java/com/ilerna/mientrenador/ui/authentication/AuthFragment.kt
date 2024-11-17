package com.ilerna.mientrenador.ui.authentication

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
    private lateinit var ContrasenaEditText: EditText
    private lateinit var verContrasena: ImageView
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var recuperarContrasenaTextView: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firestore: FirebaseFirestore
    private var ContrasenaVisible: Boolean  = false

    @SuppressLint("MissingInflatedId")
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
        ContrasenaEditText = view.findViewById(R.id.text_pass)
        verContrasena = view.findViewById(R.id.verContraseña)
        recuperarContrasenaTextView = view.findViewById(R.id.recuperarContrasena)
        loginButton = view.findViewById(R.id.Accederbutton)
        signupButton = view.findViewById(R.id.singupButton)
        verContrasena.setOnClickListener {
            if (ContrasenaVisible) {
                // Ocultar contraseña
                ContrasenaEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                // Mostrar contraseña
                ContrasenaEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            ContrasenaEditText.setSelection(ContrasenaEditText.text.length)
            ContrasenaVisible = !ContrasenaVisible // Actualizar el estado de visibilidad
        }

        // Manejar el botón de registro
        signupButton.setOnClickListener { registrarUsuario() }

        // Manejar el botón de login
        loginButton.setOnClickListener { accederUsuario() }

        recuperarContrasenaTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, introduce tu email para continuar", Toast.LENGTH_SHORT).show()
            } else {
                // Crear un diálogo para confirmar antes de enviar el correo
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Confirmar recuperación de contraseña")
                builder.setMessage("Se enviará un correo a $email para recuperar tu contraseña. ¿Deseas continuar?")
                builder.setPositiveButton("Sí") { dialog, _ ->
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Correo enviado para restablecer contraseña", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.create().show()
            }
        }

        return view
    }

    // Función para registrar al usuario con correo y contraseña
    private fun registrarUsuario() {
        val email = emailEditText.text.toString().trim()
        val password = ContrasenaEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            ContrasenaEditText.error = "Por favor, introduce una contraseña"
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
        val password = ContrasenaEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            ContrasenaEditText.error = "Por favor, introduce una contraseña"
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

    private fun mostrarFormularioPerfil(user: FirebaseUser?, contrasena: String, esEdicion: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_completar_perfil, null)
        builder.setView(dialogView)

        // Vinculación de los campos del formulario
        val nombreEditText: EditText = dialogView.findViewById(R.id.editTextNombre)
        val apellidosEditText: EditText = dialogView.findViewById(R.id.editTextApellido)
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

        // Crear el diálogo
        builder.setPositiveButton("Guardar", null)
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()

        // Obtener el botón Guardar del diálogo
        val guardarButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        // Inicialmente deshabilitar el botón Guardar
        guardarButton.isEnabled = false

        // Validar campos en tiempo real
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Validar campos dinámicamente
                guardarButton.isEnabled = validarCamposPerfilEnTiempoReal(
                    nombreEditText,
                    edadEditText,
                    clubEditText,
                    anosNadandoEditText
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // Añadir TextWatcher a los campos obligatorios
        nombreEditText.addTextChangedListener(textWatcher)
        edadEditText.addTextChangedListener(textWatcher)
        clubEditText.addTextChangedListener(textWatcher)
        anosNadandoEditText.addTextChangedListener(textWatcher)

        // Configurar el botón Guardar para guardar los datos si la validación es correcta
        guardarButton.setOnClickListener {
            if (!validarCamposPerfilEnTiempoReal(
                    nombreEditText,
                    edadEditText,
                    clubEditText,
                    anosNadandoEditText
                )
            ) {
                Toast.makeText(requireContext(), "Por favor, rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar los datos del perfil en Firestore
            val nombre = nombreEditText.text.toString().trim()
            val apellidos = apellidosEditText.text.toString().trim()
            val edad = edadEditText.text.toString().trim()
            val club = clubEditText.text.toString().trim()
            val anosNadando = anosNadandoEditText.text.toString().trim()
            val estiloFavorito = estiloFavoritoEditText.text.toString().trim()
            val pruebaFavorita = pruebaFavoritaEditText.text.toString().trim()

            val usuarioId = user?.uid ?: return@setOnClickListener
            val usuario = Usuario(
                email = user.email,
                nombre = nombre,
                apellidos = apellidos,
                edad = edad.toInt(),
                club = club,
                anosNadando = if (anosNadando.isNotEmpty()) anosNadando.toInt() else null,
                estiloFavorito = estiloFavorito,
                pruebaFavorita = pruebaFavorita,
                contrasena = contrasena
            )

            firestore.collection("usuarios").document(usuarioId)
                .set(usuario)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Perfil guardado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar el perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // Validar campos en tiempo real
    private fun validarCamposPerfilEnTiempoReal(
        nombreEditText: EditText,
        edadEditText: EditText,
        clubEditText: EditText,
        anosNadandoEditText: EditText
    ): Boolean {
        var esValido = true

        // Validar nombre
        if (nombreEditText.text.toString().trim().isEmpty()) {
            esValido = false
        }

        // Validar edad
        val edad = edadEditText.text.toString().trim()
        if (edad.isEmpty() || !edad.matches("\\d+".toRegex())) {
            esValido = false
        }

        // Validar años nadando (opcional, pero numérico si se introduce)
        val anosNadando = anosNadandoEditText.text.toString().trim()
        if (anosNadando.isNotEmpty() && !anosNadando.matches("\\d+".toRegex())) {
            esValido = false
        }

        // Validar club
        if (clubEditText.text.toString().trim().isEmpty()) {
            esValido = false
        }

        return esValido
    }

}

