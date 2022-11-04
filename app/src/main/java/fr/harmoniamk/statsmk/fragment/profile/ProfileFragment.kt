package fr.harmoniamk.statsmk.fragment.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentProfileBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.setImageURL
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URL
import java.util.concurrent.Executors


@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {

    private val binding: FragmentProfileBinding by viewBinding()
    private val viewModel: ProfileViewModel by viewModels()
    private val _onPictureSave = MutableSharedFlow<String>()
    private val disconnectPopup by lazy { PopupFragment("Êtes-vous sûr de vouloir vous déconnecter ?", "Se déconnecter") }
    var isPicking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profilePic.clipToOutline = true

        viewModel.bind(
            onPictureClick = binding.changePictureBtn.clicks(),
            onPictureEdited = _onPictureSave,
            onPopup = flowOf(binding.disconnectBtn.clicks().map { true }, disconnectPopup.onNegativeClick.map { false }).flattenMerge(),
            onChangePasswordClick = binding.changePasswordBtn.clicks(),
            onLogout = disconnectPopup.onPositiveClick,
            onChangeNameClick = binding.changeNameBtn.clicks(),
            onChangeEmailClick = binding.changeEmailBtn.clicks()
        )

        viewModel.sharedTeam
            .filterNotNull()
            .onEach { binding.teamTv.text = it }
            .launchIn(lifecycleScope)

        viewModel.sharedRole
            .onEach { binding.roleTv.text = it }
            .launchIn(lifecycleScope)


        viewModel.sharedProfile
            .onEach {
                binding.username.text = it.displayName
                binding.email.text = it.email
                binding.profilePic.setImageURL(it.photoUrl.toString())
            }.launchIn(lifecycleScope)

        viewModel.sharedPictureLoaded
            .onEach { binding.profilePic.setImageURL(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedEditPicture
            .filterNot { isPicking }
            .onEach {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, 456)
                isPicking = true
            }.launchIn(lifecycleScope)

        viewModel.showResetPopup
            .onEach { text ->
                val popup = PopupFragment(message = text)
                popup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        viewModel.sharedDisconnect
            .filter { findNavController().currentDestination?.id == R.id.profileFragment }
            .onEach {
                disconnectPopup.dismiss()
                findNavController().navigate(ProfileFragmentDirections.backToWelcome())
            }.launchIn(lifecycleScope)

        viewModel.sharedDisconnectPopup
            .onEach {
                when (it) {
                    true -> disconnectPopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                    else -> disconnectPopup.dismiss()
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedEditName
            .onEach {
                val changeNamePopup = PopupFragment(
                    message = "Modifier le pseudo",
                    positiveText = "Enregistrer",
                    editTextHint = binding.username.text.toString()
                )
                viewModel.bindDialog(false, changeNamePopup.onTextChange, changeNamePopup.onPositiveClick, changeNamePopup.onNegativeClick)
                changeNamePopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                viewModel.sharedNewName
                    .onEach {
                        changeNamePopup.dismiss()
                        binding.username.text = it
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedEditEmail
            .onEach {
                val changeEmailPopup = PopupFragment(
                    message = "Modifier l'adresse mail",
                    positiveText = "Enregistrer",
                    editTextHint = binding.email.text.toString()
                )
                viewModel.bindDialog(true, changeEmailPopup.onTextChange, changeEmailPopup.onPositiveClick, changeEmailPopup.onNegativeClick)
                changeEmailPopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                viewModel.sharedNewName
                    .onEach {
                        changeEmailPopup.dismiss()
                        binding.email.text = it
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isPicking = false
        if (requestCode == 456 && resultCode == Activity.RESULT_OK) {
            flowOf(data?.data?.toString())
                .filterNotNull()
                .bind(_onPictureSave, lifecycleScope)
        }
    }

}