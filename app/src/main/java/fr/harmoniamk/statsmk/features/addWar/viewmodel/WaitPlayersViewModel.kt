package fr.harmoniamk.statsmk.features.addWar.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import javax.inject.Inject

@HiltViewModel
class WaitPlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    fun bind() {

    }

}