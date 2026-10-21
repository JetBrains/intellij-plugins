package org.intellij.prisma.completion

import com.intellij.openapi.application.readAction
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.builder.PrismaSchemaEvaluationContext
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.intellij.prisma.lang.PrismaConstants.FieldAttributes
import org.intellij.prisma.lang.PrismaConstants.ParameterNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class PrismaAttributeParametersCompletionTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("completion/attributeParameters")

  @Test
  fun testBlockAttributeId(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
            model M {
              @@id(<caret>)
            }
        """.trimIndent(), """
            model M {
              @@id(fields: [<caret>])
            }
        """.trimIndent(),
      ParameterNames.FIELDS
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.NAME, ParameterNames.CLUSTERED)
    prisma.checkLookupDocumentation(lookupElements, ParameterNames.FIELDS)

    val presentation = lookupElements.find(ParameterNames.FIELDS).presentation!!
    assertEquals("FieldReference[]", presentation.typeText)
  }

  @Test
  fun testBlockAttributeIdFiltered(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
            model M {
              @@id(fields: [], <caret>)
            }
        """.trimIndent(), """
            model M {
              @@id(fields: [], map: "<caret>")
            }
        """.trimIndent(),
      ParameterNames.MAP
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.MAP, ParameterNames.NAME, ParameterNames.CLUSTERED)
    prisma.checkLookupDocumentation(lookupElements, ParameterNames.MAP)
  }

  @Test
  fun testBlockAttributeIdComplete() {
    prisma.completeBasic(
      """
            model M {
              @@id(fie<caret>)
            }
        """.trimIndent(),
      """
            model M {
              @@id(fields: [<caret>])
            }
        """.trimIndent(),
    )
  }

  @Test
  fun testBlockAttributeIdNoParamsInList(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            model M {
              @@id(fields: [<caret>])
            }
        """.trimIndent()
    )
    val expectedParams = getBlockAttributeParams(BlockAttributes.ID)
    assertThat(lookupElements.strings).doesNotContainAnyElementsOf(expectedParams)
  }

  @Test
  fun testBlockAttributeIdMySQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              @@id(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.NAME)
  }

  @Test
  fun testBlockAttributeIdMySQLOnField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              id String
            
              @@id([id(<caret>)])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.LENGTH)
  }

  @Test
  fun testBlockAttributeIdMySQLNoLengthInArray(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              id String
            
              @@id([id(), <caret>])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(ParameterNames.LENGTH)
  }

  @Test
  fun testBlockAttributeIdSQLServer(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@id(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.NAME, ParameterNames.CLUSTERED)
  }

  @Test
  fun testBlockAttributeIdSQLServerOnField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              id String
              
              @@id([id(<caret>)])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.SORT)
  }

  @Test
  fun testBlockAttributeIndexPostgreSQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.TYPE)
  }

  @Test
  fun testBlockAttributeIndexMySQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP)
  }

  @Test
  fun testBlockAttributeIndexMySQLOnField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              id String
            
              @@index([id(<caret>)])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.LENGTH, ParameterNames.SORT)
  }

  @Test
  fun testBlockAttributeIndexSQLServer(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.CLUSTERED)
  }

  @Test
  fun testBlockAttributeUniqueSQLServer(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@unique(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.NAME, ParameterNames.CLUSTERED)
  }

  @Test
  fun testBlockAttributeUniqueMySQLOnField(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              id String
            
              @@unique([id(<caret>)])
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.LENGTH, ParameterNames.SORT)
  }

  @Test
  fun testFieldAttributeRelation(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @relation(<caret>)
            }
        """.trimIndent(), """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @relation(fields: [<caret>])
            }
        """.trimIndent(),
      ParameterNames.FIELDS
    )
    val expectedParams = getFieldAttributeParams(FieldAttributes.RELATION)
    assertThat(lookupElements.strings).containsExactlyInAnyOrderElementsOf(expectedParams)
    prisma.checkLookupDocumentation(lookupElements, ParameterNames.FIELDS)
  }

  @Test
  fun testFieldAttributeRelationOnUpdate(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.completeSelected(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @relation(on<caret>)
            }
        """.trimIndent(), """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @relation(onUpdate: <caret>)
            }
        """.trimIndent(),
      ParameterNames.ON_UPDATE
    )
    prisma.checkLookupDocumentation(lookupElements, ParameterNames.ON_UPDATE)
  }

  @Test
  fun testFieldAttributeRelationMongoDB(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mongodb"
            }
            model M {
              user User @relation(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).doesNotContain(ParameterNames.ON_UPDATE, ParameterNames.ON_DELETE, ParameterNames.MAP)
  }

  @Test
  fun testFieldAttributeIdMySQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @id(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.LENGTH, ParameterNames.MAP)
  }

  @Test
  fun testFieldAttributeIdSQLServer(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              user User @id(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.MAP, ParameterNames.SORT, ParameterNames.CLUSTERED)
  }

  @Test
  fun testFieldAttributeUniqueMySQL(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @unique(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.LENGTH, ParameterNames.MAP, ParameterNames.SORT)
  }

  @Test
  fun testFieldAttributeUniqueSQLServer(): Unit = timeoutRunBlocking {
    val lookupElements = prisma.getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              user User @unique(<caret>)
            }
        """.trimIndent()
    )
    assertThat(lookupElements.strings).containsExactlyInAnyOrder(ParameterNames.CLUSTERED, ParameterNames.MAP, ParameterNames.SORT)
  }

  @Test
  fun testFieldAttributeDefaultMySQL(): Unit = timeoutRunBlocking {
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
    assertThat(lookupElements.strings).doesNotContain(ParameterNames.EXPRESSION)
  }

  private suspend fun getBlockAttributeParams(label: String) = getAttributeParams(PrismaSchemaKind.BLOCK_ATTRIBUTE, label)

  private suspend fun getFieldAttributeParams(label: String) = getAttributeParams(PrismaSchemaKind.FIELD_ATTRIBUTE, label)

  private suspend fun getAttributeParams(kind: PrismaSchemaKind, label: String) = readAction {
    PrismaSchemaProvider
      .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(prisma.fixture.file.findElementAt(prisma.fixture.caretOffset)))
      .getElement(kind, label)!!.params.map { it.label }
  }
}