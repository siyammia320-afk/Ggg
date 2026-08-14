package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "created_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phone: String,
    val uid: String,
    val name: String,
    val password: String,
    val cookies: String,
    val createdAt: Long = System.currentTimeMillis()
)
