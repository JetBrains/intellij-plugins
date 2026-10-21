package org.intellij.prisma

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class PrismaHighlightingTest {
  private val prisma: PrismaFixture by prismaFixture("highlighting")

  @Test
  fun testSemanticHighlighting() {
    doTest()
  }

  private fun doTest() {
    prisma.fixture.configureByFile(prisma.getTestFileName())
    prisma.fixture.checkHighlighting(false, true, false)
  }
}