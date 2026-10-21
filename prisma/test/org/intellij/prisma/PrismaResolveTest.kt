package org.intellij.prisma

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.prisma.lang.psi.PrismaNamedElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

private const val CARET = "<caret>"
private const val TARGET = "<target>"

@TestApplication
class PrismaResolveTest {
  private val prisma: PrismaFixture by prismaFixture("resolve")

  @Test
  fun testTypeReference(): Unit = timeoutRunBlocking {
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

  @Test
  fun testTypeReferenceIgnoreConfigurationBlocks(): Unit = timeoutRunBlocking {
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

  @Test
  fun testFieldExpressionReference(): Unit = timeoutRunBlocking {
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

  @Test
  fun testFieldReference(): Unit = timeoutRunBlocking {
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

  @Test
  fun testFieldReferenceNamedArgument(): Unit = timeoutRunBlocking {
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

  @Test
  fun testRelationAttributeReferences(): Unit = timeoutRunBlocking {
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

  @Test
  fun testBlockAttributeUniqueCompositeType(): Unit = timeoutRunBlocking {
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

  @Test
  fun testBlockAttributeIndexCompositeType(): Unit = timeoutRunBlocking {
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

  @Test
  fun testLocalResolveHasPriority() {
    prisma.doGotoDeclarationTest("model <caret>Address", dir = true, expectedFileName = prisma.getTestFileName())
  }

  @Test
  fun testGlobalModelResolve() {
    prisma.doGotoDeclarationTest("model <caret>Address", dir = true, expectedFileName = "address.prisma")
  }

  @Test
  fun testSchemaNameReference(): Unit = timeoutRunBlocking {
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

  private suspend fun checkGotoDeclarationFromText(source: String, fromSignature: String, declarationSignature: String) {
    prisma.fixture.configureByText("schema.prisma", source)
    withContext(Dispatchers.EDT) {
      prisma.fixture.checkGotoDeclaration(fromSignature, declarationSignature)
    }
  }

  private suspend fun checkLocalResolve(source: String): PrismaNamedElement {
    val targetOffset = findExpectedTargetOffset(source)
    val text = source.replace(TARGET, "")
    val file = prisma.fixture.configureByText("schema.prisma", text)
    val (resolve, expectedTarget) = readAction {
      val reference = file.findReferenceAt(prisma.fixture.caretOffset)
      reference?.resolve() to PsiTreeUtil.getParentOfType(file.findElementAt(targetOffset), PrismaNamedElement::class.java)
    }
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