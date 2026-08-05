package ir.gchat

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.TimeUnit

class SocketViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    var webSocket: WebSocket? = null
    /*private*/ val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        //.readTimeout(3, TimeUnit.SECONDS)
        //.writeTimeout(3, TimeUnit.SECONDS)
        //.retryOnConnectionFailure(true)
        .build()

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

    private suspend fun loadSavedData() {
        val prefs = context.dataStore.data.first()
        savedUsername = prefs[USERNAME_KEY] ?: ""
        _usernameState.value = savedUsername
        savedPassword = prefs[PASSWORD_KEY] ?: ""
        _oldLoggedIn.value = prefs[LOGGED_IN_STATUS_KEY] ?: false
        _serverIP.value = prefs[SERVERIP_KEY] ?: "127.0.0.1:8765"
    }

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        viewModelScope.launch {
            loadSavedData()
            //if (_oldLoggedIn.value) {
            //connect()
            //}
            _ready.value = true
        }
    }

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

    private val _contactSearchList = MutableStateFlow(emptyList<Contact>())
    val contactSearchList: StateFlow<List<Contact>> = _contactSearchList.asStateFlow()

    private val _messageList = MutableStateFlow(emptyList<MessageItem>())
    val messageList: StateFlow<List<MessageItem>> = _messageList.asStateFlow()

    private var openedChat = MutableStateFlow("")

    private var _shouldScrollToBottom = MutableStateFlow(false)
    val shouldScrollToBottom: StateFlow<Boolean> = _shouldScrollToBottom.asStateFlow()

    private var _uploads = MutableStateFlow(emptyList<File>())
    val uploads: StateFlow<List<File>> = _uploads.asStateFlow()

    private var uploadToken = ""

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
                        Log.d("server", jsonObject.toString())
                        when (jsonObject.getString("type")) {
                            "signup_response" -> {
                                val message = jsonObject.getString("status")
                                when (message) {
                                    "success" -> {
                                        _loginError.value = 0
                                        _oldLoggedIn.value = true
                                        _loggedIn.value = true
                                        uploadToken = jsonObject.getString("token")
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
                                        uploadToken = jsonObject.getString("token")
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
                                _contactSearchList.value = emptyList<Contact>()
                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("results")
                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)
                                        //خودت رو نشون نده
                                        if (item.getString("username") == savedUsername) {
                                            continue
                                        }
                                        _contactSearchList.value += Contact(
                                            isOnline = item.getBoolean("online"),
                                            id = item.getString("username"),
                                            name = item.getString("username"),
                                            profilePicture = "",
                                            lastMessageText = "",
                                            lastMessageDate = "",
                                            unreadMessages = 0,
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Search error", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                            "get_dms_response" -> {
                                _messageList.value = emptyList()

                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("messages")

                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)

                                        val contentArray = item.getJSONArray("content")

                                        val content = buildList {
                                            for (j in 0 until contentArray.length()) {
                                                val obj = contentArray.getJSONObject(j)

                                                add(
                                                    ContentEntity(
                                                        type = obj.getString("type"),
                                                        text = obj.optString("text"),
                                                        id = obj.optString("id"),
                                                        fileName = obj.optString("file_name")
                                                    )
                                                )
                                            }
                                        }

                                        _messageList.value += MessageItem(
                                            content = content,
                                            id = item.getInt("id"),
                                            myMessage = item.getString("sender") == savedUsername,
                                            date = item.getString("timestamp"),
                                            seen = item.getBoolean("read")
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error Receiving messages",
                                        Toast.LENGTH_SHORT
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
                                            unreadMessages = item.getInt("unread_count"),
                                            isOnline = item.getBoolean("online"),
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

                            "file_upload_response" -> {
                                when (jsonObject.getString("status")) {

                                    "duplicate" -> {
                                        val file = _uploads.value.find {
                                            it.name == jsonObject.getString("name")
                                        }

                                        file?.apply {
                                            id = jsonObject.getLong("file_id").toString()

                                            if (jsonObject.has("thumb_url")) {
                                                thumbUrl = jsonObject.getString("thumb_url")
                                            }
                                        }
                                    }

                                    "new" -> {
                                        Log.d("FileUpload", "وضعیت: new - شروع آپلود")

                                        val file = _uploads.value.find {
                                            it.name == jsonObject.getString("name")
                                        }

                                        if (file == null) {
                                            Log.e("FileUpload", "فایل پیدا نشد با نام: ${jsonObject.getString("name")}")
                                            return@launch
                                        }

                                        Log.d("FileUpload", "فایل پیدا شد: name=${file.name}, localUri=${file.localUri}")

                                        val uri = file.localUri
                                        if (uri == null) {
                                            Log.e("FileUpload", "localUri خالی است")
                                            return@launch
                                        }

                                        val input = context.contentResolver.openInputStream(uri)
                                        if (input == null) {
                                            Log.e("FileUpload", "نتونستم InputStream باز کنم برای uri: $uri")
                                            return@launch
                                        }

                                        try {
                                            val fileBytes = input.readBytes()
                                            val fileSize = fileBytes.size
                                            Log.d("FileUpload", "حجم فایل خوانده شده: $fileSize بایت (${fileSize / 1024 / 1024f} مگابایت)")

                                            val requestBody = MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart(
                                                    "file",
                                                    file.name,
                                                    fileBytes.toRequestBody("video/mp4".toMediaType())
                                                )
                                                .build()

                                            val originalUrl = jsonObject.getString("upload_url")
                                            val finalUrl = originalUrl.replace(
                                                "0.0.0.0",
                                                serverIP.value.split(":")[0]
                                            )

                                            Log.d("FileUpload", "URL اصلی: $originalUrl")
                                            Log.d("FileUpload", "URL نهایی: $finalUrl")

                                            val request = Request.Builder()
                                                .url(finalUrl)
                                                .header("Authorization", "Bearer $uploadToken")
                                                .post(requestBody)
                                                .build()

                                            Log.d("FileUpload", "درخواست آپلود ساخته شد، شروع ارسال...")

                                            OkHttpClient().newCall(request).enqueue(object : Callback {

                                                override fun onFailure(call: Call, e: IOException) {
                                                    Log.e("FileUpload", "آپلود شکست خورد (onFailure)", e)
                                                    e.printStackTrace()
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    response.use {
                                                        val code = it.code
                                                        val bodyString = it.body?.string() ?: "null"

                                                        Log.d("FileUpload", "پاسخ سرور: code=$code")
                                                        Log.d("FileUpload", "بدنه پاسخ: $bodyString")

                                                        if (!it.isSuccessful) {
                                                            Log.e("FileUpload", "آپلود ناموفق - کد: $code")
                                                            return
                                                        }

                                                        try {
                                                            val result = JSONObject(bodyString)

                                                            if (result.getString("status") == "success") {
                                                                file.id = result.getLong("file_id").toString()

                                                                if (result.has("thumb_url")) {
                                                                    file.thumbUrl = result.getString("thumb_url")
                                                                }

                                                                Log.d("FileUpload", "آپلود موفق! file_id=${file.id}, thumb=${file.thumbUrl}")
                                                            } else {
                                                                Log.e("FileUpload", "سرور status=success نداد: $bodyString")
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e("FileUpload", "خطا در پارس کردن پاسخ JSON", e)
                                                        }
                                                    }
                                                }
                                            })

                                        } catch (e: Exception) {
                                            Log.e("FileUpload", "خطا هنگام خواندن فایل یا ساخت درخواست", e)
                                        } finally {
                                            input.close()
                                            Log.d("FileUpload", "InputStream بسته شد")
                                        }
                                    }

                                    "pending" -> {
                                        Toast.makeText(
                                            context,
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    "error" -> {
                                        Toast.makeText(
                                            context,
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            "shutdown" -> {
                                _loggedIn.value = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("error", e.toString())
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
                _usernameState.value = username
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
                _usernameState.value = username
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
        _contactSearchList.value = emptyList<Contact>()
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

    fun sendMessage(contact: String, message: List<ContentEntity>/*message: String*/) {
        // id رو باید درست هندل کنیم که با دوتا پیام بدون نت کرش نکنه
        val contentArray = JSONArray()

        message.forEach { content ->
            contentArray.put(
                JSONObject().apply {
                    put("type", content.type)
                    if (content.type == "text") {
                        put("text", content.text)
                    } else {
                        put("file_id", content.id)
                        put("file_name", content.fileName)
                    }
                }
            )
        }
        _messageList.value += MessageItem(
            //text = message,
            content = message,
            myMessage = true,
            id = 0,
            date = getCurrentUtcTimestamp(),
            seen = false
        )
        _shouldScrollToBottom.value = true
        viewModelScope.launch {
            try {
                val json = JSONObject().apply {
                    put("type", "send_dm")
                    put("recipient", contact)
                    put("content", contentArray)
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

    fun onScrolledToBottom() {
        _shouldScrollToBottom.value = false
    }

    fun getUploadUri(name: String, size: Long, hash: String, uri: Uri?) {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "file_upload_request")
                        put("name", name)
                        put("size", size)
                        put("hash", hash)
                    }.toString()
                )
                _uploads.value += File(
                    name = name,
                    localUri = uri
                )
            } catch (_: Exception) {
            }
        }
    }
}