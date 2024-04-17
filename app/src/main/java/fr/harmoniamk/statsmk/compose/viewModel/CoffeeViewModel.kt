package fr.harmoniamk.statsmk.compose.viewModel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.displayedString
import fr.harmoniamk.statsmk.extension.getActivity
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

    val isGod = authenticationRepository.userRole == 3


    private var rewardedAd: RewardedAd? = null


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

    fun showAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context,"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("MKDebugOnly", adError.toString())
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("MKDebugOnly", "Ad was loaded.")
                rewardedAd = ad
                rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("MKDebugOnly", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("MKDebugOnly", "Ad dismissed fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when ad fails to show.
                        Log.e("MKDebugOnly", "Ad failed to show fullscreen content.")
                        rewardedAd = null
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("MKDebugOnly", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("MKDebugOnly", "Ad showed fullscreen content.")
                    }
                }
                context.getActivity()?.takeIf { rewardedAd != null }?.let { activity ->
                    ad.show(activity) { rewardItem ->
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        Log.d("MKDebugOnly", "User earned $rewardAmount $rewardType")
                    }
                } ?: run {
                    Log.d("MKDebugOnly", "The rewarded ad wasn't ready yet.")
                }
            }
        })
    }

}