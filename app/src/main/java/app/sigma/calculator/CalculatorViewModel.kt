package app.sigma.calculator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import app.sigma.calculator.engine.AngleMode
import app.sigma.calculator.engine.CalcException
import app.sigma.calculator.engine.Evaluator
import app.sigma.calculator.engine.formatNumber

/**
 * The calculator's brain: holds what's on screen and decides what each key does.
 *
 * The screen (CalculatorScreen.kt) only draws this state and calls press().
 * Because the values below use mutableStateOf, Compose redraws the screen
 * automatically whenever one of them changes.
 */
class CalculatorViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app)

    /** What has been typed, e.g. "2×(3+4)^2". After "=", the calculation that was run. */
    var expression by mutableStateOf("")
        private set

    /** The formatted answer, shown after "=". */
    var resultText by mutableStateOf("")
        private set

    /** True right after "=", until the next key is pressed. */
    var evaluated by mutableStateOf(false)
        private set

    /** An error message such as "Can't divide by zero", or null. */
    var error by mutableStateOf<String?>(null)
        private set

    var angleMode by mutableStateOf(store.angleMode)
        private set

    var themeMode by mutableStateOf(store.themeMode)
        private set

    var showHistory by mutableStateOf(false)
        private set

    val history = mutableStateListOf<HistoryEntry>().apply { addAll(store.loadHistory()) }

    private var ans = store.ans

    init {
        // On the very first launch, show an example so the screen isn't blank.
        if (store.consumeFirstRun()) expression = "2×(3+4)^2"
    }

    /** The live answer shown while typing, e.g. "= 98". Empty when there's nothing useful to show. */
    val preview: String
        get() {
            if (evaluated || error != null || expression.isEmpty()) return ""
            return try {
                val formatted = formatNumber(Evaluator(angleMode, ans).evaluate(expression))
                if (formatted == expression) "" else "= $formatted"
            } catch (e: Exception) {
                "" // not a complete calculation yet
            }
        }

    /** Handles one key. [id] is the key's id from Keys.kt, e.g. "7", "sin(", "=". */
    fun press(id: String) {
        when (id) {
            "AC" -> {
                expression = ""
                evaluated = false
                error = null
            }
            "DEL" -> {
                if (evaluated) {
                    expression = ""
                    evaluated = false
                } else {
                    // Delete "sin(" or "Ans" as one piece rather than letter by letter.
                    val piece = MULTI_CHARACTER.firstOrNull { expression.endsWith(it) }
                    expression = expression.dropLast(piece?.length ?: 1)
                }
                error = null
            }
            "MODE" -> {
                angleMode = if (angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
                store.angleMode = angleMode
            }
            "=" -> calculate()
            "+", "×", "÷", "^" -> {
                continueFromAnswer()
                if (expression.isEmpty() && id != "+") expression = "Ans"
                // Pressing × after + swaps the operator instead of stacking them.
                if (expression.takeLast(1) in BINARY_OPERATORS) expression = expression.dropLast(1)
                expression += id
            }
            "−" -> {
                continueFromAnswer()
                if (expression.endsWith("−") || expression.endsWith("+")) expression = expression.dropLast(1)
                expression += "−"
            }
            "!", "%", "SQ" -> {
                continueFromAnswer()
                if (expression.isEmpty()) expression = "Ans"
                expression += if (id == "SQ") "^2" else id
            }
            else -> {
                // Digits, π, e, Ans, brackets and functions.
                startFresh()
                expression += id
            }
        }
    }

    fun toggleHistory() {
        showHistory = !showHistory
    }

    /** Puts a past calculation back on screen so it can be edited or run again. */
    fun useHistoryEntry(entry: HistoryEntry) {
        expression = entry.expression
        evaluated = false
        error = null
        showHistory = false
    }

    fun clearHistory() {
        history.clear()
        store.saveHistory(history)
    }

    /** Switches between light and dark. [systemIsDark] is the phone's current setting. */
    fun toggleTheme(systemIsDark: Boolean) {
        val currentlyDark = when (themeMode) {
            ThemeMode.SYSTEM -> systemIsDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }
        themeMode = if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
        store.themeMode = themeMode
    }

    private fun calculate() {
        if (evaluated || expression.isBlank()) return
        try {
            val value = Evaluator(angleMode, ans).evaluate(expression)
            val openBrackets = expression.count { it == '(' } - expression.count { it == ')' }
            val completed = expression + ")".repeat(maxOf(0, openBrackets))

            ans = value
            store.ans = value
            resultText = formatNumber(value)
            expression = completed
            evaluated = true
            error = null

            history.add(0, HistoryEntry(completed, resultText, angleMode))
            while (history.size > MAX_HISTORY) history.removeAt(history.lastIndex)
            store.saveHistory(history)
        } catch (e: CalcException) {
            error = e.message
        }
    }

    /** After "=", typing an operator continues from the answer: "Ans+…". */
    private fun continueFromAnswer() {
        if (evaluated) {
            expression = "Ans"
            evaluated = false
        }
        error = null
    }

    /** After "=", typing a number starts a new calculation. */
    private fun startFresh() {
        if (evaluated) {
            expression = ""
            evaluated = false
        }
        error = null
    }

    private companion object {
        const val MAX_HISTORY = 50
        val BINARY_OPERATORS = setOf("+", "−", "×", "÷", "^")
        val MULTI_CHARACTER = listOf("sin(", "cos(", "tan(", "ln(", "log(", "√(", "Ans")
    }
}
