package com.example.agendaparaprofessores.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String
)

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val grade: String? = null
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val classId: Long,
    val name: String
)

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class Assessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val classId: Long,
    val title: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val bimester: Int
)

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Assessment::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("assessmentId"),
        Index("studentId"),
        Index(value = ["assessmentId", "studentId"], unique = true)
    ]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val assessmentId: Long,
    val studentId: Long,
    val score: Double?
)

@Entity(
    tableName = "lesson_plans",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index("classId"),
        Index("status"),
        Index(value = ["plannedYear", "plannedMonth", "plannedDay"])
    ]
)
data class LessonPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectId: Long?,
    val classId: Long?,
    val title: String,
    val objective: String,
    val content: String,
    val methodology: String,
    val materials: String,
    val activities: String,
    val homework: String,
    val durationMinutes: Int?,
    val plannedDay: Int?,
    val plannedMonth: Int?,
    val plannedYear: Int?,
    val status: String, // planejada, ministrada, cancelada
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lesson_reports",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("classId"),
        Index("lessonPlanId")
    ]
)
data class LessonReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectId: Long,
    val classId: Long,
    val title: String,
    val summary: String,
    val difficulty: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val bimester: Int,
    val lessonPlanId: Long? = null
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("studentId"),
        Index(value = ["studentId", "day", "month", "year"], unique = true),
        Index(value = ["classId", "year", "month", "day"])
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val classId: Long,
    val studentId: Long,
    val day: Int,
    val month: Int,
    val year: Int,
    val present: Boolean
)
