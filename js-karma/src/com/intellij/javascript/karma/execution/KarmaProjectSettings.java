// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable structure that represents a part of Karma run configuration settings that isn't shared with the rest of team.
 * These project-level are stored in ".idea/workspace.xml".
 */
public final class KarmaProjectSettings {

  private static final String KARMA_PACKAGE_DIR__KEY = "javascript.karma.karma_node_package_dir";

  private KarmaProjectSettings() {
  }

  public static @NotNull NodePackage getKarmaPackage(@NotNull Project project) {
    String path = StringUtil.notNullize(PropertiesComponent.getInstance(project).getValue(KARMA_PACKAGE_DIR__KEY));
    return KarmaUtil.PKG_DESCRIPTOR.createPackage(path);
  }

  public static void setKarmaPackage(@NotNull Project project, @NotNull NodePackage karmaPackage) {
    PropertiesComponent.getInstance(project).setValue(KARMA_PACKAGE_DIR__KEY, karmaPackage.getSystemIndependentPath());
  }
}
