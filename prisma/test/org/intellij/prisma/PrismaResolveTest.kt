package org.intellij.prisma

import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.prisma.lang.psi.PrismaNamedElement

private const val CARET = "<caret>"
private const val TARGET = "<target>"

class PrismaResolveTest : PrismaTestCase("resolve") {
  fun testTypeReference() {
    checkLocalResolve(
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
    checkLocalResolve(
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
    checkLocalResolve("""
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
    checkLocalResolve("""
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
    checkLocalResolve("""
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
    checkLocalResolve("""
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
    checkLocalResolve("""
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
    checkLocalResolve("""
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

  fun testLocalResolveHasPriority() {
    checkGotoDeclaration("model <caret>Address", dir = true, expectedFileName = getTestFileName())
  }

  fun testGlobalModelResolve() {
    checkGotoDeclaration("model <caret>Address", dir = true, expectedFileName = "address.prisma")
  }

  fun testSchemaNameReference() {
    checkGotoDeclarationFromText(
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

        model Account {
          id Int @id

          @@schema("base-schema")
        }
      """.trimIndent(),
      """@@schema("ba<caret>se-schema")""",
      """schemas  = ["<caret>base-schema", "login"]"""
    )
  }

  private fun checkGotoDeclarationFromText(source: String, fromSignature: String, declarationSignature: String) {
    myFixture.configureByText("schema.prisma", source)
    myFixture.checkGotoDeclaration(fromSignature, declarationSignature)
  }

  private fun checkLocalResolve(source: String): PrismaNamedElement {
    val targetOffset = findExpectedTargetOffset(source)
    val text = source.replace(TARGET, "")
    val file = myFixture.configureByText("schema.prisma", text)
    val reference = file.findReferenceAt(myFixture.caretOffset)
    val resolve = reference?.resolve()
    val expectedTarget =
      PsiTreeUtil.getParentOfType(file.findElementAt(targetOffset), PrismaNamedElement::class.java)
    assertNotNull(resolve)
    assertNotNull(expectedTarget)
    assertEquals(expectedTarget, resolve)
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