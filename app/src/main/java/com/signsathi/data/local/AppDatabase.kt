package com.signsathi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.signsathi.data.local.dao.LessonDao
import com.signsathi.data.local.dao.UserProgressDao
import com.signsathi.data.local.entity.LessonNodeEntity
import com.signsathi.data.local.entity.LessonUnitEntity
import com.signsathi.data.local.entity.UserProgressEntity

@Database(
    entities = [
        LessonUnitEntity::class,
        LessonNodeEntity::class,
        UserProgressEntity::class
    ],
    version = 3,
    exportSchema = true   // generates a schema JSON file — useful for migration tracking
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun userProgressDao(): UserProgressDao
}
