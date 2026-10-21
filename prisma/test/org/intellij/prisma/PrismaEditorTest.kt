package org.intellij.prisma

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.prisma.lang.PrismaFileType
import org.junit.jupiter.api.Test

@TestApplication
class PrismaEditorTest {
  private val prisma: PrismaFixture by prismaFixture("")

  @Test
  fun testEnterInFile() {
    doEnterTest(
      """
                |model User {}
                |<caret>
                |model Account {}
            """.trimMargin(),
      """
                |model User {}
                |
                |<caret>
                |model Account {}
            """.trimMargin()
    )
  }

  @Test
  fun testEnterInBlock() {
    doEnterTest(
      """
                |model User {
                |  id    Int     @id @default(autoincrement())<caret>
                |  email String  @unique
                |  name  String?
                |}
            """.trimMargin(),
      """
                |model User {
                |  id    Int     @id @default(autoincrement())
                |  <caret>
                |  email String  @unique
                |  name  String?
                |}
            """.trimMargin()
    )
  }

  private fun doEnterTest(text: String, expected: String) {
    prisma.fixture.configureByText(PrismaFileType, text)
    prisma.fixture.type('\n')
    prisma.fixture.checkResult(expected)
  }
}