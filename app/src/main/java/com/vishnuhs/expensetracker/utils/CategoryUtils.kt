package com.vishnuhs.expensetracker.utils

object CategoryUtils {

    fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "food & dining" -> "🍽️"
            "transportation" -> "🚗"
            "shopping" -> "🛍️"
            "entertainment" -> "🎬"
            "bills & utilities" -> "💡"
            "healthcare" -> "🏥"
            "education" -> "📚"
            "travel" -> "✈️"
            else -> "📋"
        }
    }

    fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
        return when (category.lowercase()) {
            "food & dining" -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
            "transportation" -> androidx.compose.ui.graphics.Color(0xFF4ECDC4)
            "shopping" -> androidx.compose.ui.graphics.Color(0xFF45B7D1)
            "entertainment" -> androidx.compose.ui.graphics.Color(0xFF96CEB4)
            "bills & utilities" -> androidx.compose.ui.graphics.Color(0xFFFFEAA7)
            "healthcare" -> androidx.compose.ui.graphics.Color(0xFFDDA0DD)
            "education" -> androidx.compose.ui.graphics.Color(0xFF98D8C8)
            "travel" -> androidx.compose.ui.graphics.Color(0xFFF7DC6F)
            else -> androidx.compose.ui.graphics.Color(0xFFAED6F1)
        }
    }
}