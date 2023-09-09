package fr.harmoniamk.statsmk.fragment.scheduleWar

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.Team
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
    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    class TeamListViewHolder(val binding: TeamItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Team, isSelected: Boolean) {
            binding.shortname.text = item.shortName
            binding.name.text = item.name
            when (isSelected) {
                true -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.root.background.mutate().setTint(binding.root.context.getColor(R.color.harmonia_dark))
                        binding.shortname.setTextColor(binding.root.context.getColor(R.color.white))
                        binding.name.setTextColor(binding.root.context.getColor(R.color.white))
                        binding.separator.background.mutate().setTint(binding.root.context.getColor(R.color.white))
                    } else {
                        binding.root.background.mutate().setTint(ContextCompat.getColor(binding.root.context, R.color.harmonia_dark))
                        binding.shortname.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.name.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.separator.background.mutate().setTint(ContextCompat.getColor(binding.root.context, R.color.white))
                    }
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.root.background.mutate().setTint(binding.root.context.getColor(R.color.white))
                        binding.shortname.setTextColor(binding.root.context.getColor(R.color.harmonia_dark))
                        binding.name.setTextColor(binding.root.context.getColor(R.color.harmonia_dark))
                        binding.separator.background.mutate().setTint(binding.root.context.getColor(R.color.harmonia_dark))
                    } else {
                        binding.root.background.mutate().setTint(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.shortname.setTextColor(ContextCompat.getColor(binding.root.context, R.color.harmonia_dark))
                        binding.name.setTextColor(ContextCompat.getColor(binding.root.context, R.color.harmonia_dark))
                        binding.separator.background.mutate().setTint(ContextCompat.getColor(binding.root.context, R.color.harmonia_dark))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamListViewHolder =
        TeamListViewHolder(TeamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TeamListViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedItemPos)
        holder.binding.root.clicks()
            .onEach {
                selectedItemPos = position
                lastItemSelectedPos = if(lastItemSelectedPos == -1)
                    selectedItemPos
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    selectedItemPos
                }
                notifyItemChanged(selectedItemPos)
                _onTeamClick.emit(item)
            }
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