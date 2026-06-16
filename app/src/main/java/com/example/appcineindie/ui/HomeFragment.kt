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
import com.google.firebase.auth.FirebaseAuth
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

        observeViewModel()
        setupClickListeners()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isNotEmpty()) viewModel.fetchHomeData(userId)
    }

    private fun observeViewModel() {
        viewModel.featuredMovie.observe(viewLifecycleOwner) { movie ->
            movie?.let {
                binding.tvFeaturedTitle.text = it.title
                binding.tvFeaturedMetadata.text = "${it.category} • ${it.duration}"
                binding.tvFeaturedDescription.text = it.description
                Glide.with(this).load(it.imageUrl).into(binding.ivFeaturedPoster)
                binding.ivFeaturedPoster.setSafeOnClickListener { navigateToDetail(movie.id) }
            }
        }

        viewModel.trendingMovies.observe(viewLifecycleOwner) { movies ->
            binding.rvTrendingNow.adapter = MovieAdapter(movies, R.layout.item_movie_trending) { navigateToDetail(it.id) }
        }

        viewModel.continueWatchingMovies.observe(viewLifecycleOwner) { movies ->
            binding.rvContinueWatching.adapter = MovieAdapter(movies, R.layout.item_movie_continue) { navigateToDetail(it.id) }
        }
    }

    private fun setupClickListeners() {
        binding.navHome.setSafeOnClickListener {
            lifecycleScope.launch {
                if (sessionManager.userType.first() == "cinephile") findNavController().navigate(R.id.reviewsFragment)
            }
        }
        binding.navSearch.setSafeOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
        binding.navProfile.setSafeOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun navigateToDetail(movieId: String) {
        if (movieId.isNotEmpty()) findNavController().navigate(R.id.movieDetailFragment, bundleOf("movieId" to movieId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
