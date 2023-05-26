package fr.harmoniamk.statsmk.fragment.dispos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.DispoItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.scheduleWar.ScheduleLineUpAdapter
import fr.harmoniamk.statsmk.model.firebase.Dispo
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class DispoAdapter(val list: MutableList<WarDispo> = mutableListOf()) : RecyclerView.Adapter<DispoAdapter.DispoViewHolder>(), CoroutineScope {

    val sharedDispoSelected = MutableSharedFlow<Pair<WarDispo, Dispo>>()
    val onClickWarSchedule = MutableSharedFlow<WarDispo>()
    val onClickOtherPlayer = MutableSharedFlow<WarDispo>()

    @FlowPreview
    @ExperimentalCoroutinesApi
    inner class DispoViewHolder(val binding: DispoItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarDispo) {
            val playersCan = item.dispoPlayers.singleOrNull { it.dispo == Dispo.CAN.ordinal }?.playerNames.orEmpty()
            val playersCanSub = item.dispoPlayers.singleOrNull { it.dispo == Dispo.CAN_SUB.ordinal }?.playerNames.orEmpty()
            val canAdapter = PlayerDispoAdapter(playersCan)
            val canSubAdapter = PlayerDispoAdapter(playersCanSub)
            val notSureAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.NOT_SURE.ordinal }?.playerNames.orEmpty())
            val cantAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.CANT.ordinal }?.playerNames.orEmpty())
            val firstHalfLuAdapter = ScheduleLineUpAdapter(nameList = item.lineupNames?.safeSubList(0, 3))
            val secondHalfLuAdapter = ScheduleLineUpAdapter(nameList = item.lineupNames?.safeSubList(3, 6))
            binding.hour.text = String.format(binding.root.context.getString(R.string.hour_placeholder), item.dispoHour.toString())
            binding.canList.adapter = canAdapter
            binding.canSubList.adapter = canSubAdapter
            binding.notSureList.adapter = notSureAdapter
            binding.cantList.adapter = cantAdapter
            binding.firstHalfLu.adapter = firstHalfLuAdapter
            binding.secondHalfLu.adapter = secondHalfLuAdapter
            binding.btnSchedule.isVisible = playersCan.size + playersCanSub.size >= 6
            binding.addOtherPlayerBtn.isVisible = playersCan.size + playersCanSub.size < 6
            item.lineUp?.let {
                binding.dispoListLayout.isVisible = false
                binding.lineupLayout.isVisible = true
                binding.btnSchedule.isVisible = false
            }
            item.opponentName?.let {
                binding.warName.isVisible = true
                binding.warName.text = it
            }
            binding.btnSchedule.clicks()
                .map { item }
                .bind(onClickWarSchedule, this@DispoAdapter)

            binding.addOtherPlayerBtn.clicks()
                .map { item }
                .bind(onClickOtherPlayer, this@DispoAdapter)
            flowOf(
                binding.canBtn.clicks().map { Dispo.CAN },
                binding.canSubBtn.clicks().map { Dispo.CAN_SUB },
                binding.notSureBtn.clicks().map { Dispo.NOT_SURE },
                binding.cantBtn.clicks().map { Dispo.CANT },
            ).flattenMerge()
                .map { Pair(item, it) }
                .bind(sharedDispoSelected, this@DispoAdapter)
        }
    }

    fun addData(dispos: List<WarDispo>) {
        when (dispos.size != list.size) {
            true -> {
                notifyItemRangeRemoved(0, itemCount)
                list.clear()
                list.addAll(dispos)
                notifyItemRangeInserted(0, itemCount)
            }
            else -> {
                list.clear()
                list.addAll(dispos)
                notifyItemRangeChanged(0, itemCount)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispoViewHolder = DispoViewHolder(
        DispoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DispoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.count()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}