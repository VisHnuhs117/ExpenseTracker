package com.vishnuhs.expensetracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vishnuhs.expensetracker.presentation.ui.screens.AddExpenseScreen
import com.vishnuhs.expensetracker.presentation.ui.screens.CameraScreen
import com.vishnuhs.expensetracker.presentation.ui.screens.ExpenseListScreen
import com.vishnuhs.expensetracker.presentation.ui.screens.HomeScreen
import com.vishnuhs.expensetracker.presentation.viewmodel.AddExpenseViewModel
import com.vishnuhs.expensetracker.utils.NavigationDataHolder

@Composable
fun ExpenseTrackerNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToAdd = { navController.navigate("add_expense") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToExpenses = { navController.navigate("expense_list") }
            )
        }

        composable("expense_list") {
            ExpenseListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate("add_expense") }
            )
        }

        composable("add_expense") {
            val addExpenseViewModel = hiltViewModel<AddExpenseViewModel>()

            // Check if we have receipt data to populate
            LaunchedEffect(Unit) {
                NavigationDataHolder.receiptData?.let { data ->
                    addExpenseViewModel.populateFromReceipt(data, NavigationDataHolder.imagePath)
                    NavigationDataHolder.clearData() // Clear after use
                }
            }

            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate("camera_from_add") },
                viewModel = addExpenseViewModel
            )
        }

        composable("camera") {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = { receiptData, imagePath ->
                    // Store data and navigate
                    NavigationDataHolder.setData(receiptData, imagePath)
                    navController.navigate("add_expense") {
                        popUpTo("camera") { inclusive = true }
                    }
                }
            )
        }

        composable("camera_from_add") {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = { receiptData, imagePath ->
                    // Store data and go back to add expense
                    NavigationDataHolder.setData(receiptData, imagePath)
                    navController.popBackStack()
                }
            )
        }
    }
}