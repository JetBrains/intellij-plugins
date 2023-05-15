package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.jetbrains.cidr.lang.workspace.OCWorkspaceListener

// A dummy example of listening for project model changes
class CdOCWorkspaceEventsListener(private val project: Project) : OCWorkspaceListener {

  override fun workspaceChanged(event: OCWorkspaceListener.OCWorkspaceEvent) {
    project.service<CdWorkspaceEvents>().workspaceChanged(event)
  }

  override fun selectedResolveConfigurationChanged() {
    project.service<CdWorkspaceEvents>().selectedResolveConfigurationChanged()
  }
}

@Service
class CdWorkspaceEvents {
  private val events = mutableListOf<String>()
  private val isEnabled = Registry.get(ENABLED_KEY).asBoolean()

  fun workspaceChanged(event: OCWorkspaceListener.OCWorkspaceEvent) {
    if (!isEnabled) return
    events.add("${formatCurrentTimeMS()}: $event")
  }

  fun selectedResolveConfigurationChanged() {
    if (!isEnabled) return
    events.add("${formatCurrentTimeMS()}: selectedResolveConfigurationChanged")
  }

  fun getResult(): String {
    if (isEnabled) {
      return events.joinToString("\n")
    }
    else {
      return CppDiagnosticsBundle.message("cpp.diagnostics.was.not.logged.0", ENABLED_KEY)
    }
  }

  companion object {
    private const val ENABLED_KEY = "cpp.diagnostics.track.events"
  }
}
