package com.example.appcineindie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcineindie.R
import com.example.appcineindie.data.Movie

class MovieAdapter(
    private val movies: List<Movie>,
    private val layoutResId: Int,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPoster: ImageView = view.findViewById(R.id.ivMoviePoster)
        val tvTitle: TextView = view.findViewById(R.id.tvMovieTitle)
        val tvRating: TextView? = view.findViewById(R.id.tvRating)
        val tvRemainingTime: TextView? = view.findViewById(R.id.tvRemainingTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.tvTitle.text = movie.title
        
        holder.tvRating?.text = if (movie.rating.isNotEmpty()) "★ ${movie.rating}" else ""
        holder.tvRemainingTime?.text = movie.remainingTime

        Glide.with(holder.itemView.context)
            .load(movie.imageUrl)
            .placeholder(android.R.drawable.progress_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.ivPoster)

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size
}