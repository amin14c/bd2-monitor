package com.bd2monitor

import java.text.SimpleDateFormat
import java.util.*

object EarlyWarningEngine {

    // ═══════════════════════════════════════
    // نتيجة التحليل الكاملة
    // ═══════════════════════════════════════
    data class WarningResult(
        val riskScore: Int,           // 0-100
        val riskLevel: RiskLevel,
        val warningType: WarningType,
        val alerts: List<String>,
        val recommendations: List<String>,
        val baselineMood: Double,
        val baselineSleep: Double,
        val baselineEnergy: Double,
        val trendDetected: TrendType
    )

    enum class RiskLevel {
        STABLE,      // 0-30  🟢
        MONITOR,     // 31-60 🟡
        ALERT,       // 61-80 🟠
        CRITICAL     // 81-100 🔴
    }

    enum class WarningType {
        NONE,
        MANIA_RISK,
        DEPRESSION_RISK,
        MIXED_RISK,
        MEDICATION_RISK
    }

    enum class TrendType {
        NONE,
        RISING,      // ارتفاع مستمر → خطر هوس
        FALLING,     // انخفاض مستمر → خطر اكتئاب
        VOLATILE     // تذبذب حاد
    }

    // ═══════════════════════════════════════
    // الدالة الرئيسية
    // ═══════════════════════════════════════
    fun analyze(records: List<DailyRecord>): WarningResult {

        if (records.size < 3) {
            return WarningResult(
                riskScore = 0,
                riskLevel = RiskLevel.STABLE,
                warningType = WarningType.NONE,
                alerts = listOf("البيانات غير كافية للتحليل — يحتاج 3 أيام على الأقل"),
                recommendations = listOf("استمر في التسجيل اليومي"),
                baselineMood = 0.0,
                baselineSleep = 0.0,
                baselineEnergy = 0.0,
                trendDetected = TrendType.NONE
            )
        }

        // حساب الـ Baseline من أول 14 يوم
        val sorted = records.sortedBy { it.date }
        val baselineRecords = sorted.take(14)
        val recentRecords = sorted.takeLast(7)
        val last3 = sorted.takeLast(3)

        val baselineMood = baselineRecords.map { it.mood }.average()
        val baselineSleep = baselineRecords.map { it.sleepHours }.average()
        val baselineEnergy = baselineRecords.map { it.energy }.average()

        val alerts = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var totalScore = 0
        var warningType = WarningType.NONE

        // ═══════════════════════════════════
        // المؤشر 1 — انحراف المزاج (25 نقطة)
        // ═══════════════════════════════════
        val moodScore = analyzeMoodDeviation(last3, baselineMood, alerts)
        totalScore += moodScore

        // ═══════════════════════════════════
        // المؤشر 2 — طاقة/نوم غير متوازنَين (25 نقطة)
        // ═══════════════════════════════════
        val energySleepScore = analyzeEnergySleep(last3, alerts)
        totalScore += energySleepScore

        // ═══════════════════════════════════
        // المؤشر 3 — تذبذب حاد (25 نقطة)
        // ═══════════════════════════════════
        val volatilityScore = analyzeVolatility(recentRecords, alerts)
        totalScore += volatilityScore

        // ═══════════════════════════════════
        // المؤشر 4 — عدم انتظام الدواء (25 نقطة)
        // ═══════════════════════════════════
        val medicationScore = analyzeMedication(recentRecords, alerts)
        totalScore += medicationScore

        // ═══════════════════════════════════
        // كشف الاتجاه (Trend Detection)
        // ═══════════════════════════════════
        val trend = detectTrend(last3, baselineMood, alerts)

        // ═══════════════════════════════════
        // كشف توقيت التسجيل
        // ═══════════════════════════════════
        analyzeTimestamp(recentRecords, alerts)

        // ═══════════════════════════════════
        // تحديد نوع الخطر
        // ═══════════════════════════════════
        warningType = determineWarningType(last3, baselineMood, recentRecords)

        // ═══════════════════════════════════
        // توليد التوصيات
        // ═══════════════════════════════════
        generateRecommendations(warningType, trend, totalScore, recommendations)

        val riskLevel = when {
            totalScore <= 30 -> RiskLevel.STABLE
            totalScore <= 60 -> RiskLevel.MONITOR
            totalScore <= 80 -> RiskLevel.ALERT
            else -> RiskLevel.CRITICAL
        }

        return WarningResult(
            riskScore = totalScore.coerceIn(0, 100),
            riskLevel = riskLevel,
            warningType = warningType,
            alerts = alerts,
            recommendations = recommendations,
            baselineMood = baselineMood,
            baselineSleep = baselineSleep,
            baselineEnergy = baselineEnergy,
            trendDetected = trend
        )
    }

    // ═══════════════════════════════════════
    // المؤشر 1 — انحراف المزاج
    // ═══════════════════════════════════════
    private fun analyzeMoodDeviation(
        last3: List<DailyRecord>,
        baselineMood: Double,
        alerts: MutableList<String>
    ): Int {
        val highMoodDays = last3.count { it.mood > baselineMood + 2 }
        val lowMoodDays = last3.count { it.mood < baselineMood - 2 }

        return when {
            highMoodDays == 3 -> {
                alerts.add("🔴 مزاج مرتفع بشكل مستمر فوق الخط الأساسي لـ 3 أيام")
                25
            }
            lowMoodDays == 3 -> {
                alerts.add("🔴 مزاج منخفض بشكل مستمر تحت الخط الأساسي لـ 3 أيام")
                25
            }
            highMoodDays == 2 || lowMoodDays == 2 -> {
                alerts.add("🟠 انحراف في المزاج لـ يومين متتاليين")
                15
            }
            highMoodDays == 1 || lowMoodDays == 1 -> 5
            else -> 0
        }
    }

    // ═══════════════════════════════════════
    // المؤشر 2 — طاقة/نوم غير متوازنَين
    // ═══════════════════════════════════════
    private fun analyzeEnergySleep(
        last3: List<DailyRecord>,
        alerts: MutableList<String>
    ): Int {
        val imbalanceDays = last3.count { it.energy >= 7 && it.sleepHours < 5 }
        val lowEnergyHighSleep = last3.count { it.energy <= 3 && it.sleepHours > 10 }

        return when {
            imbalanceDays == 3 -> {
                alerts.add("🔴 طاقة مفرطة مع نوم منخفض لـ 3 أيام — مؤشر هوس قوي")
                25
            }
            lowEnergyHighSleep == 3 -> {
                alerts.add("🔴 طاقة منخفضة مع نوم مفرط لـ 3 أيام — مؤشر اكتئاب قوي")
                25
            }
            imbalanceDays == 2 -> {
                alerts.add("🟠 طاقة/نوم غير متوازنَين لـ يومين")
                15
            }
            lowEnergyHighSleep == 2 -> {
                alerts.add("🟠 طاقة منخفضة ونوم مفرط لـ يومين")
                15
            }
            else -> 0
        }
    }

    // ═══════════════════════════════════════
    // المؤشر 3 — تذبذب حاد
    // ═══════════════════════════════════════
    private fun analyzeVolatility(
        recentRecords: List<DailyRecord>,
        alerts: MutableList<String>
    ): Int {
        if (recentRecords.size < 2) return 0

        var volatileCount = 0
        for (i in 1 until recentRecords.size) {
            val diff = Math.abs(recentRecords[i].mood - recentRecords[i - 1].mood)
            if (diff >= 3) volatileCount++
        }

        return when {
            volatileCount >= 3 -> {
                alerts.add("🔴 تذبذب حاد في المزاج (≥3 نقاط) لأكثر من 3 مرات — مؤشر مختلط")
                25
            }
            volatileCount == 2 -> {
                alerts.add("🟠 تذبذب ملحوظ في المزاج")
                15
            }
            volatileCount == 1 -> 5
            else -> 0
        }
    }

    // ═══════════════════════════════════════
    // المؤشر 4 — عدم انتظام الدواء
    // ═══════════════════════════════════════
    private fun analyzeMedication(
        recentRecords: List<DailyRecord>,
        alerts: MutableList<String>
    ): Int {
        val missedDays = recentRecords.count { !it.medicationTaken }

        return when {
            missedDays >= 3 -> {
                alerts.add("🔴 فوّت الدواء $missedDays أيام من آخر 7 — خطر انتكاسة")
                25
            }
            missedDays == 2 -> {
                alerts.add("🟡 فوّت الدواء يومين من آخر 7")
                15
            }
            missedDays == 1 -> {
                alerts.add("🟡 فوّت الدواء يوماً واحداً")
                5
            }
            else -> 0
        }
    }

    // ═══════════════════════════════════════
    // كشف الاتجاه (Trend Detection)
    // ═══════════════════════════════════════
    private fun detectTrend(
        last3: List<DailyRecord>,
        baselineMood: Double,
        alerts: MutableList<String>
    ): TrendType {
        if (last3.size < 3) return TrendType.NONE

        val rising = last3[0].mood < last3[1].mood && last3[1].mood < last3[2].mood
        val falling = last3[0].mood > last3[1].mood && last3[1].mood > last3[2].mood
        val dailyChange = last3.zipWithNext { a, b -> Math.abs(b.mood - a.mood) }
        val volatile = dailyChange.all { it >= 2 }

        return when {
            rising && last3[2].mood > baselineMood + 1 -> {
                alerts.add("📈 اتجاه تصاعدي مستمر في المزاج — إنذار مبكر بهوس محتمل")
                TrendType.RISING
            }
            falling && last3[2].mood < baselineMood - 1 -> {
                alerts.add("📉 اتجاه تنازلي مستمر في المزاج — إنذار مبكر باكتئاب محتمل")
                TrendType.FALLING
            }
            volatile -> {
                alerts.add("〰️ تذبذب يومي مستمر في المزاج")
                TrendType.VOLATILE
            }
            else -> TrendType.NONE
        }
    }

    // ═══════════════════════════════════════
    // تحليل توقيت التسجيل
    // ═══════════════════════════════════════
    private fun analyzeTimestamp(
        recentRecords: List<DailyRecord>,
        alerts: MutableList<String>
    ) {
        val sdf = SimpleDateFormat("HH", Locale.getDefault())
        val lateNightEntries = recentRecords.count { record ->
            try {
                val hour = sdf.format(Date(record.timestamp)).toInt()
                hour in 1..4
            } catch (e: Exception) { false }
        }

        val missedDays = recentRecords.size < 5

        when {
            lateNightEntries >= 2 ->
                alerts.add("🌙 تسجيل متأخر جداً (1-4 صباحاً) لـ $lateNightEntries أيام — مؤشر أرق محتمل")
            missedDays ->
                alerts.add("⚠️ انقطاع في التسجيل اليومي — قد يشير لاكتئاب أو عدم التزام")
        }
    }

    // ═══════════════════════════════════════
    // تحديد نوع الخطر
    // ═══════════════════════════════════════
    private fun determineWarningType(
        last3: List<DailyRecord>,
        baselineMood: Double,
        recentRecords: List<DailyRecord>
    ): WarningType {
        val highMood = last3.count { it.mood > baselineMood + 2 }
        val lowMood = last3.count { it.mood < baselineMood - 2 }
        val missedMeds = recentRecords.count { !it.medicationTaken }
        val maniaSign = last3.count { it.energy >= 7 && it.sleepHours < 5 }

        return when {
            highMood >= 2 || maniaSign >= 2 -> WarningType.MANIA_RISK
            lowMood >= 2 -> WarningType.DEPRESSION_RISK
            highMood >= 1 && lowMood >= 1 -> WarningType.MIXED_RISK
            missedMeds >= 3 -> WarningType.MEDICATION_RISK
            else -> WarningType.NONE
        }
    }

    // ═══════════════════════════════════════
    // توليد التوصيات
    // ═══════════════════════════════════════
    private fun generateRecommendations(
        warningType: WarningType,
        trend: TrendType,
        score: Int,
        recommendations: MutableList<String>
    ) {
        when (warningType) {
            WarningType.MANIA_RISK -> {
                recommendations.add("مراجعة جرعة مثبتات المزاج")
                recommendations.add("تقييم النوم وتطبيق نظافة النوم")
                recommendations.add("تجنب المنبهات والكافيين")
                recommendations.add("تواصل مع الطبيب خلال 24 ساعة")
            }
            WarningType.DEPRESSION_RISK -> {
                recommendations.add("تقييم خطر إيذاء النفس")
                recommendations.add("تشجيع النشاط البدني المنتظم")
                recommendations.add("مراجعة فاعلية مضادات الاكتئاب")
                recommendations.add("جدولة متابعة عاجلة")
            }
            WarningType.MIXED_RISK -> {
                recommendations.add("تقييم نوبة مختلطة")
                recommendations.add("مراجعة بروتوكول العلاج الكامل")
                recommendations.add("مراقبة مكثفة يومية")
            }
            WarningType.MEDICATION_RISK -> {
                recommendations.add("مناقشة أسباب عدم الالتزام بالدواء")
                recommendations.add("تبسيط نظام الجرعات")
                recommendations.add("تفعيل التذكير اليومي")
            }
            WarningType.NONE -> {
                if (score > 30)
                    recommendations.add("متابعة دورية — الوضع تحت المراقبة")
                else
                    recommendations.add("✅ الحالة مستقرة — استمر في التسجيل المنتظم")
            }
        }

        if (trend == TrendType.RISING)
            recommendations.add("📈 الاتجاه تصاعدي — تدخل استباقي قبل بلوغ الذروة")
        if (trend == TrendType.FALLING)
            recommendations.add("📉 الاتجاه تنازلي — تدخل استباقي قبل تعمق الاكتئاب")
    }
}
