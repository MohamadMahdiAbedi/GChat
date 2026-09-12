package ir.gchat

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.SoundEffectConstants
import android.widget.EditText
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.graphics.toColor
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import com.hrm.latex.renderer.model.LatexThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLConnection
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorLong
import io.ratex.compose.RaTeX

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
    } catch (_: Exception) {
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
    savedText: Map<String, Triple<Int?, String, List<Triple<Int, Int, String>>>>,
    setSavedText: (String, String, Int?, List<Triple<Int, Int, String>>) -> Unit,
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
    playSet: (File?) -> Unit,
    attachLaTeX: (String) -> Unit,
    removeLaTeXFromDraft: (Int) -> Unit,
    editLaTeXInDraft: (Int, String) -> Unit,
    attachFileWithId: (Int, String, Long) -> Unit,
    editMessage: (Int, List<Content>) -> Unit
) {
    val context = LocalContext.current
    var messageText by rememberSaveable { mutableStateOf("") }
    var editingMessageId by remember { mutableStateOf<Int?>(null) }
    var displayedEditingMessageId by remember {
        mutableStateOf<Int?>(null)
    }
    var styledMessageText by remember {
        mutableStateOf(
            SpannableStringBuilder()
        )
    }

    var showColorPicker by remember { mutableStateOf(false) }
    var colorPickerColors by remember { mutableStateOf(emptyList<Int>()) }
    var onColorSelectedAction by remember { mutableStateOf({ color: Int -> }) }

    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            val saved = savedText[id]

            getMessagesList(id)

            editingMessageId = saved?.first
            displayedEditingMessageId = editingMessageId

            messageText = saved?.second ?: ""

            val markDown = saved?.third ?: emptyList()

            val spannable = SpannableStringBuilder(messageText)

            markDown.forEach { (start, end, type) ->

                if (start < 0 || end > spannable.length || start >= end) {
                    return@forEach
                }

                when (type[0]) {
                    'b' -> {
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    'i' -> {
                        spannable.setSpan(
                            StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    'u' -> {
                        spannable.setSpan(
                            UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    's' -> {
                        spannable.setSpan(
                            StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    'c' -> {
                        val color = type.substring(1).toColorInt()

                        spannable.setSpan(
                            ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    'h' -> {
                        val color = type.substring(1).toColorInt()

                        spannable.setSpan(
                            BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            styledMessageText = spannable
        }
    }

    LaunchedEffect(styledMessageText) {
        val markDown = mutableListOf<Triple<Int, Int, String>>()

        styledMessageText.getSpans(
                0, styledMessageText.length, CharacterStyle::class.java
            ).forEach { span ->

                val start = styledMessageText.getSpanStart(span)
                val end = styledMessageText.getSpanEnd(span)

                if (start >= end) {
                    return@forEach
                }

                when (span) {

                    is StyleSpan -> {
                        when (span.style) {

                            Typeface.BOLD -> {
                                markDown += Triple(start, end, "b")
                            }

                            Typeface.ITALIC -> {
                                markDown += Triple(start, end, "i")
                            }

                            Typeface.BOLD_ITALIC -> {
                                markDown += Triple(start, end, "b")
                                markDown += Triple(start, end, "i")
                            }
                        }
                    }

                    is UnderlineSpan -> {
                        markDown += Triple(start, end, "u")
                    }

                    is StrikethroughSpan -> {
                        markDown += Triple(start, end, "s")
                    }

                    is ForegroundColorSpan -> {
                        markDown += Triple(
                            start,
                            end,
                            "c" + String.format("#%06X", 0xFFFFFF and span.foregroundColor)
                        )
                    }

                    is BackgroundColorSpan -> {
                        markDown += Triple(
                            start,
                            end,
                            "h" + String.format("#%06X", 0xFFFFFF and span.backgroundColor)
                        )
                    }
                }
            }
        setSavedText(id, messageText, editingMessageId, markDown)
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
    var showLatexSheet by remember { mutableStateOf(false) }
    var showEditLatexSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editingAttachmentId by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var latex by remember { mutableStateOf("") }

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
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
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

    val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    val lastNotZeroNavigationBarHeight by remember { mutableIntStateOf(navigationBarHeight) }

    val density = LocalDensity.current

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
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
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
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                back()
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                //contentDescription = "Menu"
                                contentDescription = null
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
                    val firstUnreadIndex by rememberSaveable { mutableIntStateOf(messageList.indexOfFirst { !it.seen && !it.myMessage }) }
                    // بعدا با یه چیزی بذار که فقط یه بار پاس داده بشه
                    LaunchedEffect(messageList.size, unreadCount) {
                        if (messageList.isEmpty()) return@LaunchedEffect

                        val index =
                            (messageList.size - firstUnreadIndex).coerceIn(0, messageList.lastIndex)

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
                        val selectionState = rememberSelectionState()
                        BackHandler(enabled = selectionState.selectedTexts.isNotEmpty()) {
                            selectionState.clear()
                        }
                        LaunchedEffect(firstUnreadIndex, messageList.size) {
                            if (firstUnreadIndex >= 0) {
                                listState.scrollToItem(firstUnreadIndex)
                            }
                        }

                        var imeScrollOffset by remember { mutableIntStateOf(0) }

                        var imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

                        if (imeBottom > lastNotZeroNavigationBarHeight) {
                            imeBottom -= lastNotZeroNavigationBarHeight
                        } else {
                            imeBottom = 0
                        }

                        LaunchedEffect(imeBottom) {
                            var delta = imeBottom - imeScrollOffset

                            if (delta > 0) {
                                while (showButton && delta > 0) {
                                    val before =
                                        listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset

                                    listState.scrollBy(1f)

                                    val after =
                                        listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset

                                    if (before == after) break

                                    imeScrollOffset++
                                    delta--
                                }
                            } else if (delta < 0) {
                                while (showButton && delta < 0 && imeScrollOffset > 0) {
                                    val before =
                                        listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset

                                    listState.scrollBy(-1f)

                                    val after =
                                        listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset

                                    if (before == after) break

                                    imeScrollOffset--
                                    delta++
                                }
                            }

                            if (imeBottom == 0) {
                                imeScrollOffset = 0
                            }
                        }
                        SelectionContainer(state = selectionState) {
                            LazyColumn(
                                state = listState, contentPadding = PaddingValues(
                                    top = 8.dp, bottom = if (showFileRow) 56.dp else 0.dp
                                )
                            ) {
                                itemsIndexed(
                                    items = messageList, key = { _, item -> item.id }) { _, item ->

                                    if (item.id == messageList.getOrNull(firstUnreadIndex)?.id) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.unread_messages),
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surface.copy(
                                                            alpha = 0.5f
                                                        ), shape = CircleShape
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    LaunchedEffect(item.id, item.seen) {
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
                                        playSet = playSet,
                                    )
                                }

                                item {
                                    Spacer(Modifier.height(0.dp))
                                }
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
                            //enter = slideInVertically { if (showFileRow) 2 * it else it }, // + fadeIn() + scaleIn(initialScale = 0.8f),
                            enter = slideInVertically {
                                with(density) {
                                    if (showFileRow) 128.dp.roundToPx()
                                    else 64.dp.roundToPx()
                                }
                            },
                            //exit = slideOutVertically { if (showFileRow) 2 * it else it } // + fadeOut() + scaleOut(targetScale = 0.8f)
                            exit = slideOutVertically {
                                with(density) {
                                    if (showFileRow) 128.dp.roundToPx()
                                    else 64.dp.roundToPx()
                                }
                            }) {
                            Button(
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                                        seenAll(id)
                                    }
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .requiredSize(48.dp)
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
                                    //contentDescription = "Navigate to end",
                                    contentDescription = null,
                                    modifier = Modifier.requiredSize(24.dp)
                                )
                            }

//                            if (unreadCount > 0) {
//                                Box(
//                                    modifier = Modifier
//                                        .align(Alignment.TopCenter)
//                                        .padding(4.dp)
//                                        .requiredHeight(20.dp)
//                                        .defaultMinSize(minWidth = 20.dp)
//                                        .background(
//                                            MaterialTheme.colorScheme.primary, CircleShape
//                                        )
//                                        .padding(horizontal = 5.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = unreadCount.toString(),
//                                        color = MaterialTheme.colorScheme.onPrimary,
//                                        style = MaterialTheme.typography.labelSmall
//                                    )
//                                }
//                            }
                        }

                        this@Column.AnimatedVisibility(
                            visible = editingMessageId != null,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .padding(bottom = if (showFileRow) 56.dp else 0.dp),
                            //enter = slideInVertically { if (showFileRow) 2 * it else it }, // + fadeIn() + scaleIn(initialScale = 0.8f),
                            enter = slideInVertically {
                                with(density) {
                                    if (showFileRow) 128.dp.roundToPx()
                                    else 64.dp.roundToPx()
                                }
                            },
                            //exit = slideOutVertically { if (showFileRow) 2 * it else it } // + fadeOut() + scaleOut(targetScale = 0.8f)
                            exit = slideOutVertically {
                                with(density) {
                                    if (showFileRow) 128.dp.roundToPx()
                                    else 64.dp.roundToPx()
                                }
                            }) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        editingMessageId?.let { serverMessageId ->

                                            val index = messageList.indexOfFirst {
                                                it.id == serverMessageId
                                            }

                                            if (index >= 0) {
                                                listState.scrollToItem(index)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .requiredHeight(48.dp)
                                    .padding(end = 64.dp)
                                    .align(Alignment.BottomStart)
                                    .shadow(
                                        elevation = 6.dp, shape = CircleShape, clip = false
                                    ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(
                                    top = 0.dp, start = 0.dp, bottom = 0.dp, end = 24.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                clearDraft()
                                                messageText = ""
                                                setSavedText(id, "", null, emptyList())
                                                editingMessageId = null
                                                delay(300.milliseconds)
                                                displayedEditingMessageId = null
                                            }
                                        }) {
                                        Icon(
                                            painter = painterResource(R.drawable.close),
                                            contentDescription = null,
                                            modifier = Modifier.requiredSize(24.dp)
                                        )
                                    }

                                    displayedEditingMessageId?.let { serverMessageId ->

                                        val index = messageList.indexOfFirst {
                                            it.id == serverMessageId
                                        }

                                        if (index >= 0) {
                                            val message = messageList[index]

                                            val files =
                                                message.content.filterIsInstance<Content.File>()
                                            val texts =
                                                message.content.filterIsInstance<Content.Text>()
                                            val latexs =
                                                message.content.filterIsInstance<Content.LaTeX>()

                                            if (files.isNotEmpty()) {
                                                val visibleFiles = if (files.size > 3) {
                                                    files.take(2)
                                                } else {
                                                    files
                                                }

                                                visibleFiles.forEach { content ->
                                                    Surface(
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(16.dp)
                                                    ) {
                                                        val thumbnailUrl =
                                                            "http://${serverUrl.substringBefore(":")}:8080/thumb/${content.id}"

                                                        SubcomposeAsyncImage(
                                                            model = thumbnailUrl,
                                                            imageLoader = imageLoader,
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .aspectRatio(1f)
                                                                .fillMaxSize(),
                                                            contentScale = ContentScale.Crop,
                                                            loading = {
                                                                Icon(
                                                                    painter = painterResource(R.drawable.draft),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    tint = MaterialTheme.colorScheme.onSurface
                                                                )
                                                            },
                                                            error = {
                                                                Icon(
                                                                    painter = painterResource(R.drawable.draft),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    tint = MaterialTheme.colorScheme.onSurface
                                                                )
                                                            },
                                                            onLoading = {
                                                                Log.d(
                                                                    "THUMB",
                                                                    "LOADING: $thumbnailUrl"
                                                                )
                                                            },
                                                            onSuccess = {
                                                                Log.d(
                                                                    "THUMB",
                                                                    "SUCCESS: $thumbnailUrl"
                                                                )
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

                                                if (files.size > 3) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(16.dp),
                                                        color = MaterialTheme.colorScheme.primary
                                                    ) {
                                                        Box(
                                                            contentAlignment = Alignment.Center,
                                                            modifier = Modifier.fillMaxSize()
                                                        ) {
                                                            Text(
                                                                text = "+${
                                                                    (files.size - 2).toLocalizedNumber(
                                                                        LocalContext.current
                                                                    )
                                                                }",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onPrimary
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if ((texts + latexs).isNotEmpty()) {
                                                Text(
                                                    text = buildAnnotatedString {
                                                        (texts + latexs).forEachIndexed { textIndex, content ->
                                                            if (textIndex > 0) append(" ")

                                                            append(
                                                                when (content) {
                                                                    is Content.Text -> content.text
                                                                    is Content.LaTeX -> content.text
                                                                    else -> ""
                                                                }.replace("\n", " ")
                                                            )
                                                        }
                                                    },
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
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
                                editingAttachmentId = thisDraft.id
                                showEditSheet = true
                            }
                        },
                        openLaTeX = { latexId ->
                            val thisDraft = draft[id]!![latexId]

                            if (thisDraft is Draft.LaTeX) {
                                latex = thisDraft.text
                                editingAttachmentId = thisDraft.id
                                showEditLatexSheet = true
                            }
                        },
                        removeLaTeXFromDraft = removeLaTeXFromDraft
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        IconButton(
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
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
                                    shape = RoundedCornerShape(2.dp)
                                )
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                MessageTextField(
                                    sendWith = sendWith,
                                    showFileRow = showFileRow,
                                    setMessageText = { text ->
                                        messageText = text
                                        val markDown = mutableListOf<Triple<Int, Int, String>>()

                                        styledMessageText.getSpans(
                                                0,
                                                styledMessageText.length,
                                                CharacterStyle::class.java
                                            ).forEach { span ->

                                                val start = styledMessageText.getSpanStart(span)
                                                val end = styledMessageText.getSpanEnd(span)

                                                if (start >= end) {
                                                    return@forEach
                                                }

                                                when (span) {

                                                    is StyleSpan -> {
                                                        when (span.style) {

                                                            Typeface.BOLD -> {
                                                                markDown += Triple(start, end, "b")
                                                            }

                                                            Typeface.ITALIC -> {
                                                                markDown += Triple(start, end, "i")
                                                            }

                                                            Typeface.BOLD_ITALIC -> {
                                                                markDown += Triple(start, end, "b")
                                                                markDown += Triple(start, end, "i")
                                                            }
                                                        }
                                                    }

                                                    is UnderlineSpan -> {
                                                        markDown += Triple(start, end, "u")
                                                    }

                                                    is StrikethroughSpan -> {
                                                        markDown += Triple(start, end, "s")
                                                    }

                                                    is ForegroundColorSpan -> {
                                                        markDown += Triple(
                                                            start, end, "c" + String.format(
                                                                "#%06X",
                                                                0xFFFFFF and span.foregroundColor
                                                            )
                                                        )
                                                    }

                                                    is BackgroundColorSpan -> {
                                                        markDown += Triple(
                                                            start, end, "h" + String.format(
                                                                "#%06X",
                                                                0xFFFFFF and span.backgroundColor
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        setSavedText(id, text, editingMessageId, markDown)
                                    },
                                    showAnimation = {
                                        scope.launch {
                                            animate = true
                                            delay(1000.milliseconds)
                                            animate = false
                                        }
                                    },
                                    draftSize = draft[id].orEmpty().size,
                                    getDraft = { content ->
                                        draft[id].orEmpty().forEach { item ->
                                            content += when (item) {
                                                is Draft.File -> {
                                                    Content.File(
                                                        id = item.id,
                                                        fileSize = item.size,
                                                        fileName = item.name
                                                    )
                                                }

                                                is Draft.LaTeX -> {
                                                    Content.LaTeX(text = item.text)
                                                }

                                                is Draft.Text -> {
                                                    Content.Text(
                                                        text = item.text, markDown = item.markDown
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    sendMessage = { content ->
                                        sendMessage(id, content)
                                        messageText = ""

                                        setSavedText(id, "", null, emptyList())

                                        clearDraft()
                                    },
                                    messageText = messageText,
                                    styledMessageText = styledMessageText,
                                    setStyledMessageText = { newStyledMessageText ->
                                        styledMessageText = newStyledMessageText
                                    },
                                    showColorPicker = { colors, onColorSelected ->
                                        colorPickerColors = colors
                                        showColorPicker = true
                                        onColorSelectedAction = onColorSelected
                                    }, onSelectionChanged = { start, end ->
                                        if (showColorPicker) {
                                            showColorPicker = false
                                            colorPickerColors = emptyList()
                                            onColorSelectedAction = {}
                                        }
                                    }
                                )
                            }
                        }
                        if (editingMessageId != null) {
                            IconButton(
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)

                                    messageText = messageText.replace(Regex("\\n+$"), "").trim()
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
                                                    text = item.text, markDown = item.markDown
                                                )
                                            }

                                            is Draft.LaTeX -> {
                                                Content.LaTeX(
                                                    text = item.text
                                                )
                                            }
                                        }
                                    }
                                    if (messageText.isNotBlank()) {
                                        content += Content.Text(
                                            //type = "text",
                                            text = messageText, markDown = emptyList()
                                        )
                                    }
                                    editingMessageId?.let { editMessage(it, content) }
                                    messageText = ""
                                    setSavedText(id, "", null, emptyList())
                                    clearDraft()
                                    editingMessageId = null

                                }, modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.edit),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            if (messageText.isNotBlank() || showFileRow) {
                                IconButton(
                                    onClick = {
                                        //view.playSoundEffect(SoundEffectConstants.CLICK)

                                        messageText = messageText.replace(Regex("\\n+$"), "").trim()
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
                                                        text = item.text, markDown = item.markDown
                                                    )
                                                }

                                                is Draft.LaTeX -> {
                                                    Content.LaTeX(
                                                        text = item.text
                                                    )
                                                }
                                            }
                                        }
                                        val markDown: MutableList<Triple<Int, Int, String>> =
                                            mutableListOf()

                                        styledMessageText.getSpans(
                                                0,
                                                styledMessageText.length,
                                                CharacterStyle::class.java
                                            ).forEach { span ->

                                                val start = styledMessageText.getSpanStart(span)
                                                val end = styledMessageText.getSpanEnd(span)

                                                if (start >= end) {
                                                    return@forEach
                                                }

                                                when (span) {

                                                    is StyleSpan -> {

                                                        when (span.style) {

                                                            Typeface.BOLD -> {
                                                                markDown += Triple(start, end, "b")
                                                            }

                                                            Typeface.ITALIC -> {
                                                                markDown += Triple(start, end, "i")
                                                            }

                                                            Typeface.BOLD_ITALIC -> {
                                                                markDown += Triple(start, end, "b")
                                                                markDown += Triple(start, end, "i")
                                                            }
                                                        }
                                                    }

                                                    is UnderlineSpan -> {
                                                        markDown += Triple(start, end, "u")
                                                    }

                                                    is StrikethroughSpan -> {
                                                        markDown += Triple(start, end, "s")
                                                    }

                                                    is ForegroundColorSpan -> {
                                                        markDown += Triple(
                                                            start, end, "c" + String.format(
                                                                "#%06X",
                                                                0xFFFFFF and span.foregroundColor
                                                            )
                                                        )
                                                    }

                                                    is BackgroundColorSpan -> {
                                                        markDown += Triple(
                                                            start, end, "h" + String.format(
                                                                "#%06X",
                                                                0xFFFFFF and span.backgroundColor
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        if (messageText.isNotBlank()) {
                                            content += Content.Text(
                                                //type = "text",
                                                text = messageText, markDown = markDown
                                            )
                                        }
                                        sendMessage(id, content)
                                        messageText = ""
                                        setSavedText(id, "", null, emptyList())
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

                                                // --------------------------------
                                                // Long Press
                                                // --------------------------------

                                                val threshold = 168.dp.toPx()
                                                val longPressTime = 200L

                                                var cancelled = false
                                                var gestureLocked = false

                                                recording = false
                                                locked = false
                                                distance = Offset.Zero

                                                val longPressed = withTimeoutOrNull(longPressTime) {

                                                    while (true) {

                                                        val event = awaitPointerEvent()

                                                        val change = event.changes.firstOrNull {
                                                            it.id == down.id
                                                        } ?: return@withTimeoutOrNull false

                                                        if (!change.pressed) {
                                                            return@withTimeoutOrNull false
                                                        }

                                                        distance = change.position - down.position

                                                        // لغو قبل از شروع ضبط
                                                        if (distance.x < -threshold) {

                                                            cancelled = true
                                                            distance = Offset.Zero

                                                            view.playSoundEffect(
                                                                SoundEffectConstants.CLICK
                                                            )

                                                            change.consume()

                                                            return@withTimeoutOrNull false
                                                        }

                                                        change.consume()
                                                    }

                                                } == null

                                                // --------------------------------
                                                // Tap کوتاه
                                                // --------------------------------

                                                if (!longPressed || cancelled) {

                                                    recording = false
                                                    locked = false
                                                    distance = Offset.Zero

                                                    return@awaitEachGesture
                                                }

                                                // --------------------------------
                                                // Long Press → شروع ضبط
                                                // --------------------------------

                                                if (!recordingState) {

                                                    recorder.start()
                                                    recordingState = true

                                                    view.playSoundEffect(
                                                        SoundEffectConstants.CLICK
                                                    )
                                                }

                                                recording = true
                                                locked = false
                                                distance = Offset.Zero

                                                // --------------------------------
                                                // ادامه Gesture
                                                // --------------------------------

                                                while (true) {

                                                    val event = awaitPointerEvent()

                                                    val change = event.changes.firstOrNull {
                                                        it.id == down.id
                                                    } ?: break

                                                    // --------------------------------
                                                    // انگشت برداشته شد
                                                    // --------------------------------

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

                                                        } else {

                                                            // Locked:
                                                            // با رها کردن انگشت ضبط ادامه پیدا می‌کند.
                                                            distance = Offset.Zero
                                                        }

                                                        break
                                                    }

                                                    distance = change.position - down.position

                                                    // --------------------------------
                                                    // لغو با کشیدن به چپ
                                                    //
                                                    // دقت کن:
                                                    // اینجا recorder.cancel() نمی‌کنیم!
                                                    // دقیقاً مثل نسخه سالم
                                                    // --------------------------------

                                                    if (!cancelled && !gestureLocked && distance.x < -threshold) {

                                                        cancelled = true
                                                        recording = false

                                                        view.playSoundEffect(
                                                            SoundEffectConstants.CLICK
                                                        )

                                                        distance = Offset.Zero
                                                    }

                                                    // --------------------------------
                                                    // Lock با کشیدن به بالا
                                                    // --------------------------------

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
                    AnimatedVisibility(
                        visible = showColorPicker,
                        enter = expandVertically(
                            expandFrom = Alignment.Top
                        ),
                        exit = shrinkVertically(
                            shrinkTowards = Alignment.Top
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        showColorPicker = false
                                        colorPickerColors = emptyList()
                                        onColorSelectedAction = {}
                                    },
                                painter = painterResource(R.drawable.keyboard_arrow_down),
                                contentDescription = null
                            )

                            colorPickerColors.forEach { color ->
                                Spacer(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(color))
                                        .clickable {
                                            onColorSelectedAction(color)
                                            showColorPicker = false
                                            colorPickerColors = emptyList()
                                            onColorSelectedAction = {}
                                        }
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
                    Modifier.fillMaxSize()
                ) {
                    val ime = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                    val navigationBars =
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    WobblyRecordingCircle(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(24.dp, 24.dp)
                            .offset(
                                y = if (ime < navigationBars) -navigationBars else 0.dp
                            )
                            .offset(
                                y = -ime
                            )
                            .size(96.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    WobblyRecordingCircle(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(8.dp, 8.dp)
                            .offset(
                                y = if (ime < navigationBars) -navigationBars else 0.dp
                            )
                            .offset(
                                y = -ime
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
                                y = if (ime < navigationBars) -navigationBars else 0.dp
                            )
                            .offset(
                                y = -ime
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
                            //contentDescription = "Stop recording",
                            contentDescription = null,
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
                        ,
                        tint = MaterialTheme.colorScheme.onSurface
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
                        ,
                        tint = MaterialTheme.colorScheme.onSurface
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
                            text = stringResource(R.string.cancel)
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
                                    contentDescription = null, //contentDescription = "Pause",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AudioRecorder.RecordingState.PAUSED -> {
                                Icon(
                                    painter = painterResource(R.drawable.play_circle),
                                    contentDescription = null, //contentDescription = "Resume",
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
            width = 200.dp,
            height = 160.dp,
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
                    text = { Text(text = stringResource(R.string.latex)) }, onClick = {
                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                    showLatexSheet = true
                    isExpandedAttachment = false
                }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                    Icon(
                        painterResource(R.drawable.function), contentDescription = null
                    )
                }, trailingIcon = { }, enabled = true
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.files)) }, onClick = {
                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                    isExpandedAttachment = false
                    launcher.launch("*/*")  // "image/*", "video/*"
                }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                    Icon(
                        painterResource(R.drawable.folder), contentDescription = null
                    )
                }, trailingIcon = { }, enabled = true
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.text_block)) },
                    onClick = {
                        //view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSheet = true
                        isExpandedAttachment = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.insert_text), contentDescription = null
                        )
                    },
                    trailingIcon = { },
                    enabled = true
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
                        start = menuX, top = menuY
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
                        text = { Text(stringResource(R.string.reply)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.reply), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copy)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.content_copy), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.forward)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.forward), contentDescription = null
                            )
                        })

                    DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = {
                        val messageContentList = messageList.find { it.id == messageMenuId }

                        if (messageContentList != null) {
                            clearDraft()

                            messageContentList.content.forEachIndexed { index, content ->
                                when (content) {
                                    is Content.Text -> {
                                        if (index == messageContentList.content.lastIndex) {
                                            val markDown = mutableListOf<Triple<Int, Int, String>>()

                                            styledMessageText.getSpans(
                                                    0,
                                                    styledMessageText.length,
                                                    CharacterStyle::class.java
                                                ).forEach { span ->

                                                    val start = styledMessageText.getSpanStart(span)
                                                    val end = styledMessageText.getSpanEnd(span)

                                                    if (start >= end) {
                                                        return@forEach
                                                    }

                                                    when (span) {

                                                        is StyleSpan -> {
                                                            when (span.style) {

                                                                Typeface.BOLD -> {
                                                                    markDown += Triple(
                                                                        start, end, "b"
                                                                    )
                                                                }

                                                                Typeface.ITALIC -> {
                                                                    markDown += Triple(
                                                                        start, end, "i"
                                                                    )
                                                                }

                                                                Typeface.BOLD_ITALIC -> {
                                                                    markDown += Triple(
                                                                        start, end, "b"
                                                                    )
                                                                    markDown += Triple(
                                                                        start, end, "i"
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        is UnderlineSpan -> {
                                                            markDown += Triple(start, end, "u")
                                                        }

                                                        is StrikethroughSpan -> {
                                                            markDown += Triple(start, end, "s")
                                                        }

                                                        is ForegroundColorSpan -> {
                                                            markDown += Triple(
                                                                start, end, "c" + String.format(
                                                                    "#%06X",
                                                                    0xFFFFFF and span.foregroundColor
                                                                )
                                                            )
                                                        }

                                                        is BackgroundColorSpan -> {
                                                            markDown += Triple(
                                                                start, end, "h" + String.format(
                                                                    "#%06X",
                                                                    0xFFFFFF and span.backgroundColor
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            setSavedText(
                                                id, content.text, editingMessageId, markDown
                                            )
                                            messageText = content.text
                                        } else {
                                            attachTextBlock(content.text)
                                        }
                                    }

                                    is Content.LaTeX -> {
                                        attachLaTeX(content.text)
                                    }

                                    is Content.File -> {
                                        attachFileWithId(
                                            content.id, content.fileName, content.fileSize
                                        )
                                    }
                                }
                            }
                        }
                        editingMessageId = messageMenuId
                        displayedEditingMessageId = messageMenuId
                        messageMenu = false
                    }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.edit), contentDescription = null
                        )
                    })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.save)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.bookmark), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pin)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.keep), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.translate)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.translate), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.select)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.check_box), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.share)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.share), contentDescription = null
                            )
                        })

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.report)) },
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.report), contentDescription = null
                            )
                        })

                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = {
                        deleteMessage(messageMenuId)
                        messageMenu = false
                    }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                        Icon(
                            painterResource(R.drawable.delete), contentDescription = null
                        )
                    })

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
                shape = RectangleShape, //RoundedCornerShape(2.dp,2.dp,0.dp,0.dp),
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
                            text = stringResource(R.string.text_block),
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
                            val editTextHint = stringResource(R.string.text)
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),

                                factory = { context ->
                                    EditText(context).apply {
                                        background = null

                                        maxLines = 5

                                        hint = editTextHint

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

        if (showLatexSheet) {
            LaTeXSuperEditor(
                initialText = latex,

                onTextSubmit = { result ->
                    attachLaTeX(result)
                    latex = ""
                    showLatexSheet = false
                },

                onDismiss = {
                    latex = ""
                    showLatexSheet = false
                })
        }

        if (showEditLatexSheet) {
            LaTeXSuperEditor(
                initialText = latex,

                onTextSubmit = { result ->
                    editLaTeXInDraft(editingAttachmentId, result)
                    latex = ""
                    showEditLatexSheet = false
                },

                onDismiss = {
                    latex = ""
                    showEditLatexSheet = false
                })
        }

        if (showEditSheet) {
            LaunchedEffect(Unit) {
                sheetState.show()
            }

            ModalBottomSheet(
                shape = RectangleShape,//RoundedCornerShape(2.dp,2.dp,0.dp,0.dp),
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
                            text = stringResource(R.string.edit_text_block),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        sheetState.hide()
                                        showEditSheet = false
                                        editTextInDraft(editingAttachmentId, text)
                                        text = ""
                                        editingAttachmentId = 0
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
                                    editingAttachmentId = 0
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
                            val editTextHint = stringResource(R.string.text)
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),

                                factory = { context ->
                                    EditText(context).apply {
                                        background = null

                                        maxLines = 5

                                        hint = editTextHint

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaTeXSuperEditor(
    initialText: String = "", onTextSubmit: (String) -> Unit = {}, onDismiss: () -> Unit = {}
) {
    var textFieldValue by rememberSaveable(
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            TextFieldValue(
                text = initialText, selection = TextRange(initialText.length)
            )
        )
    }

    var category by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }

    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val toolbarScroll = rememberScrollState()

    val text = textFieldValue.text

    /*
     * درج متن در محل cursor یا جایگزینی selection
     */
    fun insert(
        value: String, cursor: Int = value.length
    ) {
        val current = textFieldValue
        val currentText = current.text

        val start = current.selection.start.coerceIn(0, currentText.length)

        val end = current.selection.end.coerceIn(0, currentText.length)

        val from = minOf(start, end)
        val to = maxOf(start, end)

        val newText = currentText.substring(0, from) + value + currentText.substring(to)

        val newCursor = (from + cursor).coerceIn(0, newText.length)

        textFieldValue = TextFieldValue(
            text = newText, selection = TextRange(newCursor)
        )
    }

    /*
     * قرار دادن before و after دور selection
     *
     * اگر چیزی انتخاب نشده باشد:
     *
     *     cursor
     *
     * تبدیل می‌شود به:
     *
     *     before|after
     *
     * اگر selection وجود داشته باشد:
     *
     *     abc[x]def
     *
     * تبدیل می‌شود به:
     *
     *     abc[before x after]def
     */
    fun wrap(
        before: String, after: String
    ) {
        val current = textFieldValue
        val currentText = current.text

        val start = current.selection.start.coerceIn(0, currentText.length)

        val end = current.selection.end.coerceIn(0, currentText.length)

        val from = minOf(start, end)
        val to = maxOf(start, end)

        val selected = currentText.substring(from, to)

        val replacement = before + selected + after

        val newText = currentText.substring(0, from) + replacement + currentText.substring(to)

        /*
         * اگر selection خالی باشد،
         * cursor را بین before و after می‌گذاریم.
         *
         * مثال:
         *
         * \sqrt{|}
         */
        val newCursor = if (selected.isEmpty()) {
            from + before.length
        } else {/*
                 * اگر متن انتخاب شده باشد،
                 * بعد از کل عبارت قرار می‌گیرد.
                 *
                 * مثال:
                 *
                 * \sqrt{x}|
                 */
            from + before.length + selected.length + after.length
        }

        textFieldValue = TextFieldValue(
            text = newText, selection = TextRange(
                newCursor.coerceIn(0, newText.length)
            )
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,

        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },

        shape = RectangleShape,

        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,

        sheetGesturesEnabled = false,

        dragHandle = {}) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                )
        ) {

            /*
             * Header
             */
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(R.string.latex_editor),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        zoom = (zoom + 0.1f).coerceAtMost(2f)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_in),
                        contentDescription = "Zoom in"
                    )
                }

                IconButton(
                    onClick = {
                        zoom = (zoom - 0.1f).coerceAtLeast(0.5f)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_out),
                        contentDescription = "Zoom out"
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.close), contentDescription = "Close"
                    )
                }

                IconButton(
                    enabled = text.isNotBlank(), onClick = {
                        scope.launch {
                            sheetState.hide()
                            onTextSubmit(text)
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.check), contentDescription = "Done"
                    )
                }
            }

            /*
             * Preview
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(
                        bottom = 4.dp, top = 8.dp
                    ), shape = RoundedCornerShape(2.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(12.dp),

                    contentAlignment = Alignment.Center
                ) {

                    if (text.isBlank()) {

                        Text(
                            text = stringResource(R.string.preview), color = Color.Gray
                        )

                    } else {

                        RaTeX(
                            modifier = Modifier.wrapContentWidth(),
                            latex = text,
                            fontSize = (18f * zoom).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            displayMode = true
                        )
                    }
                }
            }

            /*
             * Text editor
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(vertical = 4.dp),

                shape = RoundedCornerShape(2.dp)
            ) {

                val editTextHint = stringResource(
                    R.string.write_your_latex_here
                )

                TextField(
                    value = textFieldValue,

                    onValueChange = { newValue ->
                        /*
                         * این مهم‌ترین قسمت است.
                         *
                         * متن + cursor + selection
                         * همگی از TextFieldValue می‌آیند.
                         *
                         * بنابراین اگر کاربر:
                         *
                         * - cursor را جابه‌جا کند
                         * - متن را انتخاب کند
                         * - وسط متن تایپ کند
                         * - Backspace بزند
                         * - متن را paste کند
                         *
                         * state کاملاً با TextField هماهنگ می‌ماند.
                         */
                        textFieldValue = newValue
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),

                    placeholder = {
                        Text(
                            text = editTextHint, color = Color.Gray
                        )
                    },

                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),

                    singleLine = false,

                    maxLines = 5,

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Default
                    ),

                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,

                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,

                        focusedTextColor = MaterialTheme.colorScheme.onSurface,

                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                        disabledTextColor = MaterialTheme.colorScheme.onSurface,

                        errorTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

                shape = RoundedCornerShape(2.dp)
            ) {

                Column {

                    ScrollableTabRow(
                        modifier = Modifier.fillMaxWidth(),

                        selectedTabIndex = category,

                        edgePadding = 0.dp
                    ) {

                        listOf(
                            "Basic",
                            "Greek",
                            "Calculus",
                            "Relations",
                            "Functions",
                            "Sets",
                            "Arrows",
                            "Matrix"
                        ).forEachIndexed { index, name ->

                            Tab(
                                selected = category == index,

                                onClick = {
                                    category = index
                                },

                                text = {
                                    Text(name)
                                })
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                toolbarScroll
                            ),

                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        when (category) {

                            /*
                             * BASIC
                             */
                            0 -> {

                                listOf(

                                    "a/b" to {
                                        wrap(
                                            "\\frac{", "}{}"
                                        )
                                    },

                                    "√" to {
                                        wrap(
                                            "\\sqrt{", "}"
                                        )
                                    },

                                    "√ⁿ" to {
                                        insert("\\sqrt[]{}")
                                    },

                                    "x²" to {
                                        wrap(
                                            "^{", "}"
                                        )
                                    },

                                    "xₙ" to {
                                        wrap(
                                            "_{", "}"
                                        )
                                    },

                                    "( )" to {
                                        wrap(
                                            "\\left(", "\\right)"
                                        )
                                    },

                                    "[ ]" to {
                                        wrap(
                                            "\\left[", "\\right]"
                                        )
                                    },

                                    "{ }" to {
                                        wrap(
                                            "\\left\\{", "\\right\\}"
                                        )
                                    },

                                    "|x|" to {
                                        wrap(
                                            "\\left|", "\\right|"
                                        )
                                    },

                                    "x̂" to {
                                        wrap(
                                            "\\hat{", "}"
                                        )
                                    },

                                    "x̄" to {
                                        wrap(
                                            "\\bar{", "}"
                                        )
                                    },

                                    "∞" to {
                                        insert("\\infty")
                                    },

                                    "±" to {
                                        insert("\\pm")
                                    },

                                    "×" to {
                                        insert("\\times")
                                    },

                                    "÷" to {
                                        insert("\\div")
                                    }

                                ).forEach { (label, action) ->

                                    TextButton(
                                        onClick = action
                                    ) {
                                        Text(
                                            text = label, fontSize = 17.sp
                                        )
                                    }
                                }
                            }

                            /*
                             * GREEK
                             */
                            1 -> {

                                listOf(
                                    "α" to "\\alpha",
                                    "β" to "\\beta",
                                    "γ" to "\\gamma",
                                    "δ" to "\\delta",
                                    "ε" to "\\epsilon",
                                    "ζ" to "\\zeta",
                                    "η" to "\\eta",
                                    "θ" to "\\theta",
                                    "λ" to "\\lambda",
                                    "μ" to "\\mu",
                                    "ξ" to "\\xi",
                                    "π" to "\\pi",
                                    "ρ" to "\\rho",
                                    "σ" to "\\sigma",
                                    "φ" to "\\phi",
                                    "ψ" to "\\psi",
                                    "ω" to "\\omega",
                                    "Γ" to "\\Gamma",
                                    "Δ" to "\\Delta",
                                    "Θ" to "\\Theta",
                                    "Λ" to "\\Lambda",
                                    "Π" to "\\Pi",
                                    "Σ" to "\\Sigma",
                                    "Φ" to "\\Phi",
                                    "Ω" to "\\Omega"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(
                                            text = label, fontSize = 19.sp
                                        )
                                    }
                                }
                            }

                            /*
                             * CALCULUS
                             */
                            2 -> {

                                listOf(
                                    "∫" to "\\int ",
                                    "∬" to "\\iint ",
                                    "∭" to "\\iiint ",
                                    "∮" to "\\oint ",
                                    "Σ" to "\\sum ",
                                    "Π" to "\\prod ",
                                    "∂" to "\\partial ",
                                    "∇" to "\\nabla ",
                                    "lim" to "\\lim_{",
                                    "d/dx" to "\\frac{d}{dx}\\left("
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * RELATIONS
                             */
                            3 -> {

                                listOf(
                                    "=" to "=",
                                    "≠" to "\\neq",
                                    "≈" to "\\approx",
                                    "≡" to "\\equiv",
                                    "≤" to "\\leq",
                                    "≥" to "\\geq",
                                    "∼" to "\\sim",
                                    "∝" to "\\propto",
                                    "∈" to "\\in",
                                    "∉" to "\\notin",
                                    "⊂" to "\\subset",
                                    "⊆" to "\\subseteq",
                                    "⊃" to "\\supset",
                                    "⊇" to "\\supseteq"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * FUNCTIONS
                             */
                            4 -> {

                                listOf(
                                    "sin" to "\\sin ",
                                    "cos" to "\\cos ",
                                    "tan" to "\\tan ",
                                    "cot" to "\\cot ",
                                    "sec" to "\\sec ",
                                    "csc" to "\\csc ",
                                    "log" to "\\log ",
                                    "ln" to "\\ln ",
                                    "exp" to "\\exp ",
                                    "max" to "\\max ",
                                    "min" to "\\min ",
                                    "det" to "\\det "
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * SETS
                             */
                            5 -> {

                                listOf(
                                    "∅" to "\\emptyset",
                                    "∀" to "\\forall",
                                    "∃" to "\\exists",
                                    "¬" to "\\neg",
                                    "∧" to "\\land",
                                    "∨" to "\\lor",
                                    "∩" to "\\cap",
                                    "∪" to "\\cup",
                                    "⇒" to "\\Rightarrow",
                                    "⇔" to "\\Leftrightarrow",
                                    "⊥" to "\\perp"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * ARROWS
                             */
                            6 -> {

                                listOf(
                                    "→" to "\\rightarrow",
                                    "←" to "\\leftarrow",
                                    "↔" to "\\leftrightarrow",
                                    "⇒" to "\\Rightarrow",
                                    "⇐" to "\\Leftarrow",
                                    "⇔" to "\\Leftrightarrow",
                                    "↦" to "\\mapsto",
                                    "↑" to "\\uparrow",
                                    "↓" to "\\downarrow",
                                    "↗" to "\\nearrow",
                                    "↘" to "\\searrow",
                                    "↙" to "\\swarrow",
                                    "↖" to "\\nwarrow"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * MATRIX
                             */
                            7 -> {

                                listOf(

                                    "matrix" to """
                                        \begin{matrix}
                                        & \\
                                        &
                                        \end{matrix}
                                    """.trimIndent(),

                                    "( )" to """
                                        \begin{pmatrix}
                                        & \\
                                        &
                                        \end{pmatrix}
                                    """.trimIndent(),

                                    "[ ]" to """
                                        \begin{bmatrix}
                                        & \\
                                        &
                                        \end{bmatrix}
                                    """.trimIndent(),

                                    "| |" to """
                                        \begin{vmatrix}
                                        & \\
                                        &
                                        \end{vmatrix}
                                    """.trimIndent(),

                                    "cases" to """
                                        \begin{cases}
                                        & \\
                                        &
                                        \end{cases}
                                    """.trimIndent()

                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * Footer
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${text.length} characters",

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onPrimary,

                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(zoom * 100).toInt()}%",

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        sheetState.show()
    }
}

@Composable
fun UploadList(
    showFileRow: Boolean,
    draft: List<Draft>,
    removeFileFromDraft: (String) -> Unit,
    removeTextFromDraft: (Int) -> Unit,
    removeLaTeXFromDraft: (Int) -> Unit,
    openText: (Int) -> Unit,
    openLaTeX: (Int) -> Unit
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = (showFileRow && draft.isNotEmpty()),
        //enter = slideInVertically { if (showFileRow) 2 * it else it }, // + fadeIn() + scaleIn(initialScale = 0.8f),
        enter = slideInVertically {
            with(density) {
                64.dp.roundToPx()
            }
        },
        //exit = slideOutVertically { if (showFileRow) 2 * it else it } // + fadeOut() + scaleOut(targetScale = 0.8f)
        exit = ExitTransition.None
    ) {
        Row(
            modifier = Modifier
                //.fillMaxWidth()
                //.background(MaterialTheme.colorScheme.primary)
                .wrapContentWidth()
                .horizontalScroll(rememberScrollState())
                .requiredHeight(64.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            draft.forEach { draftItem ->
                when (draftItem) {
                    is Draft.File -> {
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
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    removeFileFromDraft(draftItem.name)
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    is Draft.Text -> {
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
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    removeTextFromDraft(draftItem.id)
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    is Draft.LaTeX -> {
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
                                    openLaTeX(draftItem.id)
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                painter = painterResource(R.drawable.function),
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
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    removeLaTeXFromDraft(draftItem.id)
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
}

// پریویو اگه بد اسکرول کنه کرش میکنه مثل اونیکی بشو لطفا
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenPopUp(
    id: String,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    unreadCount: Int,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    seenAll: (String) -> Unit,
    serverUrl: String,
    imageLoader: ImageLoader,
    isFileDownloaded: (String) -> Boolean,
    deleteMessage: (Int) -> Unit,
    playSet: (File?) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            getMessagesList(id)
        }
    }


    var animate by remember { mutableStateOf(false) }

    var messageMenu by remember { mutableStateOf(false) }
    var messageMenuId by remember { mutableIntStateOf(0) }
    var messageMenuOffset by remember {
        mutableStateOf(DpOffset(0.dp, 0.dp))
    }


    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    val firstUnreadIndex by rememberSaveable { mutableIntStateOf(messageList.indexOfFirst { !it.seen && !it.myMessage }) }
                    // بعدا با یه چیزی بذار که فقط یه بار پاس داده بشه
                    LaunchedEffect(messageList.size, unreadCount) {
                        if (messageList.isEmpty()) return@LaunchedEffect

                        val index =
                            (messageList.size - firstUnreadIndex).coerceIn(0, messageList.lastIndex)

                        val visible = listState.layoutInfo.visibleItemsInfo

                        val firstVisible = visible.firstOrNull()?.index ?: return@LaunchedEffect
                        val lastVisible = visible.lastOrNull()?.index ?: return@LaunchedEffect

                        when {
                            index < firstVisible -> listState.scrollToItem(index)
                            index > lastVisible -> listState.scrollToItem(index)
                        }
                    }
                    Box(
                        modifier = Modifier.pointerInput(Unit) {
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
                        val selectionState = rememberSelectionState()
                        BackHandler(enabled = selectionState.selectedTexts.isNotEmpty()) {
                            selectionState.clear()
                        }
                        SelectionContainer(state = selectionState) {
                            LazyColumn(
                                state = listState, contentPadding = PaddingValues(top = 8.dp)
                            ) {
                                items(
                                    items = messageList, key = { it.id }) { item ->

                                    if (item.id == messageList.getOrNull(firstUnreadIndex)?.id) {
                                        Text(
                                            text = "خوانده‌نشده",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
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
                                        playSet = playSet,
                                    )
                                }

                                item {
                                    Spacer(Modifier.height(0.dp))
                                }
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

                        val density = LocalDensity.current

                        this@Column.AnimatedVisibility(
                            visible = showButton,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .padding(bottom = 0.dp),
                            enter = slideInVertically {
                                with(density) { 64.dp.roundToPx() }
                            },
                            exit = slideOutVertically {
                                with(density) { 64.dp.roundToPx() }
                            }) {
                            Button(
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                                        seenAll(id)
                                    }
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .requiredSize(48.dp)
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
                                    contentDescription = null, //contentDescription = "Navigate to end",
                                    modifier = Modifier.requiredSize(24.dp)
                                )
                            }
                        }
                    }
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
                            start = menuX, top = menuY
                        ),
                    width = menuWidth,
                    height = menuHeight,
                    chord = menuChord,
                    isExpanded = messageMenu,
                    close = { messageMenu = false },
                    // میتونیم اینجا هم coreIn بزاریم که قشنگ‌تر بشه و همیشه از لبه شروع نکنه
                    offsetX = -(messageMenuOffset.x - menuX + 8.dp) + 24.dp,
                    offsetY = -(messageMenuOffset.y - menuY + 8.dp) + 24.dp,
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
                            text = { Text(stringResource(R.string.copy)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.content_copy),
                                    contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.forward)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.forward), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.save)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.bookmark), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pin)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.keep), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.translate)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.translate), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.check_box), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.share), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.report)) },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.report), contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                deleteMessage(messageMenuId)
                                messageMenu = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.delete), contentDescription = null
                                )
                            })

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }
        }
    }
}