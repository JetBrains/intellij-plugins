package com.intellij.dts.parsing

import com.intellij.tools.ide.metrics.benchmark.Benchmark

class DtsPerformanceParsingTest : DtsParsingTestBase("") {
  fun testPerformance() {
    val file = loadFile("Performance.dts")

    Benchmark.newBenchmark("parser") {
      val root = createFile("file.dts", file)
      ensureParsed(root)
    }.start()
  }
}