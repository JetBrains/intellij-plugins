package org.intellij.prisma

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class PrismaHighlightingTest : PrismaTestCase("highlighting") {
  @Test
  fun testSemanticHighlighting() {
    doTest()
  }

  private fun doTest() {
    myFixture.configureByFile(getTestFileName())
    myFixture.checkHighlighting(false, true, false)
  }
}