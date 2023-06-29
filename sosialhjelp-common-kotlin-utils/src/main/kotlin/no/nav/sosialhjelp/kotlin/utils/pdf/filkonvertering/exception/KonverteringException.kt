package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception

abstract class FilKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class WordKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : FilKonverteringException(message, cause)

class CsvKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : FilKonverteringException(message, cause)

class ExcelKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : FilKonverteringException(message, cause)
