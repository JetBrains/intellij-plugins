package org.intellij.prisma

import com.intellij.psi.util.PsiTreeUtil
import junit.framework.TestCase
import org.intellij.prisma.lang.psi.PrismaNamedElement

private const val CARET = "<caret>"
private const val TARGET = "<target>"

class PrismaResolveTest : PrismaTestCase() {
  fun testTypeReference() {
    checkWithTarget(
      """
            model Post {
              id        Int      @id @default(autoincrement())
              author    Us<caret>er?    @relation(fields: [authorId], references: [id])
            }
            
            model <target>User {
              id    Int     @id @default(autoincrement())
            }
        """.trimIndent()
    )
  }

  fun testTypeReferenceIgnoreConfigurationBlocks() {
    checkWithTarget(
      """
            datasource Database {
              provider = "sqlite"
            }    
                
            model Post {
              id Int
              db Dat<caret>abase
            }
            
            model <target>Database {
              id Int
            }
        """.trimIndent()
    )
  }

  fun testFieldExpressionReference() {
    checkWithTarget("""
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id Int @id
              <target>name String
            
              @@index([na<caret>me(ops: TextMinMaxOps)], type: Brin)
            }
        """.trimIndent())
  }

  fun testFieldReference() {
    checkWithTarget("""
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id Int @id
              <target>name String
            
              @@index([na<caret>me], type: Brin)
            }
        """.trimIndent())
  }

  fun testFieldReferenceNamedArgument() {
    checkWithTarget("""
            datasource db {
              provider = "postgresql"
            }
            model Post {
              id Int @id
              <target>name String
            
              @@index(fields: [na<caret>me], type: Brin)
            }
        """.trimIndent())
  }

  fun testRelationAttributeReferences() {
    checkWithTarget("""
            model Post {
              id       Int    @id @default(autoincrement())
              title    String
              author   User   @relation(fields: [authorId], references: [i<caret>d], onDelete: Cascade)
              authorId Int
            }
            
            model User {
              <target>id    Int    @id @default(autoincrement())
              posts Post[]
            }
        """.trimIndent())
  }

  fun testBlockAttributeUniqueCompositeType() {
    checkWithTarget("""
            datasource db {
              provider = "mongodb"
            }

            type City {
              <target>name String
            }

            type Address {
              number Int
              city   City
            }

            model User {
              id      Int     @id @map("_id")
              address Address

              @@unique([address.city.na<caret>me])
            }
        """.trimIndent())
  }

  fun testBlockAttributeIndexCompositeType() {
    checkWithTarget("""
            datasource db {
              provider = "mongodb"
            }

            type City {
              <target>name String
            }

            type Address {
              number Int
              city   City
            }

            model User {
              id      Int     @id @map("_id")
              address Address

              @@index([address.city.na<caret>me])
            }
        """.trimIndent())
  }

  private fun checkWithTarget(source: String): PrismaNamedElement {
    val targetOffset = findExpectedTargetOffset(source)
    val text = source.replace(TARGET, "")
    val file = myFixture.configureByText("schema.prisma", text)
    val reference = file.findReferenceAt(myFixture.caretOffset)
    val resolve = reference?.resolve()
    val expectedTarget =
      PsiTreeUtil.getParentOfType(file.findElementAt(targetOffset), PrismaNamedElement::class.java)
    TestCase.assertNotNull(resolve)
    TestCase.assertNotNull(expectedTarget)
    TestCase.assertEquals(expectedTarget, resolve)
    return resolve as PrismaNamedElement
  }

  private fun findExpectedTargetOffset(source: String): Int {
    val targetOffset = source.indexOf(TARGET)
    val caretOffset = source.indexOf(CARET)
    check(targetOffset >= 0 && caretOffset >= 0)
    return if (targetOffset > caretOffset) {
      targetOffset - CARET.length
    }
    else {
      targetOffset
    }
  }
}