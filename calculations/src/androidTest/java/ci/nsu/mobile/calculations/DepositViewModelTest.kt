package ci.nsu.mobile.calculations

import app.cash.turbine.test
import ci.nsu.mobile.calculations.data.dao.DepositDao
import ci.nsu.mobile.calculations.ui.DepositViewModel
import ci.nsu.mobile.domain.model.DepositCalculation
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DepositViewModelTest {

    private lateinit var dao: DepositDao
    private lateinit var viewModel: DepositViewModel

    private val userId = 1L
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dao = mockk(relaxed = true)
        every { dao.getByUser(userId) } returns flowOf(emptyList())
        viewModel = DepositViewModel(dao, userId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasEmptyFieldsAndStepZero() {
        assertEquals("", viewModel.startAmount)
        assertEquals("", viewModel.months)
        assertEquals(0.0, viewModel.rate, 0.001)
        assertEquals("", viewModel.monthlyTopUp)
        assertEquals(0, viewModel.currentStep)
        assertNull(viewModel.result)
        assertNull(viewModel.savedMessage)
    }

    @Test
    fun historyStartsEmptyWhenDaoReturnsEmpty() = runTest {
        viewModel.history.test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun goToStepUpdatesCurrentStep() {
        viewModel.goToStep(1)
        assertEquals(1, viewModel.currentStep)

        viewModel.goToStep(2)
        assertEquals(2, viewModel.currentStep)
    }

    @Test
    fun calculateDepositWithValidDataSetsResultAndMovesToStepTwo() {
        viewModel.startAmount = "10000"
        viewModel.months = "12"
        viewModel.rate = 5.0
        viewModel.monthlyTopUp = "0"

        viewModel.calculateDeposit()

        assertNotNull(viewModel.result)
        assertEquals(2, viewModel.currentStep)
        assertTrue(viewModel.result!!.finalAmount > 10_000.0)
    }

    @Test
    fun calculateDepositWithBlankStartAmountDoesNothing() {
        viewModel.startAmount = ""
        viewModel.months = "12"
        viewModel.rate = 5.0

        viewModel.calculateDeposit()

        assertNull(viewModel.result)
        assertEquals(0, viewModel.currentStep)
    }

    @Test
    fun calculateDepositWithBlankMonthsDoesNothing() {
        viewModel.startAmount = "10000"
        viewModel.months = ""
        viewModel.rate = 5.0

        viewModel.calculateDeposit()

        assertNull(viewModel.result)
    }

    @Test
    fun calculateDepositWithNonNumericStartAmountDoesNothing() {
        viewModel.startAmount = "abc"
        viewModel.months = "12"
        viewModel.rate = 5.0

        viewModel.calculateDeposit()

        assertNull(viewModel.result)
    }

    @Test
    fun calculateDepositSetsCorrectUserIdInResult() {
        viewModel.startAmount = "5000"
        viewModel.months = "6"
        viewModel.rate = 10.0

        viewModel.calculateDeposit()

        assertEquals(userId, viewModel.result!!.userId)
    }

    @Test
    fun saveDepositCallsDaoInsertAndSetsSavedMessage() = runTest {
        viewModel.startAmount = "10000"
        viewModel.months = "12"
        viewModel.rate = 5.0
        viewModel.monthlyTopUp = "500"
        viewModel.calculateDeposit()
        coEvery { dao.insert(any()) } just Runs

        viewModel.saveDeposit()
        advanceUntilIdle()

        coVerify(exactly = 1) { dao.insert(any()) }
        assertEquals("Расчёт сохранён", viewModel.savedMessage)
    }

    @Test
    fun saveDepositWithoutResultDoesNotCallDao() = runTest {
        viewModel.saveDeposit()
        advanceUntilIdle()

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun deleteDepositCallsDaoDeleteById() = runTest {
        val calc = DepositCalculation(
            id = 7L,
            userId = userId,
            startAmount = 1000.0,
            months = 6,
            rate = 5.0,
            monthlyTopUp = null,
            finalAmount = 1200.0,
            profit = 200.0,
            date = 0L
        )
        coEvery { dao.deleteById(any()) } just Runs

        viewModel.deleteDeposit(calc)
        advanceUntilIdle()

        coVerify(exactly = 1) { dao.deleteById(7L) }
    }

    @Test
    fun resetCalculationClearsAllState() {
        viewModel.startAmount = "5000"
        viewModel.months = "6"
        viewModel.rate = 10.0
        viewModel.monthlyTopUp = "200"
        viewModel.currentStep = 2
        viewModel.savedMessage = "saved"

        viewModel.resetCalculation()

        assertEquals("", viewModel.startAmount)
        assertEquals("", viewModel.months)
        assertEquals(0.0, viewModel.rate, 0.001)
        assertEquals("", viewModel.monthlyTopUp)
        assertEquals(0, viewModel.currentStep)
        assertNull(viewModel.result)
        assertNull(viewModel.savedMessage)
    }

    @Test
    fun resolveRateWithBlankMonthsReturnsEmpty() {
        viewModel.months = ""
        assertTrue(viewModel.resolveRate().isEmpty())
    }

    @Test
    fun resolveRateForShortTermReturnsFifteenPercent() {
        viewModel.months = "3"
        assertEquals(listOf(15.0), viewModel.resolveRate())
    }

    @Test
    fun resolveRateForMidTermReturnsTenPercent() {
        viewModel.months = "9"
        assertEquals(listOf(10.0), viewModel.resolveRate())
    }

    @Test
    fun resolveRateForLongTermReturnsFivePercent() {
        viewModel.months = "12"
        assertEquals(listOf(5.0), viewModel.resolveRate())
    }
}