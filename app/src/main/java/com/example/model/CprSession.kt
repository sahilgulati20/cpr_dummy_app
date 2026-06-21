package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cpr_sessions")
data class CprSession(
    @PrimaryKey val id: String, // Firebase key or local generated UUID/timestamp
    val timestamp: Long,
    val currentState: String,
    val breathAttempted: Boolean,
    val breathFeedback: String,
    val breathsNoseOpen: Int,
    val deepCount: Int,
    val fastRateCount: Int,
    val goodDepthCount: Int,
    val goodDepthPct: Double,
    val goodRateCount: Int,
    val goodRatePct: Double,
    val overallStatus: String, // "PASSED" or "FAILED"
    val passBreaths: Boolean,
    val passCompressions: Boolean,
    val perfectBreaths: Int,
    val shallowCount: Int,
    val slowRateCount: Int,
    val terminationReason: String
) {
    // Utility for percentage calculation
    val compressionAccuracy: Double
        get() {
            val total = goodDepthCount + shallowCount + deepCount
            return if (total > 0) (goodDepthCount.toDouble() / total) * 100.0 else 0.0
        }

    val rateAccuracy: Double
        get() = goodRatePct

    val totalCompressions: Int
        get() = goodDepthCount + shallowCount + deepCount
}
