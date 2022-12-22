package org.intellij.prisma.completion

import org.intellij.prisma.lang.PrismaConstants.FieldAttributes

class PrismaFieldAttributesCompletionTest : PrismaCompletionTestBase() {
  override fun getBasePath(): String = "/completion/fieldAttributes"

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
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT,
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
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.UNIQUE
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
      FieldAttributes.ALL - FieldAttributes.ID - FieldAttributes.UPDATED_AT - FieldAttributes.MAP
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
}