// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.TypeScriptCompilerVersionType
import com.intellij.lang.typescript.lsp.TypeScriptGoChecker
import com.intellij.openapi.project.Project

/**
 * `true` if the classic `tsserver` serves the project, so the Vue services can run.
 * TypeScript-Go serves the Vue files itself, so the result is `false` for each source of TypeScript-Go:
 * the bundled package, a package in `node_modules`, or an explicit path.
 */
internal fun isVueServiceCompatibleTypeScriptEnabled(
  project: Project,
): Boolean {
  // The recommended TypeScript-Go package can still download, and until then no package info is available.
  if (TypeScriptCompilerSettings.getSettings(project).versionType == TypeScriptCompilerVersionType.TS_GO_PROXY_RECOMMENDED_VERSION)
    return false

  return !TypeScriptGoChecker(project).isUseTypeScriptGo()
}
