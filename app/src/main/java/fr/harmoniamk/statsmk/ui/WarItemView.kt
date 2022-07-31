package fr.harmoniamk.statsmk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.WarItemBinding
import fr.harmoniamk.statsmk.databinding.WarItemVerticalBinding
import fr.harmoniamk.statsmk.model.local.MKWar

class WarItemView : LinearLayout {

    private var binding: ViewBinding? = null
    private var isGreen = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WarItemView, 0, 0)
        binding = when (ta.getBoolean(R.styleable.WarItemView_vertical, false)) {
            true -> WarItemVerticalBinding.inflate(LayoutInflater.from(context), this, true)
            else -> WarItemBinding.inflate(LayoutInflater.from(context), this, true)
        }
        isGreen = ta.getBoolean(R.styleable.WarItemView_win, false)
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
            it.root.background.setTint(
                ContextCompat.getColor(
                    it.root.context,
                    if (isGreen) R.color.win_alphaed else R.color.lose_alphaed
                )
            )
        }
    }

}