// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.options

import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.isEffectiveUseTypesFromServer
import com.intellij.lang.typescript.compiler.ui.TypeScriptServiceRestartService
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
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

  var serviceType: AngularServiceSettings
    get() = state.innerServiceType
    set(value) {
      val prevServiceType = state.innerServiceType
      state.innerServiceType = value
      if (prevServiceType != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(prevServiceType == AngularServiceSettings.AUTO, state.useTypesFromServer)
            != isEffectiveUseTypesFromServer(state.innerServiceType == AngularServiceSettings.AUTO, state.useTypesFromServer))
      }
    }

  var useTypesFromServer: Boolean
    get() {
      val useTypesFromServerInTests = TypeScriptCompilerSettings.isUseTypesFromServerInTests()
      return useTypesFromServerInTests ?: state.useTypesFromServer
    }
    set(value) {
      val prevUseTypesFromServer = state.useTypesFromServer
      state.useTypesFromServer = value
      if (prevUseTypesFromServer != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(serviceType == AngularServiceSettings.AUTO, prevUseTypesFromServer)
            != isEffectiveUseTypesFromServer(serviceType == AngularServiceSettings.AUTO, state.useTypesFromServer))
      }
    }
}

class AngularSettingsState : BaseState() {
  var innerServiceType: AngularServiceSettings by enum(AngularServiceSettings.AUTO)
  var packageName: String? by string(defaultPackageKey)
  var useTypesFromServer: Boolean by property(true)
}

enum class AngularServiceSettings {
  AUTO,
  DISABLED
}