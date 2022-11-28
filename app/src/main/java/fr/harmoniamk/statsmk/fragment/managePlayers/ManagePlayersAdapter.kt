package fr.harmoniamk.statsmk.fragment.managePlayers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.databinding.ManagePlayersItemBinding
import fr.harmoniamk.statsmk.databinding.PlayerCategoryItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class ManagePlayersAdapter(private val items: MutableList<ManagePlayersItemViewModel> = mutableListOf(), private val showAlly: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {
    private val ITEM_CATEGORY = -155

    val sharedEdit = MutableSharedFlow<User>()

    override fun getItemViewType(position: Int): Int =
         if (items[position].isCategory) ITEM_CATEGORY
         else position


    @FlowPreview
    inner class ManagePlayersViewHolder(val binding: ManagePlayersItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: ManagePlayersItemViewModel) {
            binding.name.text = player.name
            binding.checkmark.visibility = player.checkmarkVisibility
            player.buttonsVisibility
                ?.onEach { binding.editBtn.visibility = it }
                ?.launchIn(this@ManagePlayersAdapter)
        }
    }

    @FlowPreview
    class PlayersCategoryViewHolder(val binding: PlayerCategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_CATEGORY -> PlayersCategoryViewHolder(PlayerCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> ManagePlayersViewHolder(ManagePlayersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as? ManagePlayersViewHolder)?.let {
            it.bind(item)
            it.binding.editBtn.clicks().mapNotNull { item.player }.bind(sharedEdit, this)
        }
        (holder as? PlayersCategoryViewHolder)?.let {
            it.binding.categoryName.text = when (position == 0) {
                true -> "Equipe (${items.filterNot { it.isAlly }.size})"
                else -> "Allys"
            }
        }

    }

    fun addPlayers(players: List<ManagePlayersItemViewModel>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(players.filterNot { it.isCategory }.filter { (it.isAlly && showAlly) || !it.isAlly  })
        notifyItemRangeInserted(0, itemCount)
    }

    override fun getItemCount() = items.size

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}