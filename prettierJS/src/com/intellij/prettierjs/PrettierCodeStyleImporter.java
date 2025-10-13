// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterCodeStyleImporter;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrettierCodeStyleImporter extends JSLinterCodeStyleImporter<PrettierConfig> {
  public PrettierCodeStyleImporter(boolean isForInitialImport) {
    super(isForInitialImport);
  }

  @Override
  protected @NotNull Configurable createSettingsConfigurable(@NotNull Project project) {
    return new PrettierConfigurable(project);
  }

  @Override
  protected @NotNull JSNpmLinterState getStoredState(Project project) {
    return PrettierConfiguration.getInstance(project);
  }

  @Override
  protected @NotNull String getNpmPackageName() {
    return PrettierUtil.PACKAGE_NAME;
  }

  @Override
  protected @NotNull String getToolName() {
    @NlsSafe String prettier = "Prettier";
    return prettier;
  }

  @Override
  protected boolean isDirectlyImportable(@NotNull PsiFile configPsi, @Nullable PrettierConfig parsedConfig) {
    return parsedConfig != null;
  }

  @Override
  protected @Nullable PrettierConfig parseConfigFromFile(@NotNull PsiFile configPsi) {
    return PrettierUtil.parseConfig(configPsi.getProject(), configPsi.getVirtualFile());
  }

  @Override
  protected @Nullable PrettierConfig computeEffectiveConfig(@NotNull PsiFile configPsi,
                                                            @NotNull NodeJsInterpreter interpreter,
                                                            @NotNull NodePackage linterPackage) throws ExecutionException {
    String configFilePath = FileUtil.toSystemDependentName(configPsi.getVirtualFile().getPath());
    String convertConfigScriptPath;
    try {
      convertConfigScriptPath =
        PrettierUtil.getPrettierLanguageServicePath().resolve("convert-prettier-config.js").toAbsolutePath().toString();
    }
    catch (IOException e) {
      throw new ExecutionException(e);
    }
    String absPkgPathToRequire = linterPackage.getAbsolutePackagePathToRequire(configPsi.getProject());
    if (absPkgPathToRequire == null) {
      throw new ExecutionException(PrettierBundle.message("dialog.message.cannot.find.absolute.package.path.to.require", linterPackage));
    }
    List<String> parameters = Arrays.asList(convertConfigScriptPath, absPkgPathToRequire, configFilePath);
    String text = runToolWithArguments(configPsi, interpreter, parameters);
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("Prettier: computed effective config for file %s:\n%s", configFilePath, text));
    }
    return PrettierUtil.parseConfigFromJsonText(text);
  }

  @Override
  protected @NotNull ImportResult importConfig(@NotNull PsiFile configPsi, @NotNull PrettierConfig config) {
    if (config.isInstalled(configPsi.getProject())) {
      return ImportResult.alreadyImported();
    }
    config.install(configPsi.getProject());
    return ImportResult.success(Collections.emptyList());
  }
}
