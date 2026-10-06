package app.sigma.calculator

/** How a key looks. */
enum class KeyStyle { Number, Function, Operator, Clear, Equals, Mode }

/**
 * One key on the keypad.
 * @param id what the key sends to CalculatorViewModel.press()
 * @param label the text on the key
 * @param description what a screen reader says, when the label alone isn't clear
 * @param span how many columns the key is wide
 */
data class KeySpec(
    val id: String,
    val label: String,
    val style: KeyStyle,
    val description: String? = null,
    val span: Int = 1,
)

/** The keypad, row by row. To add or move a key, edit this list. */
val KEYPAD: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("MODE", "DEG", KeyStyle.Mode, "Switch between degrees and radians"),
        KeySpec("sin(", "sin", KeyStyle.Function, "Sine"),
        KeySpec("cos(", "cos", KeyStyle.Function, "Cosine"),
        KeySpec("tan(", "tan", KeyStyle.Function, "Tangent"),
        KeySpec("π", "π", KeyStyle.Function, "Pi"),
    ),
    listOf(
        KeySpec("ln(", "ln", KeyStyle.Function, "Natural log"),
        KeySpec("log(", "log", KeyStyle.Function, "Log base 10"),
        KeySpec("√(", "√", KeyStyle.Function, "Square root"),
        KeySpec("SQ", "x²", KeyStyle.Function, "Square"),
        KeySpec("^", "xʸ", KeyStyle.Function, "Power"),
    ),
    listOf(
        KeySpec("AC", "AC", KeyStyle.Clear, "Clear all"),
        KeySpec("DEL", "⌫", KeyStyle.Clear, "Delete"),
        KeySpec("(", "(", KeyStyle.Function, "Open bracket"),
        KeySpec(")", ")", KeyStyle.Function, "Close bracket"),
        KeySpec("÷", "÷", KeyStyle.Operator, "Divide"),
    ),
    listOf(
        KeySpec("7", "7", KeyStyle.Number),
        KeySpec("8", "8", KeyStyle.Number),
        KeySpec("9", "9", KeyStyle.Number),
        KeySpec("!", "n!", KeyStyle.Function, "Factorial"),
        KeySpec("×", "×", KeyStyle.Operator, "Multiply"),
    ),
    listOf(
        KeySpec("4", "4", KeyStyle.Number),
        KeySpec("5", "5", KeyStyle.Number),
        KeySpec("6", "6", KeyStyle.Number),
        KeySpec("%", "%", KeyStyle.Function, "Percent"),
        KeySpec("−", "−", KeyStyle.Operator, "Subtract"),
    ),
    listOf(
        KeySpec("1", "1", KeyStyle.Number),
        KeySpec("2", "2", KeyStyle.Number),
        KeySpec("3", "3", KeyStyle.Number),
        KeySpec("e", "e", KeyStyle.Function, "Euler's number"),
        KeySpec("+", "+", KeyStyle.Operator, "Add"),
    ),
    listOf(
        KeySpec("0", "0", KeyStyle.Number, span = 2),
        KeySpec(".", ".", KeyStyle.Number, "Decimal point"),
        KeySpec("Ans", "Ans", KeyStyle.Function, "Previous answer"),
        KeySpec("=", "=", KeyStyle.Equals, "Equals"),
    ),
)

/** Keys on a physical keyboard (Bluetooth keyboard or Chromebook) and the key each one presses. */
val HARDWARE_KEYS: Map<Char, String> = mapOf(
    '+' to "+", '-' to "−", '*' to "×", 'x' to "×", '/' to "÷", '^' to "^",
    '(' to "(", ')' to ")", '!' to "!", '%' to "%", '.' to ".", ',' to ".", '=' to "=",
    's' to "sin(", 'c' to "cos(", 't' to "tan(", 'l' to "ln(", 'g' to "log(", 'r' to "√(",
    'p' to "π", 'e' to "e", 'a' to "Ans", 'd' to "MODE",
)
