package ir.gchat

import android.content.Context
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

data class Contact(
    val id: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val lastMessageText: String = "",
    val lastMessageDate: String = "",
    val unreadMessages: Int = 0,
    val connectionStatus: Boolean = false,
)

data class SearchEntity(
    val username: String, val isOnline: Boolean
)

data class MessageItem(
    val text: String = "",
    val id: Int = 0,
    val date: String = "",
    val myMessage: Boolean = false,
    val seen: Boolean = false
)