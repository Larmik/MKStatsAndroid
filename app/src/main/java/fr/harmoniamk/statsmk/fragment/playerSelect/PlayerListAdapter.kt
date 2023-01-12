package fr.harmoniamk.statsmk.fragment.playerSelect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerCategoryItemBinding
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class PlayerListAdapter(val items: MutableList<UserSelector> = mutableListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {
    private val ITEM_CATEGORY = -155

    private val _sharedUserSelected = MutableSharedFlow<UserSelector>()
    val sharedUserSelected = _sharedUserSelected.asSharedFlow()

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)


    @FlowPreview
    class PlayersCategoryViewHolder(val binding: PlayerCategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int =
        if (items[position].isCategory) ITEM_CATEGORY
        else position


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        ITEM_CATEGORY -> PlayersCategoryViewHolder(PlayerCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as? PlayerViewHolder)?.let {
            holder.binding.separator.isVisible = false
            holder.binding.playerPos.isVisible = false
            holder.binding.root.background.mutate().setTint(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected.isTrue) R.color.harmonia_dark else R.color.transparent_white))
            holder.binding.name.setTextColor(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected.isTrue) R.color.white else R.color.harmonia_dark))
            holder.binding.name.text = items[position].user?.name
            holder.binding.root.clicks()
                .onEach {
                    item.isSelected = !item.isSelected.isTrue
                    holder.binding.root.background.mutate().setTint(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected.isTrue) R.color.harmonia_dark else R.color.transparent_white))
                    holder.binding.name.setTextColor(ContextCompat.getColor(holder.binding.root.context, if (item.isSelected.isTrue) R.color.white else R.color.harmonia_dark))
                    _sharedUserSelected.emit(item)
                }.launchIn(this)
        }
        (holder as? PlayersCategoryViewHolder)?.let {
            it.binding.categoryName.text = when (position == 0) {
                true -> "Equipe"
                else -> "Allys"
            }
        }

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