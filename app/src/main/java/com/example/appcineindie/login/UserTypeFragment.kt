package com.example.appcineindie.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.databinding.ActivityUsertypeBinding

class UserTypeFragment : Fragment() {

    private var _binding: ActivityUsertypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityUsertypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Aquí puedes agregar la lógica para seleccionar el tipo de perfil
        binding.cardEspectador.setOnClickListener {
            // Lógica selección Espectador
        }
        
        binding.cardCinefilo.setOnClickListener {
            // Lógica selección Cinéfilo
        }
        
        binding.btnContinuar.setOnClickListener {
            findNavController().navigate(R.id.action_userTypeFragment_to_reviewsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
