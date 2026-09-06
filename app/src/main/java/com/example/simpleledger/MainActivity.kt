package com.example.simpleledger

import android.app.Application
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Query
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView.Guidelines
import com.canhub.cropper.CropImageView.RequestSizeOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Entity(tableName = "transactions")
data class LedgerTransaction(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountCents: Long,
    val category: String,
    val account: String,
    val targetAccount: String = "",
    val note: String,
    val timestamp: Long,
    val deletedAt: Long? = null,
)

private val DefaultExpenseCategories = listOf("餐饮", "交通", "购物", "居住", "娱乐", "医疗", "其他")
private val DefaultIncomeCategories = listOf("工资", "奖金", "兼职", "红包", "其他")
private val DefaultAccounts = listOf("现金", "银行卡", "微信", "支付宝")
private const val MinBackgroundOverlay = 0.15f
private const val MaxBackgroundOverlay = 0.80f
private val LedgerDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val LedgerDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
internal const val RecentlyDeletedRetentionMillis = 24L * 60L * 60L * 1000L
internal const val NoteMaxLength = 100

internal fun transactionTimestamp(
    date: LocalDate,
    now: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): Long {
    val currentTime = Instant.ofEpochMilli(now).atZone(zoneId).toLocalTime()
    return date.atTime(currentTime).atZone(zoneId).toInstant().toEpochMilli()
}

internal fun recentlyDeletedCutoff(now: Long): Long =
    now - RecentlyDeletedRetentionMillis

internal fun isRecentlyDeleted(deletedAt: Long?, now: Long): Boolean =
    deletedAt != null && deletedAt > recentlyDeletedCutoff(now) && deletedAt <= now

internal fun normalizeBackgroundOverlay(value: Float): Float =
    value.coerceIn(MinBackgroundOverlay, MaxBackgroundOverlay)

internal fun removeCustomOption(current: List<String>, defaults: List<String>, name: String): List<String> =
    if (name in defaults) current else current.filterNot { it == name }

internal fun normalizeNote(note: String): String = note.trim().take(NoteMaxLength)

internal fun sanitizeAmountInput(input: String): String {
    val normalizedSeparators = input.map { char ->
        if (char == ',' || char == '，' || char == '。') '.' else char
    }
    var hasDecimalPoint = false
    var decimalDigits = 0
    return buildString {
        normalizedSeparators.forEach { char ->
            when {
                char in '0'..'9' && (!hasDecimalPoint || decimalDigits < 2) -> {
                    append(char)
                    if (hasDecimalPoint) decimalDigits += 1
                }
                char == '.' && !hasDecimalPoint -> {
                    if (isEmpty()) append('0')
                    append(char)
                    hasDecimalPoint = true
                }
            }
        }
    }.take(12)
}

internal fun parseAmountToCents(input: String): Long? {
    val normalized = input.trim()
        .replace(',', '.')
        .replace('，', '.')
        .replace('。', '.')
    if (!normalized.matches(Regex("""\d+(\.\d{1,2})?"""))) return null
    return runCatching {
        BigDecimal(normalized).movePointRight(2).longValueExact().takeIf { it > 0L }
    }.getOrNull()
}

internal fun amountCentsToInput(amountCents: Long): String =
    BigDecimal.valueOf(amountCents, 2).stripTrailingZeros().toPlainString()

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC, id DESC")
    fun observeAll(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC, id DESC")
    fun observeDeleted(): Flow<List<LedgerTransaction>>

    @Insert
    suspend fun insert(transaction: LedgerTransaction)

    @Insert
    suspend fun insertAll(transactions: List<LedgerTransaction>)

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC, id DESC")
    suspend fun getAll(): List<LedgerTransaction>

    @Query("UPDATE transactions SET deletedAt = :deletedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun softDeleteById(id: Long, deletedAt: Long): Int

    @Update
    suspend fun update(transaction: LedgerTransaction): Int

    @Query("UPDATE transactions SET deletedAt = NULL WHERE id = :id AND deletedAt IS NOT NULL AND deletedAt > :cutoff")
    suspend fun restoreById(id: Long, cutoff: Long): Int

    @Query("DELETE FROM transactions WHERE deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long): Int
}

@Database(entities = [LedgerTransaction::class], version = 3, exportSchema = false)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var instance: LedgerDatabase? = null

        fun get(application: Application): LedgerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    application,
                    LedgerDatabase::class.java,
                    "simple-ledger.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN targetAccount TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN deletedAt INTEGER")
            }
        }
    }
}

internal enum class StatsPeriod(val label: String) {
    DAY("日"), WEEK("周"), MONTH("月"), YEAR("年"), CUSTOM("自定义")
}

internal enum class StatsMetric(val label: String, val transactionType: String) {
    EXPENSE("支出", "支出"), INCOME("收入", "收入")
}

internal data class StatsDateRange(
    val start: LocalDate,
    val endInclusive: LocalDate,
    val label: String,
)

internal fun statsDateRange(
    period: StatsPeriod,
    today: LocalDate,
    customStart: LocalDate,
    customEnd: LocalDate,
): StatsDateRange {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return when (period) {
        StatsPeriod.DAY -> StatsDateRange(today, today, today.format(formatter))
        StatsPeriod.WEEK -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            StatsDateRange(start, end, "${start.format(formatter)} 至 ${end.format(formatter)}")
        }
        StatsPeriod.MONTH -> {
            val start = today.withDayOfMonth(1)
            val end = start.plusMonths(1).minusDays(1)
            StatsDateRange(start, end, DateTimeFormatter.ofPattern("yyyy年M月").format(start))
        }
        StatsPeriod.YEAR -> {
            val start = today.withDayOfYear(1)
            val end = start.plusYears(1).minusDays(1)
            StatsDateRange(start, end, DateTimeFormatter.ofPattern("yyyy年").format(start))
        }
        StatsPeriod.CUSTOM -> {
            val start = minOf(customStart, customEnd)
            val end = maxOf(customStart, customEnd)
            StatsDateRange(start, end, "${start.format(formatter)} 至 ${end.format(formatter)}")
        }
    }
}

/**
 * Moves the anchor date by one complete period.  Keeping the anchor as a
 * date (instead of storing a range) means switching between day/week/month/
 * year keeps the user's selected point in time and makes historical browsing
 * predictable.
 */
internal fun shiftStatsAnchor(period: StatsPeriod, anchor: LocalDate, direction: Int): LocalDate =
    when (period) {
        StatsPeriod.DAY -> anchor.plusDays(direction.toLong())
        StatsPeriod.WEEK -> anchor
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusWeeks(direction.toLong())
        StatsPeriod.MONTH -> anchor
            .withDayOfMonth(1)
            .plusMonths(direction.toLong())
        StatsPeriod.YEAR -> anchor
            .withDayOfYear(1)
            .plusYears(direction.toLong())
        StatsPeriod.CUSTOM -> anchor
    }

internal fun statsRangeDisplayLabel(range: StatsDateRange): String {
    val fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val shortFormatter = DateTimeFormatter.ofPattern("MM-dd")
    return if (range.start == range.endInclusive) {
        range.start.format(fullFormatter)
    } else if (range.start.year == range.endInclusive.year) {
        "${range.start.format(fullFormatter)} ~ ${range.endInclusive.format(shortFormatter)}"
    } else {
        "${range.start.format(fullFormatter)} ~ ${range.endInclusive.format(fullFormatter)}"
    }
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LedgerDatabase.get(application).transactionDao()
    private val preferences = application.getSharedPreferences("ledger_options", Application.MODE_PRIVATE)
    val transactions: Flow<List<LedgerTransaction>> = dao.observeAll()
    val recentlyDeleted: Flow<List<LedgerTransaction>> = dao.observeDeleted().map { deleted ->
        val now = System.currentTimeMillis()
        deleted.filter { isRecentlyDeleted(it.deletedAt, now) }
    }

    init {
        viewModelScope.launch {
            while (true) {
                purgeExpiredDeleted()
                delay(60L * 60L * 1000L)
            }
        }
    }

    private val _expenseCategories = MutableStateFlow(
        loadList("expense_categories", DefaultExpenseCategories),
    )
    val expenseCategories: StateFlow<List<String>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(
        loadList("income_categories", DefaultIncomeCategories),
    )
    val incomeCategories: StateFlow<List<String>> = _incomeCategories.asStateFlow()

    private val _accounts = MutableStateFlow(
        loadList("accounts", DefaultAccounts),
    )
    val accounts: StateFlow<List<String>> = _accounts.asStateFlow()

    private val _backgroundUri = MutableStateFlow(preferences.getString("background_uri", null))
    val backgroundUri: StateFlow<String?> = _backgroundUri.asStateFlow()

    private val _backgroundOverlay = MutableStateFlow(
        normalizeBackgroundOverlay(preferences.getFloat("background_overlay", 0.50f)),
    )
    val backgroundOverlay: StateFlow<Float> = _backgroundOverlay.asStateFlow()

    private val _importUiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    internal val importUiState: StateFlow<ImportUiState> = _importUiState.asStateFlow()

    fun add(
        type: String,
        amountText: String,
        category: String,
        account: String,
        targetAccount: String,
        note: String,
        date: LocalDate,
    ) {
        val amountCents = parseAmountToCents(amountText) ?: return
        if (type !in setOf("支出", "收入", "转账") || account.isBlank()) return
        if (type != "转账" && category.isBlank()) return
        if (type == "转账" && (targetAccount.isBlank() || account == targetAccount)) return
        val timestamp = transactionTimestamp(date)
        viewModelScope.launch {
            dao.insert(
                LedgerTransaction(
                    type = type,
                    amountCents = amountCents,
                    category = if (type == "转账") "转账" else category,
                    account = account.trim(),
                    targetAccount = if (type == "转账") targetAccount.trim() else "",
                    note = normalizeNote(note),
                    timestamp = timestamp,
                ),
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { dao.softDeleteById(id, System.currentTimeMillis()) }
    }

    fun update(
        transaction: LedgerTransaction,
        type: String,
        amountText: String,
        category: String,
        account: String,
        targetAccount: String,
        note: String,
        date: LocalDate,
    ) {
        val amountCents = parseAmountToCents(amountText) ?: return
        if (type !in setOf("支出", "收入", "转账") || account.isBlank()) return
        if (type != "转账" && category.isBlank()) return
        if (type == "转账" && (targetAccount.isBlank() || account == targetAccount)) return
        val updated = transaction.copy(
            type = type,
            amountCents = amountCents,
            category = if (type == "转账") "转账" else category,
            account = account.trim(),
            targetAccount = if (type == "转账") targetAccount.trim() else "",
            note = normalizeNote(note),
            timestamp = transactionTimestamp(date, transaction.timestamp),
        )
        viewModelScope.launch { dao.update(updated) }
    }

    fun restore(id: Long) {
        viewModelScope.launch { dao.restoreById(id, recentlyDeletedCutoff(System.currentTimeMillis())) }
    }

    private suspend fun purgeExpiredDeleted() {
        dao.purgeDeletedBefore(recentlyDeletedCutoff(System.currentTimeMillis()))
    }

    fun adjustBalance(deltaCents: Long) {
        if (deltaCents == 0L) return
        viewModelScope.launch {
            dao.insert(
                LedgerTransaction(
                    type = if (deltaCents > 0) "收入" else "支出",
                    amountCents = abs(deltaCents),
                    category = "结余调整",
                    account = "现金",
                    note = "手动调整本月结余",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun addCategory(type: String, name: String) {
        val normalized = name.trim().take(20)
        if (normalized.isBlank()) return
        val target = if (type == "收入") _incomeCategories else _expenseCategories
        val key = if (type == "收入") "income_categories" else "expense_categories"
        val updated = (target.value + normalized).distinct()
        target.value = updated
        saveList(key, updated)
    }

    fun addAccount(name: String) {
        val normalized = name.trim().take(20)
        if (normalized.isBlank()) return
        val updated = (_accounts.value + normalized).distinct()
        _accounts.value = updated
        saveList("accounts", updated)
    }

    fun deleteCategory(type: String, name: String) {
        val target = if (type == "收入") _incomeCategories else _expenseCategories
        val defaults = if (type == "收入") DefaultIncomeCategories else DefaultExpenseCategories
        val key = if (type == "收入") "income_categories" else "expense_categories"
        val updated = removeCustomOption(target.value, defaults, name)
        if (updated == target.value) return
        target.value = updated
        saveList(key, updated)
    }

    fun deleteAccount(name: String) {
        val updated = removeCustomOption(_accounts.value, DefaultAccounts, name)
        if (updated == _accounts.value) return
        _accounts.value = updated
        saveList("accounts", updated)
    }

    fun setBackgroundUri(uri: String?) {
        _backgroundUri.value = uri
        val editor = preferences.edit().putString("background_uri", uri)
        if (uri != null && _backgroundOverlay.value > 0.65f) {
            _backgroundOverlay.value = 0.50f
            editor.putFloat("background_overlay", 0.50f)
        }
        editor.apply()
    }

    fun setBackgroundOverlay(value: Float) {
        val normalized = normalizeBackgroundOverlay(value)
        _backgroundOverlay.value = normalized
        preferences.edit().putFloat("background_overlay", normalized).apply()
    }

    fun prepareImport(source: Uri) {
        if (_importUiState.value is ImportUiState.Reading) return
        _importUiState.value = ImportUiState.Reading
        viewModelScope.launch {
            _importUiState.value = try {
                val preview = withContext(Dispatchers.IO) {
                    val backup = readLedgerBackup(getApplication(), source)
                    val existing = dao.getAll()
                    ImportPreview(
                        source = source,
                        transactions = backup.transactions,
                        newTransactions = filterNewTransactions(backup.transactions, existing),
                    )
                }
                ImportUiState.Ready(preview)
            } catch (error: Exception) {
                ImportUiState.Error(error.message ?: "账单文件读取失败")
            }
        }
    }

    fun confirmImport() {
        val preview = (_importUiState.value as? ImportUiState.Ready)?.preview ?: return
        _importUiState.value = ImportUiState.Reading
        viewModelScope.launch {
            try {
                val newTransactions = withContext(Dispatchers.IO) {
                    val current = dao.getAll()
                    filterNewTransactions(preview.transactions, current).also { pending ->
                        if (pending.isNotEmpty()) dao.insertAll(pending.map { it.copy(id = 0) })
                    }
                }
                mergeImportedOptions(preview.transactions)
                _importUiState.value = ImportUiState.Completed(
                    importedCount = newTransactions.size,
                    skippedCount = preview.transactions.size - newTransactions.size,
                )
            } catch (error: Exception) {
                _importUiState.value = ImportUiState.Error(error.message ?: "账单导入失败")
            }
        }
    }

    fun dismissImport() {
        _importUiState.value = ImportUiState.Idle
    }

    suspend fun exportBackup(destination: Uri): Int = withContext(Dispatchers.IO) {
        val allTransactions = dao.getAll()
        writeLedgerBackup(getApplication(), destination, allTransactions)
        allTransactions.size
    }

    suspend fun createShareBackup(): Pair<Uri, Int> = withContext(Dispatchers.IO) {
        val allTransactions = dao.getAll()
        createSharedLedgerBackup(getApplication(), allTransactions) to allTransactions.size
    }

    private fun mergeImportedOptions(imported: List<LedgerTransaction>) {
        val expenseCategories = imported.filter { it.type == "支出" }.map { it.category }
        val incomeCategories = imported.filter { it.type == "收入" }.map { it.category }
        val importedAccounts = imported.flatMap { listOf(it.account, it.targetAccount) }.filter { it.isNotBlank() }

        _expenseCategories.value = (_expenseCategories.value + expenseCategories).distinct()
        _incomeCategories.value = (_incomeCategories.value + incomeCategories).distinct()
        _accounts.value = (_accounts.value + importedAccounts).distinct()
        saveList("expense_categories", _expenseCategories.value)
        saveList("income_categories", _incomeCategories.value)
        saveList("accounts", _accounts.value)
    }

    private fun loadList(key: String, defaults: List<String>): List<String> {
        val stored = preferences.getString(key, null) ?: return defaults
        return runCatching {
            val array = JSONArray(stored)
            buildList {
                repeat(array.length()) {
                    val value = array.optString(it).trim()
                    if (value.isNotBlank()) add(value)
                }
            }.ifEmpty { defaults }
        }.getOrDefault(defaults)
    }

    private fun saveList(key: String, values: List<String>) {
        preferences.edit().putString(key, JSONArray(values).toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    private val incomingImportUri = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingImportUri.value = intent.ledgerImportUri()
        setContent {
            SimpleLedgerApp(
                incomingImportUri = incomingImportUri,
                onIncomingImportConsumed = { uri ->
                    if (incomingImportUri.value == uri) incomingImportUri.value = null
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingImportUri.value = intent.ledgerImportUri()
    }
}

@Suppress("DEPRECATION")
private fun Intent.ledgerImportUri(): Uri? = when (action) {
    Intent.ACTION_VIEW -> data
    Intent.ACTION_SEND -> getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
    else -> null
}

private val LedgerColorScheme = lightColorScheme(
    primary = Color(0xFF176B5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE9),
    onPrimaryContainer = Color(0xFF123F36),
    secondary = Color(0xFFD8644B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5DE),
    onSecondaryContainer = Color(0xFF5A1F15),
    background = Color(0xFFF4F7F6),
    onBackground = Color(0xFF1C2522),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C2522),
    surfaceVariant = Color(0xFFEDF3F1),
    onSurfaceVariant = Color(0xFF56625E),
    outline = Color(0xFFB9C8C3),
    outlineVariant = Color(0xFFDCE5E2),
    error = Color(0xFFBA3F35),
)

private val LedgerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(9.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

private enum class AppTab(val label: String) {
    HOME("首页"), BILLS("账单"), STATS("统计"), ME("我的")
}

@Composable
private fun SimpleLedgerApp(
    incomingImportUri: StateFlow<Uri?>,
    onIncomingImportConsumed: (Uri) -> Unit,
) {
    val viewModel: LedgerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as Application,
        ),
    )
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(emptyList())
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsStateWithLifecycle(emptyList())
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val backgroundUri by viewModel.backgroundUri.collectAsStateWithLifecycle()
    val backgroundOverlay by viewModel.backgroundOverlay.collectAsStateWithLifecycle()
    val importUiState by viewModel.importUiState.collectAsStateWithLifecycle()
    val incomingUri by incomingImportUri.collectAsStateWithLifecycle()
    val backgroundBitmap by rememberBackgroundBitmap(backgroundUri)
    val animatedOverlay by animateFloatAsState(
        targetValue = backgroundOverlay,
        animationSpec = tween(durationMillis = 180),
        label = "background-overlay",
    )
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingTransactionId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editingTransaction = transactions.firstOrNull { it.id == editingTransactionId }

    LaunchedEffect(incomingUri) {
        incomingUri?.let { uri ->
            viewModel.prepareImport(uri)
            onIncomingImportConsumed(uri)
        }
    }

    MaterialTheme(colorScheme = LedgerColorScheme, shapes = LedgerShapes) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = backgroundBitmap,
                animationSpec = tween(durationMillis = 320),
                label = "custom-background",
            ) { bitmap ->
                if (bitmap == null) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = animatedOverlay)),
                        )
                    }
                }
            }
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                AnimatedContent(
                    targetState = showEditor,
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically(tween(240)) { it / 8 } + fadeIn(tween(180))) togetherWith
                                (slideOutVertically(tween(180)) { -it / 12 } + fadeOut(tween(140)))
                        } else {
                            (slideInVertically(tween(240)) { -it / 12 } + fadeIn(tween(180))) togetherWith
                                (slideOutVertically(tween(180)) { it / 8 } + fadeOut(tween(140)))
                        }
                    },
                    label = "editor-transition",
                ) { editorVisible ->
                if (editorVisible) {
                    EditorScreen(
                    transaction = editingTransaction,
                    onClose = {
                        showEditor = false
                        editingTransactionId = null
                    },
                    onSave = { type, amount, category, account, targetAccount, note, date ->
                        if (editingTransaction == null) {
                            viewModel.add(type, amount, category, account, targetAccount, note, date)
                        } else {
                            viewModel.update(
                                editingTransaction,
                                type,
                                amount,
                                category,
                                account,
                                targetAccount,
                                note,
                                date,
                            )
                        }
                        showEditor = false
                        editingTransactionId = null
                    },
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    accounts = accounts,
                    onAddCategory = viewModel::addCategory,
                    onAddAccount = viewModel::addAccount,
                    onDeleteCategory = viewModel::deleteCategory,
                    onDeleteAccount = viewModel::deleteAccount,
                )
                } else {
                    MainScaffold(
                    tab = AppTab.valueOf(tab),
                    transactions = transactions,
                    recentlyDeleted = recentlyDeleted,
                    onTabChange = { tab = it.name },
                    onAdd = {
                        editingTransactionId = null
                        showEditor = true
                    },
                    onEdit = { id ->
                        editingTransactionId = id
                        showEditor = true
                    },
                    onDelete = viewModel::delete,
                    onRestore = viewModel::restore,
                    onAdjustBalance = viewModel::adjustBalance,
                    categoryCount = (expenseCategories + incomeCategories).distinct().size,
                    accounts = accounts,
                    backgroundUri = backgroundUri,
                    backgroundOverlay = backgroundOverlay,
                    onBackgroundSelected = viewModel::setBackgroundUri,
                    onBackgroundOverlayChange = viewModel::setBackgroundOverlay,
                    onImportSelected = viewModel::prepareImport,
                    exportBackup = viewModel::exportBackup,
                    createShareBackup = viewModel::createShareBackup,
                )
                }
                }
            }
            ImportStateDialog(
                state = importUiState,
                onConfirm = viewModel::confirmImport,
                onDismiss = viewModel::dismissImport,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    tab: AppTab,
    transactions: List<LedgerTransaction>,
    recentlyDeleted: List<LedgerTransaction>,
    onTabChange: (AppTab) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRestore: (Long) -> Unit,
    onAdjustBalance: (Long) -> Unit,
    categoryCount: Int,
    accounts: List<String>,
    backgroundUri: String?,
    backgroundOverlay: Float,
    onBackgroundSelected: (String?) -> Unit,
    onBackgroundOverlayChange: (Float) -> Unit,
    onImportSelected: (Uri) -> Unit,
    exportBackup: suspend (Uri) -> Int,
    createShareBackup: suspend () -> Pair<Uri, Int>,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(tab.label) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                ),
            )
        },
        floatingActionButton = {
            if (tab == AppTab.HOME || tab == AppTab.BILLS) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "记一笔")
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ) {
                NavigationBarItem(
                    selected = tab == AppTab.HOME,
                    onClick = { onTabChange(AppTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("首页") },
                )
                NavigationBarItem(
                    selected = tab == AppTab.BILLS,
                    onClick = { onTabChange(AppTab.BILLS) },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("账单") },
                )
                NavigationBarItem(
                    selected = tab == AppTab.STATS,
                    onClick = { onTabChange(AppTab.STATS) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("统计") },
                )
                NavigationBarItem(
                    selected = tab == AppTab.ME,
                    onClick = { onTabChange(AppTab.ME) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("我的") },
                )
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                (slideInHorizontally(tween(220)) { direction * it / 7 } + fadeIn(tween(180))) togetherWith
                    (slideOutHorizontally(tween(180)) { -direction * it / 9 } + fadeOut(tween(140)))
            },
            label = "tab-transition",
        ) { currentTab ->
        when (currentTab) {
            AppTab.HOME -> HomeScreen(
                padding,
                transactions,
                onAdd,
                onAdjustBalance,
                onEdit = onEdit,
            )
            AppTab.BILLS -> BillsScreen(
                padding,
                transactions,
                recentlyDeleted,
                onDelete,
                onRestore,
                onEdit = onEdit,
            )
            AppTab.STATS -> StatsScreen(padding, transactions)
            AppTab.ME -> MeScreen(
                padding,
                categoryCount,
                accounts,
                backgroundUri,
                backgroundOverlay,
                onBackgroundSelected,
                onBackgroundOverlayChange,
                onImportSelected,
                exportBackup,
                createShareBackup,
            )
        }
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    transactions: List<LedgerTransaction>,
    onAdd: () -> Unit,
    onAdjustBalance: (Long) -> Unit,
    onEdit: (Long) -> Unit,
) {
    val monthTransactions = transactions.filter { it.inCurrentMonth() }
    val expense = monthTransactions.filter { it.type == "支出" }.sumOf { it.amountCents }
    val income = monthTransactions.filter { it.type == "收入" }.sumOf { it.amountCents }
    val balance = income - expense
    var showBalanceDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("本月概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SummaryItem("支出", expense, Color(0xFFB34C3B))
                SummaryItem("收入", income, Color(0xFF246B5A))
                SummaryItem("结余", balance, Color(0xFF353D3A))
            }
        }
        TextButton(
            onClick = { showBalanceDialog = true },
            modifier = Modifier.align(Alignment.End),
        ) { Text("调整本月结余") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("记一笔")
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("最近账单", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("共 ${transactions.size} 笔", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        if (transactions.isEmpty()) {
            EmptyState("还没有账单", "记录第一笔消费，开始了解自己的钱花在哪里")
        } else {
            transactions.take(5).forEach { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onEdit = { onEdit(transaction.id) },
                )
            }
        }
        Spacer(Modifier.height(88.dp))
    }
    if (showBalanceDialog) {
        BalanceAdjustDialog(
            currentBalance = balance,
            onDismiss = { showBalanceDialog = false },
            onConfirm = { targetBalance ->
                onAdjustBalance(targetBalance - balance)
                showBalanceDialog = false
            },
        )
    }
}

@Composable
private fun SummaryItem(label: String, cents: Long, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(formatMoney(cents), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BillsScreen(
    padding: PaddingValues,
    transactions: List<LedgerTransaction>,
    recentlyDeleted: List<LedgerTransaction>,
    onDelete: (Long) -> Unit,
    onRestore: (Long) -> Unit,
    onEdit: (Long) -> Unit,
) {
    var transactionToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRecentlyDeleted by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.padding(padding),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OutlinedButton(
                onClick = { showRecentlyDeleted = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("最近删除${if (recentlyDeleted.isEmpty()) "" else " (${recentlyDeleted.size})"}")
            }
        }
        item { Text("全部记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (transactions.isEmpty()) {
            item { EmptyState("暂无账单", "点击右下角加号记录一笔") }
        } else {
            items(transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onEdit = { onEdit(transaction.id) },
                    onDelete = { transactionToDeleteId = transaction.id },
                )
            }
        }
    }
    if (showRecentlyDeleted) {
        RecentlyDeletedDialog(
            transactions = recentlyDeleted,
            onRestore = onRestore,
            onDismiss = { showRecentlyDeleted = false },
        )
    }
    transactions.firstOrNull { it.id == transactionToDeleteId }?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDeleteId = null },
            title = { Text("确认删除账单") },
            text = { Text("删除后可在最近删除中恢复，超过 24 小时将自动永久删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(transaction.id)
                        transactionToDeleteId = null
                    },
                ) { Text("确认删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDeleteId = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun RecentlyDeletedDialog(
    transactions: List<LedgerTransaction>,
    onRestore: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("最近删除") },
        text = {
            if (transactions.isEmpty()) {
                Text("最近 24 小时没有删除的账单。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions, key = { it.id }) { transaction ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(transaction.category, fontWeight = FontWeight.Medium)
                                    Text(
                                        "删除于 ${formatDeletedAt(transaction.deletedAt ?: 0L)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { onRestore(transaction.id) }) { Text("恢复") }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } },
    )
}

@Composable
private fun TransactionRow(
    transaction: LedgerTransaction,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.small,
                color = if (transaction.type == "收入") Color(0xFFDCEFE9) else Color(0xFFFFE5DE),
            ) {
                BoxCentered { Text(if (transaction.type == "收入") "+" else "-", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Medium)
                Text(
                    transaction.localDateTime().format(LedgerDateTimeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    transaction.note.ifBlank { transaction.account },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                (if (transaction.type == "收入") "+" else "-") + formatMoney(transaction.amountCents),
                color = if (transaction.type == "收入") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
            )
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑账单")
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@Composable
private fun BoxCentered(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(padding: PaddingValues, transactions: List<LedgerTransaction>) {
    var periodName by rememberSaveable { mutableStateOf(StatsPeriod.MONTH.name) }
    var metricName by rememberSaveable { mutableStateOf(StatsMetric.EXPENSE.name) }
    var anchorDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var customStartDay by rememberSaveable {
        mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay())
    }
    var customEndDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    val period = StatsPeriod.valueOf(periodName)
    val metric = StatsMetric.valueOf(metricName)
    val anchorDate = LocalDate.ofEpochDay(anchorDay)
    val customStart = LocalDate.ofEpochDay(customStartDay)
    val customEnd = LocalDate.ofEpochDay(customEndDay)
    val range = statsDateRange(period, anchorDate, customStart, customEnd)
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(period) {
        if (period != StatsPeriod.CUSTOM) anchorDay = LocalDate.now().toEpochDay()
    }
    val periodTransactions = transactions.filter { transaction ->
        transaction.localDate() in range.start..range.endInclusive
    }
    val expense = periodTransactions.filter { it.type == "支出" }.sumOf { it.amountCents }
    val income = periodTransactions.filter { it.type == "收入" }.sumOf { it.amountCents }
    val selectedTransactions = periodTransactions.filter { it.type == metric.transactionType }
    val total = selectedTransactions.sumOf { it.amountCents }.coerceAtLeast(1)
    val byCategory = selectedTransactions.groupBy { it.category }.mapValues { (_, values) -> values.sumOf { it.amountCents } }
        .toList().sortedByDescending { it.second }
    Column(
        modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val options = StatsPeriod.values()
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    selected = period == option,
                    onClick = { periodName = option.name },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    icon = {},
                ) { Text(option.label) }
            }
        }
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val options = StatsMetric.values()
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    selected = metric == option,
                    onClick = { metricName = option.name },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    icon = {},
                ) { Text(option.label) }
            }
        }
        if (period != StatsPeriod.CUSTOM) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(
                    onClick = { anchorDay = shiftStatsAnchor(period, anchorDate, -1).toEpochDay() },
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "上一段")
                }
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, anchorDate) { selected ->
                            anchorDay = selected.toEpochDay()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(statsRangeDisplayLabel(range), style = MaterialTheme.typography.labelSmall)
                }
                IconButton(
                    onClick = { anchorDay = shiftStatsAnchor(period, anchorDate, 1).toEpochDay() },
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "下一段")
                }
            }
        }
        if (period == StatsPeriod.CUSTOM) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, customStart) { selected ->
                            customStartDay = selected.toEpochDay()
                            if (selected.isAfter(customEnd)) customEndDay = selected.toEpochDay()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("起 ${customStart.format(DateTimeFormatter.ofPattern("MM-dd"))}") }
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, customEnd) { selected ->
                            customEndDay = selected.toEpochDay()
                            if (selected.isBefore(customStart)) customStartDay = selected.toEpochDay()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("止 ${customEnd.format(DateTimeFormatter.ofPattern("MM-dd"))}") }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(statsRangeDisplayLabel(range), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SummaryItem("支出", expense, Color(0xFFB34C3B))
                SummaryItem("收入", income, Color(0xFF246B5A))
                SummaryItem("结余", income - expense, Color(0xFF353D3A))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("${metric.label}分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (byCategory.isEmpty()) {
            EmptyState("暂无${metric.label}数据", "先记录一笔${metric.label}，统计会在这里显示")
        } else {
            Interactive3DPieChart(byCategory, metric.label)
            Spacer(Modifier.height(24.dp))
            byCategory.forEachIndexed { index, (category, cents) ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(chartColor(index), CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category, fontWeight = FontWeight.Medium)
                        LinearProgressIndicator(
                            progress = { cents.toFloat() / total.toFloat() },
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(formatMoney(cents), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

private fun chartColor(index: Int): Color =
    Color.hsv((index * 137.508f) % 360f, saturation = 0.62f, value = 0.78f)

@Composable
private fun Interactive3DPieChart(items: List<Pair<String, Long>>, metricLabel: String) {
    val total = items.sumOf { it.second }.coerceAtLeast(1)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(items) { selectedIndex = null }

    Box(modifier = Modifier.fillMaxWidth().height(310.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items) {
                    detectTapGestures(
                        onTap = { selectedIndex = null },
                        onLongPress = { position ->
                            val index = findPieSlice(position, size.width.toFloat(), size.height.toFloat(), items)
                            if (index != null) {
                                selectedIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                    )
                },
        ) {
            val geometry = pieGeometry(size.width, size.height)
            val sweeps = items.map { (_, cents) -> cents.toFloat() / total.toFloat() * 360f }
            val starts = buildList {
                var angle = -90f
                sweeps.forEach { sweep ->
                    add(angle)
                    angle += sweep
                }
            }
            val drawOrder = items.indices.filter { it != selectedIndex } + listOfNotNull(selectedIndex)
            val maxDepth = geometry.depth * if (selectedIndex != null) 1.9f else 1f

            for (layer in maxDepth.roundToInt() downTo 1) {
                drawOrder.forEach { index ->
                    val depth = if (index == selectedIndex) geometry.depth * 1.9f else geometry.depth
                    if (layer <= depth) {
                        val offset = sliceOffset(index, selectedIndex, starts[index], sweeps[index], geometry)
                        drawArc(
                            color = chartColor(index).darkened(0.58f),
                            startAngle = starts[index],
                            sweepAngle = sweeps[index],
                            useCenter = true,
                            topLeft = Offset(
                                geometry.center.x - geometry.radiusX + offset.x,
                                geometry.center.y - geometry.radiusY + offset.y + layer,
                            ),
                            size = Size(geometry.radiusX * 2f, geometry.radiusY * 2f),
                        )
                    }
                }
            }

            drawOrder.forEach { index ->
                val offset = sliceOffset(index, selectedIndex, starts[index], sweeps[index], geometry)
                val topLeft = Offset(
                    geometry.center.x - geometry.radiusX + offset.x,
                    geometry.center.y - geometry.radiusY + offset.y,
                )
                drawArc(
                    color = chartColor(index),
                    startAngle = starts[index],
                    sweepAngle = sweeps[index],
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(geometry.radiusX * 2f, geometry.radiusY * 2f),
                )
                if (index == selectedIndex) {
                    drawArc(
                        color = Color.White,
                        startAngle = starts[index],
                        sweepAngle = sweeps[index],
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(geometry.radiusX * 2f, geometry.radiusY * 2f),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }
            }

            selectedIndex?.let { index ->
                val midAngle = starts[index] + sweeps[index] / 2f
                val radians = Math.toRadians(midAngle.toDouble())
                val selectedOffset = sliceOffset(index, selectedIndex, starts[index], sweeps[index], geometry)
                val anchor = Offset(
                    geometry.center.x + selectedOffset.x + kotlin.math.cos(radians).toFloat() * geometry.radiusX * 0.78f,
                    geometry.center.y + selectedOffset.y + kotlin.math.sin(radians).toFloat() * geometry.radiusY * 0.78f + geometry.depth,
                )
                val boxAnchor = Offset(size.width / 2f, size.height * 0.215f)
                val elbow = Offset(anchor.x, boxAnchor.y + 16.dp.toPx())
                val connectorColor = chartColor(index).darkened(0.75f)
                drawLine(connectorColor, anchor, elbow, strokeWidth = 2.5.dp.toPx())
                drawLine(connectorColor, elbow, boxAnchor, strokeWidth = 2.5.dp.toPx())
                drawCircle(connectorColor, radius = 4.dp.toPx(), center = anchor)
            }
        }

        selectedIndex?.let { index ->
            val (category, cents) = items[index]
            val percentage = cents.toFloat() / total.toFloat() * 100f
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(min = 210.dp, max = 290.dp)
                    .height(66.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "$metricLabel 总计 ${formatMoney(cents)} · ${String.format(Locale.CHINA, "%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

internal data class PieGeometry(
    val center: Offset,
    val radiusX: Float,
    val radiusY: Float,
    val depth: Float,
)

internal fun pieGeometry(width: Float, height: Float): PieGeometry {
    val infoBottom = height * 0.215f
    val radiusX = minOf(width * 0.39f, (height - infoBottom) * 0.44f)
    val radiusY = radiusX * 0.58f
    return PieGeometry(
        center = Offset(width / 2f, infoBottom + radiusY + height * 0.045f),
        radiusX = radiusX,
        radiusY = radiusY,
        depth = (height * 0.055f).coerceAtLeast(12f),
    )
}

internal fun findPieSlice(
    position: Offset,
    width: Float,
    height: Float,
    items: List<Pair<String, Long>>,
): Int? {
    val geometry = pieGeometry(width, height)
    val normalizedX = (position.x - geometry.center.x) / geometry.radiusX
    val normalizedY = (position.y - geometry.center.y) / geometry.radiusY
    if (normalizedX * normalizedX + normalizedY * normalizedY > 1f) return null

    val degrees = Math.toDegrees(kotlin.math.atan2(normalizedY, normalizedX).toDouble()).toFloat()
    val angleFromTop = (degrees + 90f + 360f) % 360f
    val total = items.sumOf { it.second }.coerceAtLeast(1)
    var cumulative = 0f
    items.forEachIndexed { index, (_, cents) ->
        cumulative += cents.toFloat() / total.toFloat() * 360f
        if (angleFromTop <= cumulative) return index
    }
    return items.lastIndex.takeIf { it >= 0 }
}

private fun sliceOffset(
    index: Int,
    selectedIndex: Int?,
    startAngle: Float,
    sweepAngle: Float,
    geometry: PieGeometry,
): Offset {
    if (index != selectedIndex) return Offset.Zero
    val radians = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
    return Offset(
        kotlin.math.cos(radians).toFloat() * geometry.radiusX * 0.11f,
        kotlin.math.sin(radians).toFloat() * geometry.radiusY * 0.11f,
    )
}

private fun Color.darkened(factor: Float): Color = Color(
    red = red * factor,
    green = green * factor,
    blue = blue * factor,
    alpha = alpha,
)

private fun showDatePicker(
    context: android.content.Context,
    initial: LocalDate,
    onSelected: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, day -> onSelected(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

@Composable
private fun MeScreen(
    padding: PaddingValues,
    categoryCount: Int,
    accounts: List<String>,
    backgroundUri: String?,
    backgroundOverlay: Float,
    onBackgroundSelected: (String?) -> Unit,
    onBackgroundOverlayChange: (Float) -> Unit,
    onImportSelected: (Uri) -> Unit,
    exportBackup: suspend (Uri) -> Int,
    createShareBackup: suspend () -> Pair<Uri, Int>,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var transferBusy by rememberSaveable { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImportSelected)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(LedgerBackupMimeType),
    ) { uri ->
        if (uri != null) {
            transferBusy = true
            scope.launch {
                runCatching { exportBackup(uri) }
                    .onSuccess { count ->
                        android.widget.Toast.makeText(context, "已导出 $count 笔账单", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        android.widget.Toast.makeText(context, "导出失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
                    }
                transferBusy = false
            }
        }
    }
    val displayMetrics = context.resources.displayMetrics
    val cropWidth = displayMetrics.widthPixels.coerceAtLeast(1)
    val cropHeight = displayMetrics.heightPixels.coerceAtLeast(1)
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                scope.launch {
                    val storedUri = withContext(Dispatchers.IO) {
                        saveBackgroundImage(context, croppedUri)
                    }
                    if (storedUri != null) {
                        onBackgroundSelected(storedUri)
                        android.widget.Toast.makeText(context, "背景已应用", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "背景保存失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: android.widget.Toast.makeText(context, "没有收到裁剪结果，请重试", android.widget.Toast.LENGTH_SHORT).show()
        } else if (result.error != null) {
            android.widget.Toast.makeText(context, "图片处理失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val initialBackground = withContext(Dispatchers.IO) {
                    saveBackgroundImage(context, uri)
                }
                if (initialBackground != null) {
                    onBackgroundSelected(initialBackground)
                    android.widget.Toast.makeText(context, "图片已应用，可继续调整裁剪范围", android.widget.Toast.LENGTH_SHORT).show()
                }
                cropLauncher.launch(
                    CropImageContractOptions(
                        uri = uri,
                        cropImageOptions = CropImageOptions(
                            fixAspectRatio = true,
                            aspectRatioX = cropWidth,
                            aspectRatioY = cropHeight,
                            guidelines = Guidelines.ON,
                            outputRequestWidth = cropWidth,
                            outputRequestHeight = cropHeight,
                            outputRequestSizeOptions = RequestSizeOptions.RESIZE_EXACT,
                            outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG,
                            outputCompressQuality = 92,
                            activityTitle = "裁剪软件背景",
                        ),
                    ),
                )
            }
        }
    }
    Column(
        modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(16.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("本地账本", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("数据保存在当前设备，基础版本暂不需要登录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingRow("分类管理", "已启用 $categoryCount 个分类，可在记账页继续添加")
        SettingRow("账户管理", accounts.joinToString("、"))
        SettingRow("预算设置", "基础版本待接入")
        Spacer(Modifier.height(16.dp))
        Text("账单迁移", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    importLauncher.launch(
                        arrayOf(LedgerBackupMimeType, "application/json", "application/octet-stream"),
                    )
                },
                enabled = !transferBusy,
            ) { Text("导入账单") }
            OutlinedButton(
                onClick = { exportLauncher.launch(suggestedBackupName()) },
                enabled = !transferBusy,
            ) { Text("导出账单") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                transferBusy = true
                scope.launch {
                    runCatching { createShareBackup() }
                        .onSuccess { (uri, count) ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = LedgerBackupMimeType
                                putExtra(Intent.EXTRA_STREAM, uri)
                                clipData = ClipData.newUri(context.contentResolver, "简记账单", uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "发送 $count 笔账单"))
                        }
                        .onFailure {
                            android.widget.Toast.makeText(context, "账单文件生成失败", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    transferBusy = false
                }
            },
            enabled = !transferBusy,
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("发送到其他设备")
        }
        Spacer(Modifier.height(12.dp))
        Text("软件背景", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { backgroundPicker.launch("image/*") }) { Text("选择图片") }
            if (backgroundUri != null) {
                OutlinedButton(onClick = { onBackgroundSelected(null) }) { Text("恢复默认") }
            }
        }
        if (backgroundUri != null) {
            Spacer(Modifier.height(8.dp))
            Text("已应用自定义背景", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("背景清晰度")
            Text("${((1f - backgroundOverlay) * 100).roundToInt()}%")
        }
        Slider(
            value = 1f - backgroundOverlay,
            onValueChange = { clarity -> onBackgroundOverlayChange(1f - clarity) },
            valueRange = 0.20f..0.85f,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun ImportStateDialog(
    state: ImportUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        ImportUiState.Idle -> Unit
        ImportUiState.Reading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("正在读取账单") },
            text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
            confirmButton = {},
        )
        is ImportUiState.Ready -> {
            val preview = state.preview
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("导入账单") },
                text = {
                    Text(
                        "文件包含 ${preview.transactions.size} 笔账单，可导入 ${preview.newTransactions.size} 笔，" +
                            "重复 ${preview.duplicateCount} 笔。",
                    )
                },
                confirmButton = {
                    TextButton(onClick = if (preview.newTransactions.isEmpty()) onDismiss else onConfirm) {
                        Text(if (preview.newTransactions.isEmpty()) "完成" else "确认导入")
                    }
                },
                dismissButton = {
                    if (preview.newTransactions.isNotEmpty()) {
                        TextButton(onClick = onDismiss) { Text("取消") }
                    }
                },
            )
        }
        is ImportUiState.Completed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("导入完成") },
            text = { Text("已导入 ${state.importedCount} 笔账单，跳过 ${state.skippedCount} 笔重复账单。") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } },
        )
        is ImportUiState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("无法导入") },
            text = { Text(state.message) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        )
    }
}

@Composable
private fun SettingRow(title: String, detail: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(
    transaction: LedgerTransaction? = null,
    onClose: () -> Unit,
    onSave: (String, String, String, String, String, String, LocalDate) -> Unit,
    expenseCategories: List<String>,
    incomeCategories: List<String>,
    accounts: List<String>,
    onAddCategory: (String, String) -> Unit,
    onAddAccount: (String) -> Unit,
    onDeleteCategory: (String, String) -> Unit,
    onDeleteAccount: (String) -> Unit,
) {
    var type by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.type ?: "支出") }
    var amount by rememberSaveable(transaction?.id) {
        mutableStateOf(transaction?.let { amountCentsToInput(it.amountCents) } ?: "")
    }
    var category by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.category ?: "餐饮") }
    var account by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.account ?: "现金") }
    var targetAccount by rememberSaveable(transaction?.id) {
        mutableStateOf(transaction?.targetAccount?.takeIf { it.isNotBlank() } ?: "银行卡")
    }
    var note by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.note ?: "") }
    var selectedDateEpochDay by rememberSaveable(transaction?.id) {
        mutableStateOf(transaction?.localDate()?.toEpochDay() ?: LocalDate.now().toEpochDay())
    }
    var showCategoryDialog by rememberSaveable(transaction?.id) { mutableStateOf(false) }
    var accountDialogTarget by rememberSaveable(transaction?.id) { mutableStateOf<String?>(null) }
    var categoryToDelete by rememberSaveable(transaction?.id) { mutableStateOf<String?>(null) }
    var accountToDelete by rememberSaveable(transaction?.id) { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
    val currentCategories = if (type == "收入") incomeCategories else expenseCategories
    val categories = (
        if (transaction != null && transaction.type == type && transaction.category !in currentCategories) {
            listOf(transaction.category) + currentCategories
        } else {
            currentCategories
        }
    ).distinct()
    val selectableAccounts = (
        listOfNotNull(
            transaction?.account,
            transaction?.targetAccount?.takeIf { it.isNotBlank() },
        ) + accounts
    ).distinct()
    val canSave = parseAmountToCents(amount) != null &&
        account.isNotBlank() &&
        (type != "转账" || (targetAccount.isNotBlank() && account != targetAccount))
    val save = {
        if (canSave) {
            onSave(
                type,
                amount,
                if (type == "转账") "转账" else category,
                account,
                targetAccount,
                note,
                selectedDate,
            )
        }
    }

    LaunchedEffect(type, categories) {
        if (type != "转账" && category !in categories) {
            category = categories.firstOrNull() ?: "其他"
        }
    }
    LaunchedEffect(selectableAccounts) {
        if (account !in selectableAccounts) account = selectableAccounts.firstOrNull() ?: "现金"
        if (targetAccount !in selectableAccounts) {
            targetAccount = selectableAccounts.getOrNull(1) ?: selectableAccounts.firstOrNull() ?: "银行卡"
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (transaction == null) "记一笔" else "编辑账单") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "关闭") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 2.dp,
            ) {
                Button(
                    onClick = save,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                ) { Text("保存") }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("支出", "收入", "转账").forEach { option ->
                    FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option) })
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = sanitizeAmountInput(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("金额") },
                prefix = { Text("¥ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    showDatePicker(context, selectedDate) { date ->
                        selectedDateEpochDay = date.toEpochDay()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                Spacer(Modifier.width(8.dp))
                Text("日期: ${selectedDate.format(LedgerDateFormatter)}")
            }
            if (type != "转账") {
                Spacer(Modifier.height(16.dp))
                Text("分类", style = MaterialTheme.typography.labelLarge)
                OptionPicker(
                    options = categories,
                    selected = category,
                    onSelect = { category = it },
                    onAdd = { showCategoryDialog = true },
                    canDelete = { option ->
                        option in currentCategories &&
                            option !in if (type == "收入") DefaultIncomeCategories else DefaultExpenseCategories
                    },
                    onDelete = { categoryToDelete = it },
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(if (type == "转账") "转出账户" else "账户", style = MaterialTheme.typography.labelLarge)
            OptionPicker(
                options = selectableAccounts,
                selected = account,
                onSelect = { account = it },
                onAdd = { accountDialogTarget = "source" },
                canDelete = { it in accounts && it !in DefaultAccounts },
                onDelete = { accountToDelete = it },
            )
            if (type == "转账") {
                Spacer(Modifier.height(12.dp))
                Text("转入账户", style = MaterialTheme.typography.labelLarge)
                OptionPicker(
                    options = selectableAccounts,
                    selected = targetAccount,
                    onSelect = { targetAccount = it },
                    onAdd = { accountDialogTarget = "target" },
                    canDelete = { it in accounts && it !in DefaultAccounts },
                    onDelete = { accountToDelete = it },
                )
                if (account == targetAccount) {
                    Text(
                        "转出和转入账户不能相同",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(NoteMaxLength) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注（可选）") },
                singleLine = true,
            )
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.height(88.dp))
        }
    }
    if (showCategoryDialog) {
        NameInputDialog(
            title = "添加${if (type == "收入") "收入" else "支出"}分类",
            label = "分类名称",
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name ->
                val normalized = name.trim().take(20)
                onAddCategory(type, normalized)
                category = normalized
                showCategoryDialog = false
            },
        )
    }
    if (accountDialogTarget != null) {
        NameInputDialog(
            title = "添加账户",
            label = "账户名称",
            onDismiss = { accountDialogTarget = null },
            onConfirm = { name ->
                val normalized = name.trim().take(20)
                onAddAccount(normalized)
                if (accountDialogTarget == "target") targetAccount = normalized else account = normalized
                accountDialogTarget = null
            },
        )
    }
    categoryToDelete?.let { name ->
        DeleteOptionDialog(
            title = "删除自定义分类",
            name = name,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onDeleteCategory(type, name)
                categoryToDelete = null
            },
        )
    }
    accountToDelete?.let { name ->
        DeleteOptionDialog(
            title = "删除自定义账户",
            name = name,
            onDismiss = { accountToDelete = null },
            onConfirm = {
                onDeleteAccount(name)
                accountToDelete = null
            },
        )
    }
}

@Composable
private fun OptionPicker(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    canDelete: (String) -> Boolean,
    onDelete: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { it }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) },
                trailingIcon = if (canDelete(option)) {
                    {
                        IconButton(
                            onClick = { onDelete(option) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除$option", modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    null
                },
            )
        }
        item {
            OutlinedButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("自定义")
            }
        }
    }
}

@Composable
private fun DeleteOptionDialog(
    title: String,
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text("确定删除“$name”吗？已有账单仍会保留该名称。") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("删除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun NameInputDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.replace("\n", "").take(20) },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun BalanceAdjustDialog(
    currentBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var value by rememberSaveable(currentBalance) {
        mutableStateOf(String.format(Locale.US, "%.2f", currentBalance / 100.0))
    }
    val parsed = value.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整本月结余") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    value = input.filterIndexed { index, char ->
                        char.isDigit() || char == '.' || (char == '-' && index == 0)
                    }.take(14)
                },
                label = { Text("目标结余") },
                prefix = { Text("¥ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm((parsed!! * 100).roundToLong()) },
                enabled = parsed != null,
            ) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun rememberBackgroundBitmap(uriString: String?): androidx.compose.runtime.State<androidx.compose.ui.graphics.ImageBitmap?> {
    val context = androidx.compose.ui.platform.LocalContext.current
    return produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, key1 = uriString) {
        value = if (uriString == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    decodeSampledBitmap(context, Uri.parse(uriString))?.asImageBitmap()
                }.getOrNull()
            }
        }
    }
}

private fun saveBackgroundImage(context: android.content.Context, source: Uri): String? = runCatching {
    val destination = context.getFileStreamPath("custom_background.jpg")
    context.contentResolver.openInputStream(source)?.use { input ->
        context.openFileOutput(destination.name, android.content.Context.MODE_PRIVATE).use { output ->
            input.copyTo(output)
        }
    } ?: return@runCatching null
    Uri.fromFile(destination)
        .buildUpon()
        .appendQueryParameter("v", System.currentTimeMillis().toString())
        .build()
        .toString()
}.getOrNull()

private fun decodeSampledBitmap(context: android.content.Context, uri: Uri): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openImageStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 1440 || bounds.outHeight / sampleSize > 1440) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return openImageStream(context, uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }
}

private fun openImageStream(context: android.content.Context, uri: Uri): java.io.InputStream? =
    if (uri.scheme == "file") {
        uri.path?.let { java.io.File(it).inputStream() }
    } else {
        context.contentResolver.openInputStream(uri)
    }

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun LedgerTransaction.inCurrentMonth(): Boolean {
    return YearMonth.from(localDate()) == YearMonth.now()
}

private fun LedgerTransaction.localDate(): LocalDate =
    localDateTime().toLocalDate()

private fun LedgerTransaction.localDateTime(): java.time.LocalDateTime =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

private fun formatMoney(cents: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "¥${formatter.format(cents / 100.0)}"
}

private fun formatDeletedAt(timestamp: Long): String {
    if (timestamp <= 0L) return "未知时间"
    val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
}
