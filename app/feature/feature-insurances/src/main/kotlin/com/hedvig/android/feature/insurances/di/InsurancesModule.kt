package com.hedvig.android.feature.insurances.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCaseImpl
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseDemo
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCaseImpl
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.notification.badge.data.crosssell.CrossSellCardNotificationBadgeServiceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val insurancesModule = module {
  viewModel<InsuranceViewModel> {
    InsuranceViewModel(
      get<GetInsuranceContractsUseCaseProvider>(),
      get<GetCrossSellsUseCaseProvider>(),
      get<CrossSellCardNotificationBadgeServiceProvider>(),
    )
  }
  viewModel<TerminatedContractsViewModel> {
    TerminatedContractsViewModel(get<GetInsuranceContractsUseCaseProvider>())
  }
  viewModel<ContractDetailViewModel> { (contractId: String) ->
    ContractDetailViewModel(contractId, get<GetInsuranceContractsUseCaseProvider>())
  }

  provideGetContractsUseCase()
  provideGetCrossSellsUseCase()
}

private fun Module.provideGetContractsUseCase() {
  single<GetInsuranceContractsUseCaseImpl> {
    GetInsuranceContractsUseCaseImpl(
      get<ApolloClient>(octopusClient),
    )
  }

  single<GetInsuranceContractsUseCaseDemo> {
    GetInsuranceContractsUseCaseDemo()
  }

  single {
    GetInsuranceContractsUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetInsuranceContractsUseCaseImpl>(),
      demoImpl = get<GetInsuranceContractsUseCaseDemo>(),
    )
  }
}

private fun Module.provideGetCrossSellsUseCase() {
  single<GetCrossSellsUseCaseImpl> {
    GetCrossSellsUseCaseImpl(get<ApolloClient>(octopusClient))
  }

  single<GetCrossSellsUseCaseDemo> {
    GetCrossSellsUseCaseDemo()
  }

  single {
    GetCrossSellsUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetCrossSellsUseCaseImpl>(),
      demoImpl = get<GetCrossSellsUseCaseDemo>(),
    )
  }
}
