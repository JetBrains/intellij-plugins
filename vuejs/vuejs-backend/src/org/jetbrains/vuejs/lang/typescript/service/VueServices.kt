// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerState
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.VueTSPluginVersion
import org.jetbrains.vuejs.options.getVueSettings
import java.util.concurrent.ConcurrentHashMap

private const val vuePluginPath = "vuejs/vuejs-backend"

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

private fun isVueServiceContext(project: Project, context: VirtualFile): Boolean {
  return context.fileType is VueFileType || isVueContext(context, project)
}

object VueLspServerActivationRule : LspServerActivationRule(VueLspServerLoader, VueLspActivationHelper) {
  override fun isFileAcceptable(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return file.isVueFile || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
  }
}

private object VueLspActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    if (getVueSettings(project).tsPluginPreviewEnabled)
      return false

    return when (getVueSettings(project).serviceType) {
      VueServiceSettings.AUTO, VueServiceSettings.VOLAR -> true
      VueServiceSettings.TS_SERVICE -> false
      VueServiceSettings.DISABLED -> false
    }
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

@ApiStatus.Experimental
class VueTSPluginLoader(descriptor: LspServerPackageDescriptor) :
  TSPluginLoader(descriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).tsPluginPackageRef
  }
}

class VueTSPluginActivationRule(loader: VueTSPluginLoader) :
  TSPluginActivationRule(loader, VueTSPluginActivationHelper) {
  override fun isEnabled(project: Project, context: VirtualFile): Boolean {
    if (!getVueSettings(project).tsPluginPreviewEnabled)
      return false

    return super.isEnabled(project, context)
  }
}

private object VueTSPluginActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getVueSettings(project).tsPluginPreviewEnabled
  }
}

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

//<editor-fold desc="VueClassicTypeScriptService">

/**
 * Refers to the classic service that predates the official Vue LSP.
 */
fun isVueClassicTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (!isVueServiceContext(project, context) || getVueSettings(project).tsPluginPreviewEnabled)
    return false

  return when (getVueSettings(project).serviceType) {
    VueServiceSettings.AUTO, VueServiceSettings.VOLAR -> false
    VueServiceSettings.TS_SERVICE -> isTypeScriptServiceBefore5Context(project) // with TS 5+ project, nothing will be enabled
    VueServiceSettings.DISABLED -> false
  }
}

private fun isTypeScriptServiceBefore5Context(project: Project): Boolean {
  val path = getTypeScriptServiceDirectory(project)

  val packageJson = TypeScriptServerState.getPackageJsonFromServicePath(path)
  if (packageJson == null) return false // Nuxt doesn't have correct TS detection. Let's assume TS 5+
  val version = PackageJsonData.getOrCreate(packageJson).version ?: return true
  return version.major < 5
}

//</editor-fold>