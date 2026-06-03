package com.example.math

class MathUtil {

    fun add(a: Int, b: Int): Int = a + b

    fun subtract(a: Int, b: Int): Int = a - b

    fun multiply(a: Int, b: Int): Int = a * b

    fun divide(a: Int, b: Int): Int {
        require(b != 0) { "division by zero" }
        return a / b
    }

    fun isEven(value: Int): Boolean = value % 2 == 0
}
