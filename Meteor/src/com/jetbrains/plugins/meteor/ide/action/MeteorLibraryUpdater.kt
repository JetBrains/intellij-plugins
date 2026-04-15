package com.jetbrains.plugins.meteor.ide.action

import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.util.ui.update.DebouncedUpdates
import com.intellij.util.ui.update.UpdateQueue
import com.jetbrains.plugins.meteor.MeteorFacade
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity
import com.jetbrains.plugins.meteor.initMeteorProject
import com.jetbrains.plugins.meteor.settings.MeteorSettings
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.TestOnly
import org.jetbrains.annotations.VisibleForTesting
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.minutes

private val LOG = logger<MeteorLibraryUpdater>()

internal class MeteorLibraryUpdater(private val project: Project, coroutineScope: CoroutineScope) : Disposable {
  private val queue: UpdateQueue<Unit>
  private val projectStatusQueue: UpdateQueue<Unit>

  init {
    LOG.assertTrue(!project.isDefault)
    queue = DebouncedUpdates.forScope<Unit>(coroutineScope, "Meteor update packages", 300)
      .runLatest { performUpdate() }
    projectStatusQueue = DebouncedUpdates.forScope<Unit>(coroutineScope, "Meteor project status", 1000)
      .restartTimerOnAdd(true)
      .runLatest {
        smartReadAction(project) {
          findAndInitMeteorRoots(project)
        }
      }
  }

  fun update() {
    if (!MeteorSettings.getInstance().isAutoImport) {
      return
    }

    queue.queue(Unit)
  }

  private suspend fun performUpdate() {
    smartReadAction(project) {
      LOG.debug { "Check meteor libraries" }
      if (updateStoredMeteorFolders()) {
        refreshMeteorLibraries(project = project, removeDeprecated = true)
        return@smartReadAction
      }
      updateMeteorLibraryIfRequired(project)
    }
  }

  private fun updateStoredMeteorFolders(): Boolean {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val stored = MeteorFacade.getInstance().getStoredMeteorFolders(project)
    val folders = MeteorFacade.getInstance().getMeteorFolders(project)
    if (stored == folders) {
      return false
    }

    MeteorFacade.getInstance().storeMeteorFolders(project, folders.map { it.path })

    return true
  }

  fun scheduleProjectUpdate() {
    projectStatusQueue.queue(Unit)
  }

  override fun dispose() {
  }

  @TestOnly
  fun waitForUpdate() {
    try {
      queue.waitForAllExecuted(1.minutes)
      projectStatusQueue.waitForAllExecuted(1.minutes)
    }
    catch (e: TimeoutException) {
      throw RuntimeException(e)
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): MeteorLibraryUpdater = project.service()
  }
}

internal fun updateMeteorLibraryIfRequired(project: Project) {
  ApplicationManager.getApplication().assertReadAccessAllowed()

  val codes = MeteorPackagesUtil.getCodes(project)

  val pathToMeteorGlobal = MeteorPackagesUtil.getPathToGlobalMeteorRoot(project)
  if (pathToMeteorGlobal.isNullOrEmpty()) {
    return
  }

  val dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(project, null)
  if (dotMeteorVirtualFile == null) {
    LOG.debug { "Cannot find .meteor folder" }
    return
  }

  if (codes.isEmpty()) {
    return
  }

  val roots = MeteorSyntheticLibraryProvider.getRoots(project)
  val index = ProjectFileIndex.getInstance(project)
  val needToUpdateLibrary = roots.any { !index.isInLibrary(it) }
  if (needToUpdateLibrary) {
    refreshMeteorLibraries(project, false)
  }
}

internal fun refreshMeteorLibraries(project: Project, removeDeprecated: Boolean) {
  ApplicationManager.getApplication().invokeLater(
    {
      WriteAction.run<RuntimeException> {
        val libraryManager = JSLibraryManager.getInstance(project)
        if (removeDeprecated) {
          removeDeprecatedLibraries(libraryManager)
        }
        libraryManager.commitChanges(RootsChangeRescanningInfo.RESCAN_DEPENDENCIES_IF_NEEDED)
      }
    }, project.disposed)
}

private fun removeDeprecatedLibraries(libraryManager: JSLibraryManager) {
  for (value in MeteorImportPackagesAsExternalLib.CodeType.entries) {
    val name = MeteorImportPackagesAsExternalLib.getLibraryName(value)
    val model = libraryManager.getLibraryByName(name)
    if (model != null) {
      libraryManager.removeLibrary(model)
    }
  }
}

@VisibleForTesting
internal fun findAndInitMeteorRoots(project: Project) {
  val meteorFacade = MeteorFacade.getInstance()
  val shouldUpdate = if (!meteorFacade.isMeteorProject(project) &&
                         (meteorFacade.hasMeteorFolders(project) || projectHasExcludedMeteorFolder(project))) {
    meteorFacade.setIsMeteorProject(project)
    true
  }
  else {
    false
  }

  if (meteorFacade.isMeteorProject(project)) {
    initMeteorProject(project, shouldUpdate)
  }
}

/**
 * Some users want to exclude and hide '.meteor' folder from the project.
 * So we have to implement a logic for checking that the folder '.meteor' was excluded from the project
 *
 *
 * return true if excluded '.meteor' folder was detected
 */
private fun projectHasExcludedMeteorFolder(project: Project): Boolean {
  for (module in ModuleManager.getInstance(project).modules) {
    for (url in ModuleRootManager.getInstance(module).excludeRootUrls) {
      val trimEnd = url.removeSuffix("/")
      if (trimEnd.endsWith("/" + MeteorProjectStartupActivity.METEOR_FOLDER) && !JSLibraryUtil.isProbableLibraryPath(trimEnd)) {
        return true
      }
    }
  }
  return false
}

