package no.nav.sosialhjelp.selftest

import no.nav.sosialhjelp.kotlin.utils.logger
import java.util.concurrent.TimeoutException

interface DependencyCheck {

    val type: DependencyType
    val name: String
    val address: String
    val importance: Importance

    fun doCheck()

    fun check(): DependencyCheckResult {
        val startTime = System.currentTimeMillis()

        var throwable: Throwable? = null

        try {
            doCheck()
        } catch (t: Throwable) {
            log.warn("Call to dependency=$name with type=$type at url=$address timed out or circuitbreaker was tripped.", throwable)
            throwable = t
        }

        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime

        return DependencyCheckResult(
            endpoint = name,
            result = throwable?.let { if (importance == Importance.CRITICAL) Result.ERROR else Result.WARNING }
                ?: Result.OK,
            address = address,
            errorMessage = throwable?.let { "Call to dependency=$name timed out or circuitbreaker tripped. Errormessage=${getErrorMessageFromThrowable(it)}" },
            type = type,
            importance = importance,
            responseTime = "$responseTime ms",
            throwable = throwable
        )
    }

    private fun getErrorMessageFromThrowable(e: Throwable): String? {
        if (e is TimeoutException) {
            return "Call to dependency timed out by circuitbreaker"
        }
        return if (e.cause == null) e.message else e.cause!!.message
    }

    companion object {
        private val log by logger()
    }
}
