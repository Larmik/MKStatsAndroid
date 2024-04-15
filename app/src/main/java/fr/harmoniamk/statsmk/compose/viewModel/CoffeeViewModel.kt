package fr.harmoniamk.statsmk.compose.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.displayedString
import fr.harmoniamk.statsmk.model.firebase.Coffee
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.BillingRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Date
import javax.inject.Inject

sealed class CoffeePurchaseState(val message: String) {
    class Success(val coffee: Coffee) : CoffeePurchaseState("Café bien reçu ! Merci infiniment du soutien.")
    object Pending : CoffeePurchaseState("Il y a du monde au comptoir pour récupérer le café. Il sera disponible dans quelques minutes.")
    class Error(code: Int) : CoffeePurchaseState("Une erreur est survenue lors de la distribution du café. Veuillez réessayer plus tard. \n \n Votre achat a été annulé. \n \n Code d'erreur : $code")
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val billingRepository: BillingRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _total = MutableStateFlow<Int?>(null)
    private val _ownTotal = MutableStateFlow<Int?>(null)
    private val _lastCoffee = MutableStateFlow<Pair<String, String>?>(null)
    private val _lastOwnCoffee = MutableStateFlow<String?>(null)
    private val _coffeeUsersList = MutableStateFlow<List<Pair<String, Int>>>(listOf())

    val total = _total.asStateFlow()
    val ownTotal = _ownTotal.asStateFlow()
    val lastCoffee = _lastCoffee.asStateFlow()
    val lastOwnCoffee = _lastOwnCoffee.asStateFlow()
    val coffeeUsersList = _coffeeUsersList.asStateFlow()

    init {
        val users = mutableListOf<User>()
        firebaseRepository.getUsers()
            .onEach { users.addAll(it) }
            .launchIn(viewModelScope)

        firebaseRepository.listenCoffees()
            .onEach { coffees ->
                var total = 0
                var ownTotal = 0
                val finalUsersList = mutableListOf<Pair<String, Int>>()
                coffees.forEach {
                    when (it.productId) {
                        "a_coffee" -> {
                            total += it.quantity
                            if (it.userId == authenticationRepository.user?.uid)
                                ownTotal += it.quantity
                        }
                        "three_coffees" -> {
                            total += it.quantity * 3
                            if (it.userId == authenticationRepository.user?.uid)
                                ownTotal += it.quantity * 3
                        }
                        "five_coffees" -> {
                            total += it.quantity * 5
                            if (it.userId == authenticationRepository.user?.uid)
                                ownTotal += it.quantity * 5
                        }
                        "ten_coffees" -> {
                            total += it.quantity * 10
                            if (it.userId == authenticationRepository.user?.uid)
                                ownTotal += it.quantity * 10
                        }
                    }
                }
                preferencesRepository.coffees = ownTotal
                _total.value = total
                _ownTotal.value = ownTotal
                coffees.maxByOrNull { it.date }?.let { coffee ->
                    val user = users.singleOrNull { it.mid == coffee.userId }
                    _lastCoffee.value = Pair(user?.name.orEmpty(), Date(coffee.date).displayedString("dd/MM/yyyy"))
                }
                coffees.filter { it.userId == authenticationRepository.user?.uid }.maxByOrNull { it.date }?.let { coffee ->
                    _lastOwnCoffee.value =  Date(coffee.date).displayedString("dd/MM/yyyy")
                }
                coffees.groupBy { it.userId }.forEach { map ->
                    val userName = users.singleOrNull { it.mid == map.key }?.name.orEmpty()
                    var totalForUser = 0
                    map.value.forEach { coffee ->
                        when (coffee.productId) {
                            "a_coffee" -> totalForUser += coffee.quantity
                            "three_coffees" -> totalForUser += coffee.quantity * 3
                            "five_coffees" -> totalForUser += coffee.quantity * 5
                            "ten_coffees" -> totalForUser += coffee.quantity * 10
                        }
                    }
                    finalUsersList.add(Pair(userName, totalForUser))
                }
                _coffeeUsersList.value = finalUsersList.sortedByDescending { it.second }
            }.launchIn(viewModelScope)
    }


    fun startBilling(activity: Activity, productId: String) = billingRepository.startBilling(activity, productId)

}