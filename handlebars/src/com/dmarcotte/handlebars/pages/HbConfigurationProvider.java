// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
public class HbConfigurationProvider extends ConfigurableProvider {

  @NotNull
  private final Project myProject;

  public HbConfigurationProvider(@NotNull Project project) {
    myProject = project;
  }

  @Nullable
  @Override
  public Configurable createConfigurable() {
    return new HbConfigurationPage(myProject);
  }

  @Override
  public boolean canCreateConfigurable() {
    return !myProject.getExtensionArea().hasExtensionPoint("JavaScript.lang.templates");
  }
}
