package com.hedvig.app.feature.offer

import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.feature.offer.ui.checkoutLabel
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionresult.GetDataCollectionResultUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MockOfferViewModel : OfferViewModel() {
    init {
        load()
    }

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    override val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override suspend fun triggerOpenChat() = Unit

    override fun onOpenQuoteDetails(
        id: String,
    ) {
        val quote = mockData.offer.quoteBundle.quotes.first { it.id == id }
        _events.trySend(
            Event.OpenQuoteDetails(
                QuoteDetailItems(
                    quote.displayName,
                    buildPerils(quote),
                    buildInsurableLimits(quote),
                    buildDocuments(quote),
                )
            )
        )
    }

    override fun approveOffer() {
        _events.trySend(
            Event.ApproveSuccessful(LocalDate.now(), PostSignScreen.MOVE, mockData.offer.quoteBundle.displayName)
        )
    }

    override fun onOpenCheckout() {
        _events.trySend(
            Event.OpenCheckout(
                CheckoutParameter(
                    quoteIds = listOf(mockData.offer.quoteBundle.quotes[0].id)
                )
            )
        )
    }

    override fun reload() {
        shouldError = false
        load()
    }

    override fun onDiscardOffer() {
        _events.trySend(Event.DiscardOffer)
    }

    override fun onGoToDirectDebit() {}
    override fun onSwedishBankIdSign() {}

    private fun load() {
        viewModelScope.launch {
            delay(650)
            do {
                if (shouldError) {
                    _viewState.value = ViewState.Error
                    return@launch
                }
                val topOfferItems = OfferItemsBuilder.createTopOfferItems(
                    mockData.offer,
                    mockData.dataCollectionStatus,
                    mockData.dataCollectionResult?.data,
                )
                val perilItems = OfferItemsBuilder.createPerilItems(mockData.offer.quoteBundle.quotes)
                val documentItems = OfferItemsBuilder.createDocumentItems(mockData.offer.quoteBundle.quotes)
                val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.offer.quoteBundle.quotes)
                val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(mockData.offer)
                _viewState.value =
                    ViewState.Content(
                        topOfferItems = topOfferItems,
                        perils = perilItems,
                        documents = documentItems,
                        insurableLimitsItems = insurableLimitsItems,
                        bottomOfferItems = bottomOfferItems,
                        signMethod = mockData.offer.signMethodForQuotes,
                        checkoutLabel = mockData.offer.checkoutLabel(),
                        title = mockData.offer.quoteBundle.appConfiguration.title,
                        loginStatus = LoginStatus.LOGGED_IN
                    )
                delay(2000)
            } while (mockRefreshEvery2Seconds)
        }
    }

    companion object {
        var shouldError = false
        var mockData: OfferMockData = OfferMockData(OFFER_DATA_SWEDISH_APARTMENT)
        var mockRefreshEvery2Seconds = false

        data class OfferMockData(
            val offer: OfferQuery.Data,
            val dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
            val dataCollectionResult: GetDataCollectionResultUseCase.Result.Success? = null,
        ) {
            constructor(
                id: String = "id",
                offer: OfferQuery.Data = OFFER_DATA_SWEDISH_APARTMENT,
                dataCollectionValue: DataCollectionStatusSubscription.Data,
                dataCollectionResult: DataCollectionResultQuery.Data? = null,
            ) : this(
                offer = offer,
                dataCollectionStatus = SubscribeToDataCollectionStatusUseCase.Status.Content(
                    id,
                    DataCollectionStatus.fromDto(dataCollectionValue)
                ),
                dataCollectionResult = if (dataCollectionResult != null) {
                    GetDataCollectionResultUseCase.Result.Success(
                        id,
                        DataCollectionResult.fromDto(dataCollectionResult)
                    )
                } else {
                    null
                },
            )
        }
    }
}
