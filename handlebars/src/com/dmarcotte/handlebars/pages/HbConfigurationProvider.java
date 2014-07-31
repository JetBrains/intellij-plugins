package com.dmarcotte.handlebars.pages;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * If we have "JavaScript" dependency
 * then configuration {@link com.dmarcotte.handlebars.pages.HbConfigurationPage} will be added to "JavaScript/Templates" over extension point
 * with name "JavaScript.lang.templates".
 * If there is no the dependency we should use this provider for adding configuration in common list
 */
public class HbConfigurationProvider extends ConfigurableProvider {

  private Project myProject;

  public HbConfigurationProvider(Project project) {
    myProject = project;
  }

  @Nullable
  @Override
  public Configurable createConfigurable() {
    return Extensions.getArea(myProject).hasExtensionPoint("JavaScript.lang.templates") ? null : new HbConfigurationPage(myProject);
  }
}
