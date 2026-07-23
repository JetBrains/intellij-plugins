// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.TypeScriptCompilerVersionType
import com.intellij.openapi.project.Project

internal fun isVueServiceCompatibleTypeScriptEnabled(
  project: Project,
): Boolean {
  val tsCompilerSettings = TypeScriptCompilerSettings.getSettings(project)
  return when (tsCompilerSettings.versionType) {
    TypeScriptCompilerVersionType.EMBEDDED_TS_GO,
    TypeScriptCompilerVersionType.TS_GO_PROXY_RECOMMENDED_VERSION,
      -> false

    else -> true
  }
}
