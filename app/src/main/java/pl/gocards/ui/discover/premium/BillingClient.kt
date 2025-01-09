package pl.gocards.ui.discover.premium

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * https://developer.android.com/google/play/billing/integrate
 *
 * @author Grzegorz Ziemski
 */
class BillingClient(
    private val premiumViewModel: PremiumViewModel,
    private val context: Context,
    private val scope: CoroutineScope
) {

    companion object {
        const val PRODUCT_ID = "premium"
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            purchases?.forEach {
                scope.launch {
                    handlePurchase(it)
                }
            }
        }

    private val client: BillingClient = getBillingClient()

    private val formattedPrice = mutableStateOf("")

    private fun getBillingClient(): BillingClient {
        val billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .setListener(purchasesUpdatedListener)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        checkIfPremiumSubscriptionIsActive()
                        getSubscription()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }

        })

        return billingClient
    }

    private val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {}

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.products[0] == PRODUCT_ID) {
            premiumViewModel.enablePremium()
            premiumViewModel.reset()
            acknowledgePurchase(purchase)
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                withContext(Dispatchers.IO) {
                    client.acknowledgePurchase(
                        acknowledgePurchaseParams.build(),
                        acknowledgePurchaseResponseListener
                    )
                }
            }
        }
    }

    suspend fun launch(activity: Activity) {
        premiumViewModel.reset()
        val details = getProductDetails() ?: return
        val subscriptionOfferDetails = details.subscriptionOfferDetails?.get(0) ?: return

        val pricingPhase = subscriptionOfferDetails.pricingPhases.pricingPhaseList[0]
        formattedPrice.value = pricingPhase.formattedPrice

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(subscriptionOfferDetails.offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        client.launchBillingFlow(activity, billingFlowParams)
    }

    private suspend fun getProductDetails(): ProductDetails? {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = client.queryProductDetails(params.build())
        val productDetailsList = productDetailsResult.productDetailsList ?: return null

        return if (productDetailsList.isNotEmpty()) {
            productDetailsList[0]
        } else {
            null
        }
    }

    private suspend fun getSubscription() {
        val details = getProductDetails() ?: return
        val subscriptionOfferDetails = details.subscriptionOfferDetails?.get(0) ?: return

        val pricingPhase = subscriptionOfferDetails.pricingPhases.pricingPhaseList[0]
        formattedPrice.value = pricingPhase.formattedPrice
    }

    private suspend fun checkIfPremiumSubscriptionIsActive() {
        val isPremium = isPremiumSubscriptionActive()
        premiumViewModel.savePremium(isPremium)
        premiumViewModel.reset()
    }

    private suspend fun isPremiumSubscriptionActive(): Boolean {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        val purchasesResult = client.queryPurchasesAsync(params.build())
        val purchasesList = purchasesResult.purchasesList
        for (purchase in purchasesList) {
            for (product in purchase.products) {
                if (PRODUCT_ID == product) {
                    return true
                }
            }
        }
        return false
    }

    fun getFormattedPrice(): State<String> {
        return formattedPrice
    }
}