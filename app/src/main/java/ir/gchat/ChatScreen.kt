package ir.gchat

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.SoundEffectConstants
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLConnection
import kotlin.time.Duration.Companion.milliseconds

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) {
        return "$bytes B"
    }

    val units = arrayOf("KB", "MB", "GB", "TB", "PB")

    var size = bytes.toDouble()
    var unitIndex = -1

    while (size >= 1024 && unitIndex < units.lastIndex) {
        size /= 1024
        unitIndex++
    }

    return if (size % 1.0 == 0.0) {
        "${size.toInt()} ${units[unitIndex]}"
    } else {
        "%.1f %s".format(size, units[unitIndex])
    }
}

fun isAudioFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false

    val retriever = MediaMetadataRetriever()

    return try {
        retriever.setDataSource(file.absolutePath)

        val mimeType = retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_MIMETYPE
        )

        mimeType?.startsWith("audio/", ignoreCase = true) == true
    } catch (e: Exception) {
        false
    } finally {
        retriever.release()
    }
}

fun openDownloadedFile(
    context: Context, fileName: String
) {
    try {
        val file = File(
            context.filesDir, "downloads/$fileName"
        )

        if (!file.exists() || !file.isFile) {
            Log.e(
                "OpenFile", "File does not exist: ${file.absolutePath}"
            )
            return
        }

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )

        val mimeType =
            URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)

    } catch (e: ActivityNotFoundException) {
        Log.e(
            "OpenFile", "No application found to open $fileName", e
        )
    } catch (e: Exception) {
        Log.e(
            "OpenFile", "Failed to open $fileName", e
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    back: () -> Boolean,
    id: String,
    sendMessage: (String, List<Content>) -> Unit,
    messageList: List<MessageItem>,
    seenMessage: (String, Int) -> Unit,
    getMessagesList: (String) -> Unit,
    displayName: String,
    unreadCount: Int,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    draft: Map<String, List<Draft>>,
    savedText: Map<String, String>,
    setSavedText: (String, String) -> Unit,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    removeFileFromDraft: (String) -> Unit,
    removeTextFromDraft: (Int) -> Unit,
    clearDraft: () -> Unit,
    seenAll: (String) -> Unit,
    serverUrl: String,
    imageLoader: ImageLoader,
    isFileDownloaded: (String) -> Boolean,
    attachTextBlock: (String) -> Unit,
    sendWith: SendMessageWith,
    editTextInDraft: (Int, String) -> Unit,
    deleteMessage: (Int) -> Unit,
    playing: java.io.File?,
    playSet: (java.io.File?) -> Unit,
) {
    val context = LocalContext.current
    var message by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            getMessagesList(id)
            message = savedText[id] ?: ""
        }
    }

    var isExpandedAttachment by remember { mutableStateOf(false) }

    val view = LocalView.current

    val backgroundColor = materialPalette[hash19(id)].primary
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    var animate by remember { mutableStateOf(false) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        selectedUri = uri

        val name = context.getFileName(uri)!!
        val size = context.getFileSize(uri)!!
        val sha256 = context.sha256(uri)!!

        getUploadUri(name, size, sha256, uri)
    }

    var showFileRow by rememberSaveable(draft, id) {
        mutableStateOf(!draft[id].isNullOrEmpty())
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editingId by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }

    var recording by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    var distance by remember { mutableStateOf(Offset(0.dp.value, 0.dp.value)) }

    var recordingState by remember {
        mutableStateOf(false)
    }
    val recorder = remember {
        AudioRecorder(context)
    }
    val recordingStatePP by recorder.state.collectAsState()

    var hasRecordPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
    }

    var messageMenu by remember { mutableStateOf(false) }
    var messageMenuId by remember { mutableIntStateOf(0) }
    var messageMenuOffset by remember {
        mutableStateOf(DpOffset(0.dp, 0.dp))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = if (isLandscape) {
                WindowInsets(
                    left = 0.dp,
                    right = 0.dp,
                    top = ScaffoldDefaults.contentWindowInsets.getTop(LocalDensity.current).dp,
                    bottom = ScaffoldDefaults.contentWindowInsets.getBottom(LocalDensity.current).dp
                )
            } else {
                ScaffoldDefaults.contentWindowInsets
            },
            topBar = {
                if (id != "") {
                    TopAppBar(
                        windowInsets = if (isLandscape) {
                            WindowInsets.statusBars
                        } else {
                            TopAppBarDefaults.windowInsets
                        }, title = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false)
                                    ) {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .aspectRatio(1f)
                                        .clip(CircleShape),
                                    shape = CircleShape,
                                    color = backgroundColor
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.profile_black_content),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        tint = iconColor.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = displayName, modifier = Modifier.weight(1f))
                            }
                        }, navigationIcon = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    back()
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = "Menu"
                                )
                            }
                        }, colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary
                        ), modifier = Modifier.shadow(
                            elevation = 4.dp, shape = RectangleShape, clip = false
                        )
                    )
                }
            }) { innerPadding ->
            Spacer(modifier = Modifier.padding(innerPadding))
            TWallpaper(
                modifier = Modifier.fillMaxSize(),
                colors = listOf("#dbddbb", "#6ba587", "#d5d88d", "#88b884"),
                fps = 60,
                tails = 90,
                animate = animate
            )
            if (id != "") {
                Column(
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxSize()
                ) {
                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    val showButton by remember {
                        derivedStateOf {
                            listState.canScrollForward
                        }
                    }
                    LaunchedEffect(messageList.size, unreadCount) {
                        if (messageList.isEmpty()) return@LaunchedEffect

                        val index =
                            (messageList.size - unreadCount).coerceIn(0, messageList.lastIndex)

                        val visible = listState.layoutInfo.visibleItemsInfo

                        val firstVisible = visible.firstOrNull()?.index ?: return@LaunchedEffect
                        val lastVisible = visible.lastOrNull()?.index ?: return@LaunchedEffect

                        when {
                            index < firstVisible -> listState.scrollToItem(index)
                            index > lastVisible -> listState.scrollToItem(index)
                        }
                    }

                    val modifier = if (showFileRow) {
                        Modifier
                            .weight(1f)
                            .layout { measurable, constraints ->
                                val extra = 64.dp.roundToPx()

                                val newConstraints = constraints.copy(
                                    minHeight = constraints.minHeight + extra,
                                    maxHeight = constraints.maxHeight + extra
                                )

                                val placeable = measurable.measure(newConstraints)

                                layout(
                                    width = placeable.width, height = placeable.height + extra
                                ) {
                                    placeable.placeRelative(0, extra)
                                }
                            }
                    } else {
                        Modifier.weight(1f)
                    }

                    Box(
                        modifier = modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(
                                    requireUnconsumed = false, pass = PointerEventPass.Initial
                                )

                                val up = waitForUpOrCancellation(
                                    pass = PointerEventPass.Initial
                                )

                                if (up != null) {
                                    messageMenuOffset = (DpOffset(
                                        x = up.position.x.toDp(), y = up.position.y.toDp()
                                    ))

                                    println("TAP: $messageMenuOffset")
                                }
                            }
                        }) {
                        LazyColumn(
                            state = listState, contentPadding = PaddingValues(
                                top = 8.dp, bottom = if (showFileRow) 56.dp else 0.dp
                            )
                        ) {
                            items(items = messageList, key = { it.id }) { item ->
                                LaunchedEffect(Unit) {
                                    if (!item.seen && !item.myMessage) {
                                        seenMessage(id, item.id)
                                    }
                                }

                                Message(
                                    isMe = item.myMessage,
                                    content = item.content,
                                    seen = item.seen,
                                    timestamp = item.date,
                                    context = context,
                                    downloadFile = downloadFile,
                                    imageLoader = imageLoader,
                                    serverUrl = serverUrl,
                                    isFileDownloaded = isFileDownloaded,
                                    openMenu = {
                                        messageMenu = true
                                        messageMenuId = item.id
                                    },
                                    playing = playing,
                                    playSet = playSet
                                )
                            }

                            item {
                                Spacer(Modifier.height(0.dp))
                            }
                        }

                        LaunchedEffect(shouldScrollToBottom, messageList.size) {
                            if (shouldScrollToBottom && messageList.isNotEmpty()) {
                                //listState.animateScrollToItem(messageList.lastIndex)
                                listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                            }
                            if (shouldScrollToBottom) {
                                onScrolledToBottom()
                            }
                        }

                        this@Column.AnimatedVisibility(
                            visible = showButton,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .padding(bottom = if (showFileRow) 56.dp else 0.dp),
                            enter = slideInVertically { if (showFileRow) 2 * it else it },// + fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = slideOutVertically { if (showFileRow) 2 * it else it }// + fadeOut() + scaleOut(targetScale = 0.8f)
                        ) {
                            Button(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                                        seenAll(id)
                                    }
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .align(Alignment.BottomEnd)
                                    .shadow(
                                        elevation = 6.dp, shape = CircleShape, clip = false
                                    ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_down),
                                    contentDescription = "Navigate to end",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    UploadList(
                        showFileRow = showFileRow,
                        draft = draft[id] ?: emptyList(),
                        removeFileFromDraft = removeFileFromDraft,
                        removeTextFromDraft = removeTextFromDraft,
                        openText = { textId ->
                            val thisDraft = draft[id]!![textId]

                            if (thisDraft is Draft.Text) {
                                text = thisDraft.text
                                editingId = thisDraft.id
                                showEditSheet = true
                            }
                        })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                isExpandedAttachment = true
                            }, modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.attach),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp, bottom = 10.dp)
                                .shadow(
                                    elevation = 4.dp, shape = RectangleShape, clip = false
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    //color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                //IconButton({
                                //    view.playSoundEffect(SoundEffectConstants.CLICK)
                                //    isExpandedEmoji = true
                                //}) {
                                //    Icon(
                                //        painter = painterResource(R.drawable.emoji),
                                //        contentDescription = null,
                                //        tint = onSurface.copy(alpha = 0.5f)
                                //    )
                                //}

                                val onSurface = MaterialTheme.colorScheme.onSurface
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    factory = { context ->
                                        EditText(context).apply {

                                            background = null

                                            maxLines = 5

                                            hint = "Message..."
                                            setHintTextColor(Color.Gray.toArgb())

                                            setTextColor(onSurface.toArgb())

                                            setOnKeyListener { _, keyCode, event ->

                                                if (keyCode != KeyEvent.KEYCODE_ENTER || event.action != KeyEvent.ACTION_DOWN) {
                                                    return@setOnKeyListener false
                                                }

                                                val shift = event.isShiftPressed
                                                val ctrl = event.isCtrlPressed
                                                val alt = event.isAltPressed

                                                val send = when {
                                                    !shift && !ctrl && !alt -> sendWith.enter
                                                    shift && !ctrl && !alt -> sendWith.shiftEnter
                                                    !shift && ctrl && !alt -> sendWith.ctrlEnter
                                                    !shift && !ctrl && alt -> sendWith.altEnter
                                                    else -> false
                                                }

                                                Log.d(
                                                    "KEY_EVENT",
                                                    "ENTER | shift=$shift | ctrl=$ctrl | alt=$alt | send=$send"
                                                )

                                                if (!send) {
                                                    return@setOnKeyListener false
                                                }

                                                view.playSoundEffect(SoundEffectConstants.CLICK)

                                                if (message.isNotBlank() || showFileRow) {

                                                    message =
                                                        message.replace(Regex("\\n+$"), "").trim()

                                                    scope.launch {
                                                        animate = true
                                                        delay(1000.milliseconds)
                                                        animate = false
                                                    }

                                                    val content = mutableListOf<Content>()

                                                    draft[id].orEmpty().forEach { item ->
                                                        content += when (item) {
                                                            is Draft.File -> Content.File(
                                                                id = item.id,
                                                                fileSize = item.size,
                                                                fileName = item.name
                                                            )

                                                            is Draft.Text -> Content.Text(
                                                                text = item.text
                                                            )
                                                        }
                                                    }

                                                    if (message.isNotBlank()) {
                                                        content += Content.Text(text = message)
                                                    }

                                                    Log.d(
                                                        "KEY_EVENT",
                                                        "SEND | textLength=${message.length} | " + "draftItems=${draft[id].orEmpty().size} | " + "contentItems=${content.size}"
                                                    )

                                                    sendMessage(id, content)

                                                    message = ""
                                                    setSavedText(id, "")
                                                    clearDraft()

                                                } else {
                                                    Log.d("KEY_EVENT", "SEND CANCELLED")
                                                }

                                                true
                                            }

                                            addTextChangedListener(object : TextWatcher {

                                                override fun beforeTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    count: Int,
                                                    after: Int
                                                ) {
                                                }

                                                override fun onTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    before: Int,
                                                    count: Int
                                                ) {
                                                    message = s?.toString() ?: ""
                                                    setSavedText(id, message)
                                                }

                                                override fun afterTextChanged(s: Editable?) {}
                                            })
                                        }
                                    },
                                    update = { editText ->
                                        if (editText.text.toString() != message) {
                                            editText.setText(message)
                                            editText.setSelection(message.length)
                                        }
                                    })
                            }
                        }
                        if (message.isNotBlank() || showFileRow) {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)

                                    message = message.replace(Regex("\\n+$"), "").trim()
                                    scope.launch {
                                        animate = true
                                        delay(1000.milliseconds)
                                        animate = false
                                    }
                                    val content = mutableListOf<Content>()
                                    draft[id].orEmpty().forEach { item ->
                                        content += when (item) {
                                            is Draft.File -> {
                                                Content.File(
                                                    id = item.id,
                                                    fileSize = item.size,
                                                    fileName = item.name
                                                )
                                            }

                                            is Draft.Text -> {
                                                Content.Text(
                                                    text = item.text
                                                )
                                            }
                                        }
                                    }
                                    if (message.isNotBlank()) {
                                        content += Content.Text(
                                            //type = "text",
                                            text = message
                                        )
                                    }
                                    sendMessage(id, content)
                                    message = ""
                                    setSavedText(id, "")
                                    clearDraft()

                                }, modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.send),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Box(
                                Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .pointerInput(hasRecordPermission) {

                                        awaitEachGesture {

                                            val down = awaitFirstDown()

                                            // Permission نداریم
                                            if (!hasRecordPermission) {
                                                permissionLauncher.launch(
                                                    Manifest.permission.RECORD_AUDIO
                                                )
                                                return@awaitEachGesture
                                            }

                                            if (!recordingState) {

                                                recorder.start()
                                                recordingState = true

                                                view.playSoundEffect(
                                                    SoundEffectConstants.CLICK
                                                )
                                            }

                                            val threshold = 168.dp.toPx()

                                            var cancelled = false
                                            var gestureLocked = false

                                            recording = true
                                            locked = false

                                            while (true) {

                                                val event = awaitPointerEvent()

                                                val change =
                                                    event.changes.firstOrNull { it.id == down.id }
                                                        ?: break

                                                if (!change.pressed) {

                                                    if (cancelled) {

                                                        recording = false

                                                        if (recordingState) {
                                                            recorder.cancel()
                                                            recordingState = false
                                                        }

                                                        distance = Offset.Zero

                                                    } else if (!gestureLocked) {

                                                        if (recordingState) {

                                                            val recordedFile = recorder.stop()

                                                            recordedFile?.let { file ->
                                                                val fileUri = Uri.fromFile(file)
                                                                val fileName = file.name
                                                                val fileSize = file.length()
                                                                val fileHash =
                                                                    calculateFileHash(file)

                                                                getUploadUri(
                                                                    fileName,
                                                                    fileSize,
                                                                    fileHash,
                                                                    fileUri
                                                                )
                                                            }

                                                            recording = false
                                                            recordingState = false
                                                        }

                                                        recording = false
                                                    }

                                                    break
                                                }

                                                distance = change.position - down.position

                                                // لغو با کشیدن به چپ
                                                if (!cancelled && !gestureLocked && distance.x < -threshold) {

                                                    cancelled = true
                                                    recording = false

                                                    view.playSoundEffect(
                                                        SoundEffectConstants.CLICK
                                                    )

                                                    distance = Offset.Zero
                                                }

                                                // Lock با کشیدن به بالا
                                                if (!cancelled && !gestureLocked && distance.y < -threshold) {

                                                    gestureLocked = true
                                                    locked = true

                                                    view.playSoundEffect(
                                                        SoundEffectConstants.CLICK
                                                    )

                                                    distance = Offset.Zero
                                                }

                                                change.consume()
                                            }
                                        }
                                    }) {
                                Icon(
                                    painter = painterResource(R.drawable.mic),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        )
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomCenter)
                )
            }
        }

        AnimatedMenuBad(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            width = 192.dp,
            height = 192.dp,
            chord = 192.dp,
            isExpanded = recording,
            close = { recording = false },
            ratioX = 1f,
            offsetX = 24.dp,
            ratioY = 1f,
            offsetY = 24.dp,
            position = Alignment.BottomEnd,
            shadowShape = Quadrant2CircleShape(cornerRadius = 2.dp),
            extruderContent = {
                Box(
                    Modifier
                        .fillMaxSize()
                ) {
                    WobblyRecordingCircle(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(24.dp, 24.dp)
                            .offset(
                                y = -WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .size(96.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    WobblyRecordingCircle(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(8.dp, 8.dp)
                            .offset(
                                y = -WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .size(64.dp)
                            .rotate(90f), color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                y = -WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .size(48.dp),
                        onClick = {
                            val recordedFile = recorder.stop()
                            recordedFile?.let { file ->
                                val fileUri = Uri.fromFile(file)
                                val fileName = file.name
                                val fileSize = file.length()
                                val fileHash = calculateFileHash(file)

                                getUploadUri(
                                    fileName, fileSize, fileHash, fileUri
                                )
                            }

                            recording = false
                            recordingState = false
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.stop_circle),
                            contentDescription = "Stop recording",
                            modifier = Modifier.padding(6.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }) {
            if (!locked) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        painterResource(R.drawable.close),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        tint = lerp(
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.error,
                            (-distance.x / 192f).coerceIn(0f, 1f)
                        )
                    )
                    Icon(
                        painterResource(R.drawable.keyboard_double_arrow_left),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .padding(start = 64.dp) // 24 - 84
                        , tint = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        painterResource(R.drawable.lock),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        tint = lerp(
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.primary,
                            (-distance.y / 192f).coerceIn(0f, 1f)
                        )
                    )
                    Icon(
                        painterResource(R.drawable.keyboard_double_arrow_up),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .padding(top = 64.dp) // 24 - 84
                        , tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    Button(
                        onClick = {
                            recording = false

                            if (recordingState) {
                                recorder.cancel()
                                recordingState = false
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-12).dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.close), contentDescription = null
                        )
                        Text(
                            text = "Cancel"
                        )
                    }

                    IconButton(
                        onClick = {
                            when (recordingStatePP) {
                                AudioRecorder.RecordingState.RECORDING -> {
                                    recorder.pause()
                                }

                                AudioRecorder.RecordingState.PAUSED -> {
                                    recorder.resume()
                                }

                                AudioRecorder.RecordingState.IDLE -> {
                                    // کاری نکن
                                }
                            }
                        }, modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        when (recordingStatePP) {
                            AudioRecorder.RecordingState.RECORDING -> {
                                Icon(
                                    painter = painterResource(R.drawable.pause_circle),
                                    contentDescription = "Pause",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AudioRecorder.RecordingState.PAUSED -> {
                                Icon(
                                    painter = painterResource(R.drawable.play_circle),
                                    contentDescription = "Resume",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AudioRecorder.RecordingState.IDLE -> {
                                // چیزی نمایش نده
                            }
                        }
                    }
                }
            }
        }

        AnimatedMenu(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            width = 224.dp,
            height = 112.dp,
            chord = 250.dp,
            isExpanded = isExpandedAttachment,
            close = { isExpandedAttachment = false },
            ratioY = 1f,
            offsetX = (-24).dp,
            offsetY = 24.dp,
            position = Alignment.BottomStart
        ) {
            Column {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = "Files") }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isExpandedAttachment = false
                        launcher.launch("*/*")  // "image/*", "video/*"
                    }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                        Icon(
                            painterResource(R.drawable.folder), contentDescription = null
                        )
                    }, trailingIcon = { }, enabled = true
                )
                DropdownMenuItem(
                    text = { Text(text = "Text Block") }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSheet = true
                        isExpandedAttachment = false
                    }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                        Icon(
                            painterResource(R.drawable.insert_text),
                            contentDescription = null
                        )
                    }, trailingIcon = { }, enabled = true
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val menuWidth = 240.dp
            val menuHeight = 320.dp
            val menuChord = 400.dp

            val x = messageMenuOffset.x - menuWidth / 2
            val y = messageMenuOffset.y - menuHeight / 2

//            val menuX = x
//                .coerceIn(
//                    0.dp,
//                    maxWidth - menuWidth - 16.dp
//                )
//
//            val menuY = y
//                .coerceIn(
//                    0.dp,
//                    maxHeight - menuHeight - 16.dp
//                )

            val maxX = (maxWidth - menuWidth - 16.dp).coerceAtLeast(0.dp)
            val maxY = (maxHeight - menuHeight - 16.dp).coerceAtLeast(0.dp)

            val menuX = x.coerceIn(0.dp, maxX)
            val menuY = y.coerceIn(0.dp, maxY)

            AnimatedMenu(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        start = menuX,
                        top = menuY
                    ),
                width = menuWidth,
                height = menuHeight,
                chord = menuChord,
                isExpanded = messageMenu,
                close = { messageMenu = false },
                // میتونیم اینجا هم coreIn بزاریم که قشنگ‌تر بشه و همیشه از لبه شروع نکنه
                offsetX = -(messageMenuOffset.x - menuX + 8.dp) + 24.dp,
                offsetY = -(messageMenuOffset.y - menuY + 8.dp + 64.dp) + 24.dp,
                position = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    DropdownMenuItem(
                        text = { Text("Reply") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.reply),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Copy") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.content_copy),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Forward") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.forward),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.edit),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Save") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.bookmark),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Pin") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.keep),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Translate") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.translate),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Select") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.check_box),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.share),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Report") },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.report),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            deleteMessage(messageMenuId)
                            messageMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.delete),
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }

        val scope = rememberCoroutineScope()

        if (showSheet) {
            LaunchedEffect(Unit) {
                sheetState.show()
            }

            ModalBottomSheet(
                shape = RoundedCornerShape(2.dp),
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                    }
                },
                sheetState = sheetState,
                dragHandle = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                sheetGesturesEnabled = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Text Block",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        sheetState.hide()
                                        showSheet = false
                                        attachTextBlock(text)
                                        text = ""
                                    }
                                }
                            }, enabled = text.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    showSheet = false
                                    text = ""
                                }
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(
                                top = 8.dp, bottom = 10.dp
                            )
                            .shadow(
                                elevation = 4.dp, shape = RectangleShape, clip = false
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(2.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val onSurface = MaterialTheme.colorScheme.onSurface

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),

                                factory = { context ->
                                    EditText(context).apply {
                                        background = null

                                        maxLines = 5

                                        hint = "Text..."

                                        setHintTextColor(
                                            android.graphics.Color.GRAY
                                        )

                                        setTextColor(
                                            onSurface.toArgb()
                                        )

                                        addTextChangedListener(object : TextWatcher {

                                            override fun beforeTextChanged(
                                                s: CharSequence?, start: Int, count: Int, after: Int
                                            ) = Unit

                                            override fun onTextChanged(
                                                s: CharSequence?,
                                                start: Int,
                                                before: Int,
                                                count: Int
                                            ) {
                                                text = s?.toString() ?: ""
                                            }

                                            override fun afterTextChanged(
                                                s: Editable?
                                            ) = Unit
                                        })
                                    }
                                },

                                update = { editText ->
                                    if (editText.text.toString() != text) {
                                        editText.setText(text)
                                        editText.setSelection(text.length)
                                    }
                                })
                        }
                    }
                }
            }
        }

        if (showEditSheet) {
            LaunchedEffect(Unit) {
                sheetState.show()
            }

            ModalBottomSheet(
                shape = RoundedCornerShape(2.dp),
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                        showEditSheet = false
                    }
                },
                sheetState = sheetState,
                dragHandle = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                sheetGesturesEnabled = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Text Block",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        sheetState.hide()
                                        showEditSheet = false
                                        editTextInDraft(editingId, text)
                                        text = ""
                                        editingId = 0
                                    }
                                }
                            }, enabled = text.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    showEditSheet = false
                                    text = ""
                                    editingId = 0
                                }
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(
                                top = 8.dp, bottom = 10.dp
                            )
                            .shadow(
                                elevation = 4.dp, shape = RectangleShape, clip = false
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(2.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val onSurface = MaterialTheme.colorScheme.onSurface

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),

                                factory = { context ->
                                    EditText(context).apply {
                                        background = null

                                        maxLines = 5

                                        hint = "Text..."

                                        setHintTextColor(
                                            android.graphics.Color.GRAY
                                        )

                                        setTextColor(onSurface.toArgb())

                                        addTextChangedListener(object : TextWatcher {

                                            override fun beforeTextChanged(
                                                s: CharSequence?, start: Int, count: Int, after: Int
                                            ) = Unit

                                            override fun onTextChanged(
                                                s: CharSequence?,
                                                start: Int,
                                                before: Int,
                                                count: Int
                                            ) {
                                                text = s?.toString() ?: ""
                                            }

                                            override fun afterTextChanged(
                                                s: Editable?
                                            ) = Unit
                                        })
                                    }
                                },

                                update = { editText ->
                                    if (editText.text.toString() != text) {
                                        editText.setText(text)
                                        editText.setSelection(text.length)
                                    }
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UploadList(
    showFileRow: Boolean,
    draft: List<Draft>,
    removeFileFromDraft: (String) -> Unit,
    removeTextFromDraft: (Int) -> Unit,
    openText: (Int) -> Unit,
) {
    val view = LocalView.current
    if (showFileRow && draft.isNotEmpty()) {
        Row(
            modifier = Modifier
                //.fillMaxWidth()
                //.background(MaterialTheme.colorScheme.primary)
                .wrapContentWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            draft.forEach { draftItem ->
                if (draftItem is Draft.File) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 192.dp)
                            .height(48.dp)
                            .shadow(
                                elevation = 4.dp, shape = RectangleShape, clip = false
                            )
                            .background(
                                shape = RoundedCornerShape(2.dp),
                                color = MaterialTheme.colorScheme.surface
                            ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(32.dp),
                            progress = { draftItem.progress })
                        Text(
                            text = draftItem.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                removeFileFromDraft(draftItem.name)
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null
                            )
                        }
                    }
                } else if (draftItem is Draft.Text) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 192.dp)
                            .height(48.dp)
                            .shadow(
                                elevation = 4.dp, shape = RectangleShape, clip = false
                            )
                            .background(
                                shape = RoundedCornerShape(2.dp),
                                color = MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                openText(draftItem.id)
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            painter = painterResource(R.drawable.insert_text),
                            contentDescription = null
                        )
                        Text(
                            text = draftItem.text.replace("\n", " "),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                removeTextFromDraft(draftItem.id)
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Message(
    isMe: Boolean,
    content: List<Content>,
    seen: Boolean,
    timestamp: String,
    context: Context,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    imageLoader: ImageLoader,
    serverUrl: String,
    isFileDownloaded: (String) -> Boolean,
    openMenu: () -> Unit,
    playing: java.io.File?,
    playSet: (java.io.File?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                openMenu()
            }
    ) {
        var lineCount by remember(content) { mutableIntStateOf(0) }
        val formatMessageTime = formatMessageTime(timestamp)
        var timeWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            val maxMessageWidth = minOf(maxWidth * 0.8f, 480.dp)
            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(size = 2.dp),
                modifier = Modifier.widthIn(max = maxMessageWidth),
                //shadowElevation = 2.dp
            ) {
                Box {
                    Column {
                        content.forEachIndexed { index, contentEntity ->
                            if (index == content.size - 1) {
                                when (contentEntity) {
                                    is Content.Text -> {
                                        Text(
                                            text = contentEntity.text.toRichAnnotatedString(
                                                linkColor = MaterialTheme.colorScheme.onPrimary
                                            ), modifier = Modifier
                                                .padding(8.dp)
                                                .padding(
                                                    end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                                                    bottom = if (lineCount > 1) 24.dp else 0.dp
                                                ), onTextLayout = {
                                                if (lineCount == 0) {
                                                    lineCount = it.lineCount
                                                }
                                            })
                                    }

                                    is Content.File -> {
                                        val extension =
                                            contentEntity.fileName.substringAfterLast(".", "")
                                                .takeIf { it.isNotEmpty() }

                                        val downloadName = if (extension != null) {
                                            "${contentEntity.id}.$extension"
                                        } else {
                                            contentEntity.id.toString()
                                        }
                                        var downloadProgress by remember { mutableFloatStateOf(0f) }
                                        var pending by remember { mutableStateOf(false) }
                                        val progressColor =
                                            if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        var downloaded by remember { mutableLongStateOf(0L) }
                                        var showProgress by remember { mutableStateOf(false) }

                                        LaunchedEffect(downloadProgress) {
                                            if (downloadProgress > 0f && downloadProgress < 1f) {
                                                showProgress = true
                                            }

                                            if (downloadProgress == 1f) {
                                                delay(1000.milliseconds)
                                                showProgress = false
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                //.fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    brush = if (!showProgress) {
                                                        SolidColor(Color.Transparent)
                                                    } else {
                                                        Brush.horizontalGradient(
                                                            colorStops = arrayOf(
                                                                0f to progressColor.copy(alpha = 0.2f),
                                                                downloadProgress to progressColor.copy(
                                                                    alpha = 0.2f
                                                                ),
                                                                downloadProgress to Color.Transparent,
                                                                1f to Color.Transparent
                                                            )
                                                        )
                                                    }
                                                )
                                                .clickable {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                },
                                                enabled = !pending
                                            ) {
                                                val thumbnailUrl =
                                                    "http://${serverUrl.substringBefore(":")}:8080/thumb/${contentEntity.id}"

                                                Log.d(
                                                    "THUMB",
                                                    "id=${contentEntity.id}, " + "serverUrl=$serverUrl, " + "url=$thumbnailUrl"
                                                )

                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.draft),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                    AsyncImage(
                                                        model = thumbnailUrl,
                                                        imageLoader = imageLoader,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        onLoading = {
                                                            Log.d("THUMB", "LOADING: $thumbnailUrl")
                                                        },
                                                        onSuccess = {
                                                            Log.d("THUMB", "SUCCESS: $thumbnailUrl")
                                                        },
                                                        onError = {
                                                            Log.e(
                                                                "THUMB",
                                                                "ERROR: $thumbnailUrl",
                                                                it.result.throwable
                                                            )
                                                        })
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(
                                                //modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = contentEntity.fileName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = buildString {
                                                        if (downloadProgress != 0f && downloadProgress != 1f) {
                                                            append(formatFileSize(downloaded))
                                                            append(" / ")
                                                        }
                                                        append(formatFileSize(contentEntity.fileSize))
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                when (contentEntity) {
                                    is Content.Text -> {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = contentEntity.text.toRichAnnotatedString(
                                                linkColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }

                                    is Content.File -> {
                                        val extension =
                                            contentEntity.fileName.substringAfterLast(".", "")
                                                .takeIf { it.isNotEmpty() }

                                        val downloadName = if (extension != null) {
                                            "${contentEntity.id}.$extension"
                                        } else {
                                            contentEntity.id.toString()
                                        }
                                        var downloadProgress by remember { mutableFloatStateOf(0f) }
                                        var pending by remember { mutableStateOf(false) }
                                        val progressColor =
                                            if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        var downloaded by remember { mutableLongStateOf(0L) }
                                        var showProgress by remember { mutableStateOf(false) }

                                        LaunchedEffect(downloadProgress) {
                                            if (downloadProgress > 0f && downloadProgress < 1f) {
                                                showProgress = true
                                            }

                                            if (downloadProgress == 1f) {
                                                delay(1000.milliseconds)
                                                showProgress = false
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    brush = if (!showProgress) {
                                                        SolidColor(Color.Transparent)
                                                    } else {
                                                        Brush.horizontalGradient(
                                                            colorStops = arrayOf(
                                                                0f to progressColor.copy(alpha = 0.2f),
                                                                downloadProgress to progressColor.copy(
                                                                    alpha = 0.2f
                                                                ),
                                                                downloadProgress to Color.Transparent,
                                                                1f to Color.Transparent
                                                            )
                                                        )
                                                    }
                                                )
                                                .clickable {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                },
                                                enabled = !pending
                                            ) {
                                                val thumbnailUrl =
                                                    "http://${serverUrl.substringBefore(":")}:8080/thumb/${contentEntity.id}"

                                                Log.d(
                                                    "THUMB",
                                                    "id=${contentEntity.id}, " + "serverUrl=$serverUrl, " + "url=$thumbnailUrl"
                                                )

                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.draft),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                    AsyncImage(
                                                        model = thumbnailUrl,
                                                        imageLoader = imageLoader,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        onLoading = {
                                                            Log.d("THUMB", "LOADING: $thumbnailUrl")
                                                        },
                                                        onSuccess = {
                                                            Log.d("THUMB", "SUCCESS: $thumbnailUrl")
                                                        },
                                                        onError = {
                                                            Log.e(
                                                                "THUMB",
                                                                "ERROR: $thumbnailUrl",
                                                                it.result.throwable
                                                            )
                                                        })
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = contentEntity.fileName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = buildString {
                                                        if (downloadProgress != 0f && downloadProgress != 1f) {
                                                            append(formatFileSize(downloaded))
                                                            append(" / ")
                                                        }
                                                        append(formatFileSize(contentEntity.fileSize))
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(if (lineCount == 1 && content.size == 1) Alignment.CenterEnd else Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = FontStyle.Normal,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            onTextLayout = {
                                timeWidth = with(density) { it.size.width.toDp() }
                            })
                        if (isMe && seen) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.double_check),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}