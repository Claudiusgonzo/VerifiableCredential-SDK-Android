package com.microsoft.portableIdentity.sdk.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard

@Dao
interface PortableIdentityCardDao {

    @Query("SELECT * FROM PortableIdentityCard")
    fun getAllCards(): LiveData<List<PortableIdentityCard>>

    @Insert
    suspend fun insert(portableIdentityCard: PortableIdentityCard)

    @Delete
    suspend fun delete(portableIdentityCard: PortableIdentityCard)
}