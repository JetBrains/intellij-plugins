// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.JSExternalDefinitionsPackage
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.lang.typescript.lsp.TSPluginLoader
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.vuePluginPath
import java.util.concurrent.ConcurrentHashMap

@ApiStatus.Experimental
object VueTSPluginBundledLoaderFactory {
  private val loaders = ConcurrentHashMap<String, VueTSPluginBundledLoader>()

  fun getLoader(versionString: String): TSPluginLoader {
    return loaders.getOrPut(versionString) {
      createLoader(versionString)
    }
  }

  fun getLoader(version: VueTSPluginVersion): TSPluginLoader {
    return getLoader(version.versionString)
  }

  private fun createLoader(versionString: String): VueTSPluginBundledLoader {
    val packageVersion = PackageVersion.bundled<VueTSPluginPackageDescriptor>(
      version = versionString,
      pluginPath = vuePluginPath,
      localPath = "vue-language-tools/typescript-plugin/$versionString",
      isBundledEnabled = { Registry.`is`("vue.ts.plugin.bundled.enabled") },
    )

    val descriptor = VueTSPluginPackageDescriptor(packageVersion)
    return VueTSPluginBundledLoader(descriptor)
  }
}

@ApiStatus.Experimental
private class VueTSPluginBundledLoader(
  private val descriptor: LspServerPackageDescriptor,
) : TSPluginLoader(descriptor) {
  override fun getSelectedPackage(project: Project): NodePackage {
    return JSExternalDefinitionsPackage(descriptor.serverPackage)
  }
}