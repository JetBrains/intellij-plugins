// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector.PhoneGapExecutableChecker;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil.*;
import static com.intellij.openapi.roots.ModuleRootModificationUtil.updateExcludedFolders;

public final class PhoneGapStartupActivity implements StartupActivity, DumbAware {
  public static final String EXCLUDED_WWW_DIRECTORY = "excluded.www.directory";

  @Override
  public void runActivity(@NotNull Project project) {
    ReadAction.run(() -> {
      if (isPhoneGapProject(project)) {
        if (PhoneGapSettings.getInstance().isExcludePlatformFolder()) {
          ApplicationManager.getApplication().invokeLater(() -> excludeWorkingDirectories(project), project.getDisposed());
        }
        PhoneGapExecutableChecker.check(project);
      }
    });
  }

  public static boolean shouldExcludeDirectory(@NotNull VFileEvent event) {
    String path = event.getPath();
    if (path.endsWith("/" + FOLDER_PLATFORMS)) return true;
    if (!path.endsWith("/" + FOLDER_WWW)) return false;

    VirtualFile candidateParent = getEventParent(event);
    if (candidateParent == null || !candidateParent.isValid()) return false;

    return isIonic2WwwDirectory(candidateParent);
  }

  @Nullable
  public static VirtualFile getEventParent(@NotNull VFileEvent event) {
    VirtualFile candidateParent;
    if (event instanceof VFileCreateEvent) {
      candidateParent = ((VFileCreateEvent)event).getParent();
    }
    else {
      VirtualFile file = event.getFile();
      candidateParent = file != null ? file.getParent() : null;
    }
    return candidateParent;
  }

  private static boolean shouldExcludeDirectory(@NotNull String name, @NotNull VirtualFile file) {
    if (!file.isDirectory()) return false;
    if (name.equals(FOLDER_PLATFORMS)) return true;

    if (!name.equals(FOLDER_WWW)) return false;
    VirtualFile candidateParent = file.getParent();
    if (candidateParent == null || !candidateParent.isValid()) return false;

    return isIonic2WwwDirectory(candidateParent);
  }

  public static boolean isIonic2WwwDirectory(@NotNull VirtualFile parent) {
    return parent.findChild(IONIC_CONFIG) != null && parent.findChild("tsconfig.json") != null;
  }

  @NotNull
  public static Set<String> getExcludedFolderNames(@NotNull VFileEvent event) {
    return Collections.singleton(VirtualFileManager.constructUrl(event.getFileSystem().getProtocol(), event.getPath()));
  }

  @Nullable
  public static Runnable getUpdateModuleExcludeByFSEventRunnable(@NotNull Project project,
                                                                  @NotNull VirtualFile parent,
                                                                  @NotNull Set<String> oldToUpdateFolders,
                                                                  @NotNull Set<String> newToUpdateFolders) {
    Module module = ModuleUtilCore.findModuleForFile(parent, project);
    if (module == null) {
      return null;
    }
    
    VirtualFile contentRoot = getContentRoot(module, parent);
    if (contentRoot == null) {
      return null;
    }
    return () -> updateExcludedFolders(module, contentRoot, oldToUpdateFolders, newToUpdateFolders);
  }

  private static void excludeWorkingDirectories(@NotNull Project project) {
    final Collection<VirtualFile> platformsDirectories = FilenameIndex.getVirtualFilesByName(FOLDER_PLATFORMS,
                                                                                             GlobalSearchScope.projectScope(project));

    for (VirtualFile directory : platformsDirectories) {
      excludeFolder(project, directory);
    }

    final Collection<VirtualFile> wwwDirectories = FilenameIndex.getVirtualFilesByName(FOLDER_WWW,
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
