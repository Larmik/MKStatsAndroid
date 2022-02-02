package fr.harmoniamk.statsmk.features.home.tournament

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.room.model.Tournament
import fr.harmoniamk.statsmk.databinding.BestTournamentItemBinding
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
class BestTournamentAdapter(val items: MutableList<Tournament> = mutableListOf()) :
    RecyclerView.Adapter<BestTournamentAdapter.BestTournamentViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<Tournament>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class BestTournamentViewHolder(val binding: BestTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tournament: Tournament, position: Int) {
            binding.nameTv.text = tournament.name
            binding.totalScoreTv.text = tournament.points.toString()
            binding.timeTv.text = tournament.updatedDate
            binding.tmInfos.text = tournament.infos
            binding.ratioScoreTv.text = tournament.ratio.toString()
            binding.trophy.setImageResource(
                when (position) {
                    0 -> R.drawable.gold
                    1 -> R.drawable.silver
                    else -> R.drawable.bronze
                }
            )
            binding.topScoreTv.text = tournament.tops.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BestTournamentViewHolder(
        BestTournamentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: BestTournamentViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTournaments(tm: List<Tournament>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tm)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}