// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.linter.tslint.codestyle;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterCodeStyleImporter;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintConfigWrapper;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintSimpleRule;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory;

public class TsLintCodeStyleImporter extends JSLinterCodeStyleImporter<TsLintConfigWrapper> {
  public TsLintCodeStyleImporter(boolean reportAlreadyImported, boolean showUiOnMissingTool) {
    super(reportAlreadyImported, showUiOnMissingTool);
  }

  @NotNull
  @Override
  protected Configurable createSettingsConfigurable(@NotNull Project project) {
    return new TsLintConfigurable(project);
  }

  @NotNull
  @Override
  protected JSNpmLinterState getStoredState(Project project) {
    return TsLintConfiguration.getInstance(project).getExtendedState().getState();
  }

  @NotNull
  @Override
  protected String getNpmPackageName() {
    return TslintUtil.PACKAGE_NAME;
  }

  @NotNull
  @Override
  protected String getToolName() {
    return TsLintBundle.message("settings.javascript.linters.tslint.configurable.name");
  }

  @Override
  protected boolean isDirectlyImportable(@NotNull PsiFile configPsi) {
    TsLintConfigWrapper wrapper = parseConfigFromFile(configPsi);
    return wrapper != null && !wrapper.hasExtends();
  }

  @Nullable
  @Override
  protected TsLintConfigWrapper parseConfigFromFile(@NotNull PsiFile configPsi) {
    return ReadAction.compute(() -> TsLintConfigWrapper.Companion.getConfigForFile(configPsi));
  }

  @Nullable
  @Override
  protected TsLintConfigWrapper computeEffectiveConfig(@NotNull PsiFile configPsi,
                                                       @NotNull NodeJsLocalInterpreter interpreter,
                                                       @NotNull NodePackage linterPackage) throws ExecutionException {

    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.withCharset(StandardCharsets.UTF_8);
    commandLine.withWorkDirectory(ReadAction.compute(() -> configPsi.getContainingDirectory().getVirtualFile().getPath()));
    commandLine.setExePath(interpreter.getInterpreterSystemDependentPath());
    commandLine.addParameter(getPluginDirectory(TsLintImportCodeStyleAction.class, "js/convert-tslint-config.js").getAbsolutePath());
    commandLine.addParameter(linterPackage.getSystemDependentPath());
    commandLine.addParameter(configPsi.getVirtualFile().getPath());

    final CapturingProcessHandler processHandler = new CapturingProcessHandler(commandLine);
    final ProcessOutput processOutput = processHandler.runProcess((int)TimeUnit.SECONDS.toMillis(10));
    if (processOutput.isTimeout()) {
      throw new ExecutionException("Timeout processing TSLint configuration file");
    }
    final int exitCode = processOutput.getExitCode();
    final String stderr = processOutput.getStderr();
    String stdout = processOutput.getStdout();
    if (exitCode != 0) {
      throw new ExecutionException(
        "Error applying code style rules from  TSLint configuration file " + (StringUtil.isEmptyOrSpaces(stderr) ? stdout : stderr));
    }
    return TsLintConfigWrapper.Companion.getConfigFromText(stdout);
  }

  @NotNull
  @Override
  protected Pair<Collection<String>, Runnable> importConfig(@NotNull PsiFile configPsi, @NotNull TsLintConfigWrapper configWrapper) {
    Project project = configPsi.getProject();
    Collection<TsLintSimpleRule<?>> rules = configWrapper.getRulesToApply(project);
    if (rules.isEmpty()) {
      return Pair.create(ContainerUtil.emptyList(), null);
    }
    Map<TsLintSimpleRule<?>, Object> oldValues = configWrapper.getCurrentSettings(project, rules);
    configWrapper.applyRules(project, rules);
    return Pair.create(ContainerUtil.map(rules, el -> el.getOptionId()), () -> configWrapper.applyValues(project, oldValues));
  }
}
