package com.example.appcineindie.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcineindie.data.Review
import com.example.appcineindie.databinding.ItemAdminReviewBinding

class AdminReviewAdapter(
    private var reviews: List<Review> = emptyList(),
    private val onDelete: (Review) -> Unit
) : RecyclerView.Adapter<AdminReviewAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.tvUser.text = review.userName
            binding.tvMovie.text = "Movie: ${review.movieTitle}"
            binding.tvComment.text = review.comment
            binding.btnDeleteReview.setOnClickListener { onDelete(review) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}