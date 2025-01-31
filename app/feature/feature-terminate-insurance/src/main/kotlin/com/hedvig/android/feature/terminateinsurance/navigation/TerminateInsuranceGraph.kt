package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionViewModel
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartDestination
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * This flow does not have a nice start destination, so the back navigation is all messed up, with having to manually
 * check if we're going back to [TerminateInsuranceDestination.StartStep] which is a "fake" step which only launches
 * the backend driven flow. This will stay this way until a more "stable" start destination can be made, potentially
 * one which gives a brief explanation of what the flow is going to be about or something like that.
 *
 * The changes that could be added after this is fixed is to use navigator::navigateUp for all "up" actions, and remove
 * all instances of [BackHandler].
 */
fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  openChat: () -> Unit,
  openPlayStore: () -> Unit,
) {
  animatedComposable<TerminateInsuranceDestination.TerminationSuccess> {
    TerminationSuccessDestination(
      terminationDate = this.terminationDate,
      surveyUrl = this.surveyUrl,
      windowSizeClass = windowSizeClass,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  animatedComposable<TerminateInsuranceDestination.TerminationFailure> {
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(this.message),
      openChat = openChat,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  animatedComposable<TerminateInsuranceDestination.UnknownScreen> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  animatedNavigation<AppDestination.TerminateInsurance>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
  ) {
    animatedComposable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)
      val insuranceId: InsuranceId = InsuranceId(terminateInsurance.insuranceId)
      val viewModel: TerminationStartStepViewModel = koinViewModel { parametersOf(insuranceId) }
      TerminationStartDestination(
        viewModel = viewModel,
        retryLoad = { viewModel.retryToStartTerminationFlow() },
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            backStackEntry = backStackEntry,
            destination = terminationStep.toTerminateInsuranceDestination(),
          )
        },
      )
    }
    animatedComposable<TerminateInsuranceDestination.TerminationDate> { backStackEntry ->
      val shouldFinishFlowOnBack = run {
        val previousBackstackEntryRoute = navController.previousBackStackEntry?.destination?.route
        previousBackstackEntryRoute == createRoutePattern<TerminateInsuranceDestination.StartStep>()
      }
      BackHandler(shouldFinishFlowOnBack) {
        finishTerminationFlow(navController)
      }
      val viewModel: TerminationDateViewModel = koinViewModel { parametersOf(this.minDate, this.maxDate) }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            backStackEntry = backStackEntry,
            destination = terminationStep.toTerminateInsuranceDestination(),
          )
        },
        navigateBack = {
          if (shouldFinishFlowOnBack) {
            finishTerminationFlow(navController)
          } else {
          }
        },
      )
    }
    animatedComposable<TerminateInsuranceDestination.InsuranceDeletion> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)
      val shouldFinishFlowOnBack = run {
        val previousBackstackEntryRoute = navController.previousBackStackEntry?.destination?.route
        previousBackstackEntryRoute == createRoutePattern<TerminateInsuranceDestination.StartStep>()
      }
      BackHandler(shouldFinishFlowOnBack) {
        finishTerminationFlow(navController)
      }
      val viewModel: InsuranceDeletionViewModel = koinViewModel { parametersOf(this) }
      InsuranceDeletionDestination(
        viewModel = viewModel,
        insuranceDisplayName = terminateInsurance.insuranceDisplayName,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            backStackEntry = backStackEntry,
            destination = terminationStep.toTerminateInsuranceDestination(),
          )
        },
        navigateBack = {
          if (shouldFinishFlowOnBack) {
            finishTerminationFlow(navController)
          } else {
            navigator.navigateUp()
          }
        },
      )
    }
  }
}

@Composable
private fun getTerminateInsuranceDataFromParentBackstack(
  navController: NavController,
  backStackEntry: NavBackStackEntry,
): AppDestination.TerminateInsurance {
  return remember(navController, backStackEntry) {
    val terminateInsuranceEntry = navController.getBackStackEntry(
      createRoutePattern<AppDestination.TerminateInsurance>(),
    )
    decodeArguments(AppDestination.TerminateInsurance.serializer(), terminateInsuranceEntry)
  }
}

/**
 * If we're going to a terminal destination, pop the termination flow backstack completely before going there.
 */
private fun <T : TerminateInsuranceDestination> Navigator.navigateToTerminateFlowDestination(
  backStackEntry: NavBackStackEntry,
  destination: T,
) {
  val navOptions = navOptions {
    when {
      destination is TerminateInsuranceDestination.TerminationSuccess ||
        destination is TerminateInsuranceDestination.TerminationFailure ||
        destination is TerminateInsuranceDestination.UnknownScreen -> {
        popUpTo<AppDestination.TerminateInsurance> {
          inclusive = true
        }
      }
      else -> {}
    }
  }
  backStackEntry.navigate(destination, navOptions)
}

private fun finishTerminationFlow(navController: NavController) {
  navController.popBackStack<AppDestination.TerminateInsurance>(inclusive = true)
}
