package com.hedvig.app.feature.chat.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.hedvig.android.apollo.graphql.ChatMessagesQuery
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.databinding.ActivityChatBinding
import com.hedvig.app.feature.chat.ChatInputType
import com.hedvig.app.feature.chat.ParagraphInput
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.askForPermissions
import com.hedvig.app.util.extensions.calculateNonFullscreenHeightDiff
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.handleSingleSelectLink
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e
import java.io.File
import java.io.IOException

class ChatActivity : AppCompatActivity(R.layout.activity_chat) {
  private val chatViewModel: ChatViewModel by viewModel()
  private val binding by viewBinding(ActivityChatBinding::bind)

  private val imageLoader: ImageLoader by inject()
  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val featureManager: FeatureManager by inject()

  private var keyboardHeight = 0
  private var systemNavHeight = 0
  private var navHeightDiff = 0
  private var isKeyboardBreakPoint = 0

  private var isKeyboardShown = false
  private var preventOpenAttachFile = false
  private var preventOpenAttachFileHandler = Handler(Looper.getMainLooper())

  private val resetPreventOpenAttachFile = { preventOpenAttachFile = false }

  private var attachPickerDialog: AttachPickerDialog? = null

  private var currentPhotoPath: String? = null

  private var forceScrollToBottom = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    keyboardHeight = resources.getDimensionPixelSize(R.dimen.default_attach_file_height)
    isKeyboardBreakPoint =
      resources.getDimensionPixelSize(R.dimen.is_keyboard_brake_point_height)
    navHeightDiff = resources.getDimensionPixelSize(R.dimen.nav_height_div)

    chatViewModel.events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          ChatViewModel.Event.Restart -> {
            triggerRestartActivity(ChatActivity::class.java)
          }
          is ChatViewModel.Event.Error -> showAlert(
            title = com.adyen.checkout.dropin.R.string.error_dialog_title,
            message = com.adyen.checkout.dropin.R.string.component_error,
            positiveAction = {},
            negativeLabel = null,
          )
        }
      }
      .launchIn(lifecycleScope)

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
      toolbar.applyStatusBarInsets()
      messages.applyStatusBarInsets()
      input.applyInsetter {
        type(navigationBars = true, ime = true) {
          padding(animated = true)
        }
        syncTranslationTo(binding.messages)
      }
    }

    initializeToolbarButtons()
    initializeMessages()
    initializeInput()
    initializeKeyboardVisibilityHandler()
    observeData()
  }

  override fun onResume() {
    super.onResume()
    storeBoolean(ACTIVITY_IS_IN_FOREGROUND, true)
    forceScrollToBottom = true
  }

  override fun onPause() {
    storeBoolean(ACTIVITY_IS_IN_FOREGROUND, false)
    super.onPause()
  }

  private fun initializeInput() {
    binding.input.initialize(
      sendTextMessage = { message ->
        scrollToBottom(true)
        chatViewModel.respondWithTextMessage(message)
      },
      sendSingleSelect = { value ->
        scrollToBottom(true)
        chatViewModel.respondWithSingleSelect(value)
      },
      sendSingleSelectLink = { value ->
        scrollToBottom(true)
        handleSingleSelectLink(
          value = value,
          onLinkHandleFailure = {
            authenticationTokenService.authenticationToken = null
            lifecycleScope.launch {
              featureManager.invalidateExperiments()
            }
            startActivity(MarketingActivity.newInstance(this, true))
          },
        )
      },
      openAttachFile = {
        scrollToBottom(true)
        if (!preventOpenAttachFile) {
          if (hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openAttachPicker()
          } else {
            askForPermissions(
              arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
              REQUEST_WRITE_PERMISSION,
            )
          }
        }
      },
      openSendGif = {
        scrollToBottom(true)
        openGifPicker()
      },
      chatRecyclerView = binding.messages,
    )
  }

  private fun initializeMessages() {
    val adapter = ChatAdapter(
      this,
      onPressEdit = {
        showAlert(
          hedvig.resources.R.string.CHAT_EDIT_MESSAGE_TITLE,
          positiveLabel = hedvig.resources.R.string.CHAT_EDIT_MESSAGE_SUBMIT,
          negativeLabel = hedvig.resources.R.string.CHAT_EDIT_MESSAGE_CANCEL,
          positiveAction = {
            chatViewModel.editLastResponse()
          },
        )
      },
      imageLoader = imageLoader,
    )
    binding.messages.adapter = adapter
  }

  private fun initializeToolbarButtons() {
    binding.settings.setHapticClickListener {
      startActivity(SettingsActivity.newInstance(this))
    }

    if (intent?.extras?.getBoolean(EXTRA_SHOW_CLOSE, false) == true) {
      binding.close.setOnClickListener {
        onBackPressed()
      }
      binding.close.contentDescription = getString(hedvig.resources.R.string.CHAT_CLOSE_DESCRIPTION)
      binding.close.show()
    }
  }

  private fun initializeKeyboardVisibilityHandler() {
    binding.chatRoot.viewTreeObserver.addOnGlobalLayoutListener {
      val heightDiff = binding.chatRoot.calculateNonFullscreenHeightDiff()
      if (heightDiff > isKeyboardBreakPoint) {
        if (systemNavHeight > 0) systemNavHeight -= navHeightDiff
        this.keyboardHeight = heightDiff - systemNavHeight
        isKeyboardShown = true
        scrollToBottom(true)
      } else {
        systemNavHeight = heightDiff
        isKeyboardShown = false
      }
    }
  }

  private fun observeData() {
    chatViewModel.messages.observe(this) { data ->
      data?.let { bindData(it, forceScrollToBottom) }
    }
    // Maybe we should move the loading into the chatViewModel instead
    chatViewModel.sendMessageResponse.observe(this) { response ->
      if (response == true) {
        binding.input.clearInput()
      }
    }
    chatViewModel.takePictureUploadOutcome.observe(this) {
      attachPickerDialog?.uploadingTakenPicture(false)
      currentPhotoPath?.let { File(it).delete() }
    }

    chatViewModel.networkError.observe(this) { networkError ->
      if (networkError == true) {
        showAlert(
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_TITLE,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_CANCEL_ACTION,
          positiveAction = {
            chatViewModel.load()
          },
        )
      }
    }

    chatViewModel.subscribe()
    chatViewModel.load()
  }

  private fun scrollToBottom(smooth: Boolean) {
    if (smooth) {
      (binding.messages.layoutManager as LinearLayoutManager).smoothScrollToPosition(
        binding.messages,
        null,
        0,
      )
    } else {
      (binding.messages.layoutManager as LinearLayoutManager).scrollToPosition(0)
    }
  }

  private fun bindData(data: ChatMessagesQuery.Data, forceScrollToBottom: Boolean) {
    var triggerScrollToBottom = false
    val firstMessage = data.messages.firstOrNull()?.let {
      ChatInputType.from(
        it,
      )
    }
    binding.input.message = firstMessage
    if (firstMessage is ParagraphInput) {
      triggerScrollToBottom = true
    }
    (binding.messages.adapter as? ChatAdapter)?.let {
      it.messages = data.messages.filterNotNull()
      val layoutManager = binding.messages.layoutManager as LinearLayoutManager
      val pos = layoutManager.findFirstCompletelyVisibleItemPosition()
      if (pos == 0) {
        triggerScrollToBottom = true
      }
    }
    if (triggerScrollToBottom || forceScrollToBottom) {
      scrollToBottom(false)
    }
  }

  private fun openAttachPicker() {
    val attachPickerDialog = AttachPickerDialog(this)
    attachPickerDialog.initialize(
      takePhotoCallback = {
        if (hasPermissions(Manifest.permission.CAMERA)) {
          startTakePicture()
        } else {
          askForPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION,
          )
        }
      },
      showUploadBottomSheetCallback = {
        ChatFileUploadBottomSheet
          .newInstance()
          .show(
            supportFragmentManager,
            ChatFileUploadBottomSheet.TAG,
          )
      },
      dismissCallback = { motionEvent ->

        motionEvent?.let {
          preventOpenAttachFile = true
          this.dispatchTouchEvent(motionEvent)
          preventOpenAttachFileHandler.removeCallbacks(resetPreventOpenAttachFile)
          // unfortunately the best way I found to prevent reopening :(
          preventOpenAttachFileHandler.postDelayed(resetPreventOpenAttachFile, 100)
        }

        binding.input.rotateFileUploadIcon(false)
        this.attachPickerDialog = null
      },
      uploadFileCallback = { uri ->
        chatViewModel.uploadFile(uri)
      },
    )
    chatViewModel.fileUploadOutcome.observe(this) { data ->
      data?.uri?.path?.let { path ->
        attachPickerDialog.imageWasUploaded(path)
      }
    }
    attachPickerDialog.pickerHeight = keyboardHeight
    attachPickerDialog.show()

    lifecycleScope.launch(Dispatchers.IO) {
      val images = getImagesPath()
      lifecycleScope.launch(Dispatchers.Main) {
        attachPickerDialog.setImages(images)
      }
    }

    binding.input.rotateFileUploadIcon(true)
    this.attachPickerDialog = attachPickerDialog
  }

  private fun openGifPicker() {
    GifPickerBottomSheet
      .newInstance(isKeyboardShown)
      .show(
        supportFragmentManager,
        GifPickerBottomSheet.TAG,
      )
  }

  private fun startTakePicture() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      ?: run {
        e { "Could not getExternalFilesDir" }
        return
      }

    val tempTakenPhotoFile = try {
      File.createTempFile(
        "JPEG_${System.currentTimeMillis()}_",
        ".jpg",
        storageDir,
      ).apply {
        currentPhotoPath = absolutePath
      }
    } catch (ex: IOException) {
      e(ex) { "Error occurred while creating the photo file" }
      null
    }

    tempTakenPhotoFile?.also { file ->
      val photoURI: Uri = FileProvider.getUriForFile(
        this,
        getString(R.string.file_provider_authority),
        file,
      )
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
      startActivityForResult(
        takePictureIntent,
        TAKE_PICTURE_REQUEST_CODE,
      )
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      TAKE_PICTURE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
        currentPhotoPath?.let { tempFile ->
          attachPickerDialog?.uploadingTakenPicture(true)

          chatViewModel.uploadTakenPicture(Uri.fromFile(File(tempFile)))
        }
      }
    }
  }

  private suspend fun getImagesPath(): List<String> = withContext(Dispatchers.IO) {
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val listOfAllImages = ArrayList<String>()
    val columnIndexData: Int

    val projection = arrayOf(MediaColumns.DISPLAY_NAME, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    val cursor = this@ChatActivity.contentResolver.query(
      uri,
      projection,
      null,
      null,
      "${MediaColumns.DATE_ADDED} DESC",
    )

    cursor?.let {
      columnIndexData = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME)
      while (it.moveToNext()) {
        listOfAllImages.add(it.getString(columnIndexData))
      }
    }
    cursor?.close()

    listOfAllImages
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
  ) {
    when (requestCode) {
      REQUEST_WRITE_PERMISSION ->
        if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
          openAttachPicker()
        }
      REQUEST_CAMERA_PERMISSION ->
        if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
          startTakePicture()
        }
      else -> {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
      }
    }
  }

  override fun finish() {
    super.finish()
    chatViewModel.onChatClosed()
    if (intent.getBooleanExtra(EXTRA_SHOW_CLOSE, false)) {
      overridePendingTransition(R.anim.stay_in_place, R.anim.chat_slide_down_out)
    }
  }

  override fun onDestroy() {
    preventOpenAttachFileHandler.removeCallbacks(resetPreventOpenAttachFile)
    super.onDestroy()
  }

  companion object {

    private const val REQUEST_WRITE_PERMISSION = 35134
    private const val REQUEST_CAMERA_PERMISSION = 54332

    private const val TAKE_PICTURE_REQUEST_CODE = 2371

    const val EXTRA_SHOW_CLOSE = "extra_show_close"

    const val ACTIVITY_IS_IN_FOREGROUND = "chat_activity_is_in_foreground"
  }
}
