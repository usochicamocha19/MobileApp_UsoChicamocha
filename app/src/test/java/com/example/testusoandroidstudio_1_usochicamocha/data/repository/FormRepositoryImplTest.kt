package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormSyncResponse
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FormRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var formDao: FormDao
    private lateinit var imageDao: ImageDao
    private lateinit var context: Context
    private lateinit var apiService: ApiService
    private lateinit var repository: FormRepositoryImpl
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For simplicity in tests
            .build()
        formDao = db.formDao()
        imageDao = db.imageDao()
        apiService = mockk()

        repository = FormRepositoryImpl(context, formDao, imageDao, apiService)

        // Create a temporary directory for test files
        val tempDir = context.cacheDir
        tempFile = File.createTempFile("test_image", ".jpg", tempDir).apply {
            writeText("test content")
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
        tempFile.delete()
    }

    @Test
    fun getPendingFormsWithStatus_should_return_correct_status() = runTest {
        // Insert test data
        val formEntity = FormEntity(
            UUID = "test-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )
        formDao.insertForm(formEntity)

        val images = listOf(
            ImageEntity(formUUID = "test-uuid", localUri = "uri1.jpg", isSynced = false),
            ImageEntity(formUUID = "test-uuid", localUri = "uri2.jpg", isSynced = true)
        )
        imageDao.insertImages(images)

        repository.getPendingFormsWithStatus().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            val status = list[0]
            assertEquals("test-uuid", status.form.UUID)
            assertEquals(2, status.totalImageCount)
            assertEquals(1, status.syncedImageCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPendingForms_should_return_unsynced_forms() = runTest {
        // Insert test data
        val formEntity = FormEntity(
            UUID = "test-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )
        formDao.insertForm(formEntity)

        repository.getPendingForms().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("test-uuid", list[0].UUID)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveFormLocally_should_save_form_and_images() = runTest {
        val form = Form(
            localId = 1,
            serverId = null,
            UUID = "test-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )

        val imageUris = listOf("content://test/image1.jpg", "content://test/image2.jpg")

        repository.saveFormLocally(form, imageUris)

        // Verify form was saved
        formDao.getPendingFormsFlow().test {
            val pendingForms = awaitItem()
            assertEquals(1, pendingForms.size)
            assertEquals("test-uuid", pendingForms[0].UUID)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun syncForm_should_succeed_on_successful_api_response() = runTest {
        val form = Form(
            localId = 1,
            serverId = null,
            UUID = "test-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )
        // Need to insert the form first so it can be updated
        formDao.insertForm(form.toEntity())

        val mockResponse = FormSyncResponse(id = 1001L)
        coEvery { apiService.syncForm(any()) } returns Response.success(mockResponse)

        val result = repository.syncForm(form)

        assertTrue("Syncing form should succeed", result.isSuccess)

        // Verify form was marked as synced
        val syncedForm = formDao.getFormByUuid("test-uuid")
        assertTrue("Form should be marked as synced", syncedForm?.isSynced == true)
        assertEquals("Server ID should be updated", 1001L, syncedForm?.serverId)
    }

    @Test
    fun syncForm_should_fail_on_api_error() = runTest {
        val form = Form(
            localId = 1,
            serverId = null,
            UUID = "test-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )
        // Need to insert the form first so its status can be updated
        formDao.insertForm(form.toEntity())

        coEvery { apiService.syncForm(any()) } returns Response.error(500, "Server error".toResponseBody())

        val result = repository.syncForm(form)

        assertTrue("Syncing form should fail", result.isFailure)
        assertTrue(
            "Exception message should contain error code",
            result.exceptionOrNull()?.message?.contains("500") == true
        )

        // Verify form is not marked as syncing
        val formInDb = formDao.getFormByUuid("test-uuid")
        assertTrue("Form should not be marked as syncing", formInDb?.isSyncing == false)
    }

    @Test
    fun getPendingImagesForSync_should_return_pending_images() = runTest {
        // Insert synced form
        val formEntity = FormEntity(
            UUID = "synced-form-uuid",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = true,
            isSyncing = false,
            serverId = 1001L
        )
        formDao.insertForm(formEntity)

        // Insert unsynced image
        val image = ImageEntity(formUUID = "synced-form-uuid", localUri = "test_image.jpg", isSynced = false)
        imageDao.insertImages(listOf(image))

        repository.getPendingImagesForSync().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("test_image.jpg", list[0].localUri)
            assertEquals(1001L, list[0].serverId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
