package com.example.appcineindie.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.FragmentLoginBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar si ya hay una sesión activa al iniciar
        checkActiveSession()

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Ingresa tu email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            login(email, password)
        }
    }

    private fun checkActiveSession() {
        lifecycleScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (isLoggedIn) {
                val type = sessionManager.userType.first()
                if (type != null) {
                    navigateToCorrectHome(type)
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        showLoading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    fetchUserTypeAndSaveSession(uid)
                } else {
                    binding.btnLogin.isEnabled = true
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // LÓGICA ACTUALIZADA: Comprobación estricta de existencia en Firestore
    private fun fetchUserTypeAndSaveSession(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // El usuario existe: Flujo normal
                    val type = document.getString("type") ?: "spectator"
                    lifecycleScope.launch {
                        sessionManager.saveSession(uid, type)
                        hideLoading()
                        navigateToCorrectHome(type)
                    }
                } else {
                    // ELIMINADO POR ADMIN EN FIRESTORE: Procedemos a borrarlo de Authentication
                    val user = auth.currentUser

                    user?.delete()?.addOnCompleteListener { task ->
                        hideLoading()
                        binding.btnLogin.isEnabled = true
                        if (task.isSuccessful) {
                            // Borrado exitosamente de Authentication y desautenticado automáticamente
                            if (isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "Acceso denegado: Esta cuenta ha sido eliminada por completo.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            // Si falla el borrado por alguna razón de red o sesión expirada
                            auth.signOut()
                            if (isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    "Acceso denegado. Error al limpiar autenticación: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                hideLoading()
                binding.btnLogin.isEnabled = true
                if (isAdded) {
                    findNavController().navigate(R.id.action_loginFragment_to_userTypeFragment)
                }
            }
    }

    private fun navigateToCorrectHome(type: String?) {
        if (type == "cinephile") {
            findNavController().navigate(R.id.action_loginFragment_to_reviewsFragment)
        } else {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}