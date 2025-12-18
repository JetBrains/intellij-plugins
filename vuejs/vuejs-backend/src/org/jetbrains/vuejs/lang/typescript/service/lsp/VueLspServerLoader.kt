// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.vuePluginPath
import org.jetbrains.vuejs.options.getVueSettings

private val vueLspServerPackageVersion = PackageVersion.bundled<VueLspServerPackageDescriptor>(
  version = "2.2.10",
  pluginPath = vuePluginPath,
  localPath = "vue-language-tools/language-server/2.2.10",
  isBundledEnabled = { Registry.`is`("vue.language.server.bundled.enabled") },
)

private object VueLspServerPackageDescriptor : LspServerPackageDescriptor(
  name = "@vue/language-server",
  defaultVersion = vueLspServerPackageVersion,
  defaultPackageRelativePath = "/bin/vue-language-server.js",
) {
  override val registryVersion: String get() = Registry.stringValue("vue.language.server.default.version")
}

@ApiStatus.Experimental
object VueLspServerLoader : LspServerLoader(VueLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }
}