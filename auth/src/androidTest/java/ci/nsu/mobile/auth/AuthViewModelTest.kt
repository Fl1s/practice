package ci.nsu.mobile.auth

import ci.nsu.mobile.auth.data.model.dto.GroupDto
import ci.nsu.mobile.auth.data.model.dto.UserDto
import ci.nsu.mobile.auth.data.model.request.PersonDto
import ci.nsu.mobile.auth.data.model.request.RegisterRequest
import ci.nsu.mobile.auth.data.repository.AuthRepository
import ci.nsu.mobile.auth.ui.AuthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var repo: AuthRepository
    private lateinit var viewModel: AuthViewModel

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        viewModel = AuthViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginSuccessCallsOnSuccessAndClearsError() = runTest {
        coEvery { repo.login("user", "pass") } returns Result.success(Unit)
        var called = false

        viewModel.login("user", "pass") { called = true }
        advanceUntilIdle()

        assertTrue(called)
        assertNull(viewModel.error)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun loginSuccessSavesLastCredentials() = runTest {
        coEvery { repo.login("user", "pass") } returns Result.success(Unit)

        viewModel.login("user", "pass") {}
        advanceUntilIdle()

        assertEquals("user", viewModel.lastLogin)
        assertEquals("pass", viewModel.lastPassword)
    }

    @Test
    fun loginFailureSetsErrorMessage() = runTest {
        coEvery { repo.login(any(), any()) } returns Result.failure(RuntimeException("Неверный логин или пароль"))

        viewModel.login("wrong", "wrong") {}
        advanceUntilIdle()

        assertEquals("Неверный логин или пароль", viewModel.error)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun loginFailureDoesNotCallOnSuccess() = runTest {
        coEvery { repo.login(any(), any()) } returns Result.failure(RuntimeException("err"))
        var called = false

        viewModel.login("u", "p") { called = true }
        advanceUntilIdle()

        assertFalse(called)
    }

    @Test
    fun loginSetsIsLoadingToFalseAfterCompletion() = runTest {
        coEvery { repo.login(any(), any()) } returns Result.success(Unit)

        viewModel.login("u", "p") {}
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
    }

    @Test
    fun registerSuccessCallsOnSuccess() = runTest {
        coEvery { repo.register(any()) } returns Result.success(Unit)
        var called = false

        viewModel.register(makeRegisterRequest()) { called = true }
        advanceUntilIdle()

        assertTrue(called)
        assertNull(viewModel.error)
    }

    @Test
    fun registerFailureSetsError() = runTest {
        coEvery { repo.register(any()) } returns Result.failure(RuntimeException("Пользователь уже существует"))

        viewModel.register(makeRegisterRequest()) {}
        advanceUntilIdle()

        assertEquals("Пользователь уже существует", viewModel.error)
    }

    @Test
    fun loadUsersSuccessSetsUsersList() = runTest {
        val users = listOf(UserDto(1, "alice"), UserDto(2, "bob"))
        coEvery { repo.getUsers() } returns Result.success(users)

        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals(2, viewModel.users.size)
        assertEquals("alice", viewModel.users[0].login)
    }

    @Test
    fun loadUsersFailureSetsErrorAndKeepsUsersEmpty() = runTest {
        coEvery { repo.getUsers() } returns Result.failure(RuntimeException("Network error"))

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue(viewModel.users.isEmpty())
        assertEquals("Network error", viewModel.error)
    }

    @Test
    fun loadGroupsSuccessPopulatesGroups() = runTest {
        val groups = listOf(GroupDto(1, "Группа А"), GroupDto(2, "Группа Б"))
        coEvery { repo.getGroups() } returns Result.success(groups)

        viewModel.loadGroups()
        advanceUntilIdle()

        assertEquals(2, viewModel.groups.size)
        assertEquals("Группа А", viewModel.groups[0].groupName)
    }

    @Test
    fun applyQrCredentialsSetsQrLoginAndPassword() {
        viewModel.applyQrCredentials("testUser", "testPass")

        assertEquals("testUser", viewModel.qrLogin)
        assertEquals("testPass", viewModel.qrPassword)
    }

    @Test
    fun clearQrDataNullifiesQrCredentials() {
        viewModel.applyQrCredentials("u", "p")
        viewModel.clearQrData()

        assertNull(viewModel.qrLogin)
        assertNull(viewModel.qrPassword)
    }

    @Test
    fun loginWithEmptyCredentialsStillCallsRepo() = runTest {
        coEvery { repo.login("", "") } returns Result.failure(RuntimeException("Логин не может быть пустым"))

        viewModel.login("", "") {}
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.login("", "") }
    }

    private fun makeRegisterRequest() = RegisterRequest(
        login = "newUser",
        password = "secret",
        email = "user@test.com",
        phoneNumber = "+79001234567",
        roleId = 1,
        authAllowed = true,
        person = PersonDto("Иван", "Иванов", "Иванович", "2000-01-01", "MALE", 1)
    )
}