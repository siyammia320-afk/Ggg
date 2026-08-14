package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM created_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Query("DELETE FROM created_accounts")
    suspend fun clearAllAccounts()

    @Query("DELETE FROM created_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)
}
