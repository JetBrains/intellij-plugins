// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class AstroLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, AstroLspServerSupportProvider::class.java) {
  override val name = "Astro LSP"
  override val prefix = "Astro"

  override fun isAcceptable(file: VirtualFile): Boolean = isServiceEnabledAndAvailable(project, file)

  override fun getServiceId(): String = "astro"
}