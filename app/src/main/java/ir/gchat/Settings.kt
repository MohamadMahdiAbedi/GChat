package ir.gchat

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(setTheme: () -> Unit, theme: Int, navHostController: NavHostController) {
    val view = LocalView.current
    var dropdownThemeText by remember { mutableStateOf("System") }
    var dropdownThemeIcon by remember { mutableIntStateOf(R.drawable.auto) }

    when (theme) {
        0 -> {
            dropdownThemeText = "System"
            dropdownThemeIcon = R.drawable.auto
        }

        1 -> {
            dropdownThemeText = "Dark"
            dropdownThemeIcon = R.drawable.dark_mode
        }

        2 -> {
            dropdownThemeText = "Light"
            dropdownThemeIcon = R.drawable.light_mode
        }

        else -> {
            dropdownThemeText = "System"
            dropdownThemeIcon = R.drawable.auto
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        //.shadow(
        //    elevation = 16.dp,
        //    clip = false
        //),
        topBar = {
            TopAppBar(
                title = {
                    Text("Appearance")
                }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            navHostController.popBackStack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back"
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Theme",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            setTheme()
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(dropdownThemeIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dropdownThemeText,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Tap to next",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Colors",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            setTheme()
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(dropdownThemeIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dropdownThemeText,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Tap to next",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navHostController: NavHostController) {
    val view = LocalView.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        //.shadow(
        //    elevation = 16.dp,
        //    clip = false
        //),
        topBar = {
            TopAppBar(
                title = {
                    Text("Setting")
                }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            navHostController.popBackStack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back"
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            navHostController.navigate("appearanceSettings")
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.imagesearch_roller),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Appearance",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Theme, colors and fonts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.tabs),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Folders",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    // اینجا اسم چند تا پوشه رو بگیره بنویسه
                                    text = "Categorize and organize conversations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.translate),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Languages",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "English, فارسی, عربی",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.notification),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp).size(24.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Sound, vibration and alerts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpConfig(
    serverIP: String,
    device: suspend () -> Int,
    back: () -> Unit,
    setDevice: (Int) -> Unit,
    setServerIP: (String) -> Unit
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    var host by remember { mutableStateOf(serverIP.split(":")[0]) }
    var port by remember { mutableStateOf(serverIP.split(":")[1]) }

    val portNumber = port.toIntOrNull()
    val portError = portNumber == null || portNumber > 65535 || portNumber < 1

    val networkProtocols = listOf("IPv4", "IPv6", "Domain", "Localhost", "LocalServer")
    var networkProtocol by remember { mutableStateOf(networkProtocols[0]) }
    var expanded by remember { mutableStateOf(false) }
    val devices = listOf("Android Studio Emulator (AVD)", "Genymotion", "Other Devices")
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
                "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,7}:$|" + "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" + "^[0-9a-fA-F]{1,4}::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,5}:([0-9a-fA-F]{1,4}:){1,5}$|" + "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Set server IP config")
                }, /*expandedHeight = 56.dp,*/ colors = TopAppBarDefaults.topAppBarColors(
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
                            back()
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
                        color = MaterialTheme.colorScheme.surface,
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

                                    "Other Devices" -> {
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
                            .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

                                    "Other Devices" -> {
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
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        })
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
                            if (!portError && !hostError) {
                                setServerIP("$host:$port")
                                back()
                            }
                        })
                )
            }

            val enabled = false

            ExposedDropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                expanded = expanded,
                onExpandedChange = {
                    if (enabled) expanded = !expanded
                }) {
                TextField(
                    value = networkProtocol,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
                    readOnly = true,
                    enabled = enabled,
                    label = { Text("Protocol") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    )
                )

                ExposedDropdownMenu(
                    expanded = enabled && expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(2.dp)
                ) {
                    networkProtocols.forEach { protocol ->
                        DropdownMenuItem(text = { Text(protocol) }, onClick = {
                            networkProtocol = protocol
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
                    setServerIP("$host:$port")
                    back()
                },
                enabled = !portError && !hostError,
                modifier = Modifier
                    .padding(8.dp)
                    .size(56.dp)
                    .align(Alignment.BottomEnd)
                    .shadow(
                        elevation = if (!portError && !hostError) 6.dp else 0.dp,
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