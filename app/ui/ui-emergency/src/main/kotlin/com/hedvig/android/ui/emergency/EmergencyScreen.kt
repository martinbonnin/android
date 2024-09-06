package com.hedvig.android.ui.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigInfoCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Scaffold
import com.hedvig.android.design.system.hedvig.ShapeDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
fun EmergencyScreen(
  emergencyNumber: String?,
  emergencyUrl: String?,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topAppBarText = stringResource(id = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE),
    navigateUp = navigateUp,
    modifier = modifier,
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigInfoCard(
      message = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INFO_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      style = NotificationDefaults.InfoCardStyle.Default,
      priority = NotificationDefaults.NotificationPriority.Attention,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTheme(true) {
      Surface(
        shape = ShapeDefaults.CornerXLarge,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      ) {
        Column(Modifier.padding(16.dp)) {
          Spacer(Modifier.height(16.dp))
          Icon(
            imageVector = HedvigIcons.HelipadOutline,
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp),
          )
          Spacer(Modifier.height(24.dp))
          HedvigText(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_TITLE),
            textAlign = TextAlign.Center,
            style = HedvigTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(2.dp))
          HedvigText(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_LABEL),
            textAlign = TextAlign.Center,
            color = HedvigTheme.colorScheme.textTertiary,
            style = HedvigTheme.typography.bodySmall.copy(
              lineBreak = LineBreak.Heading,
            ),
            modifier = Modifier.fillMaxWidth(),
          )
          if (emergencyUrl != null) {
            Spacer(Modifier.height(24.dp))
            HedvigButton(
              buttonSize = ButtonDefaults.ButtonSize.Medium,
              enabled = true,
              text = stringResource(
                R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_URL_LABEL,
                emergencyUrl,
              ),
              onClick = {
                openUrl(emergencyUrl)
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          if (emergencyNumber != null) {
            val style = if (emergencyUrl != null) {
              ButtonDefaults.ButtonStyle.Ghost
            } else {
              ButtonDefaults.ButtonStyle.SecondaryAlt
            }
            Spacer(Modifier.height(8.dp))
            val context = LocalContext.current
            HedvigButton(
              buttonStyle = style,
              buttonSize = ButtonDefaults.ButtonSize.Medium,
              text = stringResource(
                R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL,
                emergencyNumber,
              ),
              enabled = true,
              onClick = {
                try {
                  context.startActivity(
                    Intent(
                      Intent.ACTION_DIAL,
                      Uri.parse("tel:$emergencyNumber"),
                    ),
                  )
                } catch (exception: Throwable) {
                  logcat(LogPriority.ERROR, exception) {
                    "Could not open dial activity in deflect emergency destination"
                  }
                }
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          Spacer(Modifier.height(16.dp))
          HedvigText(
            text = stringResource(R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_FOOTNOTE),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = HedvigTheme.colorScheme.textTertiary,
            style = HedvigTheme.typography.finePrint,
          )
          Spacer(Modifier.height(8.dp))
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(24.dp))
    QuestionsAndAnswers(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuestionsAndAnswers(modifier: Modifier = Modifier) {
  val faqList = listOf(
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ1_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ1_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ2_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ2_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ3_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ3_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ4_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ4_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ5_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ5_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ6_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ6_LABEL),
  )
  Column(modifier) {
    AccordionList(mapToAccordionData(faqList))
  }
}

private fun mapToAccordionData(list: List<Pair<String, String>>): List<AccordionData> {
  return buildList {
    list.forEach { faqItem ->
      add(AccordionData(title = faqItem.first, description = faqItem.second))
    }
  }
}

@Preview
@Composable
private fun PreviewEmergencyScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EmergencyScreen(
        emergencyNumber = "123456",
        emergencyUrl = "url",
        navigateUp = {},
        openUrl = {},
      )
    }
  }
}
