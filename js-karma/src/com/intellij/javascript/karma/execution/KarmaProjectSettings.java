package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable structure that represents a part of Karma run configuration settings that isn't shared with the rest of team.
 * These project-level are stored in ".idea/workspace.xml".
 */
public class KarmaProjectSettings {

  private static final String KARMA_PACKAGE_DIR__KEY = "javascript.karma.karma_node_package_dir";

  private KarmaProjectSettings() {
  }

  @NotNull
  public static NodePackage getKarmaPackage(@NotNull Project project) {
    String path = StringUtil.notNullize(PropertiesComponent.getInstance(project).getValue(KARMA_PACKAGE_DIR__KEY));
    return new NodePackage(path);
  }

  public static void setKarmaPackage(@NotNull Project project, @NotNull NodePackage karmaPackage) {
    PropertiesComponent.getInstance(project).setValue(KARMA_PACKAGE_DIR__KEY, karmaPackage.getSystemIndependentPath());
  }
}
