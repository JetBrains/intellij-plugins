// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager.Location
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
internal class PrettierLanguageServiceManager(project: Project, internal val cs: CoroutineScope) :
  MultiRootJSLinterLanguageServiceManager<PrettierLanguageServiceImpl>(project, PrettierUtil.PACKAGE_NAME) {

  val inactivityTimeoutMs: Int
    get() = Registry.intValue("prettier.service.expiration.timeout.ms", TimeUnit.MINUTES.toMillis(5).toInt())

  init {
    VirtualFileManager.getInstance().addAsyncFileListener(PrettierConfigChangeListener(this), this)
  }

  /**
   * Terminates the cached services after a change of the Prettier configuration.
   * Then notifies the code style listeners if the code style modifier is enabled and [fromSave] is `true`.
   *
   * @param fromSave `true` if the batch of VFS events contains the save of a document.
   */
  @RequiresEdt
  @RequiresWriteLock
  internal fun handleConfigChange(fromSave: Boolean) {
    if (myProject.isDisposed) return
    // Prettier configurations loaded via `import` cannot be invalidated dynamically.
    // This limitation arises because Prettier caches configurations internally,
    // and operations like `useCache` or `clearConfigCache` do not fully reload the configurations.
    // To apply changes in configuration files such as
    // `prettier.config.mjs`, `prettier.config.cjs` and etc, the service must be terminated and restarted.
    // For more context, see related issues:
    // - https://github.com/prettier/prettier-vscode/issues/3179
    // - https://youtrack.jetbrains.com/issue/WEB-70641
    terminateServices()
    if (PrettierConfiguration.getInstance(myProject).codeStyleSettingsModifierEnabled && fromSave) {
      CodeStyleSettingsManager.getInstance(myProject).notifyCodeStyleSettingsChanged()
    }
  }

  override fun createServiceInstance(
    resolvedPackage: NodePackage,
    workingDirectory: VirtualFile,
  ): PrettierLanguageServiceImpl {
    return PrettierLanguageServiceImpl(myProject, workingDirectory)
  }

  /**
   * Returns the [Location] of the cached service that would format [file], or `null` if no cached service serves it.
   * Reuses the same service-location resolution as formatting, but never creates a service (no Node.js process is
   * started): each cached service is probed with its own package via a constant ref, so resolution short-circuits.
   * Must be called under a read action.
   */
  fun findCachedServiceLocationFor(file: VirtualFile): Location? {
    val contextDirectory = PrettierLanguageService.computeContextDirectory(myProject, file)
    return jsLinterServices.keys.firstOrNull { cached ->
      val resolved = resolveServiceLocation(contextDirectory, NodePackageRef.create(cached.nodePackage))
      resolved?.workingDirectory == cached.workingDirectory
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PrettierLanguageServiceManager = project.service()
  }
}

/**
 * Calls [PrettierLanguageServiceManager.handleConfigChange] when a batch of VFS events changes a Prettier configuration file,
 * `package.json`, or `.editorconfig`.
 *
 * [prepareChange] runs in a read action on a background thread, before the VFS applies the events.
 * It gets the name of a created or renamed file from the event data.
 * A created file does not exist yet, and a renamed file still has its old name.
 * The change applier runs on EDT in the write action, after the VFS applies the events.
 */
internal class PrettierConfigChangeListener(private val manager: PrettierLanguageServiceManager) : AsyncFileListener {
  override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
    val needReload = events.any { event ->
      ProgressManager.checkCanceled()
      isConfigChange(event)
    }
    if (!needReload) return null

    val fromSave = events.any { it.isFromSave }
    return object : AsyncFileListener.ChangeApplier {
      override fun afterVfsChange() {
        manager.handleConfigChange(fromSave)
      }
    }
  }
}

private fun isConfigChange(event: VFileEvent): Boolean {
  return when (event) {
    is VFileCreateEvent -> affectsConfig(event.childName, event.isDirectory)
    is VFilePropertyChangeEvent -> {
      if (event.isRename) {
        listOf(event.oldValue, event.newValue).any { it is String && affectsConfig(it, event.file.isDirectory) }
      }
      else {
        affectsConfig(event.file)
      }
    }
    is VFileContentChangeEvent -> affectsConfig(event.file)
    is VFileDeleteEvent -> affectsConfig(event.file)
    else -> false
  }
}

private fun affectsConfig(file: VirtualFile): Boolean = affectsConfig(file.name, file.isDirectory)

private fun affectsConfig(fileName: String, isDirectory: Boolean): Boolean {
  return PrettierUtil.isConfigFileName(fileName) ||
         fileName == PrettierUtil.EDITOR_CONFIG_FILE_NAME ||
         (!isDirectory && PackageJsonCommonUtil.isPackageJsonFileName(fileName))
}
