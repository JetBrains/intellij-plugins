// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootModificationTracker;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SlowOperations;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PhoneGapUtil {

  public static final String IONIC_CONFIG = "ionic.config.json";

  public static final String FOLDER_PLATFORMS = "platforms";
  public static final String FOLDER_WWW = "www";
  public static final String FOLDER_CORDOVA = ".cordova";
  public static final String FOLDER_PLUGINS = "plugins";
  public static final String[] POSSIBLE_FOLDERS_IN_PHONEGAP_ROOT = {FOLDER_PLATFORMS, FOLDER_PLUGINS, FOLDER_WWW};

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapExecutableTextField(@Nullable Project project) {
    TextFieldWithHistoryWithBrowseButton field = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project, PhoneGapBundle.message("phonegap.conf.executable.title"),
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(), () -> getDefaultExecutablePaths());
    String executablePath = PhoneGapSettings.getInstance().getExecutablePath();
    setDefaultValue(field, executablePath);

    return field;
  }

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapWorkingDirectoryField(@Nullable final Project project) {
    TextFieldWithHistoryWithBrowseButton field = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project, PhoneGapBundle.message("phonegap.conf.work.dir.title"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor(), () -> getDefaultWorkingDirectory(project));
    setDefaultValue(field, PhoneGapSettings.getInstance().getWorkingDirectory(project));

    return field;
  }

  public static void setFieldWithHistoryWithBrowseButtonPath(@NotNull TextFieldWithHistoryWithBrowseButton field,
                                                             @Nullable String executablePath) {
    setDefaultValue(field, executablePath);
  }


  @NotNull
  public static List<String> getDefaultExecutablePaths() {
    List<String> paths = new ArrayList<>();
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_PHONEGAP));
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_IONIC));
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_CORDOVA));
    return paths;
  }

  @NotNull
  public static List<String> getDefaultWorkingDirectory(@Nullable Project project) {
    List<String> paths = new ArrayList<>();
    if (project == null) return paths;
    VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
    if (baseDir == null) return paths;

    if (folderExist(baseDir, FOLDER_PLATFORMS) ||
        folderExist(baseDir, FOLDER_WWW) ||
        folderExist(baseDir, FOLDER_CORDOVA)) {

      ContainerUtil.addIfNotNull(paths, project.getBasePath());
    }
    else {
      addPaths(paths, SlowOperations.allowSlowOperations(() -> getFolders(project)));
    }

    return paths;
  }

  private static boolean folderExist(VirtualFile baseDir, String name) {
    VirtualFile child = baseDir.findChild(name);
    return child != null && child.isDirectory();
  }

  private static void setDefaultValue(@NotNull TextFieldWithHistoryWithBrowseButton field, @Nullable String defaultValue) {
    final TextFieldWithHistory textFieldWithHistory = field.getChildComponent();

    if (StringUtil.isNotEmpty(defaultValue)) {
      setTextFieldWithHistory(textFieldWithHistory, defaultValue);
    }
  }

  public static void setTextFieldWithHistory(TextFieldWithHistory textFieldWithHistory, String value) {
    if (null != value) {
      textFieldWithHistory.setText(value);
      textFieldWithHistory.addCurrentTextToHistory();
    }
  }

  private static Collection<VirtualFile> getFolders(@NotNull Project project) {
    for (String folder : POSSIBLE_FOLDERS_IN_PHONEGAP_ROOT) {
      Collection<VirtualFile> files =
        ContainerUtil.filter(FilenameIndex.getVirtualFilesByName(folder, GlobalSearchScope.projectScope(project)),
                             file -> file.isDirectory());
      if (!files.isEmpty()) {
        return files;
      }
    }

    return ContainerUtil.emptyList();
  }

  private static void addPaths(@NotNull List<String> paths, @NotNull Collection<VirtualFile> platforms) {
    for (VirtualFile platform : platforms) {
      ContainerUtil.addIfNotNull(paths, platform.getParent().getPath());
    }
  }

  @Nullable
  private static String getPath(@NotNull String name) {
    File path = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? name + ".cmd" : name);
    return (path != null && path.exists()) ? path.getAbsolutePath() : null;
  }

  public static boolean isPhoneGapProject(@NotNull final Project project) {
    if (DumbService.isDumb(project)) return false;

    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      PsiFile[] files = FilenameIndex.getFilesByName(project, "config.xml", GlobalSearchScope.projectScope(project));

      PsiFile matchedFile = ContainerUtil.find(files, psiFile -> {
        if (!(psiFile instanceof XmlFile)) return false;

        XmlTag root = ((XmlFile)psiFile).getRootTag();
        if (root == null) return false;

        return root.getName().equals("widget");
      });

      if (matchedFile != null) {
        return CachedValueProvider.Result.create(true, matchedFile);
      }

      if (files.length > 0) {
        Object[] append = ArrayUtil.append(files, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, Object.class);
        return CachedValueProvider.Result.create(false, append);
      }

      return CachedValueProvider.Result.create(false,
                                               VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                               ProjectRootModificationTracker.getInstance(project));
    });
  }
}
