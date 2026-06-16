package com.example.appcineindie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcineindie.R
import com.example.appcineindie.data.Reply
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.FragmentReviewDetailBinding
import com.example.appcineindie.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewDetailFragment : Fragment() {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewDetailViewModel by viewModels()
    private var reviewId: String = ""
    private lateinit var adapter: ReplyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewDetailBinding.inflate(inflater, container, false)
        reviewId = arguments?.getString("reviewId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        if (reviewId.isNotEmpty()) viewModel.listenForReview(reviewId)

        binding.btnBack.setSafeOnClickListener { findNavController().navigateUp() }
        binding.btnSendReply.setSafeOnClickListener {
            val comment = binding.etReply.text.toString().trim()
            if (comment.isNotEmpty()) sendReply(comment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ReplyAdapter(
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
            onDeleteClick = { reply ->
                showConfirmDialog("Eliminar respuesta", "¿Seguro que quieres eliminar tu respuesta?") {
                    viewModel.deleteReply(reviewId, reply, {
                        Toast.makeText(requireContext(), "Respuesta eliminada", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        )
        binding.rvReplies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReplies.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.review.observe(viewLifecycleOwner) { review ->
            review?.let {
                bindReview(it)
                adapter.updateData(it.replies)
            }
        }
    }

    private fun bindReview(review: Review) {
        val rb = binding.includedReview
        rb.tvReviewUser.text = review.userName
        rb.tvMovieTitleReview.text = review.movieTitle
        rb.tvReviewComment.text = review.comment
        rb.tvReviewRatingText.text = review.rating?.toString() ?: "0.0"
        rb.tvReviewDate.text = DateUtils.formatTimestamp(review.timestamp)
        
        if (review.isOwnedBy(FirebaseAuth.getInstance().currentUser?.uid)) {
            rb.btnDeleteReview.visibility = View.VISIBLE
            rb.btnDeleteReview.setSafeOnClickListener {
                showConfirmDialog("Eliminar reseña", "¿Quieres eliminar tu reseña?") {
                    FirebaseFirestore.getInstance().collection("reviews").document(review.id).delete().addOnSuccessListener {
                        Toast.makeText(requireContext(), "Reseña eliminada", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        } else {
            rb.btnDeleteReview.visibility = View.GONE
        }
    }

    private fun sendReply(comment: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val reply = Reply(
                userId = user.uid,
                userName = doc.getString("name") ?: "Usuario",
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            viewModel.addReply(reviewId, reply, {
                binding.etReply.setText("")
                Toast.makeText(requireContext(), "Respuesta enviada", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(requireContext(), "Error al enviar", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
