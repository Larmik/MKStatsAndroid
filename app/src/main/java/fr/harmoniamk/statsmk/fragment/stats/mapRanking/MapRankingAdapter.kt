package fr.harmoniamk.statsmk.fragment.stats.mapRanking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.local.TrackStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MapRankingAdapter(val items: MutableList<TrackStats> = mutableListOf()) :
    RecyclerView.Adapter<MapRankingAdapter.MapRankingViewHolder>(), CoroutineScope {

    private val _sharedClick = MutableSharedFlow<Int>()
    val sharedClick = _sharedClick.asSharedFlow()

    @ExperimentalCoroutinesApi
    class MapRankingViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: TrackStats) {
            track.map?.let {
                binding.mapStats.isVisible = true
                binding.trackIv.clipToOutline = true
                binding.trackIv.setImageResource(it.picture)
                binding.shortname.text = it.name
                binding.name.setText(it.label)
                binding.totalPlayed.text = "Joué ${track.totalPlayed} fois"
                binding.winrate.text = "Win rate: ${track.winRate} %"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MapRankingViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: MapRankingViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
        holder.binding.root.clicks()
            .mapNotNull { track.map?.ordinal }
            .bind(_sharedClick, this)
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<TrackStats>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}