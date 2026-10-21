package org.intellij.prisma.completion

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.junit.jupiter.api.Test

@TestApplication
class PrismaDefaultAttributeCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/defaultAttribute")

  @Test
  fun testCockroach(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.DBGENERATED, Functions.SEQUENCE)
  }

  @Test
  fun testCockroachBigIntField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.DBGENERATED, Functions.SEQUENCE, Functions.AUTOINCREMENT)
  }

  @Test
  fun testSequence(): Unit = timeoutRunBlocking {
    prisma.completeSelected(
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

  @Test
  fun testSequenceAfterVirtual(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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

    assertThat(lookupElements.strings).doesNotContain("virtual")
  }

  @Test
  fun testSequenceNoVirtualAfterParam(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
                datasource db {
                  provider = "cockroachdb"
                }
                model M {
                    id Int @default(sequence(minValue: 100, <caret>))
                }
            """.trimIndent()
    )

    assertThat(lookupElements.strings).doesNotContain("virtual")
    assertThat(lookupElements.strings).contains("maxValue")
  }

  @Test
  fun testMySQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.DBGENERATED, Functions.AUTOINCREMENT)
  }

  @Test
  fun testAutoincrementOnlyOnNumeric(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
                datasource db {
                  provider = "mysql"
                }
                model M {
                    id String @default(<caret>)
                }
            """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(Functions.AUTOINCREMENT)
  }

  @Test
  fun testMongo(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.AUTO, Functions.AUTOINCREMENT)
  }

  @Test
  fun testNow(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).doesNotContain(Functions.SEQUENCE, Functions.CUID)
    assertThat(lookupElements.strings).contains(Functions.DBGENERATED)
    prisma.checkLookupDocumentation(lookupElements, Functions.NOW)
  }

  @Test
  fun testCuid(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {
                  name String @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  name String @default(cuid(<caret>))
                }
            """.trimIndent(),
      Functions.CUID
    )
    assertThat(lookupElements.strings).contains(Functions.CUID, Functions.UUID, Functions.NANOID, Functions.ULID)
  }

  @Test
  fun testCuidNullableType(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {
                  name String? @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  name String? @default(cuid(<caret>))
                }
            """.trimIndent(),
      Functions.CUID
    )
    assertThat(lookupElements.strings).contains(Functions.CUID, Functions.UUID, Functions.NANOID, Functions.ULID)
  }

  @Test
  fun testCuidVersions(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {
                  name String @default(cuid(<caret>))
                }
            """.trimIndent(), """
                model M {
                  name String @default(cuid(2<caret>))
                }
            """.trimIndent(),
      "2"
    )
    assertThat(lookupElements.strings).contains("1", "2")
  }

  @Test
  fun testForListType(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(Functions.DBGENERATED)
  }

  @Test
  fun testForBoolean(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
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
    assertThat(lookupElements.strings).contains("true", "false")
  }

  @Test
  fun testForBooleanNullable(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
                model M {
                  active Boolean? @default(<caret>)
                }
            """.trimIndent(), """
                model M {
                  active Boolean? @default(false)
                }
            """.trimIndent(),
      "false"
    )
    assertThat(lookupElements.strings).contains("true", "false")
  }
}