// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.options

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil.isEffectiveUseTypesFromServer
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.ui.TypeScriptServiceRestartService
import com.intellij.lang.typescript.lsp.createPackageRef
import com.intellij.lang.typescript.lsp.defaultPackageKey
import com.intellij.lang.typescript.lsp.extractRefText
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.TestOnly
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptPluginServiceWrapper
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerLoader
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory

fun getVueSettings(project: Project): VueSettings = project.service<VueSettings>()

@TestOnly
fun configureVueService(project: Project, disposable: Disposable, serviceSettings: VueServiceSettings) {
  val vueSettings = getVueSettings(project)
  val old = vueSettings.serviceType
  vueSettings.serviceType = serviceSettings

  Disposer.register(disposable) {
    vueSettings.serviceType = old
  }
}

@Service(Service.Level.PROJECT)
@State(name = "VueSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class VueSettings(val project: Project) : SimplePersistentStateComponent<VueSettingsState>(VueSettingsState()) {

  var serviceType: VueServiceSettings
    get() = state.innerServiceType
    set(value) {
      val prevServiceType = state.innerServiceType
      state.innerServiceType = value
      if (prevServiceType != value && !project.isDisposed) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(prevServiceType.isEnabled(), state.useTypesFromServer)
            != isEffectiveUseTypesFromServer(serviceType.isEnabled(), state.useTypesFromServer))
      }
    }

  var packageRef: NodePackageRef
    get() = createPackageRef(state.packageName, VueLspServerLoader.packageDescriptor.serverPackage)
    set(value) {
      val refText = extractRefText(value)
      val changed = state.packageName != refText
      state.packageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var tsPluginPackageRef: NodePackageRef
    get() = createPackageRef(
      ref = state.tsPluginPackageName,
      defaultPackage = VueTSPluginLoaderFactory.getLoader(tsPluginVersion).packageDescriptor.serverPackage,
    )
    set(value) {
      val refText = extractRefText(value)
      val changed = state.tsPluginPackageName != refText
      state.tsPluginPackageName = refText
      if (changed) restartTypeScriptServicesAsync(project)
    }

  var useTypesFromServer: Boolean
    get() {
      return TypeScriptCompilerSettings.useTypesFromServerInTests ?: state.useTypesFromServer
    }
    set(value) {
      val prevUseTypesFromServer = state.useTypesFromServer
      state.useTypesFromServer = value
      if (prevUseTypesFromServer != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(
          isEffectiveUseTypesFromServer(serviceType.isEnabled(), prevUseTypesFromServer)
            != isEffectiveUseTypesFromServer(serviceType.isEnabled(), state.useTypesFromServer))
      }
    }

  var tsPluginPreviewEnabled: Boolean
    get() {
      return state.tsPluginPreviewEnabled
    }
    set(value) {
      val prevTsPluginPreviewEnabled = state.tsPluginPreviewEnabled
      state.tsPluginPreviewEnabled = value
      if (prevTsPluginPreviewEnabled != value) {
        project.service<TypeScriptServiceRestartService>().restartServices(false)
      }
    }

  var tsPluginVersion: VueTSPluginVersion
    get() {
      return state.tsPluginVersion
    }
    set(value) {
      val prevTsPluginVersion = state.tsPluginVersion
      state.tsPluginVersion = value
      if (prevTsPluginVersion != value) {
        project.service<VueTypeScriptPluginServiceWrapper>().refreshService(project)
        project.service<TypeScriptServiceRestartService>().restartServices(false)
      }
    }
}

class VueSettingsState : BaseState() {
  var innerServiceType: VueServiceSettings by enum(VueServiceSettings.AUTO)
  var packageName: String? by string(defaultPackageKey)

  // TODO: Restore in WEB-74847
  // var useTypesFromServer: Boolean by property(false)
  var useTypesFromServer: Boolean; get() = false; set(_) {}
  var tsPluginPackageName: String? by string(defaultPackageKey)
  var tsPluginPreviewEnabled: Boolean by property(true)
  var tsPluginVersion: VueTSPluginVersion by enum(VueTSPluginVersion.V3_0_10)
}

enum class VueTSPluginVersion(
  @param:NlsSafe val versionString: String,
) {
  V3_0_10("3.0.10"),
  V3_2_4("3.2.4"),

  ;
}

enum class VueServiceSettings {
  AUTO,
  DISABLED,

  ;

  fun isEnabled(): Boolean = this != DISABLED
}