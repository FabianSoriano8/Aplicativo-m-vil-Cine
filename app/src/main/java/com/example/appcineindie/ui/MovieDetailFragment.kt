package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appcineindie.R
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.DialogAddReviewBinding
import com.example.appcineindie.databinding.FragmentMovieDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val reviewsViewModel: ReviewsViewModel by viewModels()
    private lateinit var reviewsAdapter: ReviewAdapter

    private var currentMovie: Movie? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getString("movieId") ?: ""
        setupReviewsRecyclerView()

        viewModel.featuredMovie.observe(viewLifecycleOwner) { movie ->
            movie?.let { movieItem ->
                currentMovie = movieItem
                binding.tvTitleDetail.text = movieItem.title
                binding.tvDescriptionDetail.text = movieItem.description
                binding.tvMetadataDetail.text = "${movieItem.category} • ${movieItem.duration} \n ${movieItem.releaseYear} • Dirigido por: ${movieItem.director} "
                binding.tvRatingDetail.text = if (movieItem.rating.isNotEmpty()) "★ ${movieItem.rating}" else ""

                Glide.with(this).load(movieItem.imageUrl).into(binding.ivPosterDetail)

                binding.btnWatchNow.setSafeOnClickListener {
                    if (movieItem.videoUrl.isNotEmpty()) {
                        val bundle = bundleOf("videoUrl" to movieItem.videoUrl, "movieId" to movieItem.id)
                        findNavController().navigate(R.id.action_movieDetailFragment_to_videoPlayerFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "No hay multimedia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (movieId.isNotEmpty()) {
            viewModel.fetchMovieData(movieId)
            reviewsViewModel.listenForReviews(movieId)
        }

        reviewsViewModel.reviewsList.observe(viewLifecycleOwner) { reviewsAdapter.updateData(it) }

        setSafeClick(binding.btnBack, binding.btnAddReviewDetail, binding.btnTrailer) { view ->
            when (view.id) {
                R.id.btnBack -> findNavController().navigateUp()
                R.id.btnAddReviewDetail -> currentMovie?.let { showAddReviewDialog(it) }
                R.id.btnTrailer -> currentMovie?.let { movie ->
                    if (movie.trailerUrl.isNotEmpty()) {
                        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(movie.trailerUrl)))
                    } else {
                        Toast.makeText(requireContext(), "Trailer no disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupReviewsRecyclerView() {
        reviewsAdapter = ReviewAdapter(
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
            listener = object : ReviewAdapter.ReviewActions {
                override fun onClick(review: Review) {
                    findNavController().navigate(R.id.reviewDetailFragment, bundleOf("reviewId" to review.id))
                }
                override fun onDelete(review: Review) {
                    showConfirmDialog("Eliminar reseña", "¿Deseas eliminar tu comentario?") {
                        reviewsViewModel.deleteReview(review, {
                            Toast.makeText(requireContext(), "Reseña eliminada", Toast.LENGTH_SHORT).show()
                        }, { e ->
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        )
        binding.rvReviewsDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun showAddReviewDialog(movie: Movie) {
        val db = DialogAddReviewBinding.inflate(layoutInflater)
        db.spinnerMovies.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(db.root).create()

        db.btnSubmitReview.setSafeOnClickListener {
            val rating = db.dialogRatingBar.rating
            val comment = db.etComment.text.toString().trim()
            if (rating < 1.0f || comment.isEmpty()) {
                Toast.makeText(requireContext(), "Datos incompletos", Toast.LENGTH_SHORT).show()
                return@setSafeOnClickListener
            }
            submitReview(movie, rating, comment)
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
            reviewsViewModel.addReview(review, {
                Toast.makeText(requireContext(), "Reseña publicada", Toast.LENGTH_SHORT).show()
            }, { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
