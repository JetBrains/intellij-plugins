// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.service.PrismaServiceFixture.Companion.prismaServiceFixture
import org.junit.jupiter.api.Test

@TestApplication
class PrismaHighlightingServiceTest {
  private val prisma by prismaServiceFixture()

  @Test
  fun testNoDiagnosticsInContract() {
    prisma.fixture.configureByText("contract.prisma", """
      // use prisma-8
      model User {
        name Foo
      }

    """.trimIndent())
    runInEdtAndWait { prisma.fixture.checkLspHighlighting() }
  }

  @Test
  fun testNoDiagnosticsInContractNextToPrisma7Schema() {
    val contract = prisma.fixture.configureByText("contract.prisma", "// use prisma-8\nmodel Account {\n  user Missing\n}\n").virtualFile
    runInEdtAndWait { waitForDiagnosticsFromLspServer(prisma.project, contract) }

    // The lint of this Prisma 7 file reads the contract from disk and also publishes a Prisma 7 error for `Missing` in it
    prisma.fixture.configureByText("schema.prisma", """
      model User {
        name <error descr="Type \"Foo\" is neither a built-in type, nor refers to another model, composite type, or enum.">Foo</error>
      }

    """.trimIndent())
    runInEdtAndWait { prisma.fixture.checkLspHighlighting() }

    runInEdtAndWait { prisma.fixture.openFileInEditor(contract) }
    assertThat(prisma.fixture.doHighlighting(HighlightSeverity.ERROR)).isEmpty()
  }

  @Test
  fun testCreateEnumQuickFix() {
    prisma.fixture.configureByText("foo.prisma", """
      model User {
        name <error descr="Type \"Foo\" is neither a built-in type, nor refers to another model, composite type, or enum.">Foo</error><caret>
      }

    """.trimIndent())
    runInEdtAndWait { prisma.fixture.checkLspHighlighting() }
    prisma.fixture.launchAction(prisma.fixture.findSingleIntention("Create new enum 'Foo'"))
    prisma.fixture.checkResult("""
      model User {
        name Foo<caret>
      }
      enum Foo {

      }
      
    """.trimIndent())
  }
}