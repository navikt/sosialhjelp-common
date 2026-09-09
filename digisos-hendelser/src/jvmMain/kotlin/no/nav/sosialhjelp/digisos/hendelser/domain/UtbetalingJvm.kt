package no.nav.sosialhjelp.digisos.hendelser.domain

import java.math.BigDecimal

/** JVM convenience — convert the platform-neutral [Utbetaling.belopString] to BigDecimal. */
val Utbetaling.belopAsBigDecimal: BigDecimal
    get() = belopString.toBigDecimalOrNull() ?: BigDecimal.ZERO
