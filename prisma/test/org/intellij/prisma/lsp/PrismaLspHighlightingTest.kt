// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lsp

import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.platform.lsp.tests.checkLspHighlighting

class PrismaLspHighlightingTest : JSTempDirWithNodeInterpreterTest() {
  fun testCreateEnumQuickFix() {
    myFixture.configureByText("foo.prisma", """
      model User {
        name <error descr="Type \"Foo\" is neither a built-in type, nor refers to another model, custom type, or enum.">Foo</error><caret>
      }

    """.trimIndent())
    myFixture.checkLspHighlighting()
    myFixture.launchAction(myFixture.findSingleIntention("Create new enum 'Foo'"))
    myFixture.checkResult("""
      model User {
        name Foo<caret>
      }

      enum Foo {

      }

    """.trimIndent())
  }
}