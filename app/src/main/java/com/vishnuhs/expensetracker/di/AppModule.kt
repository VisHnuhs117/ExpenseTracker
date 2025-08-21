package com.vishnuhs.expensetracker.di

import android.content.Context
import com.vishnuhs.expensetracker.ml.ReceiptTextExtractor
import com.vishnuhs.expensetracker.utils.FileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideReceiptTextExtractor(@ApplicationContext context: Context): ReceiptTextExtractor {
        return ReceiptTextExtractor(context)
    }

    @Provides
    @Singleton
    fun provideFileManager(@ApplicationContext context: Context): FileManager {
        return FileManager(context)
    }
}