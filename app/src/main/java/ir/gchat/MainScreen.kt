package ir.gchat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MainScreenContainer(
    chatList: List<Contact>,
    navHostController: NavHostController,
    searchContactList: List<Contact>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit,
    logout: () -> Unit,
    sendMessage: (String, List<ContentEntity>) -> Unit,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    getConversations: () -> Unit,
    seenMessage: (String, Int) -> Unit,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    draft: List<File>,
    downloadFile: (Int, String) -> Unit,
    removeFileFromDraft: (String) -> Unit,
    clearDraft: () -> Unit,
    seenAll: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val drawerRatio = when (windowSizeClass.widthSizeClass) {
        Compact -> 0.8f
        Medium -> 0.5f
        else -> 0.3f
    }

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium)) }
    var selectedChat by rememberSaveable { mutableStateOf("") }
    var selectedChatDisplayName by rememberSaveable { mutableStateOf("") }
    var selectedChatUnreadCount by rememberSaveable { mutableIntStateOf(0) }

    val view = LocalView.current

    val backgroundColor = materialColors[hash20(username)]
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    LaunchedEffect(Unit) {
        getConversations()
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.apply {
                close()
            }
        }
    }

    val context = LocalContext.current
    var isSmsApp by remember { mutableStateOf(checkSmsAppRole(context)) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isSmsApp = checkSmsAppRole(context)
        }
    }

    val navController = rememberNavController()

    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var smsAlertHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(drawerRatio)
                    .shadow(elevation = 16.dp, clip = false)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .background(backgroundColor)
                        .drawWithContent {
                            drawContent()

                            val gradientHeight = size.height * 0.15f

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent, Color.Black.copy(alpha = 0.1f)
                                    ), startY = size.height - gradientHeight, endY = size.height
                                ), blendMode = BlendMode.Multiply
                            )
                        }
                ) {

                    Icon(
                        painter = painterResource(R.drawable.profile_black_content),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        tint = iconColor.copy(alpha = 0.5f)
                    )
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ) {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                //expanded = !expanded
                            }
                            .padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = username,
                            color = iconColor,
                        )
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                // بجاش بریم صفحه پروفایل
                                //expanded = !expanded
                            }) {
                            Icon(
                                //painter = painterResource(id = R.drawable.arrow_drop_down),
                                painter = painterResource(id = R.drawable.account_circle),
                                contentDescription = null,
                                //modifier = Modifier.rotate((expansionHeight.value.value / 160) * 180f),
                                tint = iconColor
                            )
                        }
                    }
                }

                //Column(
                //    Modifier
                //        .fillMaxWidth()
                //        .height(expansionHeight.value)
                //        .background(MaterialTheme.colorScheme.surfaceContainer)
                //        .drawWithContent {
                //            drawContent()
                //            val gradientHeight = 8.dp.value //size.height * 0.15f
                //            drawRect(
                //                brush = Brush.verticalGradient(
                //                    colors = listOf(
                //                        Color.Transparent, Color.Black.copy(alpha = 0.1f)
                //                    ), startY = size.height - gradientHeight, endY = size.height
                //                ), blendMode = BlendMode.Multiply
                //            )
                //        }
                //        .verticalScroll(rememberScrollState())) {
                //    Spacer(modifier = Modifier.height(8.dp))
                //    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                //        Icon(
                //            painter = painterResource(id = R.drawable.support),
                //            contentDescription = "Support"
                //        )
                //    }, onClick = {
                //        view.playSoundEffect(SoundEffectConstants.CLICK)
                //    })
                //    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                //        Icon(
                //            painter = painterResource(id = R.drawable.support),
                //            contentDescription = "Support"
                //        )
                //    }, onClick = {
                //        view.playSoundEffect(SoundEffectConstants.CLICK)
                //    })
                //    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                //        Icon(
                //            painter = painterResource(id = R.drawable.support),
                //            contentDescription = "Support"
                //        )
                //    }, onClick = {
                //        view.playSoundEffect(SoundEffectConstants.CLICK)
                //    })
                //    Spacer(modifier = Modifier.height(8.dp))
                //}

                Column(
                    Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownMenuItem(text = { Text("Logout") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.door_open),
                            contentDescription = null
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        logout()
                    })
                    DropdownMenuItem(text = { Text("Setting") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = null
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        navHostController.navigate("settings")
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            var searching by rememberSaveable { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(
                        if (expandedScreen) 0.3f else 1f
                    )
                    .zIndex(1f)
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = if (isLandscape) {
                        val density = LocalDensity.current

                        WindowInsets(
                            left = 0.dp,
                            right = 0.dp,
                            top = with(density) {
                                ScaffoldDefaults.contentWindowInsets.getTop(this).toDp()
                            },
                            bottom = with(density) {
                                ScaffoldDefaults.contentWindowInsets.getBottom(this).toDp()
                            }
                        )
                    } else {
                        ScaffoldDefaults.contentWindowInsets
                    },
                    topBar = {
                        TopAppBar(
                            windowInsets = if (isLandscape) {
                                WindowInsets.statusBars
                            } else {
                                TopAppBarDefaults.windowInsets
                            },
                            title = {
                                Text("GChat")
                            }, navigationIcon = {
                                IconButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }) {
                                    Icon(
                                        painter = painterResource(R.drawable.menu),
                                        contentDescription = "Menu"
                                    )
                                }
                            }, actions = {
                                IconButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        searching = true
                                    }) {
                                    Icon(
                                        painter = painterResource(R.drawable.search),
                                        contentDescription = "Search"
                                    )
                                }
                            }, colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                subtitleContentColor = MaterialTheme.colorScheme.onPrimary
                            ), modifier = Modifier.shadow(
                                elevation = 4.dp, clip = false
                            )
                        )
                    }) { innerPadding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                bottom = smsAlertHeight + 8.dp
                            )
                        ) {
                            items(
                                items = chatList, key = { it.id }) { contact ->
                                ContactItem(
                                    contact = contact, onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        val id = contact.id
                                        selectedChat = id
                                        selectedChatDisplayName = contact.name
                                        selectedChatUnreadCount = contact.unreadMessages
                                        if (!expandedScreen) {
                                            navHostController.navigate(
                                                //&type=$selectedType
                                                "chatScreen?id=$id&displayName=${
                                                    Uri.encode(
                                                        selectedChatDisplayName
                                                    )
                                                }&selectedChatUnreadCount=${selectedChatUnreadCount}"
                                            )
                                        } else {
                                            navController.navigate(
                                                //&type=$selectedType
                                                "chatScreen?id=$id&displayName=${
                                                    Uri.encode(
                                                        selectedChatDisplayName
                                                    )
                                                }&selectedChatUnreadCount=${selectedChatUnreadCount}"
                                            )
                                        }
                                    })
                            }
                        }

                        if (isSmsApp) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .onSizeChanged {
                                        smsAlertHeight = with(density) {
                                            it.height.toDp()
                                        }
                                    }
                            ) {
                                val compact = maxWidth < 512.dp

                                val containerModifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 4.dp, clip = false)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .padding(16.dp)

                                if (compact) {
                                    Column(modifier = containerModifier, horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Restore your default messaging app to receive SMS messages.",
                                            color = MaterialTheme.colorScheme.onError
                                        )

                                        TextButton(
                                            modifier = Modifier.align(Alignment.End),
                                            shape = RoundedCornerShape(2.dp),
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)

                                                try {
                                                    context.startActivity(
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                                            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                                        else
                                                            Intent(Settings.ACTION_SETTINGS)
                                                    )
                                                } catch (_: Exception) {
                                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        ) {
                                            Text("Open Settings")
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = containerModifier,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Restore your default messaging app to receive SMS messages.",
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            color = MaterialTheme.colorScheme.onError
                                        )

                                        TextButton(
                                            shape = RoundedCornerShape(2.dp),
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)

                                                try {
                                                    context.startActivity(
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                                            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                                        else
                                                            Intent(Settings.ACTION_SETTINGS)
                                                    )
                                                } catch (_: Exception) {
                                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        ) {
                                            Text("Open Settings")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //search
                val heightFraction by animateFloatAsState(
                    targetValue = if (searching) 1f else 0f, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                )

                var searchContent by remember { mutableStateOf("") }
                val keyboardController = LocalSoftwareKeyboardController.current

                BackHandler(enabled = searching) {
                    searching = false
                }

                LaunchedEffect(searchContent) {
                    if (searchContent.isBlank()) {
                        clearSearchList()
                    } else {
                        searchContact(searchContent)
                    }
                }

                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .statusBarsPadding()
                        .imePadding()
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .heightIn(min = 64.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .alpha(heightFraction),
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(2.dp),
                        shadowElevation = 4.dp,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            searching = !searching
                        }) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    clearSearchList()
                                    searchContent = ""
                                    searching = false
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    contentDescription = "Menu"
                                )
                            }
                            TextField(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                value = searchContent,
                                onValueChange = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    searchContent = it
                                },
                                colors = colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,

                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                ),
                                label = {
                                    Text(text = "Search here...")
                                },
                                singleLine = true,
                                textStyle = TextStyle(textDirection = TextDirection.Content),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                    })
                            )
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    clearSearchList()
                                    searchContent = ""
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                            .fillMaxSize()
                            .shadow(
                                elevation = 4.dp,
                                clip = false
                            )
                            .background(
                                //color = MaterialTheme.colorScheme.surfaceContainer,
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(2.dp)
                            )
                    ) {
                        items(items = searchContactList) { item ->
                            ContactItem(
                                contact = item, onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    val id = item.id
                                    selectedChat = id
                                    selectedChatDisplayName = id
                                    getMessagesList(selectedChat)
                                    if (!expandedScreen) {
                                        //selectedChatUnreadCount این رو باید درست پاس بدی این یه باگ نیست در آیده هندل میشه
                                        navHostController.navigate("chatScreen?id=$id&displayName=$id&selectedChatUnreadCount=$selectedChatUnreadCount")
                                    } else {
                                        navController.navigate(
                                            "chatScreen?id=$id&displayName=${
                                                Uri.encode(
                                                    selectedChatDisplayName
                                                )
                                            }&selectedChatUnreadCount=${selectedChatUnreadCount}"
                                        )
                                    }
                                })
                        }
                    }
                }
            }

            // Expanded Screen
            if (expandedScreen) {
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = "chatScreen",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> -(fullHeight * 0.1f).toInt() },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    popEnterTransition = {
                        slideInVertically(
                            initialOffsetY = { fullHeight -> -(fullHeight * 0.1f).toInt() },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    }) {
                    composable(
                        route = "chatScreen?id={id}&displayName={displayName}&selectedChatUnreadCount={selectedChatUnreadCount}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("displayName") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("selectedChatUnreadCount") {
                                type = NavType.IntType
                                defaultValue = 0
                            }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val displayName =
                            backStackEntry.arguments?.getString("displayName") ?: ""
                        val selectedChatUnreadCount =
                            backStackEntry.arguments?.getInt("selectedChatUnreadCount") ?: 0
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(0f)
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.1f),
                                                Color.Transparent
                                            ), startX = 0.dp.toPx(), endX = 8.dp.toPx()
                                        ), blendMode = BlendMode.Multiply
                                    )
                                }
                        ) {
                            ChatScreen(
                                back = { navController.popBackStack() },
                                id = id/*selectedChat*/,
                                sendMessage = sendMessage,
                                messageList = messageList,
                                seenMessage = seenMessage,
                                getMessagesList = getMessagesList,
                                displayName = displayName/*selectedChatDisplayName*/,
                                unreadCount = selectedChatUnreadCount,
                                shouldScrollToBottom = shouldScrollToBottom,
                                onScrolledToBottom = onScrolledToBottom,
                                getUploadUri = getUploadUri,
                                draft = draft,
                                downloadFile = downloadFile,
                                removeFileFromDraft = removeFileFromDraft,
                                clearDraft = clearDraft,
                                seenAll = seenAll
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    contact: Contact, onClick: () -> Unit
) {
    val backgroundColor = remember(contact.id) {
        materialColors[hash20(contact.id)]
    }

    val iconColor = if (backgroundColor.luminance() >= 0.5f) Color.Black
    else Color.White

    val lastMessageText = remember(contact.lastMessageContent) {
        buildString {
            for ((index, content) in contact.lastMessageContent.withIndex()) {
                when (content) {
                    is Content.Text -> {
                        append(content.text)
                    }

                    is Content.File -> {
                        if (isNotEmpty()) append(" ")
                        append("📁 ")
                        append(content.fileName)
                    }
                }

                if (index < contact.lastMessageContent.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    Log.d("lastMessageText", lastMessageText)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
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
                Box(
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(backgroundColor), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.profile_black_content
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = iconColor.copy(alpha = .5f)
                        )
                    }
                    if (contact.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                                .size(9.dp)
                                .background(
                                    Color(0xFF23A55A), CircleShape
                                )
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contact.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = formatMessageTime(contact.lastMessageDate),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        text = lastMessageText.toRichAnnotatedString(
                                            linkColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Text(
                                text = lastMessageText
                                    .replace("\n", " ")
                                    .toRichAnnotatedString(
                                        linkColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (contact.unreadMessages > 0) {

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .defaultMinSize(minWidth = 20.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary, RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.unreadMessages.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}