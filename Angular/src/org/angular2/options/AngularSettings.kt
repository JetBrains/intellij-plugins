// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.options

import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
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

  var serviceType
    get() = state.innerServiceType
    set(value) {
      val changed = state.innerServiceType != value
      state.innerServiceType = value
      if (changed) restartTypeScriptServicesAsync(project)
    }

}

class AngularSettingsState : BaseState() {
  var innerServiceType by enum(AngularServiceSettings.AUTO)
  var packageName by string(defaultPackageKey)
}

enum class AngularServiceSettings {
  AUTO,
  DISABLED
}