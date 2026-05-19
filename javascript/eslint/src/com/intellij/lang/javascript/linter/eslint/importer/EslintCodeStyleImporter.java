package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.json.JsonFileType;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSLinterCodeStyleImporter;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.lang.javascript.linter.eslint.EslintConfigurable;
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EslintCodeStyleImporter extends JSLinterCodeStyleImporter<EslintConfigWrapper> {
  public EslintCodeStyleImporter(boolean isForInitialImport) {
    super(isForInitialImport);
  }

  @Override
  protected @NotNull Configurable createSettingsConfigurable(@NotNull Project project) {
    return new EslintConfigurable(project);
  }

  @Override
  protected @NotNull JSNpmLinterState getStoredState(Project project) {
    return EslintConfiguration.getInstance(project).getExtendedState().getState();
  }

  @Override
  protected @NotNull String getNpmPackageName() {
    return EslintUtil.PACKAGE_NAME;
  }

  @Override
  protected @NotNull String getToolName() {
    return EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
  }

  @Override
  protected boolean isDirectlyImportable(@NotNull PsiFile configPsi, @Nullable EslintConfigWrapper parsedConfig) {
    return configDoesNotRequireTranslation(configPsi);
  }

  @Override
  protected @Nullable EslintConfigWrapper parseConfigFromFile(@NotNull PsiFile configPsi) {
    return EslintConfigWrapper.getForFile(configPsi);
  }

  @Override
  protected @NotNull ImportResult importConfig(@NotNull PsiFile configPsi, @NotNull EslintConfigWrapper config) {
    if (!config.hasDataToImport(configPsi.getProject())) {
      return ImportResult.alreadyImported();
    }
    return ImportResult.success(config.modifySettings(configPsi.getProject()));
  }

  @Override
  protected @Nullable EslintConfigWrapper computeEffectiveConfig(@NotNull PsiFile configPsi,
                                                                 @NotNull NodeJsInterpreter interpreter,
                                                                 @NotNull NodePackage linterPackage) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    if (linterPackage instanceof YarnPnpNodePackage) {
      ((YarnPnpNodePackage)linterPackage).addYarnRunToCommandLine(commandLine, configPsi.getProject(), interpreter, null);
    }
    else {
      commandLine.addParameter(Objects.requireNonNull(getBinJsFile(linterPackage)).toString());
    }
    // UNC paths should be with backslashes on Windows, e.g. \\wsl$\...
    String configFilePath = FileUtil.toSystemDependentName(configPsi.getVirtualFile().getPath());
    commandLine.addParameters("-c", configFilePath, "--print-config", configFilePath);
    String stdOut = runCommandLine(commandLine, configPsi, interpreter);
    if (LOG.isTraceEnabled()) {
      LOG.trace(String.format("ESLint: computed effective config for file %s:\n%s", configFilePath, stdOut));
    }
    return ReadAction.compute(() -> {
      PsiFile tempFile = PsiFileFactory.getInstance(configPsi.getProject())
        .createFileFromText(
          EslintUtil.DEFAULT_CONFIG_PREFIX + ".json",
          JsonFileType.INSTANCE, stdOut);
      return EslintConfigWrapper.getForFile(tempFile);
    });
  }

  private static boolean configDoesNotRequireTranslation(final @NotNull PsiFile psiFile) {
    if (!JsonFileType.INSTANCE.equals(psiFile.getFileType())) return false;
    final JsonObject rootObject = EslintUtil.getConfigRootObject(psiFile);
    if (rootObject == null) return false;
    if (rootObject.findProperty("extends") != null) return false;
    final JsonProperty root = rootObject.findProperty("root");
    return root != null && root.getValue() != null && "true".equals(root.getValue().getText());
  }
}
