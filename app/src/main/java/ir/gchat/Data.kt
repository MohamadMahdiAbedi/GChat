package ir.gchat

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data")

val ICCID_KEY = stringPreferencesKey("iccid")
val LOGGED_IN_STATUS_KEY = booleanPreferencesKey("loggedIn")
val THEME_KEY = intPreferencesKey("theme")
val SERVERIP_KEY = stringPreferencesKey("serverIP")
val DEVICE_TYPE_KEY = intPreferencesKey("deviceTypeIP")
val USERNAME_KEY = stringPreferencesKey("username")
val PASSWORD_KEY = stringPreferencesKey("password")
val PALETTE_KEY = intPreferencesKey("palette")
val CUSTOM_PRIMARY_COLOR_KEY = intPreferencesKey("custom_color")
val SEND_WITH_ENTER = booleanPreferencesKey("sendWithEnter")
val SEND_WITH_SHIFT = booleanPreferencesKey("sendWithShift")
val SEND_WITH_CTRL = booleanPreferencesKey("sendWithCtrl")
val SEND_WITH_ALT = booleanPreferencesKey("sendWithAlt")
val USE_DYNAMIC_COLOR = booleanPreferencesKey("useDynamicColor")

data class Contact(
    val id: String,
    val name: String,
    val profilePicture: String = "",
    val lastMessageContent: List<Content>,
    val lastMessageDate: String,
    val unreadMessages: Int,
    val isOnline: Boolean
)

data class MessageItem(
    val content: List<Content>,
    val id: Int,
    val date: String,
    val myMessage: Boolean,
    val seen: Boolean
)

data class Palette(
    val primary: Color,
    val onPrimary: Color
)

sealed class Draft {
    data class File(
        val name: String,
        val id: Int = 0,
        val localUri: Uri?,
        val thumbUrl: String = "",
        val progress: Float = 0f,
        val size: Long
    ) : Draft()

    data class Text(val text: String, val id: Int) : Draft()

    data class LaTeX(val text: String, val id: Int) : Draft()
}

sealed class Content {
    data class Text(val text: String) : Content()

    data class LaTeX(val text: String) : Content()

    data class File(
        val id: Int,
        val fileName: String,
        var progress: Float = 0f,
        var fileSize: Long
    ) : Content()
}

data class SendMessageWith(
    val enter: Boolean = false,
    val shiftEnter: Boolean = false,
    val ctrlEnter: Boolean = false,
    val altEnter: Boolean = false
)

fun calculateFileHash(file: File): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        FileInputStream(file).use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}