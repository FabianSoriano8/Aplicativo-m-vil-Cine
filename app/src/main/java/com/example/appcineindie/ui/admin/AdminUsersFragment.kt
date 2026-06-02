package com.example.appcineindie.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.databinding.DialogAddUserBinding
import com.example.appcineindie.databinding.FragmentAdminUsersBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminUserAdapter
    private val userTypes = listOf("spectator", "cinephile", "admin")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddUser.setOnClickListener { showAddUserDialog() }

        viewModel.fetchAllUsers()
    }

    private fun showAddUserDialog() {
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, userTypes)
        dialogBinding.spinnerUserType.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCreateUser.setOnClickListener {
            val name = dialogBinding.etAdminUserName.text.toString().trim()
            val email = dialogBinding.etAdminUserEmail.text.toString().trim()
            val password = dialogBinding.etAdminUserPassword.text.toString().trim()
            val type = userTypes[dialogBinding.spinnerUserType.selectedItemPosition]

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Contraseña muy corta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createNewUser(name, email, password, type)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createNewUser(name: String, email: String, pass: String, type: String) {
        val auth = FirebaseAuth.getInstance()
        showLoading()
        // Creamos el usuario en Auth
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val userData = hashMapOf(
                    "name" to name,
                    "type" to type
                )
                // Guardamos sus datos en Firestore
                FirebaseFirestore.getInstance().collection("users").document(uid).set(userData)
                    .addOnCompleteListener {
                        hideLoading()
                        Toast.makeText(requireContext(), "Usuario $name creado con éxito", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                hideLoading()
                Toast.makeText(requireContext(), "Error al crear usuario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = AdminUserAdapter(
            onChangeType = { id, newType -> viewModel.changeUserType(id, newType) },
            onDelete = { id -> showDeleteConfirmation(id) }
        )
        binding.rvUsers.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.usersList.observe(viewLifecycleOwner) { users ->
            adapter.updateData(users)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
    }

    private fun showDeleteConfirmation(userId: String) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que quieres eliminar este usuario? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteUser(userId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}