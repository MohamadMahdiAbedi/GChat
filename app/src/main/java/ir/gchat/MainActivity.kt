package ir.gchat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.SoundEffectConstants
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import ir.gchat.ui.theme.GChatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    val socketViewModel: SocketViewModel by viewModels()
    val smsViewModel: SmsChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        socketViewModel.connect()

        enableEdgeToEdge()
        setContent {
            // theme
            val theme by viewModel.theme.collectAsState()
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
            val context = LocalContext.current
            if (checkSmsAppRole(context)) {
                DisposableEffect(Unit) {
                    smsViewModel.init(context)
                    smsViewModel.refreshChatList()

                    onDispose {

                    }
                }
            }
            GChatTheme(
                dynamicColor = false, darkTheme = darkTheme
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
                    seenMessage = { contact, id -> socketViewModel.seenMessage(contact, id) },
                    smsViewModel = smsViewModel,
                    viewModel = viewModel,
                    username = socketViewModel.usernameState.collectAsState().value,
                    shouldScrollToBottom = socketViewModel.shouldScrollToBottom.collectAsState().value,
                    onScrolledToBottom = { socketViewModel.onScrolledToBottom() }
                )
                SetUpSystemBars(darkTheme)
            }
        }

        window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.setPendingIntent(intent)
    }
}

@Composable
fun SetUpSystemBars(
    darkTheme: Boolean,
    statusBarColor: Color = Color(0x33000000),
    navigationBarColor: Color = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) Color.Black
    else Color.Transparent
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context as Activity
    val window = activity.window
    val lifecycleOwner = LocalLifecycleOwner.current

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    fun apply() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

        val controller = WindowCompat.getInsetsController(window, view)

        controller.isAppearanceLightStatusBars = darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = statusBarColor.toArgb()

            @Suppress("DEPRECATION")
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
    searchContactList: List<SearchEntity>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit,
    logout: () -> Unit,
    sendMessage: (String, String) -> Unit,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    getConversations: () -> Unit,
    seenMessage: (String, Int) -> Unit,
    smsViewModel: SmsChatViewModel,
    viewModel: MainViewModel,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit
) {
    val navController = rememberNavController()
    val pendingIntent by viewModel.pendingIntent.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current
    val initialIntent = (context as? Activity)?.intent

    LaunchedEffect(Unit) {
        val intent = initialIntent ?: return@LaunchedEffect
        if (intent.getBooleanExtra("open_sms_chat", false)) {
            delay(500.milliseconds)
            val id = intent.getStringExtra("id").orEmpty()
            val displayName = intent.getStringExtra("displayName").orEmpty()
            if (id.isNotBlank()) {
                navController.navigate(
                    "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
                ) {
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(pendingIntent) {
        val intent = pendingIntent ?: return@LaunchedEffect
        if (intent.getBooleanExtra("open_sms_chat", false)) {
            delay(300.milliseconds)
            val id = intent.getStringExtra("id").orEmpty()
            val displayName = intent.getStringExtra("displayName").orEmpty()

            viewModel.setPendingIntent(null)

            if (id.isNotBlank()) {
                val currentRoute = navController.currentBackStackEntry?.destination?.route

                if (currentRoute?.contains("mainScreen") == true ||
                    currentRoute?.contains("smsMainScreen") == true
                ) {
                    navController.navigate(
                        "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
                    ) {
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(
                        "smsChatScreen?id=${Uri.encode(id)}&displayName=${Uri.encode(displayName)}"
                    ) {
                        popUpTo("mainScreen") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val isSmsScreen = currentRoute?.startsWith("sms") == true

    LaunchedEffect(loggedIn, oldLoggedIn, isSmsScreen) {
        if (isSmsScreen) return@LaunchedEffect

        if (oldLoggedIn) {
            if (loggedIn) {
                if (currentRoute != "mainScreen" &&
                    currentRoute != "chatScreen"
                ) {
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
        startDestination = "greeting",

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
                smsViewModel = smsViewModel,
                username = username,
                shouldScrollToBottom = shouldScrollToBottom,
                onScrolledToBottom = onScrolledToBottom
            )
        }
        composable(route = "appearanceSettings") {
            AppearanceSettingsScreen(
                setTheme = setTheme,
                theme = theme,
                navHostController = navController
            )
        }
        composable(route = "settings") {
            SettingsScreen(navHostController = navController)
        }
        composable(
            //route = "chatScreen?id={id}&type={type}&displayName={displayName}",
            route = "chatScreen?id={id}&displayName={displayName}&selectedChatUnreadCount={selectedChatUnreadCount}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("displayName") {
                    type = NavType.StringType
                },
                navArgument("selectedChatUnreadCount") {
                    type = NavType.IntType
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
                onScrolledToBottom = onScrolledToBottom
            )
        }
        composable(route = "smsMainScreen") {
            SMSMainScreen(
                navHostController = navController, smsViewModel = smsViewModel
            )
        }
        composable(
            route = "smsChatScreen?id={id}&displayName={displayName}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
            }, navArgument("displayName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val displayName = backStackEntry.arguments?.getString("displayName") ?: ""
            SMSChatScreen(
                back = { navController.popBackStack() },
                id = id,
                displayName = displayName,
                smsViewModel = smsViewModel
            )
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
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            shape = RoundedCornerShape(2.dp), onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                navController.navigate("smsMainScreen")
                            }
                        ) {
                            Text("View SMS Chats")
                        }
                    }
                }
            }
        }
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
    searchContactList: List<SearchEntity>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit,
    logout: () -> Unit,
    sendMessage: (String, String) -> Unit,
    messageList: List<MessageItem>,
    getMessagesList: (String) -> Unit,
    getConversations: () -> Unit,
    seenMessage: (String, Int) -> Unit,
    smsViewModel: SmsChatViewModel,
    username: String,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val drawerRatio = when (windowSizeClass.widthSizeClass) {
        Compact -> 0.8f
        Medium -> 0.5f
        else -> 0.3f
    }

    val context = LocalContext.current

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium)) }
    var selectedChat by rememberSaveable { mutableStateOf("") }
    var selectedChatDisplayName by rememberSaveable { mutableStateOf("") }
    var selectedChatUnreadCount by rememberSaveable { mutableIntStateOf(0) }

    val view = LocalView.current

    val materialColors = listOf(
        Color(0xFF607D8B),
        Color(0xFF9E9E9E),
        Color(0xFFFFEB3B),
        Color(0xFFCDDC39),
        Color(0xFF03A9F4),
        Color(0xFF673AB7),
        Color(0xFFFF5722),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF009688),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF795548),
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        Color(0xFF00BCD4),
        Color(0xFF3F51B5),
        Color(0xFFE91E63)
    )

    val backgroundColor = materialColors[hash20(username)]
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    LaunchedEffect(Unit) {
        getConversations()
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
                    Image(
                        painter = painterResource(R.drawable.wallpaper_picture),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(
                            top = WindowInsets.statusBars.asPaddingValues()
                                .calculateTopPadding()
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
                    var isSmsApp by remember { mutableStateOf(checkSmsAppRole(context)) }

                    val lifecycle = LocalLifecycleOwner.current.lifecycle

                    LaunchedEffect(lifecycle) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            isSmsApp = checkSmsAppRole(context)
                        }
                    }

                    LaunchedEffect(isSmsApp) {
                        if (isSmsApp) {
                            smsViewModel.init(context)
                            smsViewModel.refreshChatList()
                        }
                    }

                    if (!isSmsApp) {
                        DropdownMenuItem(
                            text = { Text("Set GChat as default Sms") },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.sms),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                requestSmsDefaultRole(context)
                            }
                        )
                    }

                    if (isSmsApp) {
                        DropdownMenuItem(
                            text = { Text("SMS Chat List") },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.sms),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                navHostController.navigate("smsMainScreen")
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Setting") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.settings),
                                contentDescription = null
                            )
                        }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            navHostController.navigate("settings")
                        }
                    )
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
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    onClick = {
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

                                            //Surface(
                                            //    modifier = Modifier.size(40.dp), shape = CircleShape
                                            //) {
                                            //    Image(
                                            //        painter = painterResource(R.drawable.profile),
                                            //        contentDescription = null,
                                            //        modifier = Modifier.fillMaxSize(),
                                            //        contentScale = ContentScale.Crop
                                            //    )
                                            //}

                                            val backgroundColor = materialColors[hash20(contact.id)]
                                            val iconColor =
                                                if (backgroundColor.luminance() >= 0.5f) {
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
                                                    text = contact.name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = contact.lastMessageText,
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
                                                    text = formatMessageTime(contact.lastMessageDate),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                if (contact.unreadMessages > 0) {

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
                                                            text = contact.unreadMessages.toString(),
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

                Scaffold(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .alpha(heightFraction),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { innerPadding ->
                    Card(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        //colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(2.dp),
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

                                    if (it.isBlank()) {
                                        clearSearchList()
                                    } else {
                                        searchContact(it)
                                    }
                                },
                                colors = colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent
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
                                    }
                                )
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
                            .padding(innerPadding)
                            .padding(top = 56.dp)
                            .padding(8.dp)
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
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    val id = item.username
                                    selectedChat = id
                                    getMessagesList(selectedChat)
                                    if (!expandedScreen) {
                                        //selectedChatUnreadCount این رو باید درست پاس بدی این یه باگ نیست در آیده هندل میشه
                                        navHostController.navigate("chatScreen?id=$id&displayName=$id&selectedChatUnreadCount=${selectedChatUnreadCount}")
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

                                        Box(Modifier.size(40.dp)) {
                                            val backgroundColor =
                                                materialColors[hash20(item.username)]
                                            val iconColor =
                                                if (backgroundColor.luminance() >= 0.5f) {
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

                                            if (item.isOnline) {
                                                Spacer(
                                                    Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                        .size(8.dp)
                                                        .background(Color(0xFF23A55A), CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(16.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {

                                            Text(
                                                text = item.username,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = "@${item.username}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        //Spacer(Modifier.width(16.dp))

                                        //Column(
                                        //    horizontalAlignment = Alignment.End
                                        //) {

                                        //    Text(
                                        //        text = item.username,
                                        //        style = MaterialTheme.typography.bodySmall,
                                        //        maxLines = 1,
                                        //        overflow = TextOverflow.Ellipsis
                                        //    )

                                        //    Spacer(Modifier.height(4.dp))

                                        //    if (item.isOnline) {

                                        //        Box(
                                        //            modifier = Modifier
                                        //                .defaultMinSize(minWidth = 20.dp)
                                        //                .height(20.dp)
                                        //                .background(
                                        //                    MaterialTheme.colorScheme.primary,
                                        //                    RoundedCornerShape(10.dp)
                                        //                )
                                        //                .padding(horizontal = 4.dp),
                                        //            contentAlignment = Alignment.Center
                                        //        ) {

                                        //            Text(
                                        //                text = "*",
                                        //                color = MaterialTheme.colorScheme.onPrimary,
                                        //                style = MaterialTheme.typography.labelSmall,
                                        //                maxLines = 1
                                        //            )
                                        //        }
                                        //    }
                                        //}
                                    }
                                }
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
                        onScrolledToBottom = onScrolledToBottom
                    )
                }
            }
        }
    }

}

@Composable
fun AnimatedDropMenu(
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
        targetValue = if (isExpanded) (chord.value * 2).dp else 48.dp, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        ), label = "circle_size"
    )

    //val surface = MaterialTheme.colorScheme.surface
    val surface = MenuDefaults.containerColor

    whatIsMyBackgroundFilterColor(
        if (hasBackgroundCover) {
            Color(0xFF000000).copy(
                alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
                    0f, 0.25f
                )
            )
        } else {
            Color.Transparent
        }, (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f))
    )

    LaunchedEffect(
        key1 = hasBackgroundCover, key2 = sizeBtn, key3 = chord
    ) {
        whatIsMyBackgroundFilterColor(
            if (hasBackgroundCover) {
                Color(0xFF000000).copy(
                    alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
                        0f, 0.25f
                    )
                )
            } else {
                Color.Transparent
            }, (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f))
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f)) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        close()
                    }
                    .background(
                        color = if (hasBackgroundCover) {
                            Color(0xFF000000).copy(
                                alpha = ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
                                    0f, 0.25f
                                )
                            )
                        } else {
                            Color.Transparent
                        }
                    ))
        }

        Box(
            modifier = Modifier
                .padding(8.dp)
                .height(height)
                .width(width)
                .shadow(
                    elevation = if (((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)) > 0.99f && isExpanded) 8.dp else 0.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(2.dp))
                .align(position)
        ) {
            val canvasModifier =
                if (sizeBtn / 2 != 24.dp) Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) { } else Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)

            Canvas(
                modifier = canvasModifier
            ) {
                val radius = sizeBtn.toPx() / 2

                //Alignment.BottomStart -> bottomLeft Menu
                //Alignment.TopEnd -> dropdown Menu

                // با آفست و نسبت دیگه این بساط هم جمع میشه

                val centerX = when (position) {
                    Alignment.BottomStart -> size.width * ratioX + 24.dp.toPx() - offsetX.toPx()
                    Alignment.TopEnd -> if (tabletView) size.width * ratioX - 24.dp.toPx() - offsetY.toPx() else size.width * ratioX - 12.dp.toPx() - offsetX.toPx()
                    else -> size.width * ratioX - offsetX.toPx() // Default fallback
                }

                val centerY = when (position) {
                    Alignment.BottomStart -> size.height * ratioY - 24.dp.toPx() - offsetY.toPx()
                    Alignment.TopEnd -> if (tabletView) size.height * ratioY + 24.dp.toPx() - offsetY.toPx() else size.height * ratioY + 12.dp.toPx() - offsetY.toPx()
                    else -> size.height * ratioY - offsetY.toPx() // Default fallback
                }

                drawCircle(
                    color = if (radius != 24.dp.toPx()) surface else Color.Transparent,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }

            if ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value) > 0.05f) {
                Box(
                    modifier = Modifier.alpha(
                        ((sizeBtn - 48.dp).value / (chord.value * 2 - 48.dp.value)).coerceIn(
                            0f, 1f
                        )
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    back: () -> Boolean,
    id: String,
    sendMessage: (String, String) -> Unit,
    messageList: List<MessageItem>,
    seenMessage: (String, Int) -> Unit,
    getMessagesList: (String) -> Unit,
    draft: String? = "",
    displayName: String,
    unreadCount: Int,
    shouldScrollToBottom: Boolean,
    onScrolledToBottom: () -> Unit
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

    var renderValue by remember { mutableIntStateOf(4) }
    //var isExpandedAttachment by remember { mutableStateOf(false) }
    //var isExpandedEmoji by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf(draft.toString()) }

    //var coverColor by remember { mutableStateOf(Color.Transparent) }
    //val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })

    //var coverColor by rememberSaveable(
    //    stateSaver = colorSaver
    //) { mutableStateOf(Color.Transparent) }

    val view = LocalView.current

    val materialColors = listOf(
        Color(0xFF607D8B),
        Color(0xFF9E9E9E),
        Color(0xFFFFEB3B),
        Color(0xFFCDDC39),
        Color(0xFF03A9F4),
        Color(0xFF673AB7),
        Color(0xFFFF5722),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF009688),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF795548),
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        Color(0xFF00BCD4),
        Color(0xFF3F51B5),
        Color(0xFFE91E63)
    )

    val backgroundColor = materialColors[hash20(id)]
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

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
                fps = 24,
                tails = 90,
                animate = true
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

                        val index = (messageList.size - unreadCount)
                            .coerceIn(0, messageList.lastIndex)

                        val visible = listState.layoutInfo.visibleItemsInfo

                        val firstVisible = visible.firstOrNull()?.index ?: return@LaunchedEffect
                        val lastVisible = visible.lastOrNull()?.index ?: return@LaunchedEffect

                        when {
                            index < firstVisible -> listState.scrollToItem(index)
                            index > lastVisible -> listState.scrollToItem(index)
                        }
                    }

                    //val shouldScrollToBottom by viewModel.shouldScrollToBottom.collectAsState()

                    LaunchedEffect(shouldScrollToBottom) {
                        if (shouldScrollToBottom) {
                            listState.animateScrollToItem(messageList.lastIndex)
                            /*viewModel.*/onScrolledToBottom()
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
                            enter = slideInVertically { it },// + fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = slideOutVertically { it }// + fadeOut() + scaleOut(targetScale = 0.8f)
                        ) {
                            Button(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        val last = listState.layoutInfo.totalItemsCount - 1
                                        //listState.scrollToItem(last)
                                        //listState.scrollBy(-listState.layoutInfo.viewportSize.height.toFloat())
                                        listState.scrollToItem(last)
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp/*, max = 256.dp*/)
                            .background(MaterialTheme.colorScheme.primary)/*.imePadding()*/,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        //IconButton(
                        //    onClick = {
                        //        view.playSoundEffect(SoundEffectConstants.CLICK)
                        //        isExpandedAttachment = true
                        //    }, modifier = Modifier
                        //        .padding(8.dp)
                        //        .size(48.dp)
                        //) {
                        //    Icon(
                        //        painter = painterResource(R.drawable.attach),
                        //        contentDescription = null,
                        //        tint = MaterialTheme.colorScheme.onPrimary
                        //    )
                        //}

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 8.dp, start = 8.dp, bottom = 10.dp)
                                .shadow(
                                    elevation = 4.dp, shape = RectangleShape, clip = false
                                )
                                .background(
                                    //color = MaterialTheme.colorScheme.surface,
                                    color = MaterialTheme.colorScheme.background,
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
                                    renderValue = (1..10).random()
                                    sendMessage(id, message)
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

                //AnimatedDropMenu(
                //    modifier = Modifier.padding(innerPadding),
                //    width = 224.dp,
                //    height = 112.dp,
                //    chord = 250.dp,
                //    isExpanded = isExpandedAttachment,
                //    close = { isExpandedAttachment = false },
                //    ratioX = 0f,
                //    ratioY = 1f,
                //    position = Alignment.BottomStart,
                //    hasBackgroundCover = true,
                //    whatIsMyBackgroundFilterColor = { color, show ->
                //        covered = show
                //        coverColor = color
                //    }) {
                //    Column {
                //        Spacer(
                //            modifier = Modifier
                //                .fillMaxWidth()
                //                .height(8.dp)
                //        )
                //        DropdownMenuItem(
                //            text = { Text(text = "Photos&&videos") }, onClick = {
                //                view.playSoundEffect(SoundEffectConstants.CLICK)
                //            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                //                Icon(
                //                    painterResource(R.drawable.photo), contentDescription = null
                //                )
                //            }, trailingIcon = { }, enabled = true
                //        )
                //        DropdownMenuItem(
                //            text = { Text(text = "File") }, onClick = {
                //                view.playSoundEffect(SoundEffectConstants.CLICK)
                //            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                //                Icon(
                //                    painterResource(R.drawable.folder), contentDescription = null
                //                )
                //            }, trailingIcon = { }, enabled = true
                //        )
                //    }
                //}

                //AnimatedDropMenu(
                //    modifier = Modifier.padding(innerPadding),
                //    width = 400.dp,
                //    height = 420.dp,
                //    chord = 580.dp,
                //    isExpanded = isExpandedEmoji,
                //    close = { isExpandedEmoji = false },
                //    ratioX = 0.16f,
                //    ratioY = 0.98f,
                //    position = Alignment.BottomStart,
                //    hasBackgroundCover = true,
                //    whatIsMyBackgroundFilterColor = { color, show ->
                //        covered = show
                //        coverColor = color
                //    }) {
                //    Column {
                //        Spacer(
                //            modifier = Modifier
                //                .fillMaxWidth()
                //                .height(8.dp)
                //        )
                //        DropdownMenuItem(
                //            text = { Text(text = "Photos&&videos") }, onClick = {
                //                view.playSoundEffect(SoundEffectConstants.CLICK)
                //            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                //                Icon(
                //                    painterResource(R.drawable.photo), contentDescription = null
                //                )
                //            }, trailingIcon = { }, enabled = true
                //        )
                //        DropdownMenuItem(
                //            text = { Text(text = "File") }, onClick = {
                //                view.playSoundEffect(SoundEffectConstants.CLICK)
                //            }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                //                Icon(
                //                    painterResource(R.drawable.folder), contentDescription = null
                //                )
                //            }, trailingIcon = { }, enabled = true
                //        )
                //    }
                //}
            }
        }

        //if (covered) {
        //    Spacer(
        //        modifier = Modifier
        //            .fillMaxWidth()
        //            .height(
        //                64.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        //            )
        //            //top app bar size + notification bar size
        //            //.requiredHeight(64.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        //            .background(coverColor)
        //            .align(Alignment.TopCenter)
        //            .clickable(
        //                indication = null,
        //                interactionSource = remember { MutableInteractionSource() }) {
        //                if (isExpandedEmoji) {
        //                    isExpandedEmoji = false
        //                } else if (isExpandedAttachment) {
        //                    isExpandedAttachment = false
        //                }
        //            }
        //    )
        //}
    }
}

@Composable
fun Message(isMe: Boolean, message: String, seen: Boolean, timestamp: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        var lineCount by remember(message) { mutableIntStateOf(0) }
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
                color = if (isMe) Color(0xFFEFFEDD) else Color.White,
                shape = RoundedCornerShape(size = 2.dp),
                modifier = Modifier.widthIn(max = maxMessageWidth)
            ) {
                Box(
                    modifier = Modifier.padding(8.dp)
                ) {

                    Text(
                        text = message, modifier = Modifier.padding(
                            end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                            bottom = if (lineCount > 1) 24.dp else 0.dp
                        ), onTextLayout = {
                            if (lineCount == 0) {
                                lineCount = it.lineCount
                            }
                        })
                    Row(
                        modifier = Modifier.align(if (lineCount == 1) Alignment.CenterEnd else Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = FontStyle.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

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

//@Composable
//fun AdvancedDynamicLightEffectOptimized(
//    modifier: Modifier = Modifier, renderValue: Int = 3
//) {
//    val targetAngle1 = renderValue * 36f
//    val angle1 by animateFloatAsState(
//        targetValue = targetAngle1, animationSpec = tween(
//            durationMillis = 2000, easing = FastOutSlowInEasing
//        ), label = "angle1"
//    )
//
//    val targetAngle2 = targetAngle1 + 180f
//    val angle2 by animateFloatAsState(
//        targetValue = targetAngle2, animationSpec = tween(
//            durationMillis = 2000, easing = FastOutSlowInEasing
//        ), label = "angle2"
//    )
//
//    val sceneRotation by animateFloatAsState(
//        targetValue = 0f, animationSpec = tween(durationMillis = 1), label = "scene"
//    )
//
//    Canvas(
//        modifier = modifier.fillMaxSize()
//    ) {
//        val radius = hypot(size.width, size.height)
//        val center = Offset(size.width / 2f, size.height / 2f)
//        val orbitRadius = minOf(size.width, size.height) * 0.7f
//
//        val rad = Math.toRadians((angle1 + 90f).toDouble())
//
//        val dx = cos(rad).toFloat() * radius
//        val dy = sin(rad).toFloat() * radius
//
//        drawRect(
//            brush = Brush.linearGradient(
//                colors = listOf(
//                    Color(0xFF6BA587), Color(0xFF88B884)
//                ),
//                start = Offset(center.x - dx, center.y - dy),
//                end = Offset(center.x + dx, center.y + dy)
//            )
//        )
//
//        rotate(
//            degrees = sceneRotation, pivot = center
//        ) {
//            fun pointOnCircle(angleDegrees: Float): Offset {
//                val angleRad = Math.toRadians(angleDegrees.toDouble()).toFloat()
//                return Offset(
//                    x = center.x + orbitRadius * cos(angleRad),
//                    y = center.y + orbitRadius * sin(angleRad)
//                )
//            }
//
//            val lightRadius = radius * 0.7f
//
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color(0xFFd5d88d), Color.Transparent
//                    ), center = pointOnCircle(angle1), radius = lightRadius
//                ), radius = radius, center = center, blendMode = BlendMode.Screen
//            )
//
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color(0xFFdbddbb), Color.Transparent
//                    ), center = pointOnCircle(angle2), radius = lightRadius * 0.85f
//                ), radius = radius, center = center, blendMode = BlendMode.Screen
//            )
//        }
//    }
//}

// بهینه تر میشه نوشت؟؟؟
fun hash20(text: String): Int {
    return (text.hashCode() and Int.MAX_VALUE) % 19
}

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

    val materialColors = listOf(
        Color(0xFF607D8B),
        Color(0xFF9E9E9E),
        Color(0xFFFFEB3B),
        Color(0xFFCDDC39),
        Color(0xFF03A9F4),
        Color(0xFF673AB7),
        Color(0xFFFF5722),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF009688),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF795548),
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        Color(0xFF00BCD4),
        Color(0xFF3F51B5),
        Color(0xFFE91E63)
    )

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

                AnimatedDropMenu(
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
    var renderValue by remember { mutableIntStateOf(4) }
    var message by rememberSaveable { mutableStateOf(draft.toString()) }
    val view = LocalView.current
    val materialColors = listOf(
        Color(0xFF607D8B),
        Color(0xFF9E9E9E),
        Color(0xFFFFEB3B),
        Color(0xFFCDDC39),
        Color(0xFF03A9F4),
        Color(0xFF673AB7),
        Color(0xFFFF5722),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF009688),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF795548),
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        Color(0xFF00BCD4),
        Color(0xFF3F51B5),
        Color(0xFFE91E63)
    )
    val backgroundColor = materialColors[hash20(id)]
    val iconColor = if (backgroundColor.luminance() >= 0.5f) {
        Color.Black
    } else {
        Color.White
    }

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
                fps = 24,
                tails = 90,
                animate = true
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
                                    renderValue = (1..10).random()
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