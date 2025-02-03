package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.model.hasJsrImportPrefix
import com.intellij.deno.model.hasNpmImportPrefix
import com.intellij.deno.setUseDenoLibrary
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.debugger.DenoAppRule
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerManagerListener
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.utils.io.deleteRecursively
import com.intellij.util.ui.UIUtil
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

const val stdVersion = "std@0.187.0"
const val stdPathUrl = "https://deno.land/$stdVersion/path/mod.ts"

abstract class DenoServiceTestBase : JSTempDirWithNodeInterpreterTest() {
  protected val denoAppRule: DenoAppRule = DenoAppRule.LATEST

  override fun setUp() {
    super.setUp()
    denoAppRule.executeBefore()
    val service = DenoSettings.getService(project)
    service.setDenoPath(denoAppRule.exePath)
    val before = service.getUseDeno()
    service.setUseDenoAndReload(UseDeno.CONFIGURE_AUTOMATICALLY)
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    TypeScriptLanguageServiceUtil.setUseService(true)
    setUseDenoLibrary(project, true, false)
    Disposer.register(testRootDisposable) {
      TypeScriptLanguageServiceUtil.setUseService(false)
      service.setUseDeno(before)
    }
    allowAccessToDirsIfExists(service.getDenoCache())
  }

  protected fun runSimpleCommandLine(command: String) {
    val cmd = GeneralCommandLine(command.split(" "))
    val processHandler = CapturingProcessHandler(cmd)
    val output = processHandler.runProcess()
    assertEquals(output.stderr, 0, output.getExitCode())
    updateCaches()
  }

  protected fun deleteDenoCache() {
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
    val deps = LocalFileSystem.getInstance().findFileByPath(service.getDenoCacheDeps())
    if (deps != null) {
      assertEmpty(deps.children)
    }
    val npm = LocalFileSystem.getInstance().findFileByPath(service.getDenoNpm())
    if (npm != null) {
      assertEmpty(npm.children)
    }
  }

  private fun updateCaches() {
    val service = DenoSettings.getService(project)
    service.updateLibraries()
    IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject())
  }

  protected fun createServiceRestartCounter(): AtomicBoolean {
    val shutdown = AtomicBoolean(false)
    val restartFlag = AtomicBoolean(false)
    LspServerManager.getInstance(project).addLspServerManagerListener(object : LspServerManagerListener {
      override fun serverStateChanged(lspServer: LspServer) {
        if (lspServer.descriptor !is DenoLspServerDescriptor) return

        if (lspServer.state == LspServerState.ShutdownNormally ||
            lspServer.state == LspServerState.ShutdownUnexpectedly) {
          shutdown.set(true)
        }
        if ((lspServer.state == LspServerState.Initializing || lspServer.state == LspServerState.Running) && shutdown.get()) {
          restartFlag.set(true)
        }
      }

      override fun fileOpened(lspServer: LspServer, file: VirtualFile) {}
      override fun diagnosticsReceived(lspServer: LspServer, file: VirtualFile) {}

    }, testRootDisposable, false)

    return restartFlag
  }

  protected fun waitForServiceRestart(restartFlag: AtomicBoolean) {
    val start = System.currentTimeMillis()
    while (!restartFlag.get() && (System.currentTimeMillis() - start) < 20_000) {
      ProgressManager.checkCanceled()
      UIUtil.dispatchAllInvocationEvents()
    }

    if (!restartFlag.get()) {
      fail("Service did not restart")
    }
  }

  protected fun invokeCacheCommand(virtualFile: VirtualFile?, urlToCache: String) {
    assertNotNull(virtualFile)

    val manager = FileDocumentManager.getInstance()
    val document = manager.getDocument(virtualFile!!)
    WriteAction.run<RuntimeException> { manager.saveDocument(document!!) }

    val restartFlag = createServiceRestartCounter()
    val installPrefix = hasJsrImportPrefix(urlToCache) || hasNpmImportPrefix(urlToCache)

    JSImportTestUtil.findAndInvokeIntentionAction(myFixture,
                                                  "${if (installPrefix) "Install" else "Cache"} \"$urlToCache\" and its dependencies.", false)
    waitForServiceRestart(restartFlag)
    IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject())
  }

  protected fun createExpectedDataFromText(afterText: String): ExpectedHighlightingData {
    val document = DocumentImpl(StreamUtil.convertSeparators(afterText));
    val data = ExpectedHighlightingData(document, true, true, false)
    data.init()
    return data
  }
}