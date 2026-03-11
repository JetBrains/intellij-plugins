// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.options

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.util.JSLogOnceService
import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.lsp.JSBundledServiceNodePackage
import com.intellij.lang.typescript.lsp.createPackage
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
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.registry.Registry
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.util.text.SemVer
import kotlinx.serialization.Serializable
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.allVueServiceRuntimes
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeSupportProvider
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerTakeoverModeLoader
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.vueLspPackageName
import org.jetbrains.vuejs.lang.typescript.service.vueTSPluginPackageName

@Service(Service.Level.PROJECT)
@State(
  name = "VueSettings",
  storages = [Storage(StoragePathMacros.WORKSPACE_FILE)],
)
class VueSettings(private val project: Project) :
  SerializablePersistentStateComponent<VueSettings.State>(State()) {

  override fun loadState(state: State) {
    super.loadState(normalizeState(state))
  }

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

  val manualSettings: ManualSettings = ManualSettings()

  inner class ManualSettings {
    var mode: ManualMode
      get() = state.manual.mode
      set(value) {
        if (value == state.manual.mode)
          return

        updateState { state -> state.copy(manual = state.manual.copy(mode = value)) }
      }

    var lspServerPackage: NodePackage
      get() {
        return createPackage(
          ref = state.manual.lspServerPackagePath,
          defaultPackage = VueLspServerTakeoverModeLoader.packageDescriptor.serverPackage,
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
      }

    var lspHybridModePackage: NodePackage
      get() {
        return when (val lspHybridModePackage = state.manual.lspHybridModePackage) {
          is ManualPackageBundled -> createLspHybridModeBundledPackage(
            versionString = lspHybridModePackage.version,
            project = project,
          )

          is ManualPackageCustom -> createPackage(
            ref = lspHybridModePackage.path,
            defaultPackage = getDefaultLspHybridModePackage(),
          )

          null -> createLspHybridModeBundledPackage(
            versionString = VueLanguageToolsVersion.DEFAULT.versionString,
            project = project,
          )
        }
      }
      set(value) {
        val newPackage = when (value) {
          is JSBundledServiceNodePackage -> ManualPackageBundled(
            version = value.version.toString(),
          )

          else -> ManualPackageCustom(
            path = value.systemDependentPath,
          )
        }

        if (newPackage == state.manual.lspHybridModePackage)
          return

        updateState { state ->
          state.copy(
            manual = state.manual.copy(lspHybridModePackage = newPackage)
          )
        }
      }

    var tsPluginPackage: NodePackage
      get() {
        return when (val tsPluginPackage = state.manual.tsPluginPackage) {
          is ManualPackageBundled -> createTsPluginBundledPackage(
            versionString = tsPluginPackage.version,
            project = project,
          )

          is ManualPackageCustom -> createPackage(
            ref = tsPluginPackage.path,
            defaultPackage = getDefaultTsPluginPackage(),
          )

          null -> createTsPluginBundledPackage(
            versionString = VueLanguageToolsVersion.DEFAULT.versionString,
            project = project,
          )
        }
      }
      set(value) {
        val newTSPluginPackage = when (value) {
          is JSBundledServiceNodePackage -> ManualPackageBundled(
            version = value.version.toString(),
          )

          else -> ManualPackageCustom(
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
      }
  }

  private fun normalizeState(state: State): State {
    val manual = state.manual
    val normalizedTsPlugin = normalizeBundledPackage(manual.tsPluginPackage)
    val normalizedLspHybrid = normalizeBundledPackage(manual.lspHybridModePackage)
    if (normalizedTsPlugin == manual.tsPluginPackage && normalizedLspHybrid == manual.lspHybridModePackage) {
      return state
    }
    return state.copy(manual = manual.copy(
      tsPluginPackage = normalizedTsPlugin,
      lspHybridModePackage = normalizedLspHybrid,
    ))
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
    val lspHybridModePackage: ManualServicePackage? = null,
    val tsPluginPackage: ManualServicePackage? = null,
  )
  @Serializable
  enum class ManualMode(
    @param:NlsSafe
    val displayName: String,
  ) {
    HYBRID_MODE("Hybrid mode"),
    ONLY_TS_PLUGIN("TypeScript plugin"),
    ONLY_LSP_SERVER("Takeover mode"),

    ;
  }

  @Serializable
  sealed interface ManualServicePackage

  @Serializable
  data class ManualPackageBundled(
    val version: String,
  ) : ManualServicePackage

  @Serializable
  data class ManualPackageCustom(
    val path: String,
  ) : ManualServicePackage
}

private fun normalizeBundledPackage(pkg: VueSettings.ManualServicePackage?): VueSettings.ManualServicePackage? {
  if (pkg !is VueSettings.ManualPackageBundled)
    return pkg

  val resolved = VueLanguageToolsVersion.fromVersionOrInfer(pkg.version)
  if (resolved.versionString == pkg.version)
    return pkg

  return pkg.copy(version = resolved.versionString)
}

private fun createTsPluginBundledPackage(
  versionString: String,
  project: Project,
): JSBundledServiceNodePackage {
  val path = VueTSPluginLoaderFactory.getLoader(versionString).getAbsolutePath(project)

  return JSBundledServiceNodePackage(
    packageName = vueTSPluginPackageName,
    packageVersion = SemVer.parseFromText(versionString),
    path = path,
  )
}

private fun getDefaultTsPluginPackage(): TypeScriptPackageName {
  val runtime = VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT)
  return VueTSPluginLoaderFactory.getLoader(runtime)
    .packageDescriptor
    .serverPackage
}

private fun createLspHybridModeBundledPackage(
  versionString: String,
  project: Project,
): NodePackage {
  val path = VueLspServerHybridModeLoaderFactory.getLoader(versionString).getAbsolutePath(project)

  return JSBundledServiceNodePackage(
    packageName = vueLspPackageName,
    packageVersion = SemVer.parseFromText(versionString),
    path = path,
  )
}

private fun getDefaultLspHybridModePackage(): TypeScriptPackageName {
  val runtime = VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT)
  return VueLspServerHybridModeLoaderFactory.getLoader(runtime)
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

/**
 * Restarts both TypeScript services (TS plugin, regular LSP) and the
 * Vue Hybrid Mode LSP server which has its own [com.intellij.platform.lsp.api.LspServerSupportProvider]
 * not backed by a [com.intellij.lang.typescript.compiler.TypeScriptService].
 */
internal fun restartVueServicesAsync(project: Project) {
  restartTypeScriptServicesAsync(project)
  ApplicationManager.getApplication().invokeLater(
    {
      for (runtime in allVueServiceRuntimes) {
        LspServerManager.getInstance(project)
          .stopAndRestartIfNeeded(VueLspServerHybridModeSupportProvider.getProviderClass(runtime))
      }
    },
    project.disposed
  )
}