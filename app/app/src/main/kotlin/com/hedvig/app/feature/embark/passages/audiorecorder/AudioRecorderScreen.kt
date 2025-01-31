package com.hedvig.app.feature.embark.passages.audiorecorder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ScreenOnFlag
import com.hedvig.android.core.ui.audiorecording.RecordingAmplitudeIndicator
import com.hedvig.app.R
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@Composable
fun AudioRecorderScreen(
  parameters: AudioRecorderParameters,
  viewState: AudioRecorderViewModel.ViewState,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submit: () -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
      .padding(top = 24.dp),
  ) {
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    ) {
      items(parameters.messages) { message ->
        Surface(
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
              .padding(16.dp),
          )
        }
      }
    }

    when (viewState) {
      AudioRecorderViewModel.ViewState.NotRecording -> NotRecording(
        startRecording = startRecording,
      )
      is AudioRecorderViewModel.ViewState.Recording -> Recording(
        viewState = viewState,
        stopRecording = stopRecording,
        clock = clock,
      )
      is AudioRecorderViewModel.ViewState.Playback -> Playback(
        viewState = viewState,
        submit = submit,
        redo = redo,
        play = play,
        pause = pause,
      )
    }
  }
}

@Composable
fun NotRecording(startRecording: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    val label = stringResource(hedvig.resources.R.string.EMBARK_START_RECORDING)
    IconButton(
      onClick = startRecording,
      modifier = Modifier
        .padding(bottom = 24.dp)
        .size(72.dp)
        .testTag("recordClaim"),
    ) {
      Image(
        painter = painterResource(
          R.drawable.ic_record,
        ),
        contentDescription = label,
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Recording(
  viewState: AudioRecorderViewModel.ViewState.Recording,
  stopRecording: () -> Unit,
  clock: Clock,
  modifier: Modifier = Modifier,
) {
  ScreenOnFlag()
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(bottom = 24.dp),
    ) {
      if (viewState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = viewState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier
          .size(72.dp)
          .testTag("stopRecording"),
      ) {
        Image(
          painter = painterResource(
            R.drawable.ic_record_stop,
          ),
          contentDescription = stringResource(hedvig.resources.R.string.EMBARK_STOP_RECORDING),
        )
      }
    }
    val diff = Duration.between(
      viewState.startedAt,
      Instant.now(clock),
    )
    val label = String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Playback(
  viewState: AudioRecorderViewModel.ViewState.Playback,
  submit: () -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    if (!viewState.isPrepared) {
      CircularProgressIndicator()
    } else {
      PlaybackWaveForm(
        isPlaying = viewState.isPlaying,
        play = play,
        pause = pause,
        amplitudes = viewState.amplitudes,
        progress = viewState.progress,
      )
    }
    LargeContainedButton(
      onClick = submit,
      modifier = Modifier
        .padding(top = 16.dp)
        .testTag("submitClaim"),
    ) {
      Text(stringResource(hedvig.resources.R.string.EMBARK_SUBMIT_CLAIM))
    }
    LargeTextButton(
      onClick = redo,
      modifier = Modifier
        .padding(top = 8.dp)
        .testTag("recordClaimAgain"),
    ) {
      Text(stringResource(hedvig.resources.R.string.EMBARK_RECORD_AGAIN))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAudioRecorderScreenNotRecording() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AudioRecorderScreen(
        parameters = AudioRecorderParameters(
          messages = listOf("Hello", "World"),
          key = "key",
          label = "label",
          link = "link",
        ),
        viewState = AudioRecorderViewModel.ViewState.NotRecording,
        startRecording = {},
        clock = Clock.systemDefaultZone(),
        stopRecording = {},
        submit = {},
        redo = {},
        play = {},
        pause = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAudioRecorderScreenRecording() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AudioRecorderScreen(
        parameters = AudioRecorderParameters(
          messages = listOf("Hello", "World"),
          key = "key",
          label = "label",
          link = "link",
        ),
        viewState = AudioRecorderViewModel.ViewState.Recording(
          listOf(
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
          ),
          Instant.ofEpochSecond(1634025260),
          "",
        ),
        startRecording = {},
        clock = Clock.fixed(Instant.ofEpochSecond(1634025262), ZoneId.systemDefault()),
        stopRecording = {},
        submit = {},
        redo = {},
        play = {},
        pause = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAudioRecorderScreenPlayback() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AudioRecorderScreen(
        parameters = AudioRecorderParameters(
          messages = listOf("Hello", "World"),
          key = "key",
          label = "label",
          link = "link",
        ),
        viewState = AudioRecorderViewModel.ViewState.Playback(
          "",
          isPlaying = false,
          isPrepared = true,
          amplitudes = listOf(
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
            100, 200, 150, 250, 0,
          ),
          progress = 0.5f,
        ),
        startRecording = {},
        clock = Clock.systemDefaultZone(),
        stopRecording = {},
        submit = {},
        redo = {},
        play = {},
        pause = {},
      )
    }
  }
}
