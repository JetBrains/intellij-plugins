package org.jetbrains.qodana.cpp

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager
import com.jetbrains.cidr.project.workspace.CidrWorkspaceState
import com.intellij.clion.radler.core.inspections.RadHeadlessStartupExtension
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.channels.Channel
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException

class QodanaCppHeadlessStartupExtension : RadHeadlessStartupExtension {
  companion object {
    private val LOG = logger<QodanaCppHeadlessStartupExtension>()
  }

  override suspend fun afterCidrWorkspacesReady(project: Project) {
    if (!PlatformUtils.isQodana()) return

    try {
      afterCidrWorkspacesReadyImpl(project)
    } catch (e: QodanaException) {
      // Can't use QodanaException in this thread to exit qodana with an error.
      QodanaCppWorkflow.failLater(e)
      throw e
    }
  }

  suspend fun afterCidrWorkspacesReadyImpl(project: Project) {
    val wsManager = CidrWorkspaceManager.getInstance(project)
    check(wsManager.modelsAreReady) {
      "workspaceManager.modelsAreReady is false"
    }

    if (!wsManager.hasAnyLoadedModels) {
      throw QodanaException("Failed to load project: all supported build systems failed to load")
    }

    val requestedCMakeProfile = qodanaConfig.cpp?.cmakePreset
    if (requestedCMakeProfile != null) {
      val cmakeWorkspace = CMakeWorkspace.getInstance(project)
      val cmakeWorkspaceState = wsManager.workspaces[cmakeWorkspace]
      checkNotNull(cmakeWorkspaceState) {
        "CMakeWorkspace is not present in workspace manager"
      }

      if (cmakeWorkspaceState == CidrWorkspaceState.NotLoaded) {
        throw QodanaException("Cannot select CMake preset: error while loading CMake workspace")
      }

      // A single requested profile should have been enabled by this point in QodanaCppCMakeEnabledProfileInitializer, unless
      // the project was launched with existing settings in .idea, in which case the profile initializer will not be triggered.
      val allProfiles = cmakeWorkspace.settings.profiles
      if (allProfiles.none { it.name == requestedCMakeProfile }) {
        throw QodanaException("Cannot select CMake preset: preset \"$requestedCMakeProfile\" was not found")
      }

      val enabledProfiles = cmakeWorkspace.settings.activeProfiles
      if (enabledProfiles.size > 1 || enabledProfiles.none { it.name == requestedCMakeProfile }) {
        if (enabledProfiles.size > 1) {
          LOG.debug("Multiple profiles were enabled after initial project load: ${enabledProfiles.joinToString(", ") { it.name }}")
        } else {
          LOG.debug("No profiles were enabled after initial project load")
        }

        cmakeWorkspace.awaitingReload {
          cmakeWorkspace.settings.profiles = cmakeWorkspace.settings.profiles.map {  // This action will schedule a cmake reload
            it.copy(enabled = it.name == requestedCMakeProfile)
          }
          check(cmakeWorkspace.settings.activeProfiles.size == 1) {
           "CMake workspace should have exactly one enabled profile after enabling profile \"$requestedCMakeProfile\" and disabling all others"
          }
        }
      }
    }
  }
}

suspend fun <E> Channel<E>.discardUntil(value: E): E {
  var received: E
  do {
    received = receive()
  } while (received != value)
  return received
}

suspend fun CMakeWorkspace.awaitingReload(block: () -> Unit) {
  val RELOADING_STARTED = 1
  val RELOADING_FINISHED = 2

  val disposable = createDisposable()
  val messages = Channel<Int>(Channel.UNLIMITED)
  try {
    project.messageBus.connect(disposable).subscribe(CMakeWorkspaceListener.TOPIC, object : CMakeWorkspaceListener {
      override fun reloadingStarted() {
        messages.trySend(RELOADING_STARTED)
      }

      override fun reloadingFinished(canceled: Boolean) {
        messages.trySend(RELOADING_FINISHED)
      }
    })

    block()

    messages.discardUntil(RELOADING_STARTED)
    messages.discardUntil(RELOADING_FINISHED)
  }
  finally {
    Disposer.dispose(disposable)
    messages.close()
  }
}

