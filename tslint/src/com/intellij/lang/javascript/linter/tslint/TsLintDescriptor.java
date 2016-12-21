package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.javascript.linter.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 11/24/2016.
 */
public class TsLintDescriptor extends JSLinterDescriptorBase {

  @Nullable
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
  protected Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return TsLintConfiguration.class;
  }
}
