package org.jetbrains.idea.perforce.application

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.wm.IdeFrame
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager

internal class P4LocalChangesRefresher : ApplicationActivationListener {
  override fun applicationActivated(ideFrame: IdeFrame) {
    if (!Registry.`is`("p4.refresh.local.changes.on.frame.activation")) return
    val project = ideFrame.project ?: return

    val connectionManager = PerforceConnectionManager.getInstance(project)
    val dirtyScopeManager = VcsDirtyScopeManager.getInstance(project)
    connectionManager.allConnections.forEach { (root, _) ->
      dirtyScopeManager.dirDirtyRecursively(root)
    }
  }
}
