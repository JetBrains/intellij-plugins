package org.intellij.prisma

class PrismaHighlightingTest : PrismaTestCase("highlighting") {
  fun testSemanticHighlighting() {
    doTest()
  }

  private fun doTest() {
    myFixture.configureByFile(getTestFileName())
    myFixture.checkHighlighting(false, true, false)
  }
}