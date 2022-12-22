package org.intellij.prisma.completion

import org.intellij.prisma.ide.schema.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaConstants.Functions
import com.intellij.openapi.util.text.StringUtil.wrapWithDoubleQuote as quoted

class PrismaValuesCompletionTest : PrismaCompletionTestBase() {
  override fun getBasePath(): String = "/completion/values"

  fun testDatasourceProvider() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = <caret>
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "sqlserver"
                }
            """.trimIndent(),
      "\"sqlserver\""
    )
    val element = PrismaSchemaProvider
      .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(myFixture.file.findElementAt(myFixture.caretOffset)))
      .getElement(PrismaSchemaKind.DATASOURCE_FIELD, PrismaConstants.DatasourceFields.PROVIDER)!!
    assertSameElements(lookupElements.strings, element.variants.map { quoted(it.label) })
    checkLookupDocumentation(lookupElements, "\"sqlserver\"")
  }

  fun testDatasourceProviderInLiteral() {
    completeSelected(
      """
                datasource db {
                  provider = "sql<caret>"
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "sqlite"
                }
            """.trimIndent(),
      "sqlite"
    )
  }

  fun testDatasourceUrlFunction() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "postgresql"
                  url = <caret>
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "postgresql"
                  url = env("<caret>")
                }
            """.trimIndent(),
      "env"
    )

    assertSameElements(lookupElements.strings, Functions.ENV)
    checkLookupDocumentation(lookupElements, Functions.ENV)
  }

  fun testDatasourceUrlFunctionSkipInQuotes() {
    noCompletion(
      """
                datasource db {
                  url = "<caret>"
                }
            """.trimIndent()
    )
  }

  fun testDatasourceUrlFunctionArgs() {
    val item = quoted("DATABASE_URL")
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "postgresql"
                  url = env(<caret>)
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "postgresql"
                  url = env("DATABASE_URL")
                }
            """.trimIndent(),
      item
    )
    assertSameElements(lookupElements.strings, item)
  }

  fun testDatasourceUrlFunctionArgsInQuoted() {
    val item = "DATABASE_URL"
    completeSelected(
      """
                datasource db {
                  provider = "postgresql"
                  url = env("<caret>")
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "postgresql"
                  url = env("DATABASE_URL")
                }
            """.trimIndent(),
      item
    )
  }

  fun testGeneratorPreviewFeatures() {
    completeSelected(
      """
                generator client {
                  previewFeatures = [<caret>]
                }
            """.trimIndent(),
      """
                generator client {
                  previewFeatures = ["FullTextIndex"]
                }
            """.trimIndent(),
      quoted("FullTextIndex"),
    )
  }

  fun testGeneratorPreviewFeaturesLastItem() {
    val lookupElements = completeSelected(
      """
                generator client {
                  previewFeatures = ["FullTextIndex", <caret>]
                }
            """.trimIndent(),
      """
                generator client {
                  previewFeatures = ["FullTextIndex", "FullTextSearch"]
                }
            """.trimIndent(),
      quoted("FullTextSearch"),
    )

    assertDoesntContain(lookupElements.strings, quoted("FullTextIndex"))
  }

  fun testGeneratorPreviewFeaturesCompleteUnquoted() {
    completeSelected(
      """
                generator client {
                  previewFeatures = [Full<caret>]
                }
            """.trimIndent(),
      """
                generator client {
                  previewFeatures = ["FullTextIndex"]
                }
            """.trimIndent(),
      quoted("FullTextIndex"),
    )
  }

  fun testGeneratorPreviewFeaturesOnlyInList() {
    noCompletion(
      """
                generator client {
                  previewFeatures = <caret>
                }
            """.trimIndent(),
    )
  }

  fun testGeneratorPreviewFeaturesNotAfterComma() {
    noCompletion(
      """
                generator client {
                  provider = "prisma-client-js"
                  previewFeatures = ["ReferentialIntegrity", "FilteredRelationCount"<caret>]
                  engineType = "binary"
                }
            """.trimIndent(),
    )
  }

  fun testGeneratorPreviewFeaturesBetweenCommaAndValue() {
    completeSelected(
      """
                generator client {
                  provider = "prisma-client-js"
                  previewFeatures = ["ReferentialIntegrity", <caret>"FilteredRelationCount"]
                  engineType = "binary"
                }
            """.trimIndent(),
      """
                generator client {
                  provider = "prisma-client-js"
                  previewFeatures = ["ReferentialIntegrity", "FullTextIndex""FilteredRelationCount"]
                  engineType = "binary"
                }
            """.trimIndent(),
      quoted("FullTextIndex")
    )
  }

  fun testGeneratorPreviewFeaturesAtStart() {
    val lookupElements = completeSelected(
      """
                generator client {
                  provider = "prisma-client-js"
                  previewFeatures = [<caret>"ReferentialIntegrity", "FilteredRelationCount"]
                  engineType = "binary"
                }
            """.trimIndent(),
      """
                generator client {
                  provider = "prisma-client-js"
                  previewFeatures = ["FullTextIndex""ReferentialIntegrity", "FilteredRelationCount"]
                  engineType = "binary"
                }
            """.trimIndent(),
      quoted("FullTextIndex")
    )
    assertDoesntContain(
      lookupElements.strings,
      quoted("ReferentialIntegrity"),
      quoted("FilteredRelationCount")
    )
  }
}