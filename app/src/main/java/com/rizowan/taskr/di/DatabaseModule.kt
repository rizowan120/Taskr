package com.rizowan.taskr.di

import android.content.Context
import androidx.room.Room
import com.rizowan.taskr.data.local.TaskrDatabase
import com.rizowan.taskr.data.local.dao.SubTaskDao
import com.rizowan.taskr.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and DAO instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TaskrDatabase {
        return Room.databaseBuilder(
            context,
            TaskrDatabase::class.java,
            "taskr_database"
        )
            // TODO: Add proper migrations before production release
            // Example: .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            // fallbackToDestructiveMigration is kept for development convenience only
            // WARNING: This will DELETE all user data on schema changes!
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskrDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideSubTaskDao(database: TaskrDatabase): SubTaskDao {
        return database.subTaskDao()
    }
}
