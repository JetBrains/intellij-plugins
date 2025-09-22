package com.intellij.deno.settings

import com.intellij.deno.DenoBundle
import com.intellij.javascript.runtime.JSRuntimeProvider
import com.intellij.javascript.runtime.JSRuntimeType
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

internal class DenoRuntimeProvider : JSRuntimeProvider {
  override val runtimeType: JSRuntimeType
    get() = DenoRuntimeType

  override fun createSettingsConfigurable(project: Project): Configurable {
    return DenoSettingsConfigurable(project)
  }
}

internal object DenoRuntimeType : JSRuntimeType {
  override val id: @NonNls String
    get() = "deno"

  override val displayName: @Nls String
    get() = DenoBundle.message("deno.name")
}
