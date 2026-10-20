package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class PrismaKeywordCompletionTest : PrismaCompletionTestBase("completion/keywords") {
  @Test
  fun testRootKeywords(): Unit = timeoutRunBlocking {
    val lookupElements = completeSelected(
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
    completeBasic(
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
    completeBasic("mo<caret>", "model <caret>")
  }

  @Test
  fun testNoKeywordsInDeclaration(): Unit = timeoutRunBlocking {
    noCompletion("model <caret>")
  }

  @Test
  fun testNoKeywordsInBlock(): Unit = timeoutRunBlocking {
    noCompletion("model M { <caret> }")
  }

  @Test
  fun testTypeKeywordDoc(): Unit = timeoutRunBlocking {
    checkLookupDocumentation(getLookupElements("<caret>").find("type"))
  }

  @Test
  fun testNoEnumInSqlite(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
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
    noCompletion(
      """
            model M {
              names String[<caret>]
            }
        """.trimIndent()
    )
  }
}