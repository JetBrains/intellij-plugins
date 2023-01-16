// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.CompilerConfigGeneratorRt;
import com.intellij.flex.build.FlexBuildTarget;
import com.intellij.flex.build.FlexBuildTargetType;
import com.intellij.flex.model.JpsFlexCompilerProjectExtension;
import com.intellij.flex.model.JpsFlexProjectLevelCompilerOptionsExtension;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.StopBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlexBuilder extends TargetBuilder<BuildRootDescriptor, FlexBuildTarget> {

  private static final Logger LOG = Logger.getInstance(FlexBuilder.class.getName());
  private JpsBuiltInFlexCompilerHandler myBuiltInCompilerHandler;

  private enum Status {Ok, Failed, Cancelled}

  protected FlexBuilder() {
    super(Collections.singletonList(FlexBuildTargetType.INSTANCE));
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return "Flash Compiler";
  }

  @Override
  public void buildStarted(final CompileContext context) {
    super.buildStarted(context);
    myBuiltInCompilerHandler = new JpsBuiltInFlexCompilerHandler(context.getProjectDescriptor().getProject());
  }

  @Override
  public void buildFinished(final CompileContext context) {
    LOG.assertTrue(myBuiltInCompilerHandler.getActiveCompilationsNumber() == 0,
                   myBuiltInCompilerHandler.getActiveCompilationsNumber() + " Flex compilation(s) are not finished!");
    myBuiltInCompilerHandler.stopCompilerProcess();
    myBuiltInCompilerHandler = null;

    FlexCommonUtils.deleteTempFlexConfigFiles(context.getProjectDescriptor().getProject().getName());

    super.buildFinished(context);
  }

  @Override
  public ExitCode buildTarget(@NotNull final FlexBuildTarget buildTarget,
                              @NotNull final DirtyFilesHolder<BuildRootDescriptor, FlexBuildTarget> holder,
                              @NotNull final BuildOutputConsumer outputConsumer,
                              @NotNull final CompileContext context) throws ProjectBuildException, IOException {
    final Collection<String> dirtyFilePaths = new ArrayList<>();

    holder.processDirtyFiles(new FileProcessor<>() {
      @Override
      public boolean apply(final FlexBuildTarget target, final File file, final BuildRootDescriptor root) throws IOException {
        assert target == buildTarget;
        dirtyFilePaths.add(file.getPath());
        return true;
      }
    });

    if (LOG.isDebugEnabled()) {
      final StringBuilder b = new StringBuilder();
      b.append(buildTarget.getId()).append(", ").append("dirty files: ").append(dirtyFilePaths.size());

      if (dirtyFilePaths.size() < 10) {
        for (String path : dirtyFilePaths) {
          b.append('\n').append(path);
        }
      }

      LOG.debug(b.toString());
    }

    final JpsFlexBuildConfiguration mainBC = buildTarget.getBC();

    final List<JpsFlexBuildConfiguration> bcsToCompile = getAllBCsToCompile(mainBC);

    if (!FlexCommonUtils.isFlexUnitBC(mainBC) && !isFlexmojosBCWithUpdatedConfigFile(mainBC)) {
      if (dirtyFilePaths.isEmpty()) {
        boolean outputFilesExist = true;

        for (JpsFlexBuildConfiguration bc : bcsToCompile) {
          if (!new File(bc.getActualOutputFilePath()).isFile()) {
            outputFilesExist = false;
            LOG.debug("recompile because output file doesn't exist: " + bc.getActualOutputFilePath());
            break;
          }
        }

        if (outputFilesExist) {
          return ExitCode.NOTHING_DONE;
        }
      }
      else if (mainBC.getNature().isApp() && isOnlyWrapperFilesDirty(mainBC, dirtyFilePaths)) {
        LOG.debug("only wrapper files dirty");
        FlexBuilderUtils.performPostCompileActions(context, mainBC, dirtyFilePaths, outputConsumer);
        return ExitCode.OK;
      }
    }

    if (bcsToCompile.isEmpty()) {
      return ExitCode.NOTHING_DONE;
    }

    for (JpsFlexBuildConfiguration bc : bcsToCompile) {
      final Status status = compileBuildConfiguration(context, bc, myBuiltInCompilerHandler);

      switch (status) {
        case Ok:
          outputConsumer.registerOutputFile(new File(mainBC.getActualOutputFilePath()), dirtyFilePaths);
          FlexBuilderUtils.performPostCompileActions(context, bc, dirtyFilePaths, outputConsumer);
          context.processMessage(
            new CompilerMessage(FlexBuilderUtils.getCompilerName(bc), BuildMessage.Kind.INFO,
                                FlexCommonBundle.message("compilation.successful")));
          break;

        case Failed:
          final String message = bc.getOutputType() == OutputType.Application
                                 ? FlexCommonBundle.message("compilation.failed")
                                 : FlexCommonBundle.message("compilation.failed.dependent.will.be.skipped");
          context.processMessage(new CompilerMessage(FlexBuilderUtils.getCompilerName(bc), BuildMessage.Kind.INFO, message));

          throw new StopBuildException();

        case Cancelled:
          context.processMessage(
            new CompilerMessage(FlexBuilderUtils.getCompilerName(bc), BuildMessage.Kind.INFO,
                                FlexCommonBundle.message("compilation.cancelled")));
          return ExitCode.OK;
      }
    }
    return ExitCode.OK;
  }

  /**
   * This is a hacky workaround, needed because IDEA doesn't report files changed under .idea folder as dirty
   */
  private static boolean isFlexmojosBCWithUpdatedConfigFile(final JpsFlexBuildConfiguration bc) {
    final String configFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!configFilePath.contains("/.idea/flexmojos/")) {
      return false;
    }

    final File configFile = new File(configFilePath);
    final File outputFile = new File(bc.getActualOutputFilePath());
    return configFile.lastModified() > outputFile.lastModified();
  }

  private static boolean isOnlyWrapperFilesDirty(final JpsFlexBuildConfiguration bc, final Collection<String> dirtyFilePaths) {
    if (bc.getTargetPlatform() == TargetPlatform.Web && bc.isUseHtmlWrapper() && !bc.getWrapperTemplatePath().isEmpty()) {
      for (String dirtyFilePath : dirtyFilePaths) {
        if (FileUtil.isAncestor(bc.getWrapperTemplatePath(), dirtyFilePath, true)) {
          continue;
        }
        return false;
      }
      return true;
    }

    return false;
  }

  private static List<JpsFlexBuildConfiguration> getAllBCsToCompile(final JpsFlexBuildConfiguration bc) {
    final List<JpsFlexBuildConfiguration> result =
      new ArrayList<>(1 + bc.getRLMs().size() + bc.getCssFilesToCompile().size());

    result.add(bc);

    if (FlexCommonUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
      for (JpsFlexBuildConfiguration.RLMInfo rlm : bc.getRLMs()) {
        result.add(createRlmBC(bc, rlm));
      }

      for (String cssPath : bc.getCssFilesToCompile()) {
        if (new File(cssPath).isFile()) {
          result.add(createCssBC(bc, cssPath));
        }
      }
    }

    return result;
  }

  private static JpsFlexBuildConfiguration createRlmBC(final JpsFlexBuildConfiguration mainBC,
                                                       final JpsFlexBuildConfiguration.RLMInfo rlm) {
    final JpsFlexBuildConfiguration rlmBC = mainBC.getModule().getProperties().createTemporaryCopyForCompilation(mainBC);

    rlmBC.setOutputType(OutputType.RuntimeLoadedModule);
    rlmBC.setOptimizeFor(rlm.OPTIMIZE ? mainBC.getName() : ""); // any not empty string means that need to optimize

    final String subdir = PathUtilRt.getParentPath(rlm.OUTPUT_FILE);
    final String outputFileName = PathUtilRt.getFileName(rlm.OUTPUT_FILE);

    rlmBC.setMainClass(rlm.MAIN_CLASS);
    rlmBC.setOutputFileName(outputFileName);

    if (!subdir.isEmpty()) {
      final String outputFolder = PathUtilRt.getParentPath(mainBC.getActualOutputFilePath());
      rlmBC.setOutputFolder(outputFolder + "/" + subdir);
    }

    rlmBC.setUseHtmlWrapper(false);

    rlmBC.setRLMs(Collections.emptyList());
    rlmBC.setCssFilesToCompile(Collections.emptyList());

    final JpsFlexCompilerOptions compilerOptions = rlmBC.getCompilerOptions();
    compilerOptions.setResourceFilesMode(JpsFlexCompilerOptions.ResourceFilesMode.None);

    String additionalOptions = compilerOptions.getAdditionalOptions();
    additionalOptions = FlexCommonUtils.removeOptions(additionalOptions, "link-report");
    additionalOptions = FlexCommonUtils.fixSizeReportOption(additionalOptions, StringUtil.getShortName(rlmBC.getMainClass()));
    compilerOptions.setAdditionalOptions(additionalOptions);

    return rlmBC;
  }

  private static JpsFlexBuildConfiguration createCssBC(final JpsFlexBuildConfiguration mainBC, final String cssPath) {
    final JpsFlexBuildConfiguration cssBC = mainBC.getModule().getProperties().createTemporaryCopyForCompilation(mainBC);
    cssBC.setOutputType(OutputType.Application);

    cssBC.setMainClass(cssPath);
    cssBC.setOutputFileName(FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(cssPath)) + ".swf");

    final String cssDirPath = PathUtilRt.getParentPath(cssPath);
    String relativeToRoot = FlexCommonUtils.getPathRelativeToSourceRoot(mainBC.getModule(), cssDirPath);
    if (relativeToRoot == null) {
      relativeToRoot = FlexCommonUtils.getPathRelativeToContentRoot(mainBC.getModule(), cssDirPath);
    }

    if (!StringUtil.isEmpty(relativeToRoot)) {
      final String outputFolder = PathUtilRt.getParentPath(mainBC.getActualOutputFilePath());
      cssBC.setOutputFolder(outputFolder + "/" + relativeToRoot);
    }

    cssBC.setUseHtmlWrapper(false);
    cssBC.setRLMs(Collections.emptyList());
    cssBC.setCssFilesToCompile(Collections.emptyList());

    final JpsFlexCompilerOptions compilerOptions = cssBC.getCompilerOptions();
    compilerOptions.setResourceFilesMode(JpsFlexCompilerOptions.ResourceFilesMode.None);

    String additionalOptions = compilerOptions.getAdditionalOptions();
    additionalOptions = FlexCommonUtils.removeOptions(additionalOptions, "link-report");
    additionalOptions =
      FlexCommonUtils.fixSizeReportOption(additionalOptions, FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(cssPath)));
    compilerOptions.setAdditionalOptions(additionalOptions);

    return cssBC;
  }

  private static Status compileBuildConfiguration(final CompileContext context,
                                                  final JpsFlexBuildConfiguration bc,
                                                  final JpsBuiltInFlexCompilerHandler builtInCompilerHandler) {
    setProgressMessage(context, bc);

    final String compilerName = FlexBuilderUtils.getCompilerName(bc);

    try {
      final List<File> configFiles = createConfigFiles(bc, context.getProjectDescriptor());
      final String outputFilePath = bc.getActualOutputFilePath();

      if (!ensureCanCreateFile(new File(outputFilePath))) {
        context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR,
                                                   FlexCommonBundle.message("failed.to.create.file", bc.getActualOutputFilePath())));
        return Status.Failed;
      }

      return doCompile(context, bc, configFiles, compilerName, builtInCompilerHandler);
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.getMessage()));
      return Status.Failed;
    }
  }

  private static boolean ensureCanCreateFile(@NotNull File file) {
    final int maxAttempts = 3; // FileUtil.ensureCanCreateFile() may return false because of race conditions

    for (int i = 0; i < maxAttempts; i++) {
      if (FileUtil.ensureCanCreateFile(file)) return true;

      try {
        //noinspection BusyWait
        Thread.sleep(10);
      }
      catch (InterruptedException ignore) {/**/}
    }

    return false;
  }

  private static void setProgressMessage(final CompileContext context, final JpsFlexBuildConfiguration bc) {
    String postfix = bc.isTempBCForCompilation() ? " - " + FlexCommonUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(bc.getModule().getName())) postfix += " (module " + bc.getModule().getName() + ")";
    context.processMessage(new ProgressMessage(FlexCommonBundle.message("compiling", bc.getName() + postfix)));
  }

  private static List<File> createConfigFiles(final JpsFlexBuildConfiguration bc,
                                              final ProjectDescriptor projectDescriptor) throws IOException {
    final ArrayList<File> configFiles = new ArrayList<>(2);
    configFiles.add(CompilerConfigGeneratorRt.getOrCreateConfigFile(bc, projectDescriptor));

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!bc.isTempBCForCompilation() && !additionalConfigFilePath.isEmpty()) {
      final File additionalConfigFile = new File(additionalConfigFilePath);
      if (!additionalConfigFile.isFile()) {
        throw new IOException(
          FlexCommonBundle.message("additional.config.file.not.found.for.bc.0.of.module.1", additionalConfigFilePath, bc.getName(),
                                   bc.getModule().getName()));
      }
      configFiles.add(additionalConfigFile);
    }

    return configFiles;
  }

  private static Status doCompile(final CompileContext context,
                                  final JpsFlexBuildConfiguration bc,
                                  final List<File> configFiles,
                                  final String compilerName,
                                  final JpsBuiltInFlexCompilerHandler builtInCompilerHandler) {
    final boolean app = bc.getOutputType() != OutputType.Library;
    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    final boolean asc20 = bc.isPureAs() &&
                          FlexCommonUtils.containsASC20(sdk.getHomePath()) &&
                          (JpsFlexCompilerProjectExtension.getInstance(bc.getModule().getProject()).PREFER_ASC_20 ||
                           FlexCommonUtils.isAirSdkWithoutFlex(sdk));
    final boolean builtIn = !asc20 &&
                            JpsFlexCompilerProjectExtension.getInstance(bc.getModule().getProject()).USE_BUILT_IN_COMPILER &&
                            builtInCompilerHandler.canBeUsedForSdk(sdk.getHomePath());

    if (builtIn) {
      return doCompileWithBuiltInCompiler(context, bc, configFiles, compilerName, builtInCompilerHandler);
    }

    final List<String> compilerCommand = asc20 ? getASC20Command(bc.getModule().getProject(), sdk, app)
                                               : getMxmlcCompcCommand(bc.getModule().getProject(), sdk, app);
    final List<String> command = buildCommand(compilerCommand, configFiles, bc);

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexCommonUtils.getFlexCompilerWorkDirPath(bc.getModule().getProject())));

    try {
      final Process process = processBuilder.start();

      final FlexCompilerProcessHandler processHandler =
        new FlexCompilerProcessHandler(context, process, asc20, compilerName, StringUtil.join(command, " "));
      processHandler.startNotify();
      processHandler.waitFor();

      return processHandler.isCancelled() ? Status.Cancelled
                                          : processHandler.isCompilationFailed()
                                            ? Status.Failed
                                            : Status.Ok;
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.getMessage()));
      return Status.Failed;
    }
  }

  private static Status doCompileWithBuiltInCompiler(final CompileContext context,
                                                     final JpsFlexBuildConfiguration bc,
                                                     final List<File> configFiles,
                                                     final String compilerName,
                                                     final JpsBuiltInFlexCompilerHandler builtInCompilerHandler) {
    try {
      builtInCompilerHandler.startCompilerIfNeeded(bc.getSdk(), context, compilerName);
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.toString()));
      return Status.Failed;
    }

    final List<String> mxmlcOrCompc = Collections.singletonList(bc.getOutputType() == OutputType.Library ? "compc" : "mxmlc");
    final List<String> command = buildCommand(mxmlcOrCompc, configFiles, bc);
    final String plainCommand = StringUtil.join(command,
                                                s -> s.indexOf(' ') >= 0 && !(s.startsWith("\"") && s.endsWith("\"")) ? '\"' + s + '\"' : s, " ");

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.INFO, plainCommand));

    final BuiltInCompilerListener listener = new BuiltInCompilerListener(context, compilerName, () -> semaphore.up());

    builtInCompilerHandler.sendCompilationCommand(plainCommand, listener);

    semaphore.waitFor();
    builtInCompilerHandler.removeListener(listener);

    return listener.isCompilationCancelled() ? Status.Cancelled
                                             : listener.isCompilationFailed()
                                               ? Status.Failed
                                               : Status.Ok;
  }

  private static List<String> getASC20Command(final JpsProject project, final JpsSdk<?> flexSdk, final boolean isApp) {
    final String mainClass = isApp ? "com.adobe.flash.compiler.clients.MXMLC" : "com.adobe.flash.compiler.clients.COMPC";

    final String additionalClasspath = flexSdk.getSdkType() == JpsFlexmojosSdkType.INSTANCE
                                       ? null
                                       : FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compiler.jar");

    return FlexCommonUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, mainClass);
  }

  private static List<String> getMxmlcCompcCommand(final JpsProject project, final JpsSdk<?> flexSdk, final boolean isApp) {
    final String mainClass = isApp ? StringUtil.compareVersionNumbers(flexSdk.getVersionString(), "4") >= 0 ? "flex2.tools.Mxmlc"
                                                                                                            : "flex2.tools.Compiler"
                                   : "flex2.tools.Compc";

    String additionalClasspath = FileUtil.toSystemDependentName(FlexCommonUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));

    if (flexSdk.getSdkType() == JpsFlexSdkType.INSTANCE) {
      additionalClasspath += File.pathSeparator + FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compc.jar");
    }

    return FlexCommonUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, mainClass);
  }

  private static List<String> buildCommand(final List<String> compilerCommand,
                                           final List<File> configFiles,
                                           final JpsFlexBuildConfiguration bc) {
    final List<String> command = new ArrayList<>(compilerCommand);
    for (File configFile : configFiles) {
      command.add("-load-config=" + configFile.getPath());
    }

    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(),
                         JpsFlexProjectLevelCompilerOptionsExtension
                           .getProjectLevelCompilerOptions(bc.getModule().getProject()).getAdditionalOptions());
    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(),
                         bc.getModule().getProperties().getModuleLevelCompilerOptions().getAdditionalOptions());
    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(), bc.getCompilerOptions().getAdditionalOptions());

    return command;
  }

  private static void addAdditionalOptions(List<String> command, JpsModule module, String sdkHome, String additionalOptions) {
    if (!StringUtil.isEmpty(additionalOptions)) {
      // TODO handle -option="path with spaces"
      for (final String s : StringUtil.split(additionalOptions, " ")) {
        command.add(FlexCommonUtils.replacePathMacros(s, module, sdkHome));
      }
    }
  }

  private static class BuiltInCompilerListener extends CompilerMessageHandlerBase implements JpsBuiltInFlexCompilerHandler.Listener {
    private final Runnable myOnCompilationFinishedRunnable;

    BuiltInCompilerListener(final CompileContext context, final String compilerName, final Runnable onCompilationFinishedRunnable) {
      super(context, false, compilerName);
      myOnCompilationFinishedRunnable = onCompilationFinishedRunnable;
    }

    @Override
    public void textAvailable(final String text) {
      handleText(text);
    }

    @Override
    public void compilationFinished() {
      registerCompilationFinished();
      myOnCompilationFinishedRunnable.run();
    }

    @Override
    protected void onCancelled() {
      compilationFinished();
    }
  }
}
