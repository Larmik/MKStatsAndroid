package fr.harmoniamk.statsmk.fragment.allWars

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.WarItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class AllWarsAdapter(private val items: MutableList<MKWar> = mutableListOf()) : RecyclerView.Adapter<AllWarsAdapter.WarViewHolder>(), CoroutineScope {

    val sharedItemClick = MutableSharedFlow<MKWar>()

    class WarViewHolder(val binding: WarItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(war: MKWar) {
            binding.nameTv.text = war.name
            binding.timeTv.text = war.war?.createdDate
            binding.totalScoreTv.text = war.displayedScore
            binding.totalDiffTv.text = war.displayedDiff
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WarViewHolder(
        WarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: WarViewHolder, position: Int) {
        val item = items[position]
        holder.binding.root.clicks()
            .onEach { sharedItemClick.emit(item) }
            .launchIn(this)
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun addWars(wars: List<MKWar>) {
        if (wars.size != itemCount) {
            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(wars)
            notifyItemRangeInserted(0, itemCount)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

}