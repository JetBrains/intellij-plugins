package org.intellij.prisma.completion

import com.intellij.openapi.application.readAction
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.builder.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.types.PrismaPreviewFeature
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaConstants.Functions
import com.intellij.openapi.util.text.StringUtil.wrapWithDoubleQuote as quoted
import org.junit.jupiter.api.Test

@TestApplication
class PrismaValuesCompletionTest : PrismaCompletionTestBase("completion/values") {
  @Test
  fun testDatasourceProvider(): Unit = timeoutRunBlocking {
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
    val element = readAction {
      PrismaSchemaProvider
        .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(myFixture.file.findElementAt(myFixture.caretOffset)))
        .getElement(PrismaSchemaKind.DATASOURCE_FIELD, PrismaConstants.DatasourceFields.PROVIDER)!!
    }
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(element.variants.map { quoted(it.label.orEmpty()) })
    checkLookupDocumentation(lookupElements, "\"sqlserver\"")
  }

  @Test
  fun testDatasourceProviderInLiteral(): Unit = timeoutRunBlocking {
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

  @Test
  fun testDatasourceUrlFunction(): Unit = timeoutRunBlocking {
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

    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.ENV)
    checkLookupDocumentation(lookupElements, Functions.ENV)
  }

  @Test
  fun testDatasourceUrlFunctionSkipInQuotes(): Unit = timeoutRunBlocking {
    noCompletion(
      """
          datasource db {
            url = "<caret>"
          }
      """.trimIndent()
    )
  }

  @Test
  fun testDatasourceUrlFunctionArgs(): Unit = timeoutRunBlocking {
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(item)
  }

  @Test
  fun testDatasourceUrlFunctionArgsInQuoted(): Unit = timeoutRunBlocking {
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

  @Test
  fun testDatasourceRelationMode(): Unit = timeoutRunBlocking {
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

  @Test
  fun testGeneratorPreviewFeatures(): Unit = timeoutRunBlocking {
    val feature = PrismaPreviewFeature.RelationJoins.presentation

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

  @Test
  fun testGeneratorPreviewFeaturesLastItem(): Unit = timeoutRunBlocking {
    val feature1 = PrismaPreviewFeature.ShardKeys.presentation
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

    assertThat(lookupElements.strings).doesNotContain(quoted(feature2))
  }

  @Test
  fun testGeneratorPreviewFeaturesCompleteUnquoted(): Unit = timeoutRunBlocking {
    val feature = PrismaPreviewFeature.RelationJoins.presentation

    completeSelected(
      """
          generator client {
            previewFeatures = [relat<caret>]
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

  @Test
  fun testGeneratorPreviewFeaturesOnlyInList(): Unit = timeoutRunBlocking {
    noCompletion(
      """
          generator client {
            previewFeatures = <caret>
          }
      """.trimIndent(),
    )
  }

  @Test
  fun testGeneratorPreviewFeaturesNotAfterComma(): Unit = timeoutRunBlocking {
    val feature1 = PrismaPreviewFeature.RelationJoins.presentation
    val feature2 = PrismaPreviewFeature.PostgresqlExtensions.presentation

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

  @Test
  fun testGeneratorPreviewFeaturesBetweenCommaAndValue(): Unit = timeoutRunBlocking {
    val feature1 = PrismaPreviewFeature.RelationJoins.presentation
    val feature2 = PrismaPreviewFeature.PostgresqlExtensions.presentation
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

  @Test
  fun testGeneratorPreviewFeaturesAtStart(): Unit = timeoutRunBlocking {
    val feature1 = PrismaPreviewFeature.RelationJoins.presentation
    val feature2 = PrismaPreviewFeature.PostgresqlExtensions.presentation
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
    assertThat(lookupElements.strings).doesNotContain(quoted(feature1), quoted(feature2))
  }
}