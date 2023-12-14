package com.hedvig.android.feature.claim.details

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailsGraph(
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  openChat: (NavBackStackEntry) -> Unit,
) {
  composable<ClaimDetailsDestination> { backStackEntry ->
    val viewModel: ClaimDetailsViewModel = koinViewModel { parametersOf(claimId) }
    com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      navigateUp = navigateUp,
      onChatClick = { openChat(backStackEntry) },
      onAddFilesClick = {  },
      openUrl = openUrl,
    )
  }
}
