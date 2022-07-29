package fr.harmoniamk.statsmk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.viewbinding.ViewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.WarItemBinding
import fr.harmoniamk.statsmk.databinding.WarItemVerticalBinding
import fr.harmoniamk.statsmk.model.local.MKWar

class WarItemView : CardView {

    private var binding: ViewBinding? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WarItemView, 0, 0)
        binding = when (ta.getBoolean(R.styleable.WarItemView_vertical, false)) {
            true -> WarItemVerticalBinding.inflate(LayoutInflater.from(context), this, true)
            else -> WarItemBinding.inflate(LayoutInflater.from(context), this, true)
        }
        ta.recycle()
    }

    fun bind(war: MKWar?) {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.setMargins(0,0,0,0)
        (binding as? WarItemVerticalBinding)?.let {
            it.totalWarScoreTv.text = war?.displayedScore
            it.warDiff.text = war?.displayedDiff
            it.nameTv.text = war?.war?.name
            it.timeTv.text = war?.war?.createdDate
        }
    }

}