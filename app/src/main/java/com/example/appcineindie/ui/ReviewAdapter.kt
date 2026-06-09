package com.example.appcineindie.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp

class ReviewAdapter(private var reviews: List<Review> = emptyList()) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvReviewUser.text = review.userName
            binding.tvMovieTitleReview.text = review.movieTitle
            binding.tvReviewComment.text = review.comment
            binding.tvReviewRatingText.text = review.rating?.toString() ?: "0.0"
            binding.tvReviewDate.text = formatTimestamp(review.timestamp)
        }
    }

    private fun formatTimestamp(timestamp: Any?): String {
        return try {
            val millis = when (timestamp) {
                is String -> timestamp.toLongOrNull() ?: 0L
                is Long -> timestamp
                is Timestamp -> timestamp.toDate().time
                else -> 0L
            }
            if (millis == 0L) return ""

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(millis))
        } catch (e: Exception) {
            ""
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