package fr.harmoniamk.statsmk.fragment.home.war

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.BestTournamentItemBinding
import fr.harmoniamk.statsmk.databinding.BestWarItemBinding
import fr.harmoniamk.statsmk.databinding.LastTournamentItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class LastWarAdapter(val items: MutableList<MKWar> = mutableListOf()) :
    RecyclerView.Adapter<LastWarAdapter.LastWarViewHolder>(), CoroutineScope {



    private val _sharedItemClick = MutableSharedFlow<MKWar>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class LastWarViewHolder(val binding: BestWarItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(war: MKWar) {
            binding.nameTv.text = war.name
            binding.totalScoreTv.text = war.displayedScore
            binding.timeTv.text = war.war?.createdDate
            binding.diffMapTv.text = war.displayedAverage
            binding.totalDiffTv.text = war.displayedDiff
            binding.chip.setImageResource(
                when (war.displayedDiff.contains("+")) {
                    true -> R.drawable.checked
                    else -> R.drawable.close
                }
            )
            war.takeIf { it.war?.isOfficial.isTrue }?.let {
                binding.mkuIv.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LastWarViewHolder(
        BestWarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LastWarViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)


    }

    override fun getItemCount() = items.size

    fun addWars(wars: List<MKWar>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(wars)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}