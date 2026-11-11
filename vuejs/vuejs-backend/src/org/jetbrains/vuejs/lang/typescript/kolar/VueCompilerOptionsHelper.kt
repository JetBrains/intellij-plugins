// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory

internal object VueCompilerOptionsHelper {
  fun getTypesRoot(
    project: Project,
  ): String {
    if (VueTranspilerTestMixin.useDefaultTypesRoot)
      return VueCompilerOptions.DEFAULT_TYPES_ROOT

    return VueTSPluginLoaderFactory
      .getLoader(VueServiceRuntime(VueLanguageToolsVersion.DEFAULT))
      .getAbsolutePath(project)!!
      .plus("/node_modules/@vue/typescript-plugin/types")
  }
}