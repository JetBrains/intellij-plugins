package org.intellij.prisma.completion

import org.intellij.prisma.lang.PrismaConstants.Functions

class PrismaDefaultAttributeCompletionTest : PrismaCompletionTestBase() {

  override fun getBasePath(): String = "/completion/defaultAttribute"

  fun testCockroach() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(<caret>)
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(<caret>))
                }
            """.trimIndent(),
      Functions.SEQUENCE
    )
    assertSameElements(lookupElements.strings, Functions.DBGENERATED, Functions.SEQUENCE)
  }

  fun testCockroachBigIntField() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id BigInt @default(<caret>)
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id BigInt @default(autoincrement()<caret>)
                }
            """.trimIndent(),
      Functions.AUTOINCREMENT
    )
    assertSameElements(lookupElements.strings, Functions.DBGENERATED, Functions.SEQUENCE, Functions.AUTOINCREMENT)
  }

  fun testSequence() {
    completeSelected(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(<caret>))
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(virtual))
                }
            """.trimIndent(),
      "virtual"
    )
  }

  fun testSequenceAfterVirtual() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(virtual, <caret>))
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(virtual, minValue: <caret>))
                }
            """.trimIndent(),
      "minValue"
    )

    assertDoesntContain(lookupElements.strings, "virtual")
  }

  fun testSequenceNoVirtualAfterParam() {
    val lookupElements = getLookupElements(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(minValue: 100, <caret>))
                }
            """.trimIndent()
    )

    assertDoesntContain(lookupElements.strings, "virtual")
    assertContainsElements(lookupElements.strings, "maxValue")
  }

  fun testMySQL() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "mysql"
                }
                model M {
                    id Int @default(<caret>)
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "mysql"
                }
                model M {
                    id Int @default(dbgenerated("<caret>"))
                }
            """.trimIndent(),
      Functions.DBGENERATED
    )
    assertSameElements(lookupElements.strings, Functions.DBGENERATED, Functions.AUTOINCREMENT)
  }

  fun testAutoincrementOnlyOnNumeric() {
    val lookupElements = getLookupElements(
      """
                datasource db {
                  provider = "mysql"
                }
                model M {
                    id String @default(<caret>)
                }
            """.trimIndent()
    )
    assertDoesntContain(lookupElements.strings, Functions.AUTOINCREMENT)
  }

  fun testMongo() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "mongodb"
                }
                model M {
                    id Int @default(<caret>)
                }
            """.trimIndent(),
      """
                datasource db {
                  provider = "mongodb"
                }
                model M {
                    id Int @default(auto()<caret>)
                }
            """.trimIndent(),
      Functions.AUTO
    )
    assertSameElements(lookupElements.strings, Functions.AUTO, Functions.AUTOINCREMENT)
  }

  fun testNow() {
    val lookupElements = completeSelected(
      """
                model M {
                  createdAt DateTime @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  createdAt DateTime @default(now()<caret>)
                }
            """.trimIndent(),
      Functions.NOW
    )
    assertDoesntContain(lookupElements.strings, Functions.SEQUENCE, Functions.CUID)
    assertContainsElements(lookupElements.strings, Functions.DBGENERATED)
    checkLookupDocumentation(lookupElements, Functions.NOW)
  }

  fun testCuidAndUuid() {
    val lookupElements = completeSelected(
      """
                model M {
                  name String @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  name String @default(cuid()<caret>)
                }
            """.trimIndent(),
      Functions.CUID
    )
    assertContainsElements(lookupElements.strings, Functions.CUID, Functions.UUID)
  }

  fun testForListType() {
    val lookupElements = completeSelected(
      """
                datasource db {
                  provider = "mysql"
                }
                model M {
                  names String[] @default(<caret>)
                }
            """.trimIndent(), """
                datasource db {
                  provider = "mysql"
                }
                model M {
                  names String[] @default(dbgenerated("<caret>"))
                }
            """.trimIndent(),
      Functions.DBGENERATED
    )
    assertSameElements(lookupElements.strings, Functions.DBGENERATED)
  }

  fun testForBoolean() {
    val lookupElements = completeSelected(
      """
                model M {
                  active Boolean @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  active Boolean @default(false)
                }
            """.trimIndent(),
      "false"
    )
    assertContainsElements(lookupElements.strings, "true", "false")
  }
}