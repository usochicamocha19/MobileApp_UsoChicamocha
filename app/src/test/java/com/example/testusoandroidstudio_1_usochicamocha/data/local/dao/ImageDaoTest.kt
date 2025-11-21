package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var imageDao: ImageDao
    private lateinit var formDao: FormDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        imageDao = db.imageDao()
        formDao = db.formDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun createTestForm(uuid: String, isSynced: Boolean, serverId: Long? = null): FormEntity {
        return FormEntity(
            UUID = uuid,
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
            isSynced = isSynced,
            isSyncing = false,
            serverId = if (isSynced) serverId ?: 1001L else null
        )
    }

    @Test
    fun insertImages_should_insert_images_successfully() = runTest {
        val form1 = createTestForm(uuid = "test-uuid-1", isSynced = true, serverId = 1001L)
        val form2 = createTestForm(uuid = "test-uuid-2", isSynced = true, serverId = 1002L)
        formDao.insertForm(form1)
        formDao.insertForm(form2)

        val images = listOf(
            ImageEntity(formUUID = "test-uuid-1", localUri = "uri1.jpg", isSynced = false),
            ImageEntity(formUUID = "test-uuid-2", localUri = "uri2.jpg", isSynced = true)
        )

        imageDao.insertImages(images)

        // Verify by checking if they appear in pending sync (only unsynced images should appear)
        val pendingImages = imageDao.getPendingImagesForSync().first()
        assertEquals(1, pendingImages.size)
        assertEquals("uri1.jpg", pendingImages[0].localUri)
        assertFalse(pendingImages[0].localUri.contains("uri2.jpg"))
    }

    @Test
    fun markAsSynced_should_update_image_sync_status() = runTest {
        val form = createTestForm("test-uuid", true, 1003L)
        formDao.insertForm(form)

        val image = ImageEntity(formUUID = "test-uuid", localUri = "uri1.jpg", isSynced = false)
        imageDao.insertImages(listOf(image))

        // Before marking as synced, should be in pending list
        var pendingImages = imageDao.getPendingImagesForSync().first()
        assertEquals(1, pendingImages.size)
        val imageId = pendingImages[0].localId

        imageDao.markAsSynced(imageId)

        // After marking as synced, should not be in pending list
        pendingImages = imageDao.getPendingImagesForSync().first()
        assertTrue(pendingImages.isEmpty())
    }

    @Test
    fun getPendingImagesForSync_should_only_return_unsynced_images_with_synced_forms() = runTest {
        // Insert synced form
        val syncedForm = FormEntity(
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
        formDao.insertForm(syncedForm)

        // Insert unsynced form
        val unsyncedForm = FormEntity(
            UUID = "unsynced-form-uuid",
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
        formDao.insertForm(unsyncedForm)

        // Insert images for both forms
        val images = listOf(
            ImageEntity(formUUID = "synced-form-uuid", localUri = "synced_form_image.jpg", isSynced = false),
            ImageEntity(formUUID = "unsynced-form-uuid", localUri = "unsynced_form_image.jpg", isSynced = false)
        )
        imageDao.insertImages(images)

        val pendingImages = imageDao.getPendingImagesForSync().first()

        // Should only return image from synced form
        assertEquals(1, pendingImages.size)
        assertEquals("synced_form_image.jpg", pendingImages[0].localUri)
        assertEquals(1001L, pendingImages[0].serverId)
    }

    @Test
    fun getPendingImagesForSync_should_not_return_already_synced_images() = runTest {
        // Insert synced form
        val syncedForm = FormEntity(
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
        formDao.insertForm(syncedForm)

        // Insert both synced and unsynced images for the same form
        val images = listOf(
            ImageEntity(formUUID = "synced-form-uuid", localUri = "synced_image.jpg", isSynced = true),
            ImageEntity(formUUID = "synced-form-uuid", localUri = "unsynced_image.jpg", isSynced = false)
        )
        imageDao.insertImages(images)

        val pendingImages = imageDao.getPendingImagesForSync().first()

        // Should only return the unsynced image
        assertEquals(1, pendingImages.size)
        assertEquals("unsynced_image.jpg", pendingImages[0].localUri)
    }

    @Test
    fun getPendingImagesForSync_should_limit_results_to_10() = runTest {
        // Insert synced form
        val syncedForm = FormEntity(
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
        formDao.insertForm(syncedForm)

        // Insert 15 unsynced images
        val images = (1..15).map { index ->
            ImageEntity(formUUID = "synced-form-uuid", localUri = "image_$index.jpg", isSynced = false)
        }
        imageDao.insertImages(images)

        val pendingImages = imageDao.getPendingImagesForSync().first()

        // Should be limited to 10 results
        assertEquals(10, pendingImages.size)
    }

    @Test
    fun getPendingImagesForSync_should_return_correct_ImageForSync_objects() = runTest {
        // Insert synced form
        val syncedForm = FormEntity(
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
        formDao.insertForm(syncedForm)

        // Insert image
        val image = ImageEntity(formUUID = "synced-form-uuid", localUri = "test_image.jpg", isSynced = false)
        imageDao.insertImages(listOf(image))

        val pendingImages = imageDao.getPendingImagesForSync().first()

        assertEquals(1, pendingImages.size)
        val imageForSync = pendingImages[0]
        assertTrue(imageForSync is ImageForSync)
        assertEquals(1001L, imageForSync.serverId)
        assertEquals("test_image.jpg", imageForSync.localUri)
        assertTrue(imageForSync.localId > 0) // Should have auto-generated ID
    }
}
