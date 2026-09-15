package com.example.agendaparaprofessores.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendaparaprofessores.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getDatabase(app)
    private val subjectDao = db.subjectDao()
    private val classDao = db.schoolClassDao()
    private val reportDao = db.lessonReportDao()

    val subjects: StateFlow<List<Subject>> = subjectDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<SchoolClass>> = classDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<LessonReport>> = reportDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String) = viewModelScope.launch { subjectDao.insert(Subject(name = name)) }
    fun updateSubject(s: Subject) = viewModelScope.launch { subjectDao.update(s) }
    fun deleteSubject(s: Subject) = viewModelScope.launch { subjectDao.delete(s) }

    fun addClass(name: String, grade: String?) =
        viewModelScope.launch { classDao.insert(SchoolClass(name = name, grade = grade)) }
    fun updateClass(c: SchoolClass) = viewModelScope.launch { classDao.update(c) }
    fun deleteClass(c: SchoolClass) = viewModelScope.launch { classDao.delete(c) }

    fun saveReport(r: LessonReport) = viewModelScope.launch {
        if (r.id == 0L) reportDao.insert(r) else reportDao.update(r)
    }
    fun deleteReport(r: LessonReport) = viewModelScope.launch { reportDao.delete(r) }

    suspend fun getReport(id: Long): LessonReport? = reportDao.getById(id)
}
