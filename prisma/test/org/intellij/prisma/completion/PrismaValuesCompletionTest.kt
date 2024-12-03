package org.intellij.prisma.completion

import org.intellij.prisma.ide.schema.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.types.PrismaPreviewFeature
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaConstants.Functions
import com.intellij.openapi.util.text.StringUtil.wrapWithDoubleQuote as quoted

class PrismaValuesCompletionTest : PrismaCompletionTestBase("completion/values") {
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
    val feature = PrismaPreviewFeature.DriverAdapters.presentation

    completeSelected(
      """
          generator client {
            previewFeatures = [<caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$feature"]
          }
      """.trimIndent(),
      quoted(feature),
    )
  }

  fun testGeneratorPreviewFeaturesLastItem() {
    val feature1 = PrismaPreviewFeature.DriverAdapters.presentation
    val feature2 = PrismaPreviewFeature.RelationJoins.presentation

    val lookupElements = completeSelected(
      """
          generator client {
            previewFeatures = ["$feature2", <caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$feature2", "$feature1"]
          }
      """.trimIndent(),
      quoted(feature1),
    )

    assertDoesntContain(lookupElements.strings, quoted(feature2))
  }

  fun testGeneratorPreviewFeaturesCompleteUnquoted() {
    val feature = PrismaPreviewFeature.DriverAdapters.presentation

    completeSelected(
      """
          generator client {
            previewFeatures = [drive<caret>]
          }
      """.trimIndent(),
      """
          generator client {
            previewFeatures = ["$feature"]
          }
      """.trimIndent(),
      quoted(feature),
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
    val feature1 = PrismaPreviewFeature.DriverAdapters.presentation
    val feature2 = PrismaPreviewFeature.MultiSchema.presentation

    noCompletion(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$feature1", "$feature2"<caret>]
            engineType = "binary"
          }
      """.trimIndent(),
    )
  }

  fun testGeneratorPreviewFeaturesBetweenCommaAndValue() {
    val feature1 = PrismaPreviewFeature.DriverAdapters.presentation
    val feature2 = PrismaPreviewFeature.MultiSchema.presentation
    val featureToComplete = PrismaPreviewFeature.NativeDistinct.presentation

    completeSelected(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$feature1", <caret>"$feature2"]
            engineType = "binary"
          }
      """.trimIndent(),
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$feature1", "$featureToComplete""$feature2"]
            engineType = "binary"
          }
      """.trimIndent(),
      quoted(featureToComplete)
    )
  }

  fun testGeneratorPreviewFeaturesAtStart() {
    val feature1 = PrismaPreviewFeature.DriverAdapters.presentation
    val feature2 = PrismaPreviewFeature.MultiSchema.presentation
    val featureToComplete = PrismaPreviewFeature.NativeDistinct.presentation

    val lookupElements = completeSelected(
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = [<caret>"$feature1", "$feature2"]
            engineType = "binary"
          }
      """.trimIndent(),
      """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = ["$featureToComplete""$feature1", "$feature2"]
            engineType = "binary"
          }
      """.trimIndent(),
      quoted(featureToComplete)
    )
    assertDoesntContain(
      lookupElements.strings,
      quoted(feature1),
      quoted(feature2)
    )
  }
}