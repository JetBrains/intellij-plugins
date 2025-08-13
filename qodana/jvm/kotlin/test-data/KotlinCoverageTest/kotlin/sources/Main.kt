package org.example

fun answer() = 42

class Foo(val a: Int)

class Bar(val a: Int) {
    constructor(a: Int, b: Int): this(a)
}

class Baz {
    companion object {
        val a: Int
        var b: Int = 1
        init {
            if (b == 1) {
                a = 42
            } else {
                a = 1
            }
        }
    }
}

data class Fellow(val a: Int, val b: String)