// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.service.LspTypeScriptServiceFixture
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule
import org.intellij.prisma.ide.lsp.PrismaLspServerLoader
import org.intellij.prisma.ide.lsp.PrismaServiceMode
import org.intellij.prisma.ide.lsp.PrismaServiceSettings
import java.lang.reflect.Method

/**
 * The fixture of the Prisma language server tests.
 * Create it with [prismaServiceFixture].
 *
 * The setup turns on the Prisma language server and downloads it when it is missing.
 * The disposal of the test root disposable restores the Prisma service mode and the server activation.
 */
class PrismaServiceFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSTempDirFixtureOptions,
) : LspTypeScriptServiceFixture(fixture, testClass, testMethod, options) {

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

  companion object {
    /**
     * Creates a [PrismaServiceFixture] on a new project, for the test data in [basePath] under the repository root.
     * Declare the result as an instance property, so that each test gets its own project.
     */
    fun prismaServiceFixture(basePath: String = ""): TestFixture<PrismaServiceFixture> =
      jsTempDirFixture("prismaServiceFixture", JSTempDirFixtureOptions.forBasePath(basePath), ::PrismaServiceFixture)
  }
}
