package no.nav.sosialhjelp.digisos.hendelser.domain

import kotlinx.datetime.TimeZone

// @js-joda/core (used internally by kotlinx-datetime on Kotlin/JS) ships with no IANA
// timezone database by default. Requiring @js-joda/timezone here registers the tz-database
// with js-joda's zone-rules provider before TimeZone.of("Europe/Oslo") is resolved.
@Suppress("UnsafeCastFromDynamic")
internal actual val OSLO: TimeZone =
    run {
        js("require('@js-joda/timezone')")
        TimeZone.of("Europe/Oslo")
    }
