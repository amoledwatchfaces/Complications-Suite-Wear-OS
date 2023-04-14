package com.weartools.sunkalc

/**
 * Represents the moon phase
 * @property fraction illuminated fraction of the moon; varies from 0.0 (new moon) to 1.0 (full moon)
 * @property phase moon phase; varies from 0.0 to 1.0, described below
 * @property angle midpoint angle in radians of the illuminated limb of the moon reckoned eastward from the north point of the disk; the moon is waxing if the angle is negative, and waning if positive
 */
data class MoonPhaseInfo(
    val fraction: Double,
    val phase: Double,
    val angle: Double,
    val phaseName: MoonPhase,
    val emoji: String
)