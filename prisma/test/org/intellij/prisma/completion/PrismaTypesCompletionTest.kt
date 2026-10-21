package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.lang.PrismaConstants
import org.junit.jupiter.api.Test

@TestApplication
class PrismaTypesCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/types")

  @Test
  fun testPrimitiveTypes(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model Model {
                  id <caret>
                }
            """.trimIndent(),
      """
                model Model {
                  id DateTime<caret>
                }
            """.trimIndent(),
      "DateTime"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(PrismaConstants.PrimitiveTypes.ALL + "Model")
    prisma.checkLookupDocumentation(lookupElements, "DateTime")
  }

  @Test
  fun testTypeDeclarations(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model User {}
                enum Lang {}
                type Ty {}
                model M {
                  language <caret>
                }
            """.trimIndent(),
      """
                model User {}
                enum Lang {}
                type Ty {}
                model M {
                  language Lang<caret>
                }
            """.trimIndent(),
      "Lang"
    )
    assertThat(lookupElements.strings).contains("User", "Lang", "Ty", "M")
  }

  @Test
  fun testNoDecimalInMongo(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
                datasource db { 
                  provider = "mongodb" 
                }
                model M {
                  id <caret>
                }
            """.trimIndent(),
    )
    assertThat(lookupElements.strings).doesNotContain(PrismaConstants.PrimitiveTypes.DECIMAL)
  }

  @Test
  fun testUnsupportedType(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {
                  id Uns<caret>
                }
            """.trimIndent(),
      """
                model M {
                  id Unsupported("<caret>")
                }
            """.trimIndent(),
      "Unsupported"
    )
    prisma.checkLookupDocumentation(lookupElements, "Unsupported")
  }

  @Test
  fun testGlobalTypes() {
    prisma.checkLookupElementsInSplitSchema()
  }

  @Test
  fun testGlobalTypesSchemaDir() {
    prisma.checkLookupElementsInSplitSchema("prisma/schema")
  }

  @Test
  fun testTypesMonorepo() {
    prisma.checkLookupElementsInSplitSchema("module1/schema/feature1")
  }
}