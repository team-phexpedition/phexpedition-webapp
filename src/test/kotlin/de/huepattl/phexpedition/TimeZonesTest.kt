package de.huepattl.phexpedition

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TimeZonesTest {

    @Test
    fun notNullOrEmpty() {
        val list = TimeZones.list()

        assertNotNull(list)
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun oneKnownTimeZonePresent() {
        val list = TimeZones.list()

        assertNotNull(list.find { it.id == "Europe/Berlin" })
    }

    @Test
    fun sortingAtoZ() {
        val list = TimeZones.list()

        assertTrue(list.first().id.startsWith("A"))
        assertTrue(list.last().id.startsWith("Z"))
    }

}
