package no.nav.sosialhjelp.api.fiks.exceptions

class FiksException(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class FiksClientException(
    val status: Int,
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class FiksServerException(
    val status: Int,
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class FiksNotFoundException(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)
