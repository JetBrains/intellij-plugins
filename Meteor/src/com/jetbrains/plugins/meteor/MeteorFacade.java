package com.jetbrains.plugins.meteor;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class MeteorFacade {
  private static final MeteorFacade instance = new MeteorFacade();

  public static MeteorFacade getInstance() {
    return instance;
  }

  public boolean isMeteorFolder(final String path) {
    final String normalizedPath = FileUtil.toCanonicalPath(path);

    File file = new File(normalizedPath);
    if (!file.exists() || !file.isDirectory()) {
      return false;
    }

    String[] list = file.list();
    if (list == null) return false;

    int i = ArrayUtil.find(list, MeteorProjectStartupActivity.METEOR_FOLDER);
    return i != -1;
  }

  public @NotNull Collection<VirtualFile> getMeteorFolders(Project project) {
    return ContainerUtil.filter(FilenameIndex.getVirtualFilesByName(MeteorProjectStartupActivity.METEOR_FOLDER,
                                                                    GlobalSearchScope.projectScope(project)),
                                file -> !JSLibraryUtil.isProbableLibraryFile(file) && file.isDirectory());
  }

  public boolean isMeteorProject(@Nullable Project project) {
    if (project == null || project.isDefault() || project.isDisposed()) return false;
    if (ApplicationManager.getApplication().isUnitTestMode() && !"enable".equals(System.getProperty("meteor.js"))) return false;

    return PropertiesComponent.getInstance(project).getBoolean(MeteorProjectStartupActivity.METEOR_PROJECT_KEY);
  }

  public @NotNull Collection<VirtualFile> getStoredMeteorFolders(@Nullable Project project) {
    if (project == null || project.isDefault()) return ContainerUtil.emptyList();
    List<String> values = PropertiesComponent.getInstance(project).getList(MeteorProjectStartupActivity.METEOR_FOLDERS_CACHED);
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }

    return values.stream()
      .map(el -> LocalFileSystem.getInstance().findFileByPath(el)).filter(el -> el != null && el.isValid())
      .collect(Collectors.toList());
  }

  public void storeMeteorFolders(@Nullable Project project, @NotNull Collection<String> folders) {
    if (project == null || project.isDefault()) return;

    PropertiesComponent.getInstance(project).setList(MeteorProjectStartupActivity.METEOR_FOLDERS_CACHED, folders);
  }

  public boolean hasMeteorFolders(@Nullable Project project) {
    if (project == null) return false;
    return !getMeteorFolders(project).isEmpty();
  }

  public void setIsMeteorProject(@NotNull Project project) {
    PropertiesComponent.getInstance(project).setValue(MeteorProjectStartupActivity.METEOR_PROJECT_KEY, true);
  }
}

