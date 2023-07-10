package com.intellij.dts.parsing

import com.intellij.testFramework.PlatformTestUtil

class DtsPerformanceParsingTest : DtsParsingTestBase("", "dtsi") {
    fun testPerformance() {
        val file = loadFile("Performance.dts").repeat(10)

        PlatformTestUtil.startPerformanceTest("parser", 1000) {
            val root = createFile("file.dts", file)
            ensureParsed(root)
        }.assertTiming()
    }
}