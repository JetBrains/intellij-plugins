package org.intellij.prisma.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class PrismaJSCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/js")

  @Test
  fun testApiClientField() {
    val lookupElements = doDirTest()
    assertThat(lookupElements).hasSize(2)
    val userField = lookupElements.first()
    assertEquals("user", userField.lookupString)
    val userType = userField.presentation?.typeText!!
    assertTrue(userType.startsWith("Prisma.UserDelegate"))
  }

  private fun doDirTest(ext: String = "js"): Array<LookupElement> {
    val dir = prisma.fixture.copyDirectoryToProject(prisma.getTestName(true), ".")
    val filename = "${prisma.getTestName(true)}.$ext"
    prisma.fixture.configureFromExistingVirtualFile(dir.findChild(filename)!!)
    return prisma.fixture.completeBasic() ?: emptyArray()
  }
}