package ir.gchat

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import com.hrm.latex.renderer.model.LatexThemeColors
//import io.ratex.compose.RaTeX
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Message(
    isMe: Boolean,
    content: List<Content>,
    seen: Boolean,
    timestamp: String,
    context: Context,
    downloadFile: (Int, String, Long, (Float) -> Unit, (Boolean) -> Unit, (Long) -> Unit) -> Unit,
    imageLoader: ImageLoader,
    serverUrl: String,
    isFileDownloaded: (String) -> Boolean,
    openMenu: () -> Unit,
    playSet: (File?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {
                openMenu()
            }) {
        var lineCount by remember(content) { mutableIntStateOf(0) }
        val formatMessageTime =
            if (context.isRtl()) formatMessageTimeJalali(timestamp) else formatMessageTime(timestamp)
        var timeWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            val maxMessageWidth = minOf(maxWidth * 0.8f, 480.dp)
            Surface(
                color = if (isMe) /*Color(0xFFEEFFDE)*/ MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(size = 2.dp),
                modifier = Modifier.widthIn(max = maxMessageWidth),
                //shadowElevation = 2.dp
            ) {
                Box {
                    Column {
                        println(content)
                        content.forEachIndexed { index, contentEntity ->
                            if (index == content.size - 1) {
                                when (contentEntity) {
                                    is Content.Text -> {
                                        var isOverflowed by remember {
                                            mutableStateOf(false)
                                        }
                                        var overflowMode by remember { mutableStateOf(TextOverflow.Ellipsis) }
                                        var maxLinesMode by remember { mutableIntStateOf(10) }
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .padding(
                                                    end = if (isMe && lineCount == 1 && seen) timeWidth + 26.dp else if (lineCount == 1) timeWidth + 8.dp else 0.dp,
                                                    bottom = if (lineCount > 1) 24.dp else 0.dp
                                                )
                                        ) {
                                            Text(
                                                // ممکنه مشکل پیش بیاد
                                                // تکست های چند تایی چی؟؟؟
                                                text = contentEntity.text.toRichAnnotatedString(
                                                    linkColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                onTextLayout = {
                                                    if (lineCount == 0) {
                                                        lineCount = it.lineCount
                                                    }

                                                    isOverflowed = it.hasVisualOverflow
                                                },
                                                maxLines = maxLinesMode,
                                                overflow = overflowMode,
                                            )
                                            if (isOverflowed) {
                                                val context = LocalContext.current
                                                val layoutDirection = if (context.isRtl()) {
                                                    LayoutDirection.Rtl
                                                } else {
                                                    LayoutDirection.Ltr
                                                }
                                                CompositionLocalProvider(
                                                    LocalLayoutDirection provides layoutDirection
                                                ) {
                                                    TextButton(
                                                        shape = RoundedCornerShape(2.dp),
                                                        onClick = {
                                                            overflowMode = TextOverflow.Visible
                                                            maxLinesMode = Int.MAX_VALUE
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color.Transparent, contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    ) {
                                                        DisableSelection {
                                                            Text(stringResource(R.string.show_more))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    is Content.LaTeX -> {
                                        println(contentEntity.text)
                                        Box(
                                            modifier = Modifier.horizontalScroll(
                                                rememberScrollState()
                                            )
                                        ) {
                                            RaTeX(
                                                modifier = Modifier.wrapContentWidth().padding(8.dp).padding(bottom = 24.dp),
                                                latex = contentEntity.text,
                                                fontSize = 18f.sp,
                                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                displayMode = true
                                            )
                                        }
                                    }

                                    is Content.File -> {
                                        val extension =
                                            contentEntity.fileName.substringAfterLast(".", "")
                                                .takeIf { it.isNotEmpty() }

                                        val downloadName = if (extension != null) {
                                            "${contentEntity.id}.$extension"
                                        } else {
                                            contentEntity.id.toString()
                                        }
                                        var downloadProgress by remember {
                                            mutableFloatStateOf(
                                                0f
                                            )
                                        }
                                        var pending by remember { mutableStateOf(false) }
                                        val progressColor =
                                            if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        var downloaded by remember { mutableLongStateOf(0L) }
                                        var showProgress by remember { mutableStateOf(false) }

                                        LaunchedEffect(downloadProgress) {
                                            if (downloadProgress > 0f && downloadProgress < 1f) {
                                                showProgress = true
                                            }

                                            if (downloadProgress == 1f) {
                                                delay(1000.milliseconds)
                                                showProgress = false
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                //.fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    brush = if (!showProgress) {
                                                        SolidColor(Color.Transparent)
                                                    } else {
                                                        Brush.horizontalGradient(
                                                            colorStops = arrayOf(
                                                                0f to progressColor.copy(alpha = 0.2f),
                                                                downloadProgress to progressColor.copy(
                                                                    alpha = 0.2f
                                                                ),
                                                                downloadProgress to Color.Transparent,
                                                                1f to Color.Transparent
                                                            )
                                                        )
                                                    }
                                                )
                                                .clickable {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                },
                                                enabled = !pending
                                            ) {
                                                val thumbnailUrl =
                                                    "http://${serverUrl.substringBefore(":")}:8080/thumb/${contentEntity.id}"

                                                Log.d(
                                                    "THUMB",
                                                    "id=${contentEntity.id}, " + "serverUrl=$serverUrl, " + "url=$thumbnailUrl"
                                                )

                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.draft),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                    AsyncImage(
                                                        model = thumbnailUrl,
                                                        imageLoader = imageLoader,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        onLoading = {
                                                            Log.d(
                                                                "THUMB", "LOADING: $thumbnailUrl"
                                                            )
                                                        },
                                                        onSuccess = {
                                                            Log.d(
                                                                "THUMB", "SUCCESS: $thumbnailUrl"
                                                            )
                                                        },
                                                        onError = {
                                                            Log.e(
                                                                "THUMB",
                                                                "ERROR: $thumbnailUrl",
                                                                it.result.throwable
                                                            )
                                                        })
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(
                                                //modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                DisableSelection {
                                                    Text(
                                                        text = contentEntity.fileName,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Text(
                                                        text = buildString {
                                                            if (downloadProgress != 0f && downloadProgress != 1f) {
                                                                append(formatFileSize(downloaded))
                                                                append(" / ")
                                                            }
                                                            append(formatFileSize(contentEntity.fileSize))
                                                        },
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                when (contentEntity) {
                                    is Content.Text -> {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = contentEntity.text.toRichAnnotatedString(
                                                linkColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }

                                    is Content.LaTeX -> {
                                        Box(
                                            modifier = Modifier.horizontalScroll(
                                                rememberScrollState()
                                            )
                                        ) {
                                            RaTeX(
                                                modifier = Modifier.wrapContentWidth().padding(8.dp),
                                                latex = contentEntity.text,
                                                fontSize = 18f.sp,
                                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                displayMode = true
                                            )
                                        }
                                    }

                                    is Content.File -> {
                                        val extension =
                                            contentEntity.fileName.substringAfterLast(".", "")
                                                .takeIf { it.isNotEmpty() }

                                        val downloadName = if (extension != null) {
                                            "${contentEntity.id}.$extension"
                                        } else {
                                            contentEntity.id.toString()
                                        }
                                        var downloadProgress by remember {
                                            mutableFloatStateOf(
                                                0f
                                            )
                                        }
                                        var pending by remember { mutableStateOf(false) }
                                        val progressColor =
                                            if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        var downloaded by remember { mutableLongStateOf(0L) }
                                        var showProgress by remember { mutableStateOf(false) }

                                        LaunchedEffect(downloadProgress) {
                                            if (downloadProgress > 0f && downloadProgress < 1f) {
                                                showProgress = true
                                            }

                                            if (downloadProgress == 1f) {
                                                delay(1000.milliseconds)
                                                showProgress = false
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .background(
                                                    brush = if (!showProgress) {
                                                        SolidColor(Color.Transparent)
                                                    } else {
                                                        Brush.horizontalGradient(
                                                            colorStops = arrayOf(
                                                                0f to progressColor.copy(alpha = 0.2f),
                                                                downloadProgress to progressColor.copy(
                                                                    alpha = 0.2f
                                                                ),
                                                                downloadProgress to Color.Transparent,
                                                                1f to Color.Transparent
                                                            )
                                                        )
                                                    }
                                                )
                                                .clickable {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                onClick = {
                                                    if (isFileDownloaded(downloadName)) {
                                                        val file = File(
                                                            context.filesDir,
                                                            "downloads/$downloadName"
                                                        )

                                                        if (isAudioFile(file)) {
                                                            playSet(file)
                                                        } else {
                                                            openDownloadedFile(
                                                                context = context,
                                                                fileName = downloadName
                                                            )
                                                        }
                                                    } else {
                                                        if (!pending) {
                                                            val extension =
                                                                contentEntity.fileName.substringAfterLast(
                                                                    ".", ""
                                                                ).takeIf { it.isNotEmpty() }

                                                            val downloadName =
                                                                if (extension != null) {
                                                                    "${contentEntity.id}.$extension"
                                                                } else {
                                                                    contentEntity.id.toString()
                                                                }

                                                            downloadFile(
                                                                contentEntity.id,
                                                                downloadName,
                                                                contentEntity.fileSize,
                                                                { progress ->
                                                                    downloadProgress = progress
                                                                },
                                                                { newPending ->
                                                                    pending = newPending
                                                                },
                                                                { downloadedBytes ->
                                                                    downloaded = downloadedBytes
                                                                })
                                                        }
                                                    }
                                                },
                                                enabled = !pending
                                            ) {
                                                val thumbnailUrl =
                                                    "http://${serverUrl.substringBefore(":")}:8080/thumb/${contentEntity.id}"

                                                Log.d(
                                                    "THUMB",
                                                    "id=${contentEntity.id}, " + "serverUrl=$serverUrl, " + "url=$thumbnailUrl"
                                                )

                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.draft),
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                    AsyncImage(
                                                        model = thumbnailUrl,
                                                        imageLoader = imageLoader,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop,
                                                        onLoading = {
                                                            Log.d(
                                                                "THUMB", "LOADING: $thumbnailUrl"
                                                            )
                                                        },
                                                        onSuccess = {
                                                            Log.d(
                                                                "THUMB", "SUCCESS: $thumbnailUrl"
                                                            )
                                                        },
                                                        onError = {
                                                            Log.e(
                                                                "THUMB",
                                                                "ERROR: $thumbnailUrl",
                                                                it.result.throwable
                                                            )
                                                        })
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = contentEntity.fileName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = buildString {
                                                        if (downloadProgress != 0f && downloadProgress != 1f) {
                                                            append(formatFileSize(downloaded))
                                                            append(" / ")
                                                        }
                                                        append(formatFileSize(contentEntity.fileSize))
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(if (lineCount == 1 && content.size == 1) Alignment.CenterEnd else Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DisableSelection {
                            Text(
                                text = formatMessageTime,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontStyle = FontStyle.Normal,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                onTextLayout = {
                                    timeWidth = with(density) { it.size.width.toDp() }
                                })
                            if (isMe && seen) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.double_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}