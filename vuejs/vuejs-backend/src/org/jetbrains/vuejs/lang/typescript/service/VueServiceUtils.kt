// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.lsp.JSBundledServiceNodePackage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.context.isVue2
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginBundledLoaderFactory.getLoader
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginVersion

internal const val vuePluginPath = "vuejs/vuejs-backend"

internal const val vueTSPluginPackageName = "@vue/typescript-plugin"

internal fun isVueServiceContext(project: Project, context: VirtualFile): Boolean {
  return context.fileType is VueFileType || isVueContext(context, project)
}

/**
 * Determines the appropriate Vue language service version to use based on the project settings
 * and the context of the given file.
 * It should be used as a source of truth about which service is actually started in the IDE.
 */
internal fun decideVueLSBundledVersion(
  project: Project,
  context: VirtualFile,
): VueTSPluginVersion {
  return if (isVue2(project, context) || VueServiceTestMixin.forceLegacyPluginUsage) {
    // supports Vue
    VueTSPluginVersion.LEGACY
  }
  else {
    // supports only Vue 3+
    VueTSPluginVersion.DEFAULT
  }
}

internal fun getVueTSPluginNodePackages(project: Project): List<JSBundledServiceNodePackage> {
  return VueTSPluginVersion.entries.map {
    val path = getLoader(it.versionString).getAbsolutePath(project)
    JSBundledServiceNodePackage(
      packageName = vueTSPluginPackageName,
      packageVersion = SemVer.parseFromText(it.versionString),
      path = path,
    )
  }
}