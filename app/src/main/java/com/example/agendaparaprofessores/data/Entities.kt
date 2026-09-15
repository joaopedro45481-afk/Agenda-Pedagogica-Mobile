package com.example.agendaparaprofessores.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val grade: String? = null
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
    indices = [Index("subjectId"), Index("classId")]
)
data class LessonReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val classId: Long,
    val title: String,
    val summary: String,
    val difficulty: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val bimester: Int
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: Long,
    val studentId: Long,
    val score: Double? = null   // null = NA (não avaliado)
)
