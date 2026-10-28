// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.completion.PrismaCompletionFixture
import org.intellij.prisma.completion.prismaCompletionFixture
import org.intellij.prisma.completion.strings
import org.intellij.prisma.lang.isPrismaContractText
import org.junit.jupiter.api.Test

@TestApplication
class PrismaContractTest {
  private val prisma: PrismaCompletionFixture by prismaCompletionFixture("contract")

  @Test
  fun testDirective() {
    assertThat(isPrismaContractText("// use prisma-8\nmodel M {}")).isTrue()
    assertThat(isPrismaContractText("// use prisma-next")).isTrue()
    assertThat(isPrismaContractText("//use  prisma-8 ")).isTrue()
    assertThat(isPrismaContractText("\n\n  // use prisma-8\n")).isTrue()

    assertThat(isPrismaContractText("/// use prisma-8")).isFalse()
    assertThat(isPrismaContractText("// use prisma-80")).isFalse()
    assertThat(isPrismaContractText("// use prisma-7")).isFalse()
    assertThat(isPrismaContractText("model M {}\n// use prisma-8")).isFalse()
  }

  @Test
  fun testNoPrisma7Keywords(): Unit = timeoutRunBlocking {
    assertThat(complete("// use prisma-8\nmodel M {}\n<caret>"))
      .contains("model", "enum")
      .doesNotContain("datasource", "generator")
  }

  @Test
  fun testNoNativeTypeAttributes(): Unit = timeoutRunBlocking {
    addDatasource()
    assertThat(complete("model M {\n  id String <caret>\n}")).contains("@db")
    assertThat(complete("// use prisma-8\nmodel M {\n  id String <caret>\n}")).doesNotContain("@db")
  }

  @Test
  fun testNoNativeTypes(): Unit = timeoutRunBlocking {
    addDatasource()
    assertThat(complete("model M {\n  id String @db.<caret>\n}")).contains("VarChar")
    assertThat(complete("// use prisma-8\nmodel M {\n  id String @db.<caret>\n}")).isEmpty()
  }

  private fun addDatasource() {
    prisma.fixture.addFileToProject("datasource.prisma", "datasource db {\n  provider = \"postgresql\"\n}")
  }

  private suspend fun complete(text: String): List<String> = prisma.getLookupElements(text).strings
}
