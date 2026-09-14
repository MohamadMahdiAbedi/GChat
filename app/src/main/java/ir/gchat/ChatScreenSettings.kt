package ir.gchat

import android.content.res.ColorStateList
import android.util.Log
import android.widget.SeekBar
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ChatSettingsScreen(
    navHostController: NavHostController,
    gradientSettings: GradientSettings?
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val expandedScreen by remember {
        mutableStateOf(
            !(
                    windowSizeClass.widthSizeClass == Compact ||
                            windowSizeClass.widthSizeClass == Medium
                    ) &&
                    windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact
        )
    }

    var colors by remember {
        mutableStateOf(
            gradientSettings?.colors?.map { color ->
                Color(color)
            } ?: listOf(
                Color(0xffdbddbb),
                Color(0xff6ba587),
                Color(0xffd5d88d),
                Color(0xff88b884)
            )
        )
    }

    val alwaysAnimate by remember { mutableStateOf(gradientSettings?.alwaysAnimate ?: false) }

    var wallpaperMixBlendMode by remember {
        mutableStateOf(
            when (gradientSettings?.blendMode) {
                0 -> WallpaperMixBlendMode.Normal
                1 -> WallpaperMixBlendMode.Overlay
                2 -> WallpaperMixBlendMode.SoftLight
                3 -> WallpaperMixBlendMode.HardLight
                else -> WallpaperMixBlendMode.Overlay
            }
        )
    }

    var patternTint by remember {
        mutableStateOf(Color(gradientSettings?.patternTint ?: 0xFF000000.toInt()))
    }

    var patternAlpha by remember { mutableFloatStateOf(gradientSettings?.patternAlpha ?: 0.5f) }

    var showColorPicker by remember { mutableStateOf(false) }

    var selectedColorIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.chat), modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .height(64.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false)
                                ) { }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.save))
                        }
                    }
                }, /*expandedHeight = 56.dp,*/ navigationIcon = {
                    IconButton(
                        onClick = {
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
        }
    ) { innerPadding ->
        Spacer(modifier = Modifier.padding(innerPadding))
        BoxWithConstraints {
            val layoutHeight = maxHeight - 64.dp
            val layoutWidth = maxWidth
            var mainGradientHeight by remember { mutableStateOf(0.dp) }
            if (layoutWidth > layoutHeight) {
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
                                //.height(width - if (expandedScreen) 8.dp else 0.dp)
                                .padding(horizontal = if (expandedScreen) 8.dp else 0.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            shape = if (expandedScreen) RoundedCornerShape(2.dp) else RectangleShape
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.theme),
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp, vertical = 8.dp
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row {
                                    //shadow
                                    Surface(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(bottom = 8.dp)
                                            .fillMaxWidth(0.5f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = MaterialTheme.colorScheme.surface,
                                    ) {
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                            mainGradientHeight = maxHeight
                                            TWallpaper(
                                                modifier = Modifier.fillMaxSize(),
                                                colors = colors,
                                                updateFps = 60,
                                                angularSpeed = Math.PI.toFloat() / 4f,
                                                animate = true,
                                                pattern = painterResource(R.drawable.pattern),
                                                mixBlendMode = wallpaperMixBlendMode,
                                                patternTint = patternTint,
                                                patternAlpha = patternAlpha
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.height(mainGradientHeight)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.background,
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                                .innerShadow(
                                                    shape = RoundedCornerShape(2.dp),
                                                    shadow = Shadow(
                                                        color = Color.Black.copy(alpha = 0.2f),
                                                        radius = 8.dp,
                                                        spread = 0.dp,
                                                        offset = DpOffset(0.dp, 2.dp)
                                                    )
                                                )
                                                .padding(16.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            colors.forEachIndexed { index, color ->

                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .clickable {
                                                            selectedColorIndex = index
                                                            showColorPicker = true
                                                        }
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(patternTint)
                                                    .clickable {
                                                        selectedColorIndex = 4
                                                        showColorPicker = true
                                                    }
                                            )
                                        }

                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                                .padding(end = 8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.background,
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                                .innerShadow(
                                                    shape = RoundedCornerShape(2.dp),
                                                    shadow = Shadow(
                                                        color = Color.Black.copy(alpha = 0.2f),
                                                        radius = 8.dp,
                                                        spread = 0.dp,
                                                        offset = DpOffset(0.dp, 2.dp)
                                                    )
                                                ),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            items(tWallpaperColors) { colorsList ->

                                                TWallpaper(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = LocalIndication.current
                                                        ) {
                                                            colors = colorsList
                                                        },
                                                    colors = colorsList,
                                                    updateFps = 60,
                                                    angularSpeed = Math.PI.toFloat() / 4f,
                                                    animate = true,
                                                    pattern = painterResource(R.drawable.pattern),
                                                    mixBlendMode = WallpaperMixBlendMode.Overlay,
                                                    patternTint = Color.Black,
                                                    patternAlpha = 0.5f
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = 8.dp)
                                                .padding(end = 8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.background,
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                                .innerShadow(
                                                    shape = RoundedCornerShape(2.dp),
                                                    shadow = Shadow(
                                                        color = Color.Black.copy(alpha = 0.2f),
                                                        radius = 8.dp,
                                                        spread = 0.dp,
                                                        offset = DpOffset(0.dp, 2.dp)
                                                    )
                                                )
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            var expanded by remember { mutableStateOf(false) }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            // blend mode
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                                    .height(72.dp),
                                                shape = RoundedCornerShape(2.dp),
                                                onClick = {
                                                    expanded = true
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.layers),
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
                                                            text = stringResource(R.string.blend_mode),
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )

                                                        Text(
                                                            text = stringResource(R.string.how_layers_blend_together),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    val blendSettings = listOf(
                                                        Triple(
                                                            stringResource(R.string.normal),
                                                            null,
                                                            WallpaperMixBlendMode.Normal
                                                        ),
                                                        Triple(
                                                            stringResource(R.string.overlay),
                                                            null,
                                                            WallpaperMixBlendMode.Overlay
                                                        ),
                                                        Triple(
                                                            stringResource(R.string.soft_light),
                                                            stringResource(R.string.may_not_work_on_your_gpu),
                                                            WallpaperMixBlendMode.SoftLight
                                                        ),
                                                        Triple(
                                                            stringResource(R.string.hard_light),
                                                            stringResource(R.string.may_not_work_on_your_gpu),
                                                            WallpaperMixBlendMode.HardLight
                                                        ),
                                                    )
                                                    var blendModeName by remember {
                                                        mutableStateOf(
                                                            blendSettings.find { it.third == wallpaperMixBlendMode }?.first
                                                                ?: blendSettings[1].first
                                                        )
                                                    }
                                                    Box {
                                                        Text(
                                                            blendModeName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            modifier = Modifier.padding(end = 16.dp)
                                                        )

                                                        DropdownMenu(
                                                            expanded = expanded,
                                                            onDismissRequest = {
                                                                expanded = false
                                                            },
                                                            shape = RoundedCornerShape(2.dp)
                                                        ) {
                                                            blendSettings.forEach { (name, warning, blendMode) ->

                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(name)

                                                                            if (warning != null) {
                                                                                Spacer(
                                                                                    Modifier.width(
                                                                                        8.dp
                                                                                    )
                                                                                )

                                                                                TooltipBox(
                                                                                    positionProvider = TooltipDefaults
                                                                                        .rememberPlainTooltipPositionProvider(),
                                                                                    tooltip = {
                                                                                        PlainTooltip {
                                                                                            Text(
                                                                                                warning
                                                                                            )
                                                                                        }
                                                                                    },
                                                                                    state = rememberTooltipState()
                                                                                ) {
                                                                                    Icon(
                                                                                        painter = painterResource(
                                                                                            R.drawable.info
                                                                                        ),
                                                                                        contentDescription = warning,
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    },
                                                                    onClick = {
                                                                        expanded = false
                                                                        wallpaperMixBlendMode =
                                                                            blendMode
                                                                        blendModeName = name
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            // pattern alpha
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                                    .height(72.dp),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
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

                                                    Column {
                                                        Text(
                                                            text = stringResource(R.string.pattern_alpha),
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )

                                                        Text(
                                                            text = convertDigits(
                                                                text = "${stringResource(R.string.opacity)}: ${(patternAlpha * 100f).toInt()}%",
                                                                digits = arrayOf(
                                                                    stringResource(R.string.zero),
                                                                    stringResource(R.string.one),
                                                                    stringResource(R.string.two),
                                                                    stringResource(R.string.three),
                                                                    stringResource(R.string.four),
                                                                    stringResource(R.string.five),
                                                                    stringResource(R.string.six),
                                                                    stringResource(R.string.seven),
                                                                    stringResource(R.string.eight),
                                                                    stringResource(R.string.nine)
                                                                ).map { it.first() }.toCharArray()
                                                            ),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    val primaryColor =
                                                        MaterialTheme.colorScheme.primary.toArgb()
                                                    val trackColor =
                                                        MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

                                                    AndroidView(
                                                        factory = { context ->
                                                            SeekBar(context).apply {
                                                                max = 1000
                                                                progress = (progress * max)

                                                                progressTintList =
                                                                    ColorStateList.valueOf(
                                                                        primaryColor
                                                                    )

                                                                progressBackgroundTintList =
                                                                    ColorStateList.valueOf(
                                                                        trackColor
                                                                    )

                                                                thumbTintList =
                                                                    ColorStateList.valueOf(
                                                                        primaryColor
                                                                    )

                                                                setOnSeekBarChangeListener(
                                                                    object :
                                                                        SeekBar.OnSeekBarChangeListener {

                                                                        override fun onProgressChanged(
                                                                            seekBar: SeekBar?,
                                                                            value: Int,
                                                                            fromUser: Boolean
                                                                        ) {
                                                                            if (fromUser) {
                                                                                patternAlpha =
                                                                                    value / 1000f
                                                                            }
                                                                        }

                                                                        override fun onStartTrackingTouch(
                                                                            seekBar: SeekBar?
                                                                        ) = Unit

                                                                        override fun onStopTrackingTouch(
                                                                            seekBar: SeekBar?
                                                                        ) = Unit
                                                                    }
                                                                )
                                                            }
                                                        },

                                                        update = { seekBar ->
                                                            seekBar.progress =
                                                                (patternAlpha * 1000).toInt()

                                                            seekBar.progressTintList =
                                                                ColorStateList.valueOf(primaryColor)

                                                            seekBar.progressBackgroundTintList =
                                                                ColorStateList.valueOf(trackColor)

                                                            seekBar.thumbTintList =
                                                                ColorStateList.valueOf(primaryColor)
                                                        },

                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
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
                                .padding(
                                    horizontal = if (expandedScreen) 8.dp else 0.dp
                                ),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            shape = if (expandedScreen) {
                                RoundedCornerShape(2.dp)
                            } else {
                                RectangleShape
                            }
                        ) {

                            Column {

                                Text(
                                    text = stringResource(R.string.theme),
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp)
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    TWallpaper(
                                        modifier = Modifier.fillMaxSize(),
                                        colors = colors,
                                        updateFps = 60,
                                        angularSpeed = Math.PI.toFloat() / 4f,
                                        animate = alwaysAnimate,
                                        pattern = painterResource(R.drawable.pattern),
                                        mixBlendMode = wallpaperMixBlendMode,
                                        patternTint = patternTint,
                                        patternAlpha = patternAlpha
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    colors.forEachIndexed { index, color ->

                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .clickable {
                                                    selectedColorIndex = index
                                                    showColorPicker = true
                                                }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(patternTint)
                                            .clickable {
                                                selectedColorIndex = 4
                                                showColorPicker = true
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showColorPicker) {

        val initialColor = if (selectedColorIndex < colors.size) {
            colors[selectedColorIndex]
        } else {
            patternTint
        }

        ColorPickerDialog(
            initialColor = initialColor,
            onDismissRequest = {
                showColorPicker = false
            },
            onColorSelected = { color ->

                //if (selectedColorIndex < colors.size) {

                //    colors = colors
                //        //.toMutableList().apply {
                //        //    this[selectedColorIndex] = String.format(
                //        //        "#%02X%02X%02X",
                //        //        (color.red * 255).toInt(),
                //        //        (color.green * 255).toInt(),
                //        //        (color.blue * 255).toInt()
                //        //    )
                //        //}

                //} else {

                //    patternTint = color
                //}
                patternTint = color

                showColorPicker = false
            }
        )
    }
}