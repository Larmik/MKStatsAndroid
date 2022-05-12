package fr.harmoniamk.statsmk.features.currentWar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CurrentWarTrackAdapter(val items: MutableList<MKWarTrack> = mutableListOf()) :
    RecyclerView.Adapter<CurrentWarTrackAdapter.CurrentTrackViewHolder>(), CoroutineScope {

    private val _sharedClick = MutableSharedFlow<Pair<Int, NewWarTrack>>()
    val sharedClick = _sharedClick.asSharedFlow()

    class CurrentTrackViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: MKWarTrack) {
            binding.teamScoreTv.isVisible = true
            binding.root.background.setTint(ContextCompat.getColor(binding.root.context, track.backgroundColor))
            track.track?.trackIndex?.let {
                val map = Maps.values()[it]
                binding.trackIv.clipToOutline = true
                binding.trackIv.setImageResource(map.picture)
                binding.trackScore.text = track.displayedResult
                binding.trackDiff.text = track.displayedDiff
                binding.shortname.text = map.name
                binding.name.setText(map.label)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CurrentTrackViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: CurrentTrackViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        item.track?.let { tr ->
            holder.binding.root.clicks()
                .onEach { _sharedClick.emit(Pair(position+1, tr)) }
                .launchIn(this)
        }
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<MKWarTrack>) {
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