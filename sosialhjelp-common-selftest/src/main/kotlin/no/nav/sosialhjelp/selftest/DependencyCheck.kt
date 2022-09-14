package no.nav.sosialhjelp.selftest

import org.slf4j.LoggerFactory
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
            val errorMessage = "Call to dependency=$name with type=$type at url=$address timed out or circuitbreaker was tripped. ${t.message}"
            if (importance == Importance.CRITICAL) {
                log.error(errorMessage, t)
            } else {
                log.warn(errorMessage, t)
            }
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
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
