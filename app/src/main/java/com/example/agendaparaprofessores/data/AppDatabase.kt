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
        Student::class, Assessment::class, Grade::class,
        LessonPlan::class, Attendance::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun schoolClassDao(): SchoolClassDao
    abstract fun lessonReportDao(): LessonReportDao
    abstract fun studentDao(): StudentDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun lessonPlanDao(): LessonPlanDao
    abstract fun attendanceDao(): AttendanceDao

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

        // ============ Preparador de Aula ============
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `lesson_plans` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`subjectId` INTEGER, " +
                            "`classId` INTEGER, " +
                            "`title` TEXT NOT NULL, " +
                            "`objective` TEXT NOT NULL, " +
                            "`content` TEXT NOT NULL, " +
                            "`methodology` TEXT NOT NULL, " +
                            "`materials` TEXT NOT NULL, " +
                            "`activities` TEXT NOT NULL, " +
                            "`homework` TEXT NOT NULL, " +
                            "`durationMinutes` INTEGER, " +
                            "`plannedDay` INTEGER, " +
                            "`plannedMonth` INTEGER, " +
                            "`plannedYear` INTEGER, " +
                            "`status` TEXT NOT NULL, " +
                            "`notes` TEXT NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL, " +
                            "`updatedAt` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE SET NULL, " +
                            "FOREIGN KEY(`classId`) REFERENCES `classes`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE SET NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_lesson_plans_subjectId` " +
                            "ON `lesson_plans` (`subjectId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_lesson_plans_classId` " +
                            "ON `lesson_plans` (`classId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_lesson_plans_status` " +
                            "ON `lesson_plans` (`status`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS " +
                            "`index_lesson_plans_plannedYear_plannedMonth_plannedDay` " +
                            "ON `lesson_plans` (`plannedYear`, `plannedMonth`, `plannedDay`)"
                )

                db.execSQL("ALTER TABLE `lesson_reports` ADD COLUMN `lessonPlanId` INTEGER")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_lesson_reports_lessonPlanId` " +
                            "ON `lesson_reports` (`lessonPlanId`)"
                )
            }
        }

        // ============ Frequência (chamada) ============
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `attendance` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`classId` INTEGER NOT NULL, " +
                            "`studentId` INTEGER NOT NULL, " +
                            "`day` INTEGER NOT NULL, " +
                            "`month` INTEGER NOT NULL, " +
                            "`year` INTEGER NOT NULL, " +
                            "`present` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`classId`) REFERENCES `classes`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_attendance_classId` " +
                            "ON `attendance` (`classId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_attendance_studentId` " +
                            "ON `attendance` (`studentId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                            "`index_attendance_studentId_day_month_year` " +
                            "ON `attendance` (`studentId`, `day`, `month`, `year`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_attendance_classId_year_month_day` " +
                            "ON `attendance` (`classId`, `year`, `month`, `day`)"
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
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5
                    )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
