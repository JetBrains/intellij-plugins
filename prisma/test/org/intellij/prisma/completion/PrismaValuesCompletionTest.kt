package org.intellij.prisma.completion

import org.intellij.prisma.ide.schema.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.types.PrismaPreviewFeature
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

  fun testDatasourceRelationMode() {
    completeSelected(
      """
          datasource db {
            provider = "postgresql"
            relationMode = <caret>
          }
      """.trimIndent(),
      """
          datasource db {
            provider = "postgresql"
            relationMode = "prisma"
          }
      """.trimIndent(),
      "\"prisma\""
    )
  }

  fun testGeneratorPreviewFeatures() {
    val index = PrismaPreviewFeature.FullTextIndex.presentation

    completeSelected(
      """
          generator client {
            previewFeatures = [<caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$index"]
          }
      """.trimIndent(),
      quoted(index),
    )
  }

  fun testGeneratorPreviewFeaturesLastItem() {
    val search = PrismaPreviewFeature.FullTextSearch.presentation
    val index = PrismaPreviewFeature.FullTextIndex.presentation

    val lookupElements = completeSelected(
      """
          generator client {
            previewFeatures = ["$index", <caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$index", "$search"]
          }
      """.trimIndent(),
      quoted(search),
    )

    assertDoesntContain(lookupElements.strings, quoted(index))
  }

  fun testGeneratorPreviewFeaturesCompleteUnquoted() {
    val index = PrismaPreviewFeature.FullTextIndex.presentation

    completeSelected(
      """
          generator client {
            previewFeatures = [full<caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$index"]
          }
      """.trimIndent(),
      quoted(index),
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
    val search = PrismaPreviewFeature.FullTextSearch.presentation
    val relation = PrismaPreviewFeature.FilteredRelationCount.presentation

    noCompletion(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$search", "$relation"<caret>]
            engineType = "binary"
          }
      """.trimIndent(),
    )
  }

  fun testGeneratorPreviewFeaturesBetweenCommaAndValue() {
    val search = PrismaPreviewFeature.FullTextSearch.presentation
    val relation = PrismaPreviewFeature.FilteredRelationCount.presentation
    val index = PrismaPreviewFeature.FullTextIndex.presentation

    completeSelected(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$search", <caret>"$relation"]
            engineType = "binary"
          }
      """.trimIndent(),
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$search", "$index""$relation"]
            engineType = "binary"
          }
      """.trimIndent(),
      quoted(index)
    )
  }

  fun testGeneratorPreviewFeaturesAtStart() {
    val search = PrismaPreviewFeature.FullTextSearch.presentation
    val relation = PrismaPreviewFeature.FilteredRelationCount.presentation
    val index = PrismaPreviewFeature.FullTextIndex.presentation

    val lookupElements = completeSelected(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = [<caret>"$search", "$relation"]
            engineType = "binary"
          }
      """.trimIndent(),
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$index""$search", "$relation"]
            engineType = "binary"
          }
      """.trimIndent(),
      quoted(index)
    )
    assertDoesntContain(
      lookupElements.strings,
      quoted(search),
      quoted(relation)
    )
  }
}