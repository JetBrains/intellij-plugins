package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.tslint.ide.TsLintConfigFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 11/24/2016.
 */
public final class TsLintDescriptor extends JSLinterDescriptor {

  @NotNull
  @Override
  public String getDisplayName() {
    return JSBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  public String packageName() {
    return "tslint";
  }

  @Override
  public boolean hasConfigFiles(@NotNull Project project) {
    return JSLinterConfigFileUtil.projectHasConfigFiles(project, TsLintConfigFileType.INSTANCE);
  }

  @NotNull
  @Override
  public Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return TsLintConfiguration.class;
  }
}
