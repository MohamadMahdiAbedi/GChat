package ir.gchat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.compose.ui.text.*
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.SoundEffectConstants
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import android.content.Context
import android.provider.OpenableColumns
import androidx.compose.foundation.horizontalScroll
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.security.MessageDigest


class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    val socketViewModel: SocketViewModel by viewModels()
    //val smsViewModel: SmsChatViewModel by lazy {
    //    ViewModelProvider(this)[SmsChatViewModel::class.java]
    //}

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
                    // sms setup
                    //val context = LocalContext.current
                    //if (checkSmsAppRole(context)) {
                    //    DisposableEffect(Unit) {
                    //        smsViewModel.init(context)
                    //        //smsViewModel.refreshChatList()

                    //        onDispose {

                    //        }
                    //    }
                    //}
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
                            loginError = socketViewModel.loginError.collectAsState().value,
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
                            //smsViewModel = smsViewModel,
                            //viewModel = viewModel,
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
                            uploads = socketViewModel.uploads.collectAsState().value
                        )
                        SetUpSystemBars(palette = palette)
                    }
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                }
            }
        }

        socketViewModel.connect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.setPendingIntent(intent)
    }
}

@Composable
fun SetUpSystemBars(palette: Int) {
    val colorLuminance = remember(palette) {
        materialColors[palette].luminance()
    }
    val darkTheme = remember(colorLuminance) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            colorLuminance >= 0.7f
        } else {
            colorLuminance >= 0.5f
        }
    }
    //val val statusBarColor = remember { Color(0x33000000) }
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        controller.isAppearanceLightNavigationBars = !darkTheme

        // Status Bar
        controller.isAppearanceLightStatusBars = darkTheme

        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = /*statusBarColor*/Color(0x33000000).toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
        }

    }

    SideEffect {
        apply()
    }

    DisposableEffect(lifecycleOwner, darkTheme) {

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
    loginError: Int,
    getRules: () -> String,
    searchContactList: List<Contact>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit,
    logout: () -> Unit,
    sendMessage: (String, List<ContentEntity>) -> Unit,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    getConversations: () -> Unit,
    seenMessage: (String, Int) -> Unit,
    //smsViewModel: SmsChatViewModel,
    //viewModel: MainViewModel,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    setColor: (Int) -> Unit,
    paletteIndex: Int,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    uploads: List<File>
) {
    val navController = rememberNavController()
    //val pendingIntent by viewModel.pendingIntent.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    //val context = LocalContext.current
    //val initialIntent = (context as? Activity)?.intent

    //LaunchedEffect(Unit) {
    //    val intent = initialIntent ?: return@LaunchedEffect
    //    if (intent.getBooleanExtra("open_sms_chat", false)) {
    //        //delay(500.milliseconds)
    //        val id = intent.getStringExtra("id").orEmpty()
    //        val displayName = intent.getStringExtra("displayName").orEmpty()
    //        if (id.isNotBlank()) {
    //            navController.navigate(
    //                "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
    //            ) {
    //                launchSingleTop = true
    //            }
    //        }
    //    }
    //}

    //LaunchedEffect(pendingIntent) {
    //    val intent = pendingIntent ?: return@LaunchedEffect
    //    if (intent.getBooleanExtra("open_sms_chat", false)) {
    //        //delay(300.milliseconds)
    //        val id = intent.getStringExtra("id").orEmpty()
    //        val displayName = intent.getStringExtra("displayName").orEmpty()

    //        viewModel.setPendingIntent(null)

    //        if (id.isNotBlank()) {
    //            val currentRoute = navController.currentBackStackEntry?.destination?.route

    //            if (currentRoute?.contains("mainScreen") == true ||
    //                currentRoute?.contains("smsMainScreen") == true
    //            ) {
    //                navController.navigate(
    //                    "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
    //                ) {
    //                    launchSingleTop = true
    //                }
    //            } else {
    //                navController.navigate(
    //                    "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
    //                ) {
    //                    popUpTo("mainScreen") { inclusive = false }
    //                    launchSingleTop = true
    //                }
    //            }
    //        }
    //    }
    //}

    //val isSmsScreen = currentRoute?.startsWith("sms") == true

    LaunchedEffect(loggedIn, oldLoggedIn/*, isSmsScreen*/) {
        //if (isSmsScreen) return@LaunchedEffect

        if (oldLoggedIn) {
            if (loggedIn) {
                if (currentRoute != "mainScreen" && currentRoute != "chatScreen") {
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
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = "wait",
        //startDestination = "greeting",
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
                    loginError = loginError
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
                        //CircularProgressIndicator()
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
                        //TextButton(
                        //    shape = RoundedCornerShape(2.dp), onClick = {
                        //        view.playSoundEffect(SoundEffectConstants.CLICK)
                        //        navController.navigate("smsMainScreen")
                        //    }
                        //) {
                        //    Text("View SMS Chats")
                        //}
                    }
                }
            }
        }
        composable(route = "mainScreen") {
            MainScreen(
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
                //smsViewModel = smsViewModel,
                username = username,
                shouldScrollToBottom = shouldScrollToBottom,
                onScrolledToBottom = onScrolledToBottom,
                getUploadUri = getUploadUri,
                uploads = uploads
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
            route = "chatScreen?id={id}&displayName={displayName}&selectedChatUnreadCount={selectedChatUnreadCount}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
            }, navArgument("displayName") {
                type = NavType.StringType
            }, navArgument("selectedChatUnreadCount") {
                type = NavType.IntType
            })
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
                uploads = uploads
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

//@Composable
//fun ContactItem(contact: Contact, onClick: () -> Unit) {
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(72.dp),
//        color = MaterialTheme.colorScheme.surface,
//        onClick = onClick
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Spacer(
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(start = 72.dp)
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .background(
//                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
//                    )
//            )
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(Modifier.size(40.dp)) {
//                    val backgroundColor = materialColors[hash20(contact.id)]
//                    val iconColor =
//                        if (backgroundColor.luminance() >= 0.5f) {
//                            Color.Black
//                        } else {
//                            Color.White
//                        }
//
//                    Surface(
//                        modifier = Modifier.size(40.dp),
//                        shape = CircleShape,
//                        color = backgroundColor
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.profile_black_content),
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxSize(),
//                            tint = iconColor.copy(alpha = 0.5f)
//                        )
//                    }
//
//                    if (contact.isOnline) {
//                        Spacer(
//                            Modifier
//                                .align(Alignment.TopEnd)
//                                .padding(2.dp)
//                                .size(8.dp)
//                                .background(Color(0xFF23A55A), CircleShape)
//                        )
//                    }
//                }
//
//                Spacer(Modifier.width(16.dp))
//
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//
//                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f), verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = contact.name,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier.weight(1f)
//                        )
//
//                        Spacer(Modifier.width(4.dp))
//
//                        Text(
//                            text = formatMessageTime(contact.lastMessageDate),
//                            style = MaterialTheme.typography.bodySmall,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = contact.lastMessageText,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurface.copy(
//                                alpha = 0.6f
//                            ),
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier.weight(1f)
//                        )
//
//                        Spacer(Modifier.width(4.dp))
//
//                        if (contact.unreadMessages > 0) {
//
//                            Box(
//                                modifier = Modifier
//                                    .defaultMinSize(minWidth = 20.dp)
//                                    .height(20.dp)
//                                    .background(
//                                        MaterialTheme.colorScheme.primary,
//                                        RoundedCornerShape(10.dp)
//                                    )
//                                    .padding(horizontal = 4.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//
//                                Text(
//                                    text = contact.unreadMessages.toString(),
//                                    color = MaterialTheme.colorScheme.onPrimary,
//                                    style = MaterialTheme.typography.labelSmall,
//                                    maxLines = 1
//                                )
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

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
                                        contact.lastMessageText.toRichAnnotatedString(
                                            linkColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Text(
                                text = contact.lastMessageText.replace("\n", " ")
                                    .toRichAnnotatedString(linkColor = MaterialTheme.colorScheme.onPrimary),
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

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainScreen(
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
    //smsViewModel: SmsChatViewModel,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    uploads: List<File>
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val drawerRatio = when (windowSizeClass.widthSizeClass) {
        Compact -> 0.8f
        Medium -> 0.5f
        else -> 0.3f
    }

    //val context = LocalContext.current

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

    //var hasSmsAppRole by remember { mutableStateOf(false) }

    //fun refreshPermissions() {
    //    hasSmsAppRole = checkSmsAppRole(context)
    //}

    //val smsRoleLauncher = rememberLauncherForActivityResult(
    //    ActivityResultContracts.StartActivityForResult()
    //) {
    //    refreshPermissions()
    //}

    LaunchedEffect(Unit) {
        getConversations()
        //refreshPermissions()
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(drawerRatio)
                    .shadow(elevation = 16.dp, clip = false)
                    //.background(MaterialTheme.colorScheme.surface)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {

                //var expanded by remember { mutableStateOf(false) }
                //val expansionHeight = animateDpAsState(
                //    targetValue = if (expanded) 160.dp else 0.dp,
                //    animationSpec = tween(durationMillis = 200)
                //)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .background(MaterialTheme.colorScheme.primary)
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
                        }) {
                    //Image(
                    //    painter = painterResource(R.drawable.wallpaper_picture),
                    //    contentDescription = null,
                    //    modifier = Modifier.fillMaxSize(),
                    //    contentScale = ContentScale.Crop
                    //)
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Column(
                        modifier = Modifier.padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        )
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                                    .clip(CircleShape), shape = CircleShape, color = backgroundColor
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.profile_black_content),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = iconColor.copy(alpha = 0.5f)
                                )
                            }
                            //Surface(
                            //    modifier = Modifier
                            //        .padding(vertical = 16.dp)
                            //        .padding(end = 16.dp)
                            //        .fillMaxHeight(1 / 2f)
                            //        .aspectRatio(1f)
                            //        .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                            //        .clip(CircleShape), shape = CircleShape
                            //) {
                            //    Image(
                            //        painter = painterResource(R.drawable.profile),
                            //        contentDescription = null,
                            //        modifier = Modifier.fillMaxSize(),
                            //        contentScale = ContentScale.Crop
                            //    )
                            //}
                            //Surface(
                            //    modifier = Modifier
                            //        .padding(vertical = 16.dp)
                            //        .padding(end = 16.dp)
                            //        .fillMaxHeight(1 / 2f)
                            //        .aspectRatio(1f)
                            //        .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                            //        .clip(CircleShape), shape = CircleShape
                            //) {
                            //    Image(
                            //        painter = painterResource(R.drawable.profile),
                            //        contentDescription = null,
                            //        modifier = Modifier.fillMaxSize(),
                            //        contentScale = ContentScale.Crop
                            //    )
                            //}
                        }
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
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
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            //Spacer(modifier = Modifier.width(16.dp))

                            //Text(
                            //    modifier = Modifier.weight(1f),
                            //    text = "@imam_ali", // (user id)
                            //    style = MaterialTheme.typography.bodySmall,
                            //    maxLines = 1,
                            //    overflow = TextOverflow.Ellipsis,
                            //    color = MaterialTheme.colorScheme.onPrimary/*.copy(
                            //        alpha = 0.6f
                            //    )*/
                            //)
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
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
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
                    //if (!isSmsApp) {
                    //    DropdownMenuItem(
                    //        text = { Text("Set GChat as default Sms") },
                    //        leadingIcon = {
                    //            Icon(
                    //                painterResource(R.drawable.sms),
                    //                contentDescription = null
                    //            )
                    //        },
                    //        onClick = {
                    //            view.playSoundEffect(SoundEffectConstants.CLICK)
                    //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //                val roleManager =
                    //                    context.getSystemService(RoleManager::class.java)

                    //                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(
                    //                        RoleManager.ROLE_SMS
                    //                    )
                    //                ) {
                    //                    smsRoleLauncher.launch(
                    //                        roleManager.createRequestRoleIntent(
                    //                            RoleManager.ROLE_SMS
                    //                        )
                    //                    )
                    //                }
                    //            } else {
                    //                requestSmsDefaultRole(context)
                    //            }
                    //        }
                    //    )
                    //}

                    //if (isSmsApp) {
                    //    DropdownMenuItem(
                    //        text = { Text("SMS Chat List") },
                    //        leadingIcon = {
                    //            Icon(
                    //                painterResource(R.drawable.sms),
                    //                contentDescription = null
                    //            )
                    //        },
                    //        onClick = {
                    //            view.playSoundEffect(SoundEffectConstants.CLICK)
                    //            navHostController.navigate("smsMainScreen")
                    //        }
                    //    )
                    //}
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
        //Box
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
                                Text("GChat")
                            }, /*expandedHeight = 56.dp,*/ navigationIcon = {
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
                                        }
                                    })
                            }
                        }

                        //LaunchedEffect(isSmsApp) {
                        if (isSmsApp) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(innerPadding),
                                color = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Restore your default messaging app to receive SMS messages.",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    TextButton(
                                        shape = RoundedCornerShape(2.dp), onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                try {
                                                    context.startActivity(
                                                        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                                    )
                                                } catch (_: Exception) {
                                                    context.startActivity(
                                                        Intent(Settings.ACTION_SETTINGS)
                                                    )
                                                }
                                            } else {
                                                context.startActivity(
                                                    Intent(Settings.ACTION_SETTINGS)
                                                )
                                            }
                                        }, colors = ButtonDefaults.textButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Text("Open Settings")
                                    }
                                }
                            }
                        }
                        //}

                        //Button(
                        //    onClick = {
                        //        view.playSoundEffect(SoundEffectConstants.CLICK)
                        //    },
                        //    modifier = Modifier
                        //        .padding(8.dp)
                        //        .padding(innerPadding)
                        //        .size(56.dp)
                        //        .align(Alignment.BottomEnd)
                        //        .shadow(
                        //            elevation = 6.dp, shape = CircleShape, clip = false
                        //        ),
                        //    shape = CircleShape,
                        //    contentPadding = PaddingValues(16.dp)
                        //) {
                        //    Icon(
                        //        painter = painterResource(R.drawable.edit),
                        //        contentDescription = "Accept&&Sign-In",
                        //        modifier = Modifier.fillMaxSize(),
                        //        tint = MaterialTheme.colorScheme.onPrimary
                        //    )
                        //}
                    }
                }

                //search
                val heightFraction by animateFloatAsState(
                    //64.dp.value / 1000 or 0f
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
                                elevation = 4.dp, shape = RoundedCornerShape(2.dp), clip = false
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
                                    getMessagesList(selectedChat)
                                    if (!expandedScreen) {
                                        //selectedChatUnreadCount این رو باید درست پاس بدی این یه باگ نیست در آیده هندل میشه
                                        navHostController.navigate("chatScreen?id=$id&displayName=$id&selectedChatUnreadCount=${selectedChatUnreadCount}")
                                    }
                                })
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
                    ChatScreen(
                        back = { navHostController.popBackStack() },
                        id = selectedChat,
                        sendMessage = sendMessage,
                        messageList = messageList,
                        seenMessage = seenMessage,
                        getMessagesList = getMessagesList,
                        //type = selectedType,
                        displayName = selectedChatDisplayName,
                        unreadCount = selectedChatUnreadCount,
                        shouldScrollToBottom = shouldScrollToBottom,
                        onScrolledToBottom = onScrolledToBottom,
                        getUploadUri = getUploadUri,
                        uploads = uploads
                    )
                }
            }
        }
    }

}

//@Composable
//fun AnimatedMenu(
//    modifier: Modifier,
//    width: Dp,
//    height: Dp,
//    chord: Dp,
//    isExpanded: Boolean,
//    close: () -> Unit,
//    ratioX: Float,
//    offsetX: Dp = 0.dp,
//    ratioY: Float,
//    offsetY: Dp = 0.dp,
//    position: Alignment,
//    hasBackgroundCover: Boolean,
//    tabletView: Boolean = false,
//    whatIsMyBackgroundFilterColor: (Color, Boolean) -> Unit,
//    content: @Composable () -> Unit
//) {
//    val sizeBtn by animateDpAsState(
//        targetValue = if (isExpanded) (chord.value * 2).dp else 48.dp, animationSpec = tween(
//            durationMillis = 200, easing = FastOutSlowInEasing
//        ), label = "circle_size"
//    )
//
//    //val surface = MaterialTheme.colorScheme.surface
//    val surface = MenuDefaults.containerColor
//
//    whatIsMyBackgroundFilterColor(
//        if (hasBackgroundCover) {
//            Color(0xFF000000).copy(
//                alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
//                    0f, 0.25f
//                )
//            )
//        } else {
//            Color.Transparent
//        }, (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f))
//    )
//
//    BackHandler(enabled = isExpanded) {
//        close()
//    }
//
//    LaunchedEffect(
//        key1 = hasBackgroundCover, key2 = sizeBtn, key3 = chord
//    ) {
//        whatIsMyBackgroundFilterColor(
//            if (hasBackgroundCover) {
//                Color(0xFF000000).copy(
//                    alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
//                        0f, 0.25f
//                    )
//                )
//            } else {
//                Color.Transparent
//            }, (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f))
//        )
//    }
//
//    Box(modifier = modifier.fillMaxSize()) {
//        if (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f)) {
//            Spacer(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clickable(
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }) {
//                        close()
//                    }
//                    .background(
//                        color = if (hasBackgroundCover) {
//                            Color(0xFF000000).copy(
//                                alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
//                                    0f, 0.25f
//                                )
//                            )
//                        } else {
//                            Color.Transparent
//                        }
//                    ))
//        }
//
//        Box(
//            modifier = Modifier
//                .padding(8.dp)
//                .height(height)
//                .width(width)
//                .shadow(
//                    elevation = if (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)) > 0.99f && isExpanded) 8.dp else 0.dp,
//                    shape = RoundedCornerShape(2.dp),
//                    clip = false
//                )
//                .clip(RoundedCornerShape(2.dp))
//                .align(position)
//        ) {
//            val canvasModifier =
//                if (sizeBtn / 2 != 24.dp) Modifier
//                    .fillMaxSize()
//                    .align(Alignment.Center)
//                    .clickable(
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }) { } else Modifier
//                    .fillMaxSize()
//                    .align(Alignment.Center)
//
//            Canvas(
//                modifier = canvasModifier
//            ) {
//                val radius = sizeBtn.toPx() / 2
//
//                //Alignment.BottomStart -> bottomLeft Menu
//                //Alignment.TopEnd -> dropdown Menu
//
//                // با آفست و نسبت دیگه این بساط هم جمع میشه
//
//                val centerX = when (position) {
//                    Alignment.BottomStart -> size.width * ratioX + 24.dp.toPx() - offsetX.toPx()
//                    Alignment.TopEnd -> if (tabletView) size.width * ratioX - 24.dp.toPx() - offsetY.toPx() else size.width * ratioX - 12.dp.toPx() - offsetX.toPx()
//                    else -> size.width * ratioX - offsetX.toPx() // Default fallback
//                }
//
//                val centerY = when (position) {
//                    Alignment.BottomStart -> size.height * ratioY - 24.dp.toPx() - offsetY.toPx()
//                    Alignment.TopEnd -> if (tabletView) size.height * ratioY + 24.dp.toPx() - offsetY.toPx() else size.height * ratioY + 12.dp.toPx() - offsetY.toPx()
//                    else -> size.height * ratioY - offsetY.toPx() // Default fallback
//                }
//
//                drawCircle(
//                    color = if (radius != 24.dp.toPx()) surface else Color.Transparent,
//                    radius = radius,
//                    center = Offset(centerX, centerY)
//                )
//            }
//
//            if ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f) {
//                Box(
//                    modifier = Modifier.alpha(
//                        ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
//                            0f, 1f
//                        )
//                    )
//                ) {
//                    content()
//                }
//            }
//        }
//    }
//}

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

private fun Context.getFileName(uri: Uri): String? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index >= 0) {
            return cursor.getString(index)
        }
    }
    return null
}

private fun Context.getFileSize(uri: Uri): Long? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && index >= 0) {
            return cursor.getLong(index)
        }
    }
    return null
}

private fun Context.sha256(uri: Uri): String? {
    val digest = MessageDigest.getInstance("SHA-256")

    contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(8192)

        while (true) {
            val read = input.read(buffer)
            if (read == -1) break

            digest.update(buffer, 0, read)
        }
    } ?: return null

    return digest.digest().joinToString("") {
        "%02x".format(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    back: () -> Boolean,
    id: String,
    sendMessage: (String, List<ContentEntity>) -> Unit,
    messageList: List<MessageItem>,
    seenMessage: (String, Int) -> Unit,
    getMessagesList: (String) -> Unit,
    draft: String? = "",
    displayName: String,
    unreadCount: Int,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    getUploadUri: (String, Long, String, Uri?) -> Unit,
    uploads: List<File>
) {
    //val context = LocalContext.current
    //var localSms by remember { mutableStateOf(emptyList<MessageItem>()) }
    //LaunchedEffect(id, type) {
    //    if (id.isNotBlank()) {
    //        if (type == "phone_sms_contact") {
    //            localSms = getMessagesForNumber(context, id)
    //        } else {
    //            getMessagesList(id)
    //        }
    //    }
    //}

    LaunchedEffect(id) {
        if (id.isNotBlank()) {
            getMessagesList(id)
        }
    }

    //var renderValue by remember { mutableIntStateOf(4) }
    var isExpandedAttachment by remember { mutableStateOf(false) }
    //var isExpandedEmoji by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf(draft.toString()) }

    //var coverColor by remember { mutableStateOf(Color.Transparent) }
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

    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri == null) return@rememberLauncherForActivityResult

        selectedUri = uri

        val name = context.getFileName(uri)!!
        val size = context.getFileSize(uri)!!
        val sha256 = context.sha256(uri)!!

        val uri = selectedUri
        getUploadUri(name, size, sha256, uri)
    }

    var showFileRow by rememberSaveable(uploads) { mutableStateOf(uploads.isNotEmpty()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            //.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background, topBar = {
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
                        }, /*expandedHeight = 56.dp,*/ navigationIcon = {
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
                        //actions = {
                        //    if (type == "phone_sms_contact") {
                        //        IconButton(
                        //            onClick = {
                        //                view.playSoundEffect(SoundEffectConstants.CLICK)
                        //                localSms = getMessagesForNumber(context, id)
                        //            }) {
                        //            Icon(
                        //                painter = painterResource(R.drawable.refresh),
                        //                contentDescription = "Refresh Chat"
                        //            )
                        //        }
                        //    }
                        //},
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

                    //val shouldScrollToBottom by viewModel.shouldScrollToBottom.collectAsState()

//                    LaunchedEffect(shouldScrollToBottom) {
//                        if (shouldScrollToBottom) {
//                            listState.animateScrollToItem(messageList.lastIndex)
//                            onScrolledToBottom()
//                        }
//                    }

                    LaunchedEffect(shouldScrollToBottom, messageList.size) {
                        if (shouldScrollToBottom && messageList.isNotEmpty()) {
                            listState.animateScrollToItem(messageList.lastIndex)
                        }
                        if (shouldScrollToBottom) {
                            onScrolledToBottom()
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(state = listState) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

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
                                    timestamp = item.date
                                )
                            }
                        }
                        this@Column.AnimatedVisibility(
                            visible = showButton,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            enter = slideInVertically { it },// + fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = slideOutVertically { it }// + fadeOut() + scaleOut(targetScale = 0.8f)
                        ) {
                            Button(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        //val last = listState.layoutInfo.totalItemsCount - 1
                                        ////listState.scrollToItem(last)
                                        ////listState.scrollBy(-listState.layoutInfo.viewportSize.height.toFloat())
                                        //listState.scrollToItem(last)

                                        val last = listState.layoutInfo.totalItemsCount - 1
                                        if (last >= 0) {
                                            listState.scrollToItem(last)
                                        }
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
                    //}
                    if (showFileRow && uploads.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uploads.forEach { file ->
                                Surface(
                                    modifier = Modifier.padding(end = 8.dp),
                                    shape = RoundedCornerShape(2.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = file.name,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                                if (message.isNotBlank()) {
                                    message = message.replace(Regex("\\n+$"), "").trim()
                                    scope.launch {
                                        animate = true
                                        delay(1000.milliseconds)
                                        animate = false
                                    }
                                    val content = mutableListOf<ContentEntity>()
                                    uploads.forEach { file ->
                                        content += ContentEntity(
                                            type = "file",
                                            id = file.id.toString(),
                                            fileName = file.name
                                        )
                                    }
                                    content += ContentEntity(
                                        type = "text",
                                        text = message
                                    )
                                    sendMessage(id, content)
                                    message = ""
                                } else {
                                    // شروع ضبط صوت
                                }
                            }, modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = if (message.isNotBlank()) painterResource(R.drawable.send) else painterResource(
                                    R.drawable.mic
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
                            text = { Text(text = "Photos and videos") }, onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                isExpandedAttachment = false
                                launcher.launch(
                                    arrayOf(
                                        "image/*", "video/*"
                                    )
                                )
                            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.photo), contentDescription = null
                                )
                            }, trailingIcon = { }, enabled = true
                        )
                        DropdownMenuItem(
                            text = { Text(text = "File") }, onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                isExpandedAttachment = false
                            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.folder), contentDescription = null
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
                    //top app bar size + notification bar size
                    //.requiredHeight(64.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .background(coverColor)
                    .align(Alignment.TopCenter)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        isExpandedAttachment = false
                    })
        }
    }
}

@Composable
fun Message(isMe: Boolean,content: List<ContentEntity> /*message: String*/, seen: Boolean, timestamp: String) {
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
                Box(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (content.size == 1) {
                        if (content[0].type == "text") {
                            Text(
                                text = content[0].text.toRichAnnotatedString(linkColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.padding(
                                    end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                                    bottom = if (lineCount > 1) 24.dp else 0.dp
                                ),
                                onTextLayout = {
                                    if (lineCount == 0) {
                                        lineCount = it.lineCount
                                    }
                                }
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Surface(shape = CircleShape) {
                                    Icon(
                                        painter = painterResource(R.drawable.photo),
                                        contentDescription = null
                                    )
                                }
                                Text(content[0].fileName)
                            }
                        }
                        // فایل به هر صورت یک خطی
                        // متن طبق الگوریتم قبلی
                    } else {
                        Column(
                            modifier = Modifier.padding(
                                bottom = 24.dp
                            ),
                        ) {
                            content.forEach { contentEntity ->
                                when (contentEntity.type) {
                                    "text" -> {
                                        Text(
                                            text = contentEntity.text.toRichAnnotatedString(linkColor = MaterialTheme.colorScheme.onPrimary),
                                            modifier = Modifier.padding(
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
                                        Row(modifier = Modifier.fillMaxSize()) {
                                            Surface(shape = CircleShape) {
                                                Icon(
                                                    painter = painterResource(R.drawable.photo),
                                                    contentDescription = null
                                                )
                                            }
                                            Text(contentEntity.fileName)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Text(
                    //    text = message.toRichAnnotatedString(linkColor = MaterialTheme.colorScheme.onPrimary),
                    //    modifier = Modifier.padding(
                    //        end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                    //        bottom = if (lineCount > 1) 24.dp else 0.dp
                    //    ),
                    //    onTextLayout = {
                    //        if (lineCount == 0) {
                    //            lineCount = it.lineCount
                    //        }
                    //    }
                    //)
                    Row(
                        modifier = Modifier.align(if (lineCount == 1) Alignment.CenterEnd else Alignment.BottomEnd),
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
        start: Int, end: Int, style: SpanStyle = SpanStyle()
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