package com.example.lib

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LibraryTest {
    private val library = Library()

    @Test
    fun testGreeting() {
        assertEquals("Hello, World", library.greeting("World"))
    }

    // Note: shout() is intentionally not tested to demonstrate partial coverage.
}
