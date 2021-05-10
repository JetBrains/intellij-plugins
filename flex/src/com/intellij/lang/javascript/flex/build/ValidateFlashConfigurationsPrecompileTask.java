// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.*;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.navigation.Place;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ValidateFlashConfigurationsPrecompileTask implements CompileTask {

  private static final String FLASH_COMPILER_GROUP_ID = "Flash Compiler";

  private boolean myParallelCompilationSuggested = false;

  static Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> getProblems(final CompileScope scope, final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems =
      new ArrayList<>();

    for (final Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      final Module module = moduleAndBC.first;
      final FlexBuildConfiguration bc = moduleAndBC.second;

      final Consumer<FlashProjectStructureProblem> errorConsumer = problem -> problems.add(Trinity.create(module, bc, problem));

      checkConfiguration(module, bc, false, errorConsumer);

      final RunConfiguration runConfig = CompileStepBeforeRun.getRunConfiguration(scope);
      if (bc.getNature().isApp() && runConfig instanceof FlashRunConfiguration) {
        final FlashRunnerParameters params = ((FlashRunConfiguration)runConfig).getRunnerParameters();
        if (module.getName().equals(params.getModuleName()) && bc.getName().equals(params.getBCName())) {
          if (bc.getNature().isDesktopPlatform()) {
            FlashRunnerParameters.checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getAirDesktopPackagingOptions(), errorConsumer, false, "does not matter");
          }
          else if (bc.getNature().isMobilePlatform()) {
            switch (params.getMobileRunTarget()) {
              case Emulator:
                switch (params.getAppDescriptorForEmulator()) {
                  case Android:
                    FlashRunnerParameters.checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getAndroidPackagingOptions(), errorConsumer, false,
                                                                            "does not matter");
                    break;
                  case IOS:
                    FlashRunnerParameters.checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getIosPackagingOptions(), errorConsumer, false,
                                                                            "does not matter");
                    break;
                }
                break;
              case AndroidDevice:
                checkPackagingOptions(module, bc.getSdk(), bc.getAndroidPackagingOptions(), false,
                                      PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
                break;
              case iOSSimulator:
                checkPackagingOptions(module, bc.getSdk(), bc.getIosPackagingOptions(), true,
                                      PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
                break;
              case iOSDevice:
                checkPackagingOptions(module, bc.getSdk(), bc.getIosPackagingOptions(), false,
                                      PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
                break;
            }
          }
        }
      }
    }

    checkSimilarOutputFiles(modulesAndBCsToCompile,
                            trinity -> problems.add(trinity));
    return problems;
  }

  private static boolean checkSimilarOutputFiles(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile,
                                                 final Consumer<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> errorConsumer) {

    final Map<String, Pair<Module, FlexBuildConfiguration>> outputPathToModuleAndBC =
      new THashMap<>();
    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      final FlexBuildConfiguration bc = moduleAndBC.second;
      final String outputFilePath = bc.getActualOutputFilePath();
      checkOutputPathUnique(outputFilePath, moduleAndBC, outputPathToModuleAndBC, errorConsumer);
    }
    return true;
  }

  private static void checkOutputPathUnique(final String outputPath,
                                            final Pair<Module, FlexBuildConfiguration> moduleAndBC,
                                            final Map<String, Pair<Module, FlexBuildConfiguration>> outputPathToModuleAndBC,
                                            final Consumer<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> errorConsumer) {
    final String caseAwarePath = SystemInfo.isFileSystemCaseSensitive ? outputPath : StringUtil.toLowerCase(outputPath);

    final Pair<Module, FlexBuildConfiguration> existing = outputPathToModuleAndBC.put(caseAwarePath, moduleAndBC);
    if (existing != null) {
      final String message = FlexBundle.message("same.output.files", existing.second.getName(), existing.first.getName(),
                                                FileUtil.toSystemDependentName(outputPath));
      errorConsumer.consume(Trinity.create(moduleAndBC.first, moduleAndBC.second, FlashProjectStructureProblem
        .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, moduleAndBC.second.getName(), message,
                                    FlexBCConfigurable.Location.OutputFileName)));
    }
  }

  public static void checkConfiguration(final Module module,
                                        final FlexBuildConfiguration bc,
                                        final boolean checkPackaging,
                                        final Consumer<FlashProjectStructureProblem> errorConsumer) {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      errorConsumer.consume(FlashProjectStructureProblem.createDependenciesProblem(ProjectStructureProblemType.Severity.ERROR, FlexBundle.message("sdk.not.set"),
                                                                                   DependenciesConfigurable.Location.SDK));
    }

    if (sdk != null) {
      String version = sdk.getVersionString();
      if (FlexSdkUtils.isAirSdkWithoutFlex(sdk)) {
        version = version.substring(FlexCommonUtils.AIR_SDK_VERSION_PREFIX.length());
      }

      if (StringUtil.compareVersionNumbers(version, "0") < 0 || StringUtil.compareVersionNumbers(version, "100") > 0) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createDependenciesProblem(ProjectStructureProblemType.Severity.ERROR, FlexBundle.message("sdk.version.unknown", sdk.getName()),
                                                           DependenciesConfigurable.Location.SDK));
      }

      if (FlexSdkUtils.isAirSdkWithoutFlex(sdk) && !bc.isPureAs()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                            FlexBundle.message("air.sdk.requires.pure.as", sdk.getName()),
                                                            FlexBCConfigurable.Location.Nature));
      }
    }

    InfoFromConfigFile info = InfoFromConfigFile.DEFAULT;

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null || additionalConfigFile.isDirectory()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createCompilerOptionsProblem(ProjectStructureProblemType.Severity.ERROR, FlexBundle
                                  .message("additional.config.file.not.found", FileUtil.toSystemDependentName(additionalConfigFilePath)),
                                                              CompilerOptionsConfigurable.Location.AdditionalConfigFile));
      }
      if (!bc.isTempBCForCompilation()) {
        info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(additionalConfigFilePath);
      }
    }

    final BuildConfigurationNature nature = bc.getNature();

    if (!nature.isLib() && info.getMainClass(module) == null && !bc.isTempBCForCompilation()) {
      if (bc.getMainClass().isEmpty()) {
        errorConsumer
          .consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                                           FlexBundle.message("main.class.not.set"),
                                                                           FlexBCConfigurable.Location.MainClass));
      }
      else {
        if (FlexUtils.getPathToMainClassFile(bc.getMainClass(), module).isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                              FlexBundle.message("main.class.not.found", bc.getMainClass()),
                                                              FlexBCConfigurable.Location.MainClass));
        }
      }
    }

    if (info.getOutputFileName() == null && info.getOutputFolderPath() == null) {
      if (FileUtilRt.getNameWithoutExtension(bc.getOutputFileName()).isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle.message("output.file.name.not.set"),
                                                            FlexBCConfigurable.Location.OutputFileName));
      }
      else {
        if (!nature.isLib() && !StringUtil.toLowerCase(bc.getOutputFileName()).endsWith(".swf")) {
          errorConsumer.consume(
            FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                                    FlexBundle.message("output.file.wrong.extension", "swf"),
                                                                    FlexBCConfigurable.Location.OutputFileName));
        }

        if (nature.isLib() && !StringUtil.toLowerCase(bc.getOutputFileName()).endsWith(".swc")) {
          errorConsumer.consume(
            FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                                    FlexBundle.message("output.file.wrong.extension", "swc"),
                                                                    FlexBCConfigurable.Location.OutputFileName));
        }
      }

      if (bc.getOutputFolder().isEmpty()) {
        if (BCUtils.isFlexUnitBC(bc)) {
          errorConsumer.consume(FlashProjectStructureProblem.FlexUnitOutputFolderProblem.INSTANCE);
        }
        else {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle.message("output.folder.not.set"),
                                                              FlexBCConfigurable.Location.OutputFolder));
        }
      }
      else if (!FileUtil.isAbsolute(bc.getOutputFolder())) {
        if (BCUtils.isFlexUnitBC(bc)) {
          errorConsumer.consume(FlashProjectStructureProblem.FlexUnitOutputFolderProblem.INSTANCE);
        }
        else {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("output.folder.not.absolute", FileUtil.toSystemDependentName(bc.getOutputFolder())),
                                                                                        FlexBCConfigurable.Location.OutputFolder));
        }
      }
    }

    if (nature.isWebPlatform() && nature.isApp() && bc.isUseHtmlWrapper()) {
      if (bc.getWrapperTemplatePath().isEmpty()) {
        errorConsumer
          .consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                                           FlexBundle.message("html.template.folder.not.set"),
                                                                           FlexBCConfigurable.Location.HtmlTemplatePath));
      }
      else {
        final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
        if (templateDir == null || !templateDir.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("html.template.folder.not.found", FileUtil.toSystemDependentName(bc.getWrapperTemplatePath())),
                                                                                        FlexBCConfigurable.Location.HtmlTemplatePath));
        }
        else {
          final VirtualFile templateFile = templateDir.findChild(FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME);
          if (templateFile == null) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexCommonBundle
              .message("no.index.template.html.file", templateDir.getPresentableUrl()), FlexBCConfigurable.Location.HtmlTemplatePath));
          }
          else {
            // Probably heavy calculation. Will be checked only when real html template handling is performed
            /*
            try {
              if (!VfsUtilCore.loadText(templateFile).contains(FlexCompilationUtils.SWF_MACRO)) {
                errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(
                  FlexBundle.message("no.swf.macro.in.template", FileUtil.toSystemDependentName(templateFile.getPath())), "html.template"));
              }
            }
            catch (IOException e) {
              errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(
                FlexBundle.message("failed.to.load.template.file", FileUtil.toSystemDependentName(templateFile.getPath()), e.getMessage()),
                "html.template"));
            }
            */

            final String templateFolderPath = templateDir.getPath();
            boolean ok = true;

            for (String url : ModuleRootManager.getInstance(module).getContentRootUrls()) {
              if (ok) {
                ok = checkWrapperFolderClash(bc, templateFolderPath, VfsUtilCore.urlToPath(url), "module content root", errorConsumer);
              }
            }

            for (String url : ModuleRootManager.getInstance(module).getSourceRootUrls()) {
              if (ok) {
                ok = checkWrapperFolderClash(bc, templateFolderPath, VfsUtilCore.urlToPath(url), "source folder", errorConsumer);
              }
            }

            final String outputFolderPath = StringUtil.notNullize(info.getOutputFolderPath(), bc.getOutputFolder());
            if (ok && !outputFolderPath.isEmpty()) {
              ok = checkWrapperFolderClash(bc, templateFolderPath, outputFolderPath, "output folder", errorConsumer);
            }
          }
        }
      }
    }

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
      for (FlexBuildConfiguration.RLMInfo rlm : bc.getRLMs()) {
        if (rlm.MAIN_CLASS.isEmpty()) {
          errorConsumer
            .consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                                             FlexBundle.message("rlm.main.class.not.set"),
                                                                             FlexBCConfigurable.Location.RLMs));
        }
        else {
          if (FlexUtils.getPathToMainClassFile(rlm.MAIN_CLASS, module).isEmpty()) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
              .message("rlm.main.class.not.found", rlm.MAIN_CLASS), FlexBCConfigurable.Location.RLMs));
          }
        }

        if (bc.getMainClass().equals(rlm.MAIN_CLASS)) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("rlm.main.class.equal.to.bc.main.class", rlm.MAIN_CLASS), FlexBCConfigurable.Location.RLMs));
        }

        if (bc.getOutputFileName().equals(rlm.OUTPUT_FILE)) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("rlm.output.equal.to.bc.output", rlm.OUTPUT_FILE), FlexBCConfigurable.Location.RLMs));
        }

        if (rlm.OUTPUT_FILE.isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(),
                                                              FlexBundle.message("rlm.output.file.name.not.specified"),
                                                              FlexBCConfigurable.Location.RLMs));
        }
        else {
          if (!StringUtil.toLowerCase(rlm.OUTPUT_FILE).endsWith(".swf")) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle.message(
              "rlm.output.file.must.have.swf.extension"), FlexBCConfigurable.Location.RLMs));
          }
        }
      }

      for (String cssPath : bc.getCssFilesToCompile()) {
        if (!StringUtil.toLowerCase(cssPath).endsWith(".css")) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("not.a.css.runtime.stylesheet", FileUtil.toSystemDependentName(cssPath)),
                                                                                        FlexBCConfigurable.Location.RuntimeStyleSheets));
        }
        else if (LocalFileSystem.getInstance().findFileByPath(cssPath) == null) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle
            .message("css.not.found", FileUtil.toSystemDependentName(cssPath)), FlexBCConfigurable.Location.RuntimeStyleSheets));
        }
      }
    }

    if (nature.isLib()) {
      for (String path : bc.getCompilerOptions().getFilesToIncludeInSWC()) {
        if (LocalFileSystem.getInstance().findFileByPath(path) == null) {
          errorConsumer.consume(FlashProjectStructureProblem.createCompilerOptionsProblem(ProjectStructureProblemType.Severity.ERROR,
                                                                                          FlexBundle
                                                                                            .message("file.to.include.in.swc.not.found",
                                                                                                     FileUtil.toSystemDependentName(path)),
                                                                                          CompilerOptionsConfigurable.Location.FilesToIncludeInSwc));
        }
      }
    }

    if (checkPackaging) {
      checkPackagingOptions(module, bc, errorConsumer);
    }
  }

  private static boolean checkWrapperFolderClash(final FlexBuildConfiguration bc,
                                                 final String templateFolderPath,
                                                 final String otherFolderPath,
                                                 final String otherFolderDescription,
                                                 final Consumer<FlashProjectStructureProblem> errorConsumer) {
    if (FileUtil.isAncestor(templateFolderPath, otherFolderPath, false)) {
      errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(ProjectStructureProblemType.Severity.ERROR, bc.getName(), FlexBundle.message(
        "html.wrapper.folder.clash", otherFolderDescription, FileUtil.toSystemDependentName(templateFolderPath)),
                                                                                    FlexBCConfigurable.Location.HtmlTemplatePath));
      return false;
    }
    return true;
  }

  public static void checkPackagingOptions(final Module module,
                                           final FlexBuildConfiguration bc,
                                           final Consumer<FlashProjectStructureProblem> errorConsumer) {
    if (bc.getOutputType() != OutputType.Application) return;

    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      checkPackagingOptions(module, bc.getSdk(), bc.getAirDesktopPackagingOptions(), false,
                            PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        checkPackagingOptions(module, bc.getSdk(), bc.getAndroidPackagingOptions(), false,
                              PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
      if (bc.getIosPackagingOptions().isEnabled()) {
        checkPackagingOptions(module, bc.getSdk(), bc.getIosPackagingOptions(), false,
                              PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
    }
  }

  private static void checkPackagingOptions(final Module module,
                                            final @Nullable Sdk sdk,
                                            final AirPackagingOptions packagingOptions,
                                            final boolean isForIosSimulator,
                                            final String outputFolderPath,
                                            final Consumer<FlashProjectStructureProblem> errorConsumer) {
    final String device = packagingOptions instanceof AndroidPackagingOptions
                          ? "Android"
                          : packagingOptions instanceof IosPackagingOptions
                            ? "iOS"
                            : "";
    if (!packagingOptions.isUseGeneratedDescriptor()) {
      if (packagingOptions.getCustomDescriptorPath().isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions,
                                                               FlexBundle.message("custom.descriptor.not.set", device),
                                                               AirPackagingConfigurableBase.Location.CustomDescriptor));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("custom.descriptor.not.found", device,
                                             FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())),
                                                                 AirPackagingConfigurableBase.Location.CustomDescriptor));
        }
        else if (sdk != null && sdk.getSdkType() != FlexmojosSdkType.getInstance()) {
          FlashRunnerParameters.checkAirVersionIfCustomDescriptor(module, sdk, packagingOptions, errorConsumer, false, "does not matter");
        }
      }
    }

    if (packagingOptions.getPackageFileName().isEmpty()) {
      errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
        .message("package.file.name.not.set", device), AirPackagingConfigurableBase.Location.PackageFileName));
    }

    for (AirPackagingOptions.FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = entry.FILE_PATH;
      String relPathInPackage = entry.PATH_IN_PACKAGE;
      relPathInPackage = StringUtil.trimStart(relPathInPackage, "/");

      if (fullPath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                                                 .message("packaging.options.empty.file.name", device),
                                                               AirPackagingConfigurableBase.Location.FilesToPackage));
      }
      else {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (file == null) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                                                   .message("packaging.options.file.not.found", device, FileUtil.toSystemDependentName(fullPath)),
                                                                 AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (relPathInPackage.isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                                                   .message("packaging.options.empty.relative.path", device),
                                                                 AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (file != null && file.isDirectory()) {
          if (FileUtil.isAncestor(file.getPath(), outputFolderPath, false)) {
            errorConsumer.consume(FlashProjectStructureProblem
                                    .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                                                     .message("folder.to.package.includes.output", device, file.getPresentableUrl()),
                                                                   AirPackagingConfigurableBase.Location.FilesToPackage));
          }
          else if (!relPathInPackage.isEmpty() && !".".equals(relPathInPackage) && !fullPath.endsWith("/" + relPathInPackage)) {
            errorConsumer.consume(
              FlashProjectStructureProblem
                .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                                 .message("packaging.options.relative.path.not.matches", device, FileUtil.toSystemDependentName(relPathInPackage)),
                                               AirPackagingConfigurableBase.Location.FilesToPackage));
          }
        }
      }
    }

    if (packagingOptions instanceof IosPackagingOptions) {
      final String path = packagingOptions.getSigningOptions().getIOSSdkPath();
      if (!path.isEmpty() && !new File(path).isDirectory()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
          .message("packaging.options.bad.ios.sdk.path", device, FileUtil.toSystemDependentName(path)),
                                                                                         AirPackagingConfigurableBase.Location.IosSdkPath));
      }
    }

    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    if (packagingOptions instanceof IosPackagingOptions && !isForIosSimulator) {
      final String provisioningProfilePath = signingOptions.getProvisioningProfilePath();
      if (provisioningProfilePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
          .message("ios.provisioning.profile.not.set"), AirPackagingConfigurableBase.Location.ProvisioningProfile));
      }
      else {
        final VirtualFile provisioningProfile = LocalFileSystem.getInstance().findFileByPath(provisioningProfilePath);
        if (provisioningProfile == null || provisioningProfile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("ios.provisioning.profile.not.found", FileUtil.toSystemDependentName(provisioningProfilePath)),
                                                                 AirPackagingConfigurableBase.Location.ProvisioningProfile));
        }
      }
    }

    final boolean tempCertificate = packagingOptions instanceof IosPackagingOptions ? isForIosSimulator
                                                                                    : signingOptions.isUseTempCertificate();
    if (!tempCertificate) {
      final String keystorePath = signingOptions.getKeystorePath();
      if (keystorePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions,
                                                                                         FlexBundle.message("keystore.not.set", device),
                                                                                         AirPackagingConfigurableBase.Location.Keystore));
      }
      else {
        final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
        if (keystore == null || keystore.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("keystore.not.found", device, FileUtil.toSystemDependentName(keystorePath)),
                                                                 AirPackagingConfigurableBase.Location.Keystore));
        }
      }
    }
  }

  @Override
  public boolean execute(@NotNull final CompileContext context) {
    return validateConfiguration(context);
  }

  private boolean validateConfiguration(final CompileContext context) {
    try {
      final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile =
        FlexBuildTargetScopeProvider.getModulesAndBCsToCompile(context.getCompileScope());

      suggestParallelCompilationIfNeeded(context.getProject(), modulesAndBCsToCompile);

      final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems =
        ReadAction.compute(() -> getProblems(context.getCompileScope(), modulesAndBCsToCompile));

      if (!problems.isEmpty()) {
        boolean hasErrors = false;
        for (Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem> problem : problems) {
          if (problem.getThird().severity == ProjectStructureProblemType.Severity.ERROR) {
            hasErrors = true;
            break;
          }
        }

        if (hasErrors) {
          // todo remove this senseless error message when 'show first error in editor' functionality respect canNavigateToSource()
          context.addMessage(CompilerMessageCategory.ERROR,
                             "Flash build configurations contain errors. " +
                             "Double-click error message below to navigate to the corresponding field in the Project Structure dialog",
                             null, -1, -1);
        }

        reportProblems(context, problems);
        return !hasErrors;
      }
    }
    catch (ConfigurationException e) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("project.setup.problem", e.getMessage()), null, -1, -1);
      return false;
    }

    return true;
  }

  private void suggestParallelCompilationIfNeeded(final Project project,
                                                  final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    if (myParallelCompilationSuggested) return;
    if (CompilerConfiguration.getInstance(project).isParallelCompilationEnabled()) return;
    if (modulesAndBCsToCompile.size() < 2) return;
    if (!independentBCsExist(modulesAndBCsToCompile)) return;

    final NotificationListener listener = new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        notification.expire();

        if ("enable".equals(event.getDescription())) {
          CompilerConfiguration.getInstance(project).setParallelCompilationEnabled(true);

          final NotificationListener listener1 = new NotificationListener() {
            @Override
            public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
              notification.expire();
              ShowSettingsUtil.getInstance().showSettingsDialog(project, JavaCompilerBundle.message("compiler.configurable.display.name"));
            }
          };
          new Notification(FLASH_COMPILER_GROUP_ID, FlexBundle.message("parallel.compilation.enabled"),
                           FlexBundle.message("see.settings.compiler"), NotificationType.INFORMATION).setListener(listener1).notify(project);
        }
        else if ("open".equals(event.getDescription())) {
          ShowSettingsUtil.getInstance().showSettingsDialog(project, JavaCompilerBundle.message("compiler.configurable.display.name"));
        }
      }
    };

    new Notification(FLASH_COMPILER_GROUP_ID, FlexBundle.message("parallel.compilation.hint.title"),
                     FlexBundle.message("parallel.compilation.hint"), NotificationType.INFORMATION).setListener(listener).notify(project);

    myParallelCompilationSuggested = true;
  }

  private static boolean independentBCsExist(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    final Set<FlexBuildConfiguration> bcs = new THashSet<>();

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      bcs.add(moduleAndBC.second);
    }

    int independentBCsCount = 0;

    OUTER:
    for (FlexBuildConfiguration bc : bcs) {
      for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
        if (entry instanceof BuildConfigurationEntry) {
          final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
          if (dependencyBC != null && bcs.contains(dependencyBC)) {
            continue OUTER;
          }
        }
      }

      independentBCsCount++;
      if (independentBCsCount > 1) {
        return true;
      }
    }

    return false;
  }

  private static void reportProblems(final CompileContext context,
                                     final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems) {
    for (Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem> trinity : problems) {
      final Module module = trinity.getFirst();
      final FlexBuildConfiguration bc = trinity.getSecond();
      final FlashProjectStructureProblem problem = trinity.getThird();

      final String message = problem instanceof FlashProjectStructureProblem.FlexUnitOutputFolderProblem
                             ? problem.errorMessage
                             : FlexBundle.message("bc.0.module.1.problem.2", bc.getName(), module.getName(), problem.errorMessage);
      final CompilerMessageCategory severity = problem.severity == ProjectStructureProblemType.Severity.ERROR
                                               ? CompilerMessageCategory.ERROR
                                               : CompilerMessageCategory.WARNING;
      context.addMessage(severity, message, null, -1, -1, new BCProblemNavigatable(module, bc.getName(), problem));
    }
  }

  private static final class BCProblemNavigatable implements Navigatable {
    @NotNull private final Module myModule;
    @NotNull private final String myBCNme;
    @NotNull private final FlashProjectStructureProblem myProblem;

    private BCProblemNavigatable(final @NotNull Module module,
                                 final @NotNull String bcName,
                                 final @NotNull FlashProjectStructureProblem problem) {
      myModule = module;
      myBCNme = bcName;
      myProblem = problem;
    }

    @Override
    public boolean canNavigateToSource() {
      return false;
    }

    @Override
    public boolean canNavigate() {
      return !myModule.isDisposed() && FlexBuildConfigurationManager.getInstance(myModule).findConfigurationByName(myBCNme) != null;
    }

    @Override
    public void navigate(final boolean requestFocus) {
      final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(myModule.getProject());

      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), configurable, () -> {
        final Place place;

        if (myProblem instanceof FlashProjectStructureProblem.FlexUnitOutputFolderProblem) {
          place = new Place()
            .putPath(ProjectStructureConfigurable.CATEGORY, configurable.getProjectConfig());
        }
        else {
          place = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, myBCNme)
            .putPath(CompositeConfigurable.TAB_NAME, myProblem.tabName)
            .putPath(FlexBCConfigurable.LOCATION_ON_TAB, myProblem.locationOnTab);
        }

        configurable.navigateTo(place, true);
      });
    }
  }
}
