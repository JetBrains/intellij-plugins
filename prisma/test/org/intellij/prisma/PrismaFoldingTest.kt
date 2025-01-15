// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

class PrismaFoldingTest : PrismaTestCase("folding") {
  fun testComments() {
    doTest()
  }

  private fun doTest() {
    myFixture.testFolding("$testDataPath/${getTestFileName()}")
  }
}