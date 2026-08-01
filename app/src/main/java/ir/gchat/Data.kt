package ir.gchat

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.graphics.Color

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
    val lastMessageText: String,
    val lastMessageDate: String,
    val unreadMessages: Int,
    val isOnline: Boolean
)

data class MessageItem(
    val text: String,
    val id: Int,
    val date: String,
    val myMessage: Boolean,
    val seen: Boolean
)

data class Palette(
    val primary: Color,
    val onPrimary: Color
)