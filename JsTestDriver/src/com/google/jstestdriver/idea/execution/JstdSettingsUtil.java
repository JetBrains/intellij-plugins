package com.google.jstestdriver.idea.execution;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.config.JstdConfigFileType;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JstdSettingsUtil {

  private static final Key<Boolean> JSTD_CONFIG_FILES_IN_PROJECT = new Key<>("JSTD_CONFIG_FILES_IN_PROJECT");

  private JstdSettingsUtil() {}

  @NotNull
  public static List<VirtualFile> collectJstdConfigs(@NotNull Project project, @NotNull JstdRunSettings runSettings) {
    TestType testType = runSettings.getTestType();
    List<VirtualFile> res = Collections.emptyList();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      VirtualFile virtualFile = VfsUtil.findFileByIoFile(new File(runSettings.getDirectory()), true);
      if (virtualFile != null) {
        res = collectJstdConfigFilesInDirectory(project, virtualFile);
      }
    } else {
      File configFile = new File(runSettings.getConfigFile());
      VirtualFile configVirtualFile = VfsUtil.findFileByIoFile(configFile, false);
      if (configVirtualFile != null) {
        res = Collections.singletonList(configVirtualFile);
      }
    }
    return res;
  }

  @NotNull
  public static List<VirtualFile> collectJstdConfigFilesInDirectory(@NotNull Project project, @NotNull VirtualFile directory) {
    GlobalSearchScope directorySearchScope = buildDirectorySearchScope(project, directory);
    if (directorySearchScope == null) {
      return Collections.emptyList();
    }
    Collection<VirtualFile> configs = FileTypeIndex.getFiles(JstdConfigFileType.INSTANCE, directorySearchScope);
    return Lists.newArrayList(configs);
  }

  public static boolean areJstdConfigFilesInProjectCached(@NotNull Project project) {
    Boolean value = project.getUserData(JSTD_CONFIG_FILES_IN_PROJECT);
    if (value != null) {
      return value;
    }
    value = areJstdConfigFilesInScope(GlobalSearchScope.projectScope(project));
    if (value == null) {
      return false;
    }
    project.putUserData(JSTD_CONFIG_FILES_IN_PROJECT, value);
    return value;
  }

  public static boolean areJstdConfigFilesInProject(@NotNull Project project) {
    GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);
    Boolean found = areJstdConfigFilesInScope(projectScope);
    return found != null ? found : false;
  }

  public static boolean areJstdConfigFilesInDirectory(@NotNull Project project, @NotNull VirtualFile directory) {
    GlobalSearchScope directorySearchScope = buildDirectorySearchScope(project, directory);
    if (directorySearchScope == null) {
      return false;
    }
    Boolean found = areJstdConfigFilesInScope(directorySearchScope);
    return found != null ? found : false;
  }

  @Nullable
  private static Boolean areJstdConfigFilesInScope(GlobalSearchScope scope) {
    try {
      Ref<Boolean> foundRef = Ref.create(false);
      FileTypeIndex.processFiles(JstdConfigFileType.INSTANCE, file -> {
        if (JSLibraryUtil.isProbableLibraryFile(file)) {
          return true;
        }
        foundRef.set(true);
        return false;
      }, scope);
      return foundRef.get();
    }
    catch (IndexNotReadyException e) {
      return null;
    }
  }

  @Nullable
  private static GlobalSearchScope buildDirectorySearchScope(@NotNull Project project, @NotNull VirtualFile directory) {
    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(directory);
    if (module == null) {
      return null;
    }
    GlobalSearchScope directorySearchScope = GlobalSearchScopesCore.directoryScope(project, directory, true);
    return module.getModuleContentWithDependenciesScope().intersectWith(directorySearchScope);
  }

}
