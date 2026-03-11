// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.JSExternalDefinitionsPackage
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.lang.typescript.lsp.TSPluginLoader
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.vuePluginPath
import org.jetbrains.vuejs.options.VueSettings
import java.util.concurrent.ConcurrentHashMap

@ApiStatus.Experimental
object VueTSPluginLoaderFactory {
  private val loaders = ConcurrentHashMap<VueServiceRuntime, VueTSPluginLoader>()

  fun getLoader(versionString: String): TSPluginLoader {
    val version = VueLanguageToolsVersion.fromVersionOrInfer(versionString)
    return getLoader(VueServiceRuntime.Bundled(version))
  }

  fun getLoader(runtime: VueServiceRuntime): TSPluginLoader {
    return loaders.getOrPut(runtime) {
      createLoader(runtime)
    }
  }

  private fun createLoader(runtime: VueServiceRuntime): VueTSPluginLoader {
    val version = when (runtime) {
      is VueServiceRuntime.Bundled ->
        runtime.version.versionString

      is VueServiceRuntime.Manual ->
        VueLanguageToolsVersion.DEFAULT.versionString
    }

    val packageVersion = PackageVersion.bundled<VueTSPluginPackageDescriptor>(
      version = version,
      pluginPath = vuePluginPath,
      localPath = "vue-language-tools/typescript-plugin/$version",
      isBundledEnabled = { Registry.`is`("vue.ts.plugin.bundled.enabled") },
    )
    val descriptor = VueTSPluginPackageDescriptor(packageVersion)
    return VueTSPluginLoader(descriptor, runtime)
  }
}

@ApiStatus.Experimental
private class VueTSPluginLoader(
  private val descriptor: LspServerPackageDescriptor,
  private val runtime: VueServiceRuntime,
) : TSPluginLoader(descriptor) {
  override fun getSelectedPackage(project: Project): NodePackage {
    return when (runtime) {
      is VueServiceRuntime.Bundled ->
        JSExternalDefinitionsPackage(descriptor.serverPackage)

      is VueServiceRuntime.Manual ->
        VueSettings.instance(project).manualSettings.tsPluginPackage
    }
  }
}
