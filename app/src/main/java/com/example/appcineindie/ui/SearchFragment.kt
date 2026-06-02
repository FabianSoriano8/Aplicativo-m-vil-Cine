package com.example.appcineindie.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.R
import com.example.appcineindie.data.SessionManager
import com.example.appcineindie.databinding.FragmentSearchBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.filteredMovies.observe(viewLifecycleOwner) { movies ->
            binding.rvSearchResults.adapter = MovieAdapter(movies, R.layout.item_movie_trending) { movie ->
                val bundle = bundleOf("movieId" to movie.id)
                findNavController().navigate(R.id.movieDetailFragment, bundle)
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterMovies(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.fetchAllMovies()

        // Bottom Nav Logic
        binding.navHome.setOnClickListener {
            lifecycleScope.launch {
                val type = sessionManager.userType.first()
                if (type == "cinephile") {
                    findNavController().navigate(R.id.reviewsFragment)
                } else {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }

        binding.navProfile.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}