package com.rizowan.taskr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rizowan.taskr.data.local.dao.SubTaskDao
import com.rizowan.taskr.data.local.dao.TaskDao
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rizowan.taskr.data.local.dao.CategoryDao
import com.rizowan.taskr.data.local.entity.Category
import com.rizowan.taskr.data.local.entity.SubTask
import com.rizowan.taskr.data.local.entity.Task

/**
 * Room database for Taskr application.
 * Contains Task, SubTask, and Category entities.
 */
@Database(
    entities = [Task::class, SubTask::class, Category::class],
    version = 2,
    exportSchema = true  // Enable schema export for proper migration tracking
)
@TypeConverters(Converters::class)
abstract class TaskrDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    abstract fun subTaskDao(): SubTaskDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create categories table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `isSystem` INTEGER NOT NULL)"
                )
                
                // Add new columns to tasks table
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `categoryId` INTEGER")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
