package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.ide.schema.types.PrismaNativeType
import org.junit.jupiter.api.Test

@TestApplication
class PrismaNativeTypesCompletionTest : PrismaCompletionTestBase("completion/nativeTypes") {
  @Test
  fun testNativeTypeDatasource(): Unit = timeoutRunBlocking {
    val lookupElements = completeSelected(
      """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id Int <caret>
          }
      """.trimIndent(), """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id Int @pg.<caret>
          }
      """.trimIndent(),
      "@pg"
    )

    checkLookupDocumentation(lookupElements, "@pg")
  }

  @Test
  fun testNativeTypeDatasourceAfterAt(): Unit = timeoutRunBlocking {
    completeSelected(
      """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id Int @<caret>
          }
      """.trimIndent(), """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id Int @pg.<caret>
          }
      """.trimIndent(),
      "@pg"
    )
  }

  @Test
  fun testNativeTypeDatasourceSplitSchema(): Unit = timeoutRunBlocking {
    completeSelected(
      """
          model M {
            id Int <caret>
          }
      """.trimIndent(), """
          model M {
            id Int @pg.<caret>
          }
      """.trimIndent(),
      "@pg",
      """
          datasource pg {
            provider = "postgresql"
          }
      """.trimIndent()
    )
  }

  @Test
  fun testNativeTypeMultipleDatasource(): Unit = timeoutRunBlocking {
    // it works in the plugin, though it's not supported yet in Prisma, and multiple generator declarations will be marked as error.
    val lookupElements = completeSelected(
      """
          datasource local {
            provider = "sqlserver"
          }
        
          model M {
            id Int @<caret>
          }
      """.trimIndent(), """
          datasource local {
            provider = "sqlserver"
          }
        
          model M {
            id Int @other.<caret>
          }
      """.trimIndent(),
      "@other",
      """
          datasource pg {
            provider = "postgresql"
          }
          
          datasource other {
            provider = "mysql"
          }
      """.trimIndent()
    )

    assertThat(lookupElements.strings).contains("@local", "@pg", "@other")
  }

  @Test
  fun testNativeType(): Unit = timeoutRunBlocking {
    val lookupElements = completeSelected(
      """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id String @pg.<caret>
          }
      """.trimIndent(), """
          datasource pg {
            provider = "postgresql"
          }
          model M {
            id String @pg.Uuid<caret>
          }
      """.trimIndent(),
      PrismaNativeType.PostgreSQL.UUID_TYPE_NAME
    )

    assertThat(lookupElements.strings).containsExactlyInAnyOrder(PrismaNativeType.PostgreSQL.BIT_TYPE_NAME, PrismaNativeType.PostgreSQL.CHAR_TYPE_NAME, PrismaNativeType.PostgreSQL.CITEXT_TYPE_NAME, PrismaNativeType.PostgreSQL.INET_TYPE_NAME, PrismaNativeType.PostgreSQL.TEXT_TYPE_NAME, PrismaNativeType.PostgreSQL.UUID_TYPE_NAME, PrismaNativeType.PostgreSQL.VAR_BIT_TYPE_NAME, PrismaNativeType.PostgreSQL.VARCHAR_TYPE_NAME, PrismaNativeType.PostgreSQL.XML_TYPE_NAME)
  }

  @Test
  fun testNativeTypeSplitSchema() {
    checkLookupElementsInSplitSchema()
  }

  @Test
  fun testNativeTypeWithArgs(): Unit = timeoutRunBlocking {
    completeSelected(
      """
          datasource db {
            provider = "postgresql"
          }
          model M {
            id String @db.<caret>
          }
      """.trimIndent(), """
          datasource db {
            provider = "postgresql"
          }
          model M {
            id String @db.VarChar(<caret>)
          }
      """.trimIndent(),
      PrismaNativeType.PostgreSQL.VARCHAR_TYPE_NAME
    )
  }
}