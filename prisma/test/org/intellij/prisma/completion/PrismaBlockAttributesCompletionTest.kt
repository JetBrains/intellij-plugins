package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.junit.jupiter.api.Test

@TestApplication
class PrismaBlockAttributesCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/blockAttributes")

  @Test
  fun testBlockAttributes(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          datasource db {
            provider = "mysql"
          }
          model M {
            <caret>
          }
      """.trimIndent(), """
          datasource db {
            provider = "mysql"
          }
          model M {
            @@id([<caret>])
          }
      """.trimIndent(),
      "@@id"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(BlockAttributes.ALL - BlockAttributes.SCHEMA)
    prisma.checkLookupDocumentation(lookupElements, "@@id")
  }

  @Test
  fun testBlockAttributesPrefixAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            @<caret>
          }
      """.trimIndent(), """
          model M {
            @@map("<caret>")
          }
      """.trimIndent(),
      "@@map"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesPrefixAtAfterField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            id String
            
            @<caret>
          }
      """.trimIndent(), """
          model M {
            id String
            
            @@index([<caret>])
          }
      """.trimIndent(),
      "@@index"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesPrefixAtAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            @@<caret>
          }
      """.trimIndent(), """
          model M {
            @@unique([<caret>])
          }
      """.trimIndent(),
      "@@unique"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesPrefixAtAtAfterField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            id String
            
            @@<caret>
          }
      """.trimIndent(), """
          model M {
            id String
            
            @@unique([<caret>])
          }
      """.trimIndent(),
      "@@unique"
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesPrefixName() {
    prisma.completeBasic(
      """
          model M {
            @@ign<caret>
          }
      """.trimIndent(), """
          model M {
            @@ignore<caret>
          }
      """.trimIndent()
    )
  }

  @Test
  fun testBlockAttributesFulltext() {
    prisma.completeBasic(
      """
          datasource db {
            provider = "mysql"
          }
          
          model M {
            @@full<caret>
          }
      """.trimIndent(), """
          datasource db {
            provider = "mysql"
          }
          
          model M {
            @@fulltext([<caret>])
          }
      """.trimIndent()
    )
  }

  @Test
  fun testBlockAttributesNoFulltext(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          datasource db {
            provider = "postgresql"
          }
          
          model M {
            @@<caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(BlockAttributes.FULLTEXT)
  }

  @Test
  fun testNoBlockAttributesInCompositeType(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          type M {
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContainAnyElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testNoBlockAttributesForField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            id String <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContainAnyElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesNoIdIfHasIdFieldAttribute(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            id Int @id
            
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(BlockAttributes.ID)
  }

  @Test
  fun testBlockAttributesNoDuplicates(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            id Int
            
            @@map("foo")
            @@unique([id])
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(BlockAttributes.MAP, BlockAttributes.UNIQUE)
  }

  @Test
  fun testNoBlockAttributesForFieldAfterAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            id String @<caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContainAnyElementsOf(BlockAttributes.ALL)
  }

  @Test
  fun testBlockAttributesEnum(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Role {
            ADMIN
            USER
            GUEST
            
            @<caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(BlockAttributes.MAP)
  }

  @Test
  fun testBlockAttributesEnumWithoutAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Role {
            ADMIN
            USER
            GUEST
            
            <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(BlockAttributes.MAP)
  }

  @Test
  fun testBlockAttributesEnumAtAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Role {
            ADMIN
            USER
            GUEST
            
            @@<caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(BlockAttributes.MAP)
  }

  @Test
  fun testSchemaAttribute(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
        generator client {
          provider        = "prisma-client-js"
          previewFeatures = ["multiSchema"]
        }

        datasource db {
          provider = "postgresql"
          url      = ""
          schemas  = ["base-schema", "login"]
        }

        model User {
          id Int @id

          @@schema("<caret>")
        }
      """.trimIndent(),
      """
        generator client {
          provider        = "prisma-client-js"
          previewFeatures = ["multiSchema"]
        }

        datasource db {
          provider = "postgresql"
          url      = ""
          schemas  = ["base-schema", "login"]
        }

        model User {
          id Int @id

          @@schema("base-schema")
        }
      """.trimIndent(),
      "base-schema",
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("base-schema", "login")
  }

  @Test
  fun testSchemaAttributeQuoteHandler(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
        generator client {
          provider        = "prisma-client-js"
          previewFeatures = ["multiSchema"]
        }

        datasource db {
          provider = "postgresql"
          url      = ""
          schemas  = ["base-schema", "login"]
        }

        model User {
          id Int @id

          @@schema(<caret>)
        }
      """.trimIndent(),
      """
        generator client {
          provider        = "prisma-client-js"
          previewFeatures = ["multiSchema"]
        }

        datasource db {
          provider = "postgresql"
          url      = ""
          schemas  = ["base-schema", "login"]
        }

        model User {
          id Int @id

          @@schema("base-schema")
        }
      """.trimIndent(),
      "\"base-schema\"",
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder("\"base-schema\"", "\"login\"")
  }

  @Test
  fun testBlockAttributesShardKey(): Unit = timeoutRunBlocking {
    prisma.completeSelected("""
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
        
        @@<caret>
      }
    """.trimIndent(), """
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
        
        @@shardKey([<caret>])
      }
    """.trimIndent(), "@@shardKey")
  }

  @Test
  fun testBlockAttributeShardKeyOnlyMysql(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements("""
      generator client {
        provider        = "prisma-client-js"
        previewFeatures = ["shardKeys"]
      }

      datasource db {
        provider = "postgresql"
        url      = "file:./dev.db"
      }

      model User {
        id    Int     @id @default(autoincrement())
        email String  @unique
        name  String?
        
        @@<caret>
      }
    """.trimIndent())
    assertThat(lookupElements.strings).doesNotContain(BlockAttributes.SHARD_KEY)
  }
}