// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.TypeScriptLspClientCommandExecutor
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.impl.LspClientImpl
import kotlinx.coroutines.CoroutineScope

internal class VueLspClientTakeoverModeDescriptor(project: Project) : JSFrameworkLspClientDescriptor(
  project = project,
  activationRule = VueLspServerTakeoverModeActivationRule,
  presentableName = "Vue",
) {

  override fun createCommandExecutor(client: LspClientImpl, commandCoroutineScope: CoroutineScope): TypeScriptLspClientCommandExecutor =
    VueLspTakeoverModeLspClientCommandExecutor(client, commandCoroutineScope)

  override fun createInitializationOptionsWithTS(targetPath: String): Any {
    @Suppress("unused")
    return object {
      val typescript = object {
        val tsdk = targetPath
      }
      val vue = object {
        val hybridMode = false
      }
    }
  }
}