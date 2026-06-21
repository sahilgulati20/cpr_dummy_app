package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class FinalReportDto(
    val breath_attempted: Boolean? = false,
    val breath_feedback: String? = "NO_FEEDBACK",
    val breaths_nose_open: Int? = 0,
    val deep_count: Int? = 0,
    val fast_rate_count: Int? = 0,
    val good_depth_count: Int? = 0,
    val good_depth_pct: Double? = 0.0,
    val good_rate_count: Int? = 0,
    val good_rate_pct: Double? = 0.0,
    val overall_status: String? = "FAILED",
    val pass_breaths: Boolean? = false,
    val pass_compressions: Boolean? = false,
    val perfect_breaths: Int? = 0,
    val shallow_count: Int? = 0,
    val slow_rate_count: Int? = 0,
    val termination_reason: String? = "Hands removed / inactive 5s"
)

@JsonClass(generateAdapter = true)
data class CprSessionDto(
    val calibration: Boolean? = false,
    val current_state: String? = "FINISHED",
    val final_report: FinalReportDto? = null,
    val timestamp: Long? = System.currentTimeMillis()
)

interface CprApiService {
    @GET("cpr_sessions.json")
    suspend fun getSessions(): Map<String, CprSessionDto>?

    @POST("cpr_sessions.json")
    suspend fun createSession(@Body session: CprSessionDto): Map<String, String>?

    @PUT("cpr_sessions/{id}.json")
    suspend fun updateSession(@Path("id") id: String, @Body session: CprSessionDto): CprSessionDto?

    companion object {
        private const val BASE_URL = "https://alert-a5fd7-default-rtdb.asia-southeast1.firebasedatabase.app/"

        fun create(): CprApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(CprApiService::class.java)
        }
    }
}
