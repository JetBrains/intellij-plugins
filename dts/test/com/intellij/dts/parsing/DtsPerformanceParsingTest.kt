package com.intellij.dts.parsing

import com.intellij.tools.ide.metrics.benchmark.PerformanceTestUtil

class DtsPerformanceParsingTest : DtsParsingTestBase("") {
  fun testPerformance() {
    val file = loadFile("Performance.dts")

    PerformanceTestUtil.newPerformanceTest("parser") {
      val root = createFile("file.dts", file)
      ensureParsed(root)
    }.start()
  }
}