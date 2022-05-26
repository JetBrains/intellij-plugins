package com.jetbrains.plugins.meteor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;
import com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;
import static com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.METEOR_FOLDER;
import static com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.METEOR_LOCAL_FOLDER;

/**
 * Excludes ".meteor" directory and schedules the auto-update for meteor libraries
 */
public final class MeteorAsyncFileListener implements AsyncFileListener {

  @NotNull
  private static MeteorLibraryUpdater getPackageUpdater(@NotNull Project project) {
    return MeteorLibraryUpdater.get(project);
  }

  @Nullable
  public static VirtualFile getContentRoot(@NotNull Module module, @Nullable VirtualFile root) {
    return root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
  }

  @Nullable
  @Override
  public ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
    List<MeteorProjectApplier> result = null;

    for (VFileEvent event : events) {
      ProgressManager.checkCanceled();
      MeteorProjectApplier applier = null;
      if (event instanceof VFileCreateEvent) {
        applier = processCreate((VFileCreateEvent)event);
      }
      else if (event instanceof VFileContentChangeEvent) {
        applier = processChange((VFileContentChangeEvent)event);
      }
      else if (event instanceof VFileDeleteEvent) {
        applier = processDelete((VFileDeleteEvent)event);
      }
      if (applier != null) {
        if (result == null) result = new SmartList<>();
        result.add(applier);
      }
    }

    return result == null || result.size() == 0 ? null : new MeteorCompositeChangeApplier(result);
  }

  @Nullable
  private static MeteorProjectApplier processCreate(@NotNull VFileCreateEvent event) {
    String name = event.getChildName();
    VirtualFile parent = event.getParent();
    if (name.equals(MeteorPackagesUtil.VERSIONS_FILE_NAME)) {
      return new UpdatePackages(parent);
    }

    if (!name.equals(METEOR_FOLDER) || !MeteorSettings.getInstance().isExcludeMeteorLocalFolder()) {
      return null;
    }

    String url = getExcludedFolderName(parent.getUrl(), false);
    return new MeteorExcludeApplier(url, true, parent);
  }

  @Nullable
  private static MeteorProjectApplier processChange(@NotNull VFileContentChangeEvent event) {
    VirtualFile file = event.getFile();
    if (file.getName().equals(MeteorPackagesUtil.VERSIONS_FILE_NAME)) {
      return new UpdatePackages(file);
    }

    return null;
  }

  @Nullable
  private static MeteorProjectApplier processDelete(@NotNull VFileDeleteEvent delete) {
    VirtualFile file = delete.getFile();
    String name = file.getName();
    if (!name.equals(METEOR_FOLDER) || !MeteorSettings.getInstance().isExcludeMeteorLocalFolder()) {
      return null;
    }
    String url = getExcludedFolderName(file.getUrl(), true);

    return new MeteorExcludeApplier(url, false, file.getParent());
  }

  private final static class UpdatePackages implements MeteorProjectApplier {
    @NotNull
    private final VirtualFile myEventFile;

    private UpdatePackages(@NotNull VirtualFile eventFile) {
      myEventFile = eventFile;
    }

    @Override
    public VirtualFile getVirtualFile() {
      return myEventFile;
    }

    @Override
    public void afterVfsChange(@NotNull Project project) {
      if (!MeteorFacade.getInstance().isMeteorProject(project)) return;
      
      getPackageUpdater(project).update();
    }
  }

  private final static class MeteorExcludeApplier implements MeteorProjectApplier {

    private final boolean myExclude;

    @NotNull
    private final String myUrl;
    @NotNull
    private final VirtualFile myContext;

    private MeteorExcludeApplier(@NotNull String url, boolean toExclude, @NotNull VirtualFile context) {
      myUrl = url;
      myExclude = toExclude;
      myContext = context;
    }

    @Override
    public VirtualFile getVirtualFile() {
      return myContext;
    }

    @Override
    public void afterVfsChange(@NotNull Project project) {
      Set<String> exclude = myExclude ? ContainerUtil.newHashSet(myUrl) : Collections.emptySet();
      Set<String> unExclude = myExclude ? Collections.emptySet() : ContainerUtil.newHashSet(myUrl);
      updateModuleExcludeByFSEvent(project, myContext, unExclude, exclude);
    }
  }

  private static String getExcludedFolderName(@NotNull String url, boolean hasMeteorFolderInUrl) {
    return url + (hasMeteorFolderInUrl ? "" : "/" + METEOR_FOLDER) + "/" + METEOR_LOCAL_FOLDER;
  }


  private final static class MeteorCompositeChangeApplier implements ChangeApplier {
    @NotNull
    private final List<MeteorProjectApplier> myEvents;

    private MeteorCompositeChangeApplier(@NotNull List<MeteorProjectApplier> events) {
      myEvents = events;
    }

    @Override
    public void afterVfsChange() {
      Project[] projects = ProjectManager.getInstance().getOpenProjects();
      if (projects.length == 0) return;

      for (Project project : projects) {
        ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
        for (MeteorProjectApplier event : myEvents) {
          VirtualFile file = event.getVirtualFile();
          if (file == null || !file.isValid()) continue;
          if (index.isInContent(file)) {
            event.afterVfsChange(project);
          }
        }
      }
    }
  }

  public static void updateModuleExcludeByFSEvent(@NotNull Project project,
                                                  @NotNull VirtualFile context,
                                                  @NotNull Set<String> urlsToUnExclude,
                                                  @NotNull Set<String> urlsToExclude) {
    Module module = ModuleUtilCore.findModuleForFile(context, project);
    if (module == null) {
      return;
    }
    VirtualFile contentRoot = getContentRoot(module, context);
    if (contentRoot == null) {
      return;
    }
    updateExcludedFolders(module, contentRoot, urlsToUnExclude, urlsToExclude);
  }


  private interface MeteorProjectApplier {

    @Nullable
    VirtualFile getVirtualFile();
    default void afterVfsChange(@NotNull Project project) {}
  }
}
