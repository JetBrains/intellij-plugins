// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.options

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.util.JSLogOnceService
import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.lsp.JSBundledServiceNodePackage
import com.intellij.lang.typescript.lsp.createPackage
import com.intellij.lang.typescript.lsp.restartTypeScriptServicesAsync
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.text.SemVer
import kotlinx.serialization.Serializable
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerLoader
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginBundledLoaderFactory.getLoader
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginVersion
import org.jetbrains.vuejs.lang.typescript.service.vueTSPluginPackageName

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
      restartTypeScriptServicesAsync(project)
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
      restartTypeScriptServicesAsync(project)
    }

  val manualSettings: ManualSettings = ManualSettings()

  inner class ManualSettings {
    var mode: ManualMode
      get() = state.manual.mode
      set(value) {
        if (value == state.manual.mode)
          return

        updateState { state -> state.copy(manual = state.manual.copy(mode = value)) }
        restartTypeScriptServicesAsync(project)
      }

    var lspServerPackage: NodePackage
      get() {
        return createPackage(
          ref = state.manual.lspServerPackagePath,
          defaultPackage = VueLspServerLoader.packageDescriptor.serverPackage,
        )
      }
      set(value) {
        val path = value.systemDependentPath
        if (path == state.manual.lspServerPackagePath)
          return

        updateState { state ->
          state.copy(
            manual = state.manual.copy(lspServerPackagePath = path)
          )
        }
        restartTypeScriptServicesAsync(project)
      }

    var tsPluginPackage: NodePackage
      get() {
        val tsPluginPackage = state.manual.tsPluginPackage
        return when (tsPluginPackage) {
          is ManualPackageBundled -> createBundledPackage(
            versionString = tsPluginPackage.version,
            project = project,
          )

          is ManualPackageFS -> createPackage(
            ref = tsPluginPackage.path,
            defaultPackage = getDefaultTsPluginPackage(),
          )

          null -> createBundledPackage(
            versionString = VueTSPluginVersion.DEFAULT.versionString,
            project = project,
          )
        }
      }
      set(value) {
        val newTSPluginPackage = when (value) {
          is JSBundledServiceNodePackage -> ManualPackageBundled(
            version = value.version.toString(),
          )

          else -> ManualPackageFS(
            path = value.systemDependentPath,
          )
        }

        if (newTSPluginPackage == state.manual.tsPluginPackage)
          return

        updateState { state ->
          state.copy(
            manual = state.manual.copy(tsPluginPackage = newTSPluginPackage)
          )
        }
        restartTypeScriptServicesAsync(project)
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
    val manual: ManualSettingsState = ManualSettingsState(),
  )

  @Serializable
  data class ManualSettingsState(
    val mode: ManualMode = ManualMode.ONLY_TS_PLUGIN,
    val lspServerPackagePath: String? = null,
    val tsPluginPackage: ManualServicePackage? = null,
  )

  @Serializable
  enum class ManualMode(
    @param:NlsSafe
    val displayName: String,
  ) {
    ONLY_TS_PLUGIN("TypeScript plugin"),
    ONLY_LSP_SERVER("Language server"),

    ;
  }

  @Serializable
  sealed interface ManualServicePackage

  @Serializable
  data class ManualPackageBundled(
    val version: String,
  ) : ManualServicePackage

  @Serializable
  data class ManualPackageFS(
    val path: String,
  ) : ManualServicePackage
}

private fun createBundledPackage(
  versionString: String,
  project: Project,
): JSBundledServiceNodePackage {
  val path = getLoader(versionString).getAbsolutePath(project)

  return JSBundledServiceNodePackage(
    packageName = vueTSPluginPackageName,
    packageVersion = SemVer.parseFromText(versionString),
    path = path,
  )
}

private fun getDefaultTsPluginPackage(): TypeScriptPackageName {
  return getLoader(VueTSPluginVersion.DEFAULT)
    .packageDescriptor
    .serverPackage
}

@Serializable
enum class VueLSMode {
  AUTO,
  MANUAL,
  DISABLED,

  ;

  fun isEnabled(): Boolean = this != DISABLED
}