package fr.harmoniamk.statsmk.fragment.stats.mapStats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.model.local.MapDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MapStatsAdapter(val items: MutableList<MapDetails> = mutableListOf(), val userId: String? = null) :
    RecyclerView.Adapter<MapStatsAdapter.MapStatsViewHolder>(), CoroutineScope {

    val onMapClick = MutableSharedFlow<MapDetails>()

    inner class MapStatsViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: MapDetails) {
            binding.teamScoreTv.isVisible = true
            binding.shockIv.isVisible = false
            binding.root.background.mutate().setTint(ContextCompat.getColor(binding.root.context, track.warTrack.backgroundColor))
            val isIndiv = track.position != null
            track.warTrack.track?.trackIndex?.let {
                binding.trackIv.isVisible = false
                binding.trackScore.text = track.warTrack.displayedResult
                binding.trackDiff.text = track.warTrack.displayedDiff
                binding.shortname.text = track.war.war?.createdDate
                binding.name.text = track.war.name
                track.position?.let {
                    binding.root.background.mutate().setTint(ContextCompat.getColor(binding.root.context, R.color.white_alphaed))
                    binding.trackScore.isVisible = false
                    binding.trackDiff.isVisible = false
                    binding.mapPos.isVisible = true
                    binding.mapPos.text = it.toString()
                    binding.mapPos.setTextColor(ContextCompat.getColor(binding.root.context, it.positionColor()))
                }
                if (!isIndiv && track.warTrack.track.shocks?.isNotEmpty().isTrue || isIndiv && track.warTrack.track.shocks?.any { it.playerId == this@MapStatsAdapter.userId }.isTrue)
                    binding.shockIv.isVisible = true
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

    fun addTracks(tracks: List<MapDetails>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}