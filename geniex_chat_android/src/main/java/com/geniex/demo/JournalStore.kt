package com.geniex.demo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class JournalStore(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE journal_entries (
                id TEXT PRIMARY KEY,
                text TEXT NOT NULL,
                category TEXT NOT NULL,
                sentiment TEXT NOT NULL,
                mode TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_journal_created_at ON journal_entries(created_at)")
        db.execSQL("CREATE INDEX idx_journal_category ON journal_entries(category)")
        db.execSQL("CREATE INDEX idx_journal_sentiment ON journal_entries(sentiment)")
        db.execSQL("CREATE INDEX idx_journal_mode ON journal_entries(mode)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS journal_entries")
        onCreate(db)
    }

    fun addEntry(text: String, tags: JournalTags): JournalEntry {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "journal text is empty" }
        val entry = JournalEntry(
            id = UUID.randomUUID().toString(),
            text = trimmed,
            category = tags.category.normalizedOr("other"),
            sentiment = tags.sentiment.normalizedOr("neutral"),
            mode = tags.mode.normalizedOr("reflective"),
            createdAt = System.currentTimeMillis(),
        )
        writableDatabase.insertOrThrow(TABLE, null, entry.toValues())
        return entry
    }

    fun entries(category: String? = null, date: String? = null, limit: Int = 30): List<JournalEntry> {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (!category.isNullOrBlank() && category != "all") {
            where += "category = ?"
            args += category.trim().lowercase()
        }
        if (!date.isNullOrBlank()) {
            val range = dayRange(date.trim())
            where += "created_at >= ? AND created_at < ?"
            args += range.first.toString()
            args += range.second.toString()
        }
        val cursor = readableDatabase.query(
            TABLE,
            COLUMNS,
            where.takeIf { it.isNotEmpty() }?.joinToString(" AND "),
            args.takeIf { it.isNotEmpty() }?.toTypedArray(),
            null,
            null,
            "created_at DESC",
            limit.coerceIn(1, 200).toString(),
        )
        cursor.use {
            val result = mutableListOf<JournalEntry>()
            while (it.moveToNext()) result += it.toJournalEntry()
            return result
        }
    }

    fun dashboardText(category: String? = null, date: String? = null): String {
        val entries = entries(category, date, 8)
        if (entries.isEmpty()) return "No journal entries yet. Dump a thought, then save."
        return buildString {
            appendLine("Journal entries: ${countEntries(category, date)}")
            entries.forEachIndexed { index, entry ->
                appendLine("${index + 1}. ${entry.displayDate()} • ${entry.category}/${entry.sentiment}/${entry.mode}")
                appendLine(entry.text.take(140))
            }
        }.trim()
    }

    fun entriesJson(category: String? = null, date: String? = null): JsonObject = buildJsonObject {
        put("count", countEntries(category, date))
        put("entries", JsonArray(entries(category, date, 50).map { it.toJson() }))
    }

    fun weeklySummaryText(): String = summaryText(days = 7, label = "Weekly")

    fun weeklySummaryJson(): JsonObject = buildJsonObject {
        put("period", "week")
        put("summary", weeklySummaryText())
        put("entry_count", countSince(days = 7))
        put("categories", countsJson("category", days = 7))
        put("sentiments", countsJson("sentiment", days = 7))
        put("modes", countsJson("mode", days = 7))
    }

    private fun summaryText(days: Long, label: String): String {
        val total = countSince(days)
        return buildString {
            appendLine("$label journal summary")
            appendLine("Entries: $total")
            appendLine("Categories:")
            appendCounts(counts("category", days))
            appendLine("Sentiment:")
            appendCounts(counts("sentiment", days))
            appendLine("Mode:")
            appendCounts(counts("mode", days))
        }.trim()
    }

    private fun StringBuilder.appendCounts(counts: Map<String, Int>) {
        if (counts.isEmpty()) {
            appendLine("- none: 0")
            return
        }
        counts.forEach { (key, count) -> appendLine("- $key: $count") }
    }

    private fun countEntries(category: String? = null, date: String? = null): Int {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (!category.isNullOrBlank() && category != "all") {
            where += "category = ?"
            args += category.trim().lowercase()
        }
        if (!date.isNullOrBlank()) {
            val range = dayRange(date.trim())
            where += "created_at >= ? AND created_at < ?"
            args += range.first.toString()
            args += range.second.toString()
        }
        return countWhere(where.takeIf { it.isNotEmpty() }?.joinToString(" AND "), args.toTypedArray())
    }

    private fun countSince(days: Long): Int = countWhere("created_at >= ?", arrayOf(sinceMillis(days).toString()))

    private fun countWhere(where: String?, args: Array<String>): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE${if (where != null) " WHERE $where" else ""}", args).use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    private fun counts(column: String, days: Long): Map<String, Int> {
        readableDatabase.rawQuery(
            "SELECT $column, COUNT(*) FROM $TABLE WHERE created_at >= ? GROUP BY $column ORDER BY COUNT(*) DESC, $column ASC",
            arrayOf(sinceMillis(days).toString()),
        ).use { cursor ->
            val result = linkedMapOf<String, Int>()
            while (cursor.moveToNext()) result[cursor.getString(0)] = cursor.getInt(1)
            return result
        }
    }

    private fun countsJson(column: String, days: Long): JsonObject = buildJsonObject {
        counts(column, days).forEach { (key, count) -> put(key, count) }
    }

    private fun sinceMillis(days: Long): Long = Instant.now().minusSeconds(days * 24 * 60 * 60).toEpochMilli()

    private fun dayRange(date: String): Pair<Long, Long> {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val zone = ZoneId.systemDefault()
        val start = localDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = localDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    private fun JournalEntry.toValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("text", text)
        put("category", category)
        put("sentiment", sentiment)
        put("mode", mode)
        put("created_at", createdAt)
    }

    private fun android.database.Cursor.toJournalEntry(): JournalEntry = JournalEntry(
        id = getString(0),
        text = getString(1),
        category = getString(2),
        sentiment = getString(3),
        mode = getString(4),
        createdAt = getLong(5),
    )

    private fun String.normalizedOr(fallback: String): String = lowercase().trim().ifEmpty { fallback }

    companion object {
        private const val DB_NAME = "journal.db"
        private const val DB_VERSION = 1
        private const val TABLE = "journal_entries"
        private val COLUMNS = arrayOf("id", "text", "category", "sentiment", "mode", "created_at")
    }
}

data class JournalTags(val category: String, val sentiment: String, val mode: String) {
    fun toJson(): JsonObject = buildJsonObject {
        put("category", category)
        put("sentiment", sentiment)
        put("mode", mode)
    }
}

data class JournalEntry(
    val id: String,
    val text: String,
    val category: String,
    val sentiment: String,
    val mode: String,
    val createdAt: Long,
) {
    fun displayDate(): String = Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    fun toJson(): JsonObject = buildJsonObject {
        put("id", id)
        put("text", text)
        put("category", category)
        put("sentiment", sentiment)
        put("mode", mode)
        put("created_at", createdAt)
        put("display_date", displayDate())
    }
}
