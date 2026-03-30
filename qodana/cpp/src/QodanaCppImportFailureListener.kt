package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager
import com.jetbrains.cidr.project.workspace.CidrWorkspaceState

/**
 * Marks workspaces as Loaded when an external system import fails at the resolve phase.
 *
 * When the resolver throws (e.g. broken Makefile), [com.jetbrains.cidr.external.system.service.ExternalModuleDataService.postProcess]
 * is never called, so [com.jetbrains.cidr.external.system.workspace.ExternalWorkspace.doUpdate] never runs and the workspace stays
 * stuck in [CidrWorkspaceState.Initialized]. Per the ExternalWorkspace doc comment:
 * "In that case you must call CidrWorkspaceManager.markLoaded yourself."
 *
 * This unblocks the Radler backend startup (which waits for all workspaces to reach a final state
 * before signaling fullStartupFinished) and lets Qodana detect the failure cleanly.
 */
class QodanaCppImportFailureListener : ExternalSystemTaskNotificationListener {
    companion object {
        private val log = logger<QodanaCppImportFailureListener>()
    }

    override fun onFailure(projectPath: String, id: ExternalSystemTaskId, exception: Exception) {
        if (!PlatformUtils.isQodana()) return
        val project = id.findProject() ?: return

        val wsManager = CidrWorkspaceManager.getInstance(project)
        for ((workspace, state) in wsManager.workspaces) {
            if (state == CidrWorkspaceState.Initialized) {
                log.info("Marking stuck ${workspace.javaClass.simpleName} as Loaded after import failure: ${exception.message}")
                wsManager.markLoaded(workspace)
            }
        }
    }
}
