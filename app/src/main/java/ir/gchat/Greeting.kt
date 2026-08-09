package ir.gchat

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.view.SoundEffectConstants
import android.widget.ProgressBar
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Greeting(
    setTheme: () -> Unit,
    theme: Int,
    signIn: (String, String) -> Unit,
    signUp: (String, String, String) -> Unit,
    ipConfig: () -> Unit,
    getRules: () -> String,
    loginResponse: Boolean
) {
    val context = LocalContext.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }

    var dropdownThemeText by remember { mutableStateOf("Theme: System") }
    var dropdownThemeIcon by remember { mutableIntStateOf(R.drawable.auto) }

    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val navController = rememberNavController()

    when (theme) {
        0 -> {
            dropdownThemeText = "Theme: System"
            dropdownThemeIcon = R.drawable.auto
        }

        1 -> {
            dropdownThemeText = "Theme: Dark"
            dropdownThemeIcon = R.drawable.dark_mode
        }

        2 -> {
            dropdownThemeText = "Theme: Light"
            dropdownThemeIcon = R.drawable.light_mode
        }

        else -> {
            dropdownThemeText = "Theme: System"
            dropdownThemeIcon = R.drawable.auto
        }
    }

    val text = buildAnnotatedString {
        append("Click to login means accepting the ")

        val link = LinkAnnotation.Clickable(
            tag = "terms",
            linkInteractionListener = { showTermsDialog = true },
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = Color(0xFF296A47),
                    textDecoration = TextDecoration.Underline
                )
            )
        )

        withLink(link) {
            append("term of use and privacy.")
        }
    }

    val signInText = buildAnnotatedString {
        append("Already have an account? ")

        val link = LinkAnnotation.Clickable(
            tag = "signIn",
            linkInteractionListener = { navController.navigate("signIn") /*showSignInDialog = true */ },
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = Color(0xFF296A47),
                    textDecoration = TextDecoration.Underline
                )
            )
        )

        withLink(link) {
            append("Sign in.")
        }
    }

    var selectedIccid by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(3000.milliseconds)
            if (isLoading) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(loginResponse) {
        isLoading = false
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val passwordVisible = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val isUsernameError = (!Regex("^[a-z0-9_]+$").matches(username)) && username.isNotEmpty()
    val usernameSizeError = username.length !in 1..50

    val isPasswordError = password.length !in 6..128

    var firstClick by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            if (expanded) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(0f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            expanded = false
                        }
                )
            }

            if (windowSizeClass.widthSizeClass == Compact || windowSizeClass.widthSizeClass == Medium) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(1 / 3f)
                            .padding(innerPadding)
                    ) {
                        IconButton(
                            modifier = Modifier.align(Alignment.TopEnd), onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                expanded = true
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu_dots),
                                contentDescription = "Menu",
                                tint = Color(0xFFFFFFFF),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GChat", style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFDFE4DD)
                            )
                        )
                    }
                }

                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "greeting",

                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },

                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(300)
                        )
                    }) {
                    composable("greeting") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = false
                                    )
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "GChat is secure and optimized for some tasks.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("login") {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_forward),
                                        contentDescription = "Accept and Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 64.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = text,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    composable("login") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = false
                                    )
                                    .background(color = MaterialTheme.colorScheme.background)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    VerifySimCard(
                                        selectedIccid = selectedIccid,
                                        setSelectedIccid = { value -> selectedIccid = value },
                                        endPadding = 0.dp
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(innerPadding)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background
                                                )
                                            )
                                        )
                                        .padding(end = 64.dp)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = signInText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("signUp")
                                    },
                                    enabled = !isLoading && !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(8.dp)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (!isLoading && !selectedIccid.isNullOrBlank()) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }
                            }
                        }
                    }

                    composable("signIn") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .imePadding()
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = false
                                    )
                                    .background(color = MaterialTheme.colorScheme.background)
                            ) {
                                BackHandler(enabled = isLoading) { }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = "To sign-in to your account, enter your username and password.",
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(usernameFocusRequester),
                                        value = username,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            username = it
                                        },
                                        isError = isUsernameError or usernameSizeError && firstClick,
                                        supportingText = {
                                            Column {
                                                if (isUsernameError && firstClick) {
                                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                                }
                                                if (usernameSizeError && firstClick) {
                                                    Text("Username must be less than 50 characters.")
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = { Text("Username") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                passwordFocusRequester.requestFocus()
                                            }
                                        )
                                    )

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(passwordFocusRequester),
                                        value = password,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            password = it
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = {
                                            Text(text = "Password")
                                        },
                                        isError = isPasswordError && firstClick,
                                        supportingText = {
                                            if (isPasswordError && firstClick) {
                                                Text("Password must be between 6 and 128 characters.")
                                            }
                                        },
                                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(
                                                enabled = !isLoading, onClick = {
                                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                                    passwordVisible.value = !passwordVisible.value
                                                }) {
                                                Icon(
                                                    painter = painterResource(
                                                        if (passwordVisible.value) R.drawable.visibility_off
                                                        else R.drawable.visibility
                                                    ), contentDescription = null
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(textDirection = TextDirection.Content),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                coroutineScope.launch {
                                                    signIn(username, password)
                                                    isLoading = true
                                                }
                                                focusManager.moveFocus(FocusDirection.Down)
                                            })
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        if (firstClick) {
                                            signIn(username, password)
                                            isLoading = true
                                        } else {
                                            firstClick = true
                                            if (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) {
                                                signIn(username, password)
                                                isLoading = true
                                            }
                                        }
                                    },
                                    enabled = (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .navigationBarsPadding()
                                        .padding(8.dp)
                                        .size(56.dp)
                                        .shadow(
                                            elevation = if ((!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }

                                val progressColor = Color(0xFF296A47).toArgb()

                                if (isLoading) {
                                    AndroidView(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .fillMaxWidth()
                                            .offset(y = (-6).dp),
                                        factory = { context ->
                                            ProgressBar(
                                                context,
                                                null,
                                                android.R.attr.progressBarStyleHorizontal
                                            ).apply {
                                                isIndeterminate = true
                                                progressTintList =
                                                    ColorStateList.valueOf(progressColor)
                                                indeterminateTintList =
                                                    ColorStateList.valueOf(progressColor)
                                            }
                                        })
                                }
                            }
                        }
                    }

                    composable("signUp") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .imePadding()
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(2 / 3f)
                                    .padding(top = 64.dp)
                                    .shadow(
                                        elevation = 16.dp, clip = false
                                    )
                                    .background(color = MaterialTheme.colorScheme.background)
                            ) {
                                BackHandler(enabled = isLoading) { }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = "To sign-up for an account, set up a username and password.",
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(usernameFocusRequester),
                                        value = username,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            username = it
                                        },
                                        isError = isUsernameError or usernameSizeError && firstClick,
                                        supportingText = {
                                            Column {
                                                if (isUsernameError && firstClick) {
                                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                                }
                                                if (usernameSizeError && firstClick) {
                                                    Text("Username must be less than 50 characters.")
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = { Text("Username") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                passwordFocusRequester.requestFocus()
                                            })
                                    )

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(passwordFocusRequester),
                                        value = password,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            password = it
                                        },
                                        isError = isPasswordError && firstClick,
                                        supportingText = {
                                            if (isPasswordError && firstClick) {
                                                Text("Password must be between 6 and 128 characters.")
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = {
                                            Text(text = "Password")
                                        },
                                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(
                                                enabled = !isLoading, onClick = {
                                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                                    passwordVisible.value = !passwordVisible.value
                                                }) {
                                                Icon(
                                                    painter = painterResource(
                                                        if (passwordVisible.value) R.drawable.visibility_off
                                                        else R.drawable.visibility
                                                    ), contentDescription = null
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(textDirection = TextDirection.Content),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                coroutineScope.launch {
                                                    signUp(
                                                        selectedIccid.toString(), username, password
                                                    )
                                                    isLoading = true
                                                }
                                                focusManager.moveFocus(FocusDirection.Down)
                                            })
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        if (firstClick) {
                                            signUp(selectedIccid.toString(), username, password)
                                            isLoading = true
                                        } else {
                                            firstClick = true
                                            if (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) {
                                                signUp(selectedIccid.toString(), username, password)
                                                isLoading = true
                                            }
                                        }
                                    },
                                    enabled = (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .navigationBarsPadding()
                                        .padding(8.dp)
                                        .size(56.dp)
                                        .shadow(
                                            elevation = if ((!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }

                                val progressColor = Color(0xFF296A47).toArgb()

                                if (isLoading) {
                                    AndroidView(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .fillMaxWidth()
                                            .offset(y = (-6).dp),
                                        factory = { context ->
                                            ProgressBar(
                                                context,
                                                null,
                                                android.R.attr.progressBarStyleHorizontal
                                            ).apply {
                                                isIndeterminate = true
                                                progressTintList =
                                                    ColorStateList.valueOf(progressColor)
                                                indeterminateTintList =
                                                    ColorStateList.valueOf(progressColor)
                                            }
                                        })
                                }
                            }
                        }
                    }
                }

                AnimatedMenu(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    width = 200.dp,
                    height = 160.dp,
                    chord = 256.dp,
                    isExpanded = expanded,
                    close = { expanded = false },
                    ratioX = 1f,
                    ratioY = 0f,
                    position = Alignment.TopEnd,
                    hasBackgroundCover = false,
                    whatIsMyBackgroundFilterColor = { _, _ -> }) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DropdownMenuItem(text = {
                            Text(
                                text = dropdownThemeText
                            )
                        }, leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    id = dropdownThemeIcon
                                ), contentDescription = "Theme"
                            )
                        }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            setTheme()
                        })
                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.support),
                                contentDescription = "Support"
                            )
                        }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            expanded = false
                            showSupportDialog = true
                        })
                        DropdownMenuItem(text = { Text("IP config") }, leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.tune),
                                contentDescription = "IP config"
                            )
                        }, onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            expanded = false
                            ipConfig()
                        })
                    }
                }
            } else {
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "greeting",

                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(300)
                        )
                    }) {
                    composable("greeting") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = false
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Text(
                                    text = "GChat is secure and optimized for some tasks.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("login") {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_forward),
                                        contentDescription = "Accept and Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 64.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = text,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }

                                AnimatedMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatIsMyBackgroundFilterColor = { _, _ -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                            })
                                    }
                                }
                            }
                        }
                    }

                    composable("login") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = false
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    VerifySimCard(
                                        selectedIccid = selectedIccid,
                                        setSelectedIccid = { value -> selectedIccid = value },
                                        endPadding = 56.dp
                                    )
                                }

                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp), onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = Color(0xFFFFFFFF),
                                    )
                                }
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        navController.navigate("signUp")
                                    },
                                    enabled = !isLoading && !selectedIccid.isNullOrBlank(),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .padding(innerPadding)
                                        .size(56.dp)
                                        .align(Alignment.BottomEnd)
                                        .shadow(
                                            elevation = if (!isLoading && !selectedIccid.isNullOrBlank()) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 64.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .height(56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = signInText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }

                                AnimatedMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatIsMyBackgroundFilterColor = { _, _ -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                            })
                                    }
                                }
                            }
                        }
                    }

                    composable("signIn") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = false
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
                            ) {
                                BackHandler(enabled = isLoading) {

                                }

                                Text(
                                    text = "To sign-in to your account, enter your username and password.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(384.dp)
                                        .padding(24.dp)
                                ) {
                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(usernameFocusRequester),
                                        value = username,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            username = it
                                        },
                                        isError = isUsernameError or usernameSizeError && firstClick,
                                        supportingText = {
                                            Column {
                                                if (isUsernameError && firstClick) {
                                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                                }
                                                if (usernameSizeError && firstClick) {
                                                    Text("Username must be less than 50 characters.")
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = { Text("Username") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                passwordFocusRequester.requestFocus()
                                            })
                                    )

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(passwordFocusRequester),
                                        value = password,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            password = it
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = {
                                            Text(text = "Password")
                                        },
                                        isError = isPasswordError && firstClick,
                                        supportingText = {
                                            if (isPasswordError && firstClick) {
                                                Text("Password must be between 6 and 128 characters.")
                                            }
                                        },
                                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(
                                                enabled = !isLoading, onClick = {
                                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                                    passwordVisible.value = !passwordVisible.value
                                                }) {
                                                Icon(
                                                    painter = painterResource(
                                                        if (passwordVisible.value) R.drawable.visibility_off
                                                        else R.drawable.visibility
                                                    ), contentDescription = null
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(textDirection = TextDirection.Content),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                coroutineScope.launch {
                                                    signIn(username, password)
                                                    isLoading = true
                                                }
                                                focusManager.moveFocus(FocusDirection.Down)
                                            })
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        if (firstClick) {
                                            signIn(username, password)
                                            isLoading = true
                                        } else {
                                            firstClick = true
                                            if (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) {
                                                signIn(username, password)
                                                isLoading = true
                                            }
                                        }
                                    },
                                    enabled = (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick,

                                    modifier = Modifier
                                        .padding(8.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomEnd)
                                        .size(56.dp)
                                        .shadow(
                                            elevation = if ((!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }

                                val progressColor = Color(0xFF296A47).toArgb()

                                if (isLoading) {
                                    AndroidView(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .fillMaxWidth()
                                            .offset(y = (-6).dp),
                                        factory = { context ->
                                            ProgressBar(
                                                context,
                                                null,
                                                android.R.attr.progressBarStyleHorizontal
                                            ).apply {
                                                isIndeterminate = true
                                                progressTintList =
                                                    ColorStateList.valueOf(progressColor)
                                                indeterminateTintList =
                                                    ColorStateList.valueOf(progressColor)
                                            }
                                        })
                                }

                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp), onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                AnimatedMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatIsMyBackgroundFilterColor = { _, _ -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                            })
                                    }
                                }
                            }
                        }
                    }

                    composable("signUp") {

                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = ((LocalConfiguration.current.screenWidthDp - 600) * 0.3f).dp)
                                    .width(600.dp)
                                    .fillMaxHeight(0.8f)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                                        clip = false
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                    )
                            ) {
                                BackHandler(enabled = isLoading) {

                                }

                                Text(
                                    text = "To sign-up for an account, set up a username and password.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(384.dp)
                                        .padding(24.dp)
                                ) {
                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(usernameFocusRequester),
                                        value = username,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            username = it
                                        },
                                        isError = isUsernameError or usernameSizeError && firstClick,
                                        supportingText = {
                                            Column {
                                                if (isUsernameError && firstClick) {
                                                    Text("Only the \"a-z\", \"0-9\" and \"_\" characters are allowed.")
                                                }
                                                if (usernameSizeError && firstClick) {
                                                    Text("Username must be less than 50 characters.")
                                                }
                                            }
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = { Text("Username") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                passwordFocusRequester.requestFocus()
                                            })
                                    )

                                    TextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(passwordFocusRequester),
                                        value = password,
                                        onValueChange = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            password = it
                                        },
                                        enabled = !isLoading,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color(0xFF296A47),
                                            focusedLabelColor = Color(0xFF296A47),
                                            cursorColor = Color(0xFF296A47),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent
                                        ),
                                        label = {
                                            Text(text = "Password")
                                        },
                                        isError = isPasswordError && firstClick,
                                        supportingText = {
                                            if (isPasswordError && firstClick) {
                                                Text("Password must be between 6 and 128 characters.")
                                            }
                                        },
                                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(
                                                enabled = !isLoading, onClick = {
                                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                                    passwordVisible.value = !passwordVisible.value
                                                }) {
                                                Icon(
                                                    painter = painterResource(
                                                        if (passwordVisible.value) R.drawable.visibility_off
                                                        else R.drawable.visibility
                                                    ), contentDescription = null
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(textDirection = TextDirection.Content),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                coroutineScope.launch {
                                                    signIn(username, password)
                                                    isLoading = true
                                                }
                                                focusManager.moveFocus(FocusDirection.Down)
                                            })
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF296A47),
                                        contentColor = Color(0xFFFFFFFF)
                                    ),
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        if (firstClick) {
                                            signUp(selectedIccid.toString(), username, password)
                                            isLoading = true
                                        } else {
                                            firstClick = true
                                            if (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) {
                                                signUp(selectedIccid.toString(), username, password)
                                                isLoading = true
                                            }
                                        }
                                    },
                                    enabled = (!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .padding(innerPadding)
                                        .align(Alignment.BottomEnd)
                                        .size(56.dp)
                                        .shadow(
                                            elevation = if ((!isLoading && !(isUsernameError or usernameSizeError) && username.isNotBlank() && !isPasswordError) or !firstClick) 6.dp else 0.dp,
                                            shape = CircleShape,
                                            clip = false
                                        ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "OK Sign-In",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color(0xFFFFFFFF)
                                    )
                                }

                                val progressColor = Color(0xFF296A47).toArgb()

                                if (isLoading) {
                                    AndroidView(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .fillMaxWidth()
                                            .offset(y = (-6).dp),
                                        factory = { context ->
                                            ProgressBar(
                                                context,
                                                null,
                                                android.R.attr.progressBarStyleHorizontal
                                            ).apply {
                                                isIndeterminate = true
                                                progressTintList =
                                                    ColorStateList.valueOf(progressColor)
                                                indeterminateTintList =
                                                    ColorStateList.valueOf(progressColor)
                                            }
                                        })
                                }

                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp), onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        expanded = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_dots),
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                AnimatedMenu(
                                    modifier = Modifier.zIndex(1f),
                                    width = 200.dp,
                                    height = 160.dp,
                                    chord = 256.dp,
                                    isExpanded = expanded,
                                    close = { expanded = false },
                                    ratioX = 1f,
                                    ratioY = 0f,
                                    position = Alignment.TopEnd,
                                    hasBackgroundCover = false,
                                    whatIsMyBackgroundFilterColor = { _, _ -> }) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DropdownMenuItem(text = {
                                            Text(
                                                text = dropdownThemeText
                                            )
                                        }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(
                                                    id = dropdownThemeIcon
                                                ), contentDescription = "Theme"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            setTheme()
                                        })
                                        DropdownMenuItem(text = { Text("Support") }, leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.support),
                                                contentDescription = "Support"
                                            )
                                        }, onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            expanded = false
                                            showSupportDialog = true
                                        })
                                        DropdownMenuItem(
                                            text = { Text("IP config") },
                                            leadingIcon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.tune),
                                                    contentDescription = "IP config"
                                                )
                                            },
                                            onClick = {
                                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                                expanded = false
                                                ipConfig()
                                            })
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    Box(modifier = Modifier.fillMaxHeight(fraction = 1 / 3f)) { }
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GChat", style = MaterialTheme.typography.displaySmall.copy(
                                color = Color(0xFFDFE4DD)
                            )
                        )
                    }
                }
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Support") },
            text = {
                Text(text = "You can send an SMS to support by clicking the button below.")
            },
            shape = RoundedCornerShape(2.dp),
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF296A47),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        val phoneNumber = "09369152046"
                        val uri = "sms:$phoneNumber".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(2.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sms), contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS")
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF296A47)
                    ),
                    shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSupportDialog = false
                    }) {
                    Text("Dismiss")
                }
            },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 24.dp, shape = RoundedCornerShape(2.dp), clip = false
                )
                .fillMaxHeight(0.8f),
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Term of use and privacy") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = getRules())
                }
            },
            shape = RoundedCornerShape(2.dp),
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF296A47)
                    ),
                    shape = RoundedCornerShape(2.dp), onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showTermsDialog = false
                    }) {
                    Text("Dismiss")
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySimCard(selectedIccid: String?, setSelectedIccid: (String) -> Unit, endPadding: Dp) {

    val context = LocalContext.current
    val view = LocalView.current

    var iccidList by remember { mutableStateOf(emptyList<String>()) }

    var hasPhonePermission by remember { mutableStateOf(false) }

    var hasSmsAppRole by remember { mutableStateOf(false) }

    LaunchedEffect(context, hasPhonePermission, hasSmsAppRole) {
        iccidList = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            getIccidsFromSubscriptionManager(context)
        } else {
            getICCIDList(context)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    fun refreshPermissions() {
        hasPhonePermission = checkPhonePermission(context)

        hasSmsAppRole = checkSmsAppRole(context)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val smsRoleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp),
            color = Color(0xFF3B6471),
            shape = RectangleShape
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sim_toolkit),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = "The ICCID is the unique identifier of your SIM card. Use it to verify your identity.",
                        color = Color(0xFFFFFFFF),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = endPadding)
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasSmsAppRole) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        shadowElevation = 4.dp,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            val roleManager = context.getSystemService(RoleManager::class.java)

                            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(
                                    RoleManager.ROLE_SMS
                                )
                            ) {
                                smsRoleLauncher.launch(
                                    roleManager.createRequestRoleIntent(
                                        RoleManager.ROLE_SMS
                                    )
                                )
                            }
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.warning_shield),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 12.dp)
                            )
                            Text(
                                text = "Reading the ICCID requires this app to be the default SMS app. You can switch back to your previous default SMS app at any time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                modifier = Modifier.size(48.dp), onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    val roleManager =
                                        context.getSystemService(RoleManager::class.java)

                                    if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(
                                            RoleManager.ROLE_SMS
                                        )
                                    ) {
                                        smsRoleLauncher.launch(
                                            roleManager.createRequestRoleIntent(
                                                RoleManager.ROLE_SMS
                                            )
                                        )
                                    }
                                }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.security),
                                    contentDescription = "Set default SMS app",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else if (!hasPhonePermission) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        shadowElevation = 4.dp,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.warning_shield),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 12.dp)
                            )
                            Text(
                                text = "Phone permission is required to read the ICCID.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                modifier = Modifier.size(48.dp), onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                                }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.security),
                                    contentDescription = "Grant Phone permission",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        iccidList.forEachIndexed { index, iccid ->
            Surface(
                modifier = Modifier
                    .padding(
                        start = 8.dp,
                        top = if (index != 0) 0.dp else 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                    .fillMaxWidth(),
                shadowElevation = 4.dp,
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    setSelectedIccid(iccid)
                },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF296A47), // primary light
                        ),
                        selected = selectedIccid == iccid,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            setSelectedIccid(iccid)
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Column {
                        Text(
                            text = "Slot ${index + 1}", color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ICCID: $iccid",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}