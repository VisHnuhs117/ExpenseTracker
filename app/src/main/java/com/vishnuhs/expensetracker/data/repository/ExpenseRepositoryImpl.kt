package com.vishnuhs.expensetracker.data.repository

import com.vishnuhs.expensetracker.data.database.dao.CategoryDao
import com.vishnuhs.expensetracker.data.database.dao.CategoryTotal
import com.vishnuhs.expensetracker.data.database.dao.ExpenseDao
import com.vishnuhs.expensetracker.data.database.entities.Category
import com.vishnuhs.expensetracker.data.database.entities.Expense
import com.vishnuhs.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category)
    }

    override fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    override fun getCategoryTotals(): Flow<List<CategoryTotal>> {
        return expenseDao.getCategoryTotals()
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)
    }

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.copy(updatedAt = Date()))
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    override suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    override suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories)
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
}