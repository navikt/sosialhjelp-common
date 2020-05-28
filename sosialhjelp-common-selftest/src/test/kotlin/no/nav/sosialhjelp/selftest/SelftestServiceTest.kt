package no.nav.sosialhjelp.selftest

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class SelftestServiceTest {

    private val service = SelftestService()

    private val appName = "app"
    private val version = "1.0"

    @Test
    internal fun `result OK - ingen checks feiler`() {
        val result = service.getSelftest(appName, version, listOf(A()))

        result.result shouldBeEqualTo Result.OK
    }

    @Test
    internal fun `result WARNING - ikke critical check feiler`() {
        val result = service.getSelftest(appName, version, listOf(A(), B()))

        result.result shouldBeEqualTo Result.WARNING
    }

    @Test
    internal fun `result ERROR - critical check feiler`() {
        val result = service.getSelftest(appName, version, listOf(A(), B(), C()))

        result.result shouldBeEqualTo Result.ERROR
    }
}

internal class A : DependencyCheck(DependencyType.REST, "A", "A", Importance.WARNING) {
    override fun doCheck() {

    }
}

internal class B : DependencyCheck(DependencyType.REST, "B", "B", Importance.WARNING) {
    override fun doCheck() {
        throw RuntimeException("oops")
    }
}

internal class C : DependencyCheck(DependencyType.REST, "C", "C", Importance.CRITICAL) {
    override fun doCheck() {
        throw RuntimeException("oops")
    }
}