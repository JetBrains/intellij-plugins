// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.options

import com.intellij.javascript.util.JSLogOnceService
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.platform.lsp.api.LspClientManager
import kotlinx.serialization.Serializable
import org.jetbrains.vuejs.lang.typescript.service.allVueServiceRuntimes
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspIntegrationHybridModeProvider

@Service(Service.Level.PROJECT)
@State(
  name = "VueSettings",
  storages = [Storage(StoragePathMacros.WORKSPACE_FILE)],
)
class VueSettings(private val project: Project) :
  SerializablePersistentStateComponent<VueSettings.State>(State()) {

  var serviceType: VueLSMode
    get() = state.serviceType
    set(value) {
      if (value == state.serviceType)
        return

      updateState { state -> state.copy(serviceType = value) }
    }

  val useTypesFromServer: Boolean
    get() {
      val result =
        TypeScriptCompilerSettings.useTypesFromServerInTests
        ?: useServicePoweredTypesManualOverride
        ?: Registry.`is`("vue.service.powered.type.engine.enabled.by.default")
      with(project.service<JSLogOnceService>()) {
        LOG.infoOnce { "'Service-powered type engine' option of VueSettings: $result" }
      }
      return result
    }

  var useServicePoweredTypesManualOverride: Boolean?
    get() = when {
      state.useServicePoweredTypesEnabledManually -> true
      state.useServicePoweredTypesDisabledManually -> false
      else -> null
    }
    set(value) {
      if (value == useTypesFromServer) return
      updateState { state ->
        state.copy(
          useServicePoweredTypesEnabledManually = value == true,
          useServicePoweredTypesDisabledManually = value == false,
        )
      }
    }

  companion object {
    private val LOG = logger<VueSettings>()
    fun instance(project: Project): VueSettings = project.service()
  }

  @Serializable
  data class State(
    val serviceType: VueLSMode = VueLSMode.AUTO,
    val useServicePoweredTypesEnabledManually: Boolean = false,
    val useServicePoweredTypesDisabledManually: Boolean = false,
  )
}

@Serializable
enum class VueLSMode {
  AUTO,
  DISABLED,

  ;

  fun isEnabled(): Boolean = this != DISABLED
}

/**
 * Restarts both TypeScript services (TS plugin, regular LSP) and the
 * Vue Hybrid Mode LSP server which has its own [com.intellij.platform.lsp.api.LspIntegrationProvider]
 * not backed by a [com.intellij.lang.typescript.compiler.TypeScriptService].
 */
internal fun restartVueServicesAsync(project: Project) {
  restartTypeScriptServicesAsync(project)
  ApplicationManager.getApplication().invokeLater(
    {
      for (runtime in allVueServiceRuntimes) {
        LspClientManager.getInstance(project)
          .stopAndRestartClientsIfNeeded(VueLspIntegrationHybridModeProvider.getProviderClass(runtime))
      }
    },
    project.disposed
  )
}