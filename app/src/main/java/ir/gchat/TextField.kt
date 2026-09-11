package ir.gchat

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Job
import kotlin.collections.forEach
import kotlin.math.roundToInt

fun Spanned.toAnnotatedString(): AnnotatedString {

    return buildAnnotatedString {

        append(toString())

        val spans = getSpans(
            0, length, CharacterStyle::class.java
        )

        spans.forEach { span ->

            val start = getSpanStart(span)
            val end = getSpanEnd(span)

            if (start < 0 || end <= start) {
                return@forEach
            }

            when (span) {

                is StyleSpan -> {

                    when (span.style) {

                        Typeface.BOLD -> {
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold
                                ), start, end
                            )
                        }

                        Typeface.ITALIC -> {
                            addStyle(
                                SpanStyle(
                                    fontStyle = FontStyle.Italic
                                ), start, end
                            )
                        }

                        Typeface.BOLD_ITALIC -> {
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic
                                ), start, end
                            )
                        }
                    }
                }
            }
        }
    }
}

fun toggleStyle(
    editable: Editable, start: Int, end: Int, style: Int
) {
    if (start !in 0..<end) {
        return
    }

    val spans = editable.getSpans(
        start, end, StyleSpan::class.java
    )

    /*
     * آیا کل selection از قبل همین style را دارد؟
     *
     * اگر بله → باید Unbold / Unitalic شود.
     * اگر نه → باید Bold / Italic شود.
     */
    val fullyStyled = (start until end).all { index ->

        val styles = editable.getSpans(
            index, index + 1, StyleSpan::class.java
        )

        styles.any { span ->
            span.style == style
        }
    }

    if (!fullyStyled) {

        // ============================================================
        // ADD STYLE
        // ============================================================

        editable.setSpan(
            StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return
    }

    // ================================================================
    // REMOVE STYLE
    // ================================================================

    spans.filter { span ->
        span.style == style
    }.forEach { span ->

        val spanStart = editable.getSpanStart(span)

        val spanEnd = editable.getSpanEnd(span)

        /*
         * Span را اول حذف می‌کنیم.
         */
        editable.removeSpan(span)

        /*
         * ========================================================
         * قسمت قبل از Selection
         * ========================================================
         *
         * مثلاً:
         *
         * [Hello][ World]
         *       ^^^^^
         *
         * قسمت Hello باید همچنان Bold بماند.
         */
        if (spanStart < start) {

            editable.setSpan(
                StyleSpan(style), spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        /*
         * ========================================================
         * قسمت بعد از Selection
         * ========================================================
         *
         * مثلاً:
         *
         * [Hello][ World]
         *        ^^^^^
         *
         * قسمت بعدی هم باید Bold بماند.
         */
        if (spanEnd > end) {

            editable.setSpan(
                StyleSpan(style), end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}

fun <T : CharacterStyle> toggleSimpleSpan(
    editable: Editable, start: Int, end: Int, spanClass: Class<T>, createSpan: () -> T
) {
    if (start !in 0..<end) return

    val spans = editable.getSpans(
        start, end, spanClass
    )

    val fullyStyled = (start until end).all { index ->

        editable.getSpans(
            index, index + 1, spanClass
        ).isNotEmpty()
    }

    if (!fullyStyled) {

        editable.setSpan(
            createSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return
    }

    spans.forEach { span ->

        val spanStart = editable.getSpanStart(span)
        val spanEnd = editable.getSpanEnd(span)

        editable.removeSpan(span)

        // قسمت قبل از Selection
        if (spanStart < start) {
            editable.setSpan(
                createSpan(), spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // قسمت بعد از Selection
        if (spanEnd > end) {
            editable.setSpan(
                createSpan(), end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}

fun toggleColorSpan(
    editable: Editable, start: Int, end: Int, color: Int
) {
    if (start !in 0..<end) return

    val spans = editable.getSpans(
        start, end, ForegroundColorSpan::class.java
    )

    val fullyColored = (start until end).all { index ->

        editable.getSpans(
            index, index + 1, ForegroundColorSpan::class.java
        ).any { span ->
            span.foregroundColor == color
        }
    }

    if (fullyColored) {

        spans.filter { it.foregroundColor == color }.forEach { span ->

            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            editable.removeSpan(span)

            if (spanStart < start) {
                editable.setSpan(
                    ForegroundColorSpan(color),
                    spanStart,
                    start,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            if (spanEnd > end) {
                editable.setSpan(
                    ForegroundColorSpan(color), end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

    } else {

        editable.setSpan(
            ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun toggleHighlightSpan(
    editable: Editable, start: Int, end: Int, color: Int
) {
    if (start !in 0..<end) return

    val spans = editable.getSpans(
        start, end, BackgroundColorSpan::class.java
    )

    val fullyHighlighted = (start until end).all { index ->

        editable.getSpans(
            index, index + 1, BackgroundColorSpan::class.java
        ).any { span ->
            span.backgroundColor == color
        }
    }

    if (fullyHighlighted) {

        spans.filter { it.backgroundColor == color }.forEach { span ->

            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            editable.removeSpan(span)

            if (spanStart < start) {
                editable.setSpan(
                    BackgroundColorSpan(color),
                    spanStart,
                    start,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            if (spanEnd > end) {
                editable.setSpan(
                    BackgroundColorSpan(color), end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

    } else {

        editable.setSpan(
            BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun Spanned.toMarkdown(): String {

    if (isEmpty()) {
        return ""
    }

    val result = StringBuilder()

    var index = 0

    while (index < length) {

        val nextBold = getSpans(
            index, index + 1, StyleSpan::class.java
        ).any {
            it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC
        }

        val nextItalic = getSpans(
            index, index + 1, StyleSpan::class.java
        ).any {
            it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC
        }

        val currentChar = this[index]

        /*
         * پیدا کردن محدوده‌ای که formatting فعلی
         * روی آن یکسان است.
         */
        var end = index + 1

        while (end < length) {

            val bold = getSpans(
                end, end + 1, StyleSpan::class.java
            ).any {
                it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC
            }

            val italic = getSpans(
                end, end + 1, StyleSpan::class.java
            ).any {
                it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC
            }

            if (bold != nextBold || italic != nextItalic) {
                break
            }

            end++
        }

        val chunk = substring(index, end)

        when {
            nextBold && nextItalic -> {
                result.append("**_")
                result.append(chunk)
                result.append("_**")
            }

            nextBold -> {
                result.append("**")
                result.append(chunk)
                result.append("**")
            }

            nextItalic -> {
                result.append("_")
                result.append(chunk)
                result.append("_")
            }

            else -> {
                result.append(chunk)
            }
        }

        index = end
    }

    return result.toString()
}

fun showColorPicker(
    context: Context, anchor: View, colors: List<Int>, onColorSelected: (Int) -> Unit
) {
    val density = context.resources.displayMetrics.density

    fun Int.dp(): Int {
        return (this * density).roundToInt()
    }

    val grid = GridLayout(context).apply {

        columnCount = 3
        rowCount = 2

        setPadding(
            12.dp(), 12.dp(), 12.dp(), 12.dp()
        )

        background = GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            cornerRadius = 16.dp().toFloat()

            setColor(
                android.graphics.Color.WHITE
            )
        }
    }

    val popupWindow = PopupWindow(
        grid, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true
    ).apply {

        elevation = 12.dp().toFloat()

        isOutsideTouchable = true

        isFocusable = true
    }

    colors.forEachIndexed { index, color ->

        val colorView = View(context).apply {

            background = GradientDrawable().apply {

                shape = GradientDrawable.OVAL

                setColor(color)
            }

            isClickable = true

            setOnClickListener {

                onColorSelected(color)

                popupWindow.dismiss()
            }
        }

        val params = GridLayout.LayoutParams().apply {

            width = 40.dp()
            height = 40.dp()

            setMargins(
                8.dp(), 8.dp(), 8.dp(), 8.dp()
            )

            rowSpec = GridLayout.spec(
                index / 3
            )

            columnSpec = GridLayout.spec(
                index % 3
            )
        }

        grid.addView(colorView, params)
    }

    popupWindow.showAsDropDown(
        anchor, 0, -anchor.height
    )
}

@Composable
fun MessageTextField(
    sendWith: SendMessageWith,
    setMessageText: (String) -> Unit,
    showFileRow: Boolean,
    showAnimation: () -> Job,
    draftUpdate: (MutableList<Content>) -> Unit,
    draftSize: Int,
    sendMessage: (List<Content>) -> Unit,
    messageText: String,
    setSavedText: (String) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val textEditHint = stringResource(R.string.message)

    val boldActionId = 1001
    val italicActionId = 1002
    val underlineActionId = 1003
    val strikethroughActionId = 1004

    val textColorActionId = 1005
    val highlightActionId = 1006

    var styledMessageText by remember {
        mutableStateOf(
            SpannableStringBuilder()
        )
    }

    var annotatedMessageText by remember {
        mutableStateOf(
            AnnotatedString("")
        )
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),

        factory = { context ->

            EditText(context).apply {

                /*
                 * خیلی مهم:
                 *
                 * داخل object : ActionMode.Callback
                 * عبارت this به ActionMode.Callback اشاره می‌کند،
                 * نه به EditText.
                 *
                 * بنابراین خود EditText را ذخیره می‌کنیم.
                 */
                val editTextView = this

                background = null

                maxLines = 5

                hint = textEditHint

                setHintTextColor(
                    android.graphics.Color.GRAY
                )

                setTextColor(
                    onSurface.toArgb()
                )

                customSelectionActionModeCallback =
                    object : ActionMode.Callback {

                        override fun onCreateActionMode(
                            mode: ActionMode, menu: Menu
                        ): Boolean {

                            // ------------------------------------------------
                            // BOLD
                            // ------------------------------------------------

                            menu.add(
                                Menu.NONE, boldActionId, 100, "Bold"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS
                            )

                            // ------------------------------------------------
                            // ITALIC
                            // ------------------------------------------------

                            menu.add(
                                Menu.NONE, italicActionId, 101, "Italic"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS
                            )

                            // ------------------------------------------------
                            // UNDERLINE
                            // ------------------------------------------------

                            menu.add(
                                Menu.NONE,
                                underlineActionId,
                                102,
                                "Underline"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS
                            )

                            // ------------------------------------------------
                            // STRIKETHROUGH
                            // ------------------------------------------------

                            menu.add(
                                Menu.NONE,
                                strikethroughActionId,
                                103,
                                "Strikethrough"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS
                            )

                            menu.add(
                                Menu.NONE,
                                textColorActionId,
                                104,
                                "Text Color"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_NEVER
                            )

                            menu.add(
                                Menu.NONE,
                                highlightActionId,
                                105,
                                "Highlight"
                            ).setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_NEVER
                            )

                            return true
                        }

                        override fun onPrepareActionMode(
                            mode: ActionMode, menu: Menu
                        ): Boolean {
                            return false
                        }

                        override fun onActionItemClicked(
                            mode: ActionMode, item: MenuItem
                        ): Boolean {

                            val start = editTextView.selectionStart
                            val end = editTextView.selectionEnd

                            if (start !in 0..<end) {
                                mode.finish()
                                return true
                            }

                            when (item.itemId) {

                                boldActionId -> {

                                    toggleStyle(
                                        editable = editTextView.editableText,
                                        start = start,
                                        end = end,
                                        style = Typeface.BOLD
                                    )

                                    styledMessageText =
                                        SpannableStringBuilder(
                                            editTextView.editableText
                                        )

                                    annotatedMessageText =
                                        styledMessageText.toAnnotatedString()

                                    mode.finish()

                                    return true
                                }

                                italicActionId -> {

                                    toggleStyle(
                                        editable = editTextView.editableText,
                                        start = start,
                                        end = end,
                                        style = Typeface.ITALIC
                                    )

                                    styledMessageText =
                                        SpannableStringBuilder(
                                            editTextView.editableText
                                        )

                                    annotatedMessageText =
                                        styledMessageText.toAnnotatedString()

                                    mode.finish()

                                    return true
                                }

                                underlineActionId -> {

                                    toggleSimpleSpan(
                                        editable = editTextView.editableText,
                                        start = start,
                                        end = end,
                                        spanClass = UnderlineSpan::class.java,
                                        createSpan = {
                                            UnderlineSpan()
                                        })

                                    styledMessageText =
                                        SpannableStringBuilder(
                                            editTextView.editableText
                                        )

                                    annotatedMessageText =
                                        styledMessageText.toAnnotatedString()

                                    mode.finish()

                                    return true
                                }

                                strikethroughActionId -> {

                                    toggleSimpleSpan(
                                        editable = editTextView.editableText,
                                        start = start,
                                        end = end,
                                        spanClass = StrikethroughSpan::class.java,
                                        createSpan = {
                                            StrikethroughSpan()
                                        })

                                    styledMessageText =
                                        SpannableStringBuilder(
                                            editTextView.editableText
                                        )

                                    annotatedMessageText =
                                        styledMessageText.toAnnotatedString()

                                    mode.finish()

                                    return true
                                }

                                textColorActionId -> {
                                    showColorPicker(
                                        context = context,
                                        anchor = editTextView,
                                        colors = listOf(

                                            // Black
                                            android.graphics.Color.BLACK,

                                            // Red
                                            android.graphics.Color.RED,

                                            // Blue
                                            android.graphics.Color.BLUE,

                                            // Green
                                            android.graphics.Color.GREEN,

                                            // Orange
                                            android.graphics.Color.rgb(
                                                255, 152, 0
                                            ),

                                            // Purple
                                            android.graphics.Color.rgb(
                                                156, 39, 176
                                            )
                                        )
                                    ) { color ->

                                        toggleColorSpan(
                                            editable = editTextView.editableText,
                                            start = start,
                                            end = end,
                                            color = color
                                        )

                                        styledMessageText =
                                            SpannableStringBuilder(
                                                editTextView.editableText
                                            )

                                        annotatedMessageText =
                                            styledMessageText.toAnnotatedString()

                                        mode.finish()
                                    }

                                    return true
                                }

                                highlightActionId -> {

                                    showColorPicker(
                                        context = context,
                                        anchor = editTextView,
                                        colors = listOf(

                                            // Yellow
                                            android.graphics.Color.YELLOW,

                                            // Green
                                            android.graphics.Color.rgb(
                                                139, 195, 74
                                            ),

                                            // Blue
                                            android.graphics.Color.rgb(
                                                100, 181, 246
                                            ),

                                            // Pink
                                            android.graphics.Color.rgb(
                                                244, 143, 177
                                            ),

                                            // Orange
                                            android.graphics.Color.rgb(
                                                255, 183, 77
                                            ),

                                            // Purple
                                            android.graphics.Color.rgb(
                                                186, 104, 200
                                            )
                                        )
                                    ) { color ->

                                        toggleHighlightSpan(
                                            editable = editTextView.editableText,
                                            start = start,
                                            end = end,
                                            color = color
                                        )

                                        styledMessageText =
                                            SpannableStringBuilder(
                                                editTextView.editableText
                                            )

                                        annotatedMessageText =
                                            styledMessageText.toAnnotatedString()

                                        mode.finish()
                                    }

                                    return true
                                }

                                else -> { return false }
                            }
                        }

                        override fun onDestroyActionMode(
                            mode: ActionMode
                        ) {
                        }
                    }

                setOnKeyListener { _, keyCode, event ->

                    if (keyCode != KeyEvent.KEYCODE_ENTER || event.action != KeyEvent.ACTION_DOWN) {
                        return@setOnKeyListener false
                    }

                    val shift = event.isShiftPressed
                    val ctrl = event.isCtrlPressed
                    val alt = event.isAltPressed

                    val send = when {

                        !shift && !ctrl && !alt -> sendWith.enter

                        shift && !ctrl && !alt -> sendWith.shiftEnter

                        !shift && ctrl && !alt -> sendWith.ctrlEnter

                        !shift && !ctrl && alt -> sendWith.altEnter

                        else -> false
                    }

                    Log.d(
                        "KEY_EVENT",
                        "ENTER | shift=$shift | ctrl=$ctrl | alt=$alt | send=$send"
                    )

                    if (!send) {
                        return@setOnKeyListener false
                    }

                    val currentStyledText = SpannableStringBuilder(
                        editTextView.editableText
                    )

                    val plainText =
                        currentStyledText.toString().replace(
                            Regex("\\n+$"), ""
                        ).trim()

                    // اینجا رو چک کن
                    if (plainText.isBlank() && !showFileRow) {

                        Log.d(
                            "KEY_EVENT", "SEND CANCELLED"
                        )

                        return@setOnKeyListener true
                    }

                    val formattedText = SpannableStringBuilder(
                        currentStyledText
                    )

                    while (formattedText.isNotEmpty() && formattedText.last()
                            .isWhitespace()
                    ) {

                        formattedText.delete(
                            formattedText.length - 1,
                            formattedText.length
                        )
                    }

                    val markdownText = formattedText.toMarkdown()

                    styledMessageText = SpannableStringBuilder(
                        formattedText
                    )

                    annotatedMessageText = styledMessageText.toAnnotatedString()

                    setMessageText(formattedText.toString())

                    showAnimation()

                    val content = mutableListOf<Content>()

                    draftUpdate(content)

                    if (markdownText.isNotBlank()) {
                        content += Content.Text(text = markdownText)
                    }

                    Log.d(
                        "KEY_EVENT",
                        "SEND | " + "textLength=${markdownText.length} | " + "draftItems=${draftSize} | " + "contentItems=${content.size}")

                    sendMessage(content)

                    styledMessageText = SpannableStringBuilder()

                    annotatedMessageText = AnnotatedString("")

                    editTextView.editableText.clear()

                    return@setOnKeyListener true
                }

                addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        setMessageText(s?.toString().orEmpty())

                        setSavedText(messageText)
                    }

                    override fun afterTextChanged(
                        s: Editable?
                    ) {
                        if (s == null) { return }

                        styledMessageText = SpannableStringBuilder(s)

                        annotatedMessageText = styledMessageText.toAnnotatedString()
                    }
                })
            }
        },

        update = { editText ->

            val currentText = editText.editableText.toString()

            if (currentText == messageText) {
                return@AndroidView
            }

            val value: CharSequence =
                if (styledMessageText.toString() == messageText) {
                    SpannableString(styledMessageText)
                } else {
                    SpannableString(messageText)
                }

            editText.setText(value, TextView.BufferType.SPANNABLE)

            editText.setSelection(editText.editableText.length)
        })
}