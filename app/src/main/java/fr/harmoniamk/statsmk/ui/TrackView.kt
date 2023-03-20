package fr.harmoniamk.statsmk.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.databinding.TrackItemCollapsedBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.pointsToPosition
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.model.local.MKTournamentTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats

class TrackView : LinearLayout {

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

    fun bind(track: Any?, shouldDisplayPosition: Boolean = false) {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.setMargins(0,0,0,0)
        (binding as? TrackItemBinding)?.let { binding ->
            binding.root.layoutParams = lp
            binding.trackIv.clipToOutline = true
            when (track) {
                is MKWarTrack -> {
                    binding.teamScoreTv.isVisible = true
                    binding.root.background.mutate().setTint(
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
                is MKTournamentTrack -> {
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
                    binding.averageTrackScoreLabel.isVisible = false
                    binding.averageTrackScore.isVisible = false
                    binding.root.background.mutate().setTint(
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
                is TrackStats -> {
                    binding.bestTrackScore.isVisible = false
                    binding.bestTrackDiff.isVisible = false
                    binding.totalPlayed.isVisible = true
                    binding.root.background.mutate().setTint(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.transparent
                        )
                    )
                    track.map?.let {
                        binding.bestTrackIv.setImageResource(it.picture)
                        binding.bestTrackName.setText(it.label)
                    }
                        binding.averageTrackScore.typeface = ResourcesCompat.getFont(context, when (shouldDisplayPosition) {
                            true -> R.font.mk_position
                            else -> R.font.orbitron_semibold
                        })
                        binding.averageTrackScore.text = track.teamScore.toString()

                        if (shouldDisplayPosition) {
                            val position = track.playerScore.pointsToPosition()
                            binding.averageTrackScoreLabel.text = "Position moyenne"
                            binding.averageTrackScore.text = position.toString()
                            binding.averageTrackScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                            binding.averageTrackScore.setTextColor(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    context.getColor(position.positionColor())
                                else ContextCompat.getColor(context, position.positionColor())
                            )
                        }
                        binding.totalPlayed.text = "jouée ${track.totalPlayed} fois"

                }
                else -> {
                    binding.bestTrackIv.isVisible = false
                    binding.bestTrackName.isVisible = false
                    binding.averageTrackScoreLabel.text = "Aucune donnée."
                }
            }
        }

    }
}