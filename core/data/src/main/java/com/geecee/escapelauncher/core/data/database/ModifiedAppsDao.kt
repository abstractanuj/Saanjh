package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity

@Dao
interface ModifiedAppsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(app: ModifiedAppEntity): Long

    @Update
    suspend fun updateAll(apps: List<ModifiedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: ModifiedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<ModifiedAppEntity>)

    @Query("SELECT * FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getByPackageId(packageId: String): ModifiedAppEntity?

    @Query("SELECT displayName FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getDisplayName(packageId: String): String?

    @Transaction
    suspend fun setDisplayName(packageId: String, displayName: String?) {
        val current = getByPackageId(packageId)
        if (current == null) {
            upsert(
                ModifiedAppEntity(
                    packageId = packageId,
                    displayName = displayName,
                    isHidden = false,
                    isChallenge = false,
                    favouritePosition = null,
                    themeColor = null,
                    installTime = null
                )
            )
        } else {
            upsert(current.copy(displayName = displayName))
        }
    }

    @Transaction
    suspend fun clearDisplayName(packageId: String) {
        setDisplayName(packageId, null)
    }

    @Query("UPDATE modifiedApps SET isHidden = :isHidden WHERE packageId = :packageId")
    suspend fun updateIsHidden(packageId: String, isHidden: Boolean)

    @Query("SELECT isHidden FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getIsHidden(packageId: String): Boolean?

    @Query("UPDATE modifiedApps SET isChallenge = :isChallenge WHERE packageId = :packageId")
    suspend fun updateIsChallenge(packageId: String, isChallenge: Boolean)

    @Query("SELECT isChallenge FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getIsChallenge(packageId: String): Boolean?

    @Query("UPDATE modifiedApps SET favouritePosition = :favouritePosition WHERE packageId = :packageId")
    suspend fun updateFavouritePosition(packageId: String, favouritePosition: Double?)

    @Query("SELECT favouritePosition FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getFavouritePosition(packageId: String): Double?

    @Transaction
    suspend fun setIsHidden(packageId: String, isHidden: Boolean) {
        ensureRowExists(packageId)
        updateIsHidden(packageId, isHidden)
    }

    @Transaction
    suspend fun setIsChallenge(packageId: String, isChallenge: Boolean) {
        ensureRowExists(packageId)
        updateIsChallenge(packageId, isChallenge)
    }

    @Transaction
    suspend fun setFavouritePosition(packageId: String, favouritePosition: Double?) {
        ensureRowExists(packageId)
        updateFavouritePosition(packageId, favouritePosition)
    }

    @Transaction
    suspend fun clearFavouritePosition(packageId: String) {
        setFavouritePosition(packageId, null)
    }

    @Query("UPDATE modifiedApps SET category = :category WHERE packageId = :packageId")
    suspend fun updateCategory(packageId: String, category: String?)

    @Query("SELECT packageId, category FROM modifiedApps WHERE category IS NOT NULL")
    fun getCategoriesMapFlow(): kotlinx.coroutines.flow.Flow<List<PackageCategory>>

    @Query("SELECT packageId, category FROM modifiedApps WHERE category IS NOT NULL")
    suspend fun getCategoriesMap(): List<PackageCategory>

    @Transaction
    suspend fun setCategory(packageId: String, category: String?) {
        ensureRowExists(packageId)
        updateCategory(packageId, category)
    }

    @Query("UPDATE modifiedApps SET themeColor = :themeColor, installTime = :installTime WHERE packageId = :packageId")
    suspend fun updateMetadata(packageId: String, themeColor: Int?, installTime: Long?)

    @Transaction
    suspend fun setMetadata(packageId: String, themeColor: Int?, installTime: Long?) {
        ensureRowExists(packageId)
        updateMetadata(packageId, themeColor, installTime)
    }

    @Transaction
    suspend fun ensureRowExists(packageId: String) {
        insertIfMissing(
            ModifiedAppEntity(
                packageId = packageId,
                displayName = null,
                isHidden = false,
                isChallenge = false,
                favouritePosition = null,
                themeColor = null,
                installTime = null
            )
        )
    }

    @Query(
        """
        SELECT *
        FROM modifiedApps
        WHERE favouritePosition IS NOT NULL
        ORDER BY favouritePosition ASC,
                 COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    fun getFavouriteAppsInOrderFlow(): kotlinx.coroutines.flow.Flow<List<ModifiedAppEntity>>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isHidden = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    fun getHiddenPackageIdsFlow(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isChallenge = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    fun getChallengePackageIdsFlow(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query(
        """
        SELECT *
        FROM modifiedApps
        WHERE favouritePosition IS NOT NULL
        ORDER BY favouritePosition ASC,
                 COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    suspend fun getFavouriteAppsInOrder(): List<ModifiedAppEntity>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isHidden = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    suspend fun getHiddenPackageIds(): List<String>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isChallenge = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    suspend fun getChallengePackageIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageId = :packageId AND isHidden = 1)")
    suspend fun isHidden(packageId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageId = :packageId AND isChallenge = 1)")
    suspend fun isChallenge(packageId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageId = :packageId AND favouritePosition IS NOT NULL)")
    suspend fun isFavourite(packageId: String): Boolean

    @Query("SELECT * FROM modifiedApps")
    suspend fun getAllModifiedApps(): List<ModifiedAppEntity>

    @Query("DELETE FROM modifiedApps WHERE packageId = :packageId")
    suspend fun deleteByPackageId(packageId: String)

    @Query(
        """
        DELETE FROM modifiedApps
        WHERE displayName IS NULL
          AND isHidden = 0
          AND isChallenge = 0
          AND favouritePosition IS NULL
          AND category IS NULL
          AND themeColor IS NULL
          AND installTime IS NULL
        """
    )
    suspend fun purgeAppsWithNoData(): Int
}

data class PackageCategory(
    val packageId: String,
    val category: String?
)
