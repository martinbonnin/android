package com.hedvig.android.feature.home.home.ui

import com.hedvig.android.feature.home.data.GetHomeDataUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class HomeViewModel(
  getHomeDataUseCase: GetHomeDataUseCase,
) : MoleculeViewModel<HomeEvent, HomeUiState>(
  HomeUiState.Loading,
  HomePresenter(getHomeDataUseCase),
)
