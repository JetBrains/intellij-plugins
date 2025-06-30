package org.intellij.prisma.completion

import org.intellij.prisma.lang.PrismaConstants.FieldAttributes

class PrismaFieldAttributesCompletionTest : PrismaCompletionTestBase("completion/fieldAttributes") {
  fun testFieldAttributes() {
    val lookupElements = completeSelected(
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
    assertSameElements(
      lookupElements.strings,
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.SHARD_KEY,
    )

    checkLookupDocumentation(lookupElements, FieldAttributes.RELATION)
  }

  fun testFieldAttributeAfterAt() {
    completeSelected(
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

  fun testFieldAttributeWithBeforeNextField() {
    completeSelected(
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

  fun testFieldAttributeExistingAttribute() {
    completeSelected(
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

  fun testFieldAttributeAfterAnother() {
    val lookupElements = completeSelected(
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
    assertSameElements(
      lookupElements.strings,
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.UNIQUE - FieldAttributes.SHARD_KEY,
    )
    checkLookupDocumentation(lookupElements, FieldAttributes.MAP)
  }

  fun testFieldAttributeBeforeAnother() {
    val lookupElements = completeSelected(
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
    assertSameElements(
      lookupElements.strings,
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.MAP - FieldAttributes.SHARD_KEY
    )
  }

  fun testFieldAttributeForCompositeTypeField() {
    val lookupElements = getLookupElements(
      """
          type T {}
          model M {
            type T <caret>
          }
      """.trimIndent()
    )
    assertDoesntContain(lookupElements.strings, FieldAttributes.DEFAULT, FieldAttributes.RELATION)
    assertSameElements(
      lookupElements.strings,
      FieldAttributes.IGNORE, FieldAttributes.MAP, FieldAttributes.UNIQUE
    )
  }

  fun testFieldAttributeIdAllowedForEnum() {
    val lookupElements = getLookupElements(
      """
          enum Lang {}
          model M {
            lang Lang <caret>
          }
      """.trimIndent()
    )
    assertContainsElements(lookupElements.strings, FieldAttributes.ID)
  }

  fun testFieldAttributeIdAllowedForInt() {
    val lookupElements = getLookupElements(
      """
          model M {
            i Int <caret>
          }
      """.trimIndent()
    )
    assertContainsElements(lookupElements.strings, FieldAttributes.ID)
  }

  fun testFieldAttributeIdAllowedForString() {
    val lookupElements = getLookupElements(
      """
          model M {
            s String <caret>
          }
      """.trimIndent()
    )
    assertContainsElements(lookupElements.strings, FieldAttributes.ID)
  }

  fun testFieldAttributeNoIdForCompositeTypes() {
    val lookupElements = getLookupElements(
      """
          model M {
            m1 M1 <caret>
          }
          model M1 {
              id Int @id
          }
      """.trimIndent()
    )
    assertDoesntContain(lookupElements.strings, FieldAttributes.ID)
  }

  fun testFieldAttributeUpdatedAtForDateTime() {
    val lookupElements = getLookupElements(
      """
          model M {
            updated DateTime <caret>
          }
      """.trimIndent()
    )
    assertContainsElements(lookupElements.strings, FieldAttributes.UPDATED_AT)
  }

  fun testFieldAttributeNoIdIfBlockAttributeExists() {
    val lookupElements = getLookupElements(
      """
          model M {
            id Int <caret>
            
            @@id([id])
          }
      """.trimIndent()
    )
    assertDoesntContain(lookupElements.strings, FieldAttributes.ID)
  }

  fun testFieldAttributesEnum() {
    val lookupElements = getLookupElements(
      """
          enum Role {
            ADMIN @map("admin")
            USER @map("user")
            GUEST @<caret>
          }
      """.trimIndent()
    )
    assertSameElements(lookupElements.strings, FieldAttributes.MAP)
  }

  fun testFieldAttributesEnumWithoutAt() {
    val lookupElements = getLookupElements(
      """
          enum Role {
            ADMIN @map("admin")
            USER @map("user")
            GUEST <caret>
          }
      """.trimIndent()
    )
    assertSameElements(lookupElements.strings, FieldAttributes.MAP)
  }

  fun testFieldAttributesCompositeType() {
    val lookupElements = getLookupElements(
      """
          type Comp {
            id Int @<caret>
            name String
            location String
          }
      """.trimIndent()
    )
    assertSameElements(lookupElements.strings, FieldAttributes.DEFAULT, FieldAttributes.MAP)
  }

  fun testFieldAttributeShardKey() {
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
        email String  @unique <caret>
        name  String?
      }
    """.trimIndent())
    assertContainsElements(lookupElements.strings, FieldAttributes.SHARD_KEY)
  }

  fun testFieldAttributeShardKeySkipRelations() {
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
    assertDoesntContain(lookupElements.strings, FieldAttributes.SHARD_KEY)
  }

  fun testFieldAttributeShardKeySkipOptional() {
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
        name  String? <caret>
      }
    """.trimIndent())
    assertDoesntContain(lookupElements.strings, FieldAttributes.SHARD_KEY)
  }

  fun testFieldAttributeShardKeyOnlyMysql() {
    val lookupElements = getLookupElements("""
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
    assertDoesntContain(lookupElements.strings, FieldAttributes.SHARD_KEY)
  }
}