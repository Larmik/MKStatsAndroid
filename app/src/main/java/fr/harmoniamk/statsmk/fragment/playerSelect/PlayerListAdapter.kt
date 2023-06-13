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
class PlayerListAdapter(val items: MutableList<UserSelector> = mutableListOf(), val singleSelection: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {

    private val _sharedUserSelected = MutableSharedFlow<UserSelector>()
    val sharedUserSelected = _sharedUserSelected.asSharedFlow()

    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)


    @FlowPreview
    class PlayersCategoryViewHolder(val binding: PlayerCategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int =
        if (items[position].isCategory) -155
        else position


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        -155 -> PlayersCategoryViewHolder(PlayerCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as? PlayerViewHolder)?.let {
            val context = it.binding.root.context
            it.binding.playerPos.isVisible = false
            it.binding.name.text = item.user?.name
            when (singleSelection) {
                false -> {
                    it.binding.root.background.mutate().setTint(ContextCompat.getColor(context, if (item.isSelected.isTrue) R.color.harmonia_dark else R.color.transparent_white))
                    it.binding.name.setTextColor(ContextCompat.getColor(context, if (item.isSelected.isTrue) R.color.white else R.color.harmonia_dark))
                }
                else -> {
                    it.binding.root.background.mutate().setTint(ContextCompat.getColor(context, if (position == selectedItemPos) R.color.harmonia_dark else R.color.transparent_white))
                    it.binding.name.setTextColor(ContextCompat.getColor(context, if (position == selectedItemPos) R.color.white else R.color.harmonia_dark))
                }
            }
            it.binding.root.clicks()
                .onEach {
                    when (singleSelection) {
                        false -> {
                            item.isSelected = !item.isSelected.isTrue
                            holder.binding.root.background.mutate().setTint(ContextCompat.getColor(context, if (item.isSelected.isTrue) R.color.harmonia_dark else R.color.transparent_white))
                            holder.binding.name.setTextColor(ContextCompat.getColor(context, if (item.isSelected.isTrue) R.color.white else R.color.harmonia_dark))
                        }
                        else -> {
                            selectedItemPos = position
                            lastItemSelectedPos = if(lastItemSelectedPos == -1)
                                selectedItemPos
                            else {
                                notifyItemChanged(lastItemSelectedPos)
                                selectedItemPos
                            }
                            notifyItemChanged(selectedItemPos)
                        }
                    }
                    _sharedUserSelected.emit(item)
                }.launchIn(this)
        }
        (holder as? PlayersCategoryViewHolder)?.let {
            val context = it.binding.root.context
            it.binding.categoryName.text = when (position == 0) {
                true -> context.getString(R.string.equipe)
                else -> context.getString(R.string.allys)
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