package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.fragment.app.FragmentManager
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeBottomSheet
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressActivity
import com.hedvig.android.feature.odyssey.search.commonclaims.SearchActivity
import com.hedvig.android.feature.odyssey.search.groups.ClaimGroupsActivity
import com.hedvig.android.feature.travelcertificate.navigation.generateTravelCertificateGraph
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.BuildConfig
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.home.ui.HowClaimsWorkDialog
import com.hedvig.app.feature.insurance.ui.tab.insuranceGraph
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.profile.ui.tab.profileGraph
import com.hedvig.app.feature.referrals.ui.tab.referralsGraph
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.startChat
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import hedvig.resources.R
import kotlinx.coroutines.launch

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val resources = context.resources
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()
  val navController = hedvigAppState.navController
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<TopLevelGraph.HOME>(),
    route = "root",
    modifier = modifier,
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    homeGraph(
      navController = navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      nestedGraphs = {
        changeAddressGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          density = density,
          navController = navController,
          openChat = context::startChat,
        )
        generateTravelCertificateGraph(
          density = density,
          navController = navController,
          applicationId = BuildConfig.APPLICATION_ID,
        )
      },
      onStartChat = { context.startChat() },
      onStartClaim = {
        coroutineScope.launch {
          hAnalytics.beginClaim(AppScreen.HOME)
          if (featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)) {
            val intent = if (featureManager.isFeatureEnabled(Feature.CLAIMS_TRIAGING)) {
              ClaimGroupsActivity.newInstance(context = context)
            } else {
              SearchActivity.newInstance(context = context)
            }

            context.startActivity(intent)
          } else {
            HonestyPledgeBottomSheet
              .newInstance(
                embarkClaimsFlowIntent = EmbarkActivity.newInstance(
                  context = context,
                  storyName = "claims",
                  storyTitle = context.getString(R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL),
                ),
              )
              .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
          }
        }
      },
      startMovingFlow = {
        coroutineScope.launch {
          if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
            navController.navigate(AppDestination.ChangeAddress)
          } else {
            context.startActivity(
              LegacyChangeAddressActivity.newInstance(context),
            )
          }
        }
      },
      onHowClaimsWorkClick = { howClaimsWorkList ->
        val howClaimsWorkData = howClaimsWorkList.mapIndexed { index, howClaimsWork ->
          DismissiblePagerModel.NoTitlePage(
            imageUrls = ThemedIconUrls.from(
              howClaimsWork.illustration.variants.fragments.iconVariantsFragment,
            ),
            paragraph = howClaimsWork.body,
            buttonText = resources.getString(
              if (index == howClaimsWorkList.lastIndex) {
                hedvig.resources.R.string.claims_explainer_button_start_claim
              } else {
                hedvig.resources.R.string.claims_explainer_button_next
              },
            ),
          )
        }
        HowClaimsWorkDialog
          .newInstance(howClaimsWorkData)
          .show(fragmentManager, HowClaimsWorkDialog.TAG)
      },
      onGenerateTravelCertificateClicked = {
        navController.navigate(AppDestination.GenerateTravelCertificate)
      },
      navigateToPayinScreen = navigateToPayinScreen@{ paymentType ->
        val market = marketManager.market ?: return@navigateToPayinScreen
        context.startActivity(
          connectPayinIntent(
            context,
            paymentType,
            market,
            false,
          ),
        )
      },
      tryOpenUri = { uri ->
        if (context.canOpenUri(uri)) {
          context.openUri(uri)
        }
      },
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
    )
    insuranceGraph(
      imageLoader = imageLoader,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    referralsGraph(
      languageService = languageService,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    profileGraph(
      navController = navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
  }
}
