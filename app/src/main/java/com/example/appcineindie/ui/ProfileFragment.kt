package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import android.widget.Toast
import com.example.appcineindie.R
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.FragmentProfileBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var currentUserType: String = "spectator"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnSwitchProfileType.setOnClickListener {
            switchUserType()
        }

        // Configurar navegación (mock bottom nav)
        binding.navHome.setOnClickListener {
            if (currentUserType == "cinephile") {
                findNavController().navigate(R.id.reviewsFragment)
            } else {
                findNavController().navigate(R.id.homeFragment)
            }
        }
        
        binding.navSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.btnAdminPanel.setOnClickListener {
            findNavController().navigate(R.id.adminDashboardFragment)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvUserHandle.text = user.email
            
            // Cargar tipo de perfil desde SessionManager inmediatamente
            lifecycleScope.launch {
                sessionManager.userType.collect { type ->
                    if (_binding != null) {
                        val currentType = type ?: "spectator"
                        currentUserType = currentType
                        val typeLabel = if (currentType == "spectator") "Espectador" else "Cinéfilo"
                        binding.tvUserHandle.text = getString(R.string.handle_with_type, user.email, typeLabel)
                    }
                }
            }

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (_binding != null && document.exists()) {
                        val name = document.getString("name") ?: "Usuario"
                        val type = document.getString("type") ?: "spectator"
                        
                        binding.tvUserName.text = name

                        // Mostrar botón de admin si el tipo es admin
                        if (type == "admin") {
                            binding.btnAdminPanel.visibility = View.VISIBLE
                        } else {
                            binding.btnAdminPanel.visibility = View.GONE
                        }
                        
                        // Guardar en sesión para asegurar que esté sincronizado
                        lifecycleScope.launch {
                            sessionManager.saveSession(user.uid, type)
                        }
                    }
                }
        }
    }

    private fun switchUserType() {
        val user = auth.currentUser ?: return
        binding.btnSwitchProfileType.isEnabled = false
        showLoading()
        lifecycleScope.launch {
            // Obtener el tipo actual de Firestore para mayor precisión
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (_binding == null) {
                        hideLoading()
                        return@addOnSuccessListener
                    }

                    val currentType = document.getString("type") ?: "spectator"
                    val newType = if (currentType == "spectator") "cinephile" else "spectator"

                    db.collection("users").document(user.uid).update("type", newType)
                        .addOnSuccessListener {
                            lifecycleScope.launch {
                                sessionManager.saveSession(user.uid, newType)
                                hideLoading()
                                if (_binding != null && isAdded) {
                                    binding.btnSwitchProfileType.isEnabled = true
                                    val typeLabel = if (newType == "spectator") "Espectador" else "Cinéfilo"
                                    Toast.makeText(requireContext(), getString(R.string.profile_switched_to, typeLabel), Toast.LENGTH_SHORT).show()
                                    
                                    // Redirigir a la página principal correspondiente
                                    if (newType == "cinephile") {
                                        findNavController().navigate(R.id.reviewsFragment)
                                    } else {
                                        findNavController().navigate(R.id.homeFragment)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            hideLoading()
                            if (_binding != null && isAdded) {
                                binding.btnSwitchProfileType.isEnabled = true
                                Toast.makeText(requireContext(), getString(R.string.error_switching_profile), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener {
                    hideLoading()
                    if (_binding != null) {
                        binding.btnSwitchProfileType.isEnabled = true
                    }
                }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                auth.signOut()
                sessionManager.logout()
                if (isAdded) {
                    // Regresar al login limpiando el historial para que no pueda volver atrás
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        navOptions {
                            popUpTo(R.id.nav_graph) {
                                inclusive = true
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                // Navegación fallida o fragmento destruido
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
