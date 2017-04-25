package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector.PhoneGapExecutableChecker;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil.FOLDER_PLATFORMS;
import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;

public class PhoneGapProjectComponent extends AbstractProjectComponent {


  protected PhoneGapProjectComponent(Project project) {
    super(project);
  }

  @Override
  public void projectOpened() {
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(() -> {
      if (PhoneGapUtil.isPhoneGapProject(myProject)) {
        if (PhoneGapSettings.getInstance().isExcludePlatformFolder()) {
          excludePlatformFolders();
        }
        PhoneGapExecutableChecker.check(myProject);
      }
    });

    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        if (!isProcess(event)) {
          return;
        }

        updateModuleExcludeByFSEvent(event, ContainerUtil.newHashSet(), ContainerUtil.newHashSet(getExcludedFolderNames(event)));
      }

      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
        if (!isProcess(event)) {
          return;
        }

        updateModuleExcludeByFSEvent(event, getExcludedFolderNames(event), ContainerUtil.newHashSet());
      }

      private boolean isProcess(@NotNull VirtualFileEvent event) {
        return event.getFileName().equals(FOLDER_PLATFORMS) &&
               event.getFile().isDirectory() &&
               PhoneGapUtil.isPhoneGapProject(myProject) &&
               PhoneGapSettings.getInstance().isExcludePlatformFolder();
      }
    }, myProject);
  }

  private static Set<String> getExcludedFolderNames(VirtualFileEvent event) {
    return ContainerUtil.newHashSet(event.getFile().getUrl());
  }

  private void updateModuleExcludeByFSEvent(VirtualFileEvent event,
                                            Set<String> oldToUpdateFolders,
                                            Set<String> newToUpdateFolders) {
    VirtualFile eventFile = event.getFile();
    Module module = ModuleUtilCore.findModuleForFile(eventFile, myProject);
    if (module == null) {
      return;
    }
    VirtualFile contentRoot = getContentRoot(module, event.getParent());
    if (contentRoot == null) {
      return;
    }
    updateExcludedFolders(module, contentRoot, oldToUpdateFolders, newToUpdateFolders);
  }

  private void excludePlatformFolders() {
    final Collection<VirtualFile> platformsFolders = FilenameIndex.getVirtualFilesByName(myProject,
                                                                                         FOLDER_PLATFORMS,
                                                                                         GlobalSearchScope.projectScope(myProject));

    for (VirtualFile platformFolder : platformsFolders) {
      excludePlatformFolder(myProject, platformFolder);
    }
  }

  public static void excludePlatformFolder(Project project, VirtualFile platformFolder) {
    Module module = ModuleUtilCore.findModuleForFile(platformFolder, project);
    if (module == null) {
      return;
    }
    VirtualFile contentRoot = getContentRoot(module, platformFolder);
    if (contentRoot == null) return;

    Collection<String> oldExcludedFolders = getOldExcludedFolders(module, platformFolder);

    if (oldExcludedFolders.size() == 1 && oldExcludedFolders.contains(platformFolder.getUrl())) return;
    updateExcludedFolders(module, contentRoot, oldExcludedFolders, ContainerUtil.newHashSet(platformFolder.getUrl()));
  }

  private static Collection<String> getOldExcludedFolders(Module module, final VirtualFile root) {
    return ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), url -> url.startsWith(root.getUrl()));
  }

  private static VirtualFile getContentRoot(Module module, VirtualFile root) {
    return root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
  }
}
