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
 * When the resolver throws (e.g. broken Makefile), `ExternalModuleDataService.postProcess`
 * is never called, so `ExternalWorkspace.doUpdate` never runs and the workspace stays
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
        val selectedName = selectedWorkspaceComponentName
        var selectedBuildSystemFailed = false
        var anyFailedWorkspaceName: String? = null
        for ((workspace, state) in wsManager.workspaces) {
            if (state == CidrWorkspaceState.Initialized) {
                log.info("Marking stuck ${workspace.javaClass.simpleName} as Loaded after import failure: ${exception.message}")
                wsManager.markLoaded(workspace)
                if (anyFailedWorkspaceName == null) {
                    anyFailedWorkspaceName = workspace.javaClass.simpleName
                }
                if (workspace.javaClass.simpleName == selectedName) {
                    selectedBuildSystemFailed = true
                }
            }
        }

        // QD-14621: signal the startup manager so it can fail fast with a build-system-aware
        // diagnostic ("Failed to configure project as a ${buildSystem} workspace.") instead of
        // hanging on the Radler backend's `fullStartupFinished` (which never fires when the build
        // system produces no usable project model).
        //
        // Pass the failed workspace name captured here (not re-read in failWithBuildSystemError) to
        // avoid the race with a subsequent selectProcessor() resetting the AtomicReference.
        //
        // Two cases:
        //   1) selectedName is set (normal Qodana C++ flow — selectProcessor ran first): signal only
        //      when the *selected* build system's workspace failed, so collateral failures of
        //      unselected workspaces don't trigger spurious fast-fail.
        //   2) selectedName is null (pathological — selectProcessor didn't run or threw before
        //      setting the ref): log it and signal with the failed workspace name anyway, so we
        //      still produce a build-system-aware diagnostic instead of falling back to the generic
        //      backend-startup-timeout message.
        when {
            selectedBuildSystemFailed -> {
                QodanaCppStartupManager.getInstance(project)
                    .notifyBuildSystemImportFailure(selectedName, exception.message ?: exception.javaClass.simpleName)
            }
            selectedName == null && anyFailedWorkspaceName != null -> {
                log.warn(
                    "External system import failed but selectedWorkspaceComponentName is null. " +
                    "Signaling fast-fail with the first failed workspace ($anyFailedWorkspaceName) " +
                    "as the build system. This indicates Qodana C++ project selection did not run " +
                    "as expected."
                )
                QodanaCppStartupManager.getInstance(project)
                    .notifyBuildSystemImportFailure(anyFailedWorkspaceName, exception.message ?: exception.javaClass.simpleName)
            }
        }
    }
}
