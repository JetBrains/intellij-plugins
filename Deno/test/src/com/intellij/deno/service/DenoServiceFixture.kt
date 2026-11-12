// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.model.hasJsrImportPrefix
import com.intellij.deno.model.hasNpmImportPrefix
import com.intellij.deno.setUseDenoLibrary
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.debugger.DenoDownloader
import com.intellij.javascript.debugger.relativeDenoPath
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.lang.javascript.modules.JSNodeInterpreterFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspClientManagerListener
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.utils.io.deleteRecursively
import com.intellij.util.ui.UIUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

const val stdVersion = "std@0.187.0"
const val stdPathUrl = "https://deno.land/$stdVersion/path/mod.ts"

/**
 * The fixture of the Deno language server tests.
 * Create it with [denoServiceFixture].
 *
 * The setup downloads Deno 1.46.3, makes the project configure Deno automatically, and turns on the TypeScript service.
 * The disposal of the test root disposable restores the Deno mode and turns off the TypeScript service.
 */
class DenoServiceFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSTempDirFixtureOptions,
) : JSNodeInterpreterFixture(fixture, testClass, testMethod, options) {
  override fun setUp() {
    super.setUp()
    val denoPath = DenoDownloader("v1.46.3").downloadAndUnpack().resolve(relativeDenoPath())
    assertTrue(Files.exists(denoPath), "Deno is not found at $denoPath")
    val service = DenoSettings.getService(project)
    service.setDenoPath(denoPath.toString())
    val before = service.getUseDeno()
    runInEdtAndWait { service.setUseDenoAndReload(UseDeno.CONFIGURE_AUTOMATICALLY) }
    (fixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    TypeScriptLanguageServiceUtil.setUseService(true)
    setUseDenoLibrary(project, true, false)
    Disposer.register(testRootDisposable) {
      TypeScriptLanguageServiceUtil.setUseService(false)
      service.setUseDeno(before)
    }
    val denoCache = Path.of(service.getDenoCache())
    if (Files.exists(denoCache)) {
      VfsRootAccess.allowRootAccess(testRootDisposable, denoCache.toAbsolutePath().toString())
    }
  }

  /** Runs [command], checks that it exits with 0, and updates the Deno libraries. */
  fun runSimpleCommandLine(command: String) {
    val cmd = GeneralCommandLine(command.split(" "))
    val processHandler = CapturingProcessHandler(cmd)
    val output = processHandler.runProcess()
    assertEquals(0, output.getExitCode(), output.stderr)
    updateCaches()
  }

  /** Deletes the contents of the Deno dependency and npm caches, and checks that the VFS sees empty directories. */
  fun deleteDenoCache() {
    val service = DenoSettings.getService(project)
    val depsCache = Path.of(service.getDenoCacheDeps())
    if (Files.exists(depsCache)) {
      depsCache.deleteRecursively()
      Files.createDirectories(depsCache)
    }

    val npmCache = Path.of(service.getDenoNpm())
    if (Files.exists(npmCache)) {
      npmCache.deleteRecursively()
      Files.createDirectories(npmCache)
    }
    updateCaches()

    //check the vfs refresh result
    val deps = StandardFileSystems.local().findFileByPath(service.getDenoCacheDeps())
    if (deps != null) {
      assertTrue(deps.children.isEmpty())
    }
    val npm = StandardFileSystems.local().findFileByPath(service.getDenoNpm())
    if (npm != null) {
      assertTrue(npm.children.isEmpty())
    }
  }

  private fun updateCaches() {
    val service = DenoSettings.getService(project)
    runInEdtAndWait { service.updateLibraries() }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private fun createServiceRestartCounter(): AtomicBoolean {
    val shutdown = AtomicBoolean(false)
    val restartFlag = AtomicBoolean(false)
    LspClientManager.getInstance(project).addListener(object : LspClientManagerListener {
      override fun serverStateChanged(lspClient: LspClient) {
        if (lspClient.descriptor !is DenoLspClientDescriptor) return

        if (lspClient.state == LspServerState.ShutdownNormally ||
            lspClient.state == LspServerState.ShutdownUnexpectedly) {
          shutdown.set(true)
        }
        if ((lspClient.state == LspServerState.Initializing || lspClient.state == LspServerState.Running) && shutdown.get()) {
          restartFlag.set(true)
        }
      }

      override fun fileOpened(lspClient: LspClient, file: VirtualFile) {}
      override fun diagnosticsReceived(lspClient: LspClient, file: VirtualFile) {}

    }, testRootDisposable, false)

    return restartFlag
  }

  private fun waitForServiceRestart(restartFlag: AtomicBoolean) {
    val start = System.currentTimeMillis()
    runInEdtAndWait {
      while (!restartFlag.get() && (System.currentTimeMillis() - start) < 20_000) {
        ProgressManager.checkCanceled()
        UIUtil.dispatchAllInvocationEvents()
      }
    }

    if (!restartFlag.get()) {
      fail("Service did not restart")
    }
  }

  /** Saves [virtualFile], invokes the cache or install quick fix for [urlToCache], and waits for the restart of the Deno server. */
  fun invokeCacheCommand(virtualFile: VirtualFile?, urlToCache: String) {
    assertNotNull(virtualFile)

    val manager = FileDocumentManager.getInstance()
    runInEdtAndWait {
      val document = manager.getDocument(virtualFile!!)
      WriteAction.run<RuntimeException> { manager.saveDocument(document!!) }
    }

    val restartFlag = createServiceRestartCounter()
    val installPrefix = hasJsrImportPrefix(urlToCache) || hasNpmImportPrefix(urlToCache)

    JSImportTestUtil.findAndInvokeIntentionAction(fixture,
                                                  "${if (installPrefix) "Install" else "Cache"} \"$urlToCache\" and its dependencies.", false)
    waitForServiceRestart(restartFlag)
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  /** Creates the expected highlighting data of the markup in [afterText]. */
  fun createExpectedDataFromText(afterText: String): ExpectedHighlightingData {
    val document = DocumentImpl(StreamUtil.convertSeparators(afterText))
    val data = ExpectedHighlightingData(document, true, true, false)
    data.init()
    return data
  }

  companion object {
    /**
     * Creates a [DenoServiceFixture] on a new project.
     * Declare the result as an instance property, so that each test gets its own project.
     */
    fun denoServiceFixture(): TestFixture<DenoServiceFixture> =
      jsTempDirFixture("denoServiceFixture", JSTempDirFixtureOptions.forBasePath(""), ::DenoServiceFixture)
  }
}
