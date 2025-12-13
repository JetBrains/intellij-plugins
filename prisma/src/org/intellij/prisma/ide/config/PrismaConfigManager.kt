// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.config

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.javascript.nodejs.settings.NodeSettingsConfigurable
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorNotifications
import com.intellij.util.asDisposable
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.config.PrismaConfig.Companion.isPrismaConfig
import org.intellij.prisma.lang.PrismaFileType
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.seconds

private const val PRISMA_NOTIFICATION_GROUP_ID = "Prisma"

@OptIn(FlowPreview::class)
@Service(Service.Level.PROJECT)
class PrismaConfigManager(private val project: Project, private val coroutineScope: CoroutineScope) {
  companion object {
    private val LOG = logger<PrismaConfigManager>()

    fun getInstance(project: Project): PrismaConfigManager = project.service()

    suspend fun getInstanceAsync(project: Project): PrismaConfigManager = project.serviceAsync()
  }

  private val lock = ReentrantLock()
  private val dispatcher = Dispatchers.IO.limitedParallelism(1)
  private val interpreterNotificationShown = AtomicBoolean()

  private val updateOnChangeRequests = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  private val filesToUpdate = mutableSetOf<Path>() // lock

  /**
   * Directory to a contained config file mapping. [ConfigEntry.EMPTY_FILE] is used for directories without config files.
   */
  private val configFileMapping = CachedValuesManager.getManager(project).createCachedValue {
    CachedValueProvider.Result.create(mutableMapOf<VirtualFile, VirtualFile>(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
  } // lock

  /**
   * Config file -> its content.
   */
  private val configEntries = mutableMapOf<VirtualFile, ConfigEntry>() // lock

  /**
   * Config file -> task mapping.
   */
  private val tasks = mutableMapOf<VirtualFile, ReloadTask>() // lock

  init {
    if (Registry.`is`("prisma.config.loading")) {
      setupListeners()
    }
  }

  private fun setupListeners() {
    project.messageBus.connect(coroutineScope)
      .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
        override fun selectionChanged(event: FileEditorManagerEvent) {
          val file = event.newFile ?: return
          if (!(isPrismaConfig(file) || file.extension == PrismaFileType.defaultExtension)) return

          coroutineScope.launch {
            getConfigForFile(file)
          }
        }
      })

    VirtualFileManager.getInstance().addAsyncFileListenerBackgroundable(
      { events ->
        val filePaths = mutableSetOf<Path>()
        for (event in events) {
          if (event is VFileContentChangeEvent && isPrismaConfig(event.file)) {
            filePaths.add(event.file.toNioPath())
          }
        }
        if (filePaths.isEmpty()) return@addAsyncFileListenerBackgroundable null

        object : AsyncFileListener.ChangeApplier {
          override fun afterVfsChange() {
            lock.withLock {
              filesToUpdate.addAll(filePaths)
            }

            updateOnChangeRequests.tryEmit(Unit)
          }
        }
      }, coroutineScope.asDisposable())

    coroutineScope.launch {
      updateOnChangeRequests
        .debounce(3.seconds)
        .collect {
          val paths = lock.withLock {
            filesToUpdate.toList().also { filesToUpdate.clear() }
          }
          for (path in paths) {
            val file = readAction { VirtualFileManager.getInstance().findFileByNioPath(path)?.takeIf { it.isValid } } ?: continue
            getConfigForFile(file)
          }
        }
    }
  }

  suspend fun getConfigForFile(file: VirtualFile, await: Boolean = false): PrismaConfig? {
    if (!Registry.`is`("prisma.config.loading")) return null
    if (!TrustedProjects.isProjectTrusted(project)) {
      return null
    }

    val interpreter = getInterpreter(project)
    if (interpreter == null) {
      notifyMissingInterpreter(file)
      return null
    }

    LOG.debug("looking for config file for $file")
    val from = readAction {
      if (!file.isValid) return@readAction null
      if (file.isDirectory) file else file.parent
    }
    if (from == null) {
      LOG.warn("Cannot find parent for $file")
      return null
    }

    val configFile = JSProjectUtil.processDirectoriesUpToContentRootAndFindFirst(project, from) { dir ->
      findConfigInDirectory(dir)
    } ?: run {
      LOG.debug("Cannot find a valid config file for $file")
      return null
    }
    val timestamp = readAction { configFile.timeStamp }

    val cachedConfigEntry = lock.withLock { configEntries[configFile] }
    if (cachedConfigEntry != null) {
      LOG.debug { "Found cached config file for $file: $cachedConfigEntry" }
      if (cachedConfigEntry.timestamp != timestamp) {
        val task = scheduleConfigReload(configFile, timestamp)
        if (await) {
          return task.deferred.await()
        }
      }
      return cachedConfigEntry.config
    }

    val task = scheduleConfigReload(configFile, timestamp)
    return if (await) {
      task.deferred.await()
    }
    else {
      null
    }
  }

  fun getEvaluationError(file: VirtualFile): Throwable? {
    return lock.withLock { configEntries[file] }?.error
  }

  fun invalidate(file: VirtualFile) {
    lock.withLock {
      configEntries.remove(file)
      tasks.remove(file)?.deferred?.cancel()
    }
  }

  suspend fun invalidateAndReload(file: VirtualFile) {
    invalidate(file)
    getConfigForFile(file)
  }

  private fun notifyMissingInterpreter(file: VirtualFile) {
    if (interpreterNotificationShown.compareAndSet(false, true)) {
      LOG.warn("Cannot find Node interpreter to evaluate Prisma config for file: ${file}")

      val notification = Notification(
        PRISMA_NOTIFICATION_GROUP_ID,
        PrismaBundle.message("prisma.config.error.title"),
        PrismaBundle.message("prisma.config.evaluation.interpreter.not.found.error", file.name),
        NotificationType.WARNING
      ).addAction(NodeSettingsConfigurable.createConfigureInterpreterAction(project, null))
      Notifications.Bus.notify(notification)
    }
  }

  private fun scheduleConfigReload(configFile: VirtualFile, timestamp: Long): ReloadTask {
    LOG.info("Scheduling config file reload for $configFile")

    lock.withLock {
      val existingTask = tasks[configFile]
      if (existingTask != null) {
        if (existingTask.timestamp == timestamp) {
          LOG.debug { "Skipping reload of $configFile because it's already scheduled" }
          return existingTask
        }
        else {
          LOG.info("Canceling previous reload task for $configFile")
          existingTask.deferred.cancel()
        }
      }

      LOG.debug { "Starting a new reload task for $configFile" }
      val reloadTask = ReloadTask(
        configFile,
        timestamp,
        coroutineScope.async(dispatcher) {
          runConfigLoader(configFile, timestamp).also {
            backgroundWriteAction {
              PsiManager.getInstance(project).dropPsiCaches()
              ResolveCache.getInstance(project).clearCache(true)
              DaemonCodeAnalyzer.getInstance(project).restart("Config file updated: $configFile")
            }

            withContext(Dispatchers.EDT) {
              EditorNotifications.getInstance(project).updateAllNotifications()
            }
          }
        },
      )
      tasks[configFile] = reloadTask
      return reloadTask
    }
  }

  private suspend fun runConfigLoader(
    configFile: VirtualFile,
    timestamp: Long,
  ): PrismaConfig? {
    LOG.debug { "Running config loader for $configFile with timestamp $timestamp" }

    val loader = PrismaConfigLoader()
    if (!loader.accepts(configFile)) {
      LOG.error("Invalid config file for a loader: $configFile")
      return null
    }

    val config = try {
      loader.load(project, configFile)
    }
    catch (e: Exception) {
      logEvaluationError(configFile, e)
      lock.withLock {
        configEntries[configFile] = ConfigEntry(configFile, null, e, timestamp)
      }
      return null
    }

    lock.withLock {
      configEntries[configFile] = ConfigEntry(configFile, config, null, timestamp)
    }
    return config
  }

  private fun logEvaluationError(configFile: VirtualFile, e: Exception) {
    if (PrismaConfigErrorType.match(e) == PrismaConfigErrorType.MISSING_ENVIRONMENT) {
      LOG.warn("Failed to evaluate config file $configFile because of missing environment variables", e)
      return
    }

    LOG.warn("Failed to load config file $configFile", e)
  }

  @RequiresReadLock
  private fun findConfigInDirectory(dir: VirtualFile): VirtualFile? {
    val discoveredConfig = lock.withLock {
      configFileMapping.value[dir]
    }
    if (discoveredConfig != null) {
      return discoveredConfig.takeIf { isValidConfigFile(it) }
    }

    val config = findRootConfig(dir) ?: findNestedInConfigDirectory(dir) ?: return null

    return lock.withLock {
      configFileMapping.value.putIfAbsent(dir, config) ?: config
    }.takeIf { isValidConfigFile(it) }
  }

  private fun isValidConfigFile(file: VirtualFile): Boolean = file.isValid && file != ConfigEntry.EMPTY_FILE

  private fun findRootConfig(dir: VirtualFile): VirtualFile? = ROOT_CONFIG_NAMES.firstNotNullOfOrNull { dir.findChild(it) }

  private fun findNestedInConfigDirectory(dir: VirtualFile): VirtualFile? {
    val configDirectory = dir.findChild(CONFIG_DIR_NAME)?.takeIf { it.isValid && it.isDirectory } ?: return null
    return NESTED_CONFIG_NAMES.firstNotNullOfOrNull { configDirectory.findChild(it) }
  }
}

private class ReloadTask(val file: VirtualFile, val timestamp: Long, val deferred: Deferred<PrismaConfig?>)

private data class ConfigEntry(
  val file: VirtualFile,
  val config: PrismaConfig?,
  val error: Throwable?,
  val timestamp: Long,
) {
  companion object {
    val EMPTY_FILE = LightVirtualFile()
  }
}
