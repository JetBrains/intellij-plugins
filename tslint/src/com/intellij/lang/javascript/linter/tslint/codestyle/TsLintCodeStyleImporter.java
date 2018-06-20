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
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
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
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory;

public class TsLintCodeStyleImporter extends JSLinterCodeStyleImporter<TsLintConfigWrapper> {
  public TsLintCodeStyleImporter(boolean isForInitialImport) {
    super(isForInitialImport);
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
                                                       @NotNull NodeJsInterpreter interpreter,
                                                       @NotNull NodePackage linterPackage) throws ExecutionException {

    List<String> parameters =
      ContainerUtil.list(getPluginDirectory(TsLintImportCodeStyleAction.class, "js/convert-tslint-config.js").getAbsolutePath(),
                         linterPackage.getSystemDependentPath(),
                         configPsi.getVirtualFile().getPath());
    return TsLintConfigWrapper.Companion.getConfigFromText(runToolWithArguments(configPsi, interpreter, parameters));
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
