package com.example.simpleledger

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LedgerBackupInstrumentedTest {
    @Test
    fun backupRoundTripPreservesEveryTransactionField() {
        val original = LedgerTransaction(
            type = "转账",
            amountCents = 12_345,
            category = "账户互转",
            account = "微信",
            targetAccount = "支付宝",
            note = "跨设备迁移测试",
            timestamp = 1_722_188_800_000,
        )

        val decoded = LedgerBackupCodec.decode(LedgerBackupCodec.encode(listOf(original), exportedAt = 123L))

        assertEquals(123L, decoded.exportedAt)
        assertEquals(listOf(original), decoded.transactions)
    }

    @Test
    fun unrelatedJsonIsRejected() {
        assertThrows(LedgerBackupException::class.java) {
            LedgerBackupCodec.decode("{\"format\":\"another-app\",\"version\":1,\"transactions\":[]}")
        }
    }
}
