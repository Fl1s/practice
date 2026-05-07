package ci.nsu.mobile.calculations.domain

import ci.nsu.mobile.domain.model.DepositCalculation

class DepositCalculator {

    fun calculate(
        startAmount: Double,
        months: Int,
        rate: Double,
        monthlyTopUp: Double = 0.0,
        userId: Long = 0L,
        date: Long = System.currentTimeMillis()
    ): DepositCalculation {
        require(startAmount >= 0) { "Стартовая сумма не может быть отрицательной" }
        require(months > 0) { "Срок должен быть больше 0" }
        require(rate > 0) { "Ставка должна быть больше 0" }
        require(monthlyTopUp >= 0) { "Пополнение не может быть отрицательным" }

        var total = startAmount
        repeat(months) { total = (total + monthlyTopUp) * (1 + rate / 100.0) }

        return DepositCalculation(
            userId = userId,
            startAmount = startAmount,
            months = months,
            rate = rate,
            monthlyTopUp = monthlyTopUp,
            finalAmount = total,
            profit = total - (startAmount + monthlyTopUp * months),
            date = date
        )
    }

    fun resolveRates(months: Int): List<Double> = when {
        months < 1 -> emptyList()
        months < 6 -> listOf(15.0)
        months < 12 -> listOf(10.0)
        else -> listOf(5.0)
    }
}