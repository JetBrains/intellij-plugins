// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service.settings

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import org.jetbrains.astro.service.AstroLspServerLoader
import org.jetbrains.astro.service.AstroLspServerSupportProvider
import org.jetbrains.astro.service.AstroTSPluginLoader

fun getAstroServiceSettings(project: Project): AstroServiceSettings = project.service<AstroServiceSettings>()

@Service(Service.Level.PROJECT)
@State(name = "AstroServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AstroServiceSettings(val project: Project) : SimplePersistentStateComponent<AstroServiceState>(AstroServiceState()) {
  companion object {
    @JvmField
    val DEFAULT_WORKSPACE_CONFIGURATION: String = """
      {
        "emmet": {
          "showExpandedAbbreviation": "always",
          "showAbbreviationSuggestions": true,
          "optimizeStylesheetParsing": true
        }
      }
    """.trimIndent()

    @JvmStatic
    fun getParsedWorkspaceConfigurationGson(project: Project): JsonObject {
      try {
        (JsonParser.parseString(getAstroServiceSettings(project).getWorkspaceConfiguration()) as? JsonObject)?.let { return it }
      }
      catch (e: com.google.gson.JsonParseException) {
        logger<AstroServiceSettings>()
          .warn("Manually specified Astro workspace configuration has invalid JSON syntax. Falling back to the default configuration.\n$e")
      }
      return JsonParser.parseString(DEFAULT_WORKSPACE_CONFIGURATION).asJsonObject
    }
  }

  var serviceMode: AstroServiceMode
    get() = state.innerServiceMode
    set(value) {
      val changed = state.innerServiceMode != value
      state.innerServiceMode = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var lspServerPackageRef: NodePackageRef
    get() = createPackageRef(state.lspServerPackageName, AstroLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.lspServerPackageName != refText
      state.lspServerPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var tsPluginPackageRef: NodePackageRef
    get() = createPackageRef(state.tsPluginPackageName, AstroTSPluginLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.tsPluginPackageName != refText
      state.tsPluginPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  fun getWorkspaceConfiguration(): String = state.workspaceConfiguration ?: DEFAULT_WORKSPACE_CONFIGURATION

  fun setWorkspaceConfiguration(configuration: String) {
    val newValue = configuration
    val changed = state.workspaceConfiguration != newValue
    state.workspaceConfiguration = newValue
    if (changed) {
      restartTypeScriptServicesAsync(project)
      LspServerManager.getInstance(project).stopAndRestartIfNeeded(AstroLspServerSupportProvider::class.java)
    }
  }
}

class AstroServiceState : BaseState() {
  var innerServiceMode: AstroServiceMode by enum(AstroServiceMode.ENABLED)
  var lspServerPackageName: String? by string(defaultPackageKey)
  var tsPluginPackageName: String? by string(defaultPackageKey)
  var workspaceConfiguration: String? by string(AstroServiceSettings.DEFAULT_WORKSPACE_CONFIGURATION)
}

enum class AstroServiceMode {
  ENABLED,
  DISABLED
}