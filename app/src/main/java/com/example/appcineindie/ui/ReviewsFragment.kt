package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcineindie.R
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.DialogAddReviewBinding
import com.example.appcineindie.databinding.FragmentReviewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReviewsFragment : Fragment() {

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: ReviewsViewModel by viewModels()
    private val adapter = ReviewAdapter()
    private var moviesList: List<Movie> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.fetchMovies()
        // Por ahora escuchamos todas las reseñas (feed global)
        viewModel.listenForReviews("") 

        binding.navHome.setOnClickListener {
            lifecycleScope.launch {
                val type = sessionManager.userType.first()
                if (type == "spectator") {
                    findNavController().navigate(R.id.homeFragment)
                }
                // Si es cinéfilo, ya está en ReviewsFragment (su home)
            }
        }

        binding.navProfile.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        binding.navSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        binding.btnSendReview.setOnClickListener {
            showAddReviewDialog()
        }
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        
        // Configurar el spinner de películas
        if (moviesList.isEmpty()) {
            Toast.makeText(requireContext(), "Cargando películas, intenta en un momento...", Toast.LENGTH_SHORT).show()
            viewModel.fetchMovies() // Reintentar carga si está vacía
        }

        val movieTitles = moviesList.map { it.title }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, movieTitles)
        dialogBinding.spinnerMovies.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmitReview.setOnClickListener {
            val selectedPosition = dialogBinding.spinnerMovies.selectedItemPosition
            if (selectedPosition < 0 || moviesList.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una película", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMovie = moviesList[selectedPosition]
            val rating = dialogBinding.dialogRatingBar.rating
            val comment = dialogBinding.etComment.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitReview(selectedMovie, rating, comment)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun submitReview(movie: Movie, rating: Float, comment: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        // Buscamos el nombre en Firestore para estar seguros de tener el más reciente
        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val realName = doc.getString("name") ?: user.displayName ?: "Usuario"

                val review = Review(
                    userId = user.uid,
                    userName = realName,
                    movieId = movie.id,
                    movieTitle = movie.title,
                    comment = comment,
                    rating = rating.toString(),
                    timestamp = System.currentTimeMillis()
                )

                viewModel.addReview("", review, {
                    Toast.makeText(requireContext(), "Reseña publicada", Toast.LENGTH_SHORT).show()
                }, { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                })
            }
    }

    private fun setupRecyclerView() {
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReviewsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.reviewsList.observe(viewLifecycleOwner) { reviews ->
            adapter.updateData(reviews)
        }
        viewModel.moviesList.observe(viewLifecycleOwner) { movies ->
            moviesList = movies
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
