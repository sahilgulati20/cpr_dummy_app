package com.example.db

import androidx.room.*
import com.example.model.CprSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CprDao {
    @Query("SELECT * FROM cpr_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<CprSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CprSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<CprSession>)

    @Query("DELETE FROM cpr_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM cpr_sessions")
    suspend fun clearAll()
}
