package com.example.appcineindie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.ItemReviewBinding
import com.example.appcineindie.utils.DateUtils

class ReviewAdapter(
    private var reviews: List<Review> = emptyList(),
    private val currentUserId: String? = null,
    private val listener: ReviewActions
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    interface ReviewActions {
        fun onClick(review: Review)
        fun onDelete(review: Review)
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvReviewUser.text = review.userName
            binding.tvMovieTitleReview.text = review.movieTitle
            binding.tvReviewComment.text = review.comment
            binding.tvReviewRatingText.text = review.rating?.toString() ?: "0.0"
            binding.tvReviewDate.text = DateUtils.formatTimestamp(review.timestamp)
            
            // Usar la lógica del modelo
            if (review.isOwnedBy(currentUserId)) {
                binding.btnDeleteReview.visibility = View.VISIBLE
                binding.btnDeleteReview.setSafeOnClickListener { listener.onDelete(review) }
            } else {
                binding.btnDeleteReview.visibility = View.GONE
            }

            binding.root.setOnClickListener { listener.onClick(review) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}
