package fr.harmoniamk.statsmk.fragment.popup

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentPopupBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.dismiss
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.playerSelect.PlayerListAdapter
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
@Deprecated("Migration Compose")
class PopupFragment(val message: Int? = null, val positiveText: Int? = null, private val negativeText: Int = R.string.back, val editTextHint: Any? = null, val loading: Boolean = false, val playerList: List<UserSelector>? = null, val isFcCode: Boolean = false) : DialogFragment(), CoroutineScope {

    lateinit var binding: FragmentPopupBinding

    val onPositiveClick = MutableSharedFlow<Unit>()
    val onNegativeClick = MutableSharedFlow<Unit>()
    val onTextChange = MutableSharedFlow<String>()
    val onPlayerSelected = MutableSharedFlow<String>()

    // dialog view is created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View  {
        binding = FragmentPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    //dialog view is ready
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.setCancelable(false)
        message?.let {
            binding.popupMessage.isVisible = true
            binding.popupMessage.text = requireContext().getString(message)
        }
        if (isFcCode) {
            binding.popupEt.addTextChangedListener(FourDigitCardFormatWatcher())
            binding.popupEt.inputType = InputType.TYPE_CLASS_PHONE
            binding.popupEt.filters = arrayOf(InputFilter.LengthFilter(14))
        }

        binding.negativeButton.text = requireContext().getString(negativeText)
        flowOf(binding.negativeButton.clicks(), dialog.dismiss().filter { negativeText == R.string.back })
            .flattenMerge()
            .bind(onNegativeClick, lifecycleScope)
        editTextHint?.let {
            when (it) {
                is Int ->  binding.popupEt.hint = requireContext().getString(it)
                is String -> binding.popupEt.hint = it
            }
            binding.popupEt.isVisible = true
            binding.popupEt.requestFocus()
            binding.popupEt.onTextChanged().bind(onTextChange, lifecycleScope)
        }
        playerList?.let {
            val adapter = PlayerListAdapter(singleSelection = true)
            binding.popupRv.adapter = adapter
            adapter.addUsers(it)
            adapter.sharedUserSelected.mapNotNull { it.user?.mid }.bind(onPlayerSelected, this)
        }
        positiveText?.let {
            binding.positiveButton.isVisible = true
            binding.positiveButton.text = requireContext().getString(positiveText)
            binding.positiveButton.clicks().bind(onPositiveClick, lifecycleScope)
        }
        if (loading){
            binding.progress.isVisible = true
            binding.buttonsLayout.isVisible = false
        }
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    class FourDigitCardFormatWatcher : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty() && s.length % 5 == 0) {
                val c = s[s.length - 1]
                if (space == c) {
                    s.delete(s.length - 1, s.length)
                }
            }
            if (s.isNotEmpty() && s.length % 5 == 0) {
                val c = s[s.length - 1]
                // Only if its a digit where there should be a space we insert a space
                if (Character.isDigit(c) && TextUtils.split(
                        s.toString(),
                        space.toString()
                    ).size <= 3
                ) {
                    s.insert(s.length - 1, space.toString())
                }
            }
        }

        companion object {
            private const val space = '-'
        }
    }



}