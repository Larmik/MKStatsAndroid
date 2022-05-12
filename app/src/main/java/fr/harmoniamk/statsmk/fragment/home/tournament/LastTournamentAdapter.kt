package fr.harmoniamk.statsmk.fragment.home.tournament

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.model.local.MKTournament
import fr.harmoniamk.statsmk.databinding.LastTournamentItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class LastTournamentAdapter(val items: MutableList<MKTournament> = mutableListOf()) :
    RecyclerView.Adapter<LastTournamentAdapter.BestTournamentViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<MKTournament>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class BestTournamentViewHolder(val binding: LastTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(MKTournament: MKTournament) {
            binding.warTotal.isVisible = false
            binding.tmTotal.isVisible = true
            binding.nameTv.text = MKTournament.name
            binding.totalScoreTv.text = MKTournament.points.toString()
            binding.timeTv.text = MKTournament.createdDate.replace("-", "\n")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BestTournamentViewHolder(
        LastTournamentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: BestTournamentViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTournaments(tm: List<MKTournament>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tm)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}