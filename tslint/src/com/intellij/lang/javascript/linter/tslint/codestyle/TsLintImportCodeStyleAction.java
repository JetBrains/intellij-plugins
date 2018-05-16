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
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintConfigWrapper;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintSimpleRule;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory;

public class TsLintImportCodeStyleAction extends AnAction {

  @Override
  public void update(AnActionEvent e) {
    final DataContext context = e.getDataContext();
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    final boolean enabledAndVisible = e.getProject() != null
                                      && psiFile != null
                                      && TslintUtil.isConfigFile(psiFile.getVirtualFile());
    e.getPresentation().setEnabledAndVisible(enabledAndVisible);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    final PsiFile configPsi = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (configPsi == null || project == null) {
      return;
    }
    TsLintConfigWrapper configWrapper = getConfigFromFile(project, configPsi);
    if (configWrapper == null) return;
    Collection<TsLintSimpleRule<?>> rules = configWrapper.getRulesToApply(project);
    Map<TsLintSimpleRule<?>, Object> oldValues = configWrapper.getCurrentSettings(project, rules);

    configWrapper.applyRules(project, rules);
    Set<String> appliedRules = rules.stream().map(el -> el.getOptionId()).collect(Collectors.toSet());
    String message = JSBundle.message("settings.javascript.linters.tslint.configurable.name");
    JSLinterUtil.reportCodeStyleSettingsImported(project, message, configPsi.getVirtualFile(), appliedRules,
                                                 () -> configWrapper.applyValues(project, oldValues));
  }

  @Nullable
  private static TsLintConfigWrapper getConfigFromFile(Project project, PsiFile configPsi) {
    TsLintConfigWrapper fromFile = TsLintConfigWrapper.Companion.getConfigForFile(configPsi);
    if (fromFile == null) {
      return null;
    }
    if (!fromFile.hasExtends()) {
      return fromFile;
    }
    TsLintState tsLintState = TsLintConfiguration.getInstance(project).getExtendedState().getState();
    final NodeJsInterpreterRef interpreterRef = tsLintState.getInterpreterRef();
    NodeJsLocalInterpreter interpreter = NodeJsLocalInterpreter.tryCast(interpreterRef.resolve(project));
    String packagePath = tsLintState.getPackagePath();

    assert interpreter != null;
    assert packagePath != null;
    String configJson;

    try {
      configJson = toJsonConfig(configPsi, interpreter, new NodePackage(tsLintState.getPackagePath()));
    }
    catch (ExecutionException exception) {
      reportError(project, exception.getMessage());
      return null;
    }
    return TsLintConfigWrapper.Companion.getConfigFromText(configJson);
  }
  
  @NotNull
  private static String toJsonConfig(@NotNull PsiFile configPsi,
                                     @NotNull NodeJsLocalInterpreter interpreter,
                                     @NotNull NodePackage tslintPackage) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.withCharset(StandardCharsets.UTF_8);
    commandLine.withWorkDirectory(configPsi.getContainingDirectory().getVirtualFile().getPath());
    commandLine.setExePath(interpreter.getInterpreterSystemDependentPath());
    commandLine.addParameter(new File(getPluginDirectory(TsLintImportCodeStyleAction.class, "js"), "convert-tslint-config")
                               .getAbsolutePath());
    commandLine.addParameter(tslintPackage.getSystemDependentPath());
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
      throw new ExecutionException("Error applying code style rules from  TSLint configuration file" + (StringUtil.isEmptyOrSpaces(stderr) ? stdout : stderr));
    }
    return stdout;
  }

  private static void reportError(@NotNull final Project project, @NotNull final String error) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      throw new RuntimeException(error);
    }
    else {
      JSLinterGuesser.NOTIFICATION_GROUP.createNotification(error, NotificationType.ERROR).notify(project);
    }
  }
}
