// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.CharsetToolkit
import org.junit.Test
import com.intellij.grazie.GraziTestBase
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarCheckerPerformanceTest : GraziTestBase(true) {
  @Test
  fun `test performance for 10 sonnets`() {
    val text = FileUtil.loadFile(File(testDataPath, "grammar/sonnet_10.txt"), CharsetToolkit.UTF8_CHARSET)
    val tokens = plain(text.split("\n").map { it + "\n" })
    var fixes: List<Typo> = emptyList()
    val totalTime = measureTimeMillis {
      fixes = GrammarChecker.default.check(tokens).toList()
    }
    fixes.forEach { it.verify(text) }
    assert(fixes.size < 50)
    assert(totalTime < 10_000)
  }

  @Test
  fun `test performance for 50 sonnets`() {
    val text = FileUtil.loadFile(File(testDataPath, "grammar/sonnet_50.txt"), CharsetToolkit.UTF8_CHARSET)
    val tokens = plain(text.split("\n").map { it + "\n" })
    var fixes: List<Typo> = emptyList()
    val totalTime = measureTimeMillis {
      fixes = GrammarChecker.default.check(tokens).toList()
    }
    fixes.forEach { it.verify(text) }
    assert(fixes.size < 500)
    assert(totalTime < 100_000)
  }
}