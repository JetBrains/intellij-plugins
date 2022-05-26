package com.jetbrains.plugins.meteor;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import com.jetbrains.plugins.meteor.tsStubs.MeteorStubPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class MeteorProjectStartupActivity implements StartupActivity.Background {
  public static final String METEOR_FOLDER = ".meteor";
  public static final String METEOR_LOCAL_FOLDER = "local";
  public static final String METEOR_RELATIVE_PATH_TO_LOCAL_FOLDER = METEOR_FOLDER + "/" + METEOR_LOCAL_FOLDER;
  public static final String METEOR_LIBRARY_NAME = "Meteor project library";
  public static final String JS_METEOR_LIBRARY_WAS_ENABLED = "js.meteor.library.was.enabled";
  public static final String METEOR_FOLDERS_CACHED = "js.meteor.library.cached";
  public static final String METEOR_PROJECT_KEY = "js.meteor.project";

  @NotNull
  private static MeteorLibraryUpdater getPackageUpdater(@NotNull Project project) {
    return MeteorLibraryUpdater.get(project);
  }

  @Override
  public void runActivity(@NotNull Project project) {
    ReadAction.run(() -> {
      if (project.isDisposed()) return;
      MeteorLibraryUpdater updater = getPackageUpdater(project);
      updater.scheduleProjectUpdate();
      if (isMeteorProject(project)) {
        updater.update();
      }
    });
  }

  @RequiresReadLock
  public static void initMeteorProject(@NotNull Project project, boolean shouldUpdateFileTypes) {
    attachPredefinedMeteorLibrary(project);
    shouldUpdateFileTypes = setDefaultForShouldOpenHtmlAsHandlebars(project) || shouldUpdateFileTypes;

    if (MeteorSettings.getInstance().isExcludeMeteorLocalFolder()) {
      excludeLocalMeteorFolders(project);
    }

    if (shouldUpdateFileTypes) {
      updateFileTypesAfterChanges();
      MeteorLibraryUpdater.get(project).update();
    }
  }

  private static void updateFileTypesAfterChanges() {
    Application app = ApplicationManager.getApplication();
    app.invokeLaterOnWriteThread(() -> {
      app.runWriteAction(() -> {
        FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Meteor settings changed", EmptyRunnable.getInstance());
      });
    });
  }

  private static boolean isMeteorProject(Project project) {
    return MeteorFacade.getInstance().isMeteorProject(project);
  }

  public static boolean setDefaultForShouldOpenHtmlAsHandlebars(@NotNull Project project) {
    //change default value to true
    if (StringUtil.isEmpty(HbConfig.getRawOpenHtmlAsHandlebarsValue(project))) {
      HbConfig.setShouldOpenHtmlAsHandlebars(true, project);
      return true;
    }
    return false;
  }

  @RequiresReadLock
  private static void attachPredefinedMeteorLibrary(@NotNull Project project) {
    if (!isMeteorLibraryWasEnabled(project)) {
      Runnable runnable = () -> {
        JSLibraryManager libraryManager = JSLibraryManager.getInstance(project);
        //reload predefined libraries
        libraryManager.commitChanges();
        libraryManager.getLibraryMappings().associate(null, METEOR_LIBRARY_NAME, true);
        setMeteorLibraryWasEnabled(project);
        updateLibrariesFiles(project);
      };
      if (ApplicationManager.getApplication().isWriteThread()) {
        ApplicationManager.getApplication().runWriteAction(runnable);
      }
      else {
        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
          ApplicationManager.getApplication().runWriteAction(runnable);
        });
      }
    }
    else {
      updateLibrariesFiles(project);
    }
  }

  private static void updateLibrariesFiles(@NotNull Project project) {
    if (project.isDisposed()) return;
    final JSLibraryManager libraryManager = JSLibraryManager.getInstance(project);
    boolean isAsync = !ApplicationManager.getApplication().isUnitTestMode();

    MeteorStubPath.getStubDir().refresh(isAsync, true, () -> {
      if (project.isDisposed()) return;

      final ScriptingLibraryModel model = libraryManager.getLibraryByName(METEOR_LIBRARY_NAME);
      final VirtualFile meteorLib = MeteorStubPath.getLastMeteorLib();
      if (model != null && !model.containsFile(meteorLib)) {
        ApplicationManager.getApplication().runWriteAction(() -> {
          MeteorJSPredefinedLibraryProvider.resetFile();
          libraryManager.updateLibrary(METEOR_LIBRARY_NAME, METEOR_LIBRARY_NAME,
                                       new VirtualFile[]{meteorLib},
                                       VirtualFile.EMPTY_ARRAY,
                                       ArrayUtilRt.EMPTY_STRING_ARRAY);

          libraryManager.commitChanges();
        });
      }
    });
  }

  @RequiresReadLock
  private static void excludeLocalMeteorFolders(@NotNull Project project) {
    for (VirtualFile meteorFolder : MeteorFacade.getInstance().getMeteorFolders(project)) {
      Module module = ModuleUtilCore.findModuleForFile(meteorFolder, project);
      if (module == null) {
        continue;
      }
      VirtualFile root = meteorFolder.getParent();
      VirtualFile contentRoot = getContentRoot(module, root);
      if (contentRoot == null) continue;

      Collection<String> oldExcludedFolders = getOldExcludedFolders(module, root);

      if (oldExcludedFolders.size() == 1 && oldExcludedFolders.contains(meteorFolder.getUrl() + "/" + METEOR_LOCAL_FOLDER)) continue;
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedFolders,
                                                         ContainerUtil.newHashSet(meteorFolder.getUrl() + "/" + METEOR_LOCAL_FOLDER));
      });
    }
  }

  private static VirtualFile getContentRoot(@NotNull Module module, @Nullable VirtualFile root) {
    return root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
  }

  private static Collection<String> getOldExcludedFolders(@NotNull Module module, @NotNull final VirtualFile root) {
    return ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(),
                                url -> url.startsWith(root.getUrl() + "/" + METEOR_RELATIVE_PATH_TO_LOCAL_FOLDER));
  }

  private static boolean isMeteorLibraryWasEnabled(@NotNull Project project) {
    return PropertiesComponent.getInstance(project).getBoolean(JS_METEOR_LIBRARY_WAS_ENABLED);
  }

  private static void setMeteorLibraryWasEnabled(@NotNull Project project) {
    PropertiesComponent.getInstance(project).setValue(JS_METEOR_LIBRARY_WAS_ENABLED, true);
  }
}
