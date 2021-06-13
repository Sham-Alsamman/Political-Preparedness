package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ElectionListItemBinding
import com.example.android.politicalpreparedness.network.models.Election

class ElectionListAdapter(private val clickListener: ElectionListener) : ListAdapter<Election, ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val election = getItem(position)
        holder.bind(election, clickListener)
    }
}

// Create ElectionViewHolder
class ElectionViewHolder(private val binding: ElectionListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(election: Election, clickListener: ElectionListener) {
        // Bind ViewHolder
        binding.election = election
        binding.listener = clickListener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ElectionViewHolder {
            return ElectionViewHolder(ElectionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }
}

// Create ElectionDiffCallback
class ElectionDiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem == newItem
    }

}

// Create ElectionListener
class ElectionListener (val clickListener: (election: Election) -> Unit){
    fun onclick(election: Election) = clickListener(election)
}