package ir.gchat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.SoundEffectConstants
import android.widget.EditText
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    back: () -> Boolean,
    id: String,
    sendMessage: (String, List<ContentEntity>) -> Unit,
    messageList: List<MessageItem>,
    seenMessage: (String, Int) -> Unit,
    getMessagesList: (String) -> Unit,
    displayName: String,
    unreadCount: Int,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    draft: List<File>,
    downloadFile: (Int, String) -> Unit,
    removeFileFromDraft: (String) -> Unit,
    clearDraft: () -> Unit,
    seenAll: (String) -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            getMessagesList(id)
        }
    }

    var isExpandedAttachment by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }

    val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })

    var coverColor by rememberSaveable(
        stateSaver = colorSaver
    ) { mutableStateOf(Color.Transparent) }

    var covered by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current

    val backgroundColor = materialColors[hash20(id)]
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

    var showFileRow by rememberSaveable(draft) { mutableStateOf(draft.isNotEmpty()) }

    val storagePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = if (isLandscape) {
                WindowInsets(
                    left = 0.dp,
                    right = 0.dp,
                    top = ScaffoldDefaults.contentWindowInsets
                        .getTop(LocalDensity.current).dp,
                    bottom = ScaffoldDefaults.contentWindowInsets
                        .getBottom(LocalDensity.current).dp
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
                        },
                        title = {
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
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
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
            }
        ) { innerPadding ->
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
                                    width = placeable.width,
                                    height = placeable.height + extra
                                ) {
                                    placeable.placeRelative(0, extra)
                                }
                            }
                    } else {
                        Modifier.weight(1f)
                    }

                    Box(modifier = modifier) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = if (showFileRow) 56.dp else 0.dp
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
                                    storagePermissionLauncher = storagePermissionLauncher,
                                    context = context,
                                    downloadFile = downloadFile
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
                            enter = slideInVertically { if (showFileRow) 2*it else it },// + fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = slideOutVertically { if (showFileRow) 2*it else it }// + fadeOut() + scaleOut(targetScale = 0.8f)
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
                    UploadList(showFileRow = showFileRow, draft = draft, removeFileFromDraft)
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
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                if (message.isNotBlank() || showFileRow) {
                                    message = message.replace(Regex("\\n+$"), "").trim()
                                    scope.launch {
                                        animate = true
                                        delay(1000.milliseconds)
                                        animate = false
                                    }
                                    val content = mutableListOf<ContentEntity>()
                                    draft.forEach { file ->
                                        content += ContentEntity(
                                            type = "file",
                                            id = file.id,
                                            fileName = file.name
                                        )
                                    }
                                    if (message.isNotBlank()) {
                                        content += ContentEntity(
                                            type = "text",
                                            text = message
                                        )
                                    }
                                    sendMessage(id, content)
                                    message = ""
                                    clearDraft()
                                } else {
                                    // شروع ضبط صوت
                                }
                            }, modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = if (message.isNotBlank() || showFileRow) painterResource(R.drawable.send) else painterResource(
                                    R.drawable.mic
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
                    ratioX = 0f,
                    ratioY = 1f,
                    position = Alignment.BottomStart,
                    hasBackgroundCover = true,
                    whatIsMyBackgroundFilterColor = { color, show ->
                        covered = show
                        coverColor = color
                    }) {
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
                                isExpandedAttachment = false
                            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.insert_text), contentDescription = null
                                )
                            }, trailingIcon = { }, enabled = true
                        )
                    }
                }
            }
        }

        if (covered) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        64.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                    .background(coverColor)
                    .align(Alignment.TopCenter)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isExpandedAttachment = false
                    }
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                    .background(coverColor)
                    .align(Alignment.BottomCenter)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isExpandedAttachment = false
                    }
            )
        }
    }
}

@Composable
fun UploadList(
    showFileRow: Boolean, draft: List<File>,
    removeFileFromDraft: (String) -> Unit
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
            draft.forEach { file ->
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
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(32.dp),
                        progress = { file.progress }
                    )
                    Text(
                        text = file.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            removeFileFromDraft(file.name)
                        }
                    ) {
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

@Composable
fun Message(
    isMe: Boolean,
    content: List<ContentEntity>,
    seen: Boolean,
    timestamp: String,
    storagePermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    context: Context,
    downloadFile: (Int, String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
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
                                when (contentEntity.type) {
                                    "text" -> {
                                        Text(
                                            text = contentEntity.text.toRichAnnotatedString(
                                                linkColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .padding(
                                                    end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                                                    bottom = if (lineCount > 1) 24.dp else 0.dp
                                                ),
                                            onTextLayout = {
                                                if (lineCount == 0) {
                                                    lineCount = it.lineCount
                                                }
                                            }
                                        )
                                    }

                                    "file" -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    if (isMe)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (hasStoragePermission(context)) {
                                                        downloadFile(
                                                            contentEntity.id,
                                                            contentEntity.fileName
                                                        )
                                                    } else {
                                                        storagePermissionLauncher.launch(
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        )
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (hasStoragePermission(context)) {
                                                        downloadFile(
                                                            contentEntity.id,
                                                            contentEntity.fileName
                                                        )
                                                    } else {
                                                        storagePermissionLauncher.launch(
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        )
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.photo),
                                                    contentDescription = null,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Text(
                                                text = contentEntity.fileName,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                when (contentEntity.type) {
                                    "text" -> {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = contentEntity.text.toRichAnnotatedString(
                                                linkColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }

                                    "file" -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (hasStoragePermission(context)) {
                                                        downloadFile(
                                                            contentEntity.id,
                                                            contentEntity.fileName
                                                        )
                                                    } else {
                                                        storagePermissionLauncher.launch(
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        )
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (hasStoragePermission(context)) {
                                                        downloadFile(
                                                            contentEntity.id,
                                                            contentEntity.fileName
                                                        )
                                                    } else {
                                                        storagePermissionLauncher.launch(
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        )
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.photo),
                                                    contentDescription = null,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Text(
                                                text = contentEntity.fileName,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
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