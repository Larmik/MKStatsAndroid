package fr.harmoniamk.statsmk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import fr.harmoniamk.statsmk.databinding.PiechartBinding

class PieChart : FrameLayout {

    private var binding: PiechartBinding? = null


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        binding = PiechartBinding.inflate(LayoutInflater.from(context), this, true)
    }


    fun bind(win: Int, tie: Int, lose: Int) {
        val total = win+tie+lose
        binding?.backgroundProgressbar?.max = total
        binding?.backgroundProgressbar?.progress = total

        binding?.winrateProgressbar?.max = total
        binding?.winrateProgressbar?.progress = total - win


        binding?.tieProgressbar?.max = total
        binding?.tieProgressbar?.progress = tie

        binding?.winrateText?.text = "${(win*100)/total} %"
    }
}