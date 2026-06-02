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
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.DialogAddReviewBinding
import com.example.appcineindie.databinding.FragmentAdminReviewsBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading
import com.google.firebase.auth.FirebaseAuth

class AdminReviewsFragment : Fragment() {

    private var _binding: FragmentAdminReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminReviewAdapter
    private var moviesList: List<Movie> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddReview.setOnClickListener { showAddReviewDialog() }

        viewModel.fetchAllReviews()
        viewModel.fetchAllMovies()
    }

    private fun showAddReviewDialog() {
        if (moviesList.isEmpty()) {
            Toast.makeText(requireContext(), "Cargando películas...", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        val movieTitles = moviesList.map { it.title }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, movieTitles)
        dialogBinding.spinnerMovies.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmitReview.setOnClickListener {
            val selectedPosition = dialogBinding.spinnerMovies.selectedItemPosition
            val rating = dialogBinding.dialogRatingBar.rating
            val comment = dialogBinding.etComment.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMovie = moviesList[selectedPosition]
            submitAdminReview(selectedMovie, rating, comment)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun submitAdminReview(movie: Movie, rating: Float, comment: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val review = Review(
            userId = user?.uid ?: "admin_id",
            userName = user?.displayName ?: "Admin",
            movieId = movie.id,
            movieTitle = movie.title,
            comment = comment,
            rating = rating.toString(),
            timestamp = System.currentTimeMillis().toString()
        )

        showLoading()
        // Usamos el mismo método de borrado de ViewModel pero necesitamos uno de agregar en AdminViewModel o usar el existente de Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("reviews").add(review)
            .addOnSuccessListener {
                hideLoading()
                Toast.makeText(requireContext(), "Reseña agregada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                hideLoading()
                Toast.makeText(requireContext(), "Error al agregar reseña", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = AdminReviewAdapter(
            onDelete = { review -> showDeleteConfirmation(review) }
        )
        binding.rvReviews.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.reviewsList.observe(viewLifecycleOwner) { reviews ->
            adapter.updateData(reviews)
        }
        viewModel.moviesList.observe(viewLifecycleOwner) { movies ->
            moviesList = movies
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
    }

    private fun showDeleteConfirmation(review: Review) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review by ${review.userName}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteReview(review.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}