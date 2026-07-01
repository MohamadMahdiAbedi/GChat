@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "SpellCheckingInspection")

package ir.gchat

//import androidx.compose.ui.graphics.MeshGradientPainter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ir.gchat.ui.theme.GChatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

const val RULES =
    "GNet and GNet Corp Platform Rules and Regulations\n\n1. Platform Principles\nEquality: Users have complete freedom of opinion, choice of audience, and membership in groups. The GNet platform is completely user-centered and all users are equal before the law, regardless of gender, religion, ethnicity, language, or other orientation.\nContent Responsibility: The publishing user is directly responsible for all published content. The platform acts as a technical intermediary and does not accept legal or criminal liability for user content (except in cases where the law expressly provides otherwise).\n\n2. Registration, Account Ownership, and Privacy\nAccount Ownership: Your account information belongs to you and you should not make it available to other parties. The platform is not responsible for any incidents resulting from the sharing of information by the user.\nAccount Deletion: If you request to delete your account, all information related to the user, including messages, contacts, files, groups, and channels created, will be completely removed from the user's access.\nImpersonation Prohibition: Impersonating others, especially celebrities, or members of the GNet team, is prohibited and will result in immediate account ban.\n\n3. Content Privacy Classification\nThe platform's intelligent system defines different levels of content access based on the type of interaction:\n1. Private messages: Only the parties involved in the conversation (2-person or private groups) can view the content.\n2. Private group/channel: Only members who have officially joined can view the content.\n3. Public channel: The content of these channels is visible on the GNet website and can even be viewed by non-members.\n4. Unauthorized content\nAny publication or promotion of the following is prohibited and will be subject to legal action:\n1. Crimes against persons:\nInsult, humiliation, insult and spam.\nHarassment, harassment and nuisance in personal messages, groups, comments, etc.\nThreats to life, encouragement of violence, suicide, self-harm or other harm.\nPublishing and revealing the private information of others (such as address, contact number, identification documents) without their consent.\nactivities against an individual or destruction campaigns.\n2. Immoral and sexual content:\nPornography is prohibited in all spaces (including cloud space) and will lead to expulsion from the platform.\nContent related to child abuse (CSAM) will lead to the strictest treatment.\n3. Fraud and illegal activities:\nPublishing online theft links, fraud and redirection to fake portals.\nBuying and selling drugs, alcoholic beverages, smuggling, gambling and betting.\nSelling memberships and buying/selling user accounts.\nPublishing fake news that leads to serious harm to individuals or real/legal entities.\n4. Technical security:\nDistribution of viruses, malware or any malicious code (unless explicitly published for educational purposes and with the necessary warnings).\n\n5. Special rules for managing channels, groups and rooms\nCreator's responsibility: The creator of each group, channel or room is fully responsible for monitoring the content and behavior of its members.\nRoom rules:\nThe admin or creator of the room is obliged to delete the offending content and remove the offending user from the room.\nRooms whose purpose is spam (multiple messages with low content value), fraudulent advertising, or promotion of prohibited content will be closed.\nChoosing offensive/immoral names or descriptions for the room is not allowed.\nChannel rules:\nStore channels: The GNet platform is only a technical intermediary and is not responsible for payment, shipping or quality of products.\nIn store products where the model is present in the image, the model must be dressed in a way that cannot be abused.\nDisagreements: Disagreements are natural, but users are required to handle disagreements respectfully and without insults or harassment.\n\n6. Mechanism for dealing with violations\nThe GNet platform has an intelligent spam and violation detection system that automatically checks and organizes content. If a violation is confirmed, the following measures will be applied in a stepwise manner (from mild to severe):\n\na) Personal account penalties:\n1. Official warning and removal of the offending content.\n2. Stopping contact list synchronization.\n3. Restriction on sending messages to non-contacts.\n4. Restriction on sending messages in groups that the user is not an administrator of.\n5. Complete restriction on sending messages on the platform.\n6. Restriction on creating new channels or groups.\n7. Restriction on adding members to groups/channels.\n8. Expulsion from the platform: In extreme cases, the user account will be deleted and all groups and channels created by that user will also be deleted.\n\nb) Group or channel penalties:\n1. Revocation of public membership links.\n2. Disabling the public search feature for the group/channel.\n3. Restricting new post posting.\n4. Blocking or completely deleting the group/channel.\n5. Removing the public channel from the platform lists.\n\n7. Reporting violations and contacting support\nUsers are required to report any violating content or inappropriate behavior. All reports will be carefully reviewed.\nTo send suggestions, criticism, bug reports or questions, you can contact us from the internal support section of the application or through the admin ID.\n\n8. Changes to the rules\nThe GNet platform has the right to change, modify or update its rules and regulations at any time and without prior notice.\nContinuing to use the platform services after any changes are made means full acceptance of the new rules by the user.\nMajor and critical changes will be notified to users via an in-app message or the official GNet notification channel.\nUsers are advised to periodically check the page Check the rules.\n\nGNet Corp | Always with you for a safe and fast space"

fun getIccidsFromSubscriptionManager(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }

    return try {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val activeList = subscriptionManager.activeSubscriptionInfoList

        if (activeList.isNullOrEmpty()) {
            return emptyList()
        }

        val maxSlotIndex = activeList.maxOfOrNull { it.simSlotIndex } ?: -1
        if (maxSlotIndex < 0) return emptyList()

        val iccids = MutableList(maxSlotIndex + 1) { "" }

        for (subscription in activeList) {
            try {
                val iccid = subscription.iccId
                val slotIndex = subscription.simSlotIndex

                if (!iccid.isNullOrEmpty() && slotIndex >= 0 && slotIndex < iccids.size) {
                    iccids[slotIndex] = iccid
                }
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            }
        }

        iccids
    } catch (e: SecurityException) {
        emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
//class IccidCollector(private val context: Context) {
//    fun getIccidsFromSubscriptionManager(): List<String> {
//        val iccids = mutableListOf("", "")
//        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//
//        try {
//            val activeList = subscriptionManager.activeSubscriptionInfoList
//            activeList?.forEach { subscription ->
//                try {
//                    val iccid = subscription.iccId
//                    val slotIndex = subscription.simSlotIndex
//
//                    if (!iccid.isNullOrEmpty() && slotIndex in 0..1) {
//                        iccids[slotIndex] = iccid
//                    }
//                } catch (e: SecurityException) {
//                }
//            }
//        } catch (e: SecurityException) {
//        }
//
//        return iccids
//    }
//}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data")

val ICCID_KEY = stringPreferencesKey("iccid")
val LOGINED_KEY = booleanPreferencesKey("logined")
val THEME_KEY = intPreferencesKey("theme")

class SocketViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val _logined = MutableStateFlow(
        //runBlocking { context.dataStore.data.map { it[LOGINED_KEY] ?: false }.first() }
        false
    )
    val logined: StateFlow<Boolean> = _logined.asStateFlow()

    private val _oldLogined = MutableStateFlow(
        runBlocking { context.dataStore.data.map { it[LOGINED_KEY] ?: false }.first() }
    )
    val oldLogined: StateFlow<Boolean> = _oldLogined.asStateFlow()

    fun connect() {

        if (webSocket != null) {
            return
        }

        val request = Request.Builder().url("ws://10.0.2.2:8765").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModelScope.launch {
                    try {
                        if (context.dataStore.data.map { it[LOGINED_KEY] ?: false }.first()) {
                            val savedIccid =
                                context.dataStore.data.map { it[ICCID_KEY] ?: "" }.first()
                            webSocket.send(
                                """
                                {
                                    "type": "login",
                                    "iccid": "$savedIccid"
                                }
                            """.trimIndent()
                            )
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch {
                    try {
                        val jsonObject = JSONObject(text)
                        val type = jsonObject.getString("type")
                        //val message = jsonObject.getString("message")

                        if (type == "login_recived") {
                            _logined.value = true
                            context.dataStore.edit { preferences ->
                                preferences[LOGINED_KEY] = true
                            }
                            _oldLogined.value = true
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@SocketViewModel.webSocket = null
                _logined.value = false
                viewModelScope.launch {
                    delay(1000)
                    connect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@SocketViewModel.webSocket = null
                _logined.value = false
                viewModelScope.launch {
                    delay(1000)
                    connect()
                }
            }
        })
    }

    fun login(iccid: String) {
        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[ICCID_KEY] = iccid
                }
                _oldLogined.value = true

                webSocket?.send(
                    """
                    {
                        "type": "login",
                        "iccid": "$iccid"
                    }
                    """.trimIndent()
                )
            } catch (e: Exception) {
            }
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _theme = MutableStateFlow(runBlocking {
        context.dataStore.data.map { it[THEME_KEY] ?: 0 }.first()
    }) // 0: System, 1: Dark, 2: Light
    val theme: StateFlow<Int> = _theme.asStateFlow()

    fun setTheme() {
        _theme.value = when (_theme.value) {
            0 -> 1 // System -> Dark
            1 -> 2 // Dark -> Light
            2 -> 0 // Light -> System
            else -> 0
        }

        _theme.value = _theme.value

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[THEME_KEY] = _theme.value
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    val socketViewModel: SocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socketViewModel.connect()

        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsState()
            val logined by socketViewModel.logined.collectAsState()
            val oldLogined by socketViewModel.oldLogined.collectAsState()
            val darkTheme = when (theme) {
                0 -> isSystemInDarkTheme()
                1 -> true
                2 -> false
                else -> isSystemInDarkTheme()
            }
            GChatTheme(
                dynamicColor = false, darkTheme = darkTheme
            ) {
                SetUPNavigationViewTitleBar()
                SetUPNotificationBar(darkTheme)
                SetUPNavigationBar(darkTheme)
                MainNavigation(
                    setTheme = { viewModel.setTheme() },
                    theme = theme,
                    login = { iccid -> socketViewModel.login(iccid = iccid) },
                    logined = logined,
                    oldLogined = oldLogined
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    setTheme: () -> Unit,
    theme: Int,
    login: (String) -> Unit,
    logined: Boolean,
    oldLogined: Boolean
) {
    val navController = rememberNavController()
    LaunchedEffect(logined) {
        if (oldLogined) {
            if (logined) {
                navController.navigate("mainScreen") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            } else {
                navController.navigate("wait") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        }
    }
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "greeting",

        enterTransition = {
            slideInVertically(
                initialOffsetY = { it }, animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutVertically(
                targetOffsetY = { -it }, animationSpec = tween(300)
            )
        },

        popEnterTransition = {
            slideInVertically(
                initialOffsetY = { -it }, animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it }, animationSpec = tween(300)
            )
        }) {
        composable("greeting") {
            Greeting(
                setTheme = setTheme,
                theme = theme,
                login = login
            )
        }
        composable("mainScreen") {
            MainScreen()
        }
        composable("chatScreen") {

        }
        composable("wait") {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Connecting...")
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
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
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "After connecting, you will be taken to the home page.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val drawerRatio = when (windowSizeClass.widthSizeClass) {
        Compact -> 0.8f
        Medium -> 0.5f
        else -> 0.3f
    }

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium)) }

    val view = LocalView.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(drawerRatio)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(192.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f)
                                    ),
                                    startY = 184.dp.toPx(),
                                    endY = 192.dp.toPx()
                                ),
                                blendMode = BlendMode.Multiply
                            )
                        }
                )
            }

        },
    ) {
        //Box
        Row(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(
                        if (expandedScreen) 0.3f else 1f
                    ),
                    //.shadow(
                    //    elevation = 16.dp,
                    //    clip = true
                    //),
                topBar = {
                    TopAppBar(
                        title = {
                            Text("GChat")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    scope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.menu),
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = "Search"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },

                ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                }
            }
            if (expandedScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        //.fillMaxWidth(0.7f)
                        .fillMaxWidth()
                        //.align(Alignment.CenterEnd)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    startX = 0.dp.toPx(),
                                    endX = 8.dp.toPx()
                                ),
                                blendMode = BlendMode.Multiply
                            )
                        }
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val view = LocalView.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        topBar = {
            TopAppBar(
                title = {
                    Text("Contact")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.menu_dots),
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    subtitleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },

        ) { innerPadding ->
        var renderValue by remember { mutableIntStateOf(5) }

        Column {
            AdvancedDynamicLightEffectOptim(
                renderValue = renderValue,
                modifier = Modifier.weight(1f)
            )
        }

        Button({renderValue = (1..10).random()}, modifier = Modifier.padding(innerPadding)) { }
    }
}

@Composable
fun AdvancedDynamicLightEffectOptim(
    renderValue: Int = 5, // مقدار بین 1 تا 10
    modifier: Modifier = Modifier
) {
    // انیمیشن برای تغییر زاویه اول بر اساس مقدار
    val targetAngle1 = renderValue * 36f // 1->36°, 10->360°
    val angle1 by animateFloatAsState(
        targetValue = targetAngle1,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "angle1"
    )

    // انیمیشن برای تغییر زاویه دوم با اختلاف 180 درجه
    val targetAngle2 = targetAngle1 + 180f
    val angle2 by animateFloatAsState(
        targetValue = targetAngle2,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "angle2"
    )

    // انیمیشن برای چرخش صحنه - این یکی می‌تونه ثابت بمونه یا حذف بشه
    val sceneRotation by animateFloatAsState(
        targetValue = 0f, // اگر نمی‌خواید بچرخه
        animationSpec = tween(durationMillis = 1),
        label = "scene"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val radius = hypot(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        val orbitRadius = minOf(size.width, size.height) * 0.7f

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF6DA687),
                    Color(0xFF89B885)
                ),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y)
            )
        )

        rotate(
            degrees = sceneRotation,
            pivot = center
        ) {
            fun pointOnCircle(angleDegrees: Float): Offset {
                val angleRad = Math.toRadians(angleDegrees.toDouble()).toFloat()
                return Offset(
                    x = center.x + orbitRadius * cos(angleRad),
                    y = center.y + orbitRadius * sin(angleRad)
                )
            }

            val lightRadius = radius * 0.7f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD1D68C),
                        Color.Transparent
                    ),
                    center = pointOnCircle(angle1),
                    radius = lightRadius
                ),
                radius = radius,
                center = center,
                blendMode = BlendMode.Screen
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD5DAB8),
                        Color.Transparent
                    ),
                    center = pointOnCircle(angle2),
                    radius = lightRadius * 0.85f
                ),
                radius = radius,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
}

// کامپوننت استفاده کننده
@Composable
fun RenderValueDemo() {
    var renderValue by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AdvancedDynamicLightEffectOptim(
            renderValue = renderValue,
            modifier = Modifier.weight(1f)
        )

        // دکمه‌های کنترل برای تست
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(10) { index ->
                val value = index + 1
                Button(
                    onClick = { renderValue = value },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (renderValue == value)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(value.toString())
                }
            }
        }
    }
}

//@Composable
//fun AdvancedDynamicLightEffectOptim() {
//
//    val infiniteTransition = rememberInfiniteTransition(label = "")
//
//    val sceneRotation by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(
//                durationMillis = 120_000,
//                easing = LinearEasing
//            )
//        ),
//        label = "scene"
//    )
//
//    val angle1 by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(60000, easing = LinearEasing)
//        ),
//        label = "angle1"
//    )
//
//    val angle2 by infiniteTransition.animateFloat(
//        initialValue = 180f,
//        targetValue = 540f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(60000, easing = LinearEasing)
//        ),
//        label = "angle2"
//    )
//
//    //val density = LocalDensity.current
//
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            //.padding(
//            //    PaddingValues(
//            //        top = with(density) {
//            //            WindowInsets.statusBars.getTop(density).toDp() + 64.dp
//            //        },
//            //        bottom = 0.dp,
//            //        start = 0.dp,
//            //        end = 0.dp
//            //    )
//            //)
//    ) {
//
//        val radius = hypot(size.width, size.height)
//        val center = Offset(size.width / 2f, size.height / 2f)
//
//        val orbitRadius = minOf(size.width, size.height) * 0.7f
//
//        drawRect(
//            brush = Brush.linearGradient(
//                colors = listOf(
//                    Color(0xFF6DA687),
//                    Color(0xFF89B885)
//                    //Color(0xFF0F1A14),
//                    //Color(0xFF1A2D22)
//                ),
//                start = Offset(center.x - radius, center.y),
//                end = Offset(center.x + radius, center.y)
//            )
//        )
//
//        rotate(
//            degrees = sceneRotation,
//            pivot = center
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
//                        Color(0xFFD1D68C),
//                        //Color(0xFF2D4035),
//                        Color.Transparent
//                    ),
//                    center = pointOnCircle(angle1),
//                    radius = lightRadius
//                ),
//                radius = radius,
//                center = center,
//                blendMode = BlendMode.Screen
//            )
//
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color(0xFFD5DAB8),
//                        //Color(0xFF3D5545),
//                        Color.Transparent
//                    ),
//                    center = pointOnCircle(angle2),
//                    radius = lightRadius * 0.85f
//                ),
//                radius = radius,
//                center = center,
//                blendMode = BlendMode.Screen
//            )
//        }
//    }
//}

@Composable
fun SetUPNavigationViewTitleBar() {
    val context = LocalContext.current
    val activity = context as? Activity

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    SideEffect {
        activity?.let { act ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val taskDescription =
                    ActivityManager.TaskDescription.Builder().setPrimaryColor(primaryColor).build()
                act.setTaskDescription(taskDescription)
            } else {
                val taskDescription = ActivityManager.TaskDescription(null, null, primaryColor)
                act.setTaskDescription(taskDescription)
            }
        }
    }
}

@Composable
fun SetUPNotificationBar(darkIcons: Boolean) {
    val view = LocalView.current
    val window = (view.context as ComponentActivity).window

    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons

    window.statusBarColor = Color(0x33000000).toArgb()
}

@Composable
fun SetUPNavigationBar(darkTheme: Boolean) {
    val view = LocalView.current
    val window = (view.context as ComponentActivity).window

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        DisposableEffect(Unit) {
            window.navigationBarColor = Color.Black.toArgb()
            onDispose { }
        }
    } else {
        DisposableEffect(Unit) {
            window.navigationBarColor = Color.Transparent.toArgb()
            onDispose { }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                insets
            }
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            !darkTheme
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Greeting(setTheme: () -> Unit, theme: Int, login: (String) -> Unit) {
    val context = LocalContext.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }

    var dropdownThemeText by remember { mutableStateOf("Theme: System") }
    var dropdownThemeIcon by remember { mutableIntStateOf(R.drawable.auto) }

    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val navController = rememberNavController()

    when (theme) {
        0 -> {
            dropdownThemeText = "Theme: System"
            dropdownThemeIcon = R.drawable.auto
        }

        1 -> {
            dropdownThemeText = "Theme: Dark"
            dropdownThemeIcon = R.drawable.dark_mode
        }

        2 -> {
            dropdownThemeText = "Theme: Light"
            dropdownThemeIcon = R.drawable.light_mode
        }

        else -> {
            dropdownThemeText = "Theme: System"
            dropdownThemeIcon = R.drawable.auto
        }
    }

    val text = buildAnnotatedString {
        append("Click to login means accepting the ")

        val link = LinkAnnotation.Clickable(
            tag = "terms",
            linkInteractionListener = { showTermsDialog = true },
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
        )

        withLink(link) {
            append("term of use and privacy.")
        }
    }

    var hasPhonePermission by remember {
        mutableStateOf(checkPhonePermission(context))
    }

    var hasSmsAppRole by remember {
        mutableStateOf(checkSmsAppRole(context))
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPhonePermission = granted

        if (granted) {
            Toast.makeText(
                context, "READ_PHONE_STATE granted.", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context, "READ_PHONE_STATE denied. Closing app.", Toast.LENGTH_LONG
            ).show()
            (context as? Activity)?.finishAffinity()
        }
    }

    val smsRoleLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            hasSmsAppRole = result.resultCode == Activity.RESULT_OK

            if (hasSmsAppRole) {
                Toast.makeText(
                    context, "SMS Role granted.", Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context, "SMS Role denied. Closing app.", Toast.LENGTH_LONG
                ).show()
                (context as? Activity)?.finishAffinity()
            }
        }
    } else null

    LaunchedEffect(hasPhonePermission, hasSmsAppRole) { // تغییر کلید
        // ابتدا Phone Permission
        if (!hasPhonePermission) {
            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            return@LaunchedEffect
        }

        // سپس SMS Role (فقط یک بار!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasSmsAppRole) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            smsRoleLauncher?.launch(
                roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            )
        }
    }

    var selectedIccid by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(10000)
            if (isLoading) {
                isLoading = false
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            if (windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) {
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "greeting",

                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },

                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(300)
                        )
                    }) {
                    composable("greeting") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = true
                                    )
                                    .background(color = MaterialTheme.colorScheme.surface)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "GChat is secure and optimized for some tasks.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                                Button(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("login") {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp),
                                    enabled = hasPhonePermission
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_forward),
                                        contentDescription = "Accept and Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 64.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = text,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    composable("login") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = true
                                    )
                                    .background(color = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    VerifySimCard(
                                        selectedIccid = selectedIccid,
                                        setSelectedIccid = { value -> selectedIccid = value },
                                        endPadding = 0.dp
                                    )
                                }

                                Button(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        isLoading = true
                                        login(selectedIccid.toString())
                                    },
                                    enabled = !isLoading and !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(8.dp)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                if (isLoading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(1 / 3f)
                            .padding(innerPadding)
                    ) {
                        IconButton(
                            modifier = Modifier.align(Alignment.TopEnd), onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                expanded = true
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu_dots),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            DropdownMenu(
                                modifier = Modifier.width(200.dp),
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = {
                                    Text(
                                        text = dropdownThemeText
                                    )
                                }, leadingIcon = {
                                    Icon(
                                        painter = painterResource(
                                            id = dropdownThemeIcon
                                        ), contentDescription = "Theme"
                                    )
                                }, onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    setTheme()
                                })
                                DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.support),
                                        contentDescription = "Support"
                                    )
                                }, onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    expanded = false
                                    showSupportDialog = true
                                })
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GChat", style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFDFE4DD)
                            )
                        )
                    }
                }
            } else {
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "greeting",

                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(300)
                        )
                    }) {
                    composable("greeting") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = true
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    DropdownMenu(
                                        modifier = Modifier.width(200.dp),
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                    }
                                }

                                Text(
                                    text = "GChat is secure and optimized for some tasks.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                                Button(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("login") {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp),
                                    enabled = hasPhonePermission
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_forward),
                                        contentDescription = "Accept and Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 64.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = text,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    composable("login") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = true
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
//                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .align(Alignment.Center),
                                ) {
                                    VerifySimCard(
                                        selectedIccid = selectedIccid,
                                        setSelectedIccid = { value -> selectedIccid = value },
                                        endPadding = 56.dp
                                    )
                                }

                                if (isLoading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp), onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onTertiary,
                                    )
                                }
                                Button(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        isLoading = true
                                        login(selectedIccid.toString())
                                    },
                                    enabled = !isLoading and !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    DropdownMenu(
                                        modifier = Modifier.width(200.dp),
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    Box(modifier = Modifier.fillMaxHeight(fraction = 1 / 3f)) { }
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GChat", style = MaterialTheme.typography.displaySmall.copy(
                                color = Color(0xFFDFE4DD)
                            )
                        )
                    }
                }
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Support") },
            text = {
                Text(text = "You can send an SMS to support by clicking the button below.")
            },
            shape = RoundedCornerShape(4.dp),
            confirmButton = {
                Button(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        val phoneNumber = "09369152046"
                        val uri = "sms:$phoneNumber".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }, shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sms), contentDescription = null
                    )
                    Text("Send SMS")
                }
            },
            dismissButton = {
                TextButton(
                    shape = RoundedCornerShape(4.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSupportDialog = false
                    }) {
                    Text("Dismiss")
                }
            })
    }

    if (showTermsDialog) {
        AlertDialog(
            modifier = Modifier.fillMaxHeight(0.8f),
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Term of use and privacy") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = RULES)
                }
            },
            shape = RoundedCornerShape(4.dp),
            confirmButton = {
                TextButton(
                    shape = RoundedCornerShape(4.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showTermsDialog = false
                    }) {
                    Text("Dismiss")
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySimCard(
    selectedIccid: String?, setSelectedIccid: (String) -> Unit, endPadding: Dp
) {

    val context = LocalContext.current
    val view = LocalView.current

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        //val iccidCollector = remember(context) { IccidCollector(context) }
        val iccids = remember { mutableStateOf(getIccidsFromSubscriptionManager(context = context)) }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ), elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ), shape = RoundedCornerShape(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sim_toolkit),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = "The ICCID is the unique identifier of your SIM card. Use it to verify your identity.",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = endPadding)
                    )
                }
            }

            iccids.value.forEachIndexed { index, iccid ->
                Card(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = if (index != 0) 0.dp else 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                        .fillMaxWidth(), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ), elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ), shape = RoundedCornerShape(4.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        setSelectedIccid(iccid)
                    }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIccid == iccid,
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                setSelectedIccid(iccid)
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Column {
                            Text(
                                text = "Slot ${index + 1}",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ICCID: $iccid",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    } else {
        var iccidList by remember { mutableStateOf(listOf<String>()) }

        iccidList = getICCIDList(context)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ), elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ), shape = RoundedCornerShape(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sim_toolkit),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = "The ICCID is the unique identifier of your SIM card. Use it to verify your identity.",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = endPadding)
                    )
                }
            }

            iccidList.forEachIndexed { index, iccid ->
                Card(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = if (index != 0) 0.dp else 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                        .fillMaxWidth(), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ), elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ), shape = RoundedCornerShape(4.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        setSelectedIccid(iccid)
                    }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIccid == iccid,
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                setSelectedIccid(iccid)
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Column {
                            Text(
                                text = "Slot ${index + 1}",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ICCID: $iccid",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

class SmsReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
    }
}

class MmsReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
    }
}

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}

fun getICCIDList(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }

    return try {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        if (subscriptionInfoList.isNullOrEmpty()) {
            return emptyList()
        }

        val maxSlotIndex = subscriptionInfoList.maxOfOrNull { it.simSlotIndex } ?: -1
        if (maxSlotIndex < 0) return emptyList()

        val iccids = MutableList(maxSlotIndex + 1) { "" }

        for (subscriptionInfo in subscriptionInfoList) {
            val iccid = subscriptionInfo.iccId ?: ""
            val slotIndex = subscriptionInfo.simSlotIndex

            if (slotIndex in iccids.indices) {
                iccids[slotIndex] = iccid
            }
        }

        iccids
    } catch (e: SecurityException) {
        emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun checkPhonePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkSmsAppRole(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_SMS)
    } else {
        true // قبل از Android 10 نیازی به Role نیست
    }
}