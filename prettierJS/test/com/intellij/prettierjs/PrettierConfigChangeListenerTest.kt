// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.SavingRequestor
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.psi.codeStyle.CodeStyleSettingsListener
import com.intellij.testFramework.LeakHunter
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.createTestOpenProjectOptions
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * Checks how [PrettierConfigChangeListener] terminates the cached Prettier services.
 *
 * Each test caches one service with a fake package, so no test starts Node.js.
 * A test never asks for a service again before it checks the cached service.
 * A new request creates a new service, and that would hide a termination.
 */
class PrettierConfigChangeListenerTest : BasePlatformTestCase() {
  private lateinit var contextFile: VirtualFile

  override fun tearDown() {
    try {
      PrettierLanguageServiceManager.getInstance(project).terminateServices()
      // Keep PrettierConfiguration clean for the shared light project, matching PrettierConfigurationTestBase.
      val state = PrettierConfiguration.getInstance(project).state
      state.configurationMode = null
      state.codeStyleSettingsModifierEnabled = true
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testCreatingConfigFileTerminatesCachedService() {
    val service = cacheService()

    WriteAction.run<Throwable> { root().createChildData(this, ".prettierrc") }

    assertFalse(isCached(service))
  }

  fun testChangingConfigFileTerminatesCachedServiceBeforeWriteActionEnds() {
    val config = createFile(".prettierrc.json", "{}")
    val service = cacheService()

    WriteAction.run<Throwable> {
      VfsUtil.saveText(config, """{"semi": false}""")
      // Formatting cannot run before the write action ends, so it never gets the old service.
      assertFalse(isCached(service))
    }

    // The next formatting gets a new service, and the new service reads the changed configuration.
    assertNotSame(service, PrettierLanguageService.getInstance(project, contextFile, FAKE_PACKAGE))
  }

  fun testDeletingConfigFileTerminatesCachedService() {
    val config = createFile("prettier.config.mjs", "export default {}")
    val service = cacheService()

    WriteAction.run<Throwable> { config.delete(this) }

    assertFalse(isCached(service))
  }

  fun testRenamingFileToConfigNameTerminatesCachedService() {
    val file = createFile("config.json", "{}")
    val service = cacheService()

    WriteAction.run<Throwable> { file.rename(this, ".prettierrc.json") }

    assertFalse(isCached(service))
  }

  fun testRenamingConfigFileToOtherNameTerminatesCachedService() {
    val config = createFile(".prettierrc", "{}")
    val service = cacheService()

    WriteAction.run<Throwable> { config.rename(this, ".prettierrc.bak") }

    assertFalse(isCached(service))
  }

  fun testChangingPropertyOfConfigFileTerminatesCachedService() {
    val config = createFile(".prettierrc.yaml", "semi: false")
    val service = cacheService()

    WriteAction.run<Throwable> { config.isWritable = false }

    assertFalse(isCached(service))
  }

  fun testChangingPackageJsonTerminatesCachedService() {
    val packageJson = createFile("package.json", "{}")
    val service = cacheService()

    WriteAction.run<Throwable> { VfsUtil.saveText(packageJson, """{"prettier": {"semi": false}}""") }

    assertFalse(isCached(service))
  }

  fun testChangingEditorConfigTerminatesCachedService() {
    val editorConfig = createFile(".editorconfig", "root = true")
    val service = cacheService()

    WriteAction.run<Throwable> { VfsUtil.saveText(editorConfig, "root = true\n[*]\nindent_size = 4") }

    assertFalse(isCached(service))
  }

  fun testRefreshOfExternallyChangedConfigFileTerminatesCachedService() {
    val dir = createTempDirectory()
    VfsRootAccess.allowRootAccess(testRootDisposable, dir.toString())
    val configPath = Files.writeString(dir.resolve(".prettierrc.json"), "{}")
    val config = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(configPath)!!
    val service = cacheService()

    // The new content has a different length, so the refresh finds the change.
    Files.writeString(configPath, """{"semi": false}""")
    VfsUtil.markDirtyAndRefresh(false, false, false, config)

    assertFalse(isCached(service))
  }

  fun testUnrelatedChangesKeepCachedService() {
    val script = createFile("src/app.js", "let a = 1")
    val notes = createFile("notes.txt", "")
    val service = cacheService()

    WriteAction.run<Throwable> {
      val root = root()
      root.createChildData(this, "main.ts")
      VfsUtil.saveText(script, "let a = 2")
      script.rename(this, "app2.js")
      script.move(this, root)
      notes.copy(this, root, "notes2.txt")
      notes.delete(this)
    }

    assertTrue(isCached(service))
  }

  fun testSavingConfigFileNotifiesCodeStyleListenersIfModifierIsEnabled() {
    setCodeStyleModifierEnabled(true)
    val config = createFile(".prettierrc.json", "{}")
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    saveDocument(config, """{"semi": false}""")

    assertFalse(isCached(service))
    assertEquals(1, notifications.get())
  }

  fun testSavingConfigFileDoesNotNotifyCodeStyleListenersIfModifierIsDisabled() {
    setCodeStyleModifierEnabled(false)
    val config = createFile(".prettierrc.json", "{}")
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    saveDocument(config, """{"semi": false}""")

    assertFalse(isCached(service))
    assertEquals(0, notifications.get())
  }

  fun testChangingConfigFileWithoutSaveDoesNotNotifyCodeStyleListeners() {
    setCodeStyleModifierEnabled(true)
    val config = createFile(".prettierrc.json", "{}")
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    WriteAction.run<Throwable> { VfsUtil.saveText(config, """{"semi": false}""") }

    assertFalse(isCached(service))
    assertEquals(0, notifications.get())
  }

  fun testSavingUnrelatedFileKeepsCachedServiceAndDoesNotNotify() {
    setCodeStyleModifierEnabled(true)
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    saveDocument(contextFile, "let a = 1")

    assertTrue(isCached(service))
    assertEquals(0, notifications.get())
  }

  fun testSaveAnywhereInBatchNotifiesCodeStyleListenersIfModifierIsEnabled() {
    setCodeStyleModifierEnabled(true)
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    applyChange(prepareChange(saveOfUnrelatedFileAndCreationOfConfigFile())!!)

    assertFalse(isCached(service))
    assertEquals(1, notifications.get())
  }

  fun testSaveAnywhereInBatchDoesNotNotifyCodeStyleListenersIfModifierIsDisabled() {
    setCodeStyleModifierEnabled(false)
    val service = cacheService()
    val notifications = countCodeStyleNotifications()

    applyChange(prepareChange(saveOfUnrelatedFileAndCreationOfConfigFile())!!)

    assertFalse(isCached(service))
    assertEquals(0, notifications.get())
  }

  fun testEventsForConfigFileNamesRequestReload() {
    val dir = StubFile("dir", isDir = true)
    for (name in RELOAD_FILE_NAMES) {
      val file = StubFile(name)
      val events = mapOf(
        "create" to VFileCreateEvent(null, dir, name, false, null, null, null),
        "content change" to VFileContentChangeEvent(null, file, 0, -1),
        "delete" to VFileDeleteEvent(null, file),
        "rename to the name" to VFilePropertyChangeEvent(null, StubFile("draft.txt"), VirtualFile.PROP_NAME, "draft.txt", name),
        "rename from the name" to VFilePropertyChangeEvent(null, file, VirtualFile.PROP_NAME, name, "draft.txt"),
        "writable change" to VFilePropertyChangeEvent(null, file, VirtualFile.PROP_WRITABLE, true, false),
      )
      for ((kind, event) in events) {
        assertNotNull("$kind of $name must request a reload", prepareChange(listOf(event)))
      }
    }
  }

  fun testEventsForOtherFileNamesDoNotRequestReload() {
    val dir = StubFile("dir", isDir = true)
    for (name in OTHER_FILE_NAMES) {
      val file = StubFile(name)
      val events = mapOf(
        "create" to VFileCreateEvent(null, dir, name, false, null, null, null),
        "content change" to VFileContentChangeEvent(null, file, 0, -1),
        "delete" to VFileDeleteEvent(null, file),
        "rename" to VFilePropertyChangeEvent(null, file, VirtualFile.PROP_NAME, name, "draft.txt"),
        "writable change" to VFilePropertyChangeEvent(null, file, VirtualFile.PROP_WRITABLE, true, false),
      )
      for ((kind, event) in events) {
        assertNull("$kind of $name must not request a reload", prepareChange(listOf(event)))
      }
    }
    // A directory with the name package.json is not a package.json file.
    assertNull(prepareChange(listOf(VFileCreateEvent(null, dir, "package.json", true, null, null, null))))
  }

  fun testLargeBatchOfUnrelatedEventsAvoidsVfsLookups() {
    val service = cacheService()
    val dir = StubFile("src", isDir = true)
    val changed = StubFile("app.js")
    val deleted = StubFile("old.js")
    val renamed = StubFile("a.js")
    val readOnly = StubFile("lib.js")
    val moved = StubFile("moved.js")
    val copied = StubFile("copied.js")
    val events = buildList {
      repeat(BATCH_SIZE) {
        add(VFileCreateEvent(null, dir, "index.js", false, null, null, null))
        add(VFileContentChangeEvent(null, changed, 0, -1))
        add(VFileDeleteEvent(null, deleted))
        add(VFilePropertyChangeEvent(null, renamed, VirtualFile.PROP_NAME, "a.js", "b.js"))
        add(VFilePropertyChangeEvent(null, readOnly, VirtualFile.PROP_WRITABLE, true, false))
        add(VFileMoveEvent(null, moved, dir))
        add(VFileCopyEvent(null, copied, dir, "copy.js"))
      }
    }

    assertNull(prepareChange(events))

    // A create event holds the name of the new file, so the listener does not look for the file in the directory.
    assertEquals(0, dir.lookups + dir.nameReads)
    // A rename event holds both names. The listener does not examine move and copy events.
    assertEquals(0, renamed.nameReads + moved.nameReads + copied.nameReads)
    // The other events hold only the file, so the listener reads each file name one time.
    assertEquals(BATCH_SIZE, changed.nameReads)
    assertEquals(BATCH_SIZE, deleted.nameReads)
    assertEquals(BATCH_SIZE, readOnly.nameReads)
    assertEquals(0, listOf(changed, deleted, renamed, readOnly, moved, copied).sumOf { it.lookups })
    assertTrue(isCached(service))
  }

  fun testCancelledPreparationKeepsCachedService() {
    val service = cacheService()
    val indicator = EmptyProgressIndicator()
    // The first event cancels the preparation. The second event would request a reload.
    val events = listOf(
      VFileContentChangeEvent(null, StubFile("app.js", onNameRead = { indicator.cancel() }), 0, -1),
      VFileCreateEvent(null, StubFile("dir", isDir = true), ".prettierrc", false, null, null, null),
    )

    assertThrows(ProcessCanceledException::class.java) {
      ProgressManager.getInstance().runProcess(Runnable { prepareChange(events) }, indicator)
    }
    assertTrue(isCached(service))

    // The platform starts a cancelled preparation again. Only the change applier terminates the services.
    val applier = prepareChange(events)!!
    assertTrue(isCached(service))
    applyChange(applier)
    assertFalse(isCached(service))
  }

  fun testClosedProjectReleasesListenerAndIgnoresPreparedChange() {
    val otherProject = ProjectManagerEx.getInstanceEx().newProject(createTempDirectory(), createTestOpenProjectOptions())!!
    val (otherManager, applier) = try {
      val manager = PrettierLanguageServiceManager.getInstance(otherProject)
      manager to prepareChange(saveOfUnrelatedFileAndCreationOfConfigFile(), PrettierConfigChangeListener(manager))!!
    }
    finally {
      PlatformTestUtil.forceCloseProjectWithoutSaving(otherProject)
    }
    assertTrue(otherProject.isDisposed)

    // The change applier must not touch the disposed project.
    applyChange(applier)

    LeakHunter.checkLeak(VirtualFileManager.getInstance(), PrettierLanguageServiceManager::class.java) { it === otherManager }
  }

  /** Caches one service for the project root. Call it one time in a test, after the VFS changes that prepare the test. */
  private fun cacheService(): PrettierLanguageService {
    contextFile = createFile("index.js", "")
    PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    val service = PrettierLanguageService.getInstance(project, contextFile, FAKE_PACKAGE)
    assertTrue(isCached(service))
    return service
  }

  /** Reads the cached services. Unlike [PrettierLanguageService.getInstance], this never creates a service. */
  private fun isCached(service: PrettierLanguageService): Boolean {
    return PrettierLanguageServiceManager.getInstance(project).jsLinterServices.values.any { it.service === service }
  }

  private fun prepareChange(
    events: List<VFileEvent>,
    listener: AsyncFileListener = PrettierConfigChangeListener(PrettierLanguageServiceManager.getInstance(project)),
  ): AsyncFileListener.ChangeApplier? {
    return ReadAction.compute<AsyncFileListener.ChangeApplier?, Throwable> { listener.prepareChange(events) }
  }

  private fun applyChange(applier: AsyncFileListener.ChangeApplier) {
    WriteAction.run<Throwable> { applier.afterVfsChange() }
  }

  private fun saveOfUnrelatedFileAndCreationOfConfigFile(): List<VFileEvent> {
    val saveRequestor = object : SavingRequestor {}
    return listOf(
      VFileContentChangeEvent(saveRequestor, StubFile("app.js"), 0, -1),
      VFileCreateEvent(null, StubFile("dir", isDir = true), ".prettierrc", false, null, null, null),
    )
  }

  private fun root(): VirtualFile = myFixture.tempDirFixture.getFile(".")!!

  private fun createFile(path: String, text: String): VirtualFile = myFixture.tempDirFixture.createFile(path, text)

  private fun createTempDirectory(): Path {
    val dir = FileUtil.createTempDirectory("prettier-config-change", null, false)
    Disposer.register(testRootDisposable) { FileUtil.delete(dir) }
    return dir.toPath()
  }

  private fun saveDocument(file: VirtualFile, text: String) {
    val documentManager = FileDocumentManager.getInstance()
    val document = documentManager.getDocument(file)!!
    WriteCommandAction.runWriteCommandAction(project) { document.setText(text) }
    documentManager.saveDocument(document)
  }

  /**
   * Uses the automatic mode.
   * No `package.json` in these tests declares Prettier, so the code style modifier itself does not start a service.
   */
  private fun setCodeStyleModifierEnabled(enabled: Boolean) {
    val configuration = PrettierConfiguration.getInstance(project)
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC
    configuration.state.codeStyleSettingsModifierEnabled = enabled
    assertEquals(enabled, configuration.codeStyleSettingsModifierEnabled)
  }

  /** Counts the notifications for the whole project. A notification for one file has a non-null file. */
  private fun countCodeStyleNotifications(): AtomicInteger {
    val count = AtomicInteger()
    project.messageBus.connect(testRootDisposable).subscribe(CodeStyleSettingsListener.TOPIC, CodeStyleSettingsListener { event ->
      if (event.virtualFile == null) count.incrementAndGet()
    })
    return count
  }
}

private const val BATCH_SIZE = 10_000

private val FAKE_PACKAGE = NodePackage("/fake/node_modules/prettier")

/** The Prettier configuration file names. See https://prettier.io/docs/configuration. */
private val CONFIG_FILE_NAMES = listOf(
  ".prettierrc", ".prettierrc.json", ".prettierrc.yml", ".prettierrc.yaml", ".prettierrc.json5", ".prettierrc.toml",
  ".prettierrc.js", ".prettierrc.mjs", ".prettierrc.cjs", ".prettierrc.ts", ".prettierrc.mts", ".prettierrc.cts",
  "prettier.config.js", "prettier.config.mjs", "prettier.config.cjs", "prettier.config.ts", "prettier.config.mts", "prettier.config.cts",
)

private val RELOAD_FILE_NAMES = CONFIG_FILE_NAMES + listOf("package.json", ".editorconfig")

private val OTHER_FILE_NAMES = listOf("index.js", "tsconfig.json", "package-lock.json", ".prettierignore", "prettier.config.json")

/** A file outside the VFS. It counts the calls that need the VFS storage for a real file. */
private class StubFile(
  private val fileName: String,
  private val isDir: Boolean = false,
  private val onNameRead: () -> Unit = {},
) : VirtualFile() {
  var nameReads: Int = 0
    private set

  /** Counts [findChild], [getChildren], and [getPath]. */
  var lookups: Int = 0
    private set

  override fun getName(): String {
    nameReads++
    onNameRead()
    return fileName
  }

  override fun findChild(name: String): VirtualFile? {
    lookups++
    return null
  }

  override fun getChildren(): Array<VirtualFile> {
    lookups++
    return EMPTY_ARRAY
  }

  override fun getPath(): String {
    lookups++
    return "/stub/$fileName"
  }

  override fun isDirectory(): Boolean = isDir

  override fun isValid(): Boolean = true

  override fun isWritable(): Boolean = true

  override fun getParent(): VirtualFile? = null

  override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

  override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
    throw UnsupportedOperationException()
  }

  override fun contentsToByteArray(): ByteArray = throw UnsupportedOperationException()

  override fun getTimeStamp(): Long = 0

  override fun getLength(): Long = 0

  override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {
    throw UnsupportedOperationException()
  }

  override fun getInputStream(): InputStream = throw UnsupportedOperationException()
}
