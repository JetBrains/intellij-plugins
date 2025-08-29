// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service.settings

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.astro.service.AstroLspServerLoader
import org.jetbrains.astro.service.AstroTSPluginLoader

fun getAstroServiceSettings(project: Project): AstroServiceSettings = project.service<AstroServiceSettings>()

@Service(Service.Level.PROJECT)
@State(name = "AstroServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AstroServiceSettings(val project: Project) : SimplePersistentStateComponent<AstroServiceState>(AstroServiceState()) {
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
}

class AstroServiceState : BaseState() {
  var innerServiceMode: AstroServiceMode by enum(AstroServiceMode.ENABLED)
  var lspServerPackageName: String? by string(defaultPackageKey)
  var tsPluginPackageName: String? by string(defaultPackageKey)
}

enum class AstroServiceMode {
  ENABLED,
  DISABLED
}