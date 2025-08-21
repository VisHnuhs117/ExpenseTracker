package com.vishnuhs.expensetracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val name: String,
    val icon: String,
    val color: String,
    val isDefault: Boolean = false
)