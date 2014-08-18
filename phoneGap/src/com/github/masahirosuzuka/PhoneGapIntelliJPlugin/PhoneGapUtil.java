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
  public static final String FOLDER_PLUGINS = "plugins";
  public static final String[] POSSIBLE_FOLDERS_IN_PHONEGAP_ROOT = {FOLDER_PLATFORMS, FOLDER_PLUGINS, FOLDER_WWW};

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
    String executablePath = PhoneGapSettings.getInstance().getExecutablePath();
    setDefaultValue(field, executablePath);

    return field;
  }

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapWorkingDirectoryField(@Nullable final Project project) {
    TextFieldWithHistoryWithBrowseButton field = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project, PhoneGapBundle.message("phonegap.conf.work.dir.name"),
      FileChooserDescriptorFactory.createSingleFolderDescriptor(), new NotNullProducer<List<String>>() {
        @NotNull
        @Override
        public List<String> produce() {
          return getDefaultWorkingDirectory(project);
        }
      });
    setDefaultValue(field, PhoneGapSettings.getInstance().getWorkingDirectory(project));

    return field;
  }

  public static void setFieldWithHistoryWithBrowseButtonPath(@NotNull TextFieldWithHistoryWithBrowseButton field,
                                                             @Nullable String executablePath) {
    setDefaultValue(field, executablePath);
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
      addPaths(paths, getFolders(project));
    }

    return paths;
  }

  private static void setDefaultValue(@NotNull TextFieldWithHistoryWithBrowseButton field, @Nullable String defaultValue) {
    final TextFieldWithHistory textFieldWithHistory = field.getChildComponent();

    setTextFieldWithHistory(textFieldWithHistory, defaultValue);
  }

  public static void setTextFieldWithHistory(TextFieldWithHistory textFieldWithHistory, String value) {
    if (StringUtil.isNotEmpty(value)) {
      textFieldWithHistory.setText(value);
      textFieldWithHistory.addCurrentTextToHistory();
    }
  }

  private static Collection<VirtualFile> getFolders(@NotNull Project project) {
    for (String folder : POSSIBLE_FOLDERS_IN_PHONEGAP_ROOT) {
      Collection<VirtualFile> files =
        FilenameIndex.getVirtualFilesByName(project, folder, GlobalSearchScope.projectScope(project));
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
}
