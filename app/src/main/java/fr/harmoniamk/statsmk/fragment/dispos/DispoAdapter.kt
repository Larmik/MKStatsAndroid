package fr.harmoniamk.statsmk.fragment.dispos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.DispoItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
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

    @FlowPreview
    @ExperimentalCoroutinesApi
    inner class DispoViewHolder(val binding: DispoItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarDispo) {
            val canAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.CAN.ordinal }?.playerNames.orEmpty())
            val canSubAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.CAN_SUB.ordinal }?.playerNames.orEmpty())
            val notSureAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.NOT_SURE.ordinal }?.playerNames.orEmpty())
            val cantAdapter = PlayerDispoAdapter(item.dispoPlayers.singleOrNull { it.dispo == Dispo.CANT.ordinal }?.playerNames.orEmpty())
            binding.hour.text = "${item.dispoHour}h"
            binding.canList.adapter = canAdapter
            binding.canSubList.adapter = canSubAdapter
            binding.notSureList.adapter = notSureAdapter
            binding.cantList.adapter = cantAdapter
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