package com.typingtest.korean.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

enum class TypingMode(val label: String) {
    POSITION("자리 연습"),
    WORD("낱말 연습"),
    SHORT_TEXT("짧은 글"),
    LONG_TEXT("긴 글"),
    ACID_RAIN("산성비 게임")
}

data class TypingRecord(
    val id: Long = 0L,
    val mode: TypingMode,
    val detailTitle: String,
    val avgCpm: Int,
    val maxCpm: Int,
    val accuracy: Float,
    val score: Int,
    val durationSec: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ModeSummary(
    val mode: TypingMode,
    val sessionCount: Int,
    val avgCpm: Int,
    val bestCpm: Int,
    val avgAccuracy: Float,
    val bestScore: Int
)

data class DashboardSummary(
    val totalSessions: Int = 0,
    val totalDurationSec: Int = 0,
    val overallAvgCpm: Int = 0,
    val overallBestCpm: Int = 0,
    val overallAvgAccuracy: Float = 0f,
    val bestAcidRainScore: Int = 0,
    val modeSummaries: Map<TypingMode, ModeSummary> = emptyMap(),
    val recentRecords: List<TypingRecord> = emptyList()
)

class TypingRecordStore private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    private val _summaryFlow = MutableStateFlow(DashboardSummary())
    val summaryFlow: StateFlow<DashboardSummary> = _summaryFlow.asStateFlow()

    init {
        refreshSummary()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_RECORDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                mode TEXT NOT NULL,
                detail_title TEXT NOT NULL,
                avg_cpm INTEGER NOT NULL,
                max_cpm INTEGER NOT NULL,
                accuracy REAL NOT NULL,
                score INTEGER NOT NULL,
                duration_sec INTEGER NOT NULL,
                timestamp_millis INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun insertRecord(
        mode: TypingMode,
        detailTitle: String,
        avgCpm: Int,
        maxCpm: Int,
        accuracy: Float,
        score: Int = 0,
        durationSec: Int
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("mode", mode.name)
            put("detail_title", detailTitle)
            put("avg_cpm", avgCpm)
            put("max_cpm", maxCpm)
            put("accuracy", accuracy)
            put("score", score)
            put("duration_sec", durationSec)
            put("timestamp_millis", System.currentTimeMillis())
        }
        db.insert(TABLE_RECORDS, null, values)
        refreshSummary()
    }

    fun clearAllRecords() {
        writableDatabase.delete(TABLE_RECORDS, null, null)
        refreshSummary()
    }

    fun refreshSummary() {
        val records = getAllRecordsDesc()
        if (records.isEmpty()) {
            _summaryFlow.value = DashboardSummary()
            return
        }

        val typingOnly = records.filter { it.mode != TypingMode.ACID_RAIN && it.avgCpm > 0 }
        val overallAvgCpm = if (typingOnly.isNotEmpty()) {
            typingOnly.map { it.avgCpm }.average().roundToInt()
        } else {
            records.map { it.avgCpm }.average().roundToInt()
        }
        val overallBestCpm = records.maxOfOrNull { maxOf(it.avgCpm, it.maxCpm) } ?: 0
        val overallAvgAcc = records.map { it.accuracy }.average().toFloat()
        val totalDuration = records.sumOf { it.durationSec }
        val bestGameScore = records.filter { it.mode == TypingMode.ACID_RAIN }.maxOfOrNull { it.score } ?: 0

        val modeMap = mutableMapOf<TypingMode, ModeSummary>()
        for (mode in TypingMode.entries) {
            val modeRecords = records.filter { it.mode == mode }
            if (modeRecords.isNotEmpty()) {
                modeMap[mode] = ModeSummary(
                    mode = mode,
                    sessionCount = modeRecords.size,
                    avgCpm = modeRecords.map { it.avgCpm }.average().roundToInt(),
                    bestCpm = modeRecords.maxOf { maxOf(it.avgCpm, it.maxCpm) },
                    avgAccuracy = modeRecords.map { it.accuracy }.average().toFloat(),
                    bestScore = modeRecords.maxOf { it.score }
                )
            }
        }

        _summaryFlow.value = DashboardSummary(
            totalSessions = records.size,
            totalDurationSec = totalDuration,
            overallAvgCpm = overallAvgCpm,
            overallBestCpm = overallBestCpm,
            overallAvgAccuracy = overallAvgAcc,
            bestAcidRainScore = bestGameScore,
            modeSummaries = modeMap,
            recentRecords = records.take(50)
        )
    }

    private fun getAllRecordsDesc(): List<TypingRecord> {
        val list = mutableListOf<TypingRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECORDS,
            null,
            null,
            null,
            null,
            null,
            "timestamp_millis DESC",
            "200"
        )
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val modeIdx = c.getColumnIndexOrThrow("mode")
            val titleIdx = c.getColumnIndexOrThrow("detail_title")
            val avgCpmIdx = c.getColumnIndexOrThrow("avg_cpm")
            val maxCpmIdx = c.getColumnIndexOrThrow("max_cpm")
            val accIdx = c.getColumnIndexOrThrow("accuracy")
            val scoreIdx = c.getColumnIndexOrThrow("score")
            val durIdx = c.getColumnIndexOrThrow("duration_sec")
            val timeIdx = c.getColumnIndexOrThrow("timestamp_millis")

            while (c.moveToNext()) {
                val modeStr = c.getString(modeIdx)
                val mode = runCatching { TypingMode.valueOf(modeStr) }.getOrDefault(TypingMode.WORD)
                list.add(
                    TypingRecord(
                        id = c.getLong(idIdx),
                        mode = mode,
                        detailTitle = c.getString(titleIdx),
                        avgCpm = c.getInt(avgCpmIdx),
                        maxCpm = c.getInt(maxCpmIdx),
                        accuracy = c.getFloat(accIdx),
                        score = c.getInt(scoreIdx),
                        durationSec = c.getInt(durIdx),
                        timestampMillis = c.getLong(timeIdx)
                    )
                )
            }
        }
        return list
    }

    companion object {
        private const val DB_NAME = "korean_typing_master.db"
        private const val DB_VERSION = 1
        private const val TABLE_RECORDS = "typing_records"

        @Volatile
        private var INSTANCE: TypingRecordStore? = null

        fun getInstance(context: Context): TypingRecordStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TypingRecordStore(context).also { INSTANCE = it }
            }
        }
    }
}
