package fr.harmoniamk.statsmk.fragment.home.tournament

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.local.MKTournament
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
class BestTournamentAdapter(val items: MutableList<MKTournament> = mutableListOf()) :
    RecyclerView.Adapter<BestTournamentAdapter.BestTournamentViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<MKTournament>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class BestTournamentViewHolder(val binding: BestTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(MKTournament: MKTournament, position: Int) {
            binding.nameTv.text = MKTournament.name
            binding.totalScoreTv.text = MKTournament.points.toString()
            binding.timeTv.text = MKTournament.updatedDate
            binding.tmInfos.text = MKTournament.infos
            binding.ratioScoreTv.text = MKTournament.ratio.toString()
            binding.trophy.setImageResource(
                when (position) {
                    0 -> R.drawable.gold
                    1 -> R.drawable.silver
                    else -> R.drawable.bronze
                }
            )
            binding.topScoreTv.text = MKTournament.tops.toString()
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

    fun addTournaments(tm: List<MKTournament>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tm)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}