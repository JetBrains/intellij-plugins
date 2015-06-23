package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Immutable structure that represents a part of Karma run configuration settings that isn't shared with the rest of team.
 * These project-level are stored in ".idea/workspace.xml".
 */
public class KarmaProjectSettings {

  private static final String KARMA_PACKAGE_DIR__KEY = "javascript.karma.karma_node_package_dir";
  private static final String NODE_INTERPRETER_PATH__KEY = "javascript.karma.node_interpreter_path";
  private static volatile KarmaProjectSettings CURRENT_SETTINGS = null;

  private final String myNodeInterpreterPath;
  private final String myKarmaPackageDir;

  public KarmaProjectSettings(@NotNull String nodeInterpreterPath, @NotNull String karmaPackageDir) {
    myNodeInterpreterPath = nodeInterpreterPath;
    myKarmaPackageDir = karmaPackageDir;
  }

  @NotNull
  public String getNodeInterpreterPath() {
    return myNodeInterpreterPath;
  }

  @NotNull
  public String getKarmaPackageDir() {
    return myKarmaPackageDir;
  }

  @NotNull
  public static String getNodeInterpreterPath(@NotNull Project project) {
    return get(project).getNodeInterpreterPath();
  }

  @NotNull
  public static String getKarmaPackageDir(@NotNull Project project) {
    return get(project).getKarmaPackageDir();
  }

  public static void setNodeInterpreterPath(@NotNull Project project, @NotNull String nodeInterpreterPath) {
    setProjectSetting(project, NODE_INTERPRETER_PATH__KEY, nodeInterpreterPath);
    CURRENT_SETTINGS = null;
  }

  public static void setKarmaPackageDir(@NotNull Project project, @NotNull String karmaPackageDir) {
    setProjectSetting(project, KARMA_PACKAGE_DIR__KEY, karmaPackageDir);
    CURRENT_SETTINGS = null;
  }

  @NotNull
  public static KarmaProjectSettings get(@NotNull Project project) {
    KarmaProjectSettings settings = CURRENT_SETTINGS;
    if (settings != null) {
      return settings;
    }
    String nodeInterpreterPath = doGetNodeInterpreterPath(project);
    String karmaPackageDir = doGetKarmaPackageDir(project);
    settings = new KarmaProjectSettings(nodeInterpreterPath, karmaPackageDir);
    CURRENT_SETTINGS = settings;
    return settings;
  }

  @NotNull
  private static String doGetNodeInterpreterPath(@NotNull Project project) {
    String nodeInterpreterPath = getProjectSetting(project, NODE_INTERPRETER_PATH__KEY);
    if (StringUtil.isEmpty(nodeInterpreterPath)) {
      File nodeInterpreterFile = NodeDetectionUtil.findInterpreterInPath();
      if (nodeInterpreterFile != null) {
        nodeInterpreterPath = nodeInterpreterFile.getAbsolutePath();
      }
    }
    return StringUtil.notNullize(nodeInterpreterPath);
  }

  @NotNull
  private static String doGetKarmaPackageDir(@NotNull Project project) {
    KarmaPackageDirSetting karmaPackageDirSetting = KarmaPackageDirSetting.getInstance(project);
    String karmaPackageDir = karmaPackageDirSetting.getPackageDir();
    if (StringUtil.isEmpty(karmaPackageDir)) {
      karmaPackageDir = getProjectSetting(project, KARMA_PACKAGE_DIR__KEY);
    }
    return StringUtil.notNullize(karmaPackageDir);
  }

  @Nullable
  private static String getProjectSetting(@NotNull Project project, @NotNull String key) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    return propertiesComponent.getValue(key);
  }

  private static void setProjectSetting(@NotNull Project project, @NotNull String key, @NotNull String value) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    propertiesComponent.setValue(key, value);
  }
}
