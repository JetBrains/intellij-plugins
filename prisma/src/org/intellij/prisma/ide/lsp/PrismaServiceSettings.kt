// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.lsp

import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "PrismaServiceSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class PrismaServiceSettings(val project: Project) : SimplePersistentStateComponent<PrismaServiceState>(PrismaServiceState()) {
  var serviceMode
    get() = state.serviceMode
    set(value) {
      val changed = state.serviceMode != value
      state.serviceMode = value
      if (changed) restartPrismaServerAsync(project)
    }

  var lspServerPackageRef
    get() = createPackageRef(state.lspServerPackageName, PrismaLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.lspServerPackageName != refText
      state.lspServerPackageName = refText
      if (changed) restartPrismaServerAsync(project)
    }

  companion object {
    fun getInstance(project: Project): PrismaServiceSettings = project.service()
  }
}

class PrismaServiceState : BaseState() {
  var serviceMode by enum(PrismaServiceMode.ENABLED)
  var lspServerPackageName by string(defaultPackageKey)
}

enum class PrismaServiceMode {
  ENABLED,
  DISABLED
}