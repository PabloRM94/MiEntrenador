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

    private lateinit var E_Text_Mail: EditText
    private lateinit var E_Text_Contra: EditText
    private lateinit var Img_Ver_Contra: ImageView
    private lateinit var Bttn_Acceder: Button
    private lateinit var Bttn_Registro: Button
    private lateinit var Text_RecuperarContrasena: TextView
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
        firestore = FirebaseFirestore.getInstance()

        // Vincular elementos del XML
        E_Text_Mail = view.findViewById(R.id.E_Text_Mail)
        E_Text_Contra = view.findViewById(R.id.E_Text_Contra)
        Img_Ver_Contra = view.findViewById(R.id.Img_Ver_Contra)
        Text_RecuperarContrasena = view.findViewById(R.id.Text_RecuperarContrasena)
        Bttn_Acceder = view.findViewById(R.id.Bttn_Acceder)
        Bttn_Registro = view.findViewById(R.id.Bttn_Registro)

        // Manejar el botón de registro
        Bttn_Registro.setOnClickListener { registrarUsuario() }

        // Manejar el botón de login
        Bttn_Acceder.setOnClickListener { accederUsuario() }

        // Manejar el click en el ImageView para mostrar/ocultar la contraseña
        Img_Ver_Contra.setOnClickListener {
                    if (ContrasenaVisible) {
                        // Ocultar contraseña
                        E_Text_Contra.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    } else {
                        // Mostrar contraseña
                        E_Text_Contra.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                    E_Text_Contra.setSelection(E_Text_Contra.text.length)
                    ContrasenaVisible = !ContrasenaVisible // Actualizar el estado de visibilidad
                }

        // Manejar el click en el TextView de recuperar contraseña
        Text_RecuperarContrasena.setOnClickListener {
            val email = E_Text_Mail.text.toString().trim()
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
        val email = E_Text_Mail.text.toString().trim()
        val password = E_Text_Contra.text.toString().trim()
        if (email.isEmpty()) {
            E_Text_Mail.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            E_Text_Contra.error = "Por favor, introduce una contraseña"
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
                    mostrarFormularioPerfil(user, password)
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
        val email = E_Text_Mail.text.toString().trim()
        val password = E_Text_Contra.text.toString().trim()

        if (email.isEmpty()) {
            E_Text_Mail.error = "Por favor, introduce un email"
            return
        }
        if (password.isEmpty()) {
            E_Text_Contra.error = "Por favor, introduce una contraseña"
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

    // Función para mostrar el formulario de perfil
    private fun mostrarFormularioPerfil(user: FirebaseUser?, contrasena: String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_completar_perfil, null)
        builder.setView(dialogView)

        // Vinculación de los campos del formulario
        val nombreEditText: EditText = dialogView.findViewById(R.id.E_Text_Nombre)
        val apellidosEditText: EditText = dialogView.findViewById(R.id.E_text_Apellidos)
        val edadEditText: EditText = dialogView.findViewById(R.id.E_Text_Edad)
        val clubEditText: EditText = dialogView.findViewById(R.id.E_Text_Club)
        val anosNadandoEditText: EditText = dialogView.findViewById(R.id.E_Text_A_Nadando)
        val estiloFavoritoEditText: EditText = dialogView.findViewById(R.id.E_Text_Est_Fav)
        val pruebaFavoritaEditText: EditText = dialogView.findViewById(R.id.E_Text_Prub_Fav)
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

        // Validar años nadando
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

