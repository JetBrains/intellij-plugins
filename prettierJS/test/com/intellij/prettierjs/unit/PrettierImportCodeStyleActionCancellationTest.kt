// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.linter.JSLinterUtil
import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.NioFiles
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.prettierjs.PrettierConfig
import com.intellij.prettierjs.PrettierImportCodeStyleAction
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.OpenProjectTaskBuilder
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerExtension
import com.intellij.testFramework.replaceService
import com.intellij.util.ui.EDT
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

private const val IMPORT_ACTION_ID = PrettierImportCodeStyleAction.ACTION_ID

/**
 * Checks that [PrettierImportCodeStyleAction] cancels its config lookup when the context file or the project goes away first.
 * The action looks up the config in a non-blocking read action, and a write action defers the start of that lookup.
 * Each test first shows that the deferred lookup imports the config when nothing goes away.
 * The tests use static JSON configs, so the importer applies them without Node.js.
 */
class PrettierImportCodeStyleActionCancellationTest : BasePlatformTestCase() {
  private val installedTabWidths = CopyOnWriteArrayList<Int>()
  private val notifications = CopyOnWriteArrayList<String>()

  override fun setUp() {
    super.setUp()
    ApplicationManager.getApplication().registerExtension(PrettierCodeStyleInstaller.EP_NAME, InstallRecorder(), testRootDisposable)
    recordNotifications(project)
  }

  fun testDeletedContextFileCancelsPendingImport() = withTempCodeStyleSettings(project) { settings ->
    performImportInsideWriteAction(updateImport(project, addConfig("control/.prettierrc.json", 7))) {}
    NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
    assertEquals(listOf(7), installedTabWidths)
    assertEquals(1, notifications.size)
    installedTabWidths.clear()
    notifications.clear()

    val config = addConfig("c/.prettierrc.json", 6)
    val lookups = countLookups(config)
    val importEvent = updateImport(project, config)
    val indentBefore = indentSize(settings)
    val problems = recordLoggedProblems {
      lookups.start()
      performImportInsideWriteAction(importEvent) { config.delete(this) }
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
    }
    assertEquals("lookups of the deleted file", 0, lookups.count)
    assertEmpty(installedTabWidths)
    assertEmpty(notifications)
    assertEmpty(problems)
    assertEquals(indentBefore, indentSize(settings))
  }

  fun testDisposedProjectCancelsPendingImport() {
    val root = FileUtil.createTempDirectory("prettierImport", null).toPath()
    try {
      Files.writeString(Files.createDirectory(root.resolve("control")).resolve(".prettierrc.json"), """{"tabWidth": 7}""")
      Files.writeString(Files.createDirectory(root.resolve("c")).resolve(".prettierrc.json"), """{"tabWidth": 6}""")
      VfsRootAccess.allowRootAccess(testRootDisposable, root.toString())
      val rootDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(root)!!
      val otherProject = ProjectManagerEx.getInstanceEx().newProject(root.resolve("project"), OpenProjectTaskBuilder().build())!!
      try {
        val module = WriteAction.compute<Module, RuntimeException> {
          ModuleManager.getInstance(otherProject).newModule(root.resolve("m.iml"), ModuleTypeManager.getInstance().defaultModuleType.id)
        }
        ModuleRootModificationUtil.addContentRoot(module, rootDir)
        recordNotifications(otherProject)

        // The project has no code style of its own, so the temporary settings keep the import out of the default settings.
        withTempCodeStyleSettings(otherProject) {
          performImportInsideWriteAction(updateImport(otherProject, rootDir.findFileByRelativePath("control/.prettierrc.json")!!)) {}
          NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
        }
        assertEquals(listOf(7), installedTabWidths)
        assertEquals(1, notifications.size)
        installedTabWidths.clear()
        notifications.clear()

        val importEvent = updateImport(otherProject, rootDir.findFileByRelativePath("c/.prettierrc.json")!!)
        val problems = recordLoggedProblems {
          performImportInsideWriteAction(importEvent) {}
          // The project is not open, so the close disposes it in one write action and dispatches no events.
          // Thus, the deferred lookup cannot start before the disposal.
          ProjectManagerEx.getInstanceEx().forceCloseProject(otherProject)
          assertTrue(otherProject.isDisposed)
          NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
        }
        assertEmpty(installedTabWidths)
        assertEmpty(notifications)
        assertEmpty(problems)
      }
      finally {
        PlatformTestUtil.forceCloseProjectWithoutSaving(otherProject)
      }
    }
    finally {
      NioFiles.deleteRecursively(root)
    }
  }

  /**
   * Performs the import inside a write action.
   * The non-blocking read action cannot start the lookup while the write action holds the lock.
   * [beforeLookup] runs in the same write action, so it always comes before the lookup.
   */
  private fun performImportInsideWriteAction(event: AnActionEvent, beforeLookup: () -> Unit) {
    WriteAction.run<IOException> {
      assertTrue(PrettierJSTestUtil.performAction(IMPORT_ACTION_ID, event).isPerformed)
      beforeLookup()
    }
  }

  private fun withTempCodeStyleSettings(project: Project, test: (CodeStyleSettings) -> Unit) {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings -> test(settings) }
  }

  private fun addConfig(path: String, tabWidth: Int): VirtualFile {
    return myFixture.addFileToProject(path, """{"tabWidth": $tabWidth}""").virtualFile
  }

  private fun updateImport(project: Project, config: VirtualFile): AnActionEvent {
    val event = PrettierJSTestUtil.updateAction(IMPORT_ACTION_ID, PrettierJSTestUtil.fileDataContext(project, config))
    assertTrue("The import is not available for $config", event.presentation.isEnabledAndVisible)
    return event
  }

  private fun countLookups(file: VirtualFile): LookupCounter {
    val counter = LookupCounter(file)
    project.replaceService(ProjectFileIndex::class.java, CountingProjectFileIndex(ProjectFileIndex.getInstance(project), counter),
                           testRootDisposable)
    return counter
  }

  private fun indentSize(settings: CodeStyleSettings): Int {
    return settings.getCommonSettings(JavascriptLanguage).indentOptions!!.INDENT_SIZE
  }

  private fun recordNotifications(project: Project) {
    project.messageBus.connect(testRootDisposable).subscribe(Notifications.TOPIC, object : Notifications {
      override fun notify(notification: Notification) {
        if (notification.groupId == JSLinterUtil.NOTIFICATION_GROUP.displayId) {
          notifications.add(notification.content)
        }
      }
    })
  }

  private fun recordLoggedProblems(block: () -> Unit): List<String> {
    val problems = CopyOnWriteArrayList<String>()
    val token = LoggedErrorProcessor.executeWith(object : LoggedErrorProcessor() {
      override fun processError(category: String, message: String, details: Array<String>, t: Throwable?): Set<Action> {
        problems.add("ERROR $category: $message: $t")
        return Action.ALL
      }

      override fun processWarn(category: String, message: String, t: Throwable?): Boolean {
        if (t != null) {
          problems.add("WARN $category: $message: $t")
        }
        return true
      }
    })
    try {
      block()
    }
    finally {
      token.finish()
    }
    return problems
  }

  private inner class InstallRecorder : PrettierCodeStyleInstaller {
    override fun install(project: Project, config: PrettierConfig, settings: CodeStyleSettings) {
      installedTabWidths.add(config.tabWidth)
    }

    // The importer requires all installers to report an installed config, so this answer does not change the result.
    override fun isInstalled(project: Project, config: PrettierConfig, settings: CodeStyleSettings): Boolean = true
  }

  /**
   * Counts the content checks of one file on background threads after [start].
   * The lookup of the import action makes this check before any other access to the project model.
   */
  private class LookupCounter(private val file: VirtualFile) {
    private val lookups = AtomicInteger()

    @Volatile
    private var started = false

    val count: Int
      get() = lookups.get()

    fun start() {
      started = true
    }

    fun onContentCheck(checkedFile: VirtualFile) {
      if (started && checkedFile == file && !EDT.isCurrentThreadEdt()) {
        lookups.incrementAndGet()
      }
    }
  }

  private class CountingProjectFileIndex(
    private val delegate: ProjectFileIndex,
    private val counter: LookupCounter,
  ) : ProjectFileIndex by delegate {
    override fun isInContent(fileOrDir: VirtualFile): Boolean {
      counter.onContentCheck(fileOrDir)
      return delegate.isInContent(fileOrDir)
    }
  }
}
