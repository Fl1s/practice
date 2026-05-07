package ci.nsu.mobile.calculations

import ci.nsu.mobile.calculations.domain.DepositCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DepositCalculatorTest {

    private lateinit var calculator: DepositCalculator

    @Before
    fun setUp() {
        calculator = DepositCalculator()
    }

    @Test
    fun calculateWithoutTopUpReturnsCorrectFinalAmount() {
        val result = calculator.calculate(
            startAmount  = 10_000.0,
            months       = 12,
            rate         = 5.0,
            monthlyTopUp = 0.0
        )
        assertEquals(17_958.56, result.finalAmount, 1.0)
    }

    @Test
    fun calculateWithMonthlyTopUpFinalAmountExceedsTotalInvested() {
        val result = calculator.calculate(
            startAmount  = 10_000.0,
            months       = 12,
            rate         = 1.0,
            monthlyTopUp = 1_000.0
        )
        val totalInvested = 10_000.0 + 1_000.0 * 12
        assertTrue(result.finalAmount > totalInvested)
    }

    @Test
    fun profitEqualsFinalAmountMinusAllInvestedMoney() {
        val result = calculator.calculate(
            startAmount  = 10_000.0,
            months       = 6,
            rate         = 10.0,
            monthlyTopUp = 500.0
        )
        val expectedProfit = result.finalAmount - (10_000.0 + 500.0 * 6)
        assertEquals(expectedProfit, result.profit, 0.001)
    }

    @Test
    fun calculateWithZeroTopUpMatchesCalculateWithoutTopUp() {
        val r1 = calculator.calculate(10_000.0, 12, 5.0, 0.0)
        val r2 = calculator.calculate(10_000.0, 12, 5.0)
        assertEquals(r1.finalAmount, r2.finalAmount, 0.001)
    }

    @Test
    fun oneMonthDepositCalculatedCorrectly() {
        val result = calculator.calculate(1_000.0, 1, 10.0, 0.0)
        assertEquals(1_100.0, result.finalAmount, 0.001)
        assertEquals(100.0,   result.profit,      0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeStartAmountThrowsException() {
        calculator.calculate(-1.0, 12, 5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroMonthsThrowsException() {
        calculator.calculate(10_000.0, 0, 5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeMonthsThrowsException() {
        calculator.calculate(10_000.0, -1, 5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroRateThrowsException() {
        calculator.calculate(10_000.0, 12, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeRateThrowsException() {
        calculator.calculate(10_000.0, 12, -5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeMonthlyTopUpThrowsException() {
        calculator.calculate(10_000.0, 12, 5.0, -100.0)
    }

    @Test
    fun zeroStartAmountWithTopUpCalculatesWithoutException() {
        val result = calculator.calculate(0.0, 3, 10.0, 1_000.0)
        assertTrue(result.finalAmount > 0)
    }

    @Test
    fun resolveRatesForZeroMonthsReturnsEmptyList() {
        assertTrue(calculator.resolveRates(0).isEmpty())
    }

    @Test
    fun resolveRatesForShortTermReturnsFifteenPercent() {
        assertEquals(listOf(15.0), calculator.resolveRates(1))
        assertEquals(listOf(15.0), calculator.resolveRates(5))
    }

    @Test
    fun resolveRatesForMidTermReturnsTenPercent() {
        assertEquals(listOf(10.0), calculator.resolveRates(6))
        assertEquals(listOf(10.0), calculator.resolveRates(11))
    }

    @Test
    fun resolveRatesForLongTermReturnsFivePercent() {
        assertEquals(listOf(5.0), calculator.resolveRates(12))
        assertEquals(listOf(5.0), calculator.resolveRates(24))
    }
}