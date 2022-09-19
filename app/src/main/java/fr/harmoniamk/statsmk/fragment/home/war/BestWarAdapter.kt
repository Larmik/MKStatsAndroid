package fr.harmoniamk.statsmk.fragment.home.war

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.BestTournamentItemBinding
import fr.harmoniamk.statsmk.databinding.LastTournamentItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class BestWarAdapter(val items: MutableList<MKWar> = mutableListOf()) :
    RecyclerView.Adapter<BestWarAdapter.BestWarViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<MKWar>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class BestWarViewHolder(val binding: LastTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(war: MKWar) {
            binding.tmTotal.isVisible = false
            binding.warTotal.isVisible = true
            binding.nameTv.text = war.name
            binding.totalWarScoreTv.text = war.displayedScore
            binding.timeTv.text = war.war?.createdDate?.replace("-", "\n")
            binding.warDiff.text = war.displayedDiff
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BestWarViewHolder(
        LastTournamentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: BestWarViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)


    }

    override fun getItemCount() = items.size

    fun addWars(wars: List<MKWar>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(wars)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}