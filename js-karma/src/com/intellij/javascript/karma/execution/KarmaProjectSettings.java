package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable structure that represents a part of Karma run configuration settings that isn't shared with the rest of team.
 * These project-level are stored in ".idea/workspace.xml".
 */
public class KarmaProjectSettings {

  private static final String KARMA_PACKAGE_DIR__KEY = "javascript.karma.karma_node_package_dir";
  private static final Key<KarmaProjectSettings> SETTINGS_KEY = Key.create("KARMA_SETTINGS_KEY");

  private final String myKarmaPackageDir;

  public KarmaProjectSettings(@NotNull String karmaPackageDir) {
    myKarmaPackageDir = karmaPackageDir;
  }

  @NotNull
  public String getKarmaPackageDir() {
    return myKarmaPackageDir;
  }

  @NotNull
  public static String getKarmaPackageDir(@NotNull Project project) {
    return get(project).getKarmaPackageDir();
  }

  public static void setKarmaPackageDir(@NotNull Project project, @NotNull String karmaPackageDir) {
    setProjectSetting(project, KARMA_PACKAGE_DIR__KEY, karmaPackageDir);
    SETTINGS_KEY.set(project, null);
  }

  @NotNull
  private static KarmaProjectSettings get(@NotNull Project project) {
    KarmaProjectSettings settings = SETTINGS_KEY.get(project);
    if (settings != null) {
      return settings;
    }
    String karmaPackageDir = doGetKarmaPackageDir(project);
    settings = new KarmaProjectSettings(karmaPackageDir);
    SETTINGS_KEY.set(project, settings);
    return settings;
  }

  @NotNull
  private static String doGetKarmaPackageDir(@NotNull Project project) {
    return getProjectSetting(project, KARMA_PACKAGE_DIR__KEY);
  }

  @NotNull
  private static String getProjectSetting(@NotNull Project project, @NotNull String key) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    return StringUtil.notNullize(propertiesComponent.getValue(key));
  }

  private static void setProjectSetting(@NotNull Project project, @NotNull String key, @NotNull String value) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    propertiesComponent.setValue(key, value);
  }
}
