// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.codestyle;

import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterCodeStyleImporter;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintConfigWrapper;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintRule;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory;

public class TsLintCodeStyleImporter extends JSLinterCodeStyleImporter<TsLintConfigWrapper> {
  public TsLintCodeStyleImporter(boolean isForInitialImport) {
    super(isForInitialImport);
  }

  @Override
  protected @NotNull Configurable createSettingsConfigurable(@NotNull Project project) {
    return new TsLintConfigurable(project);
  }

  @Override
  protected @NotNull JSNpmLinterState getStoredState(Project project) {
    return TsLintConfiguration.getInstance(project).getExtendedState().getState();
  }

  @Override
  protected @NotNull String getNpmPackageName() {
    return TslintUtil.PACKAGE_NAME;
  }

  @Override
  protected @NotNull String getToolName() {
    return TsLintBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  protected boolean isDirectlyImportable(@NotNull PsiFile configPsi, @Nullable TsLintConfigWrapper parsedConfig) {
    return parsedConfig != null && !parsedConfig.hasExtends();
  }

  @Override
  protected @Nullable TsLintConfigWrapper parseConfigFromFile(@NotNull PsiFile configPsi) {
    return TsLintConfigWrapper.Companion.getConfigForFile(configPsi);
  }

  @Override
  protected @Nullable TsLintConfigWrapper computeEffectiveConfig(@NotNull PsiFile configPsi,
                                                                 @NotNull NodeJsInterpreter interpreter,
                                                                 @NotNull NodePackage linterPackage) throws ExecutionException {

    String configFilePath = FileUtil.toSystemDependentName(configPsi.getVirtualFile().getPath());
    List<String> parameters =
      Arrays.asList(getPluginDirectory(TsLintImportCodeStyleAction.class, "js/convert-tslint-config.js").getAbsolutePath(),
                    linterPackage.getSystemDependentPath(), configFilePath);
    String text = runToolWithArguments(configPsi, interpreter, parameters);
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("TSLint: computed effective config for file %s:\n%s", configFilePath, text));
    }
    return TsLintConfigWrapper.Companion.getConfigFromText(text);
  }

  @Override
  protected @NotNull ImportResult importConfig(@NotNull PsiFile configPsi, @NotNull TsLintConfigWrapper configWrapper) {
    Project project = configPsi.getProject();
    Collection<TsLintRule> rules = configWrapper.getRulesToApply(project);
    if (rules.isEmpty()) {
      return ImportResult.alreadyImported();
    }
    configWrapper.applyRules(project, rules);
    List<String> appliedRuleCodes = rules.stream()
      .map(TsLintRule::getOptionId)
      //in the current implementation, a single TSLint rule code will be duplicated if it changes several IDE settings
      .distinct()
      .collect(Collectors.toList());
    return ImportResult.success(appliedRuleCodes);
  }
}
