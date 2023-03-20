package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.PlayerRankingItemBinding
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
class OpponentRankingAdapter(val items: MutableList<OpponentRankingItemViewModel> = mutableListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    CoroutineScope {

    private val _sharedTeamSelected = MutableSharedFlow<OpponentRankingItemViewModel>()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()

    class OpponentRankingViewHolder(val binding: PlayerRankingItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =  OpponentRankingViewHolder(
        PlayerRankingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as? OpponentRankingViewHolder)?.let {
            holder.binding.playerName.text = item.teamName
            holder.binding.playerRankingWarsPlayed.text = item.warsPlayedLabel
            holder.binding.playerRankingWinrate.text = item.winrateLabel
            holder.binding.playerRankingAverage.text = item.averageLabel
            holder.binding.root
                .clicks()
                .onEach { _sharedTeamSelected.emit(item) }
                .launchIn(this)
        }
    }

    override fun getItemCount() = items.size

    fun addTeams(results: List<OpponentRankingItemViewModel>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(results)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}