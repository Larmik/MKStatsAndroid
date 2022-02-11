package fr.harmoniamk.statsmk.features.home.war.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.databinding.LastTournamentItemBinding
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
class LastWarAdapter(val items: MutableList<War> = mutableListOf()) :
    RecyclerView.Adapter<LastWarAdapter.LastWarViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<War>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class LastWarViewHolder(val binding: LastTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(war: War) {
            binding.tmTotal.isVisible = false
            binding.warTotal.isVisible = true
            binding.nameTv.text = war.name
            binding.totalWarScoreTv.text = war.displayedScore
            binding.timeTv.text = war.createdDate?.replace("-", "\n")
            binding.warDiff.text = war.displayedDiff
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LastWarViewHolder(
        LastTournamentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LastWarViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addWars(wars: List<War>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(wars)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}