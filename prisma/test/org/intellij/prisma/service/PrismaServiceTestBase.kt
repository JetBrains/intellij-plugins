// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspClientManagerListener
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.testFramework.PlatformTestUtil
import org.intellij.prisma.ide.lsp.PrismaLspIntegrationProvider
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule
import org.intellij.prisma.ide.lsp.PrismaLspServerLoader
import org.intellij.prisma.ide.lsp.PrismaServiceMode
import org.intellij.prisma.ide.lsp.PrismaServiceSettings
import java.util.Collections

abstract class PrismaServiceTestBase : BaseLspTypeScriptServiceTest() {
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

  override fun tearDown() {
    try {
      stopPrismaLspServers()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  /**
   * The tests start a real `@prisma/language-server` Node process (via [PrismaLspIntegrationProvider]) but nothing stops it,
   * so its process/reader threads survive into the platform thread-leak checker. Stop the client(s) and wait until they
   * actually reach a shutdown state before `super.tearDown()`.
   */
  private fun stopPrismaLspServers() {
    val lspManager = LspClientManager.getInstance(project)
    val clients = lspManager.getClients(PrismaLspIntegrationProvider::class.java)
    if (clients.isEmpty()) return

    val disposable = Disposer.newDisposable()
    try {
      val shutdownClients = Collections.synchronizedSet(HashSet<LspClient>())
      lspManager.addListener(object : LspClientManagerListener {
        override fun serverStateChanged(lspClient: LspClient) {
          if (lspClient.state == LspServerState.ShutdownNormally ||
              lspClient.state == LspServerState.ShutdownUnexpectedly) {
            shutdownClients.add(lspClient)
          }
        }
      }, disposable, sendEventsForExistingClients = true)

      lspManager.stopClients(PrismaLspIntegrationProvider::class.java)

      PlatformTestUtil.waitWithEventsDispatching(
        "Prisma LSP servers did not shut down within timeout",
        { shutdownClients.size >= clients.size },
        10, // seconds
      )
    }
    finally {
      Disposer.dispose(disposable)
    }
  }
}