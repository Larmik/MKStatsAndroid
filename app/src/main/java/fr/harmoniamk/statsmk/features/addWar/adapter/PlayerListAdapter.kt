package fr.harmoniamk.statsmk.features.addWar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.addWar.UserSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class PlayerListAdapter(val items: MutableList<UserSelector> = mutableListOf()) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>(), CoroutineScope {

    private val _sharedUserSelected = MutableSharedFlow<UserSelector>()
    val sharedUserSelected = _sharedUserSelected.asSharedFlow()

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder =
        PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val item = items[position]
        holder.binding.separator.isVisible = false
        holder.binding.playerPos.isVisible = false
        holder.binding.root.background.setTint(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected) R.color.luigi else R.color.white))
        holder.binding.name.text = items[position].user.name
        holder.binding.root.clicks()
            .onEach {
                item.isSelected = !item.isSelected
                holder.binding.root.background.setTint(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected) R.color.luigi else R.color.white))
                _sharedUserSelected.emit(item)
            }.launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addUsers(results: List<UserSelector>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(results)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}