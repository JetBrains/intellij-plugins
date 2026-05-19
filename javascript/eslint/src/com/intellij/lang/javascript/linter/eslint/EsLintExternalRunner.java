package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.eslint.config.EslintConfigFileChangeTracker;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceClient;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceClient.Response;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class EsLintExternalRunner<TResult> {
  public static final Logger LOG = Logger.getInstance(EsLintExternalRunner.class);
  private final boolean myIsOnTheFly;
  private final EslintLanguageServiceClient myLanguageService;

  public EsLintExternalRunner(boolean isOnTheFly, @NotNull EslintLanguageServiceClient service) {
    myIsOnTheFly = isOnTheFly;
    myLanguageService = service;
  }

  public static @NotNull JSLinterAnnotationResult highlight(@NotNull JSLinterInput<EslintState> input,
                                                            @NotNull EslintLanguageServiceClient service,
                                                            boolean isOnTheFly) {
    Ref<VirtualFile> bestGuessConfig = new Ref<>(null);
    EsLintExternalRunner<List<EslintError>> runner = new EsLintExternalRunner<>(isOnTheFly, service) {

      @Override
      protected @Nullable CompletableFuture<Response<List<EslintError>>> performServiceRequest(@NotNull EslintRequestData requestData) {
        bestGuessConfig.set(ContainerUtil.getFirstItem(requestData.getPossibleConfigs()));
        return service.highlight(requestData, input.getState().getExtraOptions());
      }
    };
    try {
      List<EslintError> execute = ObjectUtils.coalesce(runner.execute(input), Collections.emptyList());
      return JSLinterAnnotationResult.createLinterResult(input, execute, bestGuessConfig.get());
    }
    catch (LinterExecutionException e) {
      return JSLinterAnnotationResult.create(input, e.getAnnotation(), bestGuessConfig.get());
    }
  }

  public static @Nullable String fixFile(@NotNull JSLinterInput<EslintState> input, @NotNull EslintLanguageServiceClient service)
    throws LinterExecutionException {
    EsLintExternalRunner<String> runner = new EsLintExternalRunner<>(false, service) {

      @Override
      protected @Nullable Future<EslintLanguageServiceClient.Response<String>> performServiceRequest(@NotNull EslintRequestData requestData) {
        return service.fixFile(requestData, input.getState().getExtraOptions());
      }
    };
    return runner.execute(input);
  }

  protected abstract @Nullable Future<EslintLanguageServiceClient.Response<TResult>> performServiceRequest(@NotNull EslintRequestData requestData);

  private @Nullable TResult eslint(@NotNull JSLinterInput<EslintState> input,
                                   @NotNull EslintRequestData requestData) throws LinterExecutionException {
    final Future<Response<TResult>> future = performServiceRequest(requestData);
    String error;
    Response<TResult> result = null;
    long startNanoTime = System.nanoTime();
    boolean isTimeout = false;
    try {
      result = JSLanguageServiceUtil.awaitLanguageService(future, myLanguageService, requestData.getFileToLint(), EslintUtil.getTimeout());
      error = result == null ? JavaScriptBundle.message("javascript.language.service.cannot.get.results") : result.globalError;
    }
    catch (ExecutionException e) {
      error = e.getMessage();
      // Check if the future was cancelled, which indicates a timeout
      isTimeout = future != null && future.isCancelled();
    }
    finally {
      long timeSpent = TimeoutUtil.getDurationMillis(startNanoTime);
      ESLintEventCollector.INSTANCE.logResponse(timeSpent, isTimeout ?
                                               ESLintEventCollector.ResponseStatus.TIMEOUT :
                                               ESLintEventCollector.ResponseStatus.SUCCESS);
    }

    if (error != null && !suppressNoConfigError(result, input.getState().getNodePackageRef(), requestData)) {
      throw new LinterExecutionException(createFileLevelAnnotation(input, error));
    }

    return result != null ? result.value : null;
  }

  private static boolean suppressNoConfigError(@Nullable Response result,
                                               @NotNull NodePackageRef configuredPackage,
                                               @NotNull EslintRequestData requestData) {
    return result != null &&
           result.isNoConfigFile &&
           (configuredPackage == AutodetectLinterPackage.INSTANCE || requestData.getFileKind() != EslintUtil.FileKind.JavaScriptAndOther);
  }

  private static @NotNull JSLinterFileLevelAnnotation createFileLevelAnnotation(@NotNull JSLinterInput input,
                                                                                @NotNull @InspectionMessage String error) {
    final ProcessOutput output = new ProcessOutput();
    output.appendStderr(error);
    List<String> meaningfulLines = Arrays.stream(StringUtil.splitByLines(error))
      .map(s -> s.trim())
      .filter(string -> {
        if (StringUtil.isEmpty(string)) {
          return false;
        }
        if (FileUtil.isAbsolute(string) || string.endsWith(":") && FileUtil.isAbsolute(string.substring(0, string.length() - 1))) {
          return false;
        }
        return true;
      }).toList();

    final IntentionAction detailsAction = JSLinterUtil.createDetailsAction(
      input.getProject(), input.getVirtualFile(), null, output, JavaScriptLanguageIcons.FileTypes.Eslint);
    final JSLinterFileLevelAnnotation annotation = new JSLinterFileLevelAnnotation(error, detailsAction);
    //TODO. Consider removing setShortMessage also. JSLinterEditorNotificationPanel.createNotificationPanel takes the first non-empty line
    annotation.setShortMessage(ContainerUtil.getFirstItem(meaningfulLines));
    return annotation.withIcon(JavaScriptLanguageIcons.FileTypes.Eslint);
  }

  public @Nullable TResult execute(final @NotNull JSLinterInput<EslintState> input) throws LinterExecutionException {
    final VirtualFile fileToLint = input.getVirtualFile();
    if (!fileToLint.isValid() || fileToLint.getParent() == null) {
      return null;
    }
    final Project project = input.getProject();
    final EslintState state = input.getState();

    NodeJsInterpreter nodeInterpreter = NodeJsInterpreterManager.getInstance(project).getInterpreter(true);
    NodePackage resolvedNodePackage = myLanguageService.getNodePackage();
    JSLinterFileLevelAnnotation error = JSLinterUtil.validateInterpreterAndPackage(project, nodeInterpreter,
                                                                                   resolvedNodePackage, EslintUtil.PACKAGE_NAME,
                                                                                   fileToLint);
    if (error != null) {
      throw new LinterExecutionException(error);
    }
    SemVer packageVersion = resolvedNodePackage.getVersion(project);
    if (packageVersion != null && packageVersion.getMajor() < 1) {
      String message = EslintBundle.message("eslint.version.0.is.not.supported.please.upgrade.eslint", packageVersion.getRawVersion());
      throw new LinterExecutionException(new JSLinterFileLevelAnnotation(message));
    }

    final VirtualFile specifiedConfigFile;

    try {
      if (!StringUtil.isEmptyOrSpaces(state.getAdditionalRulesDirPath())) {
        JSLinterUtil.checkPath(state.getAdditionalRulesDirPath(), true, EslintBundle
          .message("eslint.additional.rules.directory.field.name"));
      }

      if (state.isCustomConfigFileUsed()) {
        specifiedConfigFile =
          JSLinterUtil.checkPath(state.getCustomConfigFilePath(), false, EslintBundle.message("eslint.configuration.file.field.name"));
      }
      else {
        specifiedConfigFile = null;
      }
    }
    catch (ExecutionException e) {
      throw new LinterExecutionException(new JSLinterFileLevelAnnotation(e.getMessage()));
    }

    final EslintRequestData requestData = ReadAction.compute(() -> {
      if (project.isDisposed()) {
        return null;
      }
      // project won't be disposed while read-lock acquired
      if (myIsOnTheFly) {
        EslintConfigFileChangeTracker.getInstance(project).startIfNeeded();
      }

      var flatConfigData =
        ESLintFlatConfigData.determineFlatConfig(project, specifiedConfigFile, packageVersion, myLanguageService.getWorkingDirectory());
      VirtualFile eslintIgnoreFile = flatConfigData.getFlatConfigMode() ? null
                                                                        : EslintUtil.lookupIgnoreFile(fileToLint,
                                                                                                      myLanguageService.getWorkingDirectory());
      Collection<VirtualFile> possibleConfigs = determinePossibleConfigs(specifiedConfigFile, flatConfigData, eslintIgnoreFile, fileToLint);

      if (myIsOnTheFly && !EslintUnsavedConfigManager.getInstance(project).requestSaveIfNeeded(possibleConfigs)) {
        // unsaved configs will be saved automatically later and eslint will be restarted
        LOG.debug("ESLint postponed because of unsaved configs");
        return null;
      }
      EslintUtil.FileKind fileKind = EslintUtil.getFileKind(input.getPsiFile());
      if (fileKind == null) {
        LOG.debug(String.format("Ignoring file %s because of unknown kind", input.getPsiFile().getName()));
        return null;
      }

      return new EslintRequestData(fileToLint, fileKind, input.getFileContent(), specifiedConfigFile,
                                   possibleConfigs, eslintIgnoreFile, flatConfigData.getFlatConfigMode());
    });
    return requestData == null ? null : eslint(input, requestData);
  }

  private static Collection<VirtualFile> determinePossibleConfigs(@Nullable VirtualFile specifiedConfigFile,
                                                                   @NotNull ESLintFlatConfigData flatConfigData,
                                                                   @Nullable VirtualFile eslintIgnoreFile,
                                                                   @NotNull VirtualFile fileToLint) {
    if (flatConfigData.getFlatConfigMode()) {
      if (specifiedConfigFile != null && !specifiedConfigFile.equals(fileToLint)) {
        return List.of(specifiedConfigFile);
      }

      VirtualFile flatConfigFile = flatConfigData.getFlatConfigFile();
      if (flatConfigFile != null && !flatConfigFile.equals(fileToLint)) {
        return List.of(flatConfigFile);
      }

      return Collections.emptyList();
    }

    return findPossibleConfigs(specifiedConfigFile, fileToLint, eslintIgnoreFile);
  }

  private static @NotNull Collection<VirtualFile> findPossibleConfigs(@Nullable VirtualFile specifiedConfigFile,
                                                                      @NotNull VirtualFile fileToLint,
                                                                      @Nullable VirtualFile eslintIgnoreFile) {
    final Collection<VirtualFile> configs = new LinkedHashSet<>();
    if (specifiedConfigFile != null) {
      configs.add(specifiedConfigFile);
    }
    else {
      configs.addAll(EslintUtil.findAllConfigsWithPackageJsonUpFileSystem(fileToLint));
    }
    //avoid triggering save on editing eslint config because it triggers external tools and just is unnecessary
    if (EslintUtil.isFlatOrLegacyConfigFile(fileToLint)) {
      configs.remove(fileToLint);
    }
    if (eslintIgnoreFile != null) {
      configs.add(eslintIgnoreFile);
    }
    return configs;
  }
}
