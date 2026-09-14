package ir.gchat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import com.hrm.latex.renderer.model.LatexThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaTeXSuperEditor(
    initialText: String = "", onTextSubmit: (String) -> Unit = {}, onDismiss: () -> Unit = {}
) {
    var textFieldValue by rememberSaveable(
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            TextFieldValue(
                text = initialText, selection = TextRange(initialText.length)
            )
        )
    }

    var category by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }

    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val toolbarScroll = rememberScrollState()

    val text = textFieldValue.text

    /*
     * درج متن در محل cursor یا جایگزینی selection
     */
    fun insert(
        value: String, cursor: Int = value.length
    ) {
        val current = textFieldValue
        val currentText = current.text

        val start = current.selection.start.coerceIn(0, currentText.length)

        val end = current.selection.end.coerceIn(0, currentText.length)

        val from = minOf(start, end)
        val to = maxOf(start, end)

        val newText = currentText.substring(0, from) + value + currentText.substring(to)

        val newCursor = (from + cursor).coerceIn(0, newText.length)

        textFieldValue = TextFieldValue(
            text = newText, selection = TextRange(newCursor)
        )
    }

    /*
     * قرار دادن before و after دور selection
     *
     * اگر چیزی انتخاب نشده باشد:
     *
     *     cursor
     *
     * تبدیل می‌شود به:
     *
     *     before|after
     *
     * اگر selection وجود داشته باشد:
     *
     *     abc[x]def
     *
     * تبدیل می‌شود به:
     *
     *     abc[before x after]def
     */
    fun wrap(
        before: String, after: String
    ) {
        val current = textFieldValue
        val currentText = current.text

        val start = current.selection.start.coerceIn(0, currentText.length)

        val end = current.selection.end.coerceIn(0, currentText.length)

        val from = minOf(start, end)
        val to = maxOf(start, end)

        val selected = currentText.substring(from, to)

        val replacement = before + selected + after

        val newText = currentText.substring(0, from) + replacement + currentText.substring(to)

        /*
         * اگر selection خالی باشد،
         * cursor را بین before و after می‌گذاریم.
         *
         * مثال:
         *
         * \sqrt{|}
         */
        val newCursor = if (selected.isEmpty()) {
            from + before.length
        } else {/*
                 * اگر متن انتخاب شده باشد،
                 * بعد از کل عبارت قرار می‌گیرد.
                 *
                 * مثال:
                 *
                 * \sqrt{x}|
                 */
            from + before.length + selected.length + after.length
        }

        textFieldValue = TextFieldValue(
            text = newText, selection = TextRange(
                newCursor.coerceIn(0, newText.length)
            )
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,

        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },

        shape = RectangleShape,

        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,

        sheetGesturesEnabled = false,

        dragHandle = {}) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                )
        ) {

            /*
             * Header
             */
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(R.string.latex_editor),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        zoom = (zoom + 0.1f).coerceAtMost(2f)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_in),
                        contentDescription = "Zoom in"
                    )
                }

                IconButton(
                    onClick = {
                        zoom = (zoom - 0.1f).coerceAtLeast(0.5f)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.zoom_out),
                        contentDescription = "Zoom out"
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.close), contentDescription = "Close"
                    )
                }

                IconButton(
                    enabled = text.isNotBlank(), onClick = {
                        scope.launch {
                            sheetState.hide()
                            onTextSubmit(text)
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.check), contentDescription = "Done"
                    )
                }
            }

            /*
             * Preview
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(
                        bottom = 4.dp, top = 8.dp
                    ), shape = RoundedCornerShape(2.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(12.dp),

                    contentAlignment = Alignment.Center
                ) {

                    if (text.isBlank()) {

                        Text(
                            text = stringResource(R.string.preview), color = Color.Gray
                        )

                    } else {
                        Latex(
                            modifier = Modifier.wrapContentWidth(),
                            latex = text,
                            config = LatexConfig(
                                fontSize = (18f * zoom).sp,
                                theme = LatexTheme.auto(
                                    light = LatexThemeColors(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    dark = LatexThemeColors(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            )
                        )
                        //RaTeX(
                        //    modifier = Modifier.wrapContentWidth(),
                        //    latex = text,
                        //    fontSize = (18f * zoom).sp,
                        //    color = MaterialTheme.colorScheme.onSurface,
                        //    displayMode = true
                        //)
                    }
                }
            }

            /*
             * Text editor
             */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(vertical = 4.dp),

                shape = RoundedCornerShape(2.dp)
            ) {

                val editTextHint = stringResource(
                    R.string.write_your_latex_here
                )

                TextField(
                    value = textFieldValue,

                    onValueChange = { newValue ->
                        /*
                         * این مهم‌ترین قسمت است.
                         *
                         * متن + cursor + selection
                         * همگی از TextFieldValue می‌آیند.
                         *
                         * بنابراین اگر کاربر:
                         *
                         * - cursor را جابه‌جا کند
                         * - متن را انتخاب کند
                         * - وسط متن تایپ کند
                         * - Backspace بزند
                         * - متن را paste کند
                         *
                         * state کاملاً با TextField هماهنگ می‌ماند.
                         */
                        textFieldValue = newValue
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),

                    placeholder = {
                        Text(
                            text = editTextHint, color = Color.Gray
                        )
                    },

                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),

                    singleLine = false,

                    maxLines = 5,

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Default
                    ),

                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,

                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,

                        focusedTextColor = MaterialTheme.colorScheme.onSurface,

                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                        disabledTextColor = MaterialTheme.colorScheme.onSurface,

                        errorTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

                shape = RoundedCornerShape(2.dp)
            ) {

                Column {

                    ScrollableTabRow(
                        modifier = Modifier.fillMaxWidth(),

                        selectedTabIndex = category,

                        edgePadding = 0.dp
                    ) {

                        listOf(
                            "Basic",
                            "Greek",
                            "Calculus",
                            "Relations",
                            "Functions",
                            "Sets",
                            "Arrows",
                            "Matrix"
                        ).forEachIndexed { index, name ->

                            Tab(
                                selected = category == index,

                                onClick = {
                                    category = index
                                },

                                text = {
                                    Text(name)
                                })
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                toolbarScroll
                            ),

                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        when (category) {

                            /*
                             * BASIC
                             */
                            0 -> {

                                listOf(

                                    "a/b" to {
                                        wrap(
                                            "\\frac{", "}{}"
                                        )
                                    },

                                    "√" to {
                                        wrap(
                                            "\\sqrt{", "}"
                                        )
                                    },

                                    "√ⁿ" to {
                                        insert("\\sqrt[]{}")
                                    },

                                    "x²" to {
                                        wrap(
                                            "^{", "}"
                                        )
                                    },

                                    "xₙ" to {
                                        wrap(
                                            "_{", "}"
                                        )
                                    },

                                    "( )" to {
                                        wrap(
                                            "\\left(", "\\right)"
                                        )
                                    },

                                    "[ ]" to {
                                        wrap(
                                            "\\left[", "\\right]"
                                        )
                                    },

                                    "{ }" to {
                                        wrap(
                                            "\\left\\{", "\\right\\}"
                                        )
                                    },

                                    "|x|" to {
                                        wrap(
                                            "\\left|", "\\right|"
                                        )
                                    },

                                    "x̂" to {
                                        wrap(
                                            "\\hat{", "}"
                                        )
                                    },

                                    "x̄" to {
                                        wrap(
                                            "\\bar{", "}"
                                        )
                                    },

                                    "∞" to {
                                        insert("\\infty")
                                    },

                                    "±" to {
                                        insert("\\pm")
                                    },

                                    "×" to {
                                        insert("\\times")
                                    },

                                    "÷" to {
                                        insert("\\div")
                                    }

                                ).forEach { (label, action) ->

                                    TextButton(
                                        onClick = action
                                    ) {
                                        Text(
                                            text = label, fontSize = 17.sp
                                        )
                                    }
                                }
                            }

                            1 -> {

                                listOf(
                                    "α" to "\\alpha",
                                    "β" to "\\beta",
                                    "γ" to "\\gamma",
                                    "δ" to "\\delta",
                                    "ε" to "\\epsilon",
                                    "ζ" to "\\zeta",
                                    "η" to "\\eta",
                                    "θ" to "\\theta",
                                    "λ" to "\\lambda",
                                    "μ" to "\\mu",
                                    "ξ" to "\\xi",
                                    "π" to "\\pi",
                                    "ρ" to "\\rho",
                                    "σ" to "\\sigma",
                                    "φ" to "\\phi",
                                    "ψ" to "\\psi",
                                    "ω" to "\\omega",
                                    "Γ" to "\\Gamma",
                                    "Δ" to "\\Delta",
                                    "Θ" to "\\Theta",
                                    "Λ" to "\\Lambda",
                                    "Π" to "\\Pi",
                                    "Σ" to "\\Sigma",
                                    "Φ" to "\\Phi",
                                    "Ω" to "\\Omega"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(
                                            text = label, fontSize = 19.sp
                                        )
                                    }
                                }
                            }

                            /*
                             * CALCULUS
                             */
                            2 -> {

                                listOf(
                                    "∫" to "\\int ",
                                    "∬" to "\\iint ",
                                    "∭" to "\\iiint ",
                                    "∮" to "\\oint ",
                                    "Σ" to "\\sum ",
                                    "Π" to "\\prod ",
                                    "∂" to "\\partial ",
                                    "∇" to "\\nabla ",
                                    "lim" to "\\lim_{",
                                    "d/dx" to "\\frac{d}{dx}\\left("
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * RELATIONS
                             */
                            3 -> {

                                listOf(
                                    "=" to "=",
                                    "≠" to "\\neq",
                                    "≈" to "\\approx",
                                    "≡" to "\\equiv",
                                    "≤" to "\\leq",
                                    "≥" to "\\geq",
                                    "∼" to "\\sim",
                                    "∝" to "\\propto",
                                    "∈" to "\\in",
                                    "∉" to "\\notin",
                                    "⊂" to "\\subset",
                                    "⊆" to "\\subseteq",
                                    "⊃" to "\\supset",
                                    "⊇" to "\\supseteq"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * FUNCTIONS
                             */
                            4 -> {

                                listOf(
                                    "sin" to "\\sin ",
                                    "cos" to "\\cos ",
                                    "tan" to "\\tan ",
                                    "cot" to "\\cot ",
                                    "sec" to "\\sec ",
                                    "csc" to "\\csc ",
                                    "log" to "\\log ",
                                    "ln" to "\\ln ",
                                    "exp" to "\\exp ",
                                    "max" to "\\max ",
                                    "min" to "\\min ",
                                    "det" to "\\det "
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * SETS
                             */
                            5 -> {

                                listOf(
                                    "∅" to "\\emptyset",
                                    "∀" to "\\forall",
                                    "∃" to "\\exists",
                                    "¬" to "\\neg",
                                    "∧" to "\\land",
                                    "∨" to "\\lor",
                                    "∩" to "\\cap",
                                    "∪" to "\\cup",
                                    "⇒" to "\\Rightarrow",
                                    "⇔" to "\\Leftrightarrow",
                                    "⊥" to "\\perp"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * ARROWS
                             */
                            6 -> {

                                listOf(
                                    "→" to "\\rightarrow",
                                    "←" to "\\leftarrow",
                                    "↔" to "\\leftrightarrow",
                                    "⇒" to "\\Rightarrow",
                                    "⇐" to "\\Leftarrow",
                                    "⇔" to "\\Leftrightarrow",
                                    "↦" to "\\mapsto",
                                    "↑" to "\\uparrow",
                                    "↓" to "\\downarrow",
                                    "↗" to "\\nearrow",
                                    "↘" to "\\searrow",
                                    "↙" to "\\swarrow",
                                    "↖" to "\\nwarrow"
                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }

                            /*
                             * MATRIX
                             */
                            7 -> {

                                listOf(

                                    "matrix" to """
                                        \begin{matrix}
                                        & \\
                                        &
                                        \end{matrix}
                                    """.trimIndent(),

                                    "( )" to """
                                        \begin{pmatrix}
                                        & \\
                                        &
                                        \end{pmatrix}
                                    """.trimIndent(),

                                    "[ ]" to """
                                        \begin{bmatrix}
                                        & \\
                                        &
                                        \end{bmatrix}
                                    """.trimIndent(),

                                    "| |" to """
                                        \begin{vmatrix}
                                        & \\
                                        &
                                        \end{vmatrix}
                                    """.trimIndent(),

                                    "cases" to """
                                        \begin{cases}
                                        & \\
                                        &
                                        \end{cases}
                                    """.trimIndent()

                                ).forEach { (label, latex) ->

                                    TextButton(
                                        onClick = {
                                            insert(latex)
                                        }) {
                                        Text(label)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * Footer
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${text.length} characters",

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onPrimary,

                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(zoom * 100).toInt()}%",

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        sheetState.show()
    }
}