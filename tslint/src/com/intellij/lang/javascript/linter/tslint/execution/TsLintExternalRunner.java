package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.idea.RareLogger;
import com.intellij.javascript.nodejs.NodePackageVersionUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.config.TsLintBinFileVersionManager;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintConfigFileChangeTracker;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.Producer;
import com.intellij.util.PsiErrorElementUtil;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.text.SemVer;
import com.intellij.webcore.util.CommandLineUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public class TsLintExternalRunner {
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);
  private final static String INTERNAL_ERROR =
    "Internal TSLint problem. Please report the problem and attach the log using \"Help | Show Log in "
    + ShowFilePathAction.getFileManagerName() + "\"";
  public static final int TIMEOUT_IN_MILLISECONDS = (int)TimeUnit.SECONDS.toMillis(10);
  private static final Logger RARE_LOGGER = RareLogger.wrap(LOG, false);
  private static final SemVer VERSION_2_4_0 = new SemVer("2.4.0", 2, 4, 0);
  public static final String ERROR_CANNOT_FIND_MODULE_TYPESCRIPT = "Error: Cannot find module 'typescript'";

  private final JSLinterInput<TsLintState> myInputInfo;
  private final FilesMirror myCodeFilesMirror;
  private final FilesMirror myConfigFilesMirror;
  private final TsLintBinFileVersionManager myBinFileVersionManager;
  private final Project myProject;
  private final List<Producer<JSLinterAnnotationResult<TsLintState>>> mySteps;
  private File myActualConfigFile;
  private File myActualCodeFile;
  private VirtualFile myConfigVirtualFile;
  private VirtualFile myCodeVirtualFile;
  private File myNodeFile;
  private File myPackageDir;

  private File myTsLintFile;
  private SemVer myTsLintVersion;
  private boolean myFix;
  private boolean mySkip;

  public TsLintExternalRunner(@NotNull JSLinterInput<TsLintState> inputInfo,
                              @NotNull FilesMirror codeFilesMirror,
                              @NotNull FilesMirror configFilesMirror,
                              @NotNull TsLintBinFileVersionManager binFileVersionManager,
                              @NotNull Project project) {
    myInputInfo = inputInfo;
    myCodeFilesMirror = codeFilesMirror;
    myConfigFilesMirror = configFilesMirror;
    myBinFileVersionManager = binFileVersionManager;
    myProject = project;
    mySteps = new ArrayList<>();
    mySteps.add(checkExePath());
    mySteps.add(checkTargetVirtualFile());
    mySteps.add(checkConfigPath());
    mySteps.add(checkIfTargetFileChanged());
    mySteps.add(runExternalProcess());
  }

  public JSLinterAnnotationResult<TsLintState> execute() {
    for (Producer<JSLinterAnnotationResult<TsLintState>> step : mySteps) {
      final JSLinterAnnotationResult<TsLintState> result = step.produce();
      if (result != null) return result;
      if (mySkip) return null;
    }
    return null;
  }

  private Producer<JSLinterAnnotationResult<TsLintState>> runExternalProcess() {
    return () -> {
      final File workingDirectory = myActualCodeFile.getParentFile();
      if (workingDirectory == null) {
        LOG.debug("Skipped TSLint file analysis: can not find working directory for file: " + myActualCodeFile.getPath());
        mySkip = true;
        return null;
      }
      // we are ready to start tslint => have found some config file and should start tracking it
      TsLintConfigFileChangeTracker.getInstance(myProject).startIfNeeded();

      final GeneralCommandLine commandLine = createCommandLine(workingDirectory);
      CapturingProcessHandler processHandler;
      try {
        processHandler = new CapturingProcessHandler(commandLine);
      }
      catch (ExecutionException e) {
        return createError("Can not start TSLint process: " + e.getMessage());
      }
      boolean zeroBasedRowCol = TsLintOutputJsonParser.isVersionZeroBased(myTsLintVersion);
      final TsLintProcessListener parser = new TsLintProcessListener(myActualCodeFile.getAbsolutePath(), zeroBasedRowCol);
      processHandler.addProcessListener(parser);
      final ProcessOutput processOutput = processHandler.runProcess(TIMEOUT_IN_MILLISECONDS);
      if (processOutput.isTimeout()) {
        return JSLinterAnnotationResult.create(myInputInfo, new JSLinterFileLevelAnnotation("Process timed out after "
                                                                                            +
                                                                                            DateFormatUtil
                                                                                              .formatDuration(TIMEOUT_IN_MILLISECONDS)),
                                               myConfigVirtualFile);
      }
      parser.process();

      final JSLinterErrorBase error = parser.getGlobalError();
      if (error != null) {
        String message = error.getDescription();
        if (isInnerProblem(error)) {
          message = INTERNAL_ERROR;
          final String predefinedError = parsePredefinedErrorText(error);
          if (predefinedError != null) {
            message = predefinedError;
          }
          else if (PsiErrorElementUtil.hasErrors(myProject, myCodeVirtualFile)) {
            // just ignore everything: https://github.com/palantir/tslint/issues/647
            return JSLinterAnnotationResult.createLinterResult(myInputInfo, Collections.emptyList(), myConfigVirtualFile);
          }
        }
        final JSLinterFileLevelAnnotation annotation = new JSLinterFileLevelAnnotation(message,
                                                                                       JSLinterUtil
                                                                                         .createDetailsAction(myProject, myCodeVirtualFile,
                                                                                                              commandLine, processOutput,
                                                                                                              null));
        return JSLinterAnnotationResult.create(myInputInfo, annotation, myConfigVirtualFile);
      }
      return JSLinterAnnotationResult.createLinterResult(myInputInfo, parser.getErrors(), myConfigVirtualFile);
    };
  }

  private static String parsePredefinedErrorText(JSLinterErrorBase error) {
    String description = error.getDescription();
    if (StringUtil.isEmptyOrSpaces(description)) return null;
    description = description.replace('"', '\'');
    if (description.contains(ERROR_CANNOT_FIND_MODULE_TYPESCRIPT)) return ERROR_CANNOT_FIND_MODULE_TYPESCRIPT;
    final String errorMessage = isStackTrace(description);
    if (errorMessage != null && errorMessage.startsWith("Error: Could not find custom rule directory")) return errorMessage;
    return null;
  }

  private boolean isInnerProblem(JSLinterErrorBase error) {
    final String description = error.getDescription();
    if (isStackTrace(description) != null) {
      final String version = myTsLintVersion == null ? "unknown" : myTsLintVersion.getRawVersion();
      RARE_LOGGER.info("TsLint inner error. TsLint version: " + version + "\n\n" + description);
      return true;
    }
    return false;
  }

  private static String isStackTrace(final String description) {
    final String[] lines = StringUtil.splitByLines(description);
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      if (line.contains("Error") && i < (lines.length - 1)) {
        if (lines[i + 1].trim().startsWith("at ")) {
          return lines[i];
        }
      }
    }
    return null;
  }

  private GeneralCommandLine createCommandLine(File workingDir) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.withCharset(StandardCharsets.UTF_8);
    CommandLineUtil.configureShellEnvironment(commandLine, true, Collections.emptyMap());
    commandLine.withWorkDirectory(workingDir);
    commandLine.setExePath(myNodeFile.getAbsolutePath());

    commandLine.addParameter(myTsLintFile.getAbsolutePath());

    commandLine.addParameters("-t json");
    if (myActualConfigFile != null) {
      commandLine.addParameters("-c", myActualConfigFile.getAbsolutePath());
    }
    final String rulesDirectory = myInputInfo.getState().getRulesDirectory();
    if (!StringUtil.isEmptyOrSpaces(rulesDirectory)) {
      commandLine.addParameters("-r", rulesDirectory);
    }
    boolean passOldFileFlag = myTsLintVersion != null && myTsLintVersion.compareTo(VERSION_2_4_0) < 0;
    if (passOldFileFlag) {
      commandLine.addParameter("-f");
    }
    commandLine.addParameters(myActualCodeFile.getAbsolutePath());
    return commandLine;
  }

  private Producer<JSLinterAnnotationResult<TsLintState>> checkIfTargetFileChanged() {
    return () -> {
      final PsiFile psiFile = myInputInfo.getPsiFile();
      myActualCodeFile = myCodeFilesMirror.getOrCreateFileWithActualContent(myCodeVirtualFile, myInputInfo.getFileContent());
      if (myActualCodeFile == null) {
        LOG.debug("Skipped TSLint file analysis: can not mirror target file in temp directory: " + psiFile.getName());
        mySkip = true;
      }
      return null;
    };
  }

  private Producer<JSLinterAnnotationResult<TsLintState>> checkConfigPath() {
    return () -> {
      final TsLintState state = myInputInfo.getState();
      final TsLintConfigFileSearcher searcher = new TsLintConfigFileSearcher();
      String error = searcher.validate(state);
      if (error != null) {
        return createError(error);
      }

      VirtualFile config = searcher.getConfig(state, myInputInfo.getPsiFile());
      if (config == null) {
        return createError("Configuration file for TSLint is not found");
      }

      myConfigVirtualFile = config;
      myActualConfigFile = myConfigFilesMirror.getOrCreateFileWithActualContent(myConfigVirtualFile, null);
      if (myActualConfigFile == null) {
        LOG.debug("Skipped TSLint file analysis: can not mirror config file in temp directory: " + myConfigVirtualFile.getPath());
        mySkip = true;
      }

      return null;
    };
  }

  private Producer<JSLinterAnnotationResult<TsLintState>> checkTargetVirtualFile() {
    return () -> {
      final PsiFile psiFile = myInputInfo.getPsiFile();
      myCodeVirtualFile = psiFile.getVirtualFile();
      if (myCodeVirtualFile == null || !myCodeVirtualFile.isValid()) {
        LOG.debug("Skipped TSLint file analysis: can not load target file as virtual file: " + psiFile.getName());
        mySkip = true;
        return null;
      }
      return null;
    };
  }

  private Producer<JSLinterAnnotationResult<TsLintState>> checkExePath() {
    return () -> {
      final TsLintState state = myInputInfo.getState();
      final NodeJsInterpreter interpreter = state.getInterpreterRef().resolve(myProject);
      final String nodePath = interpreter == null ? "" : interpreter.getPresentableName();
      if (StringUtil.isEmptyOrSpaces(nodePath)) {
        return createError("Node interpreter file is not specified");
      }
      myNodeFile = new File(nodePath);
      if (!myNodeFile.isFile() || !myNodeFile.canExecute()) {
        return createError("Node interpreter file ('" + nodePath + "') is not found or the file can not be executed");
      }
      final String packagePath = state.getPackagePath();
      if (StringUtil.isEmptyOrSpaces(packagePath)) {
        return createError(JSLinterUtil.getLinterPackageMissingError(myProject, packagePath, "TSLint"));
      }
      myPackageDir = new File(packagePath);
      if (myPackageDir.isDirectory()) {
        if (!myPackageDir.isAbsolute()) {
          return createError(JSLinterUtil.getLinterPackageMissingError(myProject, packagePath, "TSLint"));
        }
        myTsLintFile = new File(myPackageDir, "bin" + File.separator + "tslint");
        myTsLintVersion = NodePackageVersionUtil.getPackageVersion(myPackageDir);
      }
      else {
        myTsLintFile = myPackageDir;
        try {
          myTsLintVersion = myBinFileVersionManager.getVersion(myNodeFile.getAbsolutePath(),
                                                               myTsLintFile,
                                                               Collections.singletonList("--version"),
                                                               10000);
        }
        catch (ExecutionException e) {
          LOG.info("Cannot fetch version of " + myTsLintFile.getAbsolutePath(), e);
        }
      }
      if (!myTsLintFile.exists()) {
        if (myPackageDir.exists() && myTsLintFile == myPackageDir) {
          return createError("Provided TSLint path does not exist");
        }
        else {
          return createError(JSLinterUtil.getLinterPackageMissingError(myProject, packagePath, "TSLint"));
        }
      }
      return null;
    };
  }

  private JSLinterAnnotationResult<TsLintState> createError(JSLinterFileLevelAnnotation error) {
    return JSLinterAnnotationResult.create(myInputInfo, error, myConfigVirtualFile);
  }

  private JSLinterAnnotationResult<TsLintState> createError(String error) {
    return JSLinterAnnotationResult.create(myInputInfo, new JSLinterFileLevelAnnotation(error), myConfigVirtualFile);
  }
}
