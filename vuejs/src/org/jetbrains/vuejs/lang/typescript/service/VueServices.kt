// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerState
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.VueSettings
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File

private const val vuePluginPath = "vuejs/vue-language-tools"

private val vueLspServerPackageVersion = PackageVersion.bundled<VueLspServerPackageDescriptor>(
  version = "2.2.10",
  pluginPath = "$vuePluginPath/language-server",
  localPath = "2.2.10",
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

  override fun getAbsolutePathForDefaultKey(project: Project): String? {
    if (project.service<VueSettings>().useTypesFromServer) {
      return getNewEvalPath()
    }

    return super.getAbsolutePathForDefaultKey(project)
  }

  private fun getNewEvalPath(): String {
    // work in progress
    val registryValue = Registry.stringValue("vue.language.server.default.version")
    val version =
      if (registryValue.startsWith("1")) "tsc-vue1" // explicit Registry value is needed for old Vue LS 1 New Eval
      else "tsc-vue"
    val file = File(TypeScriptUtil.getTypeScriptCompilerFolderFile(),
                    "typescript/node_modules/$version/${packageDescriptor.defaultPackageRelativePath}")
    val path = file.absolutePath
    return path
  }
}

internal val vueLspNewEvalVersion = SemVer.parseFromText("2.0.26-eval")

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

private val vueTSPluginPackageVersion = PackageVersion.bundled<VueTSPluginPackageDescriptor>(
  version = "3.0.1",
  pluginPath = "$vuePluginPath/typescript-plugin",
  localPath = "3.0.1",
  isBundledEnabled = { Registry.`is`("vue.ts.plugin.bundled.enabled") },
)

private object VueTSPluginPackageDescriptor : LspServerPackageDescriptor(
  name = "@vue/typescript-plugin",
  defaultVersion = vueTSPluginPackageVersion,
  defaultPackageRelativePath = "",
) {
  override val registryVersion: String
    get() = Registry.stringValue("vue.ts.plugin.default.version")
}

@ApiStatus.Experimental
object VueTSPluginLoader : TSPluginLoader(VueTSPluginPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).tsPluginPackageRef
  }
}

object VueTSPluginActivationRule : TSPluginActivationRule(VueTSPluginLoader, VueTSPluginActivationHelper) {
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

//<editor-fold desc="VueClassicTypeScriptService">

/**
 * Refers to the classic service that predates official Vue LSP.
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