// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.lang.typescript.lsp.TSPluginLoader
import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.lang.typescript.service.VueTSPluginVersion
import org.jetbrains.vuejs.lang.typescript.service.vuePluginPath
import org.jetbrains.vuejs.options.VueSettings

internal object VueTSPluginManualLoader : TSPluginLoader(
  packageDescriptor = VueTSPluginPackageDescriptor(vueTSPluginManualPackageVersion),
) {
  override fun getSelectedPackage(project: Project): NodePackage? {
    val settings = VueSettings.instance(project)
    return settings.manualSettings.tsPluginPackageRef.constantPackage
  }
}

private val vueTSPluginManualPackageVersion = PackageVersion.bundled<VueTSPluginPackageDescriptor>(
  version = VueTSPluginVersion.DEFAULT.versionString,
  pluginPath = vuePluginPath,
  localPath = "vue-language-tools/typescript-plugin/${VueTSPluginVersion.DEFAULT.versionString}",
  isBundledEnabled = { true },
)