package ci.nsu.mobile.calculations

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ci.nsu.mobile.calculations.data.dao.DepositDao
import ci.nsu.mobile.calculations.data.db.AppDatabase
import ci.nsu.mobile.calculations.data.entity.DepositEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.collections.get

@RunWith(AndroidJUnit4::class)
class DepositDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DepositDao

    private fun makeEntity(
        id: Long = 0,
        userId: Long = 1L,
        startAmount: Double = 10_000.0,
        date: Long = System.currentTimeMillis()
    ) = DepositEntity(
        id           = id,
        userId       = userId,
        startAmount  = startAmount,
        months       = 12,
        rate         = 5.0,
        monthlyTopUp = 500.0,
        finalAmount  = 17_000.0,
        profit       = 1_000.0,
        date         = date
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.depositDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndGetByUserReturnsInsertedRecord() = runTest {
        dao.insert(makeEntity(userId = 1L))

        dao.getByUser(1L).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(10_000.0, list[0].startAmount, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertMultipleRecordsReturnsAllOfThem() = runTest {
        dao.insert(makeEntity(userId = 1L, startAmount = 1_000.0))
        dao.insert(makeEntity(userId = 1L, startAmount = 2_000.0))
        dao.insert(makeEntity(userId = 1L, startAmount = 3_000.0))

        dao.getByUser(1L).test {
            assertEquals(3, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByUserReturnsOnlyRecordsMatchingUserId() = runTest {
        dao.insert(makeEntity(userId = 1L))
        dao.insert(makeEntity(userId = 1L))
        dao.insert(makeEntity(userId = 2L))

        dao.getByUser(1L).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        dao.getByUser(2L).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByUserForUnknownUserReturnsEmptyList() = runTest {
        dao.getByUser(999L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByEntityRemovesOnlyThatRecord() = runTest {
        dao.insert(makeEntity(userId = 1L))
        var inserted: DepositEntity? = null
        dao.getByUser(1L).test {
            inserted = awaitItem().first()
            cancelAndIgnoreRemainingEvents()
        }

        dao.delete(inserted!!)

        dao.getByUser(1L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByIdRemovesOnlyThatRecord() = runTest {
        dao.insert(makeEntity(userId = 1L))
        var id = 0L
        dao.getByUser(1L).test {
            id = awaitItem().first().id
            cancelAndIgnoreRemainingEvents()
        }

        dao.deleteById(id)

        dao.getByUser(1L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteOneOfManyLeavesRemainingRecordsIntact() = runTest {
        dao.insert(makeEntity(userId = 1L, startAmount = 1_000.0))
        dao.insert(makeEntity(userId = 1L, startAmount = 2_000.0))
        var toDelete: DepositEntity? = null
        dao.getByUser(1L).test {
            toDelete = awaitItem().first { it.startAmount == 1_000.0 }
            cancelAndIgnoreRemainingEvents()
        }

        dao.delete(toDelete!!)

        dao.getByUser(1L).test {
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals(2_000.0, remaining[0].startAmount, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByUserReturnsSortedByDateDescending() = runTest {
        dao.insert(makeEntity(userId = 1L, date = 1_000L))
        dao.insert(makeEntity(userId = 1L, date = 3_000L))
        dao.insert(makeEntity(userId = 1L, date = 2_000L))

        dao.getByUser(1L).test {
            val list = awaitItem()
            assertTrue(list[0].date >= list[1].date)
            assertTrue(list[1].date >= list[2].date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertedRecordHasAutoGeneratedId() = runTest {
        dao.insert(makeEntity(id = 0))

        dao.getByUser(1L).test {
            val record = awaitItem().first()
            assertTrue(record.id > 0)
            cancelAndIgnoreRemainingEvents()
        }
    }
}