package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaGlobalSettingsUtil {

  public static final String NODE_PACKAGE_NAME = "karma";
  private static final String KARMA_PACKAGE_DIR__KEY = "karma_support.karma_node_package_dir";
  private static final String NODE_INTERPRETER_PATH__KEY = "karma_support.node_interpreter_path";
  private static volatile NodeSettings CURRENT_NODE_SETTINGS = null;

  public static void storeKarmaPackageDir(@NotNull Project project, @NotNull String karmaPackageDir) {
    KarmaPackageDirSetting karmaPackageDirSetting = KarmaPackageDirSetting.getInstance(project);
    karmaPackageDirSetting.setPackageDir(karmaPackageDir);
    storeApplicationSetting(KARMA_PACKAGE_DIR__KEY, karmaPackageDir);
  }

  @Nullable
  public static String getKarmaNodePackageDir(@NotNull Project project, @Nullable String configFilePath) {
    KarmaPackageDirSetting karmaPackageDirSetting = KarmaPackageDirSetting.getInstance(project);
    String karmaPackageDir = karmaPackageDirSetting.getPackageDir();
    if (StringUtil.isEmpty(karmaPackageDir)) {
      karmaPackageDir = findKarmaPackageDir(project, configFilePath);
    }
    if (StringUtil.isEmpty(karmaPackageDir)) {
      karmaPackageDir = getApplicationSetting(KARMA_PACKAGE_DIR__KEY);
    }
    return karmaPackageDir;
  }

  @Nullable
  private static String findKarmaPackageDir(@NotNull Project project, @Nullable String configFilePath) {
    List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
    VirtualFile requester = getRequester(project, configFilePath);
    NodeModuleSearchUtil.findModulesWithName(modules,
                                             NODE_PACKAGE_NAME,
                                             requester,
                                             getNodeSettings(),
                                             true);
    for (CompletionModuleInfo module : modules) {
      VirtualFile moduleRoot = module.getVirtualFile();
      if (moduleRoot != null && moduleRoot.isValid() && moduleRoot.isDirectory()) {
        return FileUtil.toSystemDependentName(moduleRoot.getPath());
      }
    }
    return null;
  }

  @Nullable
  public static VirtualFile getRequester(@NotNull Project project, @Nullable String configFilePath) {
    VirtualFile requester = null;
    if (configFilePath != null) {
      File configFile = new File(configFilePath);
      if (configFile.isFile()) {
        requester = VfsUtil.findFileByIoFile(configFile, false);
      }
    }
    if (requester == null || !requester.isValid()) {
      requester = project.getBaseDir();
    }
    return requester;
  }

  public static void storeNodeInterpreterPath(@NotNull String nodeInterpreterPath) {
    storeApplicationSetting(NODE_INTERPRETER_PATH__KEY, nodeInterpreterPath);
    CURRENT_NODE_SETTINGS = null;
  }

  @Nullable
  public static String getNodeInterpreterPath() {
    String nodeInterpreterPath = getApplicationSetting(NODE_INTERPRETER_PATH__KEY);
    if (StringUtil.isEmpty(nodeInterpreterPath)) {
      File nodeInterpreterFile = NodeDetectionUtil.findInterpreterInPath();
      if (nodeInterpreterFile != null) {
        nodeInterpreterPath = nodeInterpreterFile.getAbsolutePath();
      }
    }
    return nodeInterpreterPath;
  }

  @Nullable
  public static NodeSettings getNodeSettings() {
    NodeSettings nodeSettings = CURRENT_NODE_SETTINGS;
    if (nodeSettings == null) {
      String interpreterPath = getNodeInterpreterPath();
      if (interpreterPath != null) {
        nodeSettings = new NodeSettings(interpreterPath);
        CURRENT_NODE_SETTINGS = nodeSettings;
      }
    }
    return nodeSettings;
  }

  @Nullable
  private static String getApplicationSetting(@NotNull String key) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    return propertiesComponent.getValue(key);
  }

  private static void storeApplicationSetting(@NotNull String key, @NotNull String value) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(key, value);
  }

}
