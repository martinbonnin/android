package com.hedvig.android.feature.home.home.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.common.android.SHARED_PREFERENCE_NAME
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.plus
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyActivity
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.ClaimStatusCards
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import com.hedvig.android.feature.home.data.HomeData
import com.hedvig.android.feature.home.home.ChatTooltip
import com.hedvig.android.feature.home.otherservices.OtherServicesBottomSheet
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.ui.MemberReminderCards
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.notification.permission.rememberPreviewNotificationPermissionState
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  openUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(HomeEvent.RefreshData) },
    snoozeNotificationPermissionReminder = { viewModel.emit(HomeEvent.SnoozeNotificationPermissionReminder) },
    onStartChat = onStartChat,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    onStartClaim = onStartClaim,
    onStartMovingFlow = onStartMovingFlow,
    onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
    onOpenCommonClaim = onOpenCommonClaim,
    openUrl = openUrl,
    openAppSettings = openAppSettings,
  )
}

@Composable
private fun HomeScreen(
  uiState: HomeUiState,
  notificationPermissionState: NotificationPermissionState,
  reload: () -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  openUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
) {
  val context = LocalContext.current
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isReloading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )

  Box(Modifier.fillMaxSize()) {
    val toolbarHeight = 64.dp
    val transition = updateTransition(targetState = uiState, label = "home ui state")
    transition.AnimatedContent(
      modifier = Modifier.fillMaxSize(),
      contentKey = { it::class },
    ) { uiState ->
      when (uiState) {
        HomeUiState.Loading -> {
          HedvigFullScreenCenterAlignedProgressDebounced(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.safeDrawing),
          )
        }
        is HomeUiState.Error -> {
          HedvigErrorSection(
            retry = reload,
            modifier = Modifier
              .padding(16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing),
          )
        }
        is HomeUiState.Success -> {
          HomeScreenSuccess(
            uiState = uiState,
            pullRefreshState = pullRefreshState,
            toolbarHeight = toolbarHeight,
            notificationPermissionState = notificationPermissionState,
            snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
            onStartMovingFlow = onStartMovingFlow,
            onClaimDetailCardClicked = onClaimDetailCardClicked,
            navigateToConnectPayment = navigateToConnectPayment,
            onEmergencyClaimClicked = { emergencyData ->
              context.startActivity(
                EmergencyActivity.newInstance(
                  context = context,
                  data = emergencyData,
                ),
              )
            },
            onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
            onOpenCommonClaim = onOpenCommonClaim,
            onStartClaimClicked = onStartClaim,
            openAppSettings = openAppSettings,
            openChat = onStartChat,
            openUrl = openUrl,
          )
        }
      }
    }
    Column {
      TopAppBarLayoutForActions {
        ToolbarChatIcon(
          onClick = onStartChat,
        )
      }
      val shouldShowTooltip by produceState(false) {
        val daysSinceLastTooltipShown = daysSinceLastTooltipShown(context)
        value = daysSinceLastTooltipShown
      }
      ChatTooltip(
        showTooltip = shouldShowTooltip,
        tooltipShown = {
          context.setLastEpochDayWhenChatTooltipWasShown(java.time.LocalDate.now().toEpochDay())
        },
        modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp),
      )
    }
    PullRefreshIndicator(
      refreshing = uiState.isReloading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

private suspend fun daysSinceLastTooltipShown(context: Context): Boolean {
  val currentEpochDay = java.time.LocalDate.now().toEpochDay()
  val lastEpochDayOpened = withContext(Dispatchers.IO) {
    context.getLastEpochDayWhenChatTooltipWasShown()
  }
  val diff = currentEpochDay - lastEpochDayOpened
  val daysSinceLastTooltipShown = diff >= 30
  return daysSinceLastTooltipShown
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun HomeScreenSuccess(
  uiState: HomeUiState.Success,
  pullRefreshState: PullRefreshState,
  toolbarHeight: Dp,
  notificationPermissionState: NotificationPermissionState,
  snoozeNotificationPermissionReminder: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  onStartClaimClicked: () -> Unit,
  openAppSettings: () -> Unit,
  openChat: () -> Unit,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(true)
  var showEditYourInfoBottomSheet by rememberSaveable { mutableStateOf(false) }
  val dismissOtherServicesBottomSheet: () -> Unit = {
    coroutineScope.launch {
      sheetState.hide()
    }.invokeOnCompletion {
      showEditYourInfoBottomSheet = false
    }
  }
  if (showEditYourInfoBottomSheet) {
    OtherServicesBottomSheet(
      uiState = uiState,
      dismissBottomSheet = dismissOtherServicesBottomSheet,
      onChatClicked = openChat,
      onStartMovingFlow = onStartMovingFlow,
      onEmergencyClaimClicked = onEmergencyClaimClicked,
      onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
      onOpenCommonClaim = onOpenCommonClaim,
      sheetState = sheetState,
    )
  }

  var fullScreenSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }
  Column(
    modifier = modifier
      .fillMaxSize()
      .onSizeChanged { fullScreenSize = it }
      .pullRefresh(pullRefreshState)
      .verticalScroll(rememberScrollState()),
  ) {
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)
    HomeLayout(
      welcomeMessage = {
        WelcomeMessage(
          homeText = uiState.homeText,
          modifier = Modifier
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
      },
      claimStatusCards = {
        if (uiState.claimStatusCardsData != null) {
          var consumedWindowInsets by remember { mutableStateOf(WindowInsets(0.dp)) }
          ClaimStatusCards(
            goToDetailScreen = onClaimDetailCardClicked,
            claimStatusCardsData = uiState.claimStatusCardsData,
            contentPadding = PaddingValues(horizontal = 16.dp) +
              WindowInsets.safeDrawing.exclude(consumedWindowInsets).only(WindowInsetsSides.Horizontal).asPaddingValues(),
            modifier = Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets = it },
          )
        }
      },
      veryImportantMessages = {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
          for (veryImportantMessage in uiState.veryImportantMessages) {
            VeryImportantMessageCard(openUrl, veryImportantMessage)
          }
        }
      },
      memberReminderCards = {
        val memberReminders =
          uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
        MemberReminderCards(
          memberReminders = memberReminders,
          navigateToConnectPayment = navigateToConnectPayment,
          openUrl = openUrl,
          notificationPermissionState = notificationPermissionState,
          snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
      },
      startClaimButton = {
        HedvigContainedButton(
          text = stringResource(R.string.home_tab_claim_button_text),
          onClick = onStartClaimClicked,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
      },
      otherServicesButton = {
        HedvigTextButton(
          text = stringResource(R.string.home_tab_other_services),
          onClick = { showEditYourInfoBottomSheet = true },
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
      },
      topSpacer = {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)).height(toolbarHeight))
      },
      bottomSpacer = {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)).height(16.dp))
      },
      fullScreenSize = fullScreenSize,
    )
  }
}

@Composable
private fun VeryImportantMessageCard(
  openUrl: (String) -> Unit,
  veryImportantMessage: HomeData.VeryImportantMessage,
  modifier: Modifier = Modifier,
) {
  VectorInfoCard(
    text = veryImportantMessage.message,
    icon = Icons.Hedvig.WarningFilled,
    iconColor = MaterialTheme.colorScheme.warningElement,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.warningContainer,
      contentColor = MaterialTheme.colorScheme.onWarningContainer,
    ),
    modifier = modifier,
  ) {
    HedvigContainedSmallButton(
      text = stringResource(R.string.important_message_read_more),
      onClick = { openUrl(veryImportantMessage.link) },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
      ),
      textStyle = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun WelcomeMessage(
  homeText: HomeText,
  modifier: Modifier = Modifier,
) {
  val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
  val firstName = homeText.name
  val headlineText = when (homeText) {
    is HomeText.Active -> if (firstName != null) {
      stringResource(R.string.home_tab_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_welcome_title_without_name)
    }
    is HomeText.ActiveInFuture -> if (firstName != null) {
      stringResource(
        R.string.home_tab_active_in_future_welcome_title,
        firstName,
        formatter.format(homeText.inception.toJavaLocalDate()),
      )
    } else {
      stringResource(
        R.string.home_tab_active_in_future_welcome_title_without_name,
        formatter.format(homeText.inception.toJavaLocalDate()),
      )
    }
    is HomeText.Pending -> if (firstName != null) {
      stringResource(R.string.home_tab_pending_unknown_title, firstName)
    } else {
      stringResource(R.string.home_tab_pending_unknown_title_without_name)
    }
    is HomeText.Switching -> if (firstName != null) {
      stringResource(R.string.home_tab_pending_switchable_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_pending_switchable_welcome_title_without_name)
    }
    is HomeText.Terminated -> if (firstName != null) {
      stringResource(R.string.home_tab_terminated_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_terminated_welcome_title_without_name)
    }
  }
  Text(
    text = headlineText,
    style = MaterialTheme.typography.headlineMedium,
    modifier = modifier.fillMaxWidth(),
  )
}

private const val SHARED_PREFERENCE_LAST_OPEN = "shared_preference_last_open"

private fun Context.setLastEpochDayWhenChatTooltipWasShown(epochDay: Long) =
  getSharedPreferences().edit().putLong(SHARED_PREFERENCE_LAST_OPEN, epochDay).commit()

private fun Context.getLastEpochDayWhenChatTooltipWasShown() =
  getSharedPreferences().getLong(SHARED_PREFERENCE_LAST_OPEN, 0)

private fun Context.getSharedPreferences() =
  this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

@HedvigPreview
@Composable
private fun PreviewHomeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HomeScreen(
        uiState = HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active("John"),
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                pillsUiState = PillUiState.previewList(),
                title = "Insurance Case",
                subtitle = "Home Insurance renter",
                claimProgressItemsUiState = ClaimProgressUiState.previewList(),
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(HomeData.VeryImportantMessage("Beware of the earthquake", "")),
          memberReminders = MemberReminders(
            connectPayment = MemberReminder.ConnectPayment,
          ),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = true,
          emergencyData = null,
          commonClaimsData = persistentListOf(),
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        snoozeNotificationPermissionReminder = {},
        onStartChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        onStartMovingFlow = {},
        onGenerateTravelCertificateClicked = {},
        onOpenCommonClaim = {},
        openUrl = {},
        openAppSettings = {},
      )
    }
  }
}
