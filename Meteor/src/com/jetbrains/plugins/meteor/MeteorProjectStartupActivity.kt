package com.jetbrains.plugins.meteor

import com.dmarcotte.handlebars.config.HbConfig
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.extensions.ExtensionNotApplicableException
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtilRt
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.Companion.JS_METEOR_LIBRARY_WAS_ENABLED
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.Companion.METEOR_LIBRARY_NAME
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.Companion.METEOR_LOCAL_FOLDER
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.Companion.METEOR_RELATIVE_PATH_TO_LOCAL_FOLDER
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater
import com.jetbrains.plugins.meteor.settings.MeteorSettings
import com.jetbrains.plugins.meteor.tsStubs.MeteorStubPath

internal class MeteorProjectStartupActivity : ProjectActivity, DumbAware {
  companion object {
    const val METEOR_FOLDER: String = ".meteor"
    const val METEOR_LOCAL_FOLDER: String = "local"
    const val METEOR_RELATIVE_PATH_TO_LOCAL_FOLDER: String = "$METEOR_FOLDER/$METEOR_LOCAL_FOLDER"
    const val METEOR_LIBRARY_NAME: String = "Meteor project library"
    const val JS_METEOR_LIBRARY_WAS_ENABLED: String = "js.meteor.library.was.enabled"
    const val METEOR_FOLDERS_CACHED: String = "js.meteor.library.cached"
    const val METEOR_PROJECT_KEY: String = "js.meteor.project"
  }

  init {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      throw ExtensionNotApplicableException.create()
    }
  }

  override suspend fun execute(project: Project) {
    val updater = project.serviceAsync<MeteorLibraryUpdater>()
    updater.scheduleProjectUpdate()
    readAction {
      if (isMeteorProject(project)) {
        updater.update()
      }
    }
  }
}

internal fun initMeteorProject(project: Project, shouldUpdateFileTypes: Boolean) {
  // refresh must be run out of read action
  attachPredefinedMeteorLibrary(project)

  ReadAction.run<RuntimeException> {
    if (MeteorSettings.getInstance().isExcludeMeteorLocalFolder) {
      excludeLocalMeteorFolders(project)
    }
    if (setDefaultForShouldOpenHtmlAsHandlebars(project) || shouldUpdateFileTypes) {
      updateFileTypesAfterChanges()
      MeteorLibraryUpdater.getInstance(project).update()
    }
  }
}

private fun updateFileTypesAfterChanges() {
  val app = ApplicationManager.getApplication()
  app.invokeLaterOnWriteThread {
    app.runWriteAction {
      FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Meteor settings changed",
                                                            EmptyRunnable.getInstance())
    }
  }
}

private fun isMeteorProject(project: Project): Boolean {
  return MeteorFacade.getInstance().isMeteorProject(project)
}

private fun setDefaultForShouldOpenHtmlAsHandlebars(project: Project): Boolean {
  //change default value to true
  if (StringUtil.isEmpty(HbConfig.getRawOpenHtmlAsHandlebarsValue(project))) {
    HbConfig.setShouldOpenHtmlAsHandlebars(true, project)
    return true
  }
  return false
}

private fun attachPredefinedMeteorLibrary(project: Project) {
  if (!isMeteorLibraryWasEnabled(project)) {
    val runnable = Runnable {
      val libraryManager = JSLibraryManager.getInstance(project)
      //reload predefined libraries
      libraryManager.commitChanges(RootsChangeRescanningInfo.RESCAN_DEPENDENCIES_IF_NEEDED)
      libraryManager.libraryMappings.associate(null, METEOR_LIBRARY_NAME, true)
      setMeteorLibraryWasEnabled(project)
      updateLibrariesFiles(project)
    }
    if (ApplicationManager.getApplication().isWriteIntentLockAcquired) {
      ApplicationManager.getApplication().runWriteAction(runnable)
    }
    else {
      ApplicationManager.getApplication().invokeLaterOnWriteThread {
        ApplicationManager.getApplication().runWriteAction(runnable)
      }
    }
  }
  else {
    updateLibrariesFiles(project)
  }
}

private fun updateLibrariesFiles(project: Project) {
  if (project.isDisposed) return
  val libraryManager = JSLibraryManager.getInstance(project)
  val isAsync = true

  MeteorStubPath.getStubDir().refresh(isAsync, true) {
    if (project.isDisposed) return@refresh
    val model = libraryManager.getLibraryByName(METEOR_LIBRARY_NAME)
    val meteorLib = MeteorStubPath.getLastMeteorLib()
    if (model != null && !model.containsFile(meteorLib)) {
      ApplicationManager.getApplication().runWriteAction {
        MeteorJSPredefinedLibraryProvider.resetFile()
        libraryManager.updateLibrary(METEOR_LIBRARY_NAME,
                                     METEOR_LIBRARY_NAME,
                                     arrayOf(meteorLib),
                                     VirtualFile.EMPTY_ARRAY,
                                     ArrayUtilRt.EMPTY_STRING_ARRAY)
        libraryManager.commitChanges(RootsChangeRescanningInfo.RESCAN_DEPENDENCIES_IF_NEEDED)
      }
    }
  }
}

@RequiresReadLock
private fun excludeLocalMeteorFolders(project: Project) {
  for (meteorFolder in MeteorFacade.getInstance().getMeteorFolders(project)) {
    val module = ModuleUtilCore.findModuleForFile(meteorFolder, project)
    if (module == null) {
      continue
    }
    val root = meteorFolder.parent
    val contentRoot = getContentRoot(module, root)
    if (contentRoot == null) continue

    val oldExcludedFolders = getOldExcludedFolders(module, root)

    if (oldExcludedFolders.size == 1 && oldExcludedFolders.contains(meteorFolder.url + "/" + METEOR_LOCAL_FOLDER)) continue
    ApplicationManager.getApplication().executeOnPooledThread {
      ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedFolders,
                                                       ContainerUtil.newHashSet(meteorFolder.url + "/" + METEOR_LOCAL_FOLDER))
    }
  }
}

private fun getContentRoot(module: Module, root: VirtualFile?): VirtualFile? {
  return if (root == null) null else ProjectRootManager.getInstance(module.project).fileIndex.getContentRootForFile(root)
}

private fun getOldExcludedFolders(module: Module, root: VirtualFile): Collection<String> {
  return ModuleRootManager.getInstance(module).excludeRootUrls.filter { url: String ->
    url.startsWith(root.url + "/" + METEOR_RELATIVE_PATH_TO_LOCAL_FOLDER)
  }
}

private fun isMeteorLibraryWasEnabled(project: Project): Boolean {
  return PropertiesComponent.getInstance(project).getBoolean(JS_METEOR_LIBRARY_WAS_ENABLED)
}

private fun setMeteorLibraryWasEnabled(project: Project) {
  PropertiesComponent.getInstance(project).setValue(JS_METEOR_LIBRARY_WAS_ENABLED, true)
}
