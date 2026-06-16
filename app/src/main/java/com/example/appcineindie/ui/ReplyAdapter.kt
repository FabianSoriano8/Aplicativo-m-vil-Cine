package com.example.appcineindie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcineindie.data.Reply
import com.example.appcineindie.databinding.ItemReplyBinding
import com.example.appcineindie.utils.DateUtils

class ReplyAdapter(
    private var replies: List<Reply> = emptyList(),
    private val currentUserId: String? = null,
    private val onDeleteClick: (Reply) -> Unit = {}
) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    inner class ReplyViewHolder(private val binding: ItemReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: Reply) {
            binding.tvReplyUser.text = reply.userName
            binding.tvReplyComment.text = reply.comment
            binding.tvReplyDate.text = DateUtils.formatTimestamp(reply.timestamp)

            if (reply.isOwnedBy(currentUserId)) {
                binding.btnDeleteReply.visibility = View.VISIBLE
                binding.btnDeleteReply.setSafeOnClickListener { onDeleteClick(reply) }
            } else {
                binding.btnDeleteReply.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(replies[position])
    }

    override fun getItemCount(): Int = replies.size

    fun updateData(newReplies: List<Reply>) {
        this.replies = newReplies
        notifyDataSetChanged()
    }
}
