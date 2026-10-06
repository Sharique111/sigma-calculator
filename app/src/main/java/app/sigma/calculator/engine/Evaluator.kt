package app.sigma.calculator.engine

import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** A calculation the user typed can't be worked out. The message is shown on screen. */
class CalcException(message: String) : Exception(message)

/** Whether sin, cos and tan read their input as degrees or radians. */
enum class AngleMode { DEG, RAD }

/**
 * Works out text like "2×(3+4)^2" and returns the number.
 *
 * This file is plain Kotlin with no Android code, so it can be tested on any computer
 * (see EvaluatorTest.kt). It works in two steps:
 *  1. tokenize() splits the text into pieces: numbers, operators and function names.
 *  2. The parser functions below read those pieces following the rules of maths:
 *     brackets first, then powers, then × and ÷, then + and −.
 *
 * @param angleMode degrees or radians, for sin/cos/tan
 * @param ans the previous answer, used wherever the text says "Ans"
 */
class Evaluator(
    private val angleMode: AngleMode = AngleMode.DEG,
    private val ans: Double = 0.0,
) {
    /** One piece of the calculation. */
    private sealed interface Token {
        data class Num(val value: Double) : Token
        data class Op(val symbol: Char) : Token
        data class Fn(val name: String) : Token
        data object Ans : Token
    }

    private var tokens: List<Token> = emptyList()
    private var pos = 0 // which token the parser is looking at

    fun evaluate(input: String): Double {
        tokens = tokenize(input)
        pos = 0
        if (tokens.isEmpty()) throw CalcException("Enter a calculation")
        val value = expression()
        if (pos < tokens.size) throw CalcException("Unexpected “${describe(tokens[pos])}”")
        if (value.isNaN()) throw CalcException("That result isn't a real number")
        if (value.isInfinite()) throw CalcException("Result is too large")
        return value
    }

    // ---------- Step 1: split the text into tokens ----------

    private fun tokenize(text: String): List<Token> {
        val out = mutableListOf<Token>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == ' ' -> i++
                c.isDigit() || c == '.' -> {
                    var j = i
                    while (j < text.length && (text[j].isDigit() || text[j] == '.')) j++
                    val number = text.substring(i, j)
                    if (number == "." || number.count { it == '.' } > 1) {
                        throw CalcException("Check the decimal point")
                    }
                    out += Token.Num(number.toDouble())
                    i = j
                }
                c == 'π' -> { out += Token.Num(PI); i++ }
                c == '√' -> { out += Token.Fn("sqrt"); i++ }
                c in OPERATORS -> { out += Token.Op(OPERATORS.getValue(c)); i++ }
                else -> {
                    val word = WORDS.firstOrNull { text.startsWith(it, i) }
                        ?: throw CalcException("Unknown symbol “$c”")
                    out += when (word) {
                        "Ans" -> Token.Ans
                        "e" -> Token.Num(E)
                        else -> Token.Fn(word)
                    }
                    i += word.length
                }
            }
        }
        return out
    }

    // ---------- Step 2: read the tokens, lowest priority first ----------

    private fun peekIs(op: Char): Boolean = (tokens.getOrNull(pos) as? Token.Op)?.symbol == op

    /** True when the next token can start a number, so "2π" or "3(4)" means multiply. */
    private fun startsOperand(): Boolean = when (val t = tokens.getOrNull(pos)) {
        is Token.Num, is Token.Fn, Token.Ans -> true
        is Token.Op -> t.symbol == '('
        null -> false
    }

    /** Addition and subtraction. */
    private fun expression(): Double {
        var value = term()
        while (true) {
            value = when {
                peekIs('+') -> { pos++; value + term() }
                peekIs('-') -> { pos++; value - term() }
                else -> return value
            }
        }
    }

    /** Multiplication and division, including hidden multiplication like 2π. */
    private fun term(): Double {
        var value = unary()
        while (true) {
            value = when {
                peekIs('*') -> { pos++; value * unary() }
                peekIs('/') -> {
                    pos++
                    val divisor = unary()
                    if (divisor == 0.0) throw CalcException("Can't divide by zero")
                    value / divisor
                }
                startsOperand() -> value * unary()
                else -> return value
            }
        }
    }

    /** A leading minus or plus sign. */
    private fun unary(): Double = when {
        peekIs('-') -> { pos++; -unary() }
        peekIs('+') -> { pos++; unary() }
        else -> power()
    }

    /** Powers. 2^3^2 is read as 2^(3^2), as in standard maths. */
    private fun power(): Double {
        val base = postfix()
        if (peekIs('^')) {
            pos++
            return base.pow(unary())
        }
        return base
    }

    /** Factorial (5!) and percent (50%). */
    private fun postfix(): Double {
        var value = primary()
        while (true) {
            value = when {
                peekIs('!') -> { pos++; factorial(value) }
                peekIs('%') -> { pos++; value / 100 }
                else -> return value
            }
        }
    }

    /** A number, Ans, a function like sin(…), or a bracketed part. */
    private fun primary(): Double {
        val t = tokens.getOrNull(pos) ?: throw CalcException("Finish the calculation")
        return when {
            t is Token.Num -> { pos++; t.value }
            t == Token.Ans -> { pos++; ans }
            t is Token.Fn -> {
                pos++
                val argument = if (peekIs('(')) {
                    pos++
                    val inside = expression()
                    closeBracket()
                    inside
                } else {
                    unary()
                }
                applyFunction(t.name, argument)
            }
            peekIs('(') -> {
                pos++
                val inside = expression()
                closeBracket()
                inside
            }
            else -> throw CalcException("Unexpected “${describe(t)}”")
        }
    }

    /** Brackets left open at the very end are closed automatically. */
    private fun closeBracket() {
        when {
            peekIs(')') -> pos++
            pos < tokens.size -> throw CalcException("Missing a closing bracket")
        }
    }

    private fun applyFunction(name: String, a: Double): Double {
        val radians = if (angleMode == AngleMode.DEG) Math.toRadians(a) else a
        val result = when (name) {
            "sin" -> sin(radians)
            "cos" -> cos(radians)
            "tan" -> {
                val undefined = angleMode == AngleMode.DEG &&
                    abs(((a % 180) + 180) % 180 - 90) < 1e-12
                if (undefined) throw CalcException("tan is undefined at that angle")
                tan(radians)
            }
            "ln" -> {
                if (a <= 0) throw CalcException("ln needs a number above 0")
                ln(a)
            }
            "log" -> {
                if (a <= 0) throw CalcException("log needs a number above 0")
                log10(a)
            }
            "sqrt" -> {
                if (a < 0) throw CalcException("Square root needs a number 0 or more")
                sqrt(a)
            }
            else -> throw CalcException("Unknown function $name")
        }
        // sin(180°) comes out as 0.0000000000000001 in floating point; show it as 0.
        val isTrig = name == "sin" || name == "cos" || name == "tan"
        return if (isTrig && abs(result) < 1e-12) 0.0 else result
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw CalcException("Factorial needs a whole number, 0 or more")
        if (n > 170) throw CalcException("That number is too large")
        var result = 1.0
        for (i in 2..n.toInt()) result *= i
        return result
    }

    private fun describe(t: Token): String = when (t) {
        is Token.Num -> t.value.toString()
        is Token.Op -> when (t.symbol) {
            '*' -> "×"
            '/' -> "÷"
            '-' -> "−"
            else -> t.symbol.toString()
        }
        is Token.Fn -> t.name
        Token.Ans -> "Ans"
    }

    private companion object {
        /** Characters on screen and the operator each one means. */
        val OPERATORS = mapOf(
            '+' to '+', '-' to '-', '−' to '-',
            '*' to '*', '×' to '*', '/' to '/', '÷' to '/',
            '^' to '^', '!' to '!', '%' to '%', '(' to '(', ')' to ')',
        )

        /** Named things, checked in this order ("e" last so it doesn't swallow other words). */
        val WORDS = listOf("Ans", "sin", "cos", "tan", "log", "ln", "e")
    }
}
