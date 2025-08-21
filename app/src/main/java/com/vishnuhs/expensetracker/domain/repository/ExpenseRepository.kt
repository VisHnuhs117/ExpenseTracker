package com.vishnuhs.expensetracker.domain.repository

import com.vishnuhs.expensetracker.data.database.dao.CategoryTotal
import com.vishnuhs.expensetracker.data.database.entities.Category
import com.vishnuhs.expensetracker.data.database.entities.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<Expense>>
    fun getCategoryTotals(): Flow<List<CategoryTotal>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)

    fun getAllCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)
    suspend fun insertCategories(categories: List<Category>)
    suspend fun deleteCategory(category: Category)
}