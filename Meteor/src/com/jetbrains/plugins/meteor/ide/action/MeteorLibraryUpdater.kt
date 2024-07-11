package com.jetbrains.plugins.meteor.ide.action

import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import com.intellij.util.SingleAlarm
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import com.jetbrains.plugins.meteor.MeteorFacade
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity
import com.jetbrains.plugins.meteor.settings.MeteorSettings
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class MeteorLibraryUpdater(project: Project) : Disposable {
  private val project: Project
  private val queue: MergingUpdateQueue
  private val projectStatusAlarm: SingleAlarm

  init {
    LOG.assertTrue(!project.isDefault)
    this.project = project
    queue = MergingUpdateQueue("Meteor update packages", 300, true, null, this, null, false)
    projectStatusAlarm = SingleAlarm({ this.findAndInitMeteorRootsWhenSmart() }, 1000, this, Alarm.ThreadToUse.POOLED_THREAD)
  }

  fun update() {
    if (!MeteorSettings.getInstance().isAutoImport) {
      return
    }

    queue.queue(object : Update(this) {
      override fun run() {
        DumbService.getInstance(project).runReadActionInSmartMode {
          LOG.debug(
            "Check meteor libraries")
          if (updateStoredMeteorFolders()) {
            refreshLibraries(
              project, true)
            return@runReadActionInSmartMode
          }
          updateLibraryIfRequired(
            project)
        }
      }
    })
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
    projectStatusAlarm.cancelAndRequest()
  }

  @RequiresBackgroundThread
  private fun findAndInitMeteorRootsWhenSmart() {
    DumbService.getInstance(project).runReadActionInSmartMode {
      findAndInitMeteorRoots(project)
    }
  }

  override fun dispose() {
    queue.cancelAllUpdates()
  }

  @TestOnly
  fun waitForUpdate() {
    do {
      try {
        projectStatusAlarm.waitForAllExecuted(1, TimeUnit.MINUTES)
        queue.waitForAllExecuted(1, TimeUnit.MINUTES)
        UIUtil.dispatchAllInvocationEvents()
      }
      catch (e: TimeoutException) {
        throw RuntimeException(e)
      }
    }
    while (!queue.isEmpty || !projectStatusAlarm.isEmpty)
  }

  companion object {
    private val LOG = Logger.getInstance(MeteorLibraryUpdater::class.java)

    fun get(project: Project): MeteorLibraryUpdater {
      return project.getService(MeteorLibraryUpdater::class.java)
    }

    fun updateLibraryIfRequired(project: Project) {
      ApplicationManager.getApplication().assertReadAccessAllowed()

      val codes = MeteorPackagesUtil.getCodes(project)

      val pathToMeteorGlobal = MeteorPackagesUtil.getPathToGlobalMeteorRoot(project)
      if (StringUtil.isEmpty(pathToMeteorGlobal)) return

      val dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(project, null)
      if (dotMeteorVirtualFile == null) {
        LOG.debug("Cannot find .meteor folder")
        return
      }

      if (codes.isEmpty()) return

      val roots = MeteorSyntheticLibraryProvider.getRoots(project)
      val index = ProjectFileIndex.getInstance(project)
      val needToUpdateLibrary = ContainerUtil.exists(roots
      ) { el: VirtualFile? ->
        !index.isInLibrary(
          el!!)
      }
      if (needToUpdateLibrary) {
        refreshLibraries(project, false)
      }
    }

    fun refreshLibraries(project: Project, removeDeprecated: Boolean) {
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

    @RequiresReadLock
    @RequiresBackgroundThread
    fun findAndInitMeteorRoots(project: Project) {
      val shouldUpdateFileTypes =
        ReadAction.compute<Boolean, RuntimeException> {
          if (project.isDisposed) {
            return@compute null
          }
          val meteorFacade = MeteorFacade.getInstance()
          val shouldUpdate: Boolean
          if (!meteorFacade.isMeteorProject(project) &&
              (meteorFacade.hasMeteorFolders(project) || projectHasExcludedMeteorFolder(project))
          ) {
            meteorFacade.setIsMeteorProject(project)
            shouldUpdate = true
          }
          else {
            shouldUpdate = false
          }

          if (!meteorFacade.isMeteorProject(project)) {
            return@compute null
          }
          shouldUpdate
        }
      if (shouldUpdateFileTypes != null) {
        MeteorProjectStartupActivity.initMeteorProject(project, shouldUpdateFileTypes)
      }
    }

    /**
     * Some users want to exclude and hide '.meteor' folder from the project.
     * So we have to implement a logic for checking that the folder '.meteor' was excluded from the project
     *
     *
     * return true if excluded '.meteor' folder was detected
     */
    fun projectHasExcludedMeteorFolder(project: Project): Boolean {
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
  }
}
