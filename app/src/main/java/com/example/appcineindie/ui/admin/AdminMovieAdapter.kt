package com.example.appcineindie.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcineindie.data.Movie
import com.example.appcineindie.databinding.ItemAdminMovieBinding

class AdminMovieAdapter(
    private var movies: List<Movie> = emptyList(),
    private val onEdit: (Movie) -> Unit,
    private val onDelete: (Movie) -> Unit
) : RecyclerView.Adapter<AdminMovieAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieCategory.text = "Category: ${movie.category}"
            binding.tvMovieGender.text = "Genres: ${movie.genres.joinToString(", ")}"
            
            Glide.with(binding.ivMoviePoster.context)
                .load(movie.imageUrl)
                .into(binding.ivMoviePoster)

            binding.btnEditMovie.setOnClickListener { onEdit(movie) }
            binding.btnDeleteMovie.setOnClickListener { onDelete(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun updateData(newMovies: List<Movie>) {
        this.movies = newMovies
        notifyDataSetChanged()
    }
}