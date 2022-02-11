package fr.harmoniamk.statsmk.features.addWar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.databinding.TeamItemBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class TeamListAdapter(val items: MutableList<Team> = mutableListOf()) : RecyclerView.Adapter<TeamListAdapter.TeamListViewHolder>(), CoroutineScope {

    private val _onTeamClick = MutableSharedFlow<Team>()
    val onTeamClick = _onTeamClick.asSharedFlow()

    class TeamListViewHolder(val binding: TeamItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Team) {
            binding.shortname.text = item.shortName
            binding.name.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamListViewHolder =
        TeamListViewHolder(TeamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TeamListViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.root.clicks()
            .onEach { _onTeamClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTeams(teams: List<Team>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(teams)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

}