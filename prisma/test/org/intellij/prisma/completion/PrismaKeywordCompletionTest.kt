package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class PrismaKeywordCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/keywords")

  @Test
  fun testRootKeywords(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {}
                <caret>
                model M1 {}
            """.trimIndent(),
      """
                model M {}
                model <caret>
                model M1 {}
            """.trimIndent(),
      "model"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("model", "datasource", "generator", "type", "enum")
  }

  @Test
  fun testFinishRootKeyword() {
    prisma.completeBasic(
      """
                model M {}
                ty<caret>
                model M1 {}
            """.trimIndent(),
      """
                model M {}
                type <caret>
                model M1 {}
            """.trimIndent()
    )
  }

  @Test
  fun testKeywordInEmptyFile() {
    prisma.completeBasic("mo<caret>", "model <caret>")
  }

  @Test
  fun testNoKeywordsInDeclaration(): Unit = timeoutRunBlocking {
    prisma.noCompletion("model <caret>")
  }

  @Test
  fun testNoKeywordsInBlock(): Unit = timeoutRunBlocking {
    prisma.noCompletion("model M { <caret> }")
  }

  @Test
  fun testTypeKeywordDoc(): Unit = timeoutRunBlocking {
    prisma.checkLookupDocumentation(prisma.getLookupElements("<caret>").find("type"))
  }

  @Test
  fun testNoEnumInSqlite(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
                datasource db {
                  provider = "sqlite"
                }
                <caret>
            """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain("enum")
    assertThat(lookupElements.strings).contains("model")
  }

  @Test
  fun testNoKeywordsBetweenBrackets(): Unit = timeoutRunBlocking {
    prisma.noCompletion(
      """
            model M {
              names String[<caret>]
            }
        """.trimIndent()
    )
  }
}