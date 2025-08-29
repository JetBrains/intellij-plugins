// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.service.settings.AstroServiceConfigurable

// Pluggable TypeScript service for Astro TS plugin (@astrojs/ts-plugin)
private val astroTsPlugin = DownloadableTypeScriptServicePlugin(
  shortLabel = "Astro",
  activationRule = AstroTSPluginActivationRule
)

internal class AstroPluginTypeScriptService(project: Project) : PluggableTypeScriptService(project, astroTsPlugin) {
  override fun getProcessName(): String = "Astro + TypeScript"

  override fun getInitialOpenCommands(): List<JSLanguageServiceSimpleCommand> {
    return listOf(createConfigureCommand()) + super.getInitialOpenCommands()
  }

  private fun createConfigureCommand(): JSLanguageServiceSimpleCommand {
    // Configure the Astro TypeScript plugin for the TS server
    val arguments = ConfigurePluginRequestArguments(
      pluginName = "@astrojs/ts-plugin",
      configuration = mapOf(
        // minimal enabling configuration; additional options can be added if needed by the plugin
        "enabled" to true
      )
    )
    return ConfigurePluginRequest(arguments)
  }

  override fun isServiceFallbackResolveEnabled(): Boolean = true

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(
      service = this,
      currentFile = currentFile,
      itemIcon = AstroIcons.Astro,
      statusBarIcon = AstroIcons.Astro,
      settingsPageClass = AstroServiceConfigurable::class.java,
    )

  override fun isTypeEvaluationEnabled(): Boolean = false

  override fun isFindUsagesEnabled(): Boolean = Registry.`is`("astro.ts.find.usages.enabled")
}
