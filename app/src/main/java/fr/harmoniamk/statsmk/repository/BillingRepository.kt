package fr.harmoniamk.statsmk.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.compose.viewModel.CoffeePurchaseState
import fr.harmoniamk.statsmk.model.firebase.Coffee
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface BillingRepositoryInterface {
    fun startBilling(activity: Activity, productId: String)
    fun listenPurchases(): Flow<CoffeePurchaseState?>
}

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface BillingRepositoryModule {
    @Singleton

    @Binds
    fun bind(impl: BillingRepository): BillingRepositoryInterface
}

@OptIn(FlowPreview::class)
@ExperimentalCoroutinesApi
class BillingRepository @Inject constructor(
    @ApplicationContext var context: Context,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : BillingRepositoryInterface, CoroutineScope {


    private var client: BillingClient? = null

    override fun listenPurchases(): Flow<CoffeePurchaseState?> = callbackFlow {
        val purchaseListener = PurchasesUpdatedListener { result, purchases ->
            result.takeIf { it.responseCode != BillingResponseCode.USER_CANCELED && it.responseCode != BillingResponseCode.DEVELOPER_ERROR }
                ?.let {
                    trySend(handlePurchase(result, purchases))
                }
        }
        client = BillingClient.newBuilder(context)
            .setListener(purchaseListener)
            .enablePendingPurchases()
            .build()
        client?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val queryPurchaseParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                client?.queryPurchasesAsync(queryPurchaseParams) { result, purchases ->
                    trySend(handlePurchase(result, purchases))
                }
            }

            override fun onBillingServiceDisconnected() {}
        })

        awaitClose { }
    }

    private fun handlePurchase(
        result: BillingResult,
        purchases: List<Purchase>?
    ): CoffeePurchaseState? {
        Log.d("MKDebugOnly", "handlePurchase result: $result")
        if (result.responseCode == BillingResponseCode.OK) {
            when (purchases.isNullOrEmpty()) {
                true -> {
                    if (preferencesRepository.isPendingPurchase) {
                        preferencesRepository.isPendingPurchase = false
                        return CoffeePurchaseState.Error(result.responseCode)
                    }
                }

                else -> {
                    purchases.forEach { purchase ->
                        Log.d("MKDebugOnly", "handlePurchase purchase: $purchase")
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PENDING -> {
                                preferencesRepository.isPendingPurchase = true
                                return CoffeePurchaseState.Pending
                            }
                            Purchase.PurchaseState.PURCHASED -> {
                                preferencesRepository.isPendingPurchase = false
                                val consumeParams =
                                    ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                client?.consumeAsync(consumeParams) { result, string ->
                                }
                                val coffee = Coffee(
                                    userId = authenticationRepository.user?.uid.orEmpty(),
                                    productId = purchase.products.first(),
                                    quantity = purchase.quantity,
                                    date = purchase.purchaseTime
                                )
                                firebaseRepository.writeCoffee(coffee)
                                return CoffeePurchaseState.Success(coffee)

                            }
                        }
                    }
                }
            }
        } else  {
            if (preferencesRepository.isPendingPurchase)
                preferencesRepository.isPendingPurchase = false
            return CoffeePurchaseState.Error(result.responseCode)
        }
        return null
    }

    override fun startBilling(activity: Activity, productId: String) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()
        client?.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
            val productDetailsParamsList = productDetailsList.map {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(it)
                    .build()
            }
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            client?.launchBillingFlow(activity, billingFlowParams)

        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO


}