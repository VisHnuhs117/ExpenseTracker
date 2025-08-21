package com.vishnuhs.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnuhs.expensetracker.data.database.dao.CategoryTotal
import com.vishnuhs.expensetracker.data.database.entities.Expense
import com.vishnuhs.expensetracker.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val totalAmount: Double = 0.0
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)

    init {
        loadExpenses()
        loadCategoryTotals()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            combine(
                if (_selectedCategory.value != null) {
                    repository.getExpensesByCategory(_selectedCategory.value!!)
                } else {
                    repository.getAllExpenses()
                },
                _selectedCategory
            ) { expenses, selectedCategory ->
                _uiState.update { currentState ->
                    currentState.copy(
                        expenses = expenses,
                        selectedCategory = selectedCategory,
                        totalAmount = expenses.sumOf { it.amount },
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun loadCategoryTotals() {
        viewModelScope.launch {
            repository.getCategoryTotals().collect { totals ->
                _uiState.update { currentState ->
                    currentState.copy(categoryTotals = totals)
                }
            }
        }
    }

    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        loadExpenses()
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.insertExpense(expense)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}