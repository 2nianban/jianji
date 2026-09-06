package io.github.nianban2.jianji

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal const val LedgerBackupMimeType = "application/vnd.jianji.ledger+json"
internal const val LedgerBackupExtension = "jianji"
private const val LedgerBackupFormat = "jianji-ledger-backup"
private const val LedgerBackupVersion = 1
private const val MaxBackupBytes = 20L * 1024L * 1024L
private const val MaxBackupTransactions = 50_000

internal data class LedgerBackup(
    val transactions: List<LedgerTransaction>,
    val exportedAt: Long,
)

internal data class ImportPreview(
    val source: Uri,
    val transactions: List<LedgerTransaction>,
    val newTransactions: List<LedgerTransaction>,
) {
    val duplicateCount: Int get() = transactions.size - newTransactions.size
}

internal sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Reading : ImportUiState
    data class Ready(val preview: ImportPreview) : ImportUiState
    data class Error(val message: String) : ImportUiState
    data class Completed(val importedCount: Int, val skippedCount: Int) : ImportUiState
}

internal class LedgerBackupException(message: String) : Exception(message)

internal object LedgerBackupCodec {
    fun encode(transactions: List<LedgerTransaction>, exportedAt: Long = System.currentTimeMillis()): String {
        val items = JSONArray()
        transactions.sortedBy { it.timestamp }.forEach { transaction ->
            items.put(
                JSONObject()
                    .put("type", transaction.type)
                    .put("amountCents", transaction.amountCents)
                    .put("category", transaction.category)
                    .put("account", transaction.account)
                    .put("targetAccount", transaction.targetAccount)
                    .put("note", transaction.note)
                    .put("timestamp", transaction.timestamp),
            )
        }
        return JSONObject()
            .put("format", LedgerBackupFormat)
            .put("version", LedgerBackupVersion)
            .put("exportedAt", exportedAt)
            .put("transactions", items)
            .toString(2)
    }

    fun decode(text: String): LedgerBackup {
        val root = runCatching { JSONObject(text) }
            .getOrElse { throw LedgerBackupException("文件不是有效的简记账单") }
        if (root.optString("format") != LedgerBackupFormat) {
            throw LedgerBackupException("文件不是简记导出的账单")
        }
        val version = root.optInt("version", -1)
        if (version !in 1..LedgerBackupVersion) {
            throw LedgerBackupException("账单文件版本过新，请先升级简记")
        }
        val array = root.optJSONArray("transactions")
            ?: throw LedgerBackupException("账单文件缺少交易数据")
        if (array.length() > MaxBackupTransactions) {
            throw LedgerBackupException("账单数量超过 $MaxBackupTransactions 笔，无法导入")
        }

        val transactions = buildList {
            repeat(array.length()) { index ->
                val item = array.optJSONObject(index)
                    ?: throw LedgerBackupException("第 ${index + 1} 笔账单格式错误")
                add(item.toLedgerTransaction(index))
            }
        }
        return LedgerBackup(
            transactions = transactions,
            exportedAt = root.optLong("exportedAt", 0L),
        )
    }

    private fun JSONObject.toLedgerTransaction(index: Int): LedgerTransaction {
        val number = index + 1
        val type = optString("type").trim()
        if (type !in setOf("支出", "收入", "转账")) {
            throw LedgerBackupException("第 $number 笔账单类型无效")
        }
        val amountCents = optLong("amountCents", 0L)
        if (amountCents <= 0L) throw LedgerBackupException("第 $number 笔账单金额无效")
        val timestamp = optLong("timestamp", 0L)
        if (timestamp <= 0L) throw LedgerBackupException("第 $number 笔账单日期无效")

        return LedgerTransaction(
            type = type,
            amountCents = amountCents,
            category = requiredText("category", number, 40),
            account = requiredText("account", number, 40),
            targetAccount = optionalText("targetAccount", 40),
            note = optionalText("note", 500),
            timestamp = timestamp,
        )
    }

    private fun JSONObject.requiredText(key: String, number: Int, maxLength: Int): String {
        val value = optionalText(key, maxLength)
        if (value.isBlank()) throw LedgerBackupException("第 $number 笔账单缺少 $key")
        return value
    }

    private fun JSONObject.optionalText(key: String, maxLength: Int): String {
        val value = optString(key).trim()
        if (value.length > maxLength) throw LedgerBackupException("账单中的 $key 内容过长")
        return value
    }
}

internal fun filterNewTransactions(
    incoming: List<LedgerTransaction>,
    existing: List<LedgerTransaction>,
): List<LedgerTransaction> {
    val known = existing.mapTo(mutableSetOf(), LedgerTransaction::contentKey)
    return incoming.filter { transaction -> known.add(transaction.contentKey()) }
}

private fun LedgerTransaction.contentKey(): String = listOf(
    type,
    amountCents.toString(),
    category,
    account,
    targetAccount,
    note,
    timestamp.toString(),
).joinToString("\u001F")

internal fun readLedgerBackup(context: Context, source: Uri): LedgerBackup {
    val declaredLength = context.contentResolver.openAssetFileDescriptor(source, "r")?.use { it.length } ?: -1L
    if (declaredLength > MaxBackupBytes) throw LedgerBackupException("账单文件超过 20 MB，无法导入")
    val bytes = context.contentResolver.openInputStream(source)?.use { input ->
        val buffer = ByteArray(8 * 1024)
        val output = java.io.ByteArrayOutputStream()
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
            if (output.size() > MaxBackupBytes) throw LedgerBackupException("账单文件超过 20 MB，无法导入")
        }
        output.toByteArray()
    } ?: throw LedgerBackupException("无法读取账单文件")
    return LedgerBackupCodec.decode(String(bytes, StandardCharsets.UTF_8))
}

internal fun writeLedgerBackup(context: Context, destination: Uri, transactions: List<LedgerTransaction>) {
    val content = LedgerBackupCodec.encode(transactions)
    context.contentResolver.openOutputStream(destination, "wt")?.use { output ->
        output.write(content.toByteArray(StandardCharsets.UTF_8))
    } ?: throw LedgerBackupException("无法写入账单文件")
}

internal fun createSharedLedgerBackup(context: Context, transactions: List<LedgerTransaction>): Uri {
    val directory = File(context.cacheDir, "shared_backups").apply { mkdirs() }
    val file = File(directory, "简记账单.${LedgerBackupExtension}")
    file.writeText(LedgerBackupCodec.encode(transactions), StandardCharsets.UTF_8)
    return FileProvider.getUriForFile(context, "${context.packageName}.ledger-files", file)
}

internal fun suggestedBackupName(): String {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    return "简记账单-$stamp.$LedgerBackupExtension"
}

class LedgerFileProvider : FileProvider() {
    override fun getType(uri: Uri): String =
        if (uri.lastPathSegment?.endsWith(".$LedgerBackupExtension", ignoreCase = true) == true) {
            LedgerBackupMimeType
        } else {
            super.getType(uri) ?: "application/octet-stream"
        }
}
