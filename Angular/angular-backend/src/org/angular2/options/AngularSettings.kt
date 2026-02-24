// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.options

import com.intellij.javascript.util.JSLogOnceService
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil.isEffectiveUseTypesFromServer
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.ui.TypeScriptServiceRestartService
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.TestOnly

fun getAngularSettings(project: Project): AngularSettings = project.service<AngularSettings>()

@TestOnly
fun configureAngularSettingsService(project: Project, disposable: Disposable, serviceSettings: AngularServiceSettings) {
  val angularSettings = getAngularSettings(project)
  val old = angularSettings.serviceType
  angularSettings.serviceType = serviceSettings

  Disposer.register(disposable) {
    angularSettings.serviceType = old
  }
}

@Service(Service.Level.PROJECT)
@State(name = "AngularSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AngularSettings(val project: Project) : SimplePersistentStateComponent<AngularSettingsState>(AngularSettingsState()) {

  companion object {
    private val LOG = logger<AngularSettings>()
  }

  var serviceType: AngularServiceSettings
    get() = state.innerServiceType
    set(value) {
      val prevServiceType = state.innerServiceType
      state.innerServiceType = value
      if (prevServiceType != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(prevServiceType == AngularServiceSettings.AUTO, useTypesFromServer)
            != isEffectiveUseTypesFromServer(state.innerServiceType == AngularServiceSettings.AUTO, useTypesFromServer))
      }
    }

  val useTypesFromServer: Boolean
    get() {
      val result =
        TypeScriptCompilerSettings.useTypesFromServerInTests
        ?: useServicePoweredTypesManualOverride
        ?: (Registry.`is`("angular.service.powered.type.engine.enabled.by.default") && state.useTypesFromServer)
      with(project.service<JSLogOnceService>()) {
        LOG.infoOnce { "'Service-powered type engine' option of AngularSettings: $result" }
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
      val prevUseTypesFromServer = useTypesFromServer
      state.useServicePoweredTypesEnabledManually = value == true
      state.useServicePoweredTypesDisabledManually = value == false
      if (prevUseTypesFromServer != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(serviceType == AngularServiceSettings.AUTO, prevUseTypesFromServer)
            != isEffectiveUseTypesFromServer(serviceType == AngularServiceSettings.AUTO, useTypesFromServer))
      }
    }
}

class AngularSettingsState : BaseState() {
  var innerServiceType: AngularServiceSettings by enum(AngularServiceSettings.AUTO)
  var packageName: String? by string(defaultPackageKey)
  @Deprecated(message = "Use useServicePoweredTypesEnabledManually and useServicePoweredTypesDisabledManually")
  var useTypesFromServer: Boolean by property(true)
  var useServicePoweredTypesEnabledManually: Boolean by property(false)
  var useServicePoweredTypesDisabledManually: Boolean by property(false)
}

enum class AngularServiceSettings {
  AUTO,
  DISABLED
}