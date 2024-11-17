// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service.settings

import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jetbrains.astro.service.AstroLspServerLoader

fun getAstroServiceSettings(project: Project) = project.service<AstroServiceSettings>()

@Service(Service.Level.PROJECT)
@State(name = "AstroServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AstroServiceSettings(val project: Project) : SimplePersistentStateComponent<AstroServiceState>(AstroServiceState()) {
  var serviceMode
    get() = state.innerServiceMode
    set(value) {
      val changed = state.innerServiceMode != value
      state.innerServiceMode = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var lspServerPackageRef
    get() = createPackageRef(state.lspServerPackageName, AstroLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.lspServerPackageName != refText
      state.lspServerPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }
}

class AstroServiceState : BaseState() {
  var innerServiceMode by enum(AstroServiceMode.ENABLED)
  var lspServerPackageName by string(defaultPackageKey)
}

enum class AstroServiceMode {
  ENABLED,
  DISABLED
}