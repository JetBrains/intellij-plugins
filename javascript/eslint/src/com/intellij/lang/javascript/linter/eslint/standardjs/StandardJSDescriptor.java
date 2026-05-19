package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.formatter.StandardJSCodeStyle;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StandardJSDescriptor extends JSLinterDescriptor {


  @Override
  public @Nullable String packageJsonSectionName() {
    return StandardJSUtil.CONFIG_SECTION_NAME;
  }

  @Override
  public @NotNull @Nls String getDisplayName() {
    return EslintBundle.message("standardjs.name");
  }

  @Override
  public @NotNull String packageName() {
    return StandardJSUtil.PACKAGE_NAME;
  }

  @Override
  public @NotNull Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return StandardJSConfiguration.class;
  }

  @Override
  public void importSettings(@NotNull Project project, @NotNull JSLinterGuesser.EnableCase enableCase) {
    if (StandardJSCodeStyle.isInstalled(project)) {
      return;
    }
    StandardJSCodeStyle.install(project);
    JSLinterUtil.reportCodeStyleSettingsImported(
      project, EslintBundle.message(enableCase == JSLinterGuesser.EnableCase.dependency
                                    ? "standardjs.codestyle.updated.dependency"
                                    : enableCase == JSLinterGuesser.EnableCase.configSection
                                      ? "standardjs.codestyle.updated.config.section"
                                      : "standardjs.codestyle.updated", getDisplayName()), null, null);
  }
}
