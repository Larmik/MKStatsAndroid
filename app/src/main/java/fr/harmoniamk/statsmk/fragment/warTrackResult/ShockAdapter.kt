package fr.harmoniamk.statsmk.fragment.warTrackResult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PlayerShockItemBinding
import fr.harmoniamk.statsmk.model.firebase.Shock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class ShockAdapter(val items: MutableList<Pair<String?, Shock>> = mutableListOf()) : RecyclerView.Adapter<ShockAdapter.ShockViewHolder>(), CoroutineScope {

    class ShockViewHolder(val binding: PlayerShockItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShockViewHolder =
        ShockViewHolder(
            PlayerShockItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ShockViewHolder, position: Int) {
        val item = items[position]
        (holder as? ShockViewHolder)?.let {
            it.binding.playerName.text = item.first
            it.binding.count.text = String.format(it.binding.root.context.getString(R.string.shock_count_placeholder), item.second.count.toString())
        }
    }

    override fun getItemCount() = items.size

    fun addItems(list: List<Pair<String?, Shock>>) {
        if (list.sortedBy { it.second.playerId }.map { it.second.toString() } != items.sortedBy { it.second.playerId }.map { it.second.toString() }) {

            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(list.sortedBy { it.first })
            notifyItemRangeInserted(0, itemCount)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}