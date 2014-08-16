package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class PhoneGapUtil {

  public static final String FOLDER_PLATFORMS = "platforms";
  public static final String FOLDER_WWW = "www";
  public static final String FOLDER_CORDOVA = ".cordova";

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapExecutableTextField(@Nullable Project project) {
    TextFieldWithHistoryWithBrowseButton field = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project, PhoneGapBundle.message("phonegap.conf.executable.name"),
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(), new NotNullProducer<List<String>>() {
        @NotNull
        @Override
        public List<String> produce() {
          return
            getDefaultExecutablePaths();
        }
      });
    final TextFieldWithHistory textFieldWithHistory = field.getChildComponent();

    String executablePath = PhoneGapSettings.getInstance().getExecutablePath();
    if (StringUtil.isNotEmpty(executablePath)) {
      textFieldWithHistory.setText(executablePath);
      textFieldWithHistory.addCurrentTextToHistory();
    }

    return field;
  }

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapWorkingDirectoryField(final @Nullable Project project) {
    TextFieldWithHistoryWithBrowseButton field = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project, PhoneGapBundle.message("phonegap.conf.work.dir.name"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor(), new NotNullProducer<List<String>>() {
        @NotNull
        @Override
        public List<String> produce() {
          return getDefaultWorkingDirectory(project);
        }
      });
    final TextFieldWithHistory textFieldWithHistory = field.getChildComponent();

    String directory = PhoneGapSettings.getInstance().getWorkingDirectory(project);
    if (StringUtil.isNotEmpty(directory)) {
      textFieldWithHistory.setText(directory);
      textFieldWithHistory.addCurrentTextToHistory();
    }

    return field;
  }

  public static void setFieldWithHistoryPath(TextFieldWithHistoryWithBrowseButton field, String executablePath) {
    TextFieldWithHistory component = field.getChildComponent();
    component.setText(executablePath);
    if (!StringUtil.isEmpty(executablePath)) {
      component.addCurrentTextToHistory();
    }
  }

  @NotNull
  public static List<String> getDefaultExecutablePaths() {
    List<String> paths = ContainerUtil.newArrayList();
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_PHONEGAP));
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_IONIC));
    ContainerUtil.addIfNotNull(paths, getPath(PhoneGapCommandLine.PLATFORM_CORDOVA));
    return paths;
  }

  @NotNull
  public static List<String> getDefaultWorkingDirectory(@Nullable Project project) {
    List<String> paths = ContainerUtil.newArrayList();
    if (project == null) return paths;
    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null) return paths;

    if (baseDir.findChild(FOLDER_PLATFORMS) != null ||
        baseDir.findChild(FOLDER_WWW) != null ||
        baseDir.findChild(FOLDER_CORDOVA) != null) {

      ContainerUtil.addIfNotNull(paths, project.getBasePath());
    }
    else {
      Collection<VirtualFile> results =
        getPlatformsFolders(project);

      if (results.isEmpty()) {
        results = getPhoneGapProjectRootFolders(project);
      }
      addPaths(paths, results);
    }

    return paths;
  }

  static Collection<VirtualFile> getPhoneGapProjectRootFolders(Project project) {
    return FilenameIndex.getVirtualFilesByName(project, "www", GlobalSearchScope.projectScope(project));
  }

  static Collection<VirtualFile> getPlatformsFolders(Project project) {
    return FilenameIndex.getVirtualFilesByName(project, "platforms", GlobalSearchScope.projectScope(project));
  }

  private static void addPaths(List<String> paths, Collection<VirtualFile> platforms) {
    for (VirtualFile platform : platforms) {
      ContainerUtil.addIfNotNull(paths, platform.getParent().getPath());
    }
  }

  @Nullable
  private static String getPath(String name) {
    File path = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? name + ".cmd" : name);
    return (path != null && path.exists()) ? path.getAbsolutePath() : null;
  }
}
