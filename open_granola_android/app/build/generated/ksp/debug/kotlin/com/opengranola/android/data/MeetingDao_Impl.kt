package com.opengranola.android.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MeetingDao_Impl(
  __db: RoomDatabase,
) : MeetingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMeetingEntity: EntityInsertAdapter<MeetingEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMeetingEntity = object : EntityInsertAdapter<MeetingEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `meetings` (`id`,`title`,`startedAt`,`transcript`,`notes`,`recordingPath`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MeetingEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindLong(3, entity.startedAt)
        statement.bindText(4, entity.transcript)
        statement.bindText(5, entity.notes)
        val _tmpRecordingPath: String? = entity.recordingPath
        if (_tmpRecordingPath == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpRecordingPath)
        }
      }
    }
  }

  public override suspend fun save(meeting: MeetingEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfMeetingEntity.insertAndReturnId(_connection, meeting)
    _result
  }

  public override fun observeAll(): Flow<List<MeetingEntity>> {
    val _sql: String = "SELECT * FROM meetings ORDER BY startedAt DESC"
    return createFlow(__db, false, arrayOf("meetings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfTranscript: Int = getColumnIndexOrThrow(_stmt, "transcript")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordingPath: Int = getColumnIndexOrThrow(_stmt, "recordingPath")
        val _result: MutableList<MeetingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MeetingEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpTranscript: String
          _tmpTranscript = _stmt.getText(_columnIndexOfTranscript)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpRecordingPath: String?
          if (_stmt.isNull(_columnIndexOfRecordingPath)) {
            _tmpRecordingPath = null
          } else {
            _tmpRecordingPath = _stmt.getText(_columnIndexOfRecordingPath)
          }
          _item =
              MeetingEntity(_tmpId,_tmpTitle,_tmpStartedAt,_tmpTranscript,_tmpNotes,_tmpRecordingPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recent(limit: Int): List<MeetingEntity> {
    val _sql: String = "SELECT * FROM meetings ORDER BY startedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfTranscript: Int = getColumnIndexOrThrow(_stmt, "transcript")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfRecordingPath: Int = getColumnIndexOrThrow(_stmt, "recordingPath")
        val _result: MutableList<MeetingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MeetingEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpTranscript: String
          _tmpTranscript = _stmt.getText(_columnIndexOfTranscript)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpRecordingPath: String?
          if (_stmt.isNull(_columnIndexOfRecordingPath)) {
            _tmpRecordingPath = null
          } else {
            _tmpRecordingPath = _stmt.getText(_columnIndexOfRecordingPath)
          }
          _item =
              MeetingEntity(_tmpId,_tmpTitle,_tmpStartedAt,_tmpTranscript,_tmpNotes,_tmpRecordingPath)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM meetings WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemo() {
    val _sql: String = "DELETE FROM meetings WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearSummary(id: String) {
    val _sql: String = "UPDATE meetings SET notes = '' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
