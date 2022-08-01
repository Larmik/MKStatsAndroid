package fr.harmoniamk.statsmk.fragment.mapRanking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.enums.Maps
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
class MapRankingAdapter(val items: MutableList<Pair<Maps, Pair<Int, Int>>> = mutableListOf()) :
    RecyclerView.Adapter<MapRankingAdapter.MapRankingViewHolder>(), CoroutineScope {

    private val _sharedClick = MutableSharedFlow<Int>()
    val sharedClick = _sharedClick.asSharedFlow()

    @ExperimentalCoroutinesApi
    class MapRankingViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Pair<Maps, Pair<Int, Int>>) {
            binding.mapStats.isVisible = true
            binding.trackIv.clipToOutline = true
            binding.trackIv.setImageResource(track.first.picture)
            binding.shortname.text = track.first.name
            binding.name.setText(track.first.label)
            binding.totalPlayed.text = "Joué ${track.second.first} fois"
            binding.winrate.text = "Win rate: ${track.second.second} %"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MapRankingViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: MapRankingViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
        holder.binding.root.clicks()
            .onEach { _sharedClick.emit(track.first.ordinal) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<Pair<Maps, Pair<Int, Int>>>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}