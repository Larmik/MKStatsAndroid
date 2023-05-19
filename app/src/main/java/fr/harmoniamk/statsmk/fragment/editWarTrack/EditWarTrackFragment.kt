package fr.harmoniamk.statsmk.fragment.editWarTrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentEditWarTrackBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.trackList.TrackListAdapter
import fr.harmoniamk.statsmk.model.firebase.NewWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditWarTrackFragment(val newWar: NewWar? = null, val index: Int = 0) : BottomSheetDialogFragment(), CoroutineScope {

    lateinit var binding: FragmentEditWarTrackBinding
    private val viewModel: EditWarTrackViewModel by viewModels()

    val onDismiss = MutableSharedFlow<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditWarTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TrackListAdapter()
        binding.editTrackRv.adapter = adapter
        newWar?.let { war ->
            viewModel.bind(war, index, adapter.sharedClick, binding.searchMapEt.onTextChanged())
            viewModel.sharedDismiss.bind(onDismiss, lifecycleScope)
            viewModel.sharedSearchedItems
                .onEach { adapter.addTracks(it) }
                .launchIn(lifecycleScope)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}