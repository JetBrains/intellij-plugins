package org.intellij.prisma.completion

import junit.framework.TestCase
import org.intellij.prisma.ide.schema.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.intellij.prisma.lang.PrismaConstants.FieldAttributes
import org.intellij.prisma.lang.PrismaConstants.ParameterNames

class PrismaAttributeParametersCompletionTest : PrismaCompletionTestBase() {
  override fun getBasePath(): String = "/completion/attributeParameters"

  fun testBlockAttributeId() {
    val lookupElements = completeSelected(
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
    assertSameElements(
      lookupElements.strings,
      ParameterNames.FIELDS,
      ParameterNames.MAP,
      ParameterNames.NAME,
      ParameterNames.CLUSTERED,
    )
    checkLookupDocumentation(lookupElements, ParameterNames.FIELDS)

    val presentation = lookupElements.find(ParameterNames.FIELDS).presentation!!
    TestCase.assertEquals("FieldReference[]", presentation.typeText)
  }

  fun testBlockAttributeIdFiltered() {
    val lookupElements = completeSelected(
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
    assertSameElements(
      lookupElements.strings,
      ParameterNames.MAP,
      ParameterNames.NAME,
      ParameterNames.CLUSTERED,
    )
    checkLookupDocumentation(lookupElements, ParameterNames.MAP)
  }

  fun testBlockAttributeIdComplete() {
    completeBasic(
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

  fun testBlockAttributeIdNoParamsInList() {
    val lookupElements = getLookupElements(
      """
            model M {
              @@id(fields: [<caret>])
            }
        """.trimIndent()
    )
    val expectedParams = getBlockAttributeParams(BlockAttributes.ID)
    assertDoesntContain(lookupElements.strings, expectedParams)
  }

  fun testBlockAttributeIdMySQL() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              @@id(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(
      lookupElements.strings,
      ParameterNames.FIELDS,
      ParameterNames.MAP,
      ParameterNames.NAME,
    )
  }

  fun testBlockAttributeIdMySQLOnField() {
    val lookupElements = getLookupElements(
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
    assertSameElements(lookupElements.strings, ParameterNames.LENGTH)
  }

  fun testBlockAttributeIdMySQLNoLengthInArray() {
    val lookupElements = getLookupElements(
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
    assertDoesntContain(lookupElements.strings, ParameterNames.LENGTH)
  }

  fun testBlockAttributeIdSQLServer() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@id(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(
      lookupElements.strings,
      ParameterNames.FIELDS,
      ParameterNames.MAP,
      ParameterNames.NAME,
      ParameterNames.CLUSTERED
    )
  }

  fun testBlockAttributeIdSQLServerOnField() {
    val lookupElements = getLookupElements(
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
    assertSameElements(lookupElements.strings, ParameterNames.SORT)
  }

  fun testBlockAttributeIndexPostgreSQL() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "postgresql"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.TYPE)
  }

  fun testBlockAttributeIndexMySQL() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.FIELDS, ParameterNames.MAP)
  }

  fun testBlockAttributeIndexMySQLOnField() {
    val lookupElements = getLookupElements(
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
    assertSameElements(lookupElements.strings, ParameterNames.LENGTH, ParameterNames.SORT)
  }

  fun testBlockAttributeIndexSQLServer() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@index(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.FIELDS, ParameterNames.MAP, ParameterNames.CLUSTERED)
  }

  fun testBlockAttributeUniqueSQLServer() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              @@unique(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(
      lookupElements.strings,
      ParameterNames.FIELDS,
      ParameterNames.MAP,
      ParameterNames.NAME,
      ParameterNames.CLUSTERED
    )
  }

  fun testBlockAttributeUniqueMySQLOnField() {
    val lookupElements = getLookupElements(
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
    assertSameElements(
      lookupElements.strings,
      ParameterNames.LENGTH,
      ParameterNames.SORT,
    )
  }

  fun testFieldAttributeRelation() {
    val lookupElements = completeSelected(
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
    assertSameElements(lookupElements.strings, expectedParams)
    checkLookupDocumentation(lookupElements, ParameterNames.FIELDS)
  }

  fun testFieldAttributeRelationOnUpdate() {
    val lookupElements = completeSelected(
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
    checkLookupDocumentation(lookupElements, ParameterNames.ON_UPDATE)
  }

  fun testFieldAttributeRelationMongoDB() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mongodb"
            }
            model M {
              user User @relation(<caret>)
            }
        """.trimIndent()
    )
    assertDoesntContain(
      lookupElements.strings,
      ParameterNames.ON_UPDATE,
      ParameterNames.ON_DELETE,
      ParameterNames.MAP
    )
  }

  fun testFieldAttributeIdMySQL() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @id(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.LENGTH, ParameterNames.MAP)
  }

  fun testFieldAttributeIdSQLServer() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              user User @id(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.MAP, ParameterNames.SORT, ParameterNames.CLUSTERED)
  }

  fun testFieldAttributeUniqueMySQL() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "mysql"
            }
            model M {
              user User @unique(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(lookupElements.strings, ParameterNames.LENGTH, ParameterNames.MAP, ParameterNames.SORT)
  }

  fun testFieldAttributeUniqueSQLServer() {
    val lookupElements = getLookupElements(
      """
            datasource db {
              provider = "sqlserver"
            }
            model M {
              user User @unique(<caret>)
            }
        """.trimIndent()
    )
    assertSameElements(
      lookupElements.strings,
      ParameterNames.CLUSTERED,
      ParameterNames.MAP,
      ParameterNames.SORT
    )
  }

  fun testFieldAttributeDefaultMySQL() {
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
    assertDoesntContain(lookupElements.strings, ParameterNames.EXPRESSION)
  }

  private fun getBlockAttributeParams(label: String) = getAttributeParams(PrismaSchemaKind.BLOCK_ATTRIBUTE, label)

  private fun getFieldAttributeParams(label: String) = getAttributeParams(PrismaSchemaKind.FIELD_ATTRIBUTE, label)

  private fun getAttributeParams(kind: PrismaSchemaKind, label: String) =
    PrismaSchemaProvider
      .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(myFixture.file.findElementAt(myFixture.caretOffset)))
      .getElement(kind, label)!!.params.map { it.label }
}