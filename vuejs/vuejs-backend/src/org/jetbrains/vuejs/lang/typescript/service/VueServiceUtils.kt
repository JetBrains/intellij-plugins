// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.lsp.JSBundledServiceNodePackage
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.vuejs.context.isVue2
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory

internal const val vuePluginPath = "vuejs/vuejs-backend"

const val vueTSPluginPackageName: String = "@vue/typescript-plugin"

const val vueLspPackageName: String = "@vue/language-server"

internal fun isVueServiceContext(project: Project, context: VirtualFile): Boolean {
  return context.fileType is VueFileType || isVueContext(context, project)
}

/**
 * Determines the appropriate Vue language service version to use based on the project settings
 * and the context of the given file.
 * It should be used as a source of truth about which service is actually started in the IDE.
 */
internal fun getAppropriateVueLSVersion(
  project: Project,
  context: VirtualFile,
): VueLanguageToolsVersion {
  return if (isVue2(project, context) || VueServiceTestMixin.forceLegacyPluginUsage) {
    // supports Vue
    VueLanguageToolsVersion.LEGACY
  }
  else {
    // supports only Vue 3+
    VueLanguageToolsVersion.DEFAULT
  }
}

@Service(Service.Level.PROJECT)
internal class VueLSCoroutineScope(val cs: CoroutineScope) {
  companion object {
    fun instance(project: Project): VueLSCoroutineScope = project.service()
  }
}

internal fun getVueLspHybridModePackages(project: Project): List<JSBundledServiceNodePackage> {
  return VueLanguageToolsVersion.entries.map { version ->
    val runtime = VueServiceRuntime.Bundled(version)
    val path = VueLspServerHybridModeLoaderFactory.getLoader(runtime).getAbsolutePath(project)
    JSBundledServiceNodePackage(
      packageName = vueLspPackageName,
      packageVersion = SemVer.parseFromText(version.versionString),
      path = path,
    )
  }
}

internal fun getVueTSPluginNodePackages(project: Project): List<JSBundledServiceNodePackage> {
  return VueLanguageToolsVersion.entries.map { version ->
    val runtime = VueServiceRuntime.Bundled(version)
    val path = VueTSPluginLoaderFactory.getLoader(runtime).getAbsolutePath(project)
    JSBundledServiceNodePackage(
      packageName = vueTSPluginPackageName,
      packageVersion = SemVer.parseFromText(version.versionString),
      path = path,
    )
  }
}