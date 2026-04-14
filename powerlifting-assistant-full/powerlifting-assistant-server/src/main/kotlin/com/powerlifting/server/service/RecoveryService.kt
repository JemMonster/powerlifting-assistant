package com.powerlifting.server.service

class RecoveryService {
    /**
     * Simple deterministic rules, easy to justify on defense.
     * Inputs are nullable; if missing, recommendation may be null.
     */
    fun makeRecommendation(
        sleepHours: Double?,
        wellbeing: Int?,
        fatigue: Int?,
        soreness: Int?
    ): String? {
        if (sleepHours == null && wellbeing == null && fatigue == null && soreness == null) return null

        val sh = sleepHours ?: 8.0
        val wb = wellbeing ?: 7
        val ft = fatigue ?: 5
        val sr = soreness ?: 5

        return when {
            sh < 4.0 || wb <= 3 -> "Настоятельно рекомендуется перенести тренировку. Восстановись и попробуй завтра."
            sh < 6.0 || ft >= 8 || sr >= 8 -> "Рекомендуется снизить нагрузку: уменьшить вес/объём, увеличить отдых, следить за техникой."
            else -> "Можно тренироваться по плану. Сохраняй технику и контролируй самочувствие."
        }
    }
}
