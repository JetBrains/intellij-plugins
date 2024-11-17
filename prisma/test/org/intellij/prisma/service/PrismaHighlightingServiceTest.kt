// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.intellij.prisma.ide.lsp.PrismaLspServerLoader
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule
import org.intellij.prisma.ide.lsp.PrismaServiceMode
import org.intellij.prisma.ide.lsp.PrismaServiceSettings

class PrismaHighlightingServiceTest : PrismaServiceTestBase() {
  override fun setUp() {
    super.setUp()

    val serviceSettings = PrismaServiceSettings.getInstance(project)
    val old = serviceSettings.serviceMode
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    PrismaLspServerActivationRule.markForceEnabled(true)

    Disposer.register(testRootDisposable) {
      serviceSettings.serviceMode = old
      PrismaLspServerActivationRule.markForceEnabled(false)
    }
    serviceSettings.serviceMode = PrismaServiceMode.ENABLED

    ensureServerDownloaded(PrismaLspServerLoader)
  }

  fun testCreateEnumQuickFix() {
    myFixture.configureByText("foo.prisma", """
      model User {
        name <error descr="Type \"Foo\" is neither a built-in type, nor refers to another model, composite type, or enum.">Foo</error><caret>
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