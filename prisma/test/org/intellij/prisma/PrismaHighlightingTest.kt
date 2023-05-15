package org.intellij.prisma

class PrismaHighlightingTest : PrismaTestCase() {
  override fun getBasePath(): String = "/highlighting"

  fun testSemanticHighlighting() {
    doTest()
  }

  private fun doTest() {
    myFixture.configureByFile(getTestName())
    myFixture.checkHighlighting(false, true, false)
  }
}