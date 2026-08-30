package com.geniex.assistant.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.geniex.assistant.model.TaskStatus;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaskEntity> __insertionAdapterOfTaskEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<TaskEntity> __updateAdapterOfTaskEntity;

  public TaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaskEntity = new EntityInsertionAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `tasks` (`id`,`goalId`,`title`,`details`,`status`,`priority`,`owner`,`deadlineEpochDay`,`dependencyTaskId`,`blockedReason`,`estimatedMinutes`,`createdAtEpochMs`,`updatedAtEpochMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getGoalId());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDetails());
        final String _tmp = __converters.taskStatusToString(entity.getStatus());
        statement.bindString(5, _tmp);
        statement.bindLong(6, entity.getPriority());
        statement.bindString(7, entity.getOwner());
        if (entity.getDeadlineEpochDay() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeadlineEpochDay());
        }
        if (entity.getDependencyTaskId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getDependencyTaskId());
        }
        if (entity.getBlockedReason() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBlockedReason());
        }
        statement.bindLong(11, entity.getEstimatedMinutes());
        statement.bindLong(12, entity.getCreatedAtEpochMs());
        statement.bindLong(13, entity.getUpdatedAtEpochMs());
      }
    };
    this.__updateAdapterOfTaskEntity = new EntityDeletionOrUpdateAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `tasks` SET `id` = ?,`goalId` = ?,`title` = ?,`details` = ?,`status` = ?,`priority` = ?,`owner` = ?,`deadlineEpochDay` = ?,`dependencyTaskId` = ?,`blockedReason` = ?,`estimatedMinutes` = ?,`createdAtEpochMs` = ?,`updatedAtEpochMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getGoalId());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDetails());
        final String _tmp = __converters.taskStatusToString(entity.getStatus());
        statement.bindString(5, _tmp);
        statement.bindLong(6, entity.getPriority());
        statement.bindString(7, entity.getOwner());
        if (entity.getDeadlineEpochDay() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeadlineEpochDay());
        }
        if (entity.getDependencyTaskId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getDependencyTaskId());
        }
        if (entity.getBlockedReason() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBlockedReason());
        }
        statement.bindLong(11, entity.getEstimatedMinutes());
        statement.bindLong(12, entity.getCreatedAtEpochMs());
        statement.bindLong(13, entity.getUpdatedAtEpochMs());
        statement.bindLong(14, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final TaskEntity task, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaskEntity.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<TaskEntity> tasks,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfTaskEntity.insertAndReturnIdsList(tasks);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TaskEntity task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaskEntity.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaskEntity>> observeTasks() {
    final String _sql = "SELECT * FROM tasks ORDER BY priority DESC, COALESCE(deadlineEpochDay, 99999999) ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfOwner = CursorUtil.getColumnIndexOrThrow(_cursor, "owner");
          final int _cursorIndexOfDeadlineEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "deadlineEpochDay");
          final int _cursorIndexOfDependencyTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "dependencyTaskId");
          final int _cursorIndexOfBlockedReason = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedReason");
          final int _cursorIndexOfEstimatedMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedMinutes");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMs");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpGoalId;
            _tmpGoalId = _cursor.getLong(_cursorIndexOfGoalId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final TaskStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.taskStatusFromString(_tmp);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final String _tmpOwner;
            _tmpOwner = _cursor.getString(_cursorIndexOfOwner);
            final Long _tmpDeadlineEpochDay;
            if (_cursor.isNull(_cursorIndexOfDeadlineEpochDay)) {
              _tmpDeadlineEpochDay = null;
            } else {
              _tmpDeadlineEpochDay = _cursor.getLong(_cursorIndexOfDeadlineEpochDay);
            }
            final Long _tmpDependencyTaskId;
            if (_cursor.isNull(_cursorIndexOfDependencyTaskId)) {
              _tmpDependencyTaskId = null;
            } else {
              _tmpDependencyTaskId = _cursor.getLong(_cursorIndexOfDependencyTaskId);
            }
            final String _tmpBlockedReason;
            if (_cursor.isNull(_cursorIndexOfBlockedReason)) {
              _tmpBlockedReason = null;
            } else {
              _tmpBlockedReason = _cursor.getString(_cursorIndexOfBlockedReason);
            }
            final int _tmpEstimatedMinutes;
            _tmpEstimatedMinutes = _cursor.getInt(_cursorIndexOfEstimatedMinutes);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _item = new TaskEntity(_tmpId,_tmpGoalId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpOwner,_tmpDeadlineEpochDay,_tmpDependencyTaskId,_tmpBlockedReason,_tmpEstimatedMinutes,_tmpCreatedAtEpochMs,_tmpUpdatedAtEpochMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTasksByGoal(final long goalId,
      final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE goalId = ? ORDER BY priority DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, goalId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfOwner = CursorUtil.getColumnIndexOrThrow(_cursor, "owner");
          final int _cursorIndexOfDeadlineEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "deadlineEpochDay");
          final int _cursorIndexOfDependencyTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "dependencyTaskId");
          final int _cursorIndexOfBlockedReason = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedReason");
          final int _cursorIndexOfEstimatedMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedMinutes");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMs");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpGoalId;
            _tmpGoalId = _cursor.getLong(_cursorIndexOfGoalId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final TaskStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.taskStatusFromString(_tmp);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final String _tmpOwner;
            _tmpOwner = _cursor.getString(_cursorIndexOfOwner);
            final Long _tmpDeadlineEpochDay;
            if (_cursor.isNull(_cursorIndexOfDeadlineEpochDay)) {
              _tmpDeadlineEpochDay = null;
            } else {
              _tmpDeadlineEpochDay = _cursor.getLong(_cursorIndexOfDeadlineEpochDay);
            }
            final Long _tmpDependencyTaskId;
            if (_cursor.isNull(_cursorIndexOfDependencyTaskId)) {
              _tmpDependencyTaskId = null;
            } else {
              _tmpDependencyTaskId = _cursor.getLong(_cursorIndexOfDependencyTaskId);
            }
            final String _tmpBlockedReason;
            if (_cursor.isNull(_cursorIndexOfBlockedReason)) {
              _tmpBlockedReason = null;
            } else {
              _tmpBlockedReason = _cursor.getString(_cursorIndexOfBlockedReason);
            }
            final int _tmpEstimatedMinutes;
            _tmpEstimatedMinutes = _cursor.getInt(_cursorIndexOfEstimatedMinutes);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _item = new TaskEntity(_tmpId,_tmpGoalId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpOwner,_tmpDeadlineEpochDay,_tmpDependencyTaskId,_tmpBlockedReason,_tmpEstimatedMinutes,_tmpCreatedAtEpochMs,_tmpUpdatedAtEpochMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTask(final long taskId, final Continuation<? super TaskEntity> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaskEntity>() {
      @Override
      @Nullable
      public TaskEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfOwner = CursorUtil.getColumnIndexOrThrow(_cursor, "owner");
          final int _cursorIndexOfDeadlineEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "deadlineEpochDay");
          final int _cursorIndexOfDependencyTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "dependencyTaskId");
          final int _cursorIndexOfBlockedReason = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedReason");
          final int _cursorIndexOfEstimatedMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedMinutes");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMs");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final TaskEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpGoalId;
            _tmpGoalId = _cursor.getLong(_cursorIndexOfGoalId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final TaskStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.taskStatusFromString(_tmp);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final String _tmpOwner;
            _tmpOwner = _cursor.getString(_cursorIndexOfOwner);
            final Long _tmpDeadlineEpochDay;
            if (_cursor.isNull(_cursorIndexOfDeadlineEpochDay)) {
              _tmpDeadlineEpochDay = null;
            } else {
              _tmpDeadlineEpochDay = _cursor.getLong(_cursorIndexOfDeadlineEpochDay);
            }
            final Long _tmpDependencyTaskId;
            if (_cursor.isNull(_cursorIndexOfDependencyTaskId)) {
              _tmpDependencyTaskId = null;
            } else {
              _tmpDependencyTaskId = _cursor.getLong(_cursorIndexOfDependencyTaskId);
            }
            final String _tmpBlockedReason;
            if (_cursor.isNull(_cursorIndexOfBlockedReason)) {
              _tmpBlockedReason = null;
            } else {
              _tmpBlockedReason = _cursor.getString(_cursorIndexOfBlockedReason);
            }
            final int _tmpEstimatedMinutes;
            _tmpEstimatedMinutes = _cursor.getInt(_cursorIndexOfEstimatedMinutes);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new TaskEntity(_tmpId,_tmpGoalId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpOwner,_tmpDeadlineEpochDay,_tmpDependencyTaskId,_tmpBlockedReason,_tmpEstimatedMinutes,_tmpCreatedAtEpochMs,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object openTaskExists(final long goalId, final String title,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM tasks WHERE goalId = ? AND LOWER(title) = LOWER(?) AND status != 'COMPLETED')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, goalId);
    _argIndex = 2;
    _statement.bindString(_argIndex, title);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getOpenTasks(final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY priority DESC, COALESCE(deadlineEpochDay, 99999999) ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfOwner = CursorUtil.getColumnIndexOrThrow(_cursor, "owner");
          final int _cursorIndexOfDeadlineEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "deadlineEpochDay");
          final int _cursorIndexOfDependencyTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "dependencyTaskId");
          final int _cursorIndexOfBlockedReason = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedReason");
          final int _cursorIndexOfEstimatedMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedMinutes");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMs");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpGoalId;
            _tmpGoalId = _cursor.getLong(_cursorIndexOfGoalId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final TaskStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.taskStatusFromString(_tmp);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final String _tmpOwner;
            _tmpOwner = _cursor.getString(_cursorIndexOfOwner);
            final Long _tmpDeadlineEpochDay;
            if (_cursor.isNull(_cursorIndexOfDeadlineEpochDay)) {
              _tmpDeadlineEpochDay = null;
            } else {
              _tmpDeadlineEpochDay = _cursor.getLong(_cursorIndexOfDeadlineEpochDay);
            }
            final Long _tmpDependencyTaskId;
            if (_cursor.isNull(_cursorIndexOfDependencyTaskId)) {
              _tmpDependencyTaskId = null;
            } else {
              _tmpDependencyTaskId = _cursor.getLong(_cursorIndexOfDependencyTaskId);
            }
            final String _tmpBlockedReason;
            if (_cursor.isNull(_cursorIndexOfBlockedReason)) {
              _tmpBlockedReason = null;
            } else {
              _tmpBlockedReason = _cursor.getString(_cursorIndexOfBlockedReason);
            }
            final int _tmpEstimatedMinutes;
            _tmpEstimatedMinutes = _cursor.getInt(_cursorIndexOfEstimatedMinutes);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _item = new TaskEntity(_tmpId,_tmpGoalId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpOwner,_tmpDeadlineEpochDay,_tmpDependencyTaskId,_tmpBlockedReason,_tmpEstimatedMinutes,_tmpCreatedAtEpochMs,_tmpUpdatedAtEpochMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object countTasks(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM tasks";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
