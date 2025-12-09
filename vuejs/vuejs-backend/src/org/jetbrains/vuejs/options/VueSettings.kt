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
import com.intellij.openapi.components.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.ApiStatus.Obsolete
import org.jetbrains.annotations.TestOnly
import org.jetbrains.vuejs.lang.typescript.service.VueLspServerLoader
import org.jetbrains.vuejs.lang.typescript.service.VueTSPluginLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptPluginServiceWrapper

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
  var useTypesFromServer: Boolean by property(false)
  var tsPluginPackageName: String? by string(defaultPackageKey)
  var tsPluginPreviewEnabled: Boolean by property(true)
  var tsPluginVersion: VueTSPluginVersion by enum(VueTSPluginVersion.V3_0_1)
}

enum class VueTSPluginVersion(
  @param:NlsSafe val versionString: String,
) {
  V3_0_1("3.0.1"),
  V3_1_8("3.1.8"),
  ;
}

enum class VueServiceSettings {
  AUTO,

  /**
   * Must work exactly the same as AUTO, kept for settings deserialization compatibility, aka Vue LSP
   */
  @Obsolete
  VOLAR,
  TS_SERVICE,
  DISABLED;

  fun isEnabled(): Boolean = this != DISABLED
}