// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import org.intellij.prisma.completion.PrismaCompletionTestBase
import org.intellij.prisma.lang.isPrismaContractText

class PrismaContractTest : PrismaCompletionTestBase("contract") {
  fun testDirective() {
    assertTrue(isPrismaContractText("// use prisma-8\nmodel M {}"))
    assertTrue(isPrismaContractText("// use prisma-next"))
    assertTrue(isPrismaContractText("//use  prisma-8 "))
    assertTrue(isPrismaContractText("\n\n  // use prisma-8\n"))

    assertFalse(isPrismaContractText("/// use prisma-8"))
    assertFalse(isPrismaContractText("// use prisma-80"))
    assertFalse(isPrismaContractText("// use prisma-7"))
    assertFalse(isPrismaContractText("model M {}\n// use prisma-8"))
  }

  fun testNoPrisma7Keywords() {
    val lookupStrings = complete("// use prisma-8\nmodel M {}\n<caret>")
    assertContainsElements(lookupStrings, "model", "enum")
    assertDoesntContain(lookupStrings, "datasource", "generator")
  }

  fun testNoNativeTypeAttributes() {
    addDatasource()
    assertContainsElements(complete("model M {\n  id String <caret>\n}"), "@db")
    assertDoesntContain(complete("// use prisma-8\nmodel M {\n  id String <caret>\n}"), "@db")
  }

  fun testNoNativeTypes() {
    addDatasource()
    assertContainsElements(complete("model M {\n  id String @db.<caret>\n}"), "VarChar")
    assertEmpty(complete("// use prisma-8\nmodel M {\n  id String @db.<caret>\n}"))
  }

  private fun addDatasource() {
    myFixture.addFileToProject("datasource.prisma", "datasource db {\n  provider = \"postgresql\"\n}")
  }

  private fun complete(text: String): List<String> = getLookupElements(text).strings
}
