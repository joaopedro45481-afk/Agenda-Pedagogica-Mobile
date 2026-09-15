package com.example.agendaparaprofessores.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.agendaparaprofessores.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val subjectDao = db.subjectDao()
    private val classDao = db.schoolClassDao()
    private val reportDao = db.lessonReportDao()
    private val studentDao = db.studentDao()
    private val assessmentDao = db.assessmentDao()

    val subjects: StateFlow<List<Subject>> = subjectDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<SchoolClass>> = classDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<LessonReport>> = reportDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assessments: StateFlow<List<Assessment>> = assessmentDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ============ Base dos gráficos de desempenho da TURMA ============
    val mediasAvaliacoes: StateFlow<List<MediaAvaliacao>> = assessmentDao.observeMediasAvaliacoes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== MATÉRIAS =====

    fun addSubject(name: String) = viewModelScope.launch { subjectDao.insert(Subject(name = name)) }
    fun updateSubject(s: Subject) = viewModelScope.launch { subjectDao.update(s) }
    fun deleteSubject(s: Subject) = viewModelScope.launch { subjectDao.delete(s) }

    // ===== TURMAS =====

    fun addClass(name: String, grade: String?) =
        viewModelScope.launch { classDao.insert(SchoolClass(name = name, grade = grade)) }
    fun updateClass(c: SchoolClass) = viewModelScope.launch { classDao.update(c) }
    fun deleteClass(c: SchoolClass) = viewModelScope.launch { classDao.delete(c) }

    // ===== RELATÓRIOS =====

    fun saveReport(r: LessonReport) = viewModelScope.launch {
        if (r.id == 0L) reportDao.insert(r) else reportDao.update(r)
    }
    fun deleteReport(r: LessonReport) = viewModelScope.launch { reportDao.delete(r) }

    suspend fun getReport(id: Long): LessonReport? = reportDao.getById(id)

    // ===== ALUNOS =====

    fun alunosDaTurma(classId: Long): Flow<List<Student>> =
        studentDao.observeByClass(classId)

    fun addAluno(classId: Long, nome: String) = viewModelScope.launch {
        val n = nome.trim()
        if (n.isNotBlank()) studentDao.insert(Student(classId = classId, name = n))
    }

    fun addAlunos(classId: Long, nomes: List<String>) = viewModelScope.launch {
        val lista = nomes
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .map { Student(classId = classId, name = it) }
        if (lista.isNotEmpty()) studentDao.insertAll(lista)
    }

    fun updateAluno(student: Student) = viewModelScope.launch { studentDao.update(student) }

    fun deleteAluno(student: Student) = viewModelScope.launch { studentDao.delete(student) }

    // ===== AVALIAÇÕES =====

    fun notasDaAvaliacao(classId: Long, assessmentId: Long): Flow<List<AlunoNota>> =
        assessmentDao.observeAlunosNotas(classId, assessmentId)

    suspend fun getAssessment(id: Long): Assessment? = assessmentDao.getById(id)

    /** Notas de todos os alunos da turma, prova por prova (desempenho por aluno). */
    fun notasDaTurma(classId: Long): Flow<List<NotaAlunoProva>> =
        assessmentDao.observeNotasDaTurma(classId)

    /** Salva a avaliação e regrava TODAS as notas numa transação (null = NA). */
    suspend fun salvarAvaliacaoComNotas(a: Assessment, notas: Map<Long, Double?>): Long =
        db.withTransaction {
            val id = if (a.id == 0L) {
                assessmentDao.insert(a)
            } else {
                assessmentDao.update(a)
                a.id
            }
            assessmentDao.apagarNotas(id)
            val linhas = notas.map { (alunoId, nota) ->
                Grade(assessmentId = id, studentId = alunoId, score = nota)
            }
            if (linhas.isNotEmpty()) assessmentDao.inserirNotas(linhas)
            id
        }

    fun deleteAvaliacao(a: Assessment) = viewModelScope.launch { assessmentDao.delete(a) }
}
