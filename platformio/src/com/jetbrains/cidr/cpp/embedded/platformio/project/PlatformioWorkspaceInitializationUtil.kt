package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceListener
import com.jetbrains.cidr.util.InitializationWaiter

object PlatformioWorkspaceInitializationUtil {
  val Project.isPlatformioWorkspaceInitialized: Boolean
    get() {
      return service<PlatformioWorkspace>().isInitialized
    }

  /**
   * Run [block] after [PlatformioWorkspace] is initialized
   *
   * Note, at this moment there might be no actual state loaded. Platformio might be reloaded after this point
   */
  fun Project.runAfterPlatformioInitialized(block: () -> Unit) {
    PlatformioWorkspaceInitializationWaiter(block, this)
      .waitForInitialization(service<PlatformioWorkspace>())
  }

  private class PlatformioWorkspaceInitializationWaiter(block: () -> Unit, project: Project) : InitializationWaiter(
    block,
    project) {
    override fun isInitialized(): Boolean =
      project.isPlatformioWorkspaceInitialized

    override fun subscribeToInitialization(project: Project, parentDisposable: Disposable, callBack: () -> Unit) {
      project.messageBus.connect(parentDisposable).subscribe(CidrWorkspaceListener.TOPIC, object : CidrWorkspaceListener {
        override fun initialized(workspace: CidrWorkspace) {
          if (workspace is PlatformioWorkspace) {
            callBack()
          }
        }
      })
    }
  }
}