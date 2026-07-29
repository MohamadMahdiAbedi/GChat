package ir.gchat

import android.app.Application
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

class SocketViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    private val _oldLoggedIn = MutableStateFlow(false)
    val oldLoggedIn: StateFlow<Boolean> = _oldLoggedIn.asStateFlow()

    private var savedUsername = ""
    private var savedPassword = ""

    private val _usernameState = MutableStateFlow("")
    val usernameState: StateFlow<String> = _usernameState.asStateFlow()

    private val _serverIP = MutableStateFlow("127.0.0.1:8765")
    val serverIP: StateFlow<String> = _serverIP.asStateFlow()

    // 0 -> success
    // 1 -> iccid already exist
    // 2 -> username already exist
    // 3 -> password is incorrect
    // 4 -> untitled error
    // 5 -> username not found
    // 6 -> invalid input(limit error)
    private val _loginError = MutableStateFlow(0)
    val loginError: StateFlow<Int> = _loginError.asStateFlow()

    private val _chatList = MutableStateFlow(emptyList<Contact>())
    val chatList: StateFlow<List<Contact>> = _chatList.asStateFlow()

    private val _contactSearchList = MutableStateFlow(emptyList<SearchEntity>())
    val contactSearchList: StateFlow<List<SearchEntity>> = _contactSearchList.asStateFlow()

    private val _messageList = MutableStateFlow(emptyList<MessageItem>())
    val messageList: StateFlow<List<MessageItem>> = _messageList.asStateFlow()

    private var openedChat = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Load saved values from DataStore
            context.dataStore.data.collect { prefs ->
                savedUsername = prefs[USERNAME_KEY] ?: ""
                _usernameState.value = savedUsername

                savedPassword = prefs[PASSWORD_KEY] ?: ""

                _oldLoggedIn.value = prefs[LOGGED_IN_STATUS_KEY] ?: false

                _serverIP.value = prefs[SERVERIP_KEY] ?: "127.0.0.1:8765"
            }
        }
    }

    fun connect() {
        if (webSocket != null) {
            return
        }
        val request = Request.Builder().url("ws://${_serverIP.value}").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModelScope.launch {
                    try {
                        if (_oldLoggedIn.value) {
                            webSocket.send(
                                JSONObject().apply {
                                    put("type", "signin")
                                    put("username", savedUsername)
                                    put("password", savedPassword)
                                }.toString()
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
                        when (type) {
                            "signup_response" -> {
                                val message = jsonObject.getString("status")
                                when (message) {
                                    "success" -> {
                                        _loginError.value = 0
                                        _oldLoggedIn.value = true
                                        _loggedIn.value = true
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = true
                                        }
                                    }
                                    "iccid_error" -> {
                                        _loginError.value = 1
                                        Toast.makeText(
                                            context, "ICCID already exists", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "username_error" -> {
                                        _loginError.value = 2
                                        Toast.makeText(
                                            context, "Username already exists", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "error" -> {
                                        _loginError.value = 4
                                        Toast.makeText(
                                            context, "Untitled error", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "invalid_input" -> {
                                        _loginError.value = 6
                                        Toast.makeText(
                                            context, "Invalid input size", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                }
                            }
                            "signin_response" -> {
                                val message = jsonObject.getString("status")
                                when (message) {
                                    "success" -> {
                                        _loginError.value = 0
                                        _oldLoggedIn.value = true
                                        _loggedIn.value = true
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = true
                                        }
                                    }
                                    "password_error" -> {
                                        _loginError.value = 3
                                        Toast.makeText(
                                            context, "Password is incorrect", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "error" -> {
                                        _loginError.value = 4
                                        Toast.makeText(
                                            context, "Untitled error", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "username_error" -> {
                                        _loginError.value = 5
                                        Toast.makeText(
                                            context, "Username not found", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }
                                    "invalid_input" -> {
                                        _loginError.value = 6
                                        Toast.makeText(
                                            context, "Invalid input size", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
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
                                        //خودت رو نشون نده
                                        if (item.getString("username") == savedUsername) {
                                            continue
                                        }
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
                            "get_dms_response" -> {
                                _messageList.value = emptyList<MessageItem>()
                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("messages")
                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)
                                        _messageList.value += MessageItem(
                                            text = item.getString("content"),
                                            id = item.getInt("id"),
                                            myMessage = item.getString("sender") == savedUsername,
                                            date = item.getString("timestamp"),
                                            seen = item.getBoolean("read")
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context, "Error Receiving messages", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            "get_conversations_response" -> {
                                _chatList.value = emptyList<Contact>()
                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("conversations")
                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)
                                        _chatList.value += Contact(
                                            id = item.getString("with"),
                                            name = item.getString("with"),
                                            lastMessageText = item.getString("last_message"),
                                            lastMessageDate = item.getString("last_timestamp"),
                                            unreadMessages = item.getInt("unread_count")
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context, "Error Receiving messages", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            "new_dm" -> {
                                getConversations()
                                if (jsonObject.getString("sender") == openedChat.value) {
                                    //هندل بدون رفرش کامل
                                    //_messageList.value += MessageItem(
                                    // text = jsonObject.getString("content"),
                                    // myMessage = false,
                                    // date = jsonObject.getString("timestamp"),
                                    // seen = jsonObject.getBoolean("read")
                                    //)
                                    getMessagesList(openedChat.value)
                                }
                            }
                            "mark_read_response" -> {
                                // اگر سین نخورده بود یه بار دیگه سین بزن
                                getConversations()
                                getMessagesList(openedChat.value)
                            }
                            "message_read" -> {
                                if (jsonObject.getString("reader") == openedChat.value) {
                                    getMessagesList(openedChat.value)
                                } else {
                                    getConversations()
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                webSocket.cancel()
                //webSocket.close(1000, "Normal Closure")
                this@SocketViewModel.webSocket = null
                _loggedIn.value = false
                viewModelScope.launch {
                    delay(1000.milliseconds)
                    connect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.cancel()
                //webSocket.close(1000, "Normal Closure")
                this@SocketViewModel.webSocket = null
                _loggedIn.value = false
                viewModelScope.launch {
                    delay(1000.milliseconds)
                    connect()
                }
            }
        })
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            try {
                savedUsername = username
                context.dataStore.edit { preferences ->
                    preferences[USERNAME_KEY] = username
                }
                savedPassword = password
                context.dataStore.edit { preferences ->
                    preferences[PASSWORD_KEY] = password
                }
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "signin")
                        put("username", username)
                        put("password", password)
                    }.toString()
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
                savedUsername = username
                context.dataStore.edit { preferences ->
                    preferences[USERNAME_KEY] = username
                }
                savedPassword = password
                context.dataStore.edit { preferences ->
                    preferences[PASSWORD_KEY] = password
                }
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "signup")
                        put("iccid", iccid)
                        put("username", username)
                        put("password", password)
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun setServerIP(serverIP: String) {
        _serverIP.value = serverIP
        webSocket?.cancel()
        //webSocket.close(1000, "Normal Closure")
        this@SocketViewModel.webSocket = null
        _loggedIn.value = false
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
                    JSONObject().apply {
                        put("type", "search_user")
                        put("username", username)
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun clearSearchMemory() {
        _contactSearchList.value = emptyList<SearchEntity>()
    }

    fun logout() {
        webSocket?.cancel()
        //webSocket.close(1000, "Normal Closure")
        this@SocketViewModel.webSocket = null
        viewModelScope.launch {
            _oldLoggedIn.value = false
            _loggedIn.value = false
            context.dataStore.edit { preferences ->
                preferences[LOGGED_IN_STATUS_KEY] = false
            }
            connect()
        }
    }

    fun sendMessage(contact: String, message: String) {
        _messageList.value += MessageItem(text = message, myMessage = true)
        viewModelScope.launch {
            try {
                val json = JSONObject().apply {
                    put("type", "send_dm")
                    put("recipient", contact)
                    put("content", message)
                }
                webSocket?.send(json.toString())
            } catch (_: Exception) {
                // log error
            }
        }
        getMessagesList(contact)
    }

    fun getConversations() {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "get_conversations")
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun getMessagesList(contact: String) {
        //اینجا احتمالا یه باگ با شرف داریم
        openedChat.value = contact
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "get_dms")
                        put("with", contact)
                        put("limit", 50)
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun seenMessage(contact: String, id: Int) {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "mark_read")
                        put("with", contact)
                        put("id", id)
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }
}