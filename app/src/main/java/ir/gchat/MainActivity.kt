package ir.gchat

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.SoundEffectConstants
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    val socketViewModel: SocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val mainReady = viewModel.ready.collectAsState().value
            val serverReady = socketViewModel.ready.collectAsState().value

            if (mainReady && serverReady) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // theme
                    val theme by viewModel.theme.collectAsState()
                    val palette by viewModel.palette.collectAsState()
                    val darkTheme = when (theme) {
                        0 -> isSystemInDarkTheme()
                        1 -> true
                        2 -> false
                        else -> isSystemInDarkTheme()
                    }
                    // login status
                    val loggedIn by socketViewModel.loggedIn.collectAsState()
                    val oldLoggedIn by socketViewModel.oldLoggedIn.collectAsState()
                    // chat list
                    val chatList by socketViewModel.chatList.collectAsState()
                    val contactsSearchList by socketViewModel.contactSearchList.collectAsState()
                    GChatTheme(
                        dynamicColor = false, darkTheme = darkTheme, paletteIndex = palette
                    ) {
                        MainNavigation(
                            setTheme = { viewModel.setTheme() },
                            theme = theme,
                            signIn = { username, password ->
                                socketViewModel.signIn(
                                    username = username, password = password
                                )
                            },
                            signUp = { iccid, username, password ->
                                socketViewModel.signUp(
                                    iccid = iccid, username = username, password = password
                                )
                            },
                            loggedIn = loggedIn,
                            oldLoggedIn = oldLoggedIn,
                            chatList = chatList,
                            setServerIP = { serverIP -> socketViewModel.setServerIP(serverIP = serverIP) },
                            serverIP = socketViewModel.serverIP.collectAsState().value,
                            setDevice = { device -> viewModel.setDevice(deviceType = device) },
                            device = { viewModel.getDevice() },
                            getRules = { viewModel.getRules() },
                            searchContactList = contactsSearchList,
                            searchContact = { username -> socketViewModel.searchUsername(username = username) },
                            clearSearchList = { socketViewModel.clearSearchMemory() },
                            logout = { socketViewModel.logout() },
                            sendMessage = { contact, message ->
                                socketViewModel.sendMessage(
                                    contact = contact, message = message
                                )
                            },
                            messageList = socketViewModel.messageList.collectAsState().value,
                            getMessagesList = { contact -> socketViewModel.getMessagesList(contact) },
                            getConversations = { socketViewModel.getConversations() },
                            seenMessage = { contact, id ->
                                socketViewModel.seenMessage(
                                    contact, id
                                )
                            },
                            username = socketViewModel.usernameState.collectAsState().value,
                            shouldScrollToBottom = socketViewModel.shouldScrollToBottom.collectAsState().value,
                            onScrolledToBottom = { socketViewModel.onScrolledToBottom() },
                            setColor = { theme -> viewModel.setColor(theme) },
                            paletteIndex = palette,
                            getUploadUri = { name, size, hash, uri ->
                                socketViewModel.getUploadUri(
                                    name,
                                    size,
                                    hash,
                                    uri
                                )
                            },
                            draft = socketViewModel.drafts.collectAsState().value,
                            savedText = socketViewModel.savedText.collectAsState().value,
                            setSavedText = { id, message -> socketViewModel.setSavedText(id, message) },
                            downloadFile = { fileId, fileName, fileSize, setProgress, setPending, setDownloadedBytes ->
                                socketViewModel.downloadFile(
                                    fileId,
                                    fileName,
                                    fileSize,
                                    setProgress,
                                    setPending,
                                    setDownloadedBytes
                                )
                            },
                            loginResponse = socketViewModel.loginResponse.collectAsState().value,
                            removeFileFromDraft = { fileName ->
                                socketViewModel.removeFileFromDraft(
                                    fileName
                                )
                            },
                            clearDraft = { socketViewModel.clearDraft() },
                            setNavBarTheme = { mode -> viewModel.setNavBarTheme(mode) },
                            seenAll = { id -> socketViewModel.seenAll(id) },
                            token = socketViewModel.token.collectAsState().value,
                            isFileDownloaded = { fileName -> socketViewModel.isFileDownloaded(fileName) }
                        )
                        SetUpSystemBars(
                            palette = palette,
                            lightNavBar = viewModel.lightNavBar.collectAsState().value
                        )
                    }
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                }
            }
        }

        socketViewModel.connect()
    }
}

@Composable
fun SetUpSystemBars(palette: Int, lightNavBar: Boolean?) {
    val colorLuminance = remember(palette) {
        materialColors[palette].luminance()
    }
    val darkIcons = remember(colorLuminance) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            colorLuminance >= 0.7f
        } else {
            colorLuminance >= 0.5f
        }
    }
    val navigationBarColor = remember {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) Color.Transparent else Color.Black
    }
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context as Activity
    val window = activity.window
    val lifecycleOwner = LocalLifecycleOwner.current

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    fun apply() {
        // Task View
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            activity.setTaskDescription(
                ActivityManager.TaskDescription.Builder().setPrimaryColor(primaryColor).build()
            )
        } else {
            @Suppress("DEPRECATION") activity.setTaskDescription(
                ActivityManager.TaskDescription(
                    null, null, primaryColor
                )
            )
        }

        // Controller
        val controller = WindowCompat.getInsetsController(window, view)

        // Nav Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false

            controller.isAppearanceLightNavigationBars =
                lightNavBar ?: !darkIcons
        }

        // Status Bar
        controller.isAppearanceLightStatusBars = darkIcons

        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // 20%
            window.statusBarColor = Color(0x33000000).toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
        }

    }

    SideEffect {
        apply()
    }

    DisposableEffect(lifecycleOwner, darkIcons, lightNavBar) {

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                apply()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener {
            apply()
        }

        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    setTheme: () -> Unit,
    theme: Int,
    signIn: (String, String) -> Unit,
    signUp: (String, String, String) -> Unit,
    loggedIn: Boolean,
    oldLoggedIn: Boolean,
    chatList: List<Contact>,
    setServerIP: (String) -> Unit,
    serverIP: String,
    setDevice: (Int) -> Unit,
    device: suspend () -> Int,
    getRules: () -> String,
    searchContactList: List<Contact>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit,
    logout: () -> Unit,
    sendMessage: (String, List<Content>) -> Unit,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    getConversations: () -> Unit,
    seenMessage: (String, Int) -> Unit,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    setColor: (Int) -> Unit,
    paletteIndex: Int,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    draft: Map<String, List<Draft>>,
    savedText: Map<String, String>,
    setSavedText: (String, String) -> Unit,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    loginResponse: Boolean,
    removeFileFromDraft: (String) -> Unit,
    clearDraft: () -> Unit,
    setNavBarTheme: (Boolean?) -> Unit,
    seenAll: (String) -> Unit,
    token: String,
    isFileDownloaded: (String) -> Boolean
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val primaryLuminance = MaterialTheme.colorScheme.primary.luminance()
    LaunchedEffect(currentRoute, primaryLuminance) {
        if (currentRoute.toString().startsWith("chatScreen")) {
            setNavBarTheme(primaryLuminance > 0.5f)
        } else {
            setNavBarTheme(null)
        }
    }

    LaunchedEffect(loggedIn, oldLoggedIn) {
        if (oldLoggedIn) {
            if (loggedIn) {
                if (currentRoute != "mainScreen" && !currentRoute.toString().startsWith("chatScreen")) {
                    navController.navigate("mainScreen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                navController.navigate("wait") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            navController.navigate("greeting") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val context = LocalContext.current

    val imageLoader = remember(context, token) {
        ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()

                        val request = original.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()

                        chain.proceed(request)
                    }
                    .build()
            }
            .build()
    }

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = "wait",
        enterTransition = {
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(
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
                targetOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(
                    durationMillis = 320, easing = FastOutSlowInEasing
                )
            )
        }) {
        composable(route = "greeting") {
            Box(modifier = Modifier.fillMaxSize()) {
                Greeting(
                    setTheme = setTheme,
                    theme = theme,
                    signIn = { username, password ->
                        signIn(
                            username, password
                        )
                    },
                    signUp = { iccid, username, password ->
                        signUp(
                            iccid, username, password
                        )
                    },
                    ipConfig = { navController.navigate("ipConfig") },
                    getRules = getRules,
                    loginResponse = loginResponse
                )
            }
        }
        composable(route = "wait") {
            val view = LocalView.current
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Connecting...")
                        }, colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ), modifier = Modifier.shadow(
                            elevation = 4.dp, shape = RectangleShape, clip = false
                        ), actions = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    navController.navigate("ipConfig")
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.tune),
                                    contentDescription = "IP config"
                                )
                            }
                        })
                }) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val progressColor = MaterialTheme.colorScheme.primary.toArgb()

                        AndroidView(
                            modifier = Modifier.size(48.dp), factory = { context ->
                                ProgressBar(context).apply {
                                    isIndeterminate = true
                                    indeterminateTintList = ColorStateList.valueOf(progressColor)
                                }
                            })
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "After connecting, you will be taken to the home page.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        composable(route = "mainScreen") {
            MainScreenContainer(
                chatList = chatList,
                navHostController = navController,
                searchContactList = searchContactList,
                searchContact = searchContact,
                clearSearchList = clearSearchList,
                logout = logout,
                sendMessage = sendMessage,
                messageList = messageList,
                getMessagesList = getMessagesList,
                getConversations = getConversations,
                seenMessage = seenMessage,
                username = username,
                shouldScrollToBottom = shouldScrollToBottom,
                onScrolledToBottom = onScrolledToBottom,
                getUploadUri = getUploadUri,
                draft = draft,
                savedText = savedText,
                setSavedText = setSavedText,
                downloadFile = downloadFile,
                removeFileFromDraft = removeFileFromDraft,
                clearDraft = clearDraft,
                seenAll = seenAll,
                serverUrl = serverIP,
                imageLoader = imageLoader,
                isFileDownloaded = isFileDownloaded
            )
        }
        composable(route = "appearanceSettings") {
            AppearanceSettingsScreen(
                setTheme = setTheme,
                theme = theme,
                navHostController = navController,
                setColor = setColor,
                paletteIndex = paletteIndex
            )
        }
        composable(route = "settings") {
            SettingsScreen(navHostController = navController)
        }
        composable(
            //route = "chatScreen?id={id}&type={type}&displayName={displayName}",
            // چک شود
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
            val displayName = backStackEntry.arguments?.getString("displayName") ?: ""
            val selectedChatUnreadCount =
                backStackEntry.arguments?.getInt("selectedChatUnreadCount") ?: 0
            ChatScreen(
                back = { navController.popBackStack() },
                id = id,
                //type = type,
                sendMessage = sendMessage,
                messageList = messageList,
                seenMessage = seenMessage,
                getMessagesList = getMessagesList,
                displayName = displayName,
                unreadCount = selectedChatUnreadCount,
                shouldScrollToBottom = shouldScrollToBottom,
                onScrolledToBottom = onScrolledToBottom,
                getUploadUri = getUploadUri,
                draft = draft,
                savedText = savedText,
                setSavedText = setSavedText,
                downloadFile = downloadFile,
                removeFileFromDraft = removeFileFromDraft,
                clearDraft = clearDraft,
                seenAll = seenAll,
                serverUrl = serverIP,
                imageLoader = imageLoader,
                isFileDownloaded = isFileDownloaded
            )
        }
        //composable(route = "smsMainScreen") {
        //    SMSMainScreen(
        //        navHostController = navController, smsViewModel = smsViewModel
        //    )
        //}
        //composable(
        //    route = "smsChatScreen?id={id}&displayName={displayName}",
        //    arguments = listOf(navArgument("id") {
        //        type = NavType.StringType
        //    }, navArgument("displayName") {
        //        type = NavType.StringType
        //    })
        //) { backStackEntry ->
        //    val id = backStackEntry.arguments?.getString("id") ?: ""
        //    val displayName = backStackEntry.arguments?.getString("displayName") ?: ""
        //    SMSChatScreen(
        //        back = { navController.popBackStack() },
        //        id = id,
        //        displayName = displayName,
        //        smsViewModel = smsViewModel
        //    )
        //}
        composable(route = "ipConfig") {
            IpConfig(
                serverIP = serverIP,
                device = device,
                back = { navController.popBackStack() },
                setDevice = setDevice,
                setServerIP = setServerIP
            )
        }
    }
}

@Composable
fun AnimatedMenu(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    chord: Dp,
    isExpanded: Boolean,
    close: () -> Unit,
    ratioX: Float,
    offsetX: Dp = 0.dp,
    ratioY: Float,
    offsetY: Dp = 0.dp,
    position: Alignment,
    hasBackgroundCover: Boolean,
    tabletView: Boolean = false,
    whatIsMyBackgroundFilterColor: (Color, Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val sizeBtn by animateDpAsState(
        targetValue = if (isExpanded) chord * 2 else 48.dp, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        ), label = "circle_size"
    )

    val surface = MenuDefaults.containerColor

    val expandProgress = remember(sizeBtn, chord) {
        if (chord == 24.dp) {
            1f
        } else {
            ((sizeBtn - 48.dp).value / (chord.value * 2 - 48f)).coerceIn(0f, 1f)
        }
    }

    val showContent = expandProgress > 0.05f

    val backgroundFilter = remember(hasBackgroundCover, expandProgress) {
        if (hasBackgroundCover) {
            Color.Black.copy(alpha = expandProgress * 0.25f)
        } else {
            Color.Transparent
        }
    }

    LaunchedEffect(backgroundFilter, showContent) {
        whatIsMyBackgroundFilterColor(
            backgroundFilter, showContent
        )
    }

    BackHandler(enabled = isExpanded) {
        close()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(modifier = modifier.fillMaxSize()) {

        if (showContent) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null, interactionSource = interactionSource
                    ) {
                        close()
                    }
                    .background(backgroundFilter))
        }

        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(width, height)
                .shadow(
                    elevation = if (expandProgress > 0.99f && isExpanded) {
                        8.dp
                    } else {
                        0.dp
                    }, shape = RoundedCornerShape(2.dp), clip = false
                )
                .clip(RoundedCornerShape(2.dp))
                .align(position)
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .then(
                        if (sizeBtn != 48.dp) {
                            Modifier.clickable(
                                indication = null, interactionSource = interactionSource
                            ) {}
                        } else Modifier)) {

                val radius = sizeBtn.toPx() / 2

                val center = when (position) {

                    Alignment.BottomStart -> {
                        Offset(
                            x = size.width * ratioX + 24.dp.toPx() - offsetX.toPx(),

                            y = size.height * ratioY - 24.dp.toPx() - offsetY.toPx()
                        )
                    }

                    Alignment.TopEnd -> {
                        Offset(
                            x = size.width * ratioX - if (tabletView) 24.dp.toPx()
                            else 12.dp.toPx() - offsetX.toPx(),

                            y = size.height * ratioY + if (tabletView) 24.dp.toPx()
                            else 12.dp.toPx() - offsetY.toPx()
                        )
                    }

                    else -> {
                        Offset(
                            x = size.width * ratioX - offsetX.toPx(),
                            y = size.height * ratioY - offsetY.toPx()
                        )
                    }
                }


                drawCircle(
                    color = if (sizeBtn != 48.dp) {
                        surface
                    } else {
                        Color.Transparent
                    }, radius = radius, center = center
                )
            }


            if (showContent) {
                Box(
                    modifier = Modifier.alpha(expandProgress)
                ) {
                    content()
                }
            }
        }
    }
}

fun String.toRichAnnotatedString(
    linkColor: Color
): AnnotatedString {

    val source = this

    val spannable = SpannableString(source)

    Linkify.addLinks(
        spannable, Linkify.WEB_URLS
    )

    val urls = spannable.getSpans(
        0, spannable.length, URLSpan::class.java
    )

    fun AnnotatedString.Builder.parseRange(
        start: Int, end: Int
    ) {
        var i = start

        while (i < end) {

            // URL
            val urlSpan = urls.firstOrNull {
                spannable.getSpanStart(it) == i
            }

            if (urlSpan != null) {
                val urlEnd = spannable.getSpanEnd(urlSpan)

                withLink(
                    LinkAnnotation.Url(
                        url = urlSpan.url, styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor, textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(source.substring(i, urlEnd))
                }

                i = urlEnd
                continue
            }


            // **bold**
            if (source.startsWith("**", i)) {

                val close = source.indexOf(
                    "**", i + 2
                )

                if (close > i + 2) {

                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        parseRange(
                            i + 2, close
                        )
                    }

                    i = close + 2
                    continue
                }
            }


            // __italic__
            if (source.startsWith("__", i)) {

                val close = source.indexOf(
                    "__", i + 2
                )

                if (close > i + 2) {

                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        parseRange(
                            i + 2, close
                        )
                    }

                    i = close + 2
                    continue
                }
            }


            // ~~strike~~
            if (source.startsWith("~~", i)) {

                val close = source.indexOf(
                    "~~", i + 2
                )

                if (close > i + 2) {

                    withStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    ) {
                        parseRange(
                            i + 2, close
                        )
                    }

                    i = close + 2
                    continue
                }
            }


            append(source[i])
            i++
        }
    }


    return buildAnnotatedString {
        withStyle(
            ParagraphStyle(
                textDirection = TextDirection.Content
            )
        ) {
            parseRange(0, source.length)
        }
    }
}

//fun String.toRichAnnotatedString(
//    linkColor: Color
//): AnnotatedString {
//
//    val bidi = BidiFormatter.getInstance()
//
//    val spannable = SpannableString(
//        lines().joinToString("\n") { bidi.unicodeWrap(it) }
//    )
//
//    Linkify.addLinks(spannable, Linkify.WEB_URLS)
//
//    val urlSpans = spannable.getSpans(
//        0,
//        spannable.length,
//        URLSpan::class.java
//    )
//
//    return buildAnnotatedString {
//
//        val text = spannable.toString()
//        var index = 0
//
//        while (index < text.length) {
//
//            // لینک
//            val urlSpan = urlSpans.firstOrNull {
//                spannable.getSpanStart(it) == index
//            }
//
//            if (urlSpan != null) {
//                val end = spannable.getSpanEnd(urlSpan)
//
//                withLink(
//                    LinkAnnotation.Url(
//                        url = urlSpan.url,
//                        styles = TextLinkStyles(
//                            style = SpanStyle(
//                                color = linkColor,
//                                textDecoration = TextDecoration.Underline
//                            )
//                        )
//                    )
//                ) {
//                    append(text.substring(index, end))
//                }
//
//                index = end
//                continue
//            }
//
//
//            // بولد *text*
//            if (text[index] == '*') {
//                val end = text.indexOf('*', index + 1)
//
//                if (end > index + 1) {
//                    withStyle(
//                        SpanStyle(
//                            fontWeight = FontWeight.Bold
//                        )
//                    ) {
//                        append(text.substring(index + 1, end))
//                    }
//
//                    index = end + 1
//                    continue
//                }
//            }
//
//
//            // ایتالیک _text_
//            if (text[index] == '_') {
//                val end = text.indexOf('_', index + 1)
//
//                if (end != -1) {
//                    withStyle(
//                        SpanStyle(
//                            fontStyle = FontStyle.Italic
//                        )
//                    ) {
//                        append(text.substring(index + 1, end))
//                    }
//
//                    index = end + 1
//                    continue
//                }
//            }
//
//
//            // خط خورده ~text~
//            if (text[index] == '~') {
//                val end = text.indexOf('~', index + 1)
//
//                if (end > index + 1) {
//                    withStyle(
//                        SpanStyle(
//                            textDecoration = TextDecoration.LineThrough
//                        )
//                    ) {
//                        append(text.substring(index + 1, end))
//                    }
//
//                    index = end + 1
//                    continue
//                }
//            }
//
//            append(text[index])
//            index++
//        }
//    }
//}
//
//fun String.toAnnotatedLinkString(onPrimary: Color): AnnotatedString {
//    val spannable = SpannableString(this)
//
//    Linkify.addLinks(spannable, Linkify.WEB_URLS)
//
//    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
//
//    return buildAnnotatedString {
//        var lastIndex = 0
//
//        for (span in spans.sortedBy { urlSpan ->
//            spannable.getSpanStart(urlSpan)
//        }) {
//            val start = spannable.getSpanStart(span)
//            val end = spannable.getSpanEnd(span)
//
//            append(this@toAnnotatedLinkString.substring(lastIndex, start))
//
//            withLink(
//                LinkAnnotation.Url(
//                    url = span.url, styles = TextLinkStyles(
//                        style = SpanStyle(
//                            color = onPrimary, textDecoration = TextDecoration.Underline
//                        )
//                    )
//                )
//            ) {
//                append(this@toAnnotatedLinkString.substring(start, end))
//            }
//
//            lastIndex = end
//        }
//
//        append(this@toAnnotatedLinkString.substring(lastIndex))
//    }
//}

fun convertDigits(text: String, digits: CharArray): String {
    require(digits.size == 10) { "digits must contain exactly 10 characters." }

    val builder = StringBuilder(text.length)

    for (ch in text) {
        if (ch in '0'..'9') {
            builder.append(digits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }

    return builder.toString()
}

fun hash20(text: String): Int {
    return (text.hashCode() and Int.MAX_VALUE) % 19
}