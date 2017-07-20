package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector.PhoneGapExecutableChecker;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil.*;
import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;

public class PhoneGapStartupActivity implements StartupActivity {


  public static final String EXCLUDED_WWW_DIRECTORY = "excluded.www.directory";

  @Override
  public void runActivity(@NotNull Project project) {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
      if (isPhoneGapProject(project)) {
        if (PhoneGapSettings.getInstance().isExcludePlatformFolder()) {
          excludeWorkingDirectories(project);
        }
        PhoneGapExecutableChecker.check(project);
      }
    });

    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        if (!isProcess(event)) {
          return;
        }

        updateModuleExcludeByFSEvent(project, event, ContainerUtil.newHashSet(), ContainerUtil.newHashSet(getExcludedFolderNames(event)));
      }

      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
        if (!isProcess(event)) {
          return;
        }

        updateModuleExcludeByFSEvent(project, event, getExcludedFolderNames(event), ContainerUtil.newHashSet());
      }

      private boolean isProcess(@NotNull VirtualFileEvent event) {
        return shouldExcludeDirectory(event) &&
               isPhoneGapProject(project) &&
               PhoneGapSettings.getInstance().isExcludePlatformFolder();
      }
    }, project);
  }

  private static boolean shouldExcludeDirectory(@NotNull VirtualFileEvent event) {
    String name = event.getFileName();
    VirtualFile file = event.getFile();

    return shouldExcludeDirectory(name, file);
  }

  private static boolean shouldExcludeDirectory(@NotNull String name, @NotNull VirtualFile file) {
    if (!file.isDirectory()) return false;
    if (name.equals(FOLDER_PLATFORMS)) return true;

    if (!name.equals(FOLDER_WWW)) return false;
    VirtualFile candidateParent = file.getParent();
    if (candidateParent == null || !candidateParent.isValid()) return false;

    return isIonic2WwwDirectory(file, candidateParent);
  }

  public static boolean isIonic2WwwDirectory(@NotNull VirtualFile directory, @NotNull VirtualFile parent) {
    return parent.findChild(IONIC_CONFIG) != null && parent.findChild("tsconfig.json") != null;
  }


  private static Set<String> getExcludedFolderNames(@NotNull VirtualFileEvent event) {
    return ContainerUtil.newHashSet(event.getFile().getUrl());
  }

  private static void updateModuleExcludeByFSEvent(@NotNull Project project,
                                                   @NotNull VirtualFileEvent event,
                                                   @NotNull Set<String> oldToUpdateFolders,
                                                   @NotNull Set<String> newToUpdateFolders) {
    VirtualFile eventFile = event.getFile();
    Module module = ModuleUtilCore.findModuleForFile(eventFile, project);
    if (module == null) {
      return;
    }
    VirtualFile directory = event.getParent();
    VirtualFile contentRoot = getContentRoot(module, directory);
    if (contentRoot == null) {
      return;
    }
    updateExcludedFolders(module, contentRoot, oldToUpdateFolders, newToUpdateFolders);
  }

  private static void excludeWorkingDirectories(@NotNull Project project) {
    final Collection<VirtualFile> platformsDirectories = FilenameIndex.getVirtualFilesByName(project,
                                                                                             FOLDER_PLATFORMS,
                                                                                             GlobalSearchScope.projectScope(project));

    for (VirtualFile directory : platformsDirectories) {
      excludeFolder(project, directory);
    }

    final Collection<VirtualFile> wwwDirectories = FilenameIndex.getVirtualFilesByName(project,
                                                                                       FOLDER_WWW,
                                                                                       GlobalSearchScope.projectScope(project));

    for (VirtualFile directory : wwwDirectories) {
      PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
      if (!propertiesComponent.getBoolean(EXCLUDED_WWW_DIRECTORY, false)) {
        if (shouldExcludeDirectory(directory.getName(), directory)) {
          propertiesComponent.setValue(EXCLUDED_WWW_DIRECTORY, true);
          excludeFolder(project, directory);
        }
      }
    }
  }

  public static void excludeFolder(@NotNull Project project, @NotNull VirtualFile directoryToExclude) {
    Module module = ModuleUtilCore.findModuleForFile(directoryToExclude, project);
    if (module == null) {
      return;
    }
    VirtualFile contentRoot = getContentRoot(module, directoryToExclude);
    if (contentRoot == null) return;

    Collection<String> oldExcludedFolders = getOldExcludedFolders(module, directoryToExclude);

    if (oldExcludedFolders.size() == 1 && oldExcludedFolders.contains(directoryToExclude.getUrl())) return;
    updateExcludedFolders(module, contentRoot, oldExcludedFolders, ContainerUtil.newHashSet(directoryToExclude.getUrl()));
  }

  private static Collection<String> getOldExcludedFolders(@NotNull Module module, @NotNull final VirtualFile root) {
    return ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), url -> url.startsWith(root.getUrl()));
  }

  private static VirtualFile getContentRoot(@NotNull Module module, @Nullable VirtualFile root) {
    return root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
  }
}
