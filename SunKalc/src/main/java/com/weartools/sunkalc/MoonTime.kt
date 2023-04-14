package com.weartools.sunkalc

import java.time.LocalDateTime


/**
 * Represents the moon time
 * @property rise moonrise time as {@link java.time.LocalDateTime}
 * @property set moonset time as {@link java.time.LocalDateTime}
 * @property alwaysUp true if the moon never rises/sets and is always above the horizon during the day
 * @property alwaysDown true if the moon is always below the horizon
 */
data class MoonTime(
    val rise: LocalDateTime,
    val set: LocalDateTime,
    val alwaysUp: Boolean,
    val alwaysDown: Boolean
)