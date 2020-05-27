package no.nav.sosialhjelp.selftest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SelftestServiceTest {

    private val service = SelftestService()

    private val appName = "app"
    private val version = "1.0"

    @Test
    internal fun `result OK - ingen checks feiler`() {
        val result = service.getSelftest(appName, version, listOf(A()))

        assertEquals(Result.OK, result.result)
    }

    @Test
    internal fun `result WARNING - ikke critical check feiler`() {
        val result = service.getSelftest(appName, version, listOf(A(), B()))

        assertEquals(Result.WARNING, result.result)
    }

    @Test
    internal fun `result ERROR - critical check feiler`() {
        val result = service.getSelftest(appName, version, listOf(A(), B(), C()))

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