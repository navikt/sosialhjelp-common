package no.nav.sosialhjelp.selftest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class SelftestService {

    fun getSelftest(appName: String, version: String, dependencyChecks: List<DependencyCheck>): SelftestResult {
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

    private suspend fun checkDependencies(dependencyChecks: List<DependencyCheck>): List<DependencyCheckResult> {
        return coroutineScope {
            dependencyChecks.map {
                withContext(Dispatchers.Default) { it.check() }
            }
        }
    }
}