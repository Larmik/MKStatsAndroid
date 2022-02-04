package fr.harmoniamk.statsmk.features.addWar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.firebase.model.User
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding

class PlayerListAdapter(val items: MutableList<User> = mutableListOf()) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder =
        PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.binding.name.text = items[position].name
    }

    override fun getItemCount() = items.size

    fun addOrRemovePlayers(players: List<User>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(players)
        notifyItemRangeInserted(0, itemCount)
    }
}