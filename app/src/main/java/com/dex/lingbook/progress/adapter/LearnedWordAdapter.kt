package com.dex.lingbook.progress.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dex.lingbook.databinding.ItemLearnedWordBinding
import com.dex.lingbook.model.Vocabulary

class LearnedWordAdapter(
    private var words: List<Vocabulary>,
    private val onItemClick: (Vocabulary) -> Unit
) : RecyclerView.Adapter<LearnedWordAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLearnedWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLearnedWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = words[position]
        holder.binding.tvWord.text = word.word
        holder.binding.tvDefinition.text = word.definition

        holder.itemView.setOnClickListener {
            onItemClick(word)
        }
    }

    override fun getItemCount(): Int = words.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newWords: List<Vocabulary>) {
        words = newWords
        notifyDataSetChanged()
    }
}
