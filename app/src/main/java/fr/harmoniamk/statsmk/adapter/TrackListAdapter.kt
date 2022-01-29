package fr.harmoniamk.statsmk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
class TrackListAdapter(val items: MutableList<Maps> = Maps.values().toMutableList()) :
    RecyclerView.Adapter<TrackListAdapter.TrackListViewHolder>(), CoroutineScope {

    val _sharedClick = MutableSharedFlow<Maps>()
    val sharedClick = _sharedClick.asSharedFlow()

    @ExperimentalCoroutinesApi
    class TrackListViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Maps) {
            binding.trackIv.clipToOutline = true
            binding.trackIv.setImageResource(track.picture)
            binding.cupIv.setImageResource(track.cup.picture)
            binding.shortname.text = track.name
            binding.name.setText(track.label)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrackListViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: TrackListViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
        holder.binding.root.clicks()
            .onEach { _sharedClick.emit(track) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<Maps>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}