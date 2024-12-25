// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.pages;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * If we have "JavaScript" dependency
 * then configuration {@link HbConfigurationPage} will be added to "JavaScript/Templates" over extension point
 * with name "JavaScript.lang.templates".
 * If there is no the dependency we should use this provider for adding configuration in common list
 */
public final class HbConfigurationProvider extends ConfigurableProvider {

  private final @NotNull Project myProject;

  public HbConfigurationProvider(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public @Nullable Configurable createConfigurable() {
    return new HbConfigurationPage(myProject);
  }

  @Override
  public boolean canCreateConfigurable() {
    return !myProject.getExtensionArea().hasExtensionPoint("JavaScript.lang.templates");
  }
}
