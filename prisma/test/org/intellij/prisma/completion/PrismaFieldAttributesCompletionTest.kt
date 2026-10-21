package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.lang.PrismaConstants.FieldAttributes
import org.junit.jupiter.api.Test

@TestApplication
class PrismaFieldAttributesCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/fieldAttributes")

  @Test
  fun testFieldAttributes(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model User {
          }
          model M {
            user User <caret>
          }
      """.trimIndent(), """
          model User {
          }
          model M {
            user User @relation(<caret>)
          }
      """.trimIndent(),
      FieldAttributes.RELATION
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.SHARD_KEY)

    prisma.checkLookupDocumentation(lookupElements, FieldAttributes.RELATION)
  }

  @Test
  fun testFieldAttributeAfterAt(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
      """
          model M {
            id Int @<caret>
          }
      """.trimIndent(), """
          model M {
            id Int @id<caret>
          }
      """.trimIndent(),
      FieldAttributes.ID
    )
  }

  @Test
  fun testFieldAttributeWithBeforeNextField(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
      """
          model M {
            id Int @<caret>
            date DateTime
          }
      """.trimIndent(), """
          model M {
            id Int @ignore<caret>
            date DateTime
          }
      """.trimIndent(),
      FieldAttributes.IGNORE
    )
  }

  @Test
  fun testFieldAttributeExistingAttribute(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
      """
          model M {
            id Int @un<caret>
          }
      """.trimIndent(), """
          model M {
            id Int @unique<caret>
          }
      """.trimIndent(),
      FieldAttributes.UNIQUE
    )
  }

  @Test
  fun testFieldAttributeAfterAnother(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            user User @unique <caret>
          }
      """.trimIndent(), """
          model M {
            user User @unique @map("<caret>")
          }
      """.trimIndent(),
      FieldAttributes.MAP
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.UNIQUE - FieldAttributes.SHARD_KEY)
    prisma.checkLookupDocumentation(lookupElements, FieldAttributes.MAP)
  }

  @Test
  fun testFieldAttributeBeforeAnother(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
          model M {
            user User <caret> @map("")
          }
      """.trimIndent(), """
          model M {
            user User @unique @map("")
          }
      """.trimIndent(),
      FieldAttributes.UNIQUE
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.MAP - FieldAttributes.SHARD_KEY)
  }

  @Test
  fun testFieldAttributeForCompositeTypeField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          type T {}
          model M {
            type T <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.DEFAULT, FieldAttributes.RELATION)
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(FieldAttributes.IGNORE, FieldAttributes.MAP, FieldAttributes.UNIQUE)
  }

  @Test
  fun testFieldAttributeIdAllowedForEnum(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Lang {}
          model M {
            lang Lang <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).contains(FieldAttributes.ID)
  }

  @Test
  fun testFieldAttributeIdAllowedForInt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            i Int <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).contains(FieldAttributes.ID)
  }

  @Test
  fun testFieldAttributeIdAllowedForString(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            s String <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).contains(FieldAttributes.ID)
  }

  @Test
  fun testFieldAttributeNoIdForCompositeTypes(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            m1 M1 <caret>
          }
          model M1 {
              id Int @id
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.ID)
  }

  @Test
  fun testFieldAttributeUpdatedAtForDateTime(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            updated DateTime <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).contains(FieldAttributes.UPDATED_AT)
  }

  @Test
  fun testFieldAttributeNoIdIfBlockAttributeExists(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          model M {
            id Int <caret>
            
            @@id([id])
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.ID)
  }

  @Test
  fun testFieldAttributesEnum(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Role {
            ADMIN @map("admin")
            USER @map("user")
            GUEST @<caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(FieldAttributes.MAP)
  }

  @Test
  fun testFieldAttributesEnumWithoutAt(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          enum Role {
            ADMIN @map("admin")
            USER @map("user")
            GUEST <caret>
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(FieldAttributes.MAP)
  }

  @Test
  fun testFieldAttributesCompositeType(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
          type Comp {
            id Int @<caret>
            name String
            location String
          }
      """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(FieldAttributes.DEFAULT, FieldAttributes.MAP)
  }

  @Test
  fun testFieldAttributeShardKey(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements("""
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
        email String  @unique <caret>
        name  String?
      }
    """.trimIndent())
    assertThat(lookupElements.strings).contains(FieldAttributes.SHARD_KEY)
  }

  @Test
  fun testFieldAttributeShardKeySkipRelations(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements("""
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
        posts Post[] <caret>
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
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.SHARD_KEY)
  }

  @Test
  fun testFieldAttributeShardKeySkipOptional(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements("""
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
        name  String? <caret>
      }
    """.trimIndent())
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.SHARD_KEY)
  }

  @Test
  fun testFieldAttributeShardKeyOnlyMysql(): Unit = timeoutRunBlocking {
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
        email String  @unique <caret>
        name  String?
      }
    """.trimIndent())
    assertThat(lookupElements.strings).doesNotContain(FieldAttributes.SHARD_KEY)
  }
}