package fr.harmoniamk.statsmk.fragment.dispos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.PlayerDispoItemBinding

class PlayerDispoAdapter(val list: List<String>) : RecyclerView.Adapter<PlayerDispoAdapter.PlayerDispoViewHolder>() {

    inner class PlayerDispoViewHolder(val binding: PlayerDispoItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.name.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerDispoViewHolder = PlayerDispoViewHolder(
        PlayerDispoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: PlayerDispoViewHolder, position: Int) {
        holder.bind(list[position])
    }

}