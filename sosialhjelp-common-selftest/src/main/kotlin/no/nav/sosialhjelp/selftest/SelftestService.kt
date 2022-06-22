package no.nav.sosialhjelp.selftest

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SelftestService(
    private val appName: String,
    private val version: String,
    private val dependencyChecks: List<DependencyCheck>,
    meterRegistry: MeterRegistry
) {

    init {
        Gauge.builder("selftests_aggregate_result_status") { getAggregateResult() }
            .description("aggregert status for alle selftester. 0=ok, 1=kritisk feil, 2=ikke-kritisk feil")
            .register(meterRegistry)
    }

    fun getSelftest(): SelftestResult {
        val results = runBlocking { checkDependencies(dependencyChecks) }
        return SelftestResult(
            appName = appName,
            version = version,
            result = getOverallSelftestResult(results),
            dependencyCheckResults = results
        )
    }

    private fun getOverallSelftestResult(results: List<DependencyCheckResult>): Result {
        return when {
            results.any { it.result == Result.ERROR } -> Result.ERROR
            results.any { it.result == Result.WARNING } -> Result.WARNING
            else -> Result.OK
        }
    }

    private fun getAggregateResult(): Int {
        return when (getSelftest().result) {
            Result.OK -> 0
            Result.ERROR -> 1
            Result.WARNING -> 2
        }
    }

    private suspend fun checkDependencies(dependencyChecks: List<DependencyCheck>): List<DependencyCheckResult> {
        return coroutineScope {
            dependencyChecks.map {
                withContext(Dispatchers.Default) { it.check() }
            }
        }
    }
}
