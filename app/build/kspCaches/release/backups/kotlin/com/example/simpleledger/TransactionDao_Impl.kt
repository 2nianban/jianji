package com.example.simpleledger

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class TransactionDao_Impl(
  __db: RoomDatabase,
) : TransactionDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfLedgerTransaction: EntityInsertionAdapter<LedgerTransaction>

  private val __updateAdapterOfLedgerTransaction: EntityDeletionOrUpdateAdapter<LedgerTransaction>

  private val __preparedStmtOfSoftDeleteById: SharedSQLiteStatement

  private val __preparedStmtOfRestoreById: SharedSQLiteStatement

  private val __preparedStmtOfPurgeDeletedBefore: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfLedgerTransaction = object :
        EntityInsertionAdapter<LedgerTransaction>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `transactions` (`id`,`type`,`amountCents`,`category`,`account`,`targetAccount`,`note`,`timestamp`,`deletedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: LedgerTransaction) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.type)
        statement.bindLong(3, entity.amountCents)
        statement.bindString(4, entity.category)
        statement.bindString(5, entity.account)
        statement.bindString(6, entity.targetAccount)
        statement.bindString(7, entity.note)
        statement.bindLong(8, entity.timestamp)
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpDeletedAt)
        }
      }
    }
    this.__updateAdapterOfLedgerTransaction = object :
        EntityDeletionOrUpdateAdapter<LedgerTransaction>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `transactions` SET `id` = ?,`type` = ?,`amountCents` = ?,`category` = ?,`account` = ?,`targetAccount` = ?,`note` = ?,`timestamp` = ?,`deletedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: LedgerTransaction) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.type)
        statement.bindLong(3, entity.amountCents)
        statement.bindString(4, entity.category)
        statement.bindString(5, entity.account)
        statement.bindString(6, entity.targetAccount)
        statement.bindString(7, entity.note)
        statement.bindLong(8, entity.timestamp)
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpDeletedAt)
        }
        statement.bindLong(10, entity.id)
      }
    }
    this.__preparedStmtOfSoftDeleteById = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE transactions SET deletedAt = ? WHERE id = ? AND deletedAt IS NULL"
        return _query
      }
    }
    this.__preparedStmtOfRestoreById = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE transactions SET deletedAt = NULL WHERE id = ? AND deletedAt IS NOT NULL AND deletedAt > ?"
        return _query
      }
    }
    this.__preparedStmtOfPurgeDeletedBefore = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "DELETE FROM transactions WHERE deletedAt IS NOT NULL AND deletedAt <= ?"
        return _query
      }
    }
  }

  public override suspend fun insert(transaction: LedgerTransaction): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfLedgerTransaction.insert(transaction)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun insertAll(transactions: List<LedgerTransaction>): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfLedgerTransaction.insert(transactions)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(transaction: LedgerTransaction): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfLedgerTransaction.handle(transaction)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun softDeleteById(id: Long, deletedAt: Long): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfSoftDeleteById.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, deletedAt)
      _argIndex = 2
      _stmt.bindLong(_argIndex, id)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfSoftDeleteById.release(_stmt)
      }
    }
  })

  public override suspend fun restoreById(id: Long, cutoff: Long): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfRestoreById.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, id)
      _argIndex = 2
      _stmt.bindLong(_argIndex, cutoff)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfRestoreById.release(_stmt)
      }
    }
  })

  public override suspend fun purgeDeletedBefore(cutoff: Long): Int = CoroutinesRoom.execute(__db,
      true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfPurgeDeletedBefore.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, cutoff)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfPurgeDeletedBefore.release(_stmt)
      }
    }
  })

  public override fun observeAll(): Flow<List<LedgerTransaction>> {
    val _sql: String =
        "SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC, id DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<LedgerTransaction>> {
      public override fun call(): List<LedgerTransaction> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfAmountCents: Int = getColumnIndexOrThrow(_cursor, "amountCents")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfAccount: Int = getColumnIndexOrThrow(_cursor, "account")
          val _cursorIndexOfTargetAccount: Int = getColumnIndexOrThrow(_cursor, "targetAccount")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<LedgerTransaction> =
              ArrayList<LedgerTransaction>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: LedgerTransaction
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpType: String
            _tmpType = _cursor.getString(_cursorIndexOfType)
            val _tmpAmountCents: Long
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents)
            val _tmpCategory: String
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            val _tmpAccount: String
            _tmpAccount = _cursor.getString(_cursorIndexOfAccount)
            val _tmpTargetAccount: String
            _tmpTargetAccount = _cursor.getString(_cursorIndexOfTargetAccount)
            val _tmpNote: String
            _tmpNote = _cursor.getString(_cursorIndexOfNote)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                LedgerTransaction(_tmpId,_tmpType,_tmpAmountCents,_tmpCategory,_tmpAccount,_tmpTargetAccount,_tmpNote,_tmpTimestamp,_tmpDeletedAt)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override fun observeDeleted(): Flow<List<LedgerTransaction>> {
    val _sql: String =
        "SELECT * FROM transactions WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC, id DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<LedgerTransaction>> {
      public override fun call(): List<LedgerTransaction> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfAmountCents: Int = getColumnIndexOrThrow(_cursor, "amountCents")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfAccount: Int = getColumnIndexOrThrow(_cursor, "account")
          val _cursorIndexOfTargetAccount: Int = getColumnIndexOrThrow(_cursor, "targetAccount")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<LedgerTransaction> =
              ArrayList<LedgerTransaction>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: LedgerTransaction
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpType: String
            _tmpType = _cursor.getString(_cursorIndexOfType)
            val _tmpAmountCents: Long
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents)
            val _tmpCategory: String
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            val _tmpAccount: String
            _tmpAccount = _cursor.getString(_cursorIndexOfAccount)
            val _tmpTargetAccount: String
            _tmpTargetAccount = _cursor.getString(_cursorIndexOfTargetAccount)
            val _tmpNote: String
            _tmpNote = _cursor.getString(_cursorIndexOfNote)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                LedgerTransaction(_tmpId,_tmpType,_tmpAmountCents,_tmpCategory,_tmpAccount,_tmpTargetAccount,_tmpNote,_tmpTimestamp,_tmpDeletedAt)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override suspend fun getAll(): List<LedgerTransaction> {
    val _sql: String =
        "SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC, id DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<List<LedgerTransaction>> {
      public override fun call(): List<LedgerTransaction> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfAmountCents: Int = getColumnIndexOrThrow(_cursor, "amountCents")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfAccount: Int = getColumnIndexOrThrow(_cursor, "account")
          val _cursorIndexOfTargetAccount: Int = getColumnIndexOrThrow(_cursor, "targetAccount")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<LedgerTransaction> =
              ArrayList<LedgerTransaction>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: LedgerTransaction
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpType: String
            _tmpType = _cursor.getString(_cursorIndexOfType)
            val _tmpAmountCents: Long
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents)
            val _tmpCategory: String
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            val _tmpAccount: String
            _tmpAccount = _cursor.getString(_cursorIndexOfAccount)
            val _tmpTargetAccount: String
            _tmpTargetAccount = _cursor.getString(_cursorIndexOfTargetAccount)
            val _tmpNote: String
            _tmpNote = _cursor.getString(_cursorIndexOfNote)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                LedgerTransaction(_tmpId,_tmpType,_tmpAmountCents,_tmpCategory,_tmpAccount,_tmpTargetAccount,_tmpNote,_tmpTimestamp,_tmpDeletedAt)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
