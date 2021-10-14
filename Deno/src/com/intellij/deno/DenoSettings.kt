package com.intellij.deno

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptMessageBus
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsListener
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.ThrowableRunnable

class DenoState {
  var useDeno = false
  var denoPath = ""
  var denoCache = ""
}

@State(name = "DenoSettings", storages = [Storage("deno.xml")])
class DenoSettings(val project: Project) : PersistentStateComponent<DenoState> {
  companion object {
    fun getService(project: Project): DenoSettings {
      return project.getService(DenoSettings::class.java)
    }
  }

  private var state = DenoState()

  override fun getState(): DenoState {
    return state
  }

  override fun loadState(state: DenoState) {
    this.state = state
  }

  fun setUseDeno(useDeno: Boolean) {
    this.state.useDeno = useDeno
  }

  fun isUseDeno(): Boolean {
    return this.state.useDeno
  }

  fun getDenoPath(): String {
    val denoPath = this.state.denoPath
    if (denoPath.isEmpty()) {
      return DenoUtil.getDefaultDenoExecutable() ?: ""
    }
    return denoPath
  }

  fun setDenoPath(path: String) {
    val defaultPath = DenoUtil.getDefaultDenoExecutable() ?: ""
    this.state.denoPath = if (defaultPath == path) "" else path
  }

  fun getDenoCache(): String {
    val denoCache = this.state.denoCache
    if (denoCache.isEmpty()) {
      return DenoUtil.getDenoCache()
    }
    return denoCache
  }

  fun getDenoCacheDeps(): String {
    return getDenoCache() + "/deps"
  }

  fun setDenoCache(path: String) {
    this.state.denoCache = if (DenoUtil.getDenoCache() == path) "" else path
  }

  fun setUseDenoAndReload(useDeno: Boolean) {
    setUseDenoAndReload(useDeno, true)
  }

  fun setUseDenoAndReload(useDeno: Boolean, restartService: Boolean) {
    val libraryProvider = AdditionalLibraryRootsProvider.EP_NAME.findExtensionOrFail(DenoLibraryProvider::class.java)
    val oldRoots = libraryProvider.getRootsToWatch(project)
    setUseDeno(useDeno)

    WriteAction.run(
      ThrowableRunnable<ConfigurationException> {
        if (restartService) TypeScriptService.restartServices(project, true)

        val newRoots = libraryProvider.getRootsToWatch(project)
        AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, null, oldRoots, newRoots, "Deno")

        DaemonCodeAnalyzer.getInstance(project).restart()
      })
  }
}