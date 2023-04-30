package com.intellij.dts.util

object DtsUtil {
    // Splits the name of a node into node and unit address part. If the name
    // does not contain a unit address null will be returned.
    fun splitName(name: String): Pair<String?, String?> {
        return if (name.contains("@")) {
            val result = name.split("@", limit = 2)
            val actualName = result[0]
            val unitAddress = result[1]

            if (actualName.isEmpty()) {
                Pair(null, unitAddress)
            } else {
                Pair(actualName, unitAddress)
            }
        } else {
            Pair(name, null)
        }
    }
}