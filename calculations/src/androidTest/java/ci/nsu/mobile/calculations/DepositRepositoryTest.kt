package ci.nsu.mobile.calculations

import app.cash.turbine.test
import ci.nsu.mobile.calculations.data.dao.DepositDao
import ci.nsu.mobile.calculations.data.entity.DepositEntity
import ci.nsu.mobile.calculations.data.repository.DepositRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.collections.get

class DepositRepositoryTest {

    private lateinit var dao: DepositDao
    private lateinit var repository: DepositRepository

    private val userId = 42L

    private fun makeEntity(id: Long = 0, userId: Long = this.userId) = DepositEntity(
        id = id,
        userId = userId,
        startAmount = 10_000.0,
        months = 12,
        rate = 5.0,
        monthlyTopUp = 500.0,
        finalAmount = 17_000.0,
        profit = 1_000.0,
        date = 1_000_000L
    )

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = DepositRepository(dao)
    }

    @Test
    fun getByUserReturnsMappedDomainModels() = runTest {
        val entity = makeEntity(id = 1L)
        every { dao.getByUser(userId) } returns flowOf(listOf(entity))

        repository.getByUser(userId).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(entity.id, list[0].id)
            assertEquals(entity.startAmount, list[0].startAmount, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByUserWithEmptyDaoReturnsEmptyList() = runTest {
        every { dao.getByUser(userId) } returns flowOf(emptyList())

        repository.getByUser(userId).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCallsDaoInsertWithCorrectEntity() = runTest {
        val entity = makeEntity()
        coEvery { dao.insert(any()) } just Runs

        repository.save(entity)

        coVerify(exactly = 1) { dao.insert(entity) }
    }

    @Test
    fun deleteCallsDaoDeleteWithCorrectEntity() = runTest {
        val entity = makeEntity(id = 5L)
        coEvery { dao.delete(any()) } just Runs

        repository.delete(entity)

        coVerify(exactly = 1) { dao.delete(entity) }
    }

    @Test
    fun getByUserReturnsOnlyRecordsMatchingUserId() = runTest {
        val otherId = 99L
        every { dao.getByUser(userId) } returns flowOf(listOf(makeEntity(id = 1L, userId = userId)))
        every { dao.getByUser(otherId) } returns flowOf(listOf(makeEntity(id = 2L, userId = otherId)))

        repository.getByUser(userId).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(userId, list[0].userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveMultipleDepositsCallsDaoInsertMultipleTimes() = runTest {
        coEvery { dao.insert(any()) } just Runs
        val entities = (1..3).map { makeEntity(id = it.toLong()) }

        entities.forEach { repository.save(it) }

        coVerify(exactly = 3) { dao.insert(any()) }
    }
}