@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "SpellCheckingInspection")

package ir.gchat

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
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.view.Gravity
import android.view.SoundEffectConstants
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

fun getIccidsFromSubscriptionManager(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
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
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }

        iccids
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Exception) {
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
val SERVERIP_KEY = stringPreferencesKey("serverIP")
val DEVICE_TYPE_KEY = intPreferencesKey("deviceTypeIP")
val USERNAME_KEY = stringPreferencesKey("username")
val PASSWORD_KEY = stringPreferencesKey("password")

data class Person(
    val id: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val lastMessageText: String = "",
    val lastMessageDate: String = "",
    val unreadMessages: Int = 0,
    val conectionStatus: Boolean = false,
)

data class SearchEntity(
    val username: String,
    val isOnline: Boolean
)

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
        //false
        runBlocking { context.dataStore.data.map { it[LOGINED_KEY] ?: false }.first() }
    )
    val oldLogined: StateFlow<Boolean> = _oldLogined.asStateFlow()

    val savedUsername = runBlocking {
        context.dataStore.data.map { it[USERNAME_KEY] ?: "" }.first()
    }
    val savedPassword =
        runBlocking { context.dataStore.data.map { it[PASSWORD_KEY] ?: "" }.first() }

    private val _serverIP = MutableStateFlow(
        //"127.0.0.1:8765"
        runBlocking { context.dataStore.data.map { it[SERVERIP_KEY] ?: "127.0.0.1:8765" }.first() }
    )
    val serverIP: StateFlow<String> = _serverIP.asStateFlow()

    //init {
    //viewModelScope.launch {
    //context.dataStore.data.collect { preferences ->
    //_serverIP.value = preferences[SERVERIP_KEY] ?: "127.0.0.1:8765"
    //_oldLogined.value = preferences[LOGINED_KEY] ?: false
    //}
    //}
    //}

    //private val _device = MutableStateFlow(
    //    runBlocking { context.dataStore.data.map { it[DEVICE_TYPE_KEY] ?: 2 }.first() })
    //val device: StateFlow<Int> = _device.asStateFlow()

    // 0 -> success
    // 1 -> iccid alreasy exist
    // 2 -> username alreasy exist
    // 3 -> password is incorrect
    // 4 -> untitled error
    // 5 -> username not found
    // 6 -> invalid input(limit error)
    private val _loginError = MutableStateFlow(0)
    val loginError: StateFlow<Int> = _loginError.asStateFlow()

    private val _chatList = MutableStateFlow(
        listOf(
            Person(
                name = "عباس عراقچی",
                id = "test1",
                lastMessageDate = "12:20",
                unreadMessages = 999,
                lastMessageText = "جایگزین کردیم"
            ), Person(
                name = "امام خمینی",
                id = "test2",
                lastMessageDate = "1360",
                unreadMessages = 14,
                lastMessageText = "خیلی خری"
            ), Person(
                name = "استاد قنبری",
                id = "test3",
                lastMessageDate = "1m",
                unreadMessages = 0,
                lastMessageText = "بله خبر دارم."
            )
        )
    )
    val chatList: StateFlow<List<Person>> = _chatList.asStateFlow()

    private val _contactSearchList = MutableStateFlow(emptyList<SearchEntity>())
    val contactSearchList: StateFlow<List<SearchEntity>> = _contactSearchList.asStateFlow()

    fun connect() {

        if (webSocket != null) {
            return
        }

        val request = Request.Builder().url("ws://${_serverIP.value}").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModelScope.launch {
                    try {
                        if (_oldLogined.value/*context.dataStore.data.map { it[LOGINED_KEY] ?: false }.first()*/) {
                            webSocket.send(
                                """
                                {
                                    "type": "signin",
                                    "username": "$savedUsername",
                                    "password": "$savedPassword"
                                }
                            """.trimIndent()
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch {
                    try {
                        val jsonObject = JSONObject(text)
                        val type = jsonObject.getString("type")
                        println(jsonObject)
                        //val message = jsonObject.getString("message")

                        when (type) {
                            "signup_response" -> {
                                val message = jsonObject.getString("status")
                                when (message) {
                                    "success" -> {
                                        _loginError.value = 0
                                        _oldLogined.value = true
                                        _logined.value = true
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = true
                                        }
                                    }

                                    "iccid_error" -> {
                                        _loginError.value = 1
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "username_error" -> {
                                        _loginError.value = 2
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "error" -> {
                                        _loginError.value = 4
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "invalid_input" -> {
                                        _loginError.value = 6
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }
                                }
                            }

                            "signin_response" -> {
                                val message = jsonObject.getString("status")
                                when (message) {
                                    "success" -> {
                                        _loginError.value = 0
                                        _oldLogined.value = true
                                        _logined.value = true
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = true
                                        }
                                    }

                                    "password_error" -> {
                                        _loginError.value = 3
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "error" -> {
                                        _loginError.value = 4
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "username_error" -> {
                                        _loginError.value = 5
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }

                                    "invalid_input" -> {
                                        _loginError.value = 6
                                        _oldLogined.value = false
                                        _logined.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGINED_KEY] = false
                                        }
                                    }
                                }
                            }

                            "search_user_response" -> {
                                _contactSearchList.value = emptyList<SearchEntity>()
                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("results")
                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)
                                        _contactSearchList.value += SearchEntity(
                                            username = item.getString("username"),
                                            isOnline = item.getBoolean("online"),
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Search error", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    } catch (_: Exception) {
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

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[USERNAME_KEY] = username
                }

                context.dataStore.edit { preferences ->
                    preferences[PASSWORD_KEY] = password
                }

                webSocket?.send(
                    """
                    {
                        "type": "signin",
                        "username": "$username",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun signUp(iccid: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[ICCID_KEY] = iccid
                }

                context.dataStore.edit { preferences ->
                    preferences[USERNAME_KEY] = username
                }

                context.dataStore.edit { preferences ->
                    preferences[PASSWORD_KEY] = password
                }

                webSocket?.send(
                    """
                    {
                        "type": "signup",
                        "iccid": "$iccid",
                        "username": "$username",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun setServerIP(serverIP: String) {
        _serverIP.value = serverIP
        this@SocketViewModel.webSocket = null
        _logined.value = false
        connect()

        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[SERVERIP_KEY] = serverIP
                }
            } catch (_: Exception) {
            }
        }
    }

    fun searchUsername(username: String) {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    """
                    {
                        "type": "search_user",
                        "username": "$username"
                    }
                    """.trimIndent()
                )
            } catch (_: Exception) {

            }
        }
    }

    fun clearSearchMemory() {
        _contactSearchList.value = emptyList<SearchEntity>()
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

//    private val _theme = MutableStateFlow(runBlocking {
//        context.dataStore.data.map { it[THEME_KEY] ?: 0 }.first()
//    }) // 0: System, 1: Dark, 2: Light
//    val theme: StateFlow<Int> = _theme.asStateFlow()

    private val _theme = MutableStateFlow(0)
    val theme: StateFlow<Int> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            _theme.value = context.dataStore.data
                .map { it[THEME_KEY] ?: 0 }
                .first()
        }
    }

    fun setTheme() {
        _theme.value = when (_theme.value) {
            0 -> 1 // System -> Dark
            1 -> 2 // Dark -> Light
            2 -> 0 // Light -> System
            else -> 0
        }

        //_theme.value = _theme.value

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[THEME_KEY] = _theme.value
            }
        }
    }

    suspend fun getDevice(): Int {
        return context.dataStore.data
            .map { it[DEVICE_TYPE_KEY] ?: 2 }
            .first()
    }

    fun setDevice(deviceType: Int) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[DEVICE_TYPE_KEY] = deviceType
            }
        }
    }

    fun getRules(): String {
        return context.resources.openRawResource(R.raw.rules).bufferedReader().use { it.readText() }
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
            val chatList by socketViewModel.chatList.collectAsState()
            val contactsSearchList by socketViewModel.contactSearchList.collectAsState()
            GChatTheme(
                dynamicColor = false, darkTheme = darkTheme
            ) {
                SetUPNavigationViewTitleBar()
                SetUPNotificationBar(darkTheme)
                SetUPNavigationBar(darkTheme)
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
                    logined = logined,
                    oldLogined = oldLogined,
                    chatList = chatList,
                    setServerIP = { serverIP -> socketViewModel.setServerIP(serverIP = serverIP) },
                    serverIP = socketViewModel.serverIP.collectAsState().value,
                    device = { viewModel.getDevice() },
                    setDevice = { device -> viewModel.setDevice(deviceType = device) },
                    loginError = socketViewModel.loginError.collectAsState().value,
                    getrules = { viewModel.getRules() },
                    searchContactList = contactsSearchList,
                    searchContact = { username -> socketViewModel.searchUsername(username = username) },
                    clearSearchList = { socketViewModel.clearSearchMemory() }
                )
            }
        }
    }
}

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
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                insets
            }
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            !darkTheme
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    setTheme: () -> Unit,
    theme: Int,
    signIn: (String, String) -> Unit,
    signUp: (String, String, String) -> Unit,
    logined: Boolean,
    oldLogined: Boolean,
    chatList: List<Person>,
    setServerIP: (String) -> Unit,
    serverIP: String,
    setDevice: (Int) -> Unit,
    device: suspend () -> Int,
    loginError: Int,
    getrules: () -> String,
    searchContactList: List<SearchEntity>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(loginError) {
        when (loginError) {
            1 -> Toast.makeText(context, "ICCID already exists", Toast.LENGTH_SHORT).show()
            2 -> Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
            3 -> Toast.makeText(context, "Password is incorrect", Toast.LENGTH_SHORT).show()
            4 -> Toast.makeText(context, "Untitled error", Toast.LENGTH_SHORT).show()
            5 -> Toast.makeText(context, "Username not found", Toast.LENGTH_SHORT).show()
            6 -> Toast.makeText(context, "Invalid input size", Toast.LENGTH_SHORT).show()
        }
    }

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
        } else {
            navController.navigate("greeting") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
    }
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
        composable(route = "greeting") {
            Box(modifier = Modifier.fillMaxSize()) {
                Greeting(setTheme = setTheme, theme = theme, signIn = { username, password ->
                    signIn(
                        username, password
                    )
                }, signUp = { iccid, username, password ->
                    signUp(
                        iccid, username, password
                    )
                }, ipConfig = { navController.navigate("ipConfig") }, getrules = getrules, loginError = loginError)
            }
        }
        composable(route = "mainScreen") {
            MainScreen(
                chatList = chatList,
                navHostController = navController,
                searchContactList = searchContactList,
                searchContact = searchContact,
                clearSearchList = clearSearchList
            )
        }
        composable(
            route = "chatScreen?id={id}", arguments = listOf(
                navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ChatScreen(
                back = { navController.popBackStack() }, id = id,
                //whatismybackgroundfiltercolor = { color, show -> }
            )
        }
        composable(route = "wait") {
            val view = LocalView.current
            Scaffold(
                modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(
                        title = {
                            Text("Connecting...")
                        }, expandedHeight = 56.dp, colors = TopAppBarDefaults.topAppBarColors(
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
        composable(route = "ipConfig") {
            val view = LocalView.current
            val focusManager = LocalFocusManager.current
            var host by remember { mutableStateOf(serverIP.split(":")[0]) }
            var port by remember { mutableStateOf(serverIP.split(":")[1]) }

            val portNumber = port.toIntOrNull()
            val portError = portNumber == null || portNumber > 65535 || portNumber < 1

            val networkProtocols = listOf("IPv4", "IPv6", "Domain", "Localhost", "LocalServer")
            var networkProtocol by remember { mutableStateOf(networkProtocols[0]) }
            var expanded by remember { mutableStateOf(false) }
            val devices = listOf("Android Studio Emulator (AVD)", "Genymotion", "Other Diveses")
            //var selectedDevice by remember { mutableStateOf(devices[device()]) }
            //var automaticMode by remember { mutableStateOf(device() != 2) }
            var selectedDevice by remember { mutableStateOf(devices[2]) }
            var automaticMode by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val d = device()
                selectedDevice = devices[d]
                automaticMode = d != 2
            }

            val hostError = !when (networkProtocol) {
                "IPv4" -> {
                    !host.isBlank() && host.split(".").let { parts ->
                        parts.size == 4 && parts.all { part ->
                            val num = part.toIntOrNull()
                            num != null && num in 0..255
                        }
                    }
                }

                "IPv6" -> {
                    !host.isBlank() && Regex(
                        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,7}:$|" + "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" + "^[0-9a-fA-F]{1,4}::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,5}:([0-9a-fA-F]{1,4}:){1,5}$|" +  // پوشش بهتر compressed
                                "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"
                    ).matches(host.removeSurrounding("[", "]"))
                }

                "Domain" -> {
                    !host.isBlank() && Regex(
                        "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$"
                    ).matches(host)
                }

                "Localhost" -> {
                    !host.isBlank() && host.equals("localhost", ignoreCase = true)
                }

                "LocalServer" -> {
                    !host.isBlank() && host.length <= 253 && Regex(
                        "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
                    ).matches(host)
                }

                else -> false
            }

            val keyboardController = LocalSoftwareKeyboardController.current

            Scaffold(
                modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(
                        title = {
                            Text("Set server IP config")
                        }, expandedHeight = 56.dp, colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ), modifier = Modifier.shadow(
                            elevation = 4.dp, shape = RectangleShape, clip = false
                        ), navigationIcon = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    navController.popBackStack()
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        })
                }) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .shadow(
                                elevation = 2.dp, shape = RoundedCornerShape(2.dp), clip = false
                            )
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        devices.forEach { thisDevice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (thisDevice) {
                                            "Android Studio Emulator (AVD)" -> {
                                                setDevice(0)
                                                automaticMode = true
                                                networkProtocol = "IPv4"
                                                host = "10.0.2.2"
                                                port = "8765"
                                            }

                                            "Genymotion" -> {
                                                setDevice(1)
                                                automaticMode = true
                                                networkProtocol = "IPv4"
                                                host = "10.0.3.2"
                                                port = "8765"
                                            }

                                            "Other Diveses" -> {
                                                setDevice(2)
                                                automaticMode = false
                                            }

                                            else -> {
                                                setDevice(2)
                                                automaticMode = false
                                            }
                                        }
                                        selectedDevice = thisDevice
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedDevice == thisDevice, onClick = {
                                        when (thisDevice) {
                                            "Android Studio Emulator (AVD)" -> {
                                                setDevice(0)
                                                automaticMode = true
                                                networkProtocol = "IPv4"
                                                host = "10.0.2.2"
                                                port = "8765"
                                            }

                                            "Genymotion" -> {
                                                setDevice(1)
                                                automaticMode = true
                                                networkProtocol = "IPv4"
                                                host = "10.0.3.2"
                                                port = "8765"
                                            }

                                            "Other Diveses" -> {
                                                setDevice(2)
                                                automaticMode = false
                                            }

                                            else -> {
                                                setDevice(2)
                                                automaticMode = false
                                            }
                                        }
                                        selectedDevice = thisDevice
                                    })
                                Text(
                                    text = thisDevice,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(0.6f),
                            value = host,
                            onValueChange = { host = it },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent
                            ),
                            label = {
                                Text(text = "Host")
                            },
                            enabled = !automaticMode,
                            isError = hostError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = port,
                            onValueChange = { port = it },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent
                            ),
                            label = {
                                Text(text = "Port")
                            },
                            enabled = !automaticMode,
                            isError = portError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.moveFocus(FocusDirection.Down)
                                })
                        )
                    }

                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            value = networkProtocol,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text("Protocol")
                            },
                            enabled = false,/*!automaticMode*/
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent
                            ),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier
                        ) {
                            networkProtocols.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    networkProtocol = item
                                    expanded = false
                                })
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            navController.popBackStack()
                            setServerIP("$host:$port")
                        },
                        enabled = !portError and !hostError,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(56.dp)
                            .align(Alignment.BottomEnd)
                            .shadow(
                                elevation = if (!portError and !hostError) 6.dp else 0.dp,
                                shape = CircleShape,
                                clip = false
                            ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = "OK",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onPrimary
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
fun Greeting(
    setTheme: () -> Unit,
    theme: Int,
    signIn: (String, String) -> Unit,
    signUp: (String, String, String) -> Unit,
    ipConfig: () -> Unit,
    getrules: () -> String,
    loginError: Int
) {
    val context = LocalContext.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }

    var dropdownThemeText by remember { mutableStateOf("Theme: System") }
    var dropdownThemeIcon by remember { mutableIntStateOf(R.drawable.auto) }

    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var showSignInDialog by remember { mutableStateOf(false) }

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

    val signInText = buildAnnotatedString {
        append("Already have an account? ")

        val link = LinkAnnotation.Clickable(
            tag = "signIn",
            linkInteractionListener = { showSignInDialog = true },
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
        )

        withLink(link) {
            append("Sign in.")
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

    LaunchedEffect(loginError) {
        isLoading = false
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            if (expanded) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(0f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            expanded = false
                        })
            }

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
                                        elevation = 16.dp, clip = false
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
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (hasPhonePermission) 6.dp else 0.dp,          // سایه مطابق MD1
                                            shape = CircleShape,
                                            clip = false               // اجازه خروج سایه از محدوده
                                        ),
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
                                        elevation = 16.dp, clip = false
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

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(innerPadding)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        )
                                        .padding(end = 64.dp)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = signInText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        //isLoading = true
                                        showSignUpDialog = true
                                    },
                                    enabled = !isLoading and !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(8.dp)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (!isLoading and !selectedIccid.isNullOrBlank()) 6.dp else 0.dp,          // سایه مطابق MD1
                                            shape = CircleShape,
                                            clip = false               // اجازه خروج سایه از محدوده
                                        ),
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

//                        Box(
//                            modifier = Modifier
//                                .padding(16.dp)
//                                .align(Alignment.TopEnd)
//                        ) {
//                            DropdownMenu(
//                                modifier = Modifier.width(200.dp),
//                                expanded = expanded,
//                                onDismissRequest = { expanded = false }) {
//                                DropdownMenuItem(text = {
//                                    Text(
//                                        text = dropdownThemeText
//                                    )
//                                }, leadingIcon = {
//                                    Icon(
//                                        painter = painterResource(
//                                            id = dropdownThemeIcon
//                                        ), contentDescription = "Theme"
//                                    )
//                                }, onClick = {
//                                    view.playSoundEffect(SoundEffectConstants.CLICK)
//                                    setTheme()
//                                })
//                                DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.support),
//                                        contentDescription = "Support"
//                                    )
//                                }, onClick = {
//                                    view.playSoundEffect(SoundEffectConstants.CLICK)
//                                    expanded = false
//                                    showSupportDialog = true
//                                })
//                            }
//                        }
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

                AnimatedDropMenu(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    width = 200.dp,
                    height = 160.dp,
                    chord = 256.dp,
                    isExpanded = expanded,
                    close = { expanded = false },
                    ratioX = 1f,
                    ratioY = 0f,
                    position = Alignment.TopEnd,
                    hasBackgroundCover = false,
                    whatismybackgroundfiltercolor = { color, show -> }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                        DropdownMenuItem(text = { Text("IP config") }, leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.tune),
                                contentDescription = "IP config"
                            )
                        }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            expanded = false
                            ipConfig()
                            //navController.navigate("ipConfig")
                        })
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
                                        clip = false
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

//                                Box(
//                                    modifier = Modifier
//                                        .padding(16.dp)
//                                        .align(Alignment.TopEnd)
//                                ) {
//                                    DropdownMenu(
//                                        modifier = Modifier.width(200.dp),
//                                        expanded = expanded,
//                                        onDismissRequest = { expanded = false }) {
//                                        DropdownMenuItem(text = {
//                                            Text(
//                                                text = dropdownThemeText
//                                            )
//                                        }, leadingIcon = {
//                                            Icon(
//                                                painter = painterResource(
//                                                    id = dropdownThemeIcon
//                                                ), contentDescription = "Theme"
//                                            )
//                                        }, onClick = {
//                                            view.playSoundEffect(SoundEffectConstants.CLICK)
//                                            setTheme()
//                                        })
//                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.support),
//                                                contentDescription = "Support"
//                                            )
//                                        }, onClick = {
//                                            view.playSoundEffect(SoundEffectConstants.CLICK)
//                                            expanded = false
//                                            showSupportDialog = true
//                                        })
//                                    }
//                                }

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
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (hasPhonePermission) 6.dp else 0.dp,          // سایه مطابق MD1
                                            shape = CircleShape,
                                            clip = false               // اجازه خروج سایه از محدوده
                                        ),
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

                                AnimatedDropMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatismybackgroundfiltercolor = { color, show -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
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
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                                //navController.navigate("ipConfig")
                                            })
                                    }
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
                                        clip = false
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
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    VerifySimCard(
                                        selectedIccid = selectedIccid,
                                        setSelectedIccid = { value -> selectedIccid = value },
                                        endPadding = 56.dp
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
                                        //isLoading = true
                                        showSignUpDialog = true
                                    },
                                    enabled = !isLoading and !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (!isLoading and !selectedIccid.isNullOrBlank()) 6.dp else 0.dp,          // سایه مطابق MD1
                                            shape = CircleShape,
                                            clip = false               // اجازه خروج سایه از محدوده
                                        ),
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

//                                Box(
//                                    modifier = Modifier
//                                        .padding(16.dp)
//                                        .padding(8.dp)
//                                        .align(Alignment.TopEnd)
//                                ) {
//                                    DropdownMenu(
//                                        modifier = Modifier.width(200.dp),
//                                        expanded = expanded,
//                                        onDismissRequest = { expanded = false }) {
//                                        DropdownMenuItem(text = {
//                                            Text(
//                                                text = dropdownThemeText
//                                            )
//                                        }, leadingIcon = {
//                                            Icon(
//                                                painter = painterResource(
//                                                    id = dropdownThemeIcon
//                                                ), contentDescription = "Theme"
//                                            )
//                                        }, onClick = {
//                                            view.playSoundEffect(SoundEffectConstants.CLICK)
//                                            setTheme()
//                                        })
//                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.support),
//                                                contentDescription = "Support"
//                                            )
//                                        }, onClick = {
//                                            view.playSoundEffect(SoundEffectConstants.CLICK)
//                                            expanded = false
//                                            showSupportDialog = true
//                                        })
//                                    }
//                                }

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
                                        text = signInText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }

                                AnimatedDropMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatismybackgroundfiltercolor = { color, show -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
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
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                                //navController.navigate("ipConfig")
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
            shape = RoundedCornerShape(2.dp),
            confirmButton = {
                Button(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        val phoneNumber = "09369152046"
                        val uri = "sms:$phoneNumber".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(2.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sms), contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS")
                }
            },
            dismissButton = {
                TextButton(
                    shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSupportDialog = false
                    }) {
                    Text("Dismiss")
                }
            },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
                .fillMaxHeight(0.8f),
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Term of use and privacy") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = getrules())
                }
            },
            shape = RoundedCornerShape(2.dp),
            confirmButton = {
                TextButton(
                    shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showTermsDialog = false
                    }) {
                    Text("Dismiss")
                }
            })
    }

    if (showSignUpDialog) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        val focusManager = LocalFocusManager.current
        val passwordVisible = remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val coroutineScope = rememberCoroutineScope()

        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }

        val isUsernameError = (!Regex("^[a-z0-9_]+$").matches(username)) && username.isNotEmpty()
        val usernameSizeError = username.length !in 1..50

        val isPasswordError = password.length !in 6..128

        var firstClick by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isLoading) showSignUpDialog = false
            }, title = { Text("Sign-Up") }, text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Setup your username and password.")

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(usernameFocusRequester), // ← اضافه شد
                        value = username,
                        onValueChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            username = it
                        },
                        isError = isUsernameError or usernameSizeError and firstClick,
                        supportingText = {
                            Column() {
                                if (isUsernameError and firstClick) {
                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                }
                                if (usernameSizeError and firstClick) {
                                    Text("Username must be less than 50 characters.")
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        ),
                        label = { Text("Username") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                passwordFocusRequester.requestFocus()
                            })
                    )

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester),
                        value = password,
                        onValueChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            password = it
                        },
                        isError = isPasswordError and firstClick,
                        supportingText = {
                            if (isPasswordError and firstClick) {
                                Text("Password must be beetwin 6 and 50 characters.")
                            }
                        },
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        ),
                        label = {
                            Text(text = "Password")
                        },
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                enabled = !isLoading, onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    passwordVisible.value = !passwordVisible.value
                                }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible.value) R.drawable.visibility_off
                                        else R.drawable.visibility
                                    ), contentDescription = null
                                )
                            }
                        },
                        textStyle = TextStyle(textDirection = TextDirection.Content),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                coroutineScope.launch {
                                    signUp(selectedIccid.toString(), username, password)
                                    isLoading = true
                                }
                                focusManager.moveFocus(FocusDirection.Down)
                            })
                    )
                }
            }, shape = RoundedCornerShape(2.dp), confirmButton = {
                Button(
                    enabled = (!isLoading and !(isUsernameError or usernameSizeError) and username.isNotBlank() and !isPasswordError) or !firstClick,
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        if (firstClick) {
                            signUp(selectedIccid.toString(), username, password)
                            isLoading = true
                        } else {
                            firstClick = true
                            if (!isLoading and !(isUsernameError or usernameSizeError) and username.isNotBlank() and !isPasswordError) {
                                signUp(selectedIccid.toString(), username, password)
                                isLoading = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(2.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.login),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign-Up")
                }
            }, dismissButton = {
                TextButton(
                    enabled = !isLoading, shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isLoading = false
                        showSignUpDialog = false
                    }) {
                    Text("Cancel")
                }
            }, modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
        )
    }

    if (showSignInDialog) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        val focusManager = LocalFocusManager.current
        val passwordVisible = remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val coroutineScope = rememberCoroutineScope()

        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }

        val isUsernameError = (!Regex("^[a-z0-9_]+$").matches(username)) && username.isNotEmpty()
        val usernameSizeError = username.length !in 1..50

        val isPasswordError = password.length !in 6..128

        var firstClick by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isLoading) showSignInDialog = false
            }, title = { Text("Sign-In") }, text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "Enter your username and password.")

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(usernameFocusRequester), // ← اضافه شد
                        value = username,
                        onValueChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            username = it
                        },
                        isError = isUsernameError or usernameSizeError and firstClick,
                        supportingText = {
                            Column() {
                                if (isUsernameError and firstClick) {
                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                }
                                if (usernameSizeError and firstClick) {
                                    Text("Username must be less than 50 characters.")
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        ),
                        label = { Text("Username") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                passwordFocusRequester.requestFocus() // ← تغییر کرد
                            })
                    )

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester), // ← اضافه شد
                        value = password,
                        onValueChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            password = it
                        },
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        label = {
                            Text(text = "Password")
                        },
                        isError = isPasswordError and firstClick,
                        supportingText = {
                            if (isPasswordError and firstClick) {
                                Text("Password must be beetwin 6 and 50 characters.")
                            }
                        },
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                enabled = !isLoading, onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    passwordVisible.value = !passwordVisible.value
                                }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible.value) R.drawable.visibility_off
                                        else R.drawable.visibility
                                    ), contentDescription = null
                                )
                            }
                        },
                        textStyle = TextStyle(textDirection = TextDirection.Content),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                coroutineScope.launch {
                                    signIn(username, password)
                                    isLoading = true
                                }
                                focusManager.moveFocus(FocusDirection.Down)
                            })
                    )
                }
            }, shape = RoundedCornerShape(2.dp), confirmButton = {
                Button(
                    enabled = (!isLoading and !(isUsernameError or usernameSizeError) and username.isNotBlank() and !isPasswordError) or !firstClick,
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        if (firstClick) {
                            signIn(username, password)
                            isLoading = true
                        } else {
                            firstClick = true
                            if (!isLoading and !(isUsernameError or usernameSizeError) and username.isNotBlank() and !isPasswordError) {
                                signIn(username, password)
                                isLoading = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(2.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.login),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign-In")
                }
            }, dismissButton = {
                TextButton(
                    enabled = !isLoading, shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isLoading = false
                        showSignInDialog = false
                    }) {
                    Text("Cancel")
                }
            }, modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySimCard(selectedIccid: String?, setSelectedIccid: (String) -> Unit, endPadding: Dp) {

    val context = LocalContext.current
    val view = LocalView.current

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        //val iccidCollector = remember(context) { IccidCollector(context) }
        val iccids =
            remember { mutableStateOf(getIccidsFromSubscriptionManager(context = context)) }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ), elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ), shape = RectangleShape
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
                    ), shape = RoundedCornerShape(2.dp), onClick = {
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
                ), shape = RectangleShape
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
                    ), shape = RoundedCornerShape(2.dp), onClick = {
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

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainScreen(
    chatList: List<Person>,
    navHostController: NavHostController,
    searchContactList: List<SearchEntity>,
    searchContact: (String) -> Unit,
    clearSearchList: () -> Unit
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

    val view = LocalView.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(drawerRatio)
                    .shadow(elevation = 16.dp, clip = false)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
            ) {

                var expanded by remember { mutableStateOf(false) }
                val expansionHeight = animateDpAsState(
                    targetValue = if (expanded) 160.dp else 0.dp,
                    animationSpec = tween(durationMillis = 200)
                )

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
                        modifier = Modifier
                            //.systemBarsPadding()
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                            )
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            //verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                                    .clip(CircleShape), shape = CircleShape
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.profile),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .padding(end = 16.dp)
                                    .fillMaxHeight(1 / 2f)
                                    .aspectRatio(1f)
                                    .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                                    .clip(CircleShape), shape = CircleShape
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.profile),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .padding(end = 16.dp)
                                    .fillMaxHeight(1 / 2f)
                                    .aspectRatio(1f)
                                    .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                                    .clip(CircleShape), shape = CircleShape
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.profile),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
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
                                    expanded = !expanded
                                }
                                .padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "Imam Ali", // (account name)
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
                                    expanded = !expanded
                                }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_drop_down),
                                    contentDescription = null,
                                    modifier = Modifier.rotate((expansionHeight.value.value / 160) * 180f),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(expansionHeight.value)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .drawWithContent {
                            drawContent()

                            val gradientHeight = 8.dp.value //size.height * 0.15f

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent, Color.Black.copy(alpha = 0.1f)
                                    ), startY = size.height - gradientHeight, endY = size.height
                                ), blendMode = BlendMode.Multiply
                            )
                        }
                        .verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Column(
                    Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    })
                    DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.support),
                            contentDescription = "Support"
                        )
                    }, onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
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
            ) {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    //.shadow(
                    //    elevation = 16.dp,
                    //    clip = false
                    //),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("GChat")
                            }, expandedHeight = 56.dp, navigationIcon = {
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
                                items = chatList, key = { it.id }) { person ->

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        val id = person.id
                                        selectedChat = id
                                        if (!expandedScreen) {
                                            navHostController.navigate("chatScreen?id=$id")
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

                                            Surface(
                                                modifier = Modifier.size(40.dp), shape = CircleShape
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.profile),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Spacer(Modifier.width(16.dp))

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {

                                                Text(
                                                    text = person.name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = person.lastMessageText,
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
                                                    text = person.lastMessageDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                if (person.unreadMessages > 0) {

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
                                                            text = person.unreadMessages.toString(),
                                                            color = MaterialTheme.colorScheme.onPrimary,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            maxLines = 1
                                                        )
                                                    }

                                                } else {

                                                    Icon(
                                                        painter = painterResource(R.drawable.double_check),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
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
                                contentDescription = "Accept and Sign-In",
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                                    searchContact(it)
                                    searchContent = it
                                    println(searchContactList)
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
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
                                elevation = 4.dp,
                                shape = RoundedCornerShape(2.dp),
                                clip = false
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
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
                                }
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

                                        Box(Modifier.size(40.dp)) {
                                            Surface(
                                                modifier = Modifier.size(40.dp), shape = CircleShape
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.profile),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Spacer(
                                                Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFF23A55A), CircleShape)
                                                    .align(Alignment.TopEnd)
                                            )
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

//                if (expandedScreen and covered) {
//                    Spacer(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(coverColor)
//                            .clickable(
//                                indication = null,
//                                interactionSource = remember { MutableInteractionSource() }) {
//                            }
//                    )
//                }
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
                                        Color.Black.copy(alpha = 0.1f), Color.Transparent
                                    ), startX = 0.dp.toPx(), endX = 8.dp.toPx()
                                ), blendMode = BlendMode.Multiply
                            )
                        }) {
                    ChatScreen(
                        back = { navHostController.popBackStack() }, id = selectedChat,
//                        whatismybackgroundfiltercolor = { color, show ->
//                            covered = show
//                            coverColor = color
//                        }
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
    ratioY: Float,
    position: Alignment,
    hasBackgroundCover: Boolean,
    tabletView: Boolean = false,
    whatismybackgroundfiltercolor: (Color, Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val sizeBtn by animateDpAsState(
        targetValue = if (isExpanded) (chord.value * 2).dp else 48.dp, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        ), label = "circle_size"
    )

    //val surface = MaterialTheme.colorScheme.surface
    val surface = MenuDefaults.containerColor

    whatismybackgroundfiltercolor(
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
        whatismybackgroundfiltercolor(
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
            val canvasmodifier =
                if (sizeBtn / 2 != 24.dp) Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) { } else Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)

            Canvas(
                modifier = canvasmodifier
            ) {
                val radius = sizeBtn.toPx() / 2

                //Alignment.BottomStart -> buttomLeftmenu
                //Alignment.TopEnd -> dropdownmenu

                val centerX = when (position) {
                    Alignment.BottomStart -> size.width * ratioX + 24.dp.toPx()
                    Alignment.TopEnd -> if (tabletView) size.width * ratioX - 24.dp.toPx() else size.width * ratioX - 12.dp.toPx()
                    else -> size.width * ratioX // Default fallback
                }

                val centerY = when (position) {
                    Alignment.BottomStart -> size.height * ratioY - 24.dp.toPx()
                    Alignment.TopEnd -> if (tabletView) size.height * ratioY + 24.dp.toPx() else size.height * ratioY + 12.dp.toPx()
                    else -> size.height * ratioY // Default fallback
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
    back: () -> Boolean, id: String,
    //whatismybackgroundfiltercolor: (Color, Boolean) -> Unit
) {
    var renderValue by remember { mutableIntStateOf(5) }
    var isExpandedAttachment by remember { mutableStateOf(false) }
    //var isExpandedEmoji by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val onSurface = MaterialTheme.colorScheme.onSurface

    var covered by remember { mutableStateOf(false) }
    //var coverColor by remember { mutableStateOf(Color.Transparent) }
    val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })

    var coverColor by rememberSaveable(
        stateSaver = colorSaver
    ) { mutableStateOf(Color.Transparent) }

    val view = LocalView.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        topBar = {
            if (id != "") {

                TopAppBar(
                    title = {
                        Text("Contact")
                    }, expandedHeight = 56.dp, navigationIcon = {
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
                    }, actions = {
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                            }) {
                            Icon(
                                painter = painterResource(R.drawable.menu_dots),
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
            }
        },

        ) { innerPadding ->
        Column {
            AdvancedDynamicLightEffectOptim(
                modifier = Modifier.weight(1f), renderValue = renderValue
            )
        }
        if (id != "") {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f), reverseLayout = true
                ) {

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp, max = 256.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .imePadding(),
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

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                factory = { context ->
                                    EditText(context).apply {
                                        layoutDirection = View.LAYOUT_DIRECTION_LOCALE
                                        textDirection = View.TEXT_DIRECTION_FIRST_STRONG
                                        gravity = Gravity.START or Gravity.TOP
                                        background = null

                                        hint = "Type your message..."
                                        setHintTextColor(onSurface.copy(alpha = 0.5f).toArgb())

                                        //addTextChangedListener(object : TextWatcher {
                                        //    override fun beforeTextChanged(
                                        //        s: CharSequence?,
                                        //        start: Int,
                                        //        count: Int,
                                        //        after: Int
                                        //    ) {
                                        //    }

                                        //    override fun onTextChanged(
                                        //        s: CharSequence?,
                                        //        start: Int,
                                        //        before: Int,
                                        //        count: Int
                                        //    ) {
                                        //        val newText = s.toString()
                                        //        if (newText != message) {
                                        //            onValueChange(newText)
                                        //        }
                                        //    }

                                        //    override fun afterTextChanged(s: Editable?) {}
                                        //})
                                    }
                                },
                                update = { editText ->
                                    val currentText = editText.text.toString()
                                    if (currentText != message) {
                                        editText.setText(message)
                                        editText.setSelection(message.length)
                                    }

                                    editText.setTextColor(onSurface.toArgb())
                                })
                        }
                    }

                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            renderValue = (1..10).random()
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

            AnimatedDropMenu(
                modifier = Modifier.padding(innerPadding),
                width = 224.dp,
                height = 112.dp,
                chord = 250.dp,
                isExpanded = isExpandedAttachment,
                close = { isExpandedAttachment = false },
                ratioX = 0f,
                ratioY = 1f,
                position = Alignment.BottomStart,
                hasBackgroundCover = true,
                whatismybackgroundfiltercolor = { color, show ->
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
                        }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                            Icon(
                                painterResource(R.drawable.photo), contentDescription = null
                            )
                        }, trailingIcon = { }, enabled = true
                    )
                    DropdownMenuItem(
                        text = { Text(text = "File") }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }, modifier = Modifier.fillMaxWidth(), leadingIcon = {
                            Icon(
                                painterResource(R.drawable.folder), contentDescription = null
                            )
                        }, trailingIcon = { }, enabled = true
                    )
                }
            }

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
            //    whatismybackgroundfiltercolor = { color, show ->
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
            //            text = { Text(text = "Photos and videos") }, onClick = {
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

    if (covered) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    56.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
                .background(coverColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    //if (isExpandedEmoji) {
                    //    isExpandedEmoji = false
                    //} else
                    if (isExpandedAttachment) {
                        isExpandedAttachment = false
                    }
                })
    }
}

@Composable
fun AdvancedDynamicLightEffectOptim(
    modifier: Modifier = Modifier, renderValue: Int = 5
) {
    val targetAngle1 = renderValue * 36f
    val angle1 by animateFloatAsState(
        targetValue = targetAngle1, animationSpec = tween(
            durationMillis = 2000, easing = FastOutSlowInEasing
        ), label = "angle1"
    )

    val targetAngle2 = targetAngle1 + 180f
    val angle2 by animateFloatAsState(
        targetValue = targetAngle2, animationSpec = tween(
            durationMillis = 2000, easing = FastOutSlowInEasing
        ), label = "angle2"
    )

    val sceneRotation by animateFloatAsState(
        targetValue = 0f, animationSpec = tween(durationMillis = 1), label = "scene"
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
                    Color(0xFF6DA687), Color(0xFF89B885)
                ),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y)
            )
        )

        rotate(
            degrees = sceneRotation, pivot = center
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
                        Color(0xFFD1D68C), Color.Transparent
                    ), center = pointOnCircle(angle1), radius = lightRadius
                ), radius = radius, center = center, blendMode = BlendMode.Screen
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD5DAB8), Color.Transparent
                    ), center = pointOnCircle(angle2), radius = lightRadius * 0.85f
                ), radius = radius, center = center, blendMode = BlendMode.Screen
            )
        }
    }
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // بررسی امنیتی برای Broadcast
        if (!isValidBroadcast(intent)) {
            return
        }

        // بررسی اینکه اپلیکیشن پیش‌فرض SMS هست
        if (!isDefaultSmsApp(context)) {
            return
        }

        // کد اصلی شما اینجا
        handleSms(context, intent)
    }

    private fun isValidBroadcast(intent: Intent): Boolean {
        // بررسی اینکه intent از سیستم هست نه از اپلیکیشن دیگه
        return intent.action == "android.provider.Telephony.SMS_DELIVER"
    }

    private fun isDefaultSmsApp(context: Context): Boolean {
        val packageName = context.packageName
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
        return packageName == defaultSms
    }

    private fun handleSms(context: Context, intent: Intent) {
        // منطق دریافت SMS
    }
}

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isValidBroadcast(intent)) {
            return
        }

        if (!isDefaultSmsApp(context)) {
            return
        }

        handleMms(context, intent)
    }

    private fun isValidBroadcast(intent: Intent): Boolean {
        return intent.action == "android.provider.Telephony.WAP_PUSH_DELIVER"
    }

    private fun isDefaultSmsApp(context: Context): Boolean {
        val packageName = context.packageName
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
        return packageName == defaultSms
    }

    private fun handleMms(context: Context, intent: Intent) {
        // منطق دریافت MMS
    }
}

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // بررسی اینکه اپلیکیشن پیش‌فرض SMS هست
        if (!isDefaultSmsApp()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // بررسی مجوزها
        if (!checkPhonePermission() || !checkSmsAppRole()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // پردازش intent
        intent?.let {
            handleSendSms(it)
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun isDefaultSmsApp(): Boolean {
        val packageName = packageName
        val defaultSms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.getDefaultSmsPackage(this)
        } else {
            null
        }
        return packageName == defaultSms
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkSmsAppRole(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        } else {
            true
        }
    }

    private fun handleSendSms(intent: Intent) {
        // منطق ارسال SMS با محدودیت‌های امنیتی
        // فقط به مخاطبین اجازه بدید یا شماره‌های خاص
    }
}

fun getICCIDList(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
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
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Exception) {
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
        true
    }
}