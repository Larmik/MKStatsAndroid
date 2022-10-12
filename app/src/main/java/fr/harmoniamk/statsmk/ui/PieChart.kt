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
    fun bind(winrate: String) {
        binding?.winrateProgressbar?.progress = 100 - (winrate.split(" ")[0].toIntOrNull() ?: 0)
        binding?.winrateText?.text = winrate
    }
}