// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class PrismaFoldingTest : PrismaTestCase("folding") {
  @Test
  fun testComments() {
    doTest()
  }

  private fun doTest() {
    myFixture.testFolding("$testDataPath/${getTestFileName()}")
  }
}