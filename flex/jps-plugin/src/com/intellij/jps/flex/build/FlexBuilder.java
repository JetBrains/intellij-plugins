package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.CompilerConfigGeneratorRt;
import com.intellij.flex.build.FlexBuildTarget;
import com.intellij.flex.build.FlexBuildTargetType;
import com.intellij.flex.model.JpsFlexProjectLevelCompilerOptionsExtension;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
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

  private enum Status {Ok, Failed, Cancelled}

  protected FlexBuilder() {
    super(Collections.singletonList(FlexBuildTargetType.INSTANCE));
  }

  @NotNull
  public String getPresentableName() {
    return "Flash Compiler";
  }

  public void build(@NotNull final FlexBuildTarget buildTarget,
                    @NotNull final DirtyFilesHolder<BuildRootDescriptor, FlexBuildTarget> holder,
                    @NotNull final BuildOutputConsumer outputConsumer,
                    @NotNull final CompileContext context) throws ProjectBuildException, IOException {
    final Collection<String> dirtyFilePaths = new ArrayList<String>();

    holder.processDirtyFiles(new FileProcessor<BuildRootDescriptor, FlexBuildTarget>() {
      public boolean apply(final FlexBuildTarget target, final File file, final BuildRootDescriptor root) throws IOException {
        assert target == buildTarget;
        dirtyFilePaths.add(file.getPath());
        return true;
      }
    });

    final JpsFlexBuildConfiguration bc = buildTarget.getBC();

    if (dirtyFilePaths.isEmpty() && new File(bc.getActualOutputFilePath()).isFile()) {
      return;
    }

    String postfix = bc.isTempBCForCompilation() ? " - " + FlexCommonUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(bc.getModule().getName())) postfix += " (module " + bc.getModule().getName() + ")";
    final String compilerName = "[" + bc.getName() + postfix + "]";

    switch (compileBuildConfiguration(context, bc, compilerName)) {
      case Ok:
        context
          .processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.INFO, FlexCommonBundle.message("compilation.successful")));

        outputConsumer.registerOutputFile(bc.getActualOutputFilePath(), dirtyFilePaths);
        break;
      case Failed:
        final String message = bc.getOutputType() == OutputType.Application
                               ? FlexCommonBundle.message("compilation.failed")
                               : FlexCommonBundle.message("compilation.failed.dependent.will.be.skipped");
        context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.INFO, message));

        throw new ProjectBuildException();
      case Cancelled:
        context
          .processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.INFO, FlexCommonBundle.message("compilation.cancelled")));
        break;
    }
  }

  public static Status compileBuildConfiguration(final CompileContext context,
                                                 final JpsFlexBuildConfiguration bc,
                                                 final String compilerName) {
    try {
      final List<File> configFiles = createConfigFiles(bc);
      final String outputFilePath = bc.getActualOutputFilePath();

      if (!FileUtil.ensureCanCreateFile(new File(outputFilePath))) {
        context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR,
                                                   FlexCommonBundle.message("failed.to.create.file", bc.getActualOutputFilePath())));
        return Status.Failed;
      }

      return doCompile(context, bc, configFiles, compilerName);
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.getMessage()));
      return Status.Failed;
    }
  }

  private static List<File> createConfigFiles(final JpsFlexBuildConfiguration bc) throws IOException {
    final ArrayList<File> configFiles = new ArrayList<File>(2);
    configFiles.add(CompilerConfigGeneratorRt.getOrCreateConfigFile(bc));

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
                                  final String compilerName) {
    final boolean app = bc.getOutputType() != OutputType.Library;
    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    final List<String> compilerCommand = getMxmlcCompcCommand(bc.getModule().getProject(), sdk, app);
    final List<String> command = buildCommand(compilerCommand, configFiles, bc);

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexCommonUtils.getFlexCompilerWorkDirPath(bc.getModule().getProject())));

    try {
      final Process process = processBuilder.start();

      final FlexCompilerProcessHandler processHandler =
        new FlexCompilerProcessHandler(context, process, compilerName, StringUtil.join(command, " "));
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
    final List<String> command = new ArrayList<String>(compilerCommand);
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

  private static void addAdditionalOptions(final List<String> command,
                                           final JpsModule module,
                                           final String sdkHome,
                                           final String additionalOptions) {
    if (!StringUtil.isEmpty(additionalOptions)) {
      // TODO handle -option="path with spaces"
      for (final String s : StringUtil.split(additionalOptions, " ")) {
        command.add(FlexCommonUtils.replacePathMacros(s, module, sdkHome));
      }
    }
  }
}
