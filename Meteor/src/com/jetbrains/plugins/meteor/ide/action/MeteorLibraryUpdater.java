package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.SingleAlarm;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class MeteorLibraryUpdater implements Disposable {
  private static final Logger LOG = Logger.getInstance(MeteorLibraryUpdater.class);

  public static MeteorLibraryUpdater get(Project project) {
    return project.getService(MeteorLibraryUpdater.class);
  }

  @NotNull
  private final Project myProject;
  @NotNull
  private final MergingUpdateQueue myQueue;
  @NotNull
  private final SingleAlarm myProjectStatusAlarm;

  public MeteorLibraryUpdater(@NotNull Project project) {
    LOG.assertTrue(!project.isDefault());
    myProject = project;
    myQueue = new MergingUpdateQueue("Meteor update packages", 300, true, null, this, null, false);
    myProjectStatusAlarm = new SingleAlarm(this::findAndInitMeteorRootsWhenSmart, 1000, this, Alarm.ThreadToUse.POOLED_THREAD);
  }

  public void update() {
    if (ApplicationManager.getApplication().isUnitTestMode() || !MeteorSettings.getInstance().isAutoImport()) {
      return;
    }

    myQueue.queue(new Update(this) {
      @Override
      public void run() {
        DumbService.getInstance(myProject).runReadActionInSmartMode(() -> {
          LOG.debug("Check meteor libraries");
          if (updateStoredMeteorFolders()) {
            refreshLibraries(myProject, true);
            return;
          }

          updateLibraryIfRequired(myProject);
        });
      }
    });
  }

  private boolean updateStoredMeteorFolders() {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    Collection<VirtualFile> stored = MeteorFacade.getInstance().getStoredMeteorFolders(myProject);
    Collection<VirtualFile> folders = MeteorFacade.getInstance().getMeteorFolders(myProject);
    if (stored.equals(folders)) {
      return false;
    }

    MeteorFacade.getInstance().storeMeteorFolders(myProject, ContainerUtil.map(folders, el -> el.getPath()));

    return true;
  }

  public static void updateLibraryIfRequired(@NotNull Project project) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    final Collection<MeteorImportPackagesAsExternalLibAction.CodeType> codes = MeteorPackagesUtil.getCodes(project);

    final String pathToMeteorGlobal = MeteorPackagesUtil.getPathToGlobalMeteorRoot(project);
    if (StringUtil.isEmpty(pathToMeteorGlobal)) return;

    final VirtualFile dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(project, null);
    if (dotMeteorVirtualFile == null) {
      LOG.debug("Cannot find .meteor folder");
      return;
    }

    if (codes.isEmpty()) return;

    Collection<VirtualFile> roots = MeteorSyntheticLibraryProvider.getRoots(project);
    ProjectFileIndex index = ProjectFileIndex.getInstance(project);
    boolean needToUpdateLibrary = roots.stream().anyMatch(el -> !index.isInLibrary(el));
    if (needToUpdateLibrary) {
      refreshLibraries(project, false);
    }
  }

  public static void refreshLibraries(@NotNull Project project, boolean removeDeprecated) {
    ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> {
      JSLibraryManager libraryManager = JSLibraryManager.getInstance(project);
      if (removeDeprecated) {
        removeDeprecatedLibraries(libraryManager);
      }

      libraryManager.commitChanges();
    }), project.getDisposed());
  }

  public void scheduleProjectUpdate() {
    Application app = ApplicationManager.getApplication();
    if (app.isUnitTestMode()) {
      app.invokeLater(this::findAndInitMeteorRootsWhenSmart, myProject.getDisposed());
    }
    else {
      myProjectStatusAlarm.cancelAndRequest();
    }
  }

  @RequiresBackgroundThread
  private void findAndInitMeteorRootsWhenSmart() {
    DumbService.getInstance(myProject).runReadActionInSmartMode(() -> findAndInitMeteorRoots(myProject));
  }

  private static void removeDeprecatedLibraries(@NotNull JSLibraryManager libraryManager) {
    for (MeteorImportPackagesAsExternalLibAction.CodeType value : MeteorImportPackagesAsExternalLibAction.CodeType.values()) {
      String name = MeteorImportPackagesAsExternalLibAction.getLibraryName(value);
      ScriptingLibraryModel model = libraryManager.getLibraryByName(name);
      if (model != null) {
        libraryManager.removeLibrary(model);
      }
    }
  }

  @RequiresReadLock
  @RequiresBackgroundThread
  public static void findAndInitMeteorRoots(@NotNull Project project) {
    if (project.isDisposed()) {
      return;
    }

    boolean shouldUpdateFileTypes;
    MeteorFacade meteorFacade = MeteorFacade.getInstance();
    if (!meteorFacade.isMeteorProject(project) &&
        (meteorFacade.hasMeteorFolders(project) || projectHasExcludedMeteorFolder(project))) {
      meteorFacade.setIsMeteorProject(project);
      shouldUpdateFileTypes = true;
    }
    else {
      shouldUpdateFileTypes = false;
    }

    if (!meteorFacade.isMeteorProject(project)) {
      return;
    }

    MeteorProjectStartupActivity.initMeteorProject(project, shouldUpdateFileTypes);
  }

  /**
   * Some users want to exclude and hide '.meteor' folder from the project.
   * So we have to implement a logic for checking that the folder '.meteor' was excluded from the project
   * <p/>
   * return true if excluded '.meteor' folder was detected
   */
  public static boolean projectHasExcludedMeteorFolder(@NotNull Project project) {
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      for (String url : ModuleRootManager.getInstance(module).getExcludeRootUrls()) {
        String trimEnd = StringUtil.trimEnd(url, "/");
        if (trimEnd.endsWith("/" + MeteorProjectStartupActivity.METEOR_FOLDER) && !JSLibraryUtil.isProbableLibraryPath(trimEnd)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void dispose() {
    myQueue.cancelAllUpdates();
  }
}
