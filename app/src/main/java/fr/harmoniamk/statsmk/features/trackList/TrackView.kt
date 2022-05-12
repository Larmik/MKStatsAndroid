package fr.harmoniamk.statsmk.features.trackList

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.databinding.TrackItemCollapsedBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.local.MKWarTrack

class TrackView : CardView {

    private var binding: ViewBinding? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TrackView, 0, 0)
        binding = when (ta.getBoolean(R.styleable.TrackView_collapsed, false)) {
            true -> TrackItemCollapsedBinding.inflate(LayoutInflater.from(context), this, true)
            else -> TrackItemBinding.inflate(LayoutInflater.from(context), this, true)
        }
        ta.recycle()
    }

    fun bind(track: Any?) {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.setMargins(0,0,0,0)
        (binding as? TrackItemBinding)?.let { binding ->
            binding.root.layoutParams = lp
            binding.root.cardElevation = 0f
            binding.trackIv.clipToOutline = true
            when (track) {
                is MKWarTrack -> {
                    binding.teamScoreTv.isVisible = true
                    binding.root.background.setTint(
                        ContextCompat.getColor(
                            binding.root.context,
                            track.backgroundColor
                        )
                    )
                    track.track?.trackIndex?.let {
                        val map = Maps.values()[it]
                        binding.trackIv.setImageResource(map.picture)
                        binding.trackScore.text = track.displayedResult
                        binding.trackDiff.text = track.displayedDiff
                        binding.shortname.text = map.name
                        binding.name.setText(map.label)
                    }
                }
                is PlayedTrack -> {
                    binding.posTv.isVisible = true
                    val map = Maps.values()[track.trackIndex]
                    binding.trackIv.setImageResource(map.picture)
                    binding.posTv.text = track.displayedPos
                    binding.shortname.text = map.name
                    binding.name.setText(map.label)
                }
                is Int -> {
                    val map = Maps.values()[track]
                    binding.cupIv.isVisible = true
                    binding.trackIv.setImageResource(map.picture)
                    binding.cupIv.setImageResource(map.cup.picture)
                    binding.shortname.text = map.name
                    binding.name.setText(map.label)
                }
                else -> binding.name.text = "Données corrompues"
            }
        }
        (binding as? TrackItemCollapsedBinding)?.let { binding ->
            binding.bestTrackIv.clipToOutline = true
            when (track) {
                is MKWarTrack -> {
                    binding.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            track.backgroundColor
                        )
                    )
                    track.track?.trackIndex?.let {
                        val map = Maps.values()[it]
                        binding.bestTrackIv.setImageResource(map.picture)
                        binding.bestTrackScore.text = track.displayedResult
                        binding.bestTrackDiff.text = track.displayedDiff
                        binding.bestTrackName.setText(map.label)
                    }
                }
                else -> binding.bestTrackName.text = "Données corrompues"            }
        }

    }
}