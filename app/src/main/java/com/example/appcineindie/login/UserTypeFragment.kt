package com.example.appcineindie.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.ActivityUsertypeBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserTypeFragment : Fragment() {

    private var _binding: ActivityUsertypeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityUsertypeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.cardEspectador.setOnClickListener {
            saveUserTypeAndNavigate("spectator")
        }

        binding.btnSeleccionarEspectador.setOnClickListener {
            saveUserTypeAndNavigate("spectator")
        }
        
        binding.cardCinefilo.setOnClickListener {
            saveUserTypeAndNavigate("cinephile")
        }

        binding.btnSeleccionarCinefilo.setOnClickListener {
            saveUserTypeAndNavigate("cinephile")
        }
    }

    private fun saveUserTypeAndNavigate(type: String) {
        binding.btnSeleccionarEspectador.isEnabled = false
        binding.btnSeleccionarCinefilo.isEnabled = false
        binding.cardEspectador.isClickable = false
        binding.cardCinefilo.isClickable = false
        showLoading()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: ""
        val userName = arguments?.getString("userName") ?: "Usuario"

        // Actualizar perfil de Firebase Auth para tener el nombre disponible en toda la app
        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }
        user?.updateProfile(profileUpdates)

        // Guardar en Firestore para persistencia global
        val userMap = hashMapOf(
            "type" to type,
            "name" to userName
        )
        db.collection("users").document(uid).set(userMap)
            .addOnCompleteListener {
                lifecycleScope.launch {
                    sessionManager.saveSession(uid, type)
                    hideLoading()
                    if (isAdded) {
                        if (type == "spectator") {
                            findNavController().navigate(R.id.action_userTypeFragment_to_homeFragment)
                        } else {
                            findNavController().navigate(R.id.action_userTypeFragment_to_reviewsFragment)
                        }
                    }
                }
            }
            .addOnFailureListener {
                hideLoading()
                binding.btnSeleccionarEspectador.isEnabled = true
                binding.btnSeleccionarCinefilo.isEnabled = true
                binding.cardEspectador.isClickable = true
                binding.cardCinefilo.isClickable = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
