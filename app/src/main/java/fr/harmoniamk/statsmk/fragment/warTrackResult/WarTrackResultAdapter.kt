package fr.harmoniamk.statsmk.fragment.warTrackResult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding
import fr.harmoniamk.statsmk.model.local.MKWarPosition

class WarTrackResultAdapter(val items: MutableList<MKWarPosition> = mutableListOf()) : RecyclerView.Adapter<WarTrackResultAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder =
        PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.binding.name.setTextColor(
            ContextCompat.getColor(
                holder.binding.root.context,
                R.color.black
            )
        )
        holder.binding.separator.isVisible = true
        holder.binding.playerPos.isVisible = true
        holder.binding.name.text = items[position].playerName
        holder.binding.playerPos.text = items[position].position.position.toString()
    }

    override fun getItemCount() = items.size

    fun addResults(results: List<MKWarPosition>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(results.sortedBy { it.position.position })
        notifyItemRangeInserted(0, itemCount)
    }
}