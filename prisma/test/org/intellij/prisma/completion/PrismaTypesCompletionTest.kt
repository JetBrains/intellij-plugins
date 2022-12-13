package org.intellij.prisma.completion

import org.intellij.prisma.lang.PrismaConstants

class PrismaTypesCompletionTest : PrismaCompletionTestBase() {
  override fun getBasePath(): String = "/completion/types"

  fun testPrimitiveTypes() {
    val lookupElements = completeSelected(
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
    assertSameElements(lookupElements.strings, PrismaConstants.PrimitiveTypes.ALL + "Model")
    checkLookupDocumentation(lookupElements, "DateTime")
  }

  fun testTypeDeclarations() {
    val lookupElements = completeSelected(
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
    assertContainsElements(lookupElements.strings, "User", "Lang", "Ty", "M")
  }

  fun testNoDecimalInMongo() {
    val lookupElements = getLookupElements(
      """
                datasource db { 
                  provider = "mongodb" 
                }
                model M {
                  id <caret>
                }
            """.trimIndent(),
    )
    assertDoesntContain(lookupElements.strings, PrismaConstants.PrimitiveTypes.DECIMAL)
  }

  fun testUnsupportedType() {
    val lookupElements = completeSelected(
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
    checkLookupDocumentation(lookupElements, "Unsupported")
  }
}