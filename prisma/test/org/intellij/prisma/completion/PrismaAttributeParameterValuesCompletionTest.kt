package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.ide.schema.types.PrismaIndexAlgorithm
import org.intellij.prisma.ide.schema.types.PrismaOperatorClass
import org.intellij.prisma.ide.schema.types.PrismaReferentialAction
import org.intellij.prisma.ide.schema.types.PrismaSortOrder
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.junit.jupiter.api.Test

@TestApplication
class PrismaAttributeParameterValuesCompletionTest : PrismaCompletionTestBase("") {
  @Test
  fun testBlockAttributeIndexPostgreSQLType(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              @@index(type: <caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(PrismaIndexAlgorithm.values().map { it.name })
  }

  @Test
  fun testBlockAttributeIndexMySQLOnFieldSort(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              id String
            
              @@index([id(sort: <caret>)])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(PrismaSortOrder.values().map { it.name })
  }

  @Test
  fun testBlockAttributeIndexSQLServerClustered(): Unit = timeoutRunBlocking {
    val lookupElements = completeSelected(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@index(clustered: <caret>)
            }
        """.trimIndent(),
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@index(clustered: true)
            }
        """.trimIndent(),
      "true"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("true", "false")
  }

  @Test
  fun testFieldAttributeIdSQLServerClustered(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              int Int @id(clustered: <caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("true", "false")
  }

  @Test
  fun testFieldAttributeRelationSQLServerOnDelete(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            
            model Post {
              id       Int    @id @default(autoincrement())
              title    String
              author   User   @relation(fields: [authorId], references: [id], onDelete: <caret>, onUpdate: NoAction)
              authorId Int
            }
            
            model User {
              id    Int    @id @default(autoincrement())
              posts Post[]
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(PrismaReferentialAction.Cascade.name, PrismaReferentialAction.NoAction.name, PrismaReferentialAction.SetDefault.name, PrismaReferentialAction.SetNull.name)
  }

  @Test
  fun testFieldAttributeRelationMySQLOnDelete(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            
            model Post {
              id       Int    @id @default(autoincrement())
              title    String
              author   User   @relation(fields: [authorId], references: [id], onDelete: <caret>, onUpdate: NoAction)
              authorId Int
            }
            
            model User {
              id    Int    @id @default(autoincrement())
              posts Post[]
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(PrismaReferentialAction.Cascade.name, PrismaReferentialAction.NoAction.name, PrismaReferentialAction.Restrict.name, PrismaReferentialAction.SetDefault.name, PrismaReferentialAction.SetNull.name)
  }

  @Test
  fun testBlockAttributeIndexOpsNativeType(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              id String @db.Char
            
              @@index([id(ops: <caret>)], type: Brin)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.RAW, PrismaOperatorClass.BpcharBloomOps.name, PrismaOperatorClass.BpcharMinMaxOps.name)
  }

  @Test
  fun testBlockAttributeIndexOpsFieldType(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              id Float
            
              @@index([id(ops: <caret>)], type: Brin)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.RAW, PrismaOperatorClass.Float8BloomOps.name, PrismaOperatorClass.Float8MinMaxOps.name, PrismaOperatorClass.Float8MinMaxMultiOps.name)
  }

  @Test
  fun testBlockAttributeIndexOpsFieldTypeOptional(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              id Int?
            
              @@index([id(ops: <caret>)], type: Brin)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.RAW, PrismaOperatorClass.Int4BloomOps.name, PrismaOperatorClass.Int4MinMaxOps.name, PrismaOperatorClass.Int4MinMaxMultiOps.name)
  }

  @Test
  fun testBlockAttributeIndexOpsFieldTypeList(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              id Int[]
            
              @@index([id(ops: <caret>)], type: Gin)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.RAW, PrismaOperatorClass.ArrayOps.name)
  }

  @Test
  fun testBlockAttributeIndexFields(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id        Int      @id
              name      String
              createdAt DateTime
              isDeleted Boolean
            
              @@index([name, createdAt(), <caret>])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("id", "isDeleted")
  }

  @Test
  fun testFieldAttributeRelationReferences(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            model Post {
              id       Int    @id @default(autoincrement())
              title    String
              author   User   @relation(fields: [authorId], references: [<caret>])
              authorId Int
            }
            
            model User {
              id        Int      @id @default(autoincrement())
              posts     Post[]
              name      String
              createdAt DateTime
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("id", "posts", "name", "createdAt")
  }

  @Test
  fun testFieldAttributeRelationReferencesNoEnumValues(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            model Post {
              id       Int    @id @default(autoincrement())
              title    String
              author   Lang   @relation(fields: [authorId], references: [<caret>])
              authorId Int
            }
            
            enum Lang {
              EN
              FR
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).isEmpty()
  }

  @Test
  fun testFieldAttributeDefaultEnumValues(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id   Int  @id @default(autoincrement())
              lang Lang @default(<caret>)
            }
            enum Lang {
              EN
              FR
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.DBGENERATED, "EN", "FR")
  }

  @Test
  fun testFieldAttributeDefaultNoModelValues(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id   Int  @id @default(autoincrement())
              lang Lang @default(<caret>)
            }
            model Lang {
              id   Int
              name String
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain("id", "name")
  }

  @Test
  fun testBlockAttributeNoFieldsInFieldExpression(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id    Int    @default(autoincrement())
              title String
              email String
            
              @@id([id(<caret>)])
            }
        """.trimIndent()
    )

    assertThat(lookupElements.strings).doesNotContain("id", "title", "email")
  }

  @Test
  fun testBlockAttributeUniqueCompositeType(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mongodb"
            }

            type City {
              name String
              size Int
              zip  String
            }

            type Address {
              number Int
              city   City
            }

            model User {
              id      Int     @id @map("_id")
              address Address

              @@unique([address.city.<caret>])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("name", "size", "zip")
  }

  @Test
  fun testBlockAttributeShardKeys(): Unit = timeoutRunBlocking {
    val lookupElements = getLookupElements("""
      generator client {
        provider        = "prisma-client-js"
        previewFeatures = ["shardKeys"]
      }

      datasource db {
        provider = "mysql"
        url      = "file:./dev.db"
      }

      model User {
        id    Int     @id @default(autoincrement())
        email String  @unique
        name  String?
        posts Post[] 
        
        @@shardKey([<caret>])
      }

      model Post {
        id        Int      @id @default(autoincrement())
        createdAt DateTime @default(now())
        updatedAt DateTime @updatedAt
        title     String
        content   String?
        published Boolean  @default(false)
        viewCount Int      @default(0)
        author    User?    @relation(fields: [authorId], references: [id])
        authorId  Int?
      }
    """.trimIndent())
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("id", "email")
  }
}