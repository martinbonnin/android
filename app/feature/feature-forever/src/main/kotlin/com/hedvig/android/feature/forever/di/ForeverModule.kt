package com.hedvig.android.feature.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCase
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foreverModule = module {
  viewModel<ForeverViewModel> {
    ForeverViewModel(
      get<ForeverRepository>(),
      get<GetReferralsInformationUseCase>(),
    )
  }
  single<GetReferralsInformationUseCase> {
    GetReferralsInformationUseCase(
      get<ApolloClient>(giraffeClient),
      get<LanguageService>(),
    )
  }
}
