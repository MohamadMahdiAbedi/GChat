package ir.gchat

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.LocaleListCompat
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
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels()
    val socketViewModel: SocketViewModel by viewModels()
    private lateinit var languageContext: Context

    override fun attachBaseContext(newBase: Context) {
        val language = newBase
            .getSharedPreferences("app", MODE_PRIVATE)
            .getString("language", null)

        languageContext = if (language != null) {
            newBase.withLanguage(language)
        } else {
            newBase
        }

        super.attachBaseContext(languageContext)
    }

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
                    val dynamicColor by viewModel.useDynamicColor.collectAsState()
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
                        dynamicColor = dynamicColor, darkTheme = darkTheme, paletteIndex = palette
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
                                    name, size, hash, uri
                                )
                            },
                            draft = socketViewModel.drafts.collectAsState().value,
                            savedText = socketViewModel.savedText.collectAsState().value,
                            setSavedText = { id, message, editingId, markDown ->
                                socketViewModel.setSavedText(
                                    id, message, editingId, markDown
                                )
                            },
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
                            removeTextFromDraft = { id ->
                                socketViewModel.removeTextFromDraft(
                                    id
                                )
                            },
                            clearDraft = { socketViewModel.clearDraft() },
                            setNavBarTheme = { mode -> viewModel.setNavBarTheme(mode) },
                            seenAll = { id -> socketViewModel.seenAll(id) },
                            token = socketViewModel.token.collectAsState().value,
                            isFileDownloaded = { fileName ->
                                socketViewModel.isFileDownloaded(
                                    fileName
                                )
                            },
                            attachTextBlock = { text -> socketViewModel.attachTextBlock(text) },
                            sendWith = viewModel.sendWith.collectAsState().value,
                            setSendWith = { keys -> viewModel.setSendWith(keys) },
                            useDynamicColor = dynamicColor,
                            setUseDynamicColor = { useDynamicColor ->
                                viewModel.setUseDynamicColor(
                                    useDynamicColor
                                )
                            },
                            editTextInDraft = { id, text ->
                                socketViewModel.editTextInDraft(
                                    id = id, newText = text
                                )
                            },
                            deleteMessage = { id ->
                                socketViewModel.deleteMessage(id)
                            },
                            attachLaTeX = { text ->
                                socketViewModel.attachLaTeX(text = text)
                            },
                            removeLaTeXFromDraft = { id ->
                                socketViewModel.removeLaTeXFromDraft(id = id)
                            },
                            editLaTeXInDraft = { id, text ->
                                socketViewModel.editLaTeXInDraft(id = id, newText = text)
                            },
                            attachFileWithId = { id, fileName, fileSize ->
                                socketViewModel.attachFileWithId(
                                    id = id,
                                    fileName = fileName,
                                    fileSize = fileSize
                                )
                            },
                            editMessage = { id, message ->
                                socketViewModel.editMessage(
                                    id = id, message = message
                                )
                            },
                            gradientSettings = viewModel.gradientSettings.collectAsState().value
                        )
                        SetUpSystemBars(lightNavBar = viewModel.lightNavBar.collectAsState().value)
                    }
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                }
            }
        }

        socketViewModel.connect()
    }
}

fun Context.withLanguage(language: String): Context {
    val locale = Locale.forLanguageTag(language)

    val configuration = Configuration(resources.configuration)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.setLocale(locale)
    } else {
        @Suppress("DEPRECATION")
        configuration.locale = locale
    }

    return createConfigurationContext(configuration)
}

fun setAppLanguage(language: String) {
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(language)
    )
}

@Composable
fun SetUpSystemBars(lightNavBar: Boolean?) {
    val colorLuminance = MaterialTheme.colorScheme.primary.luminance()
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

            controller.isAppearanceLightNavigationBars = lightNavBar ?: !darkIcons
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
    savedText: Map<String, Triple<Int?, String, List<Triple<Int, Int, String>>>>,
    setSavedText: (String, String, Int?, List<Triple<Int, Int, String>>) -> Unit,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    loginResponse: Boolean,
    removeFileFromDraft: (String) -> Unit,
    removeTextFromDraft: (Int) -> Unit,
    clearDraft: () -> Unit,
    setNavBarTheme: (Boolean?) -> Unit,
    seenAll: (String) -> Unit,
    token: String,
    isFileDownloaded: (String) -> Boolean,
    attachTextBlock: (String) -> Unit,
    sendWith: SendMessageWith,
    setSendWith: (SendMessageWith) -> Unit,
    useDynamicColor: Boolean,
    setUseDynamicColor: (Boolean) -> Unit,
    editTextInDraft: (Int, String) -> Unit,
    deleteMessage: (Int) -> Unit,
    attachLaTeX: (String) -> Unit,
    removeLaTeXFromDraft: (Int) -> Unit,
    editLaTeXInDraft: (Int, String) -> Unit,
    attachFileWithId: (Int, String, Long) -> Unit,
    editMessage: (Int, List<Content>) -> Unit,
    gradientSettings: GradientSettings?
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    var selectedAudio by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }

    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    val mediaPlayer = remember { MediaPlayer() }

    LaunchedEffect(selectedAudio) {
        selectedAudio?.let { file ->
            mediaPlayer.reset()

            mediaPlayer.setDataSource(file.absolutePath)

            mediaPlayer.setOnPreparedListener {
                duration = it.duration
                currentPosition = 0

                it.start()
                isPlaying = true
            }

            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                currentPosition = duration
            }

            mediaPlayer.prepareAsync()
        }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking) {
            currentPosition = mediaPlayer.currentPosition
            delay(100.milliseconds)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    val primaryLuminance = MaterialTheme.colorScheme.primary.luminance()
    LaunchedEffect(currentRoute, primaryLuminance) {
        if (currentRoute.toString().startsWith("chatScreen") && selectedAudio != null) {
            setNavBarTheme(primaryLuminance > 0.5f)
        } else {
            setNavBarTheme(null)
        }
    }

    LaunchedEffect(loggedIn, oldLoggedIn) {
        if (oldLoggedIn) {
            if (loggedIn) {
                //if (currentRoute != "mainScreen" && !currentRoute.toString()
                //        .startsWith("chatScreen")
                //) {
                //    navController.navigate("mainScreen") {
                //        popUpTo(0) { inclusive = true }
                //    }
                //}
                when (currentRoute) {
                    "wait" -> {
                        navController.popBackStack()
                    }

                    "greeting" -> {
                        navController.navigate("mainScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    "ipConfig" -> {
                        if (navController.previousBackStackEntry?.destination?.route == "wait") {
                            navController.popBackStack("mainScreen", inclusive = false)
                        } else {
                            navController.popBackStack()
                        }
                    }
                }
            } else {
                //navController.navigate("wait") {
                //    popUpTo(0) { inclusive = true }
                //}
                navController.navigate("wait")
            }
        } else {
            navController.navigate("greeting") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val context = LocalContext.current

    val imageLoader = remember(context, token) {
        ImageLoader.Builder(context).okHttpClient {
            OkHttpClient.Builder().addInterceptor { chain ->
                val original = chain.request()

                val request =
                    original.newBuilder().addHeader("Authorization", "Bearer $token").build()

                chain.proceed(request)
            }.build()
        }.build()
    }

    val bottomPadding by animateDpAsState(
        targetValue = if (selectedAudio != null) 64.dp else 0.dp, animationSpec = tween(
            durationMillis = 300, easing = FastOutSlowInEasing
        ), label = "bottomPadding"
    )

    val playBarHeight by animateDpAsState(
        targetValue = if (selectedAudio != null) 64.dp + WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding() else 0.dp, animationSpec = tween(
            durationMillis = 300, easing = FastOutSlowInEasing
        ), label = "bottomPadding"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = bottomPadding),
            navController = navController,
            startDestination = "mainScreen",
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
                var showIpConfigDialog by rememberSaveable { mutableStateOf(false) }
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
                        ipConfig = { showIpConfigDialog = true },
                        getRules = getRules,
                        loginResponse = loginResponse
                    )
                    if (showIpConfigDialog) {
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        //    IpConfigDialogMaterialYou(
                        //        serverIP = serverIP,
                        //        device = device,
                        //        back = { showIpConfigDialog = false },
                        //        setDevice = setDevice,
                        //        setServerIP = setServerIP
                        //    )
                        //} else {
                        val layoutDirection = if (context.isRtl()) {
                            LayoutDirection.Rtl
                        } else {
                            LayoutDirection.Ltr
                        }
                        CompositionLocalProvider(
                            LocalLayoutDirection provides layoutDirection
                        ) {
                            IpConfigDialog(
                                serverIP = serverIP,
                                device = device,
                                back = { showIpConfigDialog = false },
                                setDevice = setDevice,
                                setServerIP = setServerIP
                            )
                        }
                        //}
                    }
                }
            }
            composable(route = "wait") {
                var showIpConfigDialog by rememberSaveable { mutableStateOf(false) }
                BackHandler { }
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.connecting),
                                    //modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        textDirection = TextDirection.Content
                                    )
                                )
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
                                        //view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showIpConfigDialog = true
                                    }) {
                                    Icon(
                                        painter = painterResource(R.drawable.tune),
                                        //contentDescription = "IP config"
                                        contentDescription = null
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
                                        indeterminateTintList =
                                            ColorStateList.valueOf(progressColor)
                                    }
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.after_connecting_you_will_be_taken_to_the_home_page),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    textDirection = TextDirection.Content
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                if (showIpConfigDialog) {
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    //    IpConfigDialogMaterialYou(
                    //        serverIP = serverIP,
                    //        device = device,
                    //        back = { showIpConfigDialog = false },
                    //        setDevice = setDevice,
                    //        setServerIP = setServerIP
                    //    )
                    //} else {
                    val layoutDirection = if (context.isRtl()) {
                        LayoutDirection.Rtl
                    } else {
                        LayoutDirection.Ltr
                    }
                    CompositionLocalProvider(
                        LocalLayoutDirection provides layoutDirection
                    ) {
                        IpConfigDialog(
                            serverIP = serverIP,
                            device = device,
                            back = { showIpConfigDialog = false },
                            setDevice = setDevice,
                            setServerIP = setServerIP
                        )
                    }
                    //}
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
                    removeTextFromDraft = removeTextFromDraft,
                    clearDraft = clearDraft,
                    seenAll = seenAll,
                    serverUrl = serverIP,
                    imageLoader = imageLoader,
                    isFileDownloaded = isFileDownloaded,
                    attachTextBlock = attachTextBlock,
                    sendWith = sendWith,
                    editTextInDraft = editTextInDraft,
                    deleteMessage = deleteMessage,
                    playSet = { value -> selectedAudio = value },
                    attachLaTeX = attachLaTeX,
                    removeLaTeXFromDraft = removeLaTeXFromDraft,
                    editLaTeXInDraft = editLaTeXInDraft,
                    attachFileWithId = attachFileWithId,
                    editMessage = editMessage
                )
            }
            composable(route = "appearanceSettings") {
                AppearanceSettingsScreen(
                    setTheme = setTheme,
                    theme = theme,
                    navHostController = navController,
                    setColor = setColor,
                    paletteIndex = paletteIndex,
                    useDynamicColor = useDynamicColor,
                    setUseDynamicColor = setUseDynamicColor
                )
            }
            composable(route = "sendBoxKeysSettings") {
                SendBoxKeysSettingsScreen(
                    navHostController = navController,
                    sendWith = sendWith,
                    setSendWith = setSendWith
                )
            }
            composable(route = "contentAnalysisSettings") {
                ContentAnalysisScreen(
                    navHostController = navController
                )
            }
            composable(route = "settings") {
                SettingsScreen(
                    navHostController = navController,
                    setTheme = setTheme,
                    theme = theme,
                    setServerIP = setServerIP,
                    serverIP = serverIP,
                    setDevice = setDevice,
                    device = device,
                    setColor = setColor,
                    paletteIndex = paletteIndex,
                    sendWith = sendWith,
                    setSendWith = setSendWith,
                    useDynamicColor = useDynamicColor,
                    setUseDynamicColor = setUseDynamicColor,
                    gradientSettings
                )
            }
            composable(
                route = "chatScreen?id={id}&displayName={displayName}&selectedChatUnreadCount={selectedChatUnreadCount}",
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }, navArgument("displayName") {
                    type = NavType.StringType
                    defaultValue = ""
                }, navArgument("selectedChatUnreadCount") {
                    type = NavType.IntType
                    defaultValue = 0
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
                    draft = draft,
                    savedText = savedText,
                    setSavedText = setSavedText,
                    downloadFile = downloadFile,
                    removeFileFromDraft = removeFileFromDraft,
                    removeTextFromDraft = removeTextFromDraft,
                    clearDraft = clearDraft,
                    seenAll = seenAll,
                    serverUrl = serverIP,
                    imageLoader = imageLoader,
                    isFileDownloaded = isFileDownloaded,
                    attachTextBlock = attachTextBlock,
                    sendWith = sendWith,
                    editTextInDraft = editTextInDraft,
                    deleteMessage = deleteMessage,
                    playSet = { value -> selectedAudio = value },
                    attachLaTeX = attachLaTeX,
                    removeLaTeXFromDraft = removeLaTeXFromDraft,
                    editLaTeXInDraft = editLaTeXInDraft,
                    attachFileWithId = attachFileWithId,
                    editMessage = editMessage
                )
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
            composable(route = "setLanguage") {
                Language(
                    back = { navController.popBackStack() }
                )
            }
            composable(route = "chatSettingsScreen") {
                ChatSettingsScreen(
                    navHostController = navController,
                    gradientSettings = gradientSettings
                )
            }
        }

        val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

        Box(
            modifier = Modifier
                .imePadding()
                .align(Alignment.BottomCenter)
                .shadow(elevation = 4.dp, clip = false)
                .background(color = MaterialTheme.colorScheme.surface)
                .height(if (isKeyboardOpen) bottomPadding else playBarHeight)
                .fillMaxWidth()
        ) {
            if (selectedAudio != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer.start()
                                isPlaying = true
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            painter = if (isPlaying) {
                                painterResource(R.drawable.pause_circle)
                            } else {
                                painterResource(R.drawable.play_circle)
                            }, contentDescription = null
                        )
                    }
//
//                Slider(
//                    value = if (duration > 0) {
//                    currentPosition.toFloat() / duration
//                } else {
//                    0f
//                }, onValueChange = { value ->
//                    currentPosition = (value * duration).toInt()
//                }, onValueChangeFinished = {
//                    mediaPlayer.seekTo(currentPosition)
//                }, modifier = Modifier.weight(1f)
//                )

                    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
                    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

                    AndroidView(
                        factory = { context ->
                            SeekBar(context).apply {
                                max = 1000

                                progressTintList =
                                    ColorStateList.valueOf(primaryColor)

                                progressBackgroundTintList =
                                    ColorStateList.valueOf(trackColor)

                                thumbTintList =
                                    ColorStateList.valueOf(primaryColor)

                                setOnSeekBarChangeListener(
                                    object : SeekBar.OnSeekBarChangeListener {

                                        override fun onStartTrackingTouch(
                                            seekBar: SeekBar
                                        ) {
                                            isSeeking = true
                                        }

                                        override fun onProgressChanged(
                                            seekBar: SeekBar,
                                            progress: Int,
                                            fromUser: Boolean
                                        ) {
                                            if (fromUser && duration > 0) {
                                                currentPosition =
                                                    (progress / 1000f * duration).toInt()
                                            }
                                        }

                                        override fun onStopTrackingTouch(
                                            seekBar: SeekBar
                                        ) {
                                            val position = currentPosition

                                            mediaPlayer.seekTo(position)

                                            isSeeking = false
                                        }
                                    }
                                )
                            }
                        },

                        update = { seekBar ->

                            seekBar.progressTintList =
                                ColorStateList.valueOf(primaryColor)

                            seekBar.progressBackgroundTintList =
                                ColorStateList.valueOf(trackColor)

                            seekBar.thumbTintList =
                                ColorStateList.valueOf(primaryColor)

                            // هنگام Drag مقدار SeekBar را از بیرون تغییر نده
                            if (!isSeeking) {
                                seekBar.progress =
                                    if (duration > 0) {
                                        (currentPosition * 1000f / duration)
                                            .toInt()
                                    } else {
                                        0
                                    }
                            }
                        },

                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${formatTime(currentPosition)}\n${formatTime(duration)}",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.stop()
                            }

                            mediaPlayer.reset()

                            selectedAudio = null
                            isPlaying = false
                            isSeeking = false
                            currentPosition = 0
                            duration = 0
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
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

fun findLinks(text: String): List<Triple<Int, Int, String>> {
    val spannable = SpannableString(text)

    Linkify.addLinks(
        spannable,
        Linkify.WEB_URLS
    )

    return spannable
        .getSpans(0, spannable.length, URLSpan::class.java)
        .mapNotNull { span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)

            if (start in 0..<end) {
                Triple(start, end, "l${span.url}")
            } else {
                null
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

fun hash19(text: String): Int {
    return (text.hashCode() and Int.MAX_VALUE) % 18
}