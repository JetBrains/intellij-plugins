// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class PrettierLanguageServiceManager(project: Project, internal val cs: CoroutineScope) :
  MultiRootJSLinterLanguageServiceManager<PrettierLanguageServiceImpl>(project, PrettierUtil.PACKAGE_NAME) {

  val inactivityTimeoutMs: Int
    get() = Registry.intValue("prettier.service.expiration.timeout.ms", TimeUnit.MINUTES.toMillis(5).toInt())

  init {
    project.messageBus.connect(this).subscribe<BulkFileListener>(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
      override fun after(events: List<VFileEvent>) {
        val needReload = events.any { ev ->
          val file = ev.file ?: return@any false
          val name = file.name
          (ev is VFileContentChangeEvent || ev is VFileCreateEvent || ev is VFileDeleteEvent || ev is VFilePropertyChangeEvent) &&
          (PrettierUtil.isConfigFileOrPackageJson(file) || name == PrettierUtil.EDITOR_CONFIG_FILE_NAME)
        }

        if (needReload) {
          // Prettier configurations loaded via `import` cannot be invalidated dynamically.
          // This limitation arises because Prettier caches configurations internally,
          // and operations like `useCache` or `clearConfigCache` do not fully reload the configurations.
          // To apply changes in configuration files such as
          // `prettier.config.mjs`, `prettier.config.cjs` and etc, the service must be terminated and restarted.
          // For more context, see related issues:
          // - https://github.com/prettier/prettier-vscode/issues/3179
          // - https://youtrack.jetbrains.com/issue/WEB-70641
          terminateServices()
          if (PrettierConfiguration.getInstance(project).codeStyleSettingsModifierEnabled && events.any { it.isFromSave }) {
            CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()
          }
        }
      }
    })
  }

  override fun createServiceInstance(
    resolvedPackage: NodePackage,
    workingDirectory: VirtualFile,
  ): PrettierLanguageServiceImpl {
    return PrettierLanguageServiceImpl(myProject, workingDirectory)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PrettierLanguageServiceManager = project.service()
  }
}
