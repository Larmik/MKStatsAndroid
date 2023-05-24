package fr.harmoniamk.statsmk.fragment.scheduleWar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.ScheduleLineUpItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class ScheduleLineUpAdapter(val list: MutableList<LineUpSelector> = mutableListOf(), val nameList: List<String>? = null) : RecyclerView.Adapter<ScheduleLineUpAdapter.ScheduleLineUpViewHolder>(), CoroutineScope {

    private var totalList = mutableListOf<LineUpSelector>()
    val onPlayerDelete = MutableSharedFlow<LineUpSelector>()

    class ScheduleLineUpViewHolder(val binding: ScheduleLineUpItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LineUpSelector, crossVisible: Boolean) {
            binding.name.text = item.user?.name
            binding.deleteBtn.visibility = when (crossVisible) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }

        }
        fun bind(item: String) {
            binding.name.text = item
            binding.deleteBtn.isVisible = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleLineUpViewHolder =
        ScheduleLineUpViewHolder(ScheduleLineUpItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ScheduleLineUpViewHolder, position: Int) {
        list.getOrNull(position)?.let { item ->
            val crossVisible = when {
                item.dispo == 1 && totalList.filter { it.dispo == 0 }.size < 6 -> true
                item.dispo == 0 && totalList.filter { it.dispo == 0 }.size > 6 -> true
                else -> false
            }
            holder.bind(item, crossVisible && totalList.size > 6)
            holder.binding.deleteBtn.clicks()
                .map { item }
                .bind(onPlayerDelete, this)
        }
        nameList?.getOrNull(position)?.let { item ->
            holder.bind(item)
        }

    }

    override fun getItemCount() = nameList?.size ?: list.size

    fun addUsers(results: List<LineUpSelector>, allPlayers: List<LineUpSelector>) {
        notifyItemRangeRemoved(0, itemCount)
        list.clear()
        list.addAll(results)
        totalList.clear()
        totalList.addAll(allPlayers)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}