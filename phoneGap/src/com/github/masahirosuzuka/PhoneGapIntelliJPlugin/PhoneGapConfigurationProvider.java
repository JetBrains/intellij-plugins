package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.PhoneGapConfigurable;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * If we have "JavaScript" dependency
 * then configuration  will be added to "JavaScript.Phonegap/Cordova"
 * If there is no the dependency we should use this provider for adding configuration in common list
 */
public class PhoneGapConfigurationProvider extends ConfigurableProvider {

  private final @NotNull Project myProject;

  public PhoneGapConfigurationProvider(@NotNull Project project) {
    myProject = project;
  }

  @Nullable
  @Override
  public Configurable createConfigurable() {
    return new PhoneGapConfigurable(myProject);
  }

  @Override
  public boolean canCreateConfigurable() {
    return FileTypeManager.getInstance().getStdFileType("JavaScript") == PlainTextFileType.INSTANCE;
  }
}
