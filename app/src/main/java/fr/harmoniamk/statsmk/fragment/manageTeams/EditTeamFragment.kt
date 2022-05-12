package fr.harmoniamk.statsmk.fragment.manageTeams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.databinding.FragmentEditTeamBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class EditTeamFragment(val team: Team? = null) : BottomSheetDialogFragment(), CoroutineScope {

    lateinit var binding: FragmentEditTeamBinding

    val onTeamEdit = MutableSharedFlow<Team>()
    val onTeamDelete = MutableSharedFlow<Team>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.teamnameEt.setText(team?.name)
        binding.teamcodeLayout.isVisible = team?.accessCode != "null" && !team?.accessCode.isNullOrEmpty()
        binding.teamcodeEt.setText(team?.accessCode)
        binding.teamshortEt.setText(team?.shortName)
        binding.nextBtn.clicks().mapNotNull {
            team.apply {
                this?.name = binding.teamnameEt.text.toString()
                this?.shortName = binding.teamshortEt.text.toString()
                this?.accessCode = binding.teamcodeEt.text.toString()
            }
        }.bind(onTeamEdit, this)
        binding.deleteBtn.clicks().mapNotNull { team }.bind(onTeamDelete, this)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}