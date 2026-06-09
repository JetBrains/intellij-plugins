package com.example.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppTest {
    private val app = App()

    @Test
    fun testWelcome() {
        assertEquals("Hello, World from app", app.welcome("World"))
    }

    // Note: farewell() is intentionally not tested to demonstrate partial coverage.
}
