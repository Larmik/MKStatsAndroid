package fr.harmoniamk.statsmk.fragment.warTrackResult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class WarTrackResultAdapter(val items: MutableList<MKWarPosition> = mutableListOf(), val shockLayoutVisible: Boolean = true, val positionVisible: Boolean = true) : RecyclerView.Adapter<WarTrackResultAdapter.PlayerViewHolder>(), CoroutineScope {

    val onShockAdded = MutableSharedFlow<String>()
    val onShockRemoved = MutableSharedFlow<String>()

    class PlayerViewHolder(val binding: PlayerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder =
        PlayerViewHolder(PlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val context = holder.binding.root.context
        holder.binding.root.setPadding(0, 20, 0,20)
        holder.binding.root.background.setTint(ContextCompat.getColor(context, R.color.transparent_white))
        holder.binding.name.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.black
            )
        )
        holder.binding.playerPos.isVisible = positionVisible
        holder.binding.shockLayout.isVisible = shockLayoutVisible
        holder.binding.name.text = items[position].player?.name
        holder.binding.playerPos.text = items[position].position.position.toString()
        holder.binding.playerPos.setTextColor(ContextCompat.getColor(context, items[position].position.position.positionColor()))
        holder.binding.addShock.clicks()
            .mapNotNull { items[position].player?.mid }
            .bind(onShockAdded, this)
        holder.binding.removeShock.clicks()
            .mapNotNull { items[position].player?.mid }
            .bind(onShockRemoved, this)
    }

    override fun getItemCount() = items.size

    fun addResults(results: List<MKWarPosition>) {
        if (results.sortedBy { it.position.mid }.map { it.position.toString() } != items.sortedBy { it.position.mid }.map { it.position.toString() }) {
            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(results.sortedBy { it.position.position })
            notifyItemRangeInserted(0, itemCount)
        }

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}