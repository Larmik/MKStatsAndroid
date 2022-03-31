package fr.harmoniamk.statsmk.features.managePlayers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.model.User
import fr.harmoniamk.statsmk.databinding.ManagePlayersItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.MKWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class ManagePlayersAdapter(private val items: MutableList<User> = mutableListOf()) : RecyclerView.Adapter<ManagePlayersAdapter.ManagePlayersViewHolder>(), CoroutineScope {

    val sharedDelete = MutableSharedFlow<User>()

    class ManagePlayersViewHolder(val binding: ManagePlayersItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: User) {
            binding.name.text = player.name
            binding.checkmark.visibility = when (player.accessCode) {
                "null" -> View.INVISIBLE
                else -> View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagePlayersViewHolder =
        ManagePlayersViewHolder(ManagePlayersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ManagePlayersViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.deleteBtn.clicks().map { item }.bind(sharedDelete, this)
    }

    fun addPlayers(players: List<User>) {
        if (players.size != itemCount) {
            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(players)
            notifyItemRangeInserted(0, itemCount)
        }
    }

    override fun getItemCount() = items.size

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}