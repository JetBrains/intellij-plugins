// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.astro.context.isAstroProject
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings


private val astroLspServerPackageVersion = PackageVersion.bundled<AstroLspServerPackageDescriptor>(
  version = "2.15.5",
  pluginPath = "Astro",
  localPath = "astro-language-server",
  isBundledEnabled = {
    Registry.`is`("astro.language.server.bundled.enabled")
  }
)

private object AstroLspServerPackageDescriptor : LspServerPackageDescriptor(
  name = "@astrojs/language-server",
  defaultVersion = astroLspServerPackageVersion,
  defaultPackageRelativePath = "/bin/nodeServer.js"
) {
  override val registryVersion: String get() = Registry.stringValue("astro.language.server.default.version")
}

@ApiStatus.Experimental
object AstroLspServerLoader : LspServerLoader(AstroLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getAstroServiceSettings(project).lspServerPackageRef
  }
}

object AstroLspServerActivationRule : LspServerActivationRule(AstroLspServerLoader, AstroActivationHelper) {
  override fun isFileAcceptable(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false
    return file.fileType == AstroFileType
  }
}

private val astroTSPluginPackageVersion = PackageVersion.bundled<AstroTSPluginPackageDescriptor>(
  version = "1.10.4",
  pluginPath = "Astro",
  localPath = "typescript-astro-plugin",
  isBundledEnabled = { Registry.`is`("astro.language.server.bundled.enabled") },
)

private object AstroTSPluginPackageDescriptor : LspServerPackageDescriptor(
  name = "@astrojs/ts-plugin",
  defaultVersion = astroTSPluginPackageVersion,
  defaultPackageRelativePath = "",
) {
  override val registryVersion: String
    get() = Registry.stringValue("astro.ts.plugin.default.version")
}

@ApiStatus.Experimental
object AstroTSPluginLoader : TSPluginLoader(AstroTSPluginPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getAstroServiceSettings(project).tsPluginPackageRef
  }
}

object AstroTSPluginActivationRule : TSPluginActivationRule(AstroTSPluginLoader, AstroActivationHelper)

object AstroActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return context.fileType is AstroFileType || isAstroProject(context, project)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getAstroServiceSettings(project).serviceMode == AstroServiceMode.ENABLED
  }
}
