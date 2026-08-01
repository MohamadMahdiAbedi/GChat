package ir.gchat

import android.app.Application
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _theme = MutableStateFlow(0)
    val theme: StateFlow<Int> = _theme.asStateFlow()

    private val _palette = MutableStateFlow(8)
    val palette: StateFlow<Int> = _palette.asStateFlow()

    init {
        viewModelScope.launch {
            _theme.value = context.dataStore.data.map { it[THEME_KEY] ?: 0 }.first()
            _palette.value = context.dataStore.data.map { it[PALETTE_KEY] ?: 8 }.first()
        }
    }

    fun setTheme() {
        _theme.value = when (_theme.value) {
            0 -> 1 // System -> Dark
            1 -> 2 // Dark -> Light
            2 -> 0 // Light -> System
            else -> 0
        }

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[THEME_KEY] = _theme.value
            }
        }
    }

    suspend fun getDevice(): Int {
        return context.dataStore.data.map { it[DEVICE_TYPE_KEY] ?: 2 }.first()
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

    private val _pendingIntent = MutableStateFlow<Intent?>(null)
    val pendingIntent: StateFlow<Intent?> = _pendingIntent

    fun setPendingIntent(intent: Intent?) {
        _pendingIntent.value = intent
    }
}