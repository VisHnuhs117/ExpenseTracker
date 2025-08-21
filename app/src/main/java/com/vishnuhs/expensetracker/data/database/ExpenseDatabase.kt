package com.vishnuhs.expensetracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.vishnuhs.expensetracker.data.database.converters.DateConverter
import com.vishnuhs.expensetracker.data.database.dao.CategoryDao
import com.vishnuhs.expensetracker.data.database.dao.ExpenseDao
import com.vishnuhs.expensetracker.data.database.entities.Category
import com.vishnuhs.expensetracker.data.database.entities.Expense

@Database(
    entities = [Expense::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insert default categories
            db.execSQL("""
                INSERT INTO categories (name, icon, color, isDefault) VALUES
                ('Food & Dining', '🍽️', '#FF6B6B', 1),
                ('Transportation', '🚗', '#4ECDC4', 1),
                ('Shopping', '🛍️', '#45B7D1', 1),
                ('Entertainment', '🎬', '#96CEB4', 1),
                ('Bills & Utilities', '💡', '#FFEAA7', 1),
                ('Healthcare', '🏥', '#DDA0DD', 1),
                ('Education', '📚', '#98D8C8', 1),
                ('Travel', '✈️', '#F7DC6F', 1),
                ('Other', '📋', '#AED6F1', 1)
            """)
        }
    }
}