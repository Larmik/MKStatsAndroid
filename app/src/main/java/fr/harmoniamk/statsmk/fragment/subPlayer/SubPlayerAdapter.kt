package fr.harmoniamk.statsmk.fragment.subPlayer

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding
import fr.harmoniamk.statsmk.databinding.SubPlayerItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class SubPlayerAdapter(val list: MutableList<UserSelector> = mutableListOf()) : RecyclerView.Adapter<SubPlayerAdapter.PlayerViewHolder>(), CoroutineScope {

    class PlayerViewHolder(val binding: SubPlayerItemBinding) : ViewHolder(binding.root)

    private val _sharedUserSelected = MutableSharedFlow<UserSelector>()
    val sharedUserSelected = _sharedUserSelected.asSharedFlow()

    var oldPlayerHolder : PlayerViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlayerViewHolder(
            SubPlayerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val item = list[position]
        holder.binding.root.background.mutate().setTint(ContextCompat.getColor(holder.binding.root.context,R.color.transparent_white))
        holder.binding.name.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.harmonia_dark))
        holder.binding.name.text = item.user?.name
        holder.binding.root.clicks()
            .onEach {
                oldPlayerHolder?.binding?.root?.background?.mutate()?.setTint(ContextCompat.getColor(holder.binding.root.context, R.color.transparent_white))
                oldPlayerHolder?.binding?.name?.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.harmonia_dark))
                oldPlayerHolder = holder
                holder.binding.root.background.mutate().setTint(ContextCompat.getColor(holder.binding.root.context, R.color.harmonia_dark))
                holder.binding.name.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.white))
                _sharedUserSelected.emit(item)
            }.launchIn(this)
    }

    override fun getItemCount() = list.size


    fun addUsers(results: List<UserSelector>) {
        notifyItemRangeRemoved(0, itemCount)
        list.clear()
        list.addAll(results)
        notifyItemRangeInserted(0, itemCount)
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

}