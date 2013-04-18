package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaGlobalSettingsUtil {

  public static final String NODE_PACKAGE_NAME = "karma";
  private static final String KARMA_NODE_PACKAGE_DIR = "karma_support.karma_node_package_dir";
  private static final String NODE_INTERPRETER_PATH = "karma_support.node_interpreter_path";

  public static void storeKarmaNodePackageDir(@NotNull String karmaNodePackageDir) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(KARMA_NODE_PACKAGE_DIR, karmaNodePackageDir);
  }

  @Nullable
  public static String loadKarmaNodePackageDir() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    return propertiesComponent.getValue(KARMA_NODE_PACKAGE_DIR);
  }

  public static void storeNodeInterpreterPath(@NotNull String nodeInterpreterPath) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(NODE_INTERPRETER_PATH, nodeInterpreterPath);
  }

  @Nullable
  public static String loadNodeInterpreterPath() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    return propertiesComponent.getValue(NODE_INTERPRETER_PATH);
  }

}
