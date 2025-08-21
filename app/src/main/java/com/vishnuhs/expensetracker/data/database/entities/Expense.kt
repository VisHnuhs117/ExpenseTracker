package com.vishnuhs.expensetracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Date,
    val merchant: String? = null,
    val receiptImagePath: String? = null,
    val isFromReceipt: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)