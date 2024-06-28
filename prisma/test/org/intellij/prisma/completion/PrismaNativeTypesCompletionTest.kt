package org.intellij.prisma.completion

import org.intellij.prisma.ide.schema.types.PrismaNativeType

class PrismaNativeTypesCompletionTest : PrismaCompletionTestBase("completion/nativeTypes") {
  fun testNativeTypeDatasource() {
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

  fun testNativeTypeDatasourceAfterAt() {
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

  fun testNativeTypeDatasourceSplitSchema() {
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

  fun testNativeTypeMultipleDatasource() {
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

    assertContainsElements(
      lookupElements.strings,
      "@local",
      "@pg",
      "@other",
    )
  }

  fun testNativeType() {
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

    assertSameElements(
      lookupElements.strings,
      PrismaNativeType.PostgreSQL.BIT_TYPE_NAME,
      PrismaNativeType.PostgreSQL.CHAR_TYPE_NAME,
      PrismaNativeType.PostgreSQL.CITEXT_TYPE_NAME,
      PrismaNativeType.PostgreSQL.INET_TYPE_NAME,
      PrismaNativeType.PostgreSQL.TEXT_TYPE_NAME,
      PrismaNativeType.PostgreSQL.UUID_TYPE_NAME,
      PrismaNativeType.PostgreSQL.VAR_BIT_TYPE_NAME,
      PrismaNativeType.PostgreSQL.VARCHAR_TYPE_NAME,
      PrismaNativeType.PostgreSQL.XML_TYPE_NAME,
    )
  }

  fun testNativeTypeSplitSchema() {
    checkLookupElementsInSplitSchema()
  }

  fun testNativeTypeWithArgs() {
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