package com.example.agendaparaprofessores.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun observeAll(): Flow<List<Subject>>

    @Insert suspend fun insert(subject: Subject)
    @Update suspend fun update(subject: Subject)
    @Delete suspend fun delete(subject: Subject)
}

@Dao
interface SchoolClassDao {
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun observeAll(): Flow<List<SchoolClass>>

    @Insert suspend fun insert(schoolClass: SchoolClass)
    @Update suspend fun update(schoolClass: SchoolClass)
    @Delete suspend fun delete(schoolClass: SchoolClass)
}

@Dao
interface LessonReportDao {
    @Query("SELECT * FROM lesson_reports ORDER BY year DESC, month DESC, day DESC")
    fun observeAll(): Flow<List<LessonReport>>

    @Query("SELECT * FROM lesson_reports WHERE id = :id")
    suspend fun getById(id: Long): LessonReport?

    @Insert suspend fun insert(report: LessonReport)
    @Update suspend fun update(report: LessonReport)
    @Delete suspend fun delete(report: LessonReport)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name COLLATE NOCASE")
    fun observeByClass(classId: Long): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(students: List<Student>)

    @Update suspend fun update(student: Student)
    @Delete suspend fun delete(student: Student)
}

/** Resultado do LEFT JOIN: aluno + a nota dele naquela avaliação (null = NA). */
data class AlunoNota(
    val studentId: Long,
    val nome: String,
    val nota: Double?
)

/** Média da turma numa avaliação (media = null quando ninguém foi avaliado). */
data class MediaAvaliacao(
    val assessmentId: Long,
    val classId: Long,
    val titulo: String,
    val bimester: Int,
    val ano: Int,
    val mes: Int,
    val dia: Int,
    val media: Double?,
    val quantos: Int
)

/** Uma nota de um aluno numa prova. Base do desempenho por aluno. */
data class NotaAlunoProva(
    val studentId: Long,
    val studentName: String,
    val assessmentId: Long,
    val assessmentTitle: String,
    val bimester: Int,
    val day: Int,
    val month: Int,
    val year: Int,
    val score: Double
)

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments ORDER BY year DESC, month DESC, day DESC")
    fun observeAll(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE id = :id")
    suspend fun getById(id: Long): Assessment?

    @Insert suspend fun insert(assessment: Assessment): Long
    @Update suspend fun update(assessment: Assessment)
    @Delete suspend fun delete(assessment: Assessment)

    @Query(
        "SELECT s.id AS studentId, s.name AS nome, g.score AS nota " +
                "FROM students s " +
                "LEFT JOIN grades g ON g.studentId = s.id AND g.assessmentId = :assessmentId " +
                "WHERE s.classId = :classId " +
                "ORDER BY s.name COLLATE NOCASE"
    )
    fun observeAlunosNotas(classId: Long, assessmentId: Long): Flow<List<AlunoNota>>

    @Query("DELETE FROM grades WHERE assessmentId = :assessmentId")
    suspend fun apagarNotas(assessmentId: Long)

    @Insert suspend fun inserirNotas(grades: List<Grade>)

    // ============ Base dos gráficos de desempenho da TURMA ============
    @Query(
        "SELECT a.id AS assessmentId, a.classId AS classId, a.title AS titulo, " +
                "a.bimester AS bimester, a.year AS ano, a.month AS mes, a.day AS dia, " +
                "AVG(g.score) AS media, COUNT(g.score) AS quantos " +
                "FROM assessments a " +
                "LEFT JOIN grades g ON g.assessmentId = a.id " +
                "GROUP BY a.id " +
                "ORDER BY a.year, a.month, a.day"
    )
    fun observeMediasAvaliacoes(): Flow<List<MediaAvaliacao>>

    // ============ Base do desempenho por ALUNO ============
    @Query(
        "SELECT s.id AS studentId, s.name AS studentName, a.id AS assessmentId, " +
                "a.title AS assessmentTitle, a.bimester AS bimester, a.day AS day, " +
                "a.month AS month, a.year AS year, g.score AS score " +
                "FROM grades g " +
                "INNER JOIN assessments a ON a.id = g.assessmentId " +
                "INNER JOIN students s ON s.id = g.studentId " +
                "WHERE a.classId = :classId AND g.score IS NOT NULL " +
                "ORDER BY s.name COLLATE NOCASE, a.year, a.month, a.day"
    )
    fun observeNotasDaTurma(classId: Long): Flow<List<NotaAlunoProva>>
}
