package app.sigma.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.util.Locale
import kotlin.math.abs

/**
 * Turns a result into display text: up to 12 significant digits, thousands separators,
 * a proper minus sign, and scientific notation (like 1.5e20) for very large or tiny numbers.
 */
fun formatNumber(value: Double): String {
    val v = if (value == 0.0) 0.0 else value // turns −0 into 0
    val size = abs(v)
    val text = if (size != 0.0 && (size >= 1e15 || size < 1e-9)) {
        scientific(v)
    } else {
        val rounded = BigDecimal(v).round(MathContext(12)).stripTrailingZeros().toPlainString()
        groupThousands(rounded)
    }
    return text.replaceFirst("-", "−")
}

private fun scientific(v: Double): String =
    String.format(Locale.US, "%.9e", v) // e.g. 1.500000000e+20
        .replace(Regex("\\.?0+e"), "e")  // 1.5e+20
        .replace("e+", "e")               // 1.5e20

private fun groupThousands(number: String): String {
    val negative = number.startsWith("-")
    val digits = number.removePrefix("-")
    val whole = digits.substringBefore('.')
    val fraction = digits.substringAfter('.', "")
    val grouped = whole.reversed().chunked(3).joinToString(",").reversed()
    return (if (negative) "-" else "") + grouped + (if (fraction.isNotEmpty()) ".$fraction" else "")
}
