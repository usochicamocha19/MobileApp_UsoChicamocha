package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TokenManagerTest {

    private lateinit var tokenManager: TokenManager
    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testFile = File(context.cacheDir, "test_session_data.preferences_pb")
        if (testFile.exists()) testFile.delete() // Clean up any existing data
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { testFile }
        )
        tokenManager = TokenManager(testDataStore)
    }

    @Test
    fun saveTokens_should_store_access_and_refresh_tokens() = runTest {
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"

        tokenManager.saveTokens(accessToken, refreshToken)

        val savedAccessToken = tokenManager.getAccessToken().first()
        val savedRefreshToken = tokenManager.getRefreshToken().first()

        assertEquals(accessToken, savedAccessToken)
        assertEquals(refreshToken, savedRefreshToken)
    }

    @Test
    fun saveUserId_should_store_user_id() = runTest {
        val userId = 42

        tokenManager.saveUserId(userId)

        val savedUserId = tokenManager.getUserId().first()

        assertEquals(userId, savedUserId)
    }

    @Test
    fun getAccessToken_should_return_null_when_no_token_stored() = runTest {
        val accessToken = tokenManager.getAccessToken().first()

        assertNull(accessToken)
    }

    @Test
    fun getRefreshToken_should_return_null_when_no_token_stored() = runTest {
        val refreshToken = tokenManager.getRefreshToken().first()

        assertNull(refreshToken)
    }

    @Test
    fun getUserId_should_return_null_when_no_user_id_stored() = runTest {
        val userId = tokenManager.getUserId().first()

        assertNull(userId)
    }

    @Test
    fun clearSessionData_should_remove_all_stored_data() = runTest {
        // First save some data
        tokenManager.saveTokens("access_token", "refresh_token")
        tokenManager.saveUserId(123)

        // Verify data is stored
        assertEquals("access_token", tokenManager.getAccessToken().first())
        assertEquals("refresh_token", tokenManager.getRefreshToken().first())
        assertEquals(123, tokenManager.getUserId().first())

        // Clear session data
        tokenManager.clearSessionData()

        // Verify all data is cleared
        assertNull(tokenManager.getAccessToken().first())
        assertNull(tokenManager.getRefreshToken().first())
        assertNull(tokenManager.getUserId().first())
    }

    @Test
    fun saveTokens_should_overwrite_existing_tokens() = runTest {
        // Save initial tokens
        tokenManager.saveTokens("old_access", "old_refresh")

        // Save new tokens
        tokenManager.saveTokens("new_access", "new_refresh")

        // Verify only new tokens are stored
        assertEquals("new_access", tokenManager.getAccessToken().first())
        assertEquals("new_refresh", tokenManager.getRefreshToken().first())
    }

    @Test
    fun saveUserId_should_overwrite_existing_user_id() = runTest {
        // Save initial user ID
        tokenManager.saveUserId(111)

        // Save new user ID
        tokenManager.saveUserId(222)

        // Verify only new user ID is stored
        assertEquals(222, tokenManager.getUserId().first())
    }

    @Test
    fun getAccessToken_should_emit_flow_updates() = runTest {
        // Initially null
        assertNull(tokenManager.getAccessToken().first())

        // Save token
        tokenManager.saveTokens("test_token", "refresh_token")

        // Should emit new value
        assertEquals("test_token", tokenManager.getAccessToken().first())
    }

    @Test
    fun getRefreshToken_should_emit_flow_updates() = runTest {
        // Initially null
        assertNull(tokenManager.getRefreshToken().first())

        // Save token
        tokenManager.saveTokens("access_token", "test_refresh")

        // Should emit new value
        assertEquals("test_refresh", tokenManager.getRefreshToken().first())
    }

    @Test
    fun getUserId_should_emit_flow_updates() = runTest {
        // Initially null
        assertNull(tokenManager.getUserId().first())

        // Save user ID
        tokenManager.saveUserId(999)

        // Should emit new value
        assertEquals(999, tokenManager.getUserId().first())
    }

    @Test
    fun saveTokens_with_empty_strings_should_store_empty_strings() = runTest {
        tokenManager.saveTokens("", "")

        assertEquals("", tokenManager.getAccessToken().first())
        assertEquals("", tokenManager.getRefreshToken().first())
    }

    @Test
    fun saveUserId_with_zero_should_store_zero() = runTest {
        tokenManager.saveUserId(0)

        assertEquals(0, tokenManager.getUserId().first())
    }

    @Test
    fun saveUserId_with_negative_value_should_store_negative_value() = runTest {
        tokenManager.saveUserId(-1)

        assertEquals(-1, tokenManager.getUserId().first())
    }
}