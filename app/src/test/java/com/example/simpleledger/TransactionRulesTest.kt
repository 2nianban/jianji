package com.example.simpleledger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import androidx.compose.ui.geometry.Offset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TransactionRulesTest {
    @Test
    fun transactionTimestampCombinesSelectedDateWithCurrentTime() {
        val zoneId = ZoneId.of("Asia/Shanghai")
        val now = LocalDateTime.of(2026, 8, 1, 19, 35, 27)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        val result = transactionTimestamp(LocalDate.of(2026, 7, 28), now, zoneId)

        val expected = LocalDateTime.of(2026, 7, 28, 19, 35, 27)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
        assertEquals(expected, result)
    }

    @Test
    fun recentlyDeletedWindowIncludesExactlyNowAndExcludesTwentyFourHours() {
        val now = 1_800_000_000_000L

        assertEquals(true, isRecentlyDeleted(now, now))
        assertEquals(true, isRecentlyDeleted(now - RecentlyDeletedRetentionMillis + 1, now))
        assertEquals(false, isRecentlyDeleted(now - RecentlyDeletedRetentionMillis, now))
        assertEquals(false, isRecentlyDeleted(now + 1, now))
        assertEquals(false, isRecentlyDeleted(null, now))
        assertEquals(now - RecentlyDeletedRetentionMillis, recentlyDeletedCutoff(now))
    }

    @Test
    fun transactionStoresPositiveAmountInCents() {
        val transaction = LedgerTransaction(
            type = "支出",
            amountCents = 3280,
            category = "餐饮",
            account = "现金",
            targetAccount = "",
            note = "午餐",
            timestamp = 0,
        )

        assertEquals(3280L, transaction.amountCents)
        assertEquals("支出", transaction.type)
    }

    @Test
    fun decimalAmountsAreConvertedToExactCents() {
        assertEquals(1689L, parseAmountToCents("16.89"))
        assertEquals(1690L, parseAmountToCents("16.9"))
        assertEquals(1690L, parseAmountToCents("16,90"))
        assertNull(parseAmountToCents("16.899"))
        assertNull(parseAmountToCents("0"))
    }

    @Test
    fun amountInputAllowsOneSeparatorAndTwoDecimalPlaces() {
        assertEquals("16.89", sanitizeAmountInput("16.899"))
        assertEquals("16.9", sanitizeAmountInput("16,9"))
        assertEquals("0.89", sanitizeAmountInput(".89"))
        assertEquals("16.89", amountCentsToInput(1689L))
        assertEquals("16.9", amountCentsToInput(1690L))
    }

    @Test
    fun dayStatsUseTodayAndAppearBeforeWeek() {
        val today = LocalDate.of(2026, 7, 28)

        val range = statsDateRange(StatsPeriod.DAY, today, LocalDate.MIN, LocalDate.MIN)

        assertEquals(today, range.start)
        assertEquals(today, range.endInclusive)
        assertEquals("2026-07-28", range.label)
        assertEquals(listOf(StatsPeriod.DAY, StatsPeriod.WEEK), StatsPeriod.values().take(2))
    }

    @Test
    fun weekStatsRunFromMondayThroughSunday() {
        val range = statsDateRange(
            StatsPeriod.WEEK,
            LocalDate.of(2026, 7, 28),
            LocalDate.MIN,
            LocalDate.MIN,
        )

        assertEquals(LocalDate.of(2026, 7, 27), range.start)
        assertEquals(LocalDate.of(2026, 8, 2), range.endInclusive)
    }

    @Test
    fun monthAndYearStatsUseCalendarBoundaries() {
        val today = LocalDate.of(2026, 7, 28)

        val month = statsDateRange(StatsPeriod.MONTH, today, LocalDate.MIN, LocalDate.MIN)
        val year = statsDateRange(StatsPeriod.YEAR, today, LocalDate.MIN, LocalDate.MIN)

        assertEquals(LocalDate.of(2026, 7, 1), month.start)
        assertEquals(LocalDate.of(2026, 7, 31), month.endInclusive)
        assertEquals(LocalDate.of(2026, 1, 1), year.start)
        assertEquals(LocalDate.of(2026, 12, 31), year.endInclusive)
        assertEquals("2026-07-01 ~ 07-31", statsRangeDisplayLabel(month))
        assertEquals("2026-01-01 ~ 12-31", statsRangeDisplayLabel(year))
    }

    @Test
    fun customStatsNormalizeReversedDates() {
        val range = statsDateRange(
            StatsPeriod.CUSTOM,
            LocalDate.of(2026, 7, 28),
            LocalDate.of(2026, 7, 20),
            LocalDate.of(2026, 7, 5),
        )

        assertEquals(LocalDate.of(2026, 7, 5), range.start)
        assertEquals(LocalDate.of(2026, 7, 20), range.endInclusive)
    }

    @Test
    fun statsRangeLabelRepeatsYearOnlyWhenRangeCrossesYears() {
        val crossYear = StatsDateRange(
            LocalDate.of(2025, 12, 29),
            LocalDate.of(2026, 1, 4),
            "",
        )
        assertEquals("2025-12-29 ~ 2026-01-04", statsRangeDisplayLabel(crossYear))
    }

    @Test
    fun historicalStatsCanMoveByCompletePeriods() {
        val monthEnd = LocalDate.of(2026, 1, 31)
        assertEquals(
            LocalDate.of(2025, 12, 1),
            shiftStatsAnchor(StatsPeriod.MONTH, monthEnd, -1),
        )
        assertEquals(
            LocalDate.of(2026, 2, 1),
            shiftStatsAnchor(StatsPeriod.MONTH, monthEnd, 1),
        )

        val weekAnchor = LocalDate.of(2026, 7, 29)
        assertEquals(
            LocalDate.of(2026, 7, 20),
            shiftStatsAnchor(StatsPeriod.WEEK, weekAnchor, -1),
        )
        assertEquals(
            LocalDate.of(2026, 8, 3),
            shiftStatsAnchor(StatsPeriod.WEEK, weekAnchor, 1),
        )
    }

    @Test
    fun pieLongPressHitTestingFindsTheExpectedSlice() {
        val items = listOf("餐饮" to 25L, "交通" to 75L)
        val width = 1000f
        val height = 800f
        val geometry = pieGeometry(width, height)

        val firstSlice = Offset(geometry.center.x, geometry.center.y - geometry.radiusY * 0.6f)
        val secondSlice = Offset(geometry.center.x, geometry.center.y + geometry.radiusY * 0.6f)
        val outside = Offset(0f, 0f)

        assertEquals(0, findPieSlice(firstSlice, width, height, items))
        assertEquals(1, findPieSlice(secondSlice, width, height, items))
        assertNull(findPieSlice(outside, width, height, items))
    }

    @Test
    fun onlyCustomOptionsCanBeRemoved() {
        val defaults = listOf("现金", "微信")
        val options = defaults + "旅行卡"

        assertEquals(defaults, removeCustomOption(options, defaults, "旅行卡"))
        assertEquals(options, removeCustomOption(options, defaults, "微信"))
    }

    @Test
    fun backgroundOverlayKeepsTheImageVisible() {
        assertEquals(0.80f, normalizeBackgroundOverlay(0.95f))
        assertEquals(0.15f, normalizeBackgroundOverlay(0.05f))
        assertEquals(0.50f, normalizeBackgroundOverlay(0.50f))
    }

    @Test
    fun noteNormalizationTrimsWhitespaceAndLimitsStoredLength() {
        assertEquals("午餐", normalizeNote("  午餐  "))
        assertEquals(NoteMaxLength, normalizeNote("备".repeat(NoteMaxLength + 1)).length)
    }

    @Test
    fun importingTheSameBackupTwiceDoesNotDuplicateTransactions() {
        val transaction = LedgerTransaction(
            type = "支出",
            amountCents = 2680,
            category = "餐饮",
            account = "微信",
            note = "晚餐",
            timestamp = 1_722_188_800_000,
        )

        assertEquals(listOf(transaction), filterNewTransactions(listOf(transaction, transaction), emptyList()))
        assertEquals(emptyList<LedgerTransaction>(), filterNewTransactions(listOf(transaction), listOf(transaction)))
    }
}
