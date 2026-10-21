package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class PrismaKeyValueCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/keyValue")

  @Test
  fun testDatasourceField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          datasource db {
            <caret>
          }
      """.trimIndent(), """
          datasource db {
            url = <caret>
          }
      """.trimIndent(),
      "url"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("directUrl", "provider", "url", "shadowDatabaseUrl", "relationMode", "extensions", "schemas")
    prisma.checkLookupDocumentation(lookupElements, "url")
  }

  @Test
  fun testDatasourceNoRelationModeForMongo(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          datasource db {
            provider = "mongodb"
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("directUrl", "url", "shadowDatabaseUrl")
  }

  @Test
  fun testDatasourceExtensions(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
      """
          datasource db {
            provider   = "postgresql"
            url        = env("DATABASE_URL")
            <caret>
          }
      """.trimIndent(), """
          datasource db {
            provider   = "postgresql"
            url        = env("DATABASE_URL")
            extensions = [<caret>]
          }
      """.trimIndent(),
      "extensions"
    )
  }

  @Test
  fun testGeneratorField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          generator client {
            <caret>
          }
      """.trimIndent(), """
          generator client {
            provider = "<caret>"
          }
      """.trimIndent(),
      "provider"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("provider", "output", "binaryTargets", "previewFeatures", "engineType")
    prisma.checkLookupDocumentation(lookupElements, "provider")
  }

  @Test
  fun testGeneratorPreviewFeatures(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          generator client {
            provider = "prisma-client-js"
            <caret>
            engineType = "binary"
          }
      """.trimIndent(), """
          generator client {
            provider = "prisma-client-js"
            previewFeatures = [<caret>]
            engineType = "binary"
          }
      """.trimIndent(),
      "previewFeatures"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("output", "binaryTargets", "previewFeatures")
  }

  @Test
  fun testGeneratorBinaryTargets(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
      """
          generator client {
            <caret>
          }
      """.trimIndent(), """
          generator client {
            binaryTargets = [<caret>]
          }
      """.trimIndent(),
      "binaryTargets"
    )
  }

  @Test
  fun testGeneratorWithPrismaClientProvider(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          generator client {
            provider = "prisma-client"
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).contains("runtime", "moduleFormat", "generatedFileExtension", "importFileExtension")
  }

  @Test
  fun testGeneratorWithDefaultProvider(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          generator client {
            provider = "prisma-client-js"
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain("runtime", "moduleFormat", "generatedFileExtension", "importFileExtension")
  }
}
