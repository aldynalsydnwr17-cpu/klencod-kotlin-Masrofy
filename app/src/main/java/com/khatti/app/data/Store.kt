package com.khatti.app.data

import android.content.Context
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

class Store(private val ctx: Context) {
    private val sp = ctx.getSharedPreferences("khatti", Context.MODE_PRIVATE)

    var drillIndex: Int
        get() = sp.getInt("drillIndex", 0) % DRILLS.size
        set(v) { sp.edit().putInt("drillIndex", v % DRILLS.size).apply() }

    fun streak(): Int {
        val last = sp.getString("last", null)?.let { LocalDate.parse(it) } ?: return 0
        val today = LocalDate.now()
        return if (last == today || last == today.minusDays(1)) sp.getInt("streak", 0) else 0
    }

    fun recordPractice(failedChecks: Boolean, letter: String) {
        val today = LocalDate.now()
        val last = sp.getString("last", null)?.let { LocalDate.parse(it) }
        val streak = when (last) {
            today -> sp.getInt("streak", 1)
            today.minusDays(1) -> sp.getInt("streak", 0) + 1
            else -> 1
        }
        val best = maxOf(streak, sp.getInt("best", 0))
        val e = sp.edit().putString("last", today.toString()).putInt("streak", streak).putInt("best", best)
        e.putInt("d_$today", sp.getInt("d_$today", 0) + 1)
        if (failedChecks) e.putInt("w_$letter", sp.getInt("w_$letter", 0) + 1)
        e.apply()
    }

    fun bestStreak() = sp.getInt("best", 0)

    fun dayCount(d: LocalDate) = sp.getInt("d_$d", 0)

    fun reset() {
        sp.edit().clear().apply()
        photosDir().listFiles()?.forEach { it.delete() }
    }

    /** عدد التدريبات لكل يوم من السبت للجمعة في الأسبوع الحالي */
    fun weekCounts(): List<Int> {
        val today = LocalDate.now()
        var start = today
        while (start.dayOfWeek != DayOfWeek.SATURDAY) start = start.minusDays(1)
        return (0..6).map { sp.getInt("d_${start.plusDays(it.toLong())}", 0) }
    }

    fun weakLetters(): List<String> =
        DRILLS.map { it.letter to sp.getInt("w_${it.letter}", 0) }
            .filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

    fun photosDir(): File = File(ctx.filesDir, "photos").apply { mkdirs() }

    fun newPhotoFile(): File = File(photosDir(), "p_${System.currentTimeMillis()}.jpg")

    /** الصور من الأقدم للأحدث */
    fun photos(): List<File> = photosDir().listFiles { f -> f.extension == "jpg" }?.sortedBy { it.name } ?: emptyList()
}
