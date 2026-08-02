package ir.gchat

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.SoundEffectConstants
import android.widget.EditText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import ir.gchat.SmsUtils.getContactName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SMSMainScreen(
    navHostController: NavHostController, smsViewModel: SmsChatViewModel
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium)) }
    var selectedChat by rememberSaveable { mutableStateOf("") }
    var selectedChatDisplayName by rememberSaveable { mutableStateOf("") }

    val view = LocalView.current

    val smsList by smsViewModel.chatList.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val colorSaver = Saver<MutableState<Color>, Int>(
        save = { it.value.toArgb() },
        restore = { mutableStateOf(Color(it)) }
    )

    var coverColor by rememberSaveable(
        saver = colorSaver
    ) {
        mutableStateOf(Color.Transparent)
    }

    var newSmsTarget by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(
                    if (expandedScreen) 0.3f else 1f
                )
                .zIndex(1f)
        ) {
            // Main Screen
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize(),
                //.shadow(
                //    elevation = 16.dp,
                //    clip = false
                //),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("SMS")
                        }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    navHostController.popBackStack()
                                }
                            ) {
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
                }) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(
                            items = smsList, key = { it.id }) { chat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    val id = chat.id
                                    selectedChat = id
                                    selectedChatDisplayName = chat.name
                                    if (!expandedScreen) {
                                        navHostController.navigate(
                                            "smsChatScreen?id=$id&displayName=${
                                                Uri.encode(
                                                    selectedChatDisplayName
                                                )
                                            }"
                                        )
                                    }
                                }) {

                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    Spacer(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 72.dp)
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                            )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        val backgroundColor = materialColors[hash20(chat.id)]
                                        val iconColor = if (backgroundColor.luminance() >= 0.5f) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        }

                                        Surface(
                                            modifier = Modifier.size(40.dp),
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

                                        Spacer(Modifier.width(16.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {

                                            Text(
                                                text = chat.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = chat.lastMessageText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(Modifier.width(16.dp))

                                        Column(
                                            horizontalAlignment = Alignment.End
                                        ) {

                                            Text(
                                                text = formatMessageTime(chat.lastMessageDate),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            if (chat.unreadMessages > 0) {

                                                Box(
                                                    modifier = Modifier
                                                        .defaultMinSize(minWidth = 20.dp)
                                                        .height(20.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(horizontal = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {

                                                    Text(
                                                        text = chat.unreadMessages.toString(),
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1
                                                    )
                                                }

                                            } else {
                                                //Icon(
                                                //    painter = painterResource(R.drawable.double_check),
                                                //    contentDescription = null,
                                                //    modifier = Modifier.size(20.dp),
                                                //    tint = MaterialTheme.colorScheme.primary
                                                //)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            expanded = true
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .padding(innerPadding)
                            .size(56.dp)
                            .align(Alignment.BottomEnd)
                            .shadow(
                                elevation = 6.dp, shape = CircleShape, clip = false
                            ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.edit),
                            contentDescription = "Send SMS",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                val width = maxWidth
                //val height = maxHeight

                AnimatedMenu(
                    modifier = Modifier.fillMaxSize(),
                    width = width,
                    height = 64.dp,
                    // 64^2=4096
                    chord = sqrt(width.value * width.value + 4096f).dp,
                    isExpanded = expanded,
                    close = { expanded = false },
                    ratioX = 1f,
                    offsetX = 28.dp,
                    ratioY = 1f,
                    offsetY = 28.dp,
                    position = Alignment.BottomCenter,
                    hasBackgroundCover = true,
                    whatIsMyBackgroundFilterColor = { color, _ ->
                        coverColor = color
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = newSmsTarget,
                            onValueChange = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                newSmsTarget = it
                            },
                            colors = colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent
                            ),
                            label = {
                                Text(text = "Send new SMS for...")
                            },
                            singleLine = true,
                            textStyle = TextStyle(textDirection = TextDirection.Content),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    keyboardController?.hide()
                                    val id = newSmsTarget
                                    selectedChat = id
                                    selectedChatDisplayName =
                                        getContactName(context, newSmsTarget) ?: newSmsTarget
                                    if (!expandedScreen) {
                                        navHostController.navigate(
                                            "smsChatScreen?id=$id&displayName=${
                                                Uri.encode(
                                                    selectedChatDisplayName
                                                )
                                            }"
                                        )
                                    }
                                }
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                val id = newSmsTarget
                                selectedChat = id
                                selectedChatDisplayName =
                                    getContactName(context, newSmsTarget) ?: newSmsTarget
                                if (!expandedScreen) {
                                    navHostController.navigate(
                                        "smsChatScreen?id=$id&displayName=${
                                            Uri.encode(
                                                selectedChatDisplayName
                                            )
                                        }"
                                    )
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = "Next",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // Expanded Screen
        if (expandedScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f), Color.Transparent
                                ), startX = 0.dp.toPx(), endX = 8.dp.toPx()
                            ), blendMode = BlendMode.Multiply
                        )
                    }) {
                SMSChatScreen(
                    back = { navHostController.popBackStack() },
                    id = selectedChat,
                    displayName = selectedChatDisplayName,
                    smsViewModel = smsViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSChatScreen(
    back: () -> Boolean,
    id: String,
    draft: String? = "",
    displayName: String,
    smsViewModel: SmsChatViewModel
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showButton by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }
    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            smsViewModel.loadMessages(id)
            smsViewModel.markMessageAsRead(context, id)
        }
    }
    LaunchedEffect(Unit) {
        scope.launch {
            val last = listState.layoutInfo.totalItemsCount - 1
            listState.animateScrollToItem(last)
        }
    }
    val messages = smsViewModel.messages.collectAsState().value
    //var renderValue by remember { mutableIntStateOf(4) }
    var message by rememberSaveable { mutableStateOf(draft.toString()) }
    val view = LocalView.current

    val backgroundColor = materialColors[hash20(id)]
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    var animate by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            //.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (id != "") {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    //.clip(HalfCutCircleShape())
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = false)
                                    ) {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        openContact(context, id)
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

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(items = messages, key = { it.id }) { item ->
                                Message(
                                    isMe = item.myMessage,
                                    message = item.text,
                                    seen = item.seen,
                                    timestamp = item.date
                                )

                            }
                        }
                        this@Column.AnimatedVisibility(
                            visible = showButton,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            enter = slideInVertically { it / 2 } + fadeIn(),// + scaleIn(initialScale = 0.8f),
                            exit = slideOutVertically { it / 2 } + fadeOut()// + scaleOut(targetScale = 0.8f)
                        ) {
                            Button(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        val last = listState.layoutInfo.totalItemsCount - 1

                                        listState.animateScrollToItem(last)
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
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_down),
                                    contentDescription = "Navigate to end",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp/*, max = 256.dp*/)
                            .background(MaterialTheme.colorScheme.primary)/*.imePadding()*/,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp, start = 8.dp, bottom = 10.dp)
                                .shadow(
                                    elevation = 4.dp, shape = RectangleShape, clip = false
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {

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
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                if (message.isNotBlank()) {
                                    message = message.replace(Regex("\\n+$"), "").trim()
                                    scope.launch {
                                        animate = true
                                        delay(1000.milliseconds)
                                        animate = false
                                    }
                                    //renderValue = (1..10).random()
                                    smsViewModel.sendMessage(context, id, message)
                                    message = ""
                                }
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
                    }
                }
            }
        }
    }
}