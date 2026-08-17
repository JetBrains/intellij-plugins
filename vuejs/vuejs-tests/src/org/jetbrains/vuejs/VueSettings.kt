// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.TypeScriptCompilerVersionType
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

internal fun configureVueSettings(
  project: Project,
  disposable: Disposable,
  testMode: VueTestMode,
) {
  when (testMode) {
    VueTestMode.TS_GO_PROXY -> {
      Registry.get("typescript.ts-go.enabled").setValue(true, disposable)

      val tsCompilerSettings = TypeScriptCompilerSettings.getSettings(project)
      val oldVersionType = tsCompilerSettings.versionType
      tsCompilerSettings.versionType = TypeScriptCompilerVersionType.TS_GO_PROXY_RECOMMENDED_VERSION

      Disposer.register(disposable) {
        tsCompilerSettings.versionType = oldVersionType
      }
    }

    VueTestMode.NO_PLUGIN -> {
      val vueSettings = VueSettings.instance(project)
      val oldServiceType = vueSettings.serviceType
      vueSettings.serviceType = VueLSMode.DISABLED

      Disposer.register(disposable) {
        vueSettings.serviceType = oldServiceType
      }
    }

    else -> {}
  }
}