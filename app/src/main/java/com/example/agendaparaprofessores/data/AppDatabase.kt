package com.example.agendaparaprofessores.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Subject::class, SchoolClass::class, LessonReport::class,
        Student::class, Assessment::class, Grade::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun schoolClassDao(): SchoolClassDao
    abstract fun lessonReportDao(): LessonReportDao
    abstract fun studentDao(): StudentDao
    abstract fun assessmentDao(): AssessmentDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `students` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`classId` INTEGER NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "FOREIGN KEY(`classId`) REFERENCES `classes`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_students_classId` " +
                            "ON `students` (`classId`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `assessments` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`classId` INTEGER NOT NULL, " +
                            "`title` TEXT NOT NULL, " +
                            "`day` INTEGER NOT NULL, " +
                            "`month` INTEGER NOT NULL, " +
                            "`year` INTEGER NOT NULL, " +
                            "`bimester` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`classId`) REFERENCES `classes`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_assessments_classId` " +
                            "ON `assessments` (`classId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `grades` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`assessmentId` INTEGER NOT NULL, " +
                            "`studentId` INTEGER NOT NULL, " +
                            "`score` REAL, " +
                            "FOREIGN KEY(`assessmentId`) REFERENCES `assessments`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_grades_assessmentId` " +
                            "ON `grades` (`assessmentId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_grades_studentId` " +
                            "ON `grades` (`studentId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_grades_assessmentId_studentId` " +
                            "ON `grades` (`assessmentId`, `studentId`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "professor_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
