package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentEditRoleBinding
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.checks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditRoleFragment(private val currentRole: Int?) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentEditRoleBinding
    private val viewModel: EditRoleViewModel by viewModels()

    val onRoleChange = MutableSharedFlow<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (currentRole) {
            UserRole.ADMIN.ordinal -> binding.roleRg.check(R.id.admin)
            UserRole.MEMBER.ordinal -> binding.roleRg.check(R.id.member)
            UserRole.LEADER.ordinal -> binding.roleRg.check(R.id.leader)
        }
        viewModel.bind(binding.roleRg.checks())
        viewModel.sharedRole.filterNotNull().bind(onRoleChange, lifecycleScope)
    }


}