package app.sigma.calculator.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.sigma.calculator.CalculatorViewModel
import app.sigma.calculator.HARDWARE_KEYS
import app.sigma.calculator.HistoryEntry
import app.sigma.calculator.KEYPAD
import app.sigma.calculator.KeySpec
import app.sigma.calculator.KeyStyle
import app.sigma.calculator.engine.AngleMode
import app.sigma.calculator.ui.theme.Figtree
import app.sigma.calculator.ui.theme.LocalSigmaColors
import app.sigma.calculator.ui.theme.PlexMono

private val Gap = 8.dp

/**
 * The whole screen: top bar, display, and either the keypad or the history list.
 * Each piece below is a @Composable function: a small reusable part of the screen.
 */
@Composable
fun CalculatorScreen(vm: CalculatorViewModel, dark: Boolean, onToggleTheme: () -> Unit) {
    val colors = LocalSigmaColors.current
    val view = LocalView.current
    val focus = remember { FocusRequester() }

    // A light buzz on every tap, like the phone's own keyboard.
    val pressKey: (String) -> Unit = { id ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        vm.press(id)
    }

    // Take keyboard focus so a physical keyboard works straight away.
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding() // keeps clear of the status bar, notch and navigation bar
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .onPreviewKeyEvent { event -> handleHardwareKey(event, vm) }
            .focusRequester(focus)
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TopBar(
            showingHistory = vm.showHistory,
            dark = dark,
            onToggleHistory = vm::toggleHistory,
            onToggleTheme = onToggleTheme,
        )
        Display(vm, Modifier.fillMaxWidth().weight(1f))
        if (vm.showHistory) {
            HistoryPanel(
                entries = vm.history,
                onUse = vm::useHistoryEntry,
                onClear = vm::clearHistory,
                modifier = Modifier.fillMaxWidth().weight(2.3f),
            )
        } else {
            Keypad(vm.angleMode, pressKey, Modifier.fillMaxWidth().weight(2.3f))
        }
    }
}

// ---------------- Top bar ----------------

@Composable
private fun TopBar(
    showingHistory: Boolean,
    dark: Boolean,
    onToggleHistory: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    val colors = LocalSigmaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("σ", color = colors.onAccent, fontFamily = PlexMono, fontWeight = FontWeight.Medium, fontSize = 18.sp)
        }
        Text("Sigma", color = colors.text, fontFamily = Figtree, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Spacer(Modifier.weight(1f))
        Chip(if (showingHistory) "Keypad" else "History", onToggleHistory)
        Chip(if (dark) "Light" else "Dark", onToggleTheme)
    }
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    val colors = LocalSigmaColors.current
    val shape = RoundedCornerShape(50)
    Text(
        text = label.uppercase(),
        color = colors.text,
        fontFamily = Figtree,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.7.sp,
        modifier = Modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

// ---------------- Display ----------------

@Composable
private fun Display(vm: CalculatorViewModel, modifier: Modifier) {
    val colors = LocalSigmaColors.current
    val shape = RoundedCornerShape(20.dp)
    val showingError = vm.error != null && !vm.evaluated

    val subLine = when {
        vm.evaluated -> vm.expression + " ="
        showingError -> vm.error.orEmpty()
        else -> vm.preview
    }
    val mainLine = if (vm.evaluated) vm.resultText else vm.expression
    val mainSize = when {
        mainLine.length > 20 -> 28.sp
        mainLine.length > 13 -> 36.sp
        else -> 46.sp
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .padding(16.dp),
    ) {
        ModeTag(vm.angleMode)
        Spacer(Modifier.weight(1f))
        ScrollingLine(
            text = subLine,
            style = TextStyle(
                fontFamily = if (showingError) Figtree else PlexMono,
                fontWeight = if (showingError) FontWeight.Medium else FontWeight.Normal,
                fontSize = 15.sp,
                color = if (showingError) colors.danger else colors.muted,
            ),
        )
        ScrollingLine(
            text = mainLine.ifEmpty { "0" },
            style = TextStyle(
                fontFamily = PlexMono,
                fontSize = mainSize,
                color = if (mainLine.isEmpty()) colors.muted else colors.text,
            ),
        )
    }
}

@Composable
private fun ModeTag(mode: AngleMode) {
    val colors = LocalSigmaColors.current
    Text(
        text = mode.name,
        color = colors.accent,
        fontFamily = Figtree,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.9.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.accentSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** One right-aligned line. If it gets too long, it scrolls sideways and keeps the end in view. */
@Composable
private fun ScrollingLine(text: String, style: TextStyle) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState(), reverseScrolling = true)) {
            Text(
                text = text,
                style = style.copy(textAlign = TextAlign.End),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.widthIn(min = width),
            )
        }
    }
}

// ---------------- Keypad ----------------

@Composable
private fun Keypad(angleMode: AngleMode, onPress: (String) -> Unit, modifier: Modifier) {
    BoxWithConstraints(modifier) {
        // Width of one key, so the wide "0" key lines up exactly with the keys above it.
        val keyWidth: Dp = (maxWidth - Gap * 4) / 5
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Gap)) {
            KEYPAD.forEachIndexed { index, row ->
                // The two rows of scientific functions are a little shorter.
                val rowWeight = if (index < 2) 0.8f else 1f
                Row(
                    modifier = Modifier.fillMaxWidth().weight(rowWeight),
                    horizontalArrangement = Arrangement.spacedBy(Gap),
                ) {
                    row.forEach { key ->
                        val keyModifier = if (key.span > 1) {
                            Modifier.width(keyWidth * key.span + Gap * (key.span - 1)).fillMaxHeight()
                        } else {
                            Modifier.weight(1f).fillMaxHeight()
                        }
                        CalcKey(
                            key = key,
                            label = if (key.style == KeyStyle.Mode) angleMode.name else key.label,
                            onPress = onPress,
                            modifier = keyModifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(key: KeySpec, label: String, onPress: (String) -> Unit, modifier: Modifier) {
    val colors = LocalSigmaColors.current
    val background = when (key.style) {
        KeyStyle.Number, KeyStyle.Operator -> colors.key
        KeyStyle.Function, KeyStyle.Mode, KeyStyle.Clear -> colors.keyFunction
        KeyStyle.Equals -> colors.accent
    }
    val textColor = when (key.style) {
        KeyStyle.Number, KeyStyle.Function -> colors.text
        KeyStyle.Operator, KeyStyle.Mode -> colors.accent
        KeyStyle.Clear -> colors.danger
        KeyStyle.Equals -> colors.onAccent
    }
    val font: FontFamily = if (key.style == KeyStyle.Number) PlexMono else Figtree
    val size = when (key.style) {
        KeyStyle.Number -> 24.sp
        KeyStyle.Operator, KeyStyle.Equals -> 27.sp
        KeyStyle.Mode -> 13.sp
        KeyStyle.Function, KeyStyle.Clear -> 18.sp
    }
    val weight = when (key.style) {
        KeyStyle.Mode -> FontWeight.Bold
        KeyStyle.Operator, KeyStyle.Clear, KeyStyle.Equals -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(role = Role.Button) { onPress(key.id) }
            .then(
                if (key.description != null) Modifier.semantics { contentDescription = key.description }
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontFamily = font,
            fontWeight = weight,
            fontSize = size,
            letterSpacing = if (key.style == KeyStyle.Mode) 0.8.sp else 0.sp,
        )
    }
}

// ---------------- History ----------------

@Composable
private fun HistoryPanel(
    entries: List<HistoryEntry>,
    onUse: (HistoryEntry) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalSigmaColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "HISTORY",
                color = colors.muted,
                fontFamily = Figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                Text(
                    "Clear",
                    color = colors.accent,
                    fontFamily = Figtree,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = onClear)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
        if (entries.isEmpty()) {
            Text(
                "Your calculations will appear here. Tap one to use it again.",
                color = colors.muted,
                fontFamily = Figtree,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(entries) { entry -> HistoryRow(entry) { onUse(entry) } }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onClick: () -> Unit) {
    val colors = LocalSigmaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.key)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        val mode = if (entry.angleMode == AngleMode.RAD) " (RAD)" else ""
        Text(
            entry.expression + " =" + mode,
            color = colors.muted,
            fontFamily = PlexMono,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
        )
        Text(
            entry.result,
            color = colors.text,
            fontFamily = PlexMono,
            fontSize = 20.sp,
            textAlign = TextAlign.End,
        )
    }
}

// ---------------- Physical keyboard ----------------

private fun handleHardwareKey(event: KeyEvent, vm: CalculatorViewModel): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    if (event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) return false
    val id = when (event.key) {
        Key.Enter, Key.NumPadEnter -> "="
        Key.Backspace -> "DEL"
        Key.Escape, Key.Delete -> "AC"
        else -> {
            val ch = event.utf16CodePoint.toChar()
            if (ch in '0'..'9') ch.toString() else HARDWARE_KEYS[ch]
        }
    } ?: return false
    vm.press(id)
    return true
}
