package ir.gchat

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.BufferedSink
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class SocketViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    var webSocket: WebSocket? = null
    /*private*/ val client = OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS)
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

    private val _loginResponded = MutableStateFlow(false)
    val loginResponse: StateFlow<Boolean> = _loginResponded.asStateFlow()

    private val _chatList = MutableStateFlow(emptyList<Contact>())
    val chatList: StateFlow<List<Contact>> = _chatList.asStateFlow()

    private var _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _contactSearchList = MutableStateFlow(emptyList<Contact>())
    val contactSearchList: StateFlow<List<Contact>> = _contactSearchList.asStateFlow()

    private val _messageList = MutableStateFlow(emptyList<MessageItem>())
    val messageList: StateFlow<List<MessageItem>> = _messageList.asStateFlow()

    private var openedChat = MutableStateFlow("")

    private var _shouldScrollToBottom = MutableStateFlow(false)
    val shouldScrollToBottom: StateFlow<Boolean> = _shouldScrollToBottom.asStateFlow()

    private var _drafts = MutableStateFlow<Map<String, List<Draft>>>(emptyMap())
    val drafts: StateFlow<Map<String, List<Draft>>> = _drafts.asStateFlow()

    private var _savedText = MutableStateFlow<Map<String, Triple<Int?, String, List<Triple<Int, Int, String>>>>>(emptyMap())
    val savedText: StateFlow<Map<String, Triple<Int?, String, List<Triple<Int, Int, String>>>>> = _savedText.asStateFlow()

    fun connect() {
        if (webSocket != null) {
            return
        }
        val request = Request.Builder().url("ws://${_serverIP.value}").build()
        //val hostPort = _serverIP.value

        //val url = if (hostPort.count { it == ':' } > 1) {
        //    // IPv6
        //    val lastColon = hostPort.lastIndexOf(':')
        //    val host = hostPort.substring(0, lastColon)
        //    val port = hostPort.substring(lastColon + 1)

        //    "ws://[$host]:$port"
        //} else {
        //    // IPv4 / hostname
        //    "ws://$hostPort"
        //}

        //val request = Request.Builder()
        //    .url(url)
        //    .build()
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
                                        _loginResponded.value = !_loginResponded.value
                                        _oldLoggedIn.value = true
                                        _loggedIn.value = true
                                        _token.value = jsonObject.getString("token")
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = true
                                        }
                                    }

                                    "iccid_error" -> {
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
                                        Toast.makeText(
                                            context, "Invalid input size", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }

                                    "iccid_invalid" -> {
                                        _loginResponded.value = !_loginResponded.value
                                        Toast.makeText(
                                            context, "Invalid ICCID", Toast.LENGTH_SHORT
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
                                        _loginResponded.value = !_loginResponded.value
                                        _oldLoggedIn.value = true
                                        _loggedIn.value = true
                                        _token.value = jsonObject.getString("token")
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = true
                                        }
                                    }

                                    "password_error" -> {
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
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
                                        _loginResponded.value = !_loginResponded.value
                                        Toast.makeText(
                                            context, "Invalid input size", Toast.LENGTH_SHORT
                                        ).show()
                                        _oldLoggedIn.value = false
                                        _loggedIn.value = false
                                        context.dataStore.edit { preferences ->
                                            preferences[LOGGED_IN_STATUS_KEY] = false
                                        }
                                    }

                                    "iccid_invalid" -> {
                                        _loginResponded.value = !_loginResponded.value
                                        Toast.makeText(
                                            context, "Invalid ICCID", Toast.LENGTH_SHORT
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
                                            lastMessageContent = emptyList<Content>(),
                                            lastMessageDate = "",
                                            unreadMessages = 0,
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Search error", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                            "get_messages_response" -> {
                                _messageList.value = emptyList()

                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("messages")

                                    for (i in 0 until results.length()) {
                                        val item = results.getJSONObject(i)

                                        val contentArray = item.getJSONArray("content")
                                        val content = buildList {
                                            for (j in 0 until contentArray.length()) {
                                                val obj = contentArray.getJSONObject(j)

                                                when (obj.getString("type")) {
                                                    "text" -> add(
                                                        Content.Text(
                                                            text = obj.optString("text"),
                                                            markDown = obj.optJSONArray("mark_down")?.let { array ->
                                                                (0 until array.length()).map { i ->
                                                                    val item = array.getJSONArray(i)
                                                                    Triple(
                                                                        item.getInt(0),
                                                                        item.getInt(1),
                                                                        item.getString(2)
                                                                    )
                                                                }
                                                            } ?: emptyList()
                                                        )
                                                    )

                                                    "latex" -> add(
                                                        Content.LaTeX(
                                                            text = obj.optString("text")
                                                        )
                                                    )


                                                    "file" -> add(
                                                        Content.File(
                                                            id = obj.optInt("file_id"),
                                                            fileName = obj.optString("name"),
                                                            fileSize = obj.optLong("size")
                                                        )
                                                    )
                                                }
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
                                        context, "Error Receiving messages", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            "get_conversations_response" -> {
                                _chatList.value = emptyList<Contact>()
                                if (jsonObject.getString("status") == "success") {
                                    val results = jsonObject.getJSONArray("conversations")
                                    for (i in 0 until results.length()) {
                                        val chat = results.getJSONObject(i)

                                        val lastMessage = chat.getJSONArray("last_message")
                                        val lastMessageContent = mutableListOf<Content>()
                                        for (j in 0 until lastMessage.length()) {
                                            val item = lastMessage.getJSONObject(j)
                                            when (item.getString("type")) {
                                                "text" -> {
                                                    lastMessageContent += Content.Text(
                                                        text = item.getString("text"),
                                                        markDown = item.optJSONArray("mark_down")?.let { array ->
                                                            (0 until array.length()).map { i ->
                                                                val item = array.getJSONArray(i)
                                                                Triple(
                                                                    item.getInt(0),
                                                                    item.getInt(1),
                                                                    item.getString(2)
                                                                )
                                                            }
                                                        } ?: emptyList()
                                                    )
                                                }

                                                "latex" -> {
                                                    val latex = item.optString("text", "")

                                                    Log.d(
                                                        "LATEX",
                                                        "raw item=$item | latex='$latex' | length=${latex.length}"
                                                    )

                                                    lastMessageContent += Content.LaTeX(
                                                        text = latex
                                                    )
                                                }

                                                "file" -> {
                                                    lastMessageContent += Content.File(
                                                        id = item.getInt("file_id"),
                                                        fileName = item.getString("name"),
                                                        fileSize = item.getLong("size")
                                                    )
                                                }
                                            }
                                        }

                                        _chatList.value += Contact(
                                            id = chat.getString("with"),
                                            name = chat.getString("with"),
                                            lastMessageContent = lastMessageContent,
                                            lastMessageDate = chat.getString("last_timestamp"),
                                            unreadMessages = chat.getInt("unread_count"),
                                            isOnline = chat.getBoolean("online"),
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context, "Error Receiving messages", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            "new_message" -> {
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
                                        val name = jsonObject.getString("name")
                                        val fileId = jsonObject.getInt("file_id")

                                        val thumbUrl = if (jsonObject.has("thumb_url")) {
                                            jsonObject.getString("thumb_url")
                                        } else {
                                            null
                                        }

                                        _drafts.update { draftsMap ->
                                            val drafts = draftsMap[openedChat.value]
                                                ?: return@update draftsMap

                                            draftsMap + (openedChat.value to drafts.map { draft ->
                                                if (draft is Draft.File && draft.name == name) {
                                                    draft.copy(
                                                        id = fileId,
                                                        progress = 1f,
                                                        thumbUrl = thumbUrl ?: draft.thumbUrl
                                                    )
                                                } else {
                                                    draft
                                                }
                                            })
                                        }
                                    }

                                    "new" -> {
                                        val file =
                                            _drafts.value[openedChat.value]?.filterIsInstance<Draft.File>()
                                                ?.find {
                                                    it.name == jsonObject.getString("name")
                                                }

                                        if (file == null) {
                                            Log.e(
                                                "FileUpload",
                                                "فایل پیدا نشد با نام: ${jsonObject.getString("name")}"
                                            )
                                            return@launch
                                        }

                                        val uri = file.localUri

                                        if (uri == null) {
                                            Log.e("FileUpload", "localUri خالی است")
                                            return@launch
                                        }

                                        val input = context.contentResolver.openInputStream(uri)

                                        if (input == null) {
                                            Log.e(
                                                "FileUpload",
                                                "نتونستم InputStream باز کنم برای uri: $uri"
                                            )
                                            return@launch
                                        }

                                        try {
                                            val fileBytes = input.readBytes()
                                            val fileSize = fileBytes.size

                                            Log.d(
                                                "FileUpload",
                                                "حجم فایل خوانده شده: $fileSize بایت (${fileSize / 1024 / 1024f} مگابایت)"
                                            )

                                            val mimeType =
                                                context.contentResolver.getType(uri) ?: run {
                                                    val extension =
                                                        file.name.substringAfterLast('.', "")
                                                            .lowercase()

                                                    android.webkit.MimeTypeMap.getSingleton()
                                                        .getMimeTypeFromExtension(extension)
                                                        ?: "application/octet-stream"
                                                }

                                            Log.d(
                                                "FileUpload", "MIME Type تشخیص داده شده: $mimeType"
                                            )

                                            _drafts.update { draftsMap ->
                                                val drafts = draftsMap[openedChat.value]
                                                    ?: return@update draftsMap

                                                draftsMap + (openedChat.value to drafts.map { draft ->
                                                    if (draft is Draft.File && draft.name == file.name) {
                                                        draft.copy(progress = 0f)
                                                    } else {
                                                        draft
                                                    }

                                                })
                                            }
                                            val progressRequestBody = object : RequestBody() {

                                                override fun contentType(): MediaType {
                                                    return mimeType.toMediaType()
                                                }

                                                override fun contentLength(): Long {
                                                    return fileBytes.size.toLong()
                                                }

                                                override fun writeTo(sink: BufferedSink) {
                                                    val total = fileBytes.size.toLong()
                                                    var uploaded = 0L

                                                    val bufferSize = when {
                                                        fileSize < 1024 * 1024 -> 8192
                                                        fileSize < 100 * 1024 * 1024 -> 65536
                                                        else -> 262144
                                                    }
                                                    var offset = 0

                                                    while (offset < fileBytes.size) {
                                                        val count = minOf(
                                                            bufferSize, fileBytes.size - offset
                                                        )

                                                        sink.write(
                                                            fileBytes, offset, count
                                                        )

                                                        offset += count
                                                        uploaded += count

                                                        _drafts.update { draftsMap ->
                                                            val drafts = draftsMap[openedChat.value]
                                                                ?: return@update draftsMap

                                                            draftsMap + (openedChat.value to drafts.map { draft ->
                                                                if (draft is Draft.File && draft.name == file.name) {
                                                                    val roundedProgress =
                                                                        (uploaded.toFloat() / total.toFloat() * 100).toInt() / 100f
                                                                    if (draft.progress != roundedProgress) {
                                                                        draft.copy(progress = roundedProgress)
                                                                    } else {
                                                                        draft
                                                                    }
                                                                } else {
                                                                    draft
                                                                }
                                                            })
                                                        }
                                                    }
                                                }
                                            }

                                            val requestBody =
                                                MultipartBody.Builder().setType(MultipartBody.FORM)
                                                    .addFormDataPart(
                                                        "file", file.name, progressRequestBody
                                                    ).build()

                                            val originalUrl = jsonObject.getString("upload_url")

                                            val finalUrl = originalUrl.replace(
                                                "0.0.0.0", serverIP.value.split(":")[0]
                                            )

                                            Log.d(
                                                "FileUpload", "URL اصلی: $originalUrl"
                                            )

                                            Log.d(
                                                "FileUpload", "URL نهایی: $finalUrl"
                                            )

                                            val request = Request.Builder().url(finalUrl).header(
                                                "Authorization", "Bearer ${_token.value}"
                                            ).post(requestBody).build()

                                            Log.d(
                                                "FileUpload",
                                                "درخواست آپلود ساخته شد، شروع ارسال..."
                                            )

                                            /*OkHttpClient()*/client.newCall(request)
                                                .enqueue(object : Callback {

                                                    override fun onFailure(
                                                        call: Call, e: IOException
                                                    ) {
                                                        Log.e(
                                                            "FileUpload",
                                                            "آپلود شکست خورد (onFailure)",
                                                            e
                                                        )

                                                        showToast(
                                                            message = "Upload failed"
                                                        )

                                                        removeFileFromDraft(fileName = file.name)
                                                    }

                                                    override fun onResponse(
                                                        call: Call, response: Response
                                                    ) {
                                                        response.use {

                                                            val code = it.code
                                                            val bodyString = it.body.string()

                                                            Log.d(
                                                                "FileUpload",
                                                                "پاسخ سرور: code=$code"
                                                            )

                                                            when (code) {
                                                                200 -> {
                                                                    try {
                                                                        val result =
                                                                            JSONObject(bodyString)

                                                                        if (result.optString("status") != "success") {
                                                                            showToast("آپلود ناموفق")
                                                                        }

                                                                        val fileId =
                                                                            result.getInt("file_id")

                                                                        val thumbUrl =
                                                                            result.optString(
                                                                                "thumb_url",
                                                                                ""
                                                                            )

                                                                        // فایل موقت
                                                                        val sourceFile = File(
                                                                            context.filesDir,
                                                                            "recordings/${file.name}"
                                                                        )

                                                                        // پوشه نهایی Downloads
                                                                        val targetDir = File(
                                                                            context.filesDir,
                                                                            "downloads"
                                                                        )

                                                                        targetDir.mkdirs()

                                                                        // گرفتن پسوند فایل
                                                                        val extension =
                                                                            sourceFile.extension

                                                                        // نام نهایی = file_id.extension
                                                                        val finalName =
                                                                            if (extension.isNotEmpty()) {
                                                                                "$fileId.$extension"
                                                                            } else {
                                                                                fileId.toString()
                                                                            }

                                                                        val targetFile = File(
                                                                            targetDir,
                                                                            finalName
                                                                        )

                                                                        // انتقال + تغییر نام
                                                                        val localFile = when {
                                                                            targetFile.exists() -> {
                                                                                if (sourceFile.exists()) {
                                                                                    sourceFile.delete()
                                                                                }

                                                                                targetFile
                                                                            }

                                                                            sourceFile.exists() -> {
                                                                                if (sourceFile.renameTo(
                                                                                        targetFile
                                                                                    )
                                                                                ) {
                                                                                    targetFile
                                                                                } else {
                                                                                    try {
                                                                                        sourceFile.copyTo(
                                                                                            targetFile,
                                                                                            overwrite = true
                                                                                        )
                                                                                        sourceFile.delete()
                                                                                        targetFile
                                                                                    } catch (_: Exception) {
                                                                                        null
                                                                                    }
                                                                                }
                                                                            }

                                                                            else -> null
                                                                        }

                                                                        // آپدیت Draft
                                                                        _drafts.update { draftsMap ->

                                                                            val chatId =
                                                                                openedChat.value

                                                                            val drafts =
                                                                                draftsMap[chatId]
                                                                                    ?: return@update draftsMap

                                                                            draftsMap + (
                                                                                    chatId to drafts.map { currentDraft ->

                                                                                        if (
                                                                                            currentDraft is Draft.File &&
                                                                                            currentDraft.name == file.name
                                                                                        ) {
                                                                                            currentDraft.copy(
                                                                                                id = fileId,
                                                                                                progress = 1f,
                                                                                                thumbUrl = thumbUrl,
                                                                                                localUri = localFile?.let { movedFile ->
                                                                                                    Uri.fromFile(
                                                                                                        movedFile
                                                                                                    )
                                                                                                }
                                                                                                    ?: currentDraft.localUri
                                                                                            )
                                                                                        } else {
                                                                                            currentDraft
                                                                                        }
                                                                                    }
                                                                                    )
                                                                        }

                                                                    } catch (e: Exception) {
                                                                        Log.e(
                                                                            "FileUpload",
                                                                            "Error",
                                                                            e
                                                                        )
                                                                    }
                                                                }

                                                                404 -> {
                                                                    showToast("Invalid upload token")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }

                                                                410 -> {
                                                                    showToast("Upload token has expired")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }

                                                                413 -> {
                                                                    showToast("File is too large")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }

                                                                415 -> {
                                                                    showToast("Unsupported file type")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }

                                                                500 -> {
                                                                    showToast("Server error")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }

                                                                else -> {
                                                                    showToast("Upload failed: $code")
                                                                    removeFileFromDraft(fileName = file.name)
                                                                    return
                                                                }
                                                            }

                                                            Log.d(
                                                                "FileUpload",
                                                                "بدنه پاسخ: $bodyString"
                                                            )

                                                            if (!it.isSuccessful) {
                                                                Log.e(
                                                                    "FileUpload",
                                                                    "آپلود ناموفق - کد: $code"
                                                                )
                                                                return
                                                            }

                                                            try {
                                                                val result = JSONObject(bodyString)

                                                                if (result.getString("status") == "success") {
                                                                    _drafts.update { draftsMap ->
                                                                        val drafts =
                                                                            draftsMap[openedChat.value]
                                                                                ?: return@update draftsMap

                                                                        draftsMap + (openedChat.value to drafts.map { draft ->
                                                                            if (draft is Draft.File && draft.name == file.name) {
                                                                                draft.copy(
                                                                                    id = result.getInt(
                                                                                        "file_id"
                                                                                    ),
                                                                                    progress = 1f,
                                                                                    thumbUrl = result.optString(
                                                                                        "thumb_url"
                                                                                    )
                                                                                )
                                                                            } else {
                                                                                draft
                                                                            }
                                                                        })
                                                                    }

                                                                    Log.d(
                                                                        "FileUpload",
                                                                        "Upload successful! file_id=${
                                                                            result.getInt(
                                                                                "file_id"
                                                                            )
                                                                        }, " + "thumb=${
                                                                            result.optString(
                                                                                "thumb_url"
                                                                            )
                                                                        }"
                                                                    )
                                                                } else {
                                                                    Log.e(
                                                                        "FileUpload",
                                                                        "سرور status=success نداد: $bodyString"
                                                                    )
                                                                }

                                                            } catch (e: Exception) {
                                                                Log.e(
                                                                    "FileUpload",
                                                                    "خطا در پارس کردن پاسخ JSON",
                                                                    e
                                                                )
                                                            }
                                                        }
                                                    }
                                                })

                                        } catch (e: Exception) {
                                            Log.e(
                                                "FileUpload",
                                                "خطا هنگام خواندن فایل یا ساخت درخواست",
                                                e
                                            )
                                        } finally {
                                            input.close()

                                            Log.d(
                                                "FileUpload", "InputStream بسته شد"
                                            )
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
                                // بهتره اپ رو ببندیم
                                this@SocketViewModel.webSocket = null
                                _loggedIn.value = false
                                viewModelScope.launch {
                                    delay(1000.milliseconds)
                                    connect()
                                }
                            }

                            "delete_message_response" -> {
                                getConversations()
                                getMessagesList(openedChat.value)
                            }

                            "message_deleted" -> {
                                getConversations()
                                getMessagesList(openedChat.value)
                            }

                            "message_edited" -> {
                                getConversations()
                                getMessagesList(openedChat.value)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Error", e.toString())
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

    fun sendMessage(contact: String, message: List<Content>/*message: String*/) {
        // id رو باید درست هندل کنیم که با دوتا پیام بدون نت کرش نکنه
        val contentArray = JSONArray()

        message.forEach { content ->
            when (content) {
                is Content.Text -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "text")
                            put("text", content.text)
                            put(
                                "mark_down",
                                JSONArray().apply {
                                    content.markDown.forEach { (start, end, type) ->
                                        put(
                                            JSONArray().apply {
                                                put(start)
                                                put(end)
                                                put(type)
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    )
                }

                is Content.File -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "file")
                            put("file_id", content.id)
                            put("name", content.fileName)
                        })
                }

                is Content.LaTeX -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "latex")
                            put("text", content.text)
                        })
                }
            }
        }
        _messageList.value += MessageItem(
            //text = message,
            content = message,
            myMessage = true,
            id = -System.currentTimeMillis().toInt(),
            //id = 0,
            date = getCurrentUtcTimestamp(),
            seen = false
        )
        _shouldScrollToBottom.value = true
        viewModelScope.launch {
            try {
                val json = JSONObject().apply {
                    put("type", "send_message")
                    put("recipient", contact)
                    put("content", contentArray)
                }
                webSocket?.send(json.toString())
            } catch (_: Exception) {
                // log error
            }
        }
        getMessagesList(contact)
        getConversations()
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
        //دوتا شد
        openedChat.value = contact
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "get_chunk")
                        put("with", contact)
                        put("chunk", "unread")
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun deleteMessage(id: Int) {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "delete_message")
                        put("id", id)
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
                _drafts.update { draftsMap ->
                    val chatId = openedChat.value
                    val drafts = draftsMap[chatId].orEmpty()

                    draftsMap + (chatId to (drafts + Draft.File(
                        name = name, localUri = uri, size = size
                    )))
                }
            } catch (_: Exception) {
            }
        }
    }

    fun downloadFile(
        fileId: Int,
        fileName: String,
        fileSize: Long,
        setProgress: (Float) -> Unit,
        setPending: (Boolean) -> Unit,
        setDownloadedBytes: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            setPending(true)
            setProgress(0f)

            try {
                val host = serverIP.value.substringBefore(":")
                val url = "http://$host:8080/files/$fileId"

                val token = _token.value

                Log.d("Download", "URL = $url")
                Log.d("Download", "TOKEN = $token")

                val request =
                    Request.Builder().url(url).header("Authorization", "Bearer $token").build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        Log.e(
                            "Download", "HTTP ${response.code}: ${response.message}"
                        )
                        return@launch
                    }

                    val body = response.body

                    val contentLength = body.contentLength()

                    // بعضی سرورها Content-Length را ارسال نمی‌کنند
                    val totalBytes = when {
                        contentLength > 0L -> contentLength
                        fileSize > 0L -> fileSize
                        else -> -1L
                    }

                    Log.d(
                        "Download",
                        "Content-Length=$contentLength, fileSize=$fileSize, total=$totalBytes"
                    )

                    // =========================================================
                    // Internal Storage
                    // =========================================================

                    val downloadsDirectory = File(
                        context.filesDir, "downloads"
                    )

                    if (!downloadsDirectory.exists()) {
                        if (!downloadsDirectory.mkdirs() && !downloadsDirectory.exists()) {
                            throw IOException(
                                "Could not create internal downloads directory"
                            )
                        }
                    }

                    var targetFile = File(
                        downloadsDirectory, fileName
                    )

                    /*
                     * اگر فایل همنام وجود داشت، اسم جدید بساز.
                     */
                    if (targetFile.exists()) {

                        val baseName = targetFile.nameWithoutExtension
                        val extension = targetFile.extension

                        var index = 1

                        do {
                            val newName = if (extension.isEmpty()) {
                                "$baseName ($index)"
                            } else {
                                "$baseName ($index).$extension"
                            }

                            targetFile = File(
                                downloadsDirectory, newName
                            )

                            index++

                        } while (targetFile.exists())
                    }

                    Log.d(
                        "Download", "Saving to ${targetFile.absolutePath}"
                    )

                    try {

                        FileOutputStream(targetFile).use { outputStream ->

                            body.byteStream().use { inputStream ->

                                val buffer = ByteArray(32 * 1024)

                                var downloadedBytes = 0L
                                setDownloadedBytes(downloadedBytes)

                                var lastProgress = -1f

                                while (true) {

                                    val read = inputStream.read(buffer)

                                    if (read == -1) {
                                        break
                                    }

                                    outputStream.write(
                                        buffer, 0, read
                                    )

                                    downloadedBytes += read
                                    setDownloadedBytes(downloadedBytes)

                                    if (totalBytes > 0L) {

                                        val progress =
                                            (downloadedBytes.toDouble() / totalBytes.toDouble()).toFloat()
                                                .coerceIn(0f, 1f)

                                        /*
                                         * فقط زمانی UI را آپدیت می‌کنیم
                                         * که حداقل 1 درصد تغییر کرده باشد.
                                         */
                                        if (progress >= 1f || progress - lastProgress >= 0.01f) {
                                            lastProgress = progress

                                            Log.d(
                                                "Download",
                                                "progress=${(progress * 100).toInt()}% " + "($downloadedBytes/$totalBytes)"
                                            )

                                            setProgress(progress)
                                        }

                                    } else {

                                        // اندازه فایل مشخص نیست
                                        Log.d(
                                            "Download", "Downloaded=$downloadedBytes bytes"
                                        )
                                    }
                                }

                                outputStream.flush()
                            }
                        }

                        // دانلود کامل شده
                        setProgress(1f)

                        Log.d(
                            "Download", "Download finished: ${targetFile.absolutePath}"
                        )

                    } catch (e: Exception) {

                        // حذف فایل ناقص
                        if (targetFile.exists()) {
                            targetFile.delete()
                        }

                        throw e
                    }
                }

            } catch (e: CancellationException) {

                Log.d(
                    "Download", "Download cancelled: fileId=$fileId"
                )

                throw e

            } catch (e: Exception) {

                Log.e(
                    "Download", "Download failed: fileId=$fileId", e
                )

                setProgress(0f)

            } finally {

                setPending(false)
            }
        }
    }

    fun isFileDownloaded(fileName: String): Boolean {
        val file = File(
            context.filesDir, "downloads/$fileName"
        )

        return file.exists() && file.isFile
    }

    fun removeFileFromDraft(fileName: String) {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            val drafts = draftsMap[chatId] ?: return@update draftsMap

            draftsMap + (chatId to drafts.filter { draft ->
                draft !is Draft.File || draft.name != fileName
            })
        }
    }

    fun removeTextFromDraft(id: Int) {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            val drafts = draftsMap[chatId] ?: return@update draftsMap

            draftsMap + (chatId to drafts.filter { draft ->
                draft !is Draft.Text || draft.id != id
            })
        }
    }

    fun clearDraft() {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            draftsMap + (chatId to emptyList())
        }
    }

    fun seenAll(id: String) {
        viewModelScope.launch {
            try {
                webSocket?.send(
                    JSONObject().apply {
                        put("type", "mark_read")
                        put("id", id)
                    }.toString()
                )
            } catch (_: Exception) {
            }
        }
    }

    fun setSavedText(id: String, message: String, editingId: Int?, markDown: List<Triple<Int, Int, String>>) {
        _savedText.update { current ->
            current + (id to Triple(editingId, message, markDown))
        }
        println(_savedText.value.toString())
    }

    fun attachTextBlock(text: String) {
        _drafts.update { draftsMap ->
            val chatId = openedChat.value
            val drafts = draftsMap[chatId] ?: emptyList()

            val newId = (drafts.filterIsInstance<Draft.Text>().maxOfOrNull { it.id } ?: -1) + 1

            draftsMap + (chatId to (drafts + Draft.Text(text = text, markDown = emptyList(), id = newId)))
        }
    }

    fun editTextInDraft(id: Int, newText: String) {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            val drafts = draftsMap[chatId] ?: return@update draftsMap

            draftsMap + (chatId to drafts.map { draft ->
                if (draft is Draft.Text && draft.id == id) {
                    draft.copy(text = newText)
                } else {
                    draft
                }
            })
        }
    }

    fun removeLaTeXFromDraft(id: Int) {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            val drafts = draftsMap[chatId] ?: return@update draftsMap

            draftsMap + (chatId to drafts.filter { draft ->
                draft !is Draft.LaTeX || draft.id != id
            })
        }
    }

    fun attachLaTeX(text: String) {
        _drafts.update { draftsMap ->
            val chatId = openedChat.value
            val drafts = draftsMap[chatId] ?: emptyList()

            val newId = (drafts.filterIsInstance<Draft.LaTeX>().maxOfOrNull { it.id } ?: -1) + 1

            draftsMap + (chatId to (drafts + Draft.LaTeX(text, id = newId)))
        }
    }

    fun editLaTeXInDraft(id: Int, newText: String) {
        val chatId = openedChat.value

        _drafts.update { draftsMap ->
            val drafts = draftsMap[chatId] ?: return@update draftsMap

            draftsMap + (chatId to drafts.map { draft ->
                if (draft is Draft.LaTeX && draft.id == id) {
                    draft.copy(text = newText)
                } else {
                    draft
                }
            })
        }
    }

    // id دادن به فایل ها و لاتکس ها و متن ها رو یکپارچه کن اینجا باگ داره
    fun attachFileWithId(id: Int, fileName: String, fileSize: Long) {
        _drafts.update { draftsMap ->
            val chatId = openedChat.value
            val drafts = draftsMap[chatId] ?: emptyList()

            draftsMap + (chatId to (drafts + Draft.File(name = fileName,id = id, localUri = null, size = fileSize, progress = 1f)))
        }
    }

    fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context, message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun editMessage(id: Int, message: List<Content>/*message: String*/) {
        // id رو باید درست هندل کنیم که با دوتا پیام بدون نت کرش نکنه
        val contentArray = JSONArray()

        message.forEach { content ->
            when (content) {
                is Content.Text -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "text")
                            put("text", content.text)
                            put(
                                "mark_down",
                                JSONArray().apply {
                                    content.markDown.forEach { (start, end, type) ->
                                        put(
                                            JSONArray().apply {
                                                put(start)
                                                put(end)
                                                put(type)
                                            }
                                        )
                                    }
                                }
                            )
                        })
                }

                is Content.File -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "file")
                            put("file_id", content.id)
                            put("name", content.fileName)
                        })
                }

                is Content.LaTeX -> {
                    contentArray.put(
                        JSONObject().apply {
                            put("type", "latex")
                            put("text", content.text)
                        })
                }
            }
        }
        _messageList.value += MessageItem(
            //text = message,
            content = message,
            myMessage = true,
            id = -System.currentTimeMillis().toInt(),
            //id = 0,
            date = getCurrentUtcTimestamp(),
            seen = false
        )
        _shouldScrollToBottom.value = true
        viewModelScope.launch {
            try {
                val json = JSONObject().apply {
                    put("type", "edit_message")
                    put("id", id)
                    put("content", contentArray)
                }
                webSocket?.send(json.toString())
            } catch (_: Exception) {
                // log error
            }
        }
        // بعدا یه چیز درست حسابی بده
        getMessagesList(openedChat.value)
        getConversations()
    }
}