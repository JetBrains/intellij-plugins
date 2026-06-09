package com.example.app

import com.example.lib.Library

class App(private val library: Library = Library()) {
    fun welcome(name: String): String {
        return library.greeting(name) + " from app"
    }

    // Intentionally not exercised by tests -> partial coverage.
    fun farewell(name: String): String {
        return "Goodbye, $name"
    }
}
