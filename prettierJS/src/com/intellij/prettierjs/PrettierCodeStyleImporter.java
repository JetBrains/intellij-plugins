// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterCodeStyleImporter;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory;

public class PrettierCodeStyleImporter extends JSLinterCodeStyleImporter<PrettierConfig> {
  public PrettierCodeStyleImporter(boolean isForInitialImport) {
    super(isForInitialImport);
  }

  @NotNull
  @Override
  protected Configurable createSettingsConfigurable(@NotNull Project project) {
    return new PrettierConfigurable(project);
  }

  @NotNull
  @Override
  protected JSNpmLinterState getStoredState(Project project) {
    return PrettierConfiguration.getInstance(project);
  }

  @NotNull
  @Override
  protected String getNpmPackageName() {
    return PrettierUtil.PACKAGE_NAME;
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "Prettier";
  }

  @Override
  protected boolean isDirectlyImportable(@NotNull PsiFile configPsi, @Nullable PrettierConfig parsedConfig) {
    return parsedConfig != null;
  }

  @Nullable
  @Override
  protected PrettierConfig parseConfigFromFile(@NotNull PsiFile configPsi) {
    return PrettierUtil.parseConfig(configPsi.getProject(), configPsi.getVirtualFile());
  }

  @Nullable
  @Override
  protected PrettierConfig computeEffectiveConfig(@NotNull PsiFile configPsi,
                                                  @NotNull NodeJsInterpreter interpreter,
                                                  @NotNull NodePackage linterPackage) throws ExecutionException {
    String configFilePath = FileUtil.toSystemDependentName(configPsi.getVirtualFile().getPath());
    String convertConfigScriptPath = getPluginDirectory(PrettierCodeStyleImporter.class, "prettierLanguageService/convert-prettier-config.js").getAbsolutePath();
    String absPkgPathToRequire = linterPackage.getAbsolutePackagePathToRequire(configPsi.getProject());
    if (absPkgPathToRequire == null) {
      throw new ExecutionException("Cannot find absolute package path to require: " + linterPackage);
    }
    List<String> parameters = Arrays.asList(convertConfigScriptPath, absPkgPathToRequire, configFilePath);
    String text = runToolWithArguments(configPsi, interpreter, parameters);
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Prettier: computed effective config for file %s:\n%s", configFilePath, text));
    }
    return PrettierUtil.parseConfigFromJsonText(text);
  }

  @NotNull
  @Override
  protected ImportResult importConfig(@NotNull PsiFile configPsi, @NotNull PrettierConfig config) {
    if (config.isInstalled(configPsi.getProject())) {
      return ImportResult.alreadyImported();
    }
    config.install(configPsi.getProject());
    return ImportResult.success(Collections.emptyList());
  }
}
