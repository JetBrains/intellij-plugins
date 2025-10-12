package com.intellij.deno.settings

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.findDenoConfig
import com.intellij.javascript.runtime.JSDetectableRuntimeType
import com.intellij.javascript.runtime.JSRuntimeProvider
import com.intellij.javascript.runtime.JSRuntimeType
import com.intellij.javascript.runtime.settings.JSRuntimeConfigurationListener
import com.intellij.openapi.application.UiWithModelAccess
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

internal class DenoRuntimeProvider : JSRuntimeProvider {
  override val runtimeType: JSRuntimeType
    get() = DenoRuntimeType

  override fun createSettingsConfigurable(project: Project): Configurable {
    return DenoSettingsConfigurable(project)
  }
}

internal object DenoRuntimeType : JSDetectableRuntimeType, JSRuntimeConfigurationListener {
  override val id: @NonNls String
    get() = "deno"

  override val displayName: @Nls String
    get() = DenoBundle.message("deno.name")

  override fun detect(project: Project): Boolean {
    return findDenoConfig(project, project.guessProjectDir()) != null
  }

  override fun onConfigured(project: Project, type: JSRuntimeType) {
    val service = DenoSettings.getService(project)
    if (service.getUseDeno() != type.useDeno) {
      service.coroutineScope.launch(Dispatchers.UiWithModelAccess) {
        service.setUseDenoAndReload(type.useDeno)
      }
    }
  }
}

internal val JSRuntimeType?.useDeno: UseDeno
  get() = if (this == DenoRuntimeType) UseDeno.ENABLE else UseDeno.DISABLE
