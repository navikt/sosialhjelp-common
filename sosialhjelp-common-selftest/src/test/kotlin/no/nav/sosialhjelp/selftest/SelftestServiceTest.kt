package no.nav.sosialhjelp.selftest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SelftestServiceTest {

    private val appName = "app"
    private val version = "1.0"

    @Test
    internal fun `result OK - ingen checks feiler`() {
        val service = SelftestService(appName, version, listOf(A()))

        val result = service.getSelftest()

        assertEquals(Result.OK, result.result)
    }

    @Test
    internal fun `result WARNING - ikke critical check feiler`() {
        val service = SelftestService(appName, version, listOf(A(), B()))
        val result = service.getSelftest()

        assertEquals(Result.WARNING, result.result)
    }

    @Test
    internal fun `result ERROR - critical check feiler`() {
        val service = SelftestService(appName, version, listOf(A(), B(), C()))
        val result = service.getSelftest()

        assertEquals(Result.ERROR, result.result)
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