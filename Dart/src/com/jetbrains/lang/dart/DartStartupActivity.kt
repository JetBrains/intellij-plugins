// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService
import com.jetbrains.lang.dart.projectWizard.DartModuleBuilder
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil
import com.jetbrains.lang.dart.util.PubspecYamlUtil
import kotlinx.coroutines.launch

/**
 * [DartStartupActivity] configures "Dart Packages" library (based on Dart-specific pubspec.yaml and .packages files) on a project open.
 * Afterward the "Dart Packages" library is kept up-to-dated thanks to [DartFileListener] and [DartWorkspaceModelChangeListener].
 *
 * @see DartFileListener
 * @see DartWorkspaceModelChangeListener
 */
class DartStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val serviceScope = DartAnalysisServerService.getInstance(project).serviceScope

    serviceScope.launch {
      val writeActions: List<() -> Unit> = readAction {
        val pubspecYamlFiles = FilenameIndex.getVirtualFilesByName(PubspecYamlUtil.PUBSPEC_YAML, GlobalSearchScope.projectScope(project))
        pubspecYamlFiles.mapNotNull {
          val module = ModuleUtilCore.findModuleForFile(it, project) ?: return@mapNotNull null
          prepareExcludeBuildAndToolCacheFolders(module, it)
        }
      }

      if (writeActions.isEmpty()) return@launch

      writeAction {
        writeActions.forEach { it() }
      }

      DartFileListener.scheduleDartPackageRootsUpdate(project)
    }

    serviceScope.launch {
      startAnalysisServerIfNeeded(project)
    }
  }

  private suspend fun startAnalysisServerIfNeeded(project: Project) {
    if (DartModuleBuilder.isPubGetScheduledForNewlyCreatedProject(project)) {
      // We want to start Analysis Server after the initial 'pub get' is finished, this will be done in DartPubActionBase
      return
    }

    if (DartSdk.getDartSdk(project) == null) return
    if (ModuleManager.getInstance(project).modules.find { DartSdkLibUtil.isDartSdkEnabled(it) } == null) return

    readAction {
      DartAnalysisServerService.getInstance(project).serverReadyForRequest()
    }
  }
}

fun excludeBuildAndToolCacheFolders(module: Module, pubspecYamlFile: VirtualFile) {
  prepareExcludeBuildAndToolCacheFolders(module, pubspecYamlFile)?.invoke()
}

private fun prepareExcludeBuildAndToolCacheFolders(module: Module, pubspecYamlFile: VirtualFile): (() -> Unit)? {
  val root = pubspecYamlFile.parent ?: return null
  val contentRoot = ProjectFileIndex.getInstance(module.project).getContentRootForFile(root) ?: return null
  val rootUrl = root.url

  val urlsToExclude = mutableSetOf("$rootUrl/.dart_tool", "$rootUrl/.pub", "$rootUrl/build")
    .also { it.removeAll(ModuleRootManager.getInstance(module).excludeRootUrls.toSet()) }
  if (urlsToExclude.isEmpty()) return null

  return {
    ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, emptyList(), urlsToExclude)
  }
}
