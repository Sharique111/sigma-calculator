package app.sigma.calculator.engine

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks the maths. These run on GitHub before every APK build;
 * if one fails, no APK is published.
 */
class EvaluatorTest {

    private fun eval(text: String, mode: AngleMode = AngleMode.DEG, ans: Double = 0.0) =
        Evaluator(mode, ans).evaluate(text)

    @Test fun followsOrderOfOperations() = assertEquals(98.0, eval("2×(3+4)^2"), 1e-9)

    @Test fun hiddenMultiplication() {
        assertEquals(2 * Math.PI, eval("2π"), 1e-12)
        assertEquals(12.0, eval("3(4)"), 1e-12)
    }

    @Test fun minusAppliesAfterPower() = assertEquals(-4.0, eval("−2^2"), 1e-12)

    @Test fun powersGroupFromTheRight() = assertEquals(512.0, eval("2^3^2"), 1e-9)

    @Test fun trigInDegrees() {
        assertEquals(0.5, eval("sin(30)"), 1e-12)
        assertEquals(0.0, eval("cos(90)"), 0.0)
    }

    @Test fun trigInRadians() = assertEquals(1.0, eval("sin(π÷2)", AngleMode.RAD), 1e-12)

    @Test fun logarithms() {
        assertEquals(2.0, eval("log(100)"), 1e-12)
        assertEquals(1.0, eval("ln(e)"), 1e-12)
    }

    @Test fun factorialAndPercent() {
        assertEquals(120.0, eval("5!"), 0.0)
        assertEquals(0.5, eval("50%"), 1e-12)
    }

    @Test fun usesPreviousAnswer() = assertEquals(10.0, eval("Ans×2", ans = 5.0), 0.0)

    @Test fun closesOpenBracketsAtTheEnd() = assertEquals(3.0, eval("√(9"), 1e-12)

    @Test(expected = CalcException::class) fun divideByZeroIsAnError() { eval("1÷0") }

    @Test(expected = CalcException::class) fun tan90IsAnError() { eval("tan(90)") }

    @Test(expected = CalcException::class) fun factorialNeedsWholeNumber() { eval("2.5!") }

    @Test(expected = CalcException::class) fun doubleDecimalPointIsAnError() { eval("1.2.3") }

    @Test fun formatsNumbersForDisplay() {
        assertEquals("0.3", formatNumber(0.1 + 0.2))
        assertEquals("1,234,567", formatNumber(1234567.0))
        assertEquals("−5", formatNumber(-5.0))
        assertEquals("0", formatNumber(-0.0))
        assertEquals("1e15", formatNumber(1e15))
        assertEquals("1.5e-10", formatNumber(1.5e-10))
    }
}
