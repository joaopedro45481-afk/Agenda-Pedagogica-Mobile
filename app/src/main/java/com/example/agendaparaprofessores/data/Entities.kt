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
    indices = [Index("subjectId"), Index("classId"), Index("lessonPlanId")]
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
    val bimester: Int,
    /** Plano de aula que originou este relatório (null = relatório avulso). NOVO */
    val lessonPlanId: Long? = null
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

/* ============================================================
 *  NOVO — PREPARADOR DE AULA
 *  O plano é o "antes" da aula; o relatório é o "depois".
 *  subjectId/classId aceitam null: se você apagar a turma,
 *  o plano continua existindo (não perde o seu planejamento).
 * ============================================================ */
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
        Index("plannedYear", "plannedMonth", "plannedDay")
    ]
)
data class LessonPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val subjectId: Long? = null,
    val classId: Long? = null,

    val title: String,

    /** O que os alunos deverão aprender. */
    val objective: String = "",

    /** O que será trabalhado. */
    val content: String = "",

    /** Como será a aula (passo a passo). */
    val methodology: String = "",

    /** Listas editáveis: cada item em uma linha. Sem limite fixo. */
    val materials: String = "",
    val activities: String = "",
    val homework: String = "",

    val durationMinutes: Int? = null,

    val plannedDay: Int? = null,
    val plannedMonth: Int? = null,
    val plannedYear: Int? = null,

    /** Texto livre: "planejada", "ministrada", "adiada", o que você quiser. */
    val status: String = "planejada",

    val notes: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
