package no.nav.sosialhjelp.selftest

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder

class SelftestMeterBinder(
    private val selftestService: SelftestService
) : MeterBinder {

    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder("selftests_aggregate_result_status") { getAggregateResult() }
            .description("aggregert status for alle selftester. 0=ok, 1=kritisk feil, 2=ikke-kritisk feil")
            .register(registry)
    }

    private fun getAggregateResult(): Int {
        return when (selftestService.getSelftest().result) {
            Result.OK -> 0
            Result.ERROR -> 1
            Result.WARNING -> 2
        }
    }
}
