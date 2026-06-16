package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
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
    private lateinit var adapter: ReviewAdapter
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
        setupClickListeners()
        setupSortSpinner()

        viewModel.fetchMovies()
        viewModel.listenForReviews("")
        binding.etSearchReviews.addTextChangedListener { text ->
            viewModel.filterReviews(text.toString())
        }
    }

    private fun setupSortSpinner() {
        val sortAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerSort.adapter = sortAdapter
        binding.spinnerSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSortCriterion(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.navHome.setSafeOnClickListener {
            lifecycleScope.launch {
                if (sessionManager.userType.first() == "spectator") findNavController().navigate(R.id.homeFragment)
            }
        }
        binding.navProfile.setSafeOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
        binding.navSearch.setSafeOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
        binding.btnSendReview.setSafeOnClickListener {
            showAddReviewDialog()
        }
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        if (moviesList.isEmpty()) viewModel.fetchMovies()

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, moviesList.map { it.title })
        dialogBinding.spinnerMovies.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmitReview.setSafeOnClickListener {
            val pos = dialogBinding.spinnerMovies.selectedItemPosition
            val rating = dialogBinding.dialogRatingBar.rating
            val comment = dialogBinding.etComment.text.toString().trim()

            if (pos < 0 || rating < 1.0f || comment.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setSafeOnClickListener
            }

            submitReview(moviesList[pos], rating, comment)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun submitReview(movie: Movie, rating: Float, comment: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val review = Review(
                userId = user.uid,
                userName = doc.getString("name") ?: user.displayName ?: "Usuario",
                movieId = movie.id,
                movieTitle = movie.title,
                comment = comment,
                rating = rating.toString(),
                timestamp = System.currentTimeMillis()
            )
            viewModel.addReview(review, {
                Toast.makeText(requireContext(), "Reseña publicada", Toast.LENGTH_SHORT).show()
            }, { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
            listener = object : ReviewAdapter.ReviewActions {
                override fun onClick(review: Review) {
                    findNavController().navigate(R.id.reviewDetailFragment, bundleOf("reviewId" to review.id))
                }
                override fun onDelete(review: Review) {
                    showConfirmDialog("Eliminar reseña", "¿Quieres eliminar tu reseña de ${review.movieTitle}?") {
                        viewModel.deleteReview(review, {
                            Toast.makeText(requireContext(), "Reseña eliminada", Toast.LENGTH_SHORT).show()
                        }, { e ->
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        )
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReviewsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.reviewsList.observe(viewLifecycleOwner) { adapter.updateData(it) }
        viewModel.moviesList.observe(viewLifecycleOwner) { moviesList = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
