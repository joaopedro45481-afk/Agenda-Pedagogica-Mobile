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
