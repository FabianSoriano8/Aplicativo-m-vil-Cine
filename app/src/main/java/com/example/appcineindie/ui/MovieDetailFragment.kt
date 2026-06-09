package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.appcineindie.R
import com.bumptech.glide.Glide
import com.example.appcineindie.data.Movie
import com.example.appcineindie.databinding.FragmentMovieDetailBinding

class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    // Usaremos el mismo ViewModel si tiene lógica para obtener una película por ID
    private val viewModel: HomeViewModel by viewModels()
    
    // Si usas SafeArgs, puedes recibir el ID así
    // private val args: MovieDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el movieId de los argumentos (pasado al navegar)
        val movieId = arguments?.getString("movieId") ?: ""

        viewModel.featuredMovie.observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                binding.tvTitleDetail.text = movie.title
                binding.tvDescriptionDetail.text = movie.description
                binding.tvMetadataDetail.text = "${movie.category} • ${movie.duration}"
                binding.tvRatingDetail.text = if (movie.rating.isNotEmpty()) "★ ${movie.rating}" else ""

                Glide.with(this)
                    .load(movie.imageUrl)
                    .into(binding.ivPosterDetail)

                binding.btnWatchNow.setOnClickListener {
                    if (movie.videoUrl.isNotEmpty()) {
                        val bundle = Bundle().apply {
                            putString("videoUrl", movie.videoUrl)
                            putString("movieId", movie.id) // <-- Agregamos esta línea
                        }
                        findNavController().navigate(R.id.action_movieDetailFragment_to_videoPlayerFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Error: No hay multimedia", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        if (movieId.isNotEmpty()) {
            viewModel.fetchMovieData(movieId)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}