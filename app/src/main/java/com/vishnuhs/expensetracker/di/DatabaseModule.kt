package com.vishnuhs.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.vishnuhs.expensetracker.data.database.ExpenseDatabase
import com.vishnuhs.expensetracker.data.database.dao.CategoryDao
import com.vishnuhs.expensetracker.data.database.dao.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return ExpenseDatabase.getDatabase(context)
    }

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideCategoryDao(database: ExpenseDatabase): CategoryDao {
        return database.categoryDao()
    }
}