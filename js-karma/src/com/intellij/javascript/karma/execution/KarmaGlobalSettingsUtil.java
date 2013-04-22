package com.intellij.javascript.karma.execution;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodeSettings;
import com.intellij.javascript.nodejs.ResolvedModuleInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaGlobalSettingsUtil {

  public static final String NODE_NODE_PACKAGE_NAME = "karma";
  private static final String KARMA_NODE_PACKAGE_DIR = "karma_support.karma_node_package_dir";
  private static final String NODE_INTERPRETER_PATH = "karma_support.node_interpreter_path";
  private static volatile NodeSettings CURRENT_NODE_SETTINGS = null;

  public static void storeKarmaNodePackageDir(@NotNull String karmaNodePackageDir) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(KARMA_NODE_PACKAGE_DIR, karmaNodePackageDir);
  }

  @Nullable
  public static String getKarmaNodePackageDir(@NotNull Project project) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    String karmaNodePackageDir = propertiesComponent.getValue(KARMA_NODE_PACKAGE_DIR);
    if (StringUtil.isEmpty(karmaNodePackageDir)) {
      ResolvedModuleInfo moduleInfo = NodeModuleSearchUtil.resolveModule(NODE_NODE_PACKAGE_NAME, project.getBaseDir(), getNodeSettings());
      if (moduleInfo != null) {
        VirtualFile dir = moduleInfo.getModuleSourceRoot();
        if (dir.isDirectory()) {
          karmaNodePackageDir = FileUtil.toSystemDependentName(dir.getPath());
        }
      }
    }
    return karmaNodePackageDir;
  }

  public static void storeNodeInterpreterPath(@NotNull String nodeInterpreterPath) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(NODE_INTERPRETER_PATH, nodeInterpreterPath);
    CURRENT_NODE_SETTINGS = null;
  }

  @Nullable
  public static String getNodeInterpreterPath() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    String nodeInterpreterPath = propertiesComponent.getValue(NODE_INTERPRETER_PATH);
    if (StringUtil.isEmpty(nodeInterpreterPath)) {
      File nodeInterpreterFile = PathEnvironmentVariableUtil.findInPath(NodeDetectionUtil.NODE_INTERPRETER_BASE_NAME);
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

}
