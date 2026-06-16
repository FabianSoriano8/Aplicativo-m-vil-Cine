package com.example.appcineindie.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.databinding.FragmentRegisterBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btRegistrar.setOnClickListener {
            registerUser()
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()
        binding.btRegistrar.isEnabled = false
        db.collection("users")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            hideLoading()
                            if (task.isSuccessful) {
                                val bundle = Bundle().apply {
                                    putString("userName", name)
                                }
                                findNavController().navigate(R.id.action_registerFragment_to_userTypeFragment, bundle)
                            } else {
                                binding.btRegistrar.isEnabled = true
                                Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    hideLoading()
                    binding.btRegistrar.isEnabled = true
                    Toast.makeText(requireContext(), "Este nombre de usuario ya está en uso. Por favor elige otro.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                binding.btRegistrar.isEnabled = true
                Toast.makeText(requireContext(), "Error al verificar disponibilidad: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                hideLoading()
                binding.btRegistrar.isEnabled = true
                Toast.makeText(requireContext(), "Error al verificar disponibilidad: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
