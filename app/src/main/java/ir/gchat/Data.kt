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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data")

val ICCID_KEY = stringPreferencesKey("iccid")
val LOGGED_IN_STATUS_KEY = booleanPreferencesKey("loggedIn")
val THEME_KEY = intPreferencesKey("theme")
val SERVERIP_KEY = stringPreferencesKey("serverIP")
val DEVICE_TYPE_KEY = intPreferencesKey("deviceTypeIP")
val USERNAME_KEY = stringPreferencesKey("username")
val PASSWORD_KEY = stringPreferencesKey("password")
val PALETTE_KEY = intPreferencesKey("palette")

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

    data class Text(val text: String) : Draft()
}

sealed class Content {
    data class Text(val text: String) : Content()

    data class File(
        val id: Int,
        val fileName: String,
        var progress: Float = 0f,
        var fileSize: Long
    ) : Content()
}