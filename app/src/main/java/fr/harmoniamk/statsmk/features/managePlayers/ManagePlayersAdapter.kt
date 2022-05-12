package fr.harmoniamk.statsmk.features.managePlayers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.databinding.ManagePlayersItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class ManagePlayersAdapter(private val items: MutableList<ManagePlayersItemViewModel> = mutableListOf()) : RecyclerView.Adapter<ManagePlayersAdapter.ManagePlayersViewHolder>(), CoroutineScope {

    val sharedEdit = MutableSharedFlow<User>()

    @FlowPreview
    class ManagePlayersViewHolder(val binding: ManagePlayersItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: ManagePlayersItemViewModel) {
            binding.name.text = player.name
            binding.checkmark.visibility = player.checkmarkVisibility
            binding.editBtn.visibility = player.buttonsVisibility
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagePlayersViewHolder =
        ManagePlayersViewHolder(ManagePlayersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ManagePlayersViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.editBtn.clicks().map { item.player }.bind(sharedEdit, this)
    }

    fun addPlayers(players: List<ManagePlayersItemViewModel>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(players)
        notifyItemRangeInserted(0, itemCount)
    }

    override fun getItemCount() = items.size

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}