package ir.gchat

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.LocaleListCompat
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

    private val _palette = MutableStateFlow(19)
    val palette: StateFlow<Int> = _palette.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _sendWith = MutableStateFlow(SendMessageWith())
    val sendWith: StateFlow<SendMessageWith> = _sendWith.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(false)
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    init {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()

            _theme.value = preferences[THEME_KEY] ?: 0
            _useDynamicColor.value = preferences[USE_DYNAMIC_COLOR] ?: false
            _palette.value = preferences[PALETTE_KEY] ?: 19

            preferences[CUSTOM_PRIMARY_COLOR_KEY]?.let { argb ->
                val color = Color(argb)

                materialPalette[19] = Palette(
                    primary = color,
                    onPrimary = if (color.luminance() >= 0.5f) {
                        Color.Black
                    } else {
                        Color.White
                    }
                )
            }

            _ready.value = true

            val enter = preferences[SEND_WITH_ENTER] ?: false
            val shiftEnter = preferences[SEND_WITH_SHIFT] ?: false
            val ctrlEnter = preferences[SEND_WITH_CTRL] ?: false
            val altEnter = preferences[SEND_WITH_ALT] ?: false

            _sendWith.value = SendMessageWith(
                enter = enter,
                shiftEnter = shiftEnter,
                ctrlEnter = ctrlEnter,
                altEnter = altEnter
            )
        }
    }

    private val _lightNavBar: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val lightNavBar: StateFlow<Boolean?> = _lightNavBar.asStateFlow()

    fun setSendWith(value: SendMessageWith) {
        _sendWith.value = value

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[SEND_WITH_ENTER] = value.enter
                preferences[SEND_WITH_SHIFT] = value.shiftEnter
                preferences[SEND_WITH_CTRL] = value.ctrlEnter
                preferences[SEND_WITH_ALT] = value.altEnter
            }
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean) {
        _useDynamicColor.value = useDynamicColor
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[USE_DYNAMIC_COLOR] = useDynamicColor
            }
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

    fun setColor(index: Int) {
        _palette.value = index

        viewModelScope.launch {
            context.dataStore.edit {
                it[PALETTE_KEY] = index
                if (index == 19/*materialPalette[_palette.value] == materialPalette[19]*/) {
                    it[CUSTOM_PRIMARY_COLOR_KEY] = materialPalette[19].primary.toArgb()
                }
            }
        }
    }

    fun setNavBarTheme(light: Boolean?) {
        _lightNavBar.value = light
    }
}