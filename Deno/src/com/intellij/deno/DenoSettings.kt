package com.intellij.deno

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

class DenoState {
  var useDeno = false
}

@State(name = "DenoSettings", storages = [Storage("deno.xml")])
class DenoSettings : PersistentStateComponent<DenoState> {
  companion object {
    fun getService(project: Project): DenoSettings {
      return ServiceManager.getService(project, DenoSettings::class.java)
    }
  }

  private var state = DenoState()

  override fun getState(): DenoState? {
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
}