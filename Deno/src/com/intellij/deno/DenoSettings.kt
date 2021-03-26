package com.intellij.deno

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.typescript.compiler.TypeScriptCompilerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.util.ThrowableRunnable

class DenoState {
  var useDeno = false
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

  fun setUseDenoAndReload(useDeno: Boolean) {
    ApplicationManager.getApplication().isWriteAccessAllowed
    setUseDeno(useDeno)

    WriteAction.run(
      ThrowableRunnable<ConfigurationException> {
        TypeScriptCompilerService.restartServices(project)

        ThrowableRunnable<ConfigurationException> {
          ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(
            EmptyRunnable.getInstance(), false, true)
        }
        DaemonCodeAnalyzer.getInstance(project).restart()
      })
  }
}