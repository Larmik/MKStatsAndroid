package fr.harmoniamk.statsmk.fragment.popup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import fr.harmoniamk.statsmk.databinding.FragmentPopupBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalCoroutinesApi
class PopupFragment(val message: String, val positiveText: String? = null, private val negativeText: String = "Retour", val editTextHint: String? = null, val loading: Boolean = false) : DialogFragment() {

    lateinit var binding: FragmentPopupBinding

    val onPositiveClick = MutableSharedFlow<Unit>()
    val onNegativeClick = MutableSharedFlow<Unit>()
    val onTextChange = MutableSharedFlow<String>()

    // dialog view is created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View  {
        binding = FragmentPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    //dialog view is ready
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.popupMessage.text = message
        binding.negativeButton.text = negativeText
        binding.negativeButton.clicks().bind(onNegativeClick, lifecycleScope)
        editTextHint?.let {
            binding.popupEt.hint = it
            binding.popupEt.isVisible = true
            binding.popupEt.requestFocus()
            binding.popupEt.onTextChanged().bind(onTextChange, lifecycleScope)
        }
        positiveText?.let {
            binding.positiveButton.isVisible = true
            binding.positiveButton.text = positiveText
            binding.positiveButton.clicks().bind(onPositiveClick, lifecycleScope)
        }
        if (loading){
            binding.progress.isVisible = true
            binding.buttonsLayout.isVisible = false
        }
    }

    fun setLoading(text: String) {
        binding.progress.isVisible = true
        binding.popupMessage.text = text
        binding.buttonsLayout.isVisible = false
    }


}