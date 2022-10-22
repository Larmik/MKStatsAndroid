package fr.harmoniamk.statsmk.fragment.currentWar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.CurrentPlayerItemBinding
import fr.harmoniamk.statsmk.extension.isTrue

class CurrentPlayerAdapter(private val list: MutableList<CurrentPlayerModel> = mutableListOf()) : RecyclerView.Adapter<CurrentPlayerAdapter.CurrentPlayerViewHolder>() {

    class CurrentPlayerViewHolder(val binding: CurrentPlayerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: CurrentPlayerModel) {
            binding.playerName.text = item.player?.name
            binding.playerScore.text = item.score.toString()
            when {
                item.isOld.isTrue -> {
                    binding.replaceIv.isVisible = true
                    binding.replaceIv.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.lose))
                    binding.replaceIv.rotation = 180f
                }
                item.isNew.isTrue -> {
                    binding.replaceIv.isVisible = true
                    binding.replaceIv.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.green))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentPlayerViewHolder {
        return CurrentPlayerViewHolder(CurrentPlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CurrentPlayerViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount() = list.size

    fun addPlayers(elements: List<CurrentPlayerModel>) {
        notifyItemRangeRemoved(0, itemCount)
        list.clear()
        list.addAll(elements)
        notifyItemRangeInserted(0, itemCount)
    }

}