package com.example.appcineindie.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.appcineindie.R
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Escuchamos cuando lleguen los datos de la película destacada
        viewModel.featuredMovie.observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                Log.d("HomeFragment", "Pelicula destacada recibida: ${movie.title}")
                binding.tvFeaturedTitle.text = movie.title
                binding.tvFeaturedMetadata.text = "${movie.category} • ${movie.duration}"
                binding.tvFeaturedDescription.text = movie.description

                Glide.with(this)
                    .load(movie.imageUrl)
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.ivFeaturedPoster)

                // Click en la película destacada (Banner)
                binding.ivFeaturedPoster.setOnClickListener {
                    navigateToDetail(movie.id)
                }
            }
        }

        // Observamos películas en tendencia
        viewModel.trendingMovies.observe(viewLifecycleOwner) { movies ->
            binding.rvTrendingNow.adapter = MovieAdapter(movies, R.layout.item_movie_trending) { movie ->
                navigateToDetail(movie.id)
            }
        }

        // Observamos películas para continuar viendo
        viewModel.continueWatchingMovies.observe(viewLifecycleOwner) { movies ->
            binding.rvContinueWatching.adapter = MovieAdapter(movies, R.layout.item_movie_continue) { movie ->
                navigateToDetail(movie.id)
            }
        }
        lifecycleScope.launch {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
                viewModel.fetchHomeData(userId)
            }
        }
        // --- Lógica de navegación existente ---
        binding.navHome.setOnClickListener {
            lifecycleScope.launch {
                val type = sessionManager.userType.first()
                if (type == "cinephile") {
                    findNavController().navigate(R.id.reviewsFragment)
                }
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
    }

    private fun navigateToDetail(movieId: String) {
        if (movieId.isNotEmpty()) {
            val bundle = bundleOf("movieId" to movieId)
            findNavController().navigate(R.id.movieDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}