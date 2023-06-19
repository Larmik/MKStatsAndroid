package fr.harmoniamk.statsmk.fragment.currentWar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.PenaltyItemBinding
import fr.harmoniamk.statsmk.model.firebase.Penalty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class PenaltyAdapter(private val list: MutableList<Penalty> = mutableListOf()) : RecyclerView.Adapter<PenaltyAdapter.PenaltyViewHolder>(), CoroutineScope {

    class PenaltyViewHolder(val binding: PenaltyItemBinding) : ViewHolder(binding.root) {
        fun bind(item: Penalty) {
            binding.teamName.text = item.teamName
            binding.amount.text = String.format(binding.root.context.getString(R.string.minus_placeholder), item.amount.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaltyViewHolder = PenaltyViewHolder(
        PenaltyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: PenaltyViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

}