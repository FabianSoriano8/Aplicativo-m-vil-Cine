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
import com.example.appcineindie.databinding.DialogAddMovieBinding
import com.example.appcineindie.databinding.FragmentAdminMoviesBinding
import com.example.appcineindie.ui.hideLoading
import com.example.appcineindie.ui.showLoading

class AdminMoviesFragment : Fragment() {

    private var _binding: FragmentAdminMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminMovieAdapter
    private val categories = listOf("Featured", "Trending", "Continue", "Se va pronto")
    private val genres = listOf("Ninguno", "Acción", "Comedia", "Drama", "Terror", "Ciencia Ficción", "Animación", "Suspenso", "Romance", "Documental")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddMovie.setOnClickListener { showMovieDialog(null) }

        viewModel.fetchAllMovies()
    }

    private fun setupRecyclerView() {
        adapter = AdminMovieAdapter(
            onEdit = { movie -> showMovieDialog(movie) },
            onDelete = { movie -> showDeleteConfirmation(movie) }
        )
        binding.rvMovies.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.moviesList.observe(viewLifecycleOwner) { movies ->
            adapter.updateData(movies)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
    }

    private fun showMovieDialog(movie: Movie?) {
        val isEdit = movie != null
        val dialogBinding = DialogAddMovieBinding.inflate(layoutInflater)
        
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        dialogBinding.spinnerCategory.adapter = spinnerAdapter

        val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        dialogBinding.spinnerGender1.adapter = genreAdapter
        dialogBinding.spinnerGender2.adapter = genreAdapter
        dialogBinding.spinnerGender3.adapter = genreAdapter

        if (isEdit) {
            dialogBinding.tvDialogTitle.text = "Edit Movie"
            dialogBinding.etMovieTitle.setText(movie!!.title)
            dialogBinding.etMovieDesc.setText(movie.description)
            dialogBinding.etMovieUrl.setText(movie.imageUrl)
            dialogBinding.etMovieDuration.setText(movie.duration)
            
            val catIndex = categories.indexOf(movie.category)
            if (catIndex >= 0) dialogBinding.spinnerCategory.setSelection(catIndex)

            movie.genres.getOrNull(0)?.let { g ->
                val idx = genres.indexOf(g)
                if (idx >= 0) dialogBinding.spinnerGender1.setSelection(idx)
            }
            movie.genres.getOrNull(1)?.let { g ->
                val idx = genres.indexOf(g)
                if (idx >= 0) dialogBinding.spinnerGender2.setSelection(idx)
            }
            movie.genres.getOrNull(2)?.let { g ->
                val idx = genres.indexOf(g)
                if (idx >= 0) dialogBinding.spinnerGender3.setSelection(idx)
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSaveMovie.setOnClickListener {
            val title = dialogBinding.etMovieTitle.text.toString().trim()
            if (title.isEmpty()) return@setOnClickListener

            val selectedGenres = mutableListOf<String>()
            val g1 = genres[dialogBinding.spinnerGender1.selectedItemPosition]
            val g2 = genres[dialogBinding.spinnerGender2.selectedItemPosition]
            val g3 = genres[dialogBinding.spinnerGender3.selectedItemPosition]
            if (g1 != "Ninguno") selectedGenres.add(g1)
            if (g2 != "Ninguno") selectedGenres.add(g2)
            if (g3 != "Ninguno") selectedGenres.add(g3)

            val newMovie = Movie(
                id = movie?.id ?: "",
                title = title,
                description = dialogBinding.etMovieDesc.text.toString().trim(),
                category = categories[dialogBinding.spinnerCategory.selectedItemPosition],
                imageUrl = dialogBinding.etMovieUrl.text.toString().trim(),
                duration = dialogBinding.etMovieDuration.text.toString().trim(),
                genres = selectedGenres,
                rating = movie?.rating ?: "0.0",
                remainingTime = movie?.remainingTime ?: ""
            )

            viewModel.addOrUpdateMovie(newMovie, isEdit)
            dialog.dismiss()
            Toast.makeText(requireContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(movie: Movie) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Delete Movie")
            .setMessage("Are you sure you want to delete ${movie.title}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMovie(movie.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}