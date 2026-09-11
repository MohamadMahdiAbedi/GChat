package ir.gchat

import android.content.res.ColorStateList
import android.os.Build
import android.util.Log
import android.view.SoundEffectConstants
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppearanceSettingsScreen(
    setTheme: () -> Unit,
    theme: Int,
    navHostController: NavHostController,
    setColor: (Int) -> Unit,
    paletteIndex: Int,
    useDynamicColor: Boolean,
    setUseDynamicColor: (Boolean) -> Unit
) {
    val view = LocalView.current

    val dropdownThemeText = when (theme) {
        0 -> stringResource(R.string.system)
        1 -> stringResource(R.string.dark)
        2 -> stringResource(R.string.light)
        else -> stringResource(R.string.system)
    }

    val dropdownThemeIcon = when (theme) {
        0 -> R.drawable.auto
        1 -> R.drawable.dark_mode
        2 -> R.drawable.light_mode
        else -> R.drawable.auto
    }

    var showColorPicker by remember {
        mutableStateOf(false)
    }

    var selectedColor by remember(paletteIndex) {
        mutableStateOf(materialPalette[paletteIndex].primary)
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) && windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize(),
        //.shadow(
        //    elevation = 16.dp,
        //    clip = false
        //),
        topBar = {
            TopAppBar(
                title = {
                Text(stringResource(R.string.appearance))
            }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                IconButton(
                    onClick = {
                        //view.playSoundEffect(SoundEffectConstants.CLICK)

                        Log.d(
                            "AppearanceBack",
                            "BEFORE | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )

                        val result = navHostController.popBackStack()

                        Log.d(
                            "AppearanceBack",
                            "AFTER | " + "result=$result | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )

                    }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        //contentDescription = "Back"
                        contentDescription = null
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
        }) { innerPadding ->
        Spacer(modifier = Modifier.padding(innerPadding))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 64.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val layoutDirection = if (LocalContext.current.isRtl()) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
                ) {
                    Column {

                        Text(
                            text = stringResource(R.string.theme),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                setTheme()
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(dropdownThemeIcon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = dropdownThemeText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.tap_to_next),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    setUseDynamicColor(!useDynamicColor)
                                }) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        painter = painterResource(R.drawable.palette),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = stringResource(R.string.dynamic_color),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.use_your_systems_dynamic_color),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    LegacySwitch(
                                        checked = useDynamicColor,
                                        onCheckedChange = { setUseDynamicColor(it) })
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!useDynamicColor) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp,
                        shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
                    ) {

                        Column {

                            Text(
                                text = stringResource(R.string.colors),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            ColorPaletteSelector(
                                materialPalette = materialPalette,
                                selectedColor = selectedColor,
                                onColorSelected = { theme ->
                                    selectedColor = materialPalette[theme].primary
                                    setColor(theme)
                                },
                                showColorPicker = { showColorPicker = true })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(initialColor = selectedColor, onDismissRequest = {
            showColorPicker = false
        }, onColorSelected = { color ->
            materialPalette[19] = Palette(
                primary = color, onPrimary = if (color.luminance() >= 0.5f) {
                    Color.Black
                } else {
                    Color.White
                }
            )

            selectedColor = color
            setColor(19)
        })
    }
}

@Composable
private fun LegacySwitch(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val outline = MaterialTheme.colorScheme.outline.toArgb()

    val thumbColors = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)
        ), intArrayOf(
            primary, outline
        )
    )

    val trackColors = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)
        ), intArrayOf(
            primary, outline
        )
    )

    AndroidView(modifier = Modifier.padding(end = 16.dp), factory = { context ->
        Switch(context).apply {

            fun applyColors() {
                thumbDrawable?.let { drawable ->
                    DrawableCompat.setTintList(
                        DrawableCompat.wrap(drawable.mutate()), thumbColors
                    )
                }

                trackDrawable?.let { drawable ->
                    DrawableCompat.setTintList(
                        DrawableCompat.wrap(drawable.mutate()), trackColors
                    )
                }
            }

            isChecked = checked
            applyColors()

            setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(isChecked)
            }
        }
    }, update = { switch ->
        if (switch.isChecked != checked) {
            switch.isChecked = checked
        }

        switch.thumbDrawable?.let { drawable ->
            DrawableCompat.setTintList(
                DrawableCompat.wrap(drawable.mutate()), thumbColors
            )
        }

        switch.trackDrawable?.let { drawable ->
            DrawableCompat.setTintList(
                DrawableCompat.wrap(drawable.mutate()), trackColors
            )
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ContentAnalysisScreen(
    navHostController: NavHostController,
    //enableContentAnalysis: (String) -> Unit
) {
    val view = LocalView.current

    var obscene by rememberSaveable { mutableStateOf(false) }
    var spam by rememberSaveable { mutableStateOf(false) }
    var hate by rememberSaveable { mutableStateOf(false) }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) && windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize(),
        //.shadow(
        //    elevation = 16.dp,
        //    clip = false
        //),
        topBar = {
            TopAppBar(
                title = {
                Text(stringResource(R.string.content_analysis))
            }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                IconButton(
                    onClick = {
                        //view.playSoundEffect(SoundEffectConstants.CLICK)
                        Log.d(
                            "AppearanceBack",
                            "BEFORE | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )

                        val result = navHostController.popBackStack()

                        Log.d(
                            "AppearanceBack",
                            "AFTER | " + "result=$result | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        //contentDescription = "Back"
                        contentDescription = null
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
        }) { innerPadding ->
        Spacer(modifier = Modifier.padding(innerPadding))
        val layoutDirection = if (LocalContext.current.isRtl()) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
                ) {
                    Column {

                        Text(
                            text = stringResource(R.string.phrases_to_identify),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                hate = !hate
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.campaign),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.spam),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.advertising_or_unwanted_messages),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LegacySwitch(
                                    checked = hate, onCheckedChange = { checked ->
                                        hate = checked
                                    })
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                obscene = !obscene
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.sentiment_extremely_dissatisfied),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.hate),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.insults_or_offensive_language),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                }

                                LegacySwitch(
                                    checked = obscene, onCheckedChange = { checked ->
                                        obscene = checked
                                    })
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                spam = !spam
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.no_adult_content),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.obscene),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.explicit_or_inappropriate_content),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LegacySwitch(
                                    checked = spam, onCheckedChange = { checked ->
                                        spam = checked
                                    })
                            }
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(8.dp))
//
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
//                    color = MaterialTheme.colorScheme.surface,
//                    shadowElevation = 4.dp,
//                    shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
//                ) {
//                    Column {
//
//                        Text(
//                            text = "Detector Reaction",
//                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
//                            style = MaterialTheme.typography.titleSmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SendBoxKeysSettingsScreen(
    navHostController: NavHostController,
    sendWith: SendMessageWith,
    setSendWith: (SendMessageWith) -> Unit
) {
    val view = LocalView.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) && windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact) }

    var sendWithReturnKey by rememberSaveable { mutableStateOf(sendWith.enter) }
    var sendWithShiftReturnKey by rememberSaveable { mutableStateOf(sendWith.shiftEnter) }
    var sendWithCtrlReturnKey by rememberSaveable { mutableStateOf(sendWith.ctrlEnter) }
    var sendWithOptionReturnKey by rememberSaveable { mutableStateOf(sendWith.altEnter) }

    LaunchedEffect(
        sendWithReturnKey, sendWithShiftReturnKey, sendWithCtrlReturnKey, sendWithOptionReturnKey
    ) {
        setSendWith(
            SendMessageWith(
                enter = sendWithReturnKey,
                shiftEnter = sendWithShiftReturnKey,
                ctrlEnter = sendWithCtrlReturnKey,
                altEnter = sendWithOptionReturnKey
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize(),
        //.shadow(
        //    elevation = 16.dp,
        //    clip = false
        //),
        topBar = {
            TopAppBar(
                title = {
                Text(stringResource(R.string.send_short_keys))
                //Row(verticalAlignment = Alignment.CenterVertically) {
                //    Text("Send Short Keys", modifier = Modifier.weight(1f))
                //    Row(
                //        modifier = Modifier
                //            .height(64.dp)
                //            .clickable(
                //                interactionSource = remember { MutableInteractionSource() },
                //                indication = ripple(bounded = false)
                //            ) {
                //                //view.playSoundEffect(SoundEffectConstants.CLICK)
                //                setSendWith(
                //                    SendMessageWith(
                //                        enter = sendWithReturnKey,
                //                        shiftEnter = sendWithShiftReturnKey,
                //                        ctrlEnter = sendWithCtrlReturnKey,
                //                        altEnter = sendWithOptionReturnKey
                //                    )
                //                )
                //            }, verticalAlignment = Alignment.CenterVertically
                //    ) {
                //        Text(text = "Save")
                //    }
                //}
            }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                IconButton(
                    onClick = {
                        //view.playSoundEffect(SoundEffectConstants.CLICK)
                        Log.d(
                            "AppearanceBack",
                            "BEFORE | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )

                        val result = navHostController.popBackStack()

                        Log.d(
                            "AppearanceBack",
                            "AFTER | " + "result=$result | " + "current=${navHostController.currentBackStackEntry?.destination?.route} | " + "previous=${navHostController.previousBackStackEntry?.destination?.route}"
                        )
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        //contentDescription = "Back"
                        contentDescription = null
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
        }) { innerPadding ->
        Spacer(modifier = Modifier.padding(innerPadding))
        val layoutDirection = if (LocalContext.current.isRtl()) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
                ) {
                    Column {

                        Text(
                            text = stringResource(R.string.keys),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                sendWithReturnKey = !sendWithReturnKey
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.subdirectory_arrow_left),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.return_string),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.send_message_with_return_key),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                }

                                LegacySwitch(
                                    checked = sendWithReturnKey, onCheckedChange = { checked ->
                                        sendWithReturnKey = checked
                                    })
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                sendWithShiftReturnKey = !sendWithShiftReturnKey
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.shift),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.shift_return),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.send_message_with_shift_and_return_key),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LegacySwitch(
                                    checked = sendWithShiftReturnKey, onCheckedChange = { checked ->
                                        sendWithShiftReturnKey = checked
                                    })
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                sendWithCtrlReturnKey = !sendWithCtrlReturnKey
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_up),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.control_return),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.Send_message_with_control_and_return_key),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LegacySwitch(
                                    checked = sendWithCtrlReturnKey, onCheckedChange = { checked ->
                                        sendWithCtrlReturnKey = checked
                                    })
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            color = MaterialTheme.colorScheme.surface,
                            onClick = {
                                //view.playSoundEffect(SoundEffectConstants.CLICK)
                                sendWithOptionReturnKey = !sendWithOptionReturnKey
                            }) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(R.drawable.keyboard_option_key),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(24.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = stringResource(R.string.alt_return),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = stringResource(R.string.send_message_with_Alt_and_return_key),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                LegacySwitch(
                                    checked = sendWithOptionReturnKey,
                                    onCheckedChange = { checked ->
                                        sendWithOptionReturnKey = checked
                                    })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ColorPaletteSelector(
    materialPalette: List<Palette>,
    selectedColor: Color,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showColorPicker: () -> Unit = { }
) {
    val view = LocalView.current

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        materialPalette.forEachIndexed { index, paletteItem ->
            val isSelected = selectedColor == paletteItem.primary

            val animatedSize by animateDpAsState(
                targetValue = if (isSelected) 64.dp else 48.dp,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "colorSize"
            )

            val animatedPadding by animateDpAsState(
                targetValue = if (isSelected) 0.dp else 8.dp,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "colorPadding"
            )

            Box(
                modifier = Modifier
                    .padding(animatedPadding)
                    .size(animatedSize)
                    .shadow(
                        elevation = 4.dp, shape = CircleShape, clip = false
                    )
                    .clip(CircleShape)
                    .background(paletteItem.primary)
                    .clickable {
                        //view.playSoundEffect(SoundEffectConstants.CLICK)
                        onColorSelected(index)
                        if (index == materialPalette.size - 1 && isSelected) {
                            showColorPicker()
                        }
                    }) {
                if (index == materialPalette.size - 1) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(R.drawable.colorize),
                        contentDescription = null,
                        tint = if (paletteItem.primary.luminance() >= 0.5f) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    setTheme: () -> Unit,
    theme: Int,
    setServerIP: (String) -> Unit,
    serverIP: String,
    setDevice: (Int) -> Unit,
    device: suspend () -> Int,
    setColor: (Int) -> Unit,
    paletteIndex: Int,
    sendWith: SendMessageWith,
    setSendWith: (SendMessageWith) -> Unit,
    useDynamicColor: Boolean,
    setUseDynamicColor: (Boolean) -> Unit
) {
    val view = LocalView.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember { mutableStateOf(!(windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) && windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact) }
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->

            val current = entry.destination.route
            val previous = navController.previousBackStackEntry?.destination?.route

            Log.d(
                "NAV_DEBUG", """
            ╔══════════════════════════════════════
            ║ CURRENT  : $current
            ║ PREVIOUS : $previous
            ╚══════════════════════════════════════
            """.trimIndent()
            )

            // اگر به empty رسیدیم، یعنی root هستیم.
            // Back دیگر نباید empty را pop کند.
            if (current == "empty") {
                Log.d("NAV_DEBUG", "ROOT reached")
            }
        }
    }

    Row {
        Scaffold(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(
                    if (expandedScreen) 0.3f else 1f
                ), containerColor = MaterialTheme.colorScheme.background, topBar = {
                TopAppBar(
                    title = {
                    Text(stringResource(R.string.setting))
                }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                    IconButton(
                        onClick = {
                            //view.playSoundEffect(SoundEffectConstants.CLICK)
                            navHostController.popBackStack()
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null //contentDescription = "Back"
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
            }) { innerPadding ->
            Spacer(modifier = Modifier.padding(innerPadding))
            val layoutDirection = if (LocalContext.current.isRtl()) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 64.dp)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Appearance
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    if (expandedScreen) {
                                        navController.navigate("appearanceSettings")
                                    } else {
                                        navHostController.navigate("appearanceSettings")
                                    }
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.imagesearch_roller),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.appearance),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.theme_colors_and_fonts),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Send shortcuts
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    if (expandedScreen) {
                                        navController.navigate("sendBoxKeysSettings")
                                    } else {
                                        navHostController.navigate("sendBoxKeysSettings")
                                    }
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.send),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            //text = "SendBox Keys",
                                            //text = "Send shortcuts",
                                            text = stringResource(R.string.send_short_keys),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            //text = "Enter, Shift + Enter, Ctrl + Enter...",
                                            text = stringResource(R.string.choose_which_keys_send_messages),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Chat
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)

                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.edit),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.chat),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.customize_chat_elements_and_layout),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Language
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    if (expandedScreen) {
                                        navController.navigate("setLanguage")
                                    } else {
                                        navHostController.navigate("setLanguage")
                                    }
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.translate),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.languages),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            //text = "English, فارسی, عربی",
                                            text = stringResource(R.string.choose_the_language_used_by_the_app),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Folders
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.tabs),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.folders),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(

                                            text = stringResource(R.string.categorize_and_organize_conversations),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.server),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    if (expandedScreen) {
                                        navController.navigate("ipConfig")
                                    } else {
                                        navHostController.navigate("ipConfig")
                                    }
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.tune),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ip_config),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.host_port_protocol),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                    if (expandedScreen) {
                                        navController.navigate("contentAnalysisSettings")
                                    } else {
                                        navHostController.navigate("contentAnalysisSettings")
                                    }
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.report),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.content_analysis),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(

                                            text = stringResource(R.string.obscene_spam_hate),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.data_and_storage),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)

                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.database),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.storage),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.manage_storage_and_data_preferences),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.play_circle),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.media),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.manage_media_display_and_handling),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                color = MaterialTheme.colorScheme.surface,
                                onClick = {
                                    //view.playSoundEffect(SoundEffectConstants.CLICK)
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.mobiledata_arrows),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(24.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.data_usage),
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = stringResource(R.string.manage_data_usage_and_network_preferences),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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

        if (expandedScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Black.copy(alpha = 0.04f),
                                    Color.Black.copy(alpha = 0.02f),
                                    Color.Transparent
                                ), startX = 0.dp.toPx(), endX = 8.dp.toPx()
                            ), blendMode = BlendMode.Multiply
                        )
                    }) {
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = "empty",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> -(fullHeight * 0.1f).toInt() },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    popEnterTransition = {
                        slideInVertically(
                            initialOffsetY = { fullHeight -> -(fullHeight * 0.1f).toInt() },
                            animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(
                                durationMillis = 320, easing = FastOutSlowInEasing
                            )
                        )
                    }) {
                    composable(route = "empty") { }
                    composable(route = "appearanceSettings") {
                        AppearanceSettingsScreen(
                            setTheme = setTheme,
                            theme = theme,
                            navHostController = navController,
                            setColor = setColor,
                            paletteIndex = paletteIndex,
                            useDynamicColor = useDynamicColor,
                            setUseDynamicColor = setUseDynamicColor
                        )
                    }
                    composable(route = "sendBoxKeysSettings") {
                        SendBoxKeysSettingsScreen(
                            navHostController = navController,
                            sendWith = sendWith,
                            setSendWith = setSendWith
                        )
                    }
                    composable(route = "contentAnalysisSettings") {
                        ContentAnalysisScreen(
                            navHostController = navController
                        )
                    }
                    composable(route = "ipConfig") {
                        IpConfig(
                            serverIP = serverIP,
                            device = device,
                            back = { navController.popBackStack() },
                            setDevice = setDevice,
                            setServerIP = setServerIP
                        )
                    }
                    composable(route = "setLanguage") {
                        Language(back = { navController.popBackStack() })
                    }
                }
            }
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
    val devices = listOf(
        stringResource(R.string.android_studio_emulator),
        stringResource(R.string.genymotion),
        stringResource(R.string.other_devices)
        //"Android Studio Emulator", "Genymotion", "Other Devices"
    )
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
                    Text(stringResource(R.string.set_server_ip))
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
                            //view.playSoundEffect(SoundEffectConstants.CLICK)
                            back()
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null //contentDescription = "Back"
                        )
                    }
                })
        }) { innerPadding ->
        val context = LocalContext.current
        val layoutDirection = if (context.isRtl()) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
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
                        .shadow(
                            elevation = 2.dp, shape = RoundedCornerShape(2.dp), clip = false
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(2.dp)
                        )
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    devices.forEach { thisDevice ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (thisDevice) {
                                    devices[0] -> {
                                        setDevice(0)
                                        automaticMode = true
                                        networkProtocol = "IPv4"
                                        host = "10.0.2.2"
                                        port = "8765"
                                    }

                                    devices[1] -> {
                                        setDevice(1)
                                        automaticMode = true
                                        networkProtocol = "IPv4"
                                        host = "10.0.3.2"
                                        port = "8765"
                                    }

                                    //devices[2] -> {
                                    //    setDevice(2)
                                    //    automaticMode = false
                                    //}

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
                                        devices[0] -> {
                                            setDevice(0)
                                            automaticMode = true
                                            networkProtocol = "IPv4"
                                            host = "10.0.2.2"
                                            port = "8765"
                                        }

                                        devices[1] -> {
                                            setDevice(1)
                                            automaticMode = true
                                            networkProtocol = "IPv4"
                                            host = "10.0.3.2"
                                            port = "8765"
                                        }

                                        //devices[2] -> {
                                        //    setDevice(2)
                                        //    automaticMode = false
                                        //}

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
                            Text(text = stringResource(R.string.host))
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
                            Text(text = stringResource(R.string.port))
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
                        label = { Text(stringResource(R.string.protocol)) },
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
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Button(
                onClick = {
                    //view.playSoundEffect(SoundEffectConstants.CLICK)
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
                    contentDescription = null, //contentDescription = "OK",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun IpConfigDialogMaterialYou(
//    serverIP: String,
//    device: suspend () -> Int,
//    back: () -> Unit,
//    setDevice: (Int) -> Unit,
//    setServerIP: (String) -> Unit
//) {
//    val view = LocalView.current
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    var host by remember { mutableStateOf(serverIP.split(":")[0]) }
//    var port by remember { mutableStateOf(serverIP.split(":")[1]) }
//
//    val portNumber = port.toIntOrNull()
//    val portError = portNumber == null || portNumber > 65535 || portNumber < 1
//
//    val networkProtocols = listOf(
//        "IPv4", "IPv6", "Domain", "Localhost", "LocalServer"
//    )
//
//    var networkProtocol by remember {
//        mutableStateOf(networkProtocols[0])
//    }
//
//    var expanded by remember { mutableStateOf(false) }
//
//    val devices = listOf(
//        "Android Studio Emulator", "Genymotion", "Other Devices"
//    )
//
//    var selectedDevice by remember {
//        mutableStateOf(devices[2])
//    }
//
//    var automaticMode by remember {
//        mutableStateOf(false)
//    }
//
//    LaunchedEffect(Unit) {
//        val d = device()
//        selectedDevice = devices[d]
//        automaticMode = d != 2
//    }
//
//    val hostError = !when (networkProtocol) {
//        "IPv4" -> {
//            host.isNotBlank() && host.split(".").let { parts ->
//                parts.size == 4 && parts.all { part ->
//                    val num = part.toIntOrNull()
//                    num != null && num in 0..255
//                }
//            }
//        }
//
//        "IPv6" -> {
//            host.isNotBlank() && Regex(
//                "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,7}:$|" + "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" + "^[0-9a-fA-F]{1,4}::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,5}:){1,5}:([0-9a-fA-F]{1,4}:){1,5}$|" + "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"
//            ).matches(
//                host.removeSurrounding("[", "]")
//            )
//        }
//
//        "Domain" -> {
//            host.isNotBlank() && Regex(
//                "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" + "(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}" + "[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$"
//            ).matches(host)
//        }
//
//        "Localhost" -> {
//            host.isNotBlank() && host.equals("localhost", ignoreCase = true)
//        }
//
//        "LocalServer" -> {
//            host.isNotBlank() && host.length <= 253 && Regex(
//                "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}" + "[a-zA-Z0-9])?(\\.[a-zA-Z0-9]" + "([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
//            ).matches(host)
//        }
//
//        else -> false
//    }
//
//    AlertDialog(onDismissRequest = back, title = {
//        Text("Set Server IP")
//    }, text = {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .verticalScroll(
//                    rememberScrollState()
//                ), verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//
//            devices.forEach { thisDevice ->
//
//                fun selectDevice() {
//                    when (thisDevice) {
//                        "Android Studio Emulator" -> {
//                            setDevice(0)
//                            automaticMode = true
//                            networkProtocol = "IPv4"
//                            host = "10.0.2.2"
//                            port = "8765"
//                        }
//
//                        "Genymotion" -> {
//                            setDevice(1)
//                            automaticMode = true
//                            networkProtocol = "IPv4"
//                            host = "10.0.3.2"
//                            port = "8765"
//                        }
//
//                        else -> {
//                            setDevice(2)
//                            automaticMode = false
//                        }
//                    }
//
//                    selectedDevice = thisDevice
//                }
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(CircleShape)
//                        .clickable {
//                            selectDevice()
//                        }, verticalAlignment = Alignment.CenterVertically
//                ) {
//                    RadioButton(
//                        selected = selectedDevice == thisDevice, onClick = {
//                            selectDevice()
//                        })
//
//                    Text(
//                        text = thisDevice, modifier = Modifier.padding(start = 8.dp)
//                    )
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                TextField(
//                    modifier = Modifier.weight(0.6f), value = host, onValueChange = {
//                        host = it
//                    }, enabled = !automaticMode, isError = hostError, singleLine = true, label = {
//                        Text("Host")
//                    }, colors = TextFieldDefaults.colors(
//                        focusedContainerColor = Color.Transparent,
//                        unfocusedContainerColor = Color.Transparent,
//                        disabledContainerColor = Color.Transparent,
//                        errorContainerColor = Color.Transparent
//                    ), keyboardOptions = KeyboardOptions(
//                        imeAction = ImeAction.Next
//                    ), keyboardActions = KeyboardActions(
//                        onNext = {
//                            focusManager.moveFocus(
//                                FocusDirection.Next
//                            )
//                        })
//                )
//
//                Spacer(
//                    modifier = Modifier.width(8.dp)
//                )
//
//                TextField(
//                    modifier = Modifier.weight(0.4f), value = port, onValueChange = {
//                        port = it
//                    }, enabled = !automaticMode, isError = portError, singleLine = true, label = {
//                        Text("Port")
//                    }, colors = TextFieldDefaults.colors(
//                        focusedContainerColor = Color.Transparent,
//                        unfocusedContainerColor = Color.Transparent,
//                        disabledContainerColor = Color.Transparent,
//                        errorContainerColor = Color.Transparent
//                    ), keyboardOptions = KeyboardOptions(
//                        imeAction = ImeAction.Done
//                    ), keyboardActions = KeyboardActions(
//                        onDone = {
//                            keyboardController?.hide()
//
//                            if (!portError && !hostError) {
//                                setServerIP("$host:$port")
//                                back()
//                            }
//                        })
//                )
//            }
//
//            val enabled = false
//
//            ExposedDropdownMenuBox(
//                expanded = expanded, onExpandedChange = {
//                    if (enabled) {
//                        expanded = !expanded
//                    }
//                }) {
//                TextField(
//                    value = networkProtocol,
//                    onValueChange = {},
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .menuAnchor(
//                            ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled
//                        ),
//                    readOnly = true,
//                    enabled = enabled,
//                    label = {
//                        Text("Protocol")
//                    },
//                    trailingIcon = {
//                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
//                    },
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = Color.Transparent,
//                        unfocusedContainerColor = Color.Transparent,
//                        disabledContainerColor = Color.Transparent,
//                        errorContainerColor = Color.Transparent
//                    )
//                )
//
//                ExposedDropdownMenu(
//                    expanded = enabled && expanded, onDismissRequest = {
//                        expanded = false
//                    }) {
//                    networkProtocols.forEach { protocol ->
//                        DropdownMenuItem(text = {
//                            Text(protocol)
//                        }, onClick = {
//                            networkProtocol = protocol
//                            expanded = false
//                        })
//                    }
//                }
//            }
//        }
//    }, confirmButton = {
//        Button(
//            onClick = {
//                view.playSoundEffect(
//                    SoundEffectConstants.CLICK
//                )
//
//                setServerIP("$host:$port")
//                back()
//            }, enabled = !portError && !hostError
//        ) {
//            Text("OK")
//        }
//    }, dismissButton = {
//        TextButton(
//            onClick = {
//                view.playSoundEffect(
//                    SoundEffectConstants.CLICK
//                )
//                back()
//            }) {
//            Text("Cancel")
//        }
//    })
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpConfigDialog(
    serverIP: String,
    device: suspend () -> Int,
    back: () -> Unit,
    setDevice: (Int) -> Unit,
    setServerIP: (String) -> Unit
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var host by remember { mutableStateOf(serverIP.split(":")[0]) }
    var port by remember { mutableStateOf(serverIP.split(":")[1]) }

    val portNumber = port.toIntOrNull()
    val portError = portNumber == null || portNumber > 65535 || portNumber < 1

    val networkProtocols = listOf(
        "IPv4", "IPv6", "Domain", "Localhost", "LocalServer"
    )

    var networkProtocol by remember {
        mutableStateOf(networkProtocols[0])
    }

    var expanded by remember { mutableStateOf(false) }

    val devices = listOf(
        stringResource(R.string.android_studio_emulator),
        stringResource(R.string.genymotion),
        stringResource(R.string.other_devices)
        //"Android Studio Emulator", "Genymotion", "Other Devices"
    )

    var selectedDevice by remember {
        mutableStateOf(devices[2])
    }

    var automaticMode by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val d = device()
        selectedDevice = devices[d]
        automaticMode = d != 2
    }

    val hostError = !when (networkProtocol) {
        "IPv4" -> {
            host.isNotBlank() && host.split(".").let { parts ->
                parts.size == 4 && parts.all { part ->
                    val num = part.toIntOrNull()
                    num != null && num in 0..255
                }
            }
        }

        "IPv6" -> {
            host.isNotBlank() && Regex(
                "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,4}:){1,7}:$|" + "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" + "^[0-9a-fA-F]{1,4}::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" + "^([0-9a-fA-F]{1,5}:){1,5}:([0-9a-fA-F]{1,4}:){1,5}$|" + "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"
            ).matches(
                host.removeSurrounding("[", "]")
            )
        }

        "Domain" -> {
            host.isNotBlank() && Regex(
                "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" + "(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}" + "[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$"
            ).matches(host)
        }

        "Localhost" -> {
            host.isNotBlank() && host.equals("localhost", ignoreCase = true)
        }

        "LocalServer" -> {
            host.isNotBlank() && host.length <= 253 && Regex(
                "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}" + "[a-zA-Z0-9])?(\\.[a-zA-Z0-9]" + "([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
            ).matches(host)
        }

        else -> false
    }

    AlertDialog(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .shadow(
                elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
            ), onDismissRequest = back, title = {
        Text(stringResource(R.string.set_server_ip))
    }, shape = RoundedCornerShape(2.dp), text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                ), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp, shape = RoundedCornerShape(2.dp), clip = false
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(2.dp)
                    )
            ) {
                devices.forEach { thisDevice ->

                    fun selectDevice() {
                        when (thisDevice) {
                            // Android Studio emulator
                            devices[0] -> {
                                setDevice(0)
                                automaticMode = true
                                networkProtocol = "IPv4"
                                host = "10.0.2.2"
                                port = "8765"
                            }

                            // Genymotion emulator
                            devices[1] -> {
                                setDevice(1)
                                automaticMode = true
                                networkProtocol = "IPv4"
                                host = "10.0.3.2"
                                port = "8765"
                            }

                            // other devices
                            else -> {
                                setDevice(2)
                                automaticMode = false
                            }
                        }

                        selectedDevice = thisDevice
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectDevice()
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDevice == thisDevice, onClick = {
                                selectDevice()
                            })

                        Text(
                            text = thisDevice, modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier.weight(0.6f),
                    value = host,
                    onValueChange = {
                        host = it
                    },
                    enabled = !automaticMode,
                    isError = hostError,
                    singleLine = true,
                    label = {
                        Text(stringResource(R.string.host))
                    },
                    textStyle = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(
                                FocusDirection.Next
                            )
                        })
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                TextField(
                    modifier = Modifier.weight(0.4f),
                    value = port,
                    onValueChange = {
                        port = it
                    },
                    textStyle = LocalTextStyle.current.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    enabled = !automaticMode,
                    isError = portError,
                    singleLine = true,
                    label = {
                        Text(stringResource(R.string.port))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
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
                expanded = expanded, onExpandedChange = {
                    if (enabled) {
                        expanded = !expanded
                    }
                }) {
                TextField(
                    value = networkProtocol,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled
                        ),
                    readOnly = true,
                    enabled = enabled,
                    label = {
                        Text(stringResource(R.string.protocol))
                    },
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
                    expanded = enabled && expanded, onDismissRequest = {
                        expanded = false
                    }) {
                    networkProtocols.forEach { protocol ->
                        DropdownMenuItem(text = {
                            Text(protocol)
                        }, onClick = {
                            networkProtocol = protocol
                            expanded = false
                        })
                    }
                }
            }
        }
    }, confirmButton = {
        Button(
            onClick = {
                view.playSoundEffect(
                    SoundEffectConstants.CLICK
                )

                setServerIP("$host:$port")
                back()
            }, shape = RoundedCornerShape(2.dp), enabled = !portError && !hostError
        ) {
            Text(stringResource(R.string.ok))
        }
    }, dismissButton = {
        TextButton(
            shape = RoundedCornerShape(2.dp), onClick = {
                view.playSoundEffect(
                    SoundEffectConstants.CLICK
                )
                back()
            }) {
            Text(stringResource(R.string.cancel))
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language(
    back: () -> Unit
) {
    val view = LocalView.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.languages))
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
                            //view.playSoundEffect(SoundEffectConstants.CLICK)
                            back()
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                })
        }) { innerPadding ->
        val context = LocalContext.current
        val layoutDirection = if (context.isRtl()) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            Spacer(modifier = Modifier.padding(innerPadding))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val languages = remember { listOf("ar", "zh", "de", "en", "fa", "he", "ru") }
                val languagesName = remember {
                    listOf("العربية", "中文", "Deutsch", "English", "فارسی", "עברית", "Русский")
                }

                val selectedLanguage = AppCompatDelegate
                    .getApplicationLocales()
                    .get(0)
                    ?.language
                    ?: "en"
                val editedSelectedLanguage = selectedLanguage.replace("iw", "he")

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        languages.forEachIndexed { index, language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Log.d(
                                            "LANGUAGE",
                                            "BEFORE: ${AppCompatDelegate.getApplicationLocales()}"
                                        )

                                        setAppLanguage(language)

                                        Log.d(
                                            "LANGUAGE",
                                            "AFTER: ${AppCompatDelegate.getApplicationLocales()}"
                                        )
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = editedSelectedLanguage == language,
                                    onClick = {
                                        Log.d(
                                            "LANGUAGE",
                                            "BEFORE: ${AppCompatDelegate.getApplicationLocales()}"
                                        )

                                        setAppLanguage(language)

                                        Log.d(
                                            "LANGUAGE",
                                            "AFTER: ${AppCompatDelegate.getApplicationLocales()}"
                                        )
                                    }
                                )

                                Text(
                                    text = languagesName[index],
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
