package com.vishnuhs.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnuhs.expensetracker.data.database.entities.Category
import com.vishnuhs.expensetracker.data.database.entities.Expense
import com.vishnuhs.expensetracker.domain.repository.ExpenseRepository
import com.vishnuhs.expensetracker.ml.ReceiptData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val selectedCategory: String = "Other",
    val selectedDate: Date = Date(),
    val merchant: String = "",
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val receiptImagePath: String? = null,
    val isFromReceipt: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.update {
                    it.copy(categories = categories)
                }
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(merchant = merchant) }
    }

    fun populateFromReceipt(receiptData: ReceiptData, imagePath: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                amount = receiptData.amount?.toString() ?: "",
                merchant = receiptData.merchant ?: "",
                selectedDate = receiptData.date ?: Date(),
                receiptImagePath = imagePath,
                isFromReceipt = true,
                description = receiptData.merchant ?: "Receipt scan"
            )
        }
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val amount = currentState.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a description") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val expense = Expense(
                    amount = amount,
                    description = currentState.description,
                    category = currentState.selectedCategory,
                    date = currentState.selectedDate,
                    merchant = currentState.merchant.takeIf { it.isNotBlank() },
                    receiptImagePath = currentState.receiptImagePath,
                    isFromReceipt = currentState.isFromReceipt
                )

                repository.insertExpense(expense)
                onSuccess()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to save expense",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}