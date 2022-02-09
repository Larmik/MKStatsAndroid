package fr.harmoniamk.statsmk.features.currentWar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.enums.Maps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CurrentWarTrackAdapter(val items: MutableList<WarTrack> = mutableListOf()) :
    RecyclerView.Adapter<CurrentWarTrackAdapter.CurrentTrackViewHolder>(), CoroutineScope {


    class CurrentTrackViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: WarTrack) {
            binding.teamScoreTv.isVisible = true
            track.trackIndex?.let {
                val map = Maps.values()[track.trackIndex]
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
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<WarTrack>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}