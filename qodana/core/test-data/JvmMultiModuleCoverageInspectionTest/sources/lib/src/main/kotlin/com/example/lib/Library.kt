package com.example.lib

class Library {
    fun greeting(name: String): String {
        return "Hello, $name"
    }

    // Intentionally not exercised by tests -> partial coverage.
    fun shout(text: String): String {
        return text.uppercase() + "!"
    }
}
