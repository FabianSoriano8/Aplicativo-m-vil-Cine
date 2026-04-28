package com.example.appcineindie.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            // Lógica de login aquí
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()){
                binding.etEmail.error = "Ingresa tu email"
                return@setOnClickListener
            }

            if (password.isEmpty()){
                binding.etPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            login(email, password)
        }
    }

    private fun login(email: String, password: String){
        if (email == "admin@test.com" && password == "123456"){
            findNavController().navigate(R.id.action_loginFragment_to_reviewsFragment)
        }else{
            Toast.makeText(requireContext(), "Credenciales incorrectas",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
