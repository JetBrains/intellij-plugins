package org.intellij.prisma

import org.intellij.prisma.lang.PrismaFileType

class PrismaEditorTest : PrismaTestCase() {
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
    myFixture.configureByText(PrismaFileType, text)
    myFixture.type('\n')
    myFixture.checkResult(expected)
  }
}