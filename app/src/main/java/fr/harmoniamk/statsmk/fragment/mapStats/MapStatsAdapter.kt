package fr.harmoniamk.statsmk.fragment.mapStats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MapStatsAdapter(val items: MutableList<Pair<MKWar, MKWarTrack>> = mutableListOf()) :
    RecyclerView.Adapter<MapStatsAdapter.MapStatsViewHolder>(), CoroutineScope {

    val onMapClick = MutableSharedFlow<Pair<MKWar, MKWarTrack>>()

    class MapStatsViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Pair<MKWar, MKWarTrack>) {
            binding.teamScoreTv.isVisible = true
            binding.root.background.mutate().setTint(ContextCompat.getColor(binding.root.context, track.second.backgroundColor))
            track.second.track?.trackIndex?.let {
                binding.trackIv.isVisible = false
                binding.trackScore.text = track.second.displayedResult
                binding.trackDiff.text = track.second.displayedDiff
                binding.shortname.text = track.first.war?.createdDate
                binding.name.text = track.first.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MapStatsViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: MapStatsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root
            .clicks()
            .onEach { onMapClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<Pair<MKWar, MKWarTrack>>) {
        if (tracks.size != itemCount) {
            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(tracks)
            notifyItemRangeInserted(0, itemCount)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}