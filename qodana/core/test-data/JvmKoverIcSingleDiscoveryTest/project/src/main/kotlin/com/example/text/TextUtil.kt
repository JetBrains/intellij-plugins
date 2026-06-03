package com.example.text

class TextUtil {

    fun reverse(input: String): String = input.reversed()

    fun isPalindrome(input: String): Boolean = input == reverse(input)

    fun capitalize(input: String): String =
        if (input.isEmpty()) input else input[0].uppercase() + input.substring(1)

    fun countVowels(input: String): Int =
        input.count { it.lowercaseChar() in "aeiou" }
}
