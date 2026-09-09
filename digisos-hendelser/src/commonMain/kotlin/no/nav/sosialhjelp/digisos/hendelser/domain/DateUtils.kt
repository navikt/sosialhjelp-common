package no.nav.sosialhjelp.digisos.hendelser.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal expect val OSLO: TimeZone

/**
 * Parse an ISO-8601 timestamp string (e.g. "2024-03-15T13:37:00.134Z") to [Instant].
 * Returns [Instant.DISTANT_PAST] on parse failure to ensure safe sort ordering.
 */
fun String.toInstant(): Instant = runCatching { Instant.parse(this) }.getOrElse { Instant.DISTANT_PAST }

/**
 * Parse a date string to [LocalDate] in Europe/Oslo timezone.
 * Accepts both "YYYY-MM-DD" and full ISO-8601 date-time strings.
 */
fun String.toLocalDate(): LocalDate =
    if (this.length > 10) {
        Instant.parse(this).toLocalDateTime(OSLO).date
    } else {
        LocalDate.parse(this)
    }

/** Convert a Unix epoch-millisecond timestamp to [Instant]. */
fun unixToInstant(epochMillis: Long): Instant = Instant.fromEpochMilliseconds(epochMillis)

/** Convert an [Instant] to [LocalDate] in Europe/Oslo. */
fun Instant.toLocalDateOslo(): LocalDate = toLocalDateTime(OSLO).date

/** Strip " kommune" suffix from Nav-enhet names for display. */
fun stripEnhetsnavnForKommune(navn: String?): String? = navn?.replace(" kommune", "")

/**
 * Format a [Double] amount as a string, always including a decimal point (e.g. "5000.0").
 *
 * On the JVM, [Double.toString] already includes a decimal point for whole numbers
 * (e.g. `5000.0.toString() == "5000.0"`). On Kotlin/JS, [Double] is backed by a plain
 * JS number, so [Double.toString] omits the decimal point for whole numbers
 * (`5000.0.toString() == "5000"`). This normalizes both platforms to the JVM behavior.
 */
fun Double.toBelopString(): String {
    val s = toString()
    return if ('.' in s || 'e' in s || 'E' in s || s == "NaN" || s == "Infinity" || s == "-Infinity") {
        s
    } else {
        "$s.0"
    }
}
