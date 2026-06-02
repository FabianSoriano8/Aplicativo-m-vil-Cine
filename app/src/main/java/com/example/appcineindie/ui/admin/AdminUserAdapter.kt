package com.example.appcineindie.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcineindie.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private var users: List<Map<String, Any>> = emptyList(),
    private val onChangeType: (String, String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Map<String, Any>) {
            val id = user["id"] as String
            val name = user["name"] as? String ?: "No Name"
            val type = user["type"] as? String ?: "spectator"

            binding.tvUserName.text = name
            binding.tvUserType.text = "Type: $type"
            
            binding.btnChangeType.setOnClickListener {
                val newType = if (type == "admin") "spectator" else "admin"
                onChangeType(id, newType)
            }
            binding.btnDeleteUser.setOnClickListener { onDelete(id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<Map<String, Any>>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}