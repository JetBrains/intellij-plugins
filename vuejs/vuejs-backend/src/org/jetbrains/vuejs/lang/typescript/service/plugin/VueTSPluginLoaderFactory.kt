// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.vuePluginPath
import org.jetbrains.vuejs.options.VueTSPluginVersion
import java.util.concurrent.ConcurrentHashMap

@ApiStatus.Experimental
object VueTSPluginLoaderFactory {
  private val loaders = ConcurrentHashMap<String, VueTSPluginLoader>()

  fun getLoader(version: VueTSPluginVersion): VueTSPluginLoader {
    return loaders.getOrPut(version.versionString) {
      createLoader(version.versionString)
    }
  }

  fun getActivationRule(version: VueTSPluginVersion): VueTSPluginActivationRule {
    return VueTSPluginActivationRule(getLoader(version))
  }

  private fun createLoader(versionString: String): VueTSPluginLoader {
    val packageVersion = PackageVersion.bundled<VueTSPluginPackageDescriptor>(
      version = versionString,
      pluginPath = vuePluginPath,
      localPath = "vue-language-tools/typescript-plugin/$versionString",
      isBundledEnabled = { Registry.`is`("vue.ts.plugin.bundled.enabled") },
    )

    val descriptor = VueTSPluginPackageDescriptor(packageVersion)
    return VueTSPluginLoader(descriptor)
  }
}

private class VueTSPluginPackageDescriptor(version: PackageVersion) : LspServerPackageDescriptor(
  name = "@vue/typescript-plugin",
  defaultVersion = version,
  defaultPackageRelativePath = "",
) {
  override val registryVersion: String
    get() = Registry.stringValue("vue.ts.plugin.default.version")
}