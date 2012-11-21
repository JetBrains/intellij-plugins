package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.AirPackagingConfigurableBase;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompilerOptionsConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.DependenciesConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;
import static com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions.ResourceFilesMode;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

public class FlexCompiler implements SourceProcessingCompiler {
  private static final Logger LOG = Logger.getInstance(FlexCompiler.class.getName());
  private static final Key<Collection<Pair<Module, FlexBuildConfiguration>>> MODULES_AND_BCS_TO_COMPILE =
    Key.create("modules.and.bcs.to.compile");

  @NotNull
  public static FlexCompiler getInstance(final @NotNull Project project) {
    final FlexCompiler[] compilers = CompilerManager.getInstance(project).getCompilers(FlexCompiler.class);
    LOG.assertTrue(compilers.length == 1, compilers.length);
    return compilers[0];
  }

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    saveProject(context.getProject());
    final List<ProcessingItem> itemList = new ArrayList<ProcessingItem>();

    try {
      for (final Pair<Module, FlexBuildConfiguration> moduleAndBC : getModulesAndBCsToCompile(context.getCompileScope())) {
        itemList.add(new MyProcessingItem(moduleAndBC.first, moduleAndBC.second));
      }
    }
    catch (ConfigurationException e) {
      // can't happen because already validated
      throw new RuntimeException(e);
    }

    return itemList.toArray(new ProcessingItem[itemList.size()]);
  }

  private static void saveProject(final Project project) {
    Runnable runnable = new Runnable() {
      public void run() {
        project.save();
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.defaultModalityState());
    }
  }

  public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items) {
    if (items.length == 0) return items;

    final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(context.getProject());
    final FlexCompilerProjectConfiguration flexCompilerConfiguration = FlexCompilerProjectConfiguration.getInstance(context.getProject());

    if (!context.isMake()) {
      flexCompilerHandler.quitCompilerShell();
      for (ProcessingItem item : items) {
        flexCompilerHandler.getCompilerDependenciesCache().markBCDirty(((MyProcessingItem)item).myModule, ((MyProcessingItem)item).myBC);
      }
    }

    if (flexCompilerConfiguration.USE_FCSH) {
      context.addMessage(CompilerMessageCategory.INFORMATION,
                         "FCSH tool is not supported yet. Please choose another compiler at File | Settings | Compiler | Flex Compiler",
                         null, -1, -1);
      return ProcessingItem.EMPTY_ARRAY;
    }
    else {
      boolean builtInCompilerShell = flexCompilerConfiguration.USE_BUILT_IN_COMPILER;
      final Sdk commonSdk = getSdkIfSame(items);

      if (builtInCompilerShell && commonSdk == null) {
        builtInCompilerShell = false;
        flexCompilerHandler.getBuiltInFlexCompilerHandler().stopCompilerProcess();
        context.addMessage(CompilerMessageCategory.INFORMATION, FlexBundle.message("can.not.use.built.in.compiler.shell"), null, -1, -1);
      }

      final StringBuilder buf = new StringBuilder();
      buf.append(FlexBundle.message(builtInCompilerShell ? "using.builtin.compiler" : "using.mxmlc.compc",
                                    flexCompilerConfiguration.MAX_PARALLEL_COMPILATIONS));
      if (flexCompilerConfiguration.PREFER_ASC_20) buf.append(FlexBundle.message("or.asc.2.0"));
      buf.append("\n").append(FlexBundle.message("see.flex.compiler.page"));

      context.addMessage(CompilerMessageCategory.INFORMATION, buf.toString(), null, -1, -1);

      final Collection<FlexCompilationTask> compilationTasks = new ArrayList<FlexCompilationTask>();
      for (final ProcessingItem item : items) {
        final Collection<FlexBuildConfiguration> dependencies = new HashSet<FlexBuildConfiguration>();
        final FlexBuildConfiguration bc = ((MyProcessingItem)item).myBC;

        for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
          if (entry instanceof BuildConfigurationEntry) {
            final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
            if (dependencyBC != null && !dependencyBC.isSkipCompile() &&
                entry.getDependencyType().getLinkageType() != LinkageType.LoadInRuntime) {
              dependencies.add(dependencyBC);
            }
          }
        }

        compilationTasks.add(createCompilationTask(((MyProcessingItem)item).myModule, bc, dependencies, builtInCompilerShell));

        if (BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
          for (FlexBuildConfiguration.RLMInfo rlm : bc.getRLMs()) {
            final ModifiableFlexBuildConfiguration rlmBC = Factory.getTemporaryCopyForCompilation(bc);

            rlmBC.setOutputType(OutputType.RuntimeLoadedModule);
            rlmBC.setOptimizeFor(rlm.OPTIMIZE ? bc.getName() : ""); // any not empty string means that need to optimize

            final String subdir = PathUtil.getParentPath(rlm.OUTPUT_FILE);
            final String outputFileName = PathUtil.getFileName(rlm.OUTPUT_FILE);

            rlmBC.setMainClass(rlm.MAIN_CLASS);
            rlmBC.setOutputFileName(outputFileName);

            if (!subdir.isEmpty()) {
              final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
              rlmBC.setOutputFolder(outputFolder + "/" + subdir);
            }


            rlmBC.setUseHtmlWrapper(false);

            rlmBC.setRLMs(Collections.<FlexBuildConfiguration.RLMInfo>emptyList());
            rlmBC.setCssFilesToCompile(Collections.<String>emptyList());

            final ModifiableCompilerOptions compilerOptions = rlmBC.getCompilerOptions();
            compilerOptions.setResourceFilesMode(ResourceFilesMode.None);
            compilerOptions.setAdditionalOptions(FlexCommonUtils.removeOptions(compilerOptions.getAdditionalOptions(), "link-report"));

            compilationTasks.add(createCompilationTask(((MyProcessingItem)item).myModule, rlmBC, dependencies, builtInCompilerShell));
          }

          for (String cssPath : bc.getCssFilesToCompile()) {
            final VirtualFile cssFile = LocalFileSystem.getInstance().findFileByPath(cssPath);
            if (cssFile == null) continue;

            final ModifiableFlexBuildConfiguration cssBC = Factory.getTemporaryCopyForCompilation(bc);
            cssBC.setOutputType(OutputType.Application);

            cssBC.setMainClass(cssPath);
            cssBC.setOutputFileName(FileUtil.getNameWithoutExtension(PathUtil.getFileName(cssPath)) + ".swf");

            VirtualFile root = ProjectRootManager.getInstance(context.getProject()).getFileIndex().getSourceRootForFile(cssFile);
            if (root == null) root = ProjectRootManager.getInstance(context.getProject()).getFileIndex().getContentRootForFile(cssFile);
            final String relativePath = root == null ? null : VfsUtilCore.getRelativePath(cssFile.getParent(), root, '/');
            if (!StringUtil.isEmpty(relativePath)) {
              final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
              cssBC.setOutputFolder(outputFolder + "/" + relativePath);
            }

            cssBC.setUseHtmlWrapper(false);
            cssBC.setRLMs(Collections.<FlexBuildConfiguration.RLMInfo>emptyList());
            cssBC.setCssFilesToCompile(Collections.<String>emptyList());

            cssBC.getCompilerOptions().setResourceFilesMode(ResourceFilesMode.None);

            compilationTasks.add(createCompilationTask(((MyProcessingItem)item).myModule, cssBC, dependencies, builtInCompilerShell));
          }
        }
      }

      if (builtInCompilerShell) {
        try {
          flexCompilerHandler.getBuiltInFlexCompilerHandler().startCompilerIfNeeded(commonSdk, context);
        }
        catch (IOException e) {
          context.addMessage(CompilerMessageCategory.ERROR, e.toString(), null, -1, -1);
          return ProcessingItem.EMPTY_ARRAY;
        }
      }

      new FlexCompilationManager(context, compilationTasks).compile();

      final int activeCompilationsNumber = flexCompilerHandler.getBuiltInFlexCompilerHandler().getActiveCompilationsNumber();
      if (activeCompilationsNumber != 0) {
        LOG.error(activeCompilationsNumber + " Flex compilation(s) are not finished!");
      }

      if (ApplicationManager.getApplication().isUnitTestMode()) {
        Function<CompilerMessage, String> toString = new Function<CompilerMessage, String>() {
          @Override
          public String fun(final CompilerMessage compilerMessage) {
            return compilerMessage.getMessage();
          }
        };

        StringBuilder s = new StringBuilder("Compiler errors:\n");
        s.append(StringUtil.join(context.getMessages(CompilerMessageCategory.ERROR), toString, "\n"));
        s.append("\nCompiler warnings:\n");
        s.append(StringUtil.join(context.getMessages(CompilerMessageCategory.WARNING), toString, "\n"));
        FlexCompilerHandler.getInstance(context.getProject()).setLastCompilationMessages(s.toString());
      }
      return items;
    }
  }

  private static FlexCompilationTask createCompilationTask(final Module module,
                                                           final FlexBuildConfiguration bc,
                                                           final Collection<FlexBuildConfiguration> dependencies,
                                                           final boolean builtInCompilerShell) {
    final boolean asc20 = bc.isPureAs() &&
                          FlexCompilerProjectConfiguration.getInstance(module.getProject()).PREFER_ASC_20 &&
                          containsASC20(bc.getSdk());
    if (asc20) return new ASC20CompilationTask(module, bc, dependencies);
    if (builtInCompilerShell) return new BuiltInCompilationTask(module, bc, dependencies);
    return new MxmlcCompcCompilationTask(module, bc, dependencies);
  }

  private static boolean containsASC20(final Sdk sdk) {
    if (sdk.getSdkType() == FlexmojosSdkType.getInstance()) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        for (String path : ((FlexmojosSdkAdditionalData)data).getFlexCompilerClasspath()) {
          final String fileName = PathUtil.getFileName(path);
          if (fileName.startsWith("compiler-") && fileName.endsWith(".jar") && new File(path).isFile()) {
            return true;
          }
        }
      }
      return false;
    }

    return new File(sdk.getHomePath() + "/lib/compiler.jar").isFile();
  }

  @SuppressWarnings("ConstantConditions") // already checked in validateConfiguration()
  @Nullable
  private static Sdk getSdkIfSame(final ProcessingItem[] items) {
    final Sdk sdk = ((MyProcessingItem)items[0]).myBC.getSdk();

    for (int i = 1; i < items.length; i++) {
      if (!sdk.equals(((MyProcessingItem)items[i]).myBC.getSdk())) {
        return null;
      }
    }

    return sdk;
  }

  @NotNull
  public String getDescription() {
    return "ActionScript Compiler";
  }

  public boolean validateConfiguration(final CompileScope scope) {
    final Module[] modules = scope.getAffectedModules();
    final Project project = modules.length > 0 ? modules[0].getProject() : null;
    if (project == null) return true;

    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile;
    try {
      modulesAndBCsToCompile = getModulesAndBCsToCompile(scope);
    }
    catch (ConfigurationException e) {
      // can't happen because already checked at RunConfiguration.getState()
      Messages.showErrorDialog(project, e.getMessage(), FlexBundle.message("project.setup.problem.title"));
      return false;
    }

    final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems =
      getProblems(scope, modulesAndBCsToCompile);

    if (!problems.isEmpty()) {
      final FlashProjectStructureErrorsDialog dialog = new FlashProjectStructureErrorsDialog(project, problems);
      dialog.show();
      if (dialog.isOK()) {
        ShowSettingsUtil.getInstance().editConfigurable(project, ProjectStructureConfigurable.getInstance(project));
      }
      return false;
    }

    return true;
  }

  static Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> getProblems(final CompileScope scope,
                                                                                                       final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems =
      new ArrayList<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>>();

    for (final Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      final Module module = moduleAndBC.first;
      final FlexBuildConfiguration bc = moduleAndBC.second;

      final Consumer<FlashProjectStructureProblem> errorConsumer = new Consumer<FlashProjectStructureProblem>() {
        public void consume(final FlashProjectStructureProblem problem) {
          problems.add(Trinity.create(module, bc, problem));
        }
      };

      checkConfiguration(module, bc, false, errorConsumer);

      if (bc.getNature().isMobilePlatform() && bc.getNature().isApp()) {
        final RunConfiguration runConfig = CompileStepBeforeRun.getRunConfiguration(scope);
        if (runConfig instanceof FlashRunConfiguration) {
          final FlashRunnerParameters params = ((FlashRunConfiguration)runConfig).getRunnerParameters();
          if (module.getName().equals(params.getModuleName()) &&
              bc.getName().equals(params.getBCName())) {
            if (params.getMobileRunTarget() == AirMobileRunTarget.AndroidDevice) {
              checkPackagingOptions(bc.getAndroidPackagingOptions(), PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
            }
            else if (params.getMobileRunTarget() == AirMobileRunTarget.iOSDevice) {
              checkPackagingOptions(bc.getIosPackagingOptions(), PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
            }
          }
        }
      }
    }
    checkSimilarOutputFiles(modulesAndBCsToCompile,
                            new Consumer<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>>() {
                              public void consume(final Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem> trinity) {
                                problems.add(trinity);
                              }
                            });
    return problems;
  }

  private static boolean checkSimilarOutputFiles(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile,
                                                 final Consumer<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> errorConsumer) {

    final Map<String, Pair<Module, FlexBuildConfiguration>> outputPathToModuleAndBC =
      new THashMap<String, Pair<Module, FlexBuildConfiguration>>();
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
    final String caseAwarePath = SystemInfo.isFileSystemCaseSensitive ? outputPath : outputPath.toLowerCase();

    final Pair<Module, FlexBuildConfiguration> existing = outputPathToModuleAndBC.put(caseAwarePath, moduleAndBC);
    if (existing != null) {
      final String message = FlexBundle.message("same.output.files", existing.second.getName(), existing.first.getName(),
                                                FileUtil.toSystemDependentName(outputPath));
      errorConsumer.consume(Trinity.create(moduleAndBC.first, moduleAndBC.second, FlashProjectStructureProblem
        .createGeneralOptionProblem(moduleAndBC.second.getName(), message, FlexBCConfigurable.Location.OutputFileName)));
    }
  }

  static Collection<Pair<Module, FlexBuildConfiguration>> getModulesAndBCsToCompile(final CompileScope scope)
    throws ConfigurationException {

    final Collection<Pair<Module, FlexBuildConfiguration>> result = new HashSet<Pair<Module, FlexBuildConfiguration>>();
    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile = getBCsToCompileForPackaging(scope);
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(scope);

    if (modulesAndBCsToCompile != null) {
      for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
        if (!moduleAndBC.second.isSkipCompile()) {
          final FlexBuildConfiguration bcWithForcedDebugStatus = forceDebugStatus(moduleAndBC.first.getProject(), moduleAndBC.second);
          result.add(Pair.create(moduleAndBC.first, bcWithForcedDebugStatus));
          appendBCDependencies(result, moduleAndBC.first, moduleAndBC.second);
        }
      }
    }
    else if (runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof FlexUnitRunConfiguration) {
      final BCBasedRunnerParameters params = runConfiguration instanceof FlashRunConfiguration
                                             ? ((FlashRunConfiguration)runConfiguration).getRunnerParameters()
                                             : ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
      final Pair<Module, FlexBuildConfiguration> moduleAndBC;

      final Ref<RuntimeConfigurationError> exceptionRef = new Ref<RuntimeConfigurationError>();
      moduleAndBC = ApplicationManager.getApplication().runReadAction(new NullableComputable<Pair<Module, FlexBuildConfiguration>>() {
        public Pair<Module, FlexBuildConfiguration> compute() {
          try {
            return params.checkAndGetModuleAndBC(runConfiguration.getProject());
          }
          catch (RuntimeConfigurationError e) {
            exceptionRef.set(e);
            return null;
          }
        }
      });
      if (!exceptionRef.isNull()) {
        throw new ConfigurationException(exceptionRef.get().getMessage(),
                                         FlexBundle.message("run.configuration.0", runConfiguration.getName()));
      }

      if (!moduleAndBC.second.isSkipCompile()) {
        result.add(moduleAndBC);
        appendBCDependencies(result, moduleAndBC.first, moduleAndBC.second);
      }
    }
    else {
      for (final Module module : scope.getAffectedModules()) {
        if (ModuleType.get(module) != FlexModuleType.getInstance()) continue;
        for (final FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
          if (!bc.isSkipCompile()) {
            result.add(Pair.create(module, bc));
          }
        }
      }
    }

    return result;
  }

  public static void setBCsToCompileForPackaging(final CompileScope scope, final Collection<Pair<Module, FlexBuildConfiguration>> bcs) {
    scope.putUserData(MODULES_AND_BCS_TO_COMPILE, bcs);
  }

  @Nullable
  public static Collection<Pair<Module, FlexBuildConfiguration>> getBCsToCompileForPackaging(final CompileScope scope) {
    return scope.getUserData(MODULES_AND_BCS_TO_COMPILE);
  }

  private static FlexBuildConfiguration forceDebugStatus(final Project project, final FlexBuildConfiguration bc) {
    final boolean debug = getForcedDebugStatus(project, bc);

    // must not use getTemporaryCopyForCompilation() here because additional config file must not be merged with the generated one when compiling swf for release or AIR package
    final ModifiableFlexBuildConfiguration result = Factory.getCopy(bc);
    final String additionalOptions = FlexCommonUtils
      .removeOptions(bc.getCompilerOptions().getAdditionalOptions(), "debug", "compiler.debug");
    result.getCompilerOptions().setAdditionalOptions(additionalOptions + " -debug=" + String.valueOf(debug));

    return result;
  }

  public static boolean getForcedDebugStatus(final Project project, final FlexBuildConfiguration bc) {
    final boolean debug;

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      final AirPackageProjectParameters params = AirPackageProjectParameters.getInstance(project);
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        debug = params.androidPackageType != AirPackageProjectParameters.AndroidPackageType.Release;
      }
      else {
        debug = params.iosPackageType == AirPackageProjectParameters.IOSPackageType.DebugOverNetwork;
      }
    }
    else {
      debug = false;
    }

    return debug;
  }

  private static void appendBCDependencies(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs,
                                           final Module module,
                                           final FlexBuildConfiguration bc) throws ConfigurationException {
    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;

        final Module dependencyModule = bcEntry.findModule();
        final FlexBuildConfiguration dependencyBC = dependencyModule == null ? null : bcEntry.findBuildConfiguration();

        if (dependencyModule == null || dependencyBC == null) {
          throw new ConfigurationException(FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(),
                                                              bc.getName(), module.getName()));
        }

        final Pair<Module, FlexBuildConfiguration> dependencyModuleAndBC = Pair.create(dependencyModule, dependencyBC);
        if (!dependencyBC.isSkipCompile()) {
          if (modulesAndBCs.add(dependencyModuleAndBC)) {
            appendBCDependencies(modulesAndBCs, dependencyModule, dependencyBC);
          }
        }
      }
    }
  }

  public static void checkConfiguration(final Module module,
                                        final FlexBuildConfiguration bc,
                                        final boolean checkPackaging,
                                        final Consumer<FlashProjectStructureProblem> errorConsumer) {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      errorConsumer.consume(FlashProjectStructureProblem.createDependenciesProblem(FlexBundle.message("sdk.not.set"),
                                                                                   DependenciesConfigurable.Location.SDK));
    }

    if (sdk != null &&
        (StringUtil.compareVersionNumbers(sdk.getVersionString(), "0") < 0 ||
         StringUtil.compareVersionNumbers(sdk.getVersionString(), "100") > 0)) {
      errorConsumer.consume(FlashProjectStructureProblem.createDependenciesProblem(FlexBundle.message("sdk.version.unknown", sdk.getName()),
                                                                                   DependenciesConfigurable.Location.SDK));
    }

    InfoFromConfigFile info = InfoFromConfigFile.DEFAULT;

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null || additionalConfigFile.isDirectory()) {
        errorConsumer.consume(FlashProjectStructureProblem.createCompilerOptionsProblem(
          FlexBundle.message("additional.config.file.not.found", FileUtil.toSystemDependentName(additionalConfigFilePath)),
          CompilerOptionsConfigurable.Location.AdditonalConfigFile));
      }
      if (!bc.isTempBCForCompilation()) {
        info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(additionalConfigFilePath);
      }
    }

    final BuildConfigurationNature nature = bc.getNature();

    if (!nature.isLib() && info.getMainClass(module) == null && !bc.isTempBCForCompilation()) {
      if (bc.getMainClass().isEmpty()) {
        errorConsumer
          .consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message("main.class.not.set"),
                                                                           FlexBCConfigurable.Location.MainClass));
      }
      else {
        if (FlexUtils.getPathToMainClassFile(bc.getMainClass(), module).isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(bc.getName(), FlexBundle.message("main.class.not.found", bc.getMainClass()),
                                                              FlexBCConfigurable.Location.MainClass));
        }
      }
    }

    if (info.getOutputFileName() == null && info.getOutputFolderPath() == null) {
      if (bc.getOutputFileName().isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createGeneralOptionProblem(bc.getName(), FlexBundle.message("output.file.name.not.set"),
                                                            FlexBCConfigurable.Location.OutputFileName));
      }
      else {
        if (!nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swf")) {
          errorConsumer.consume(
            FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message("output.file.wrong.extension", "swf"),
                                                                    FlexBCConfigurable.Location.OutputFileName));
        }

        if (nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swc")) {
          errorConsumer.consume(
            FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message("output.file.wrong.extension", "swc"),
                                                                    FlexBCConfigurable.Location.OutputFileName));
        }
      }

      if (bc.getOutputFolder().isEmpty()) {
        if (BCUtils.isFlexUnitBC(bc)) {
          errorConsumer.consume(FlashProjectStructureProblem.FlexUnitOutputFolderProblem.INSTANCE);
        }
        else {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(bc.getName(), FlexBundle.message("output.folder.not.set"),
                                                              FlexBCConfigurable.Location.OutputFolder));
        }
      }
      else if (!FileUtil.isAbsolute(bc.getOutputFolder())) {
        errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
          .message("output.folder.not.absolute", FileUtil.toSystemDependentName(bc.getOutputFolder())),
                                                                                      FlexBCConfigurable.Location.OutputFolder));
      }
    }

    if (nature.isWebPlatform() && nature.isApp() && bc.isUseHtmlWrapper()) {
      if (bc.getWrapperTemplatePath().isEmpty()) {
        errorConsumer
          .consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message("html.template.folder.not.set"),
                                                                           FlexBCConfigurable.Location.HtmlTemplatePath));
      }
      else {
        final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
        if (templateDir == null || !templateDir.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
            .message("html.template.folder.not.found", FileUtil.toSystemDependentName(bc.getWrapperTemplatePath())),
                                                                                        FlexBCConfigurable.Location.HtmlTemplatePath));
        }
        else {
          final VirtualFile templateFile = templateDir.findChild(FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME);
          if (templateFile == null) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexCommonBundle
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
            .consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message("rlm.main.class.not.set"),
                                                                             FlexBCConfigurable.Location.RLMs));
        }
        else {
          if (FlexUtils.getPathToMainClassFile(rlm.MAIN_CLASS, module).isEmpty()) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
              .message("rlm.main.class.not.found", rlm.MAIN_CLASS), FlexBCConfigurable.Location.RLMs));
          }
        }

        if (bc.getMainClass().equals(rlm.MAIN_CLASS)) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
            .message("rlm.main.class.equal.to.bc.main.class", rlm.MAIN_CLASS), FlexBCConfigurable.Location.RLMs));
        }

        if (bc.getOutputFileName().equals(rlm.OUTPUT_FILE)) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
            .message("rlm.output.equal.to.bc.output", rlm.OUTPUT_FILE), FlexBCConfigurable.Location.RLMs));
        }

        if (rlm.OUTPUT_FILE.isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createGeneralOptionProblem(bc.getName(), FlexBundle.message("rlm.output.file.name.not.specified"),
                                                              FlexBCConfigurable.Location.RLMs));
        }
        else {
          if (!rlm.OUTPUT_FILE.toLowerCase().endsWith(".swf")) {
            errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message(
              "rlm.output.file.must.have.swf.extension"), FlexBCConfigurable.Location.RLMs));
          }
        }
      }

      for (String cssPath : bc.getCssFilesToCompile()) {
        if (!cssPath.toLowerCase().endsWith(".css")) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
            .message("not.a.css.runtime.stylesheet", FileUtil.toSystemDependentName(cssPath)),
                                                                                        FlexBCConfigurable.Location.RuntimeStyleSheets));
        }
        else if (LocalFileSystem.getInstance().findFileByPath(cssPath) == null) {
          errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle
            .message("css.not.found", FileUtil.toSystemDependentName(cssPath)), FlexBCConfigurable.Location.RuntimeStyleSheets));
        }
      }
    }

    if (nature.isLib()) {
      for (String path : bc.getCompilerOptions().getFilesToIncludeInSWC()) {
        if (LocalFileSystem.getInstance().findFileByPath(path) == null) {
          errorConsumer.consume(FlashProjectStructureProblem.createCompilerOptionsProblem(
            FlexBundle.message("file.to.include.in.swc.not.found", FileUtil.toSystemDependentName(path)),
            CompilerOptionsConfigurable.Location.FilesToIncludeInSwc));
        }
      }
    }

    if (checkPackaging) {
      checkPackagingOptions(bc, errorConsumer);
    }

    //checkDependencies(moduleName, bc);
  }

  /* This verification is disabled because Vladimir Krivosheev has app on app dependency because he needs predictable compilation order.
   So we do not check dependencies and ignore incompatible ones when doing highlighting and compilation. */
  private static void checkDependencies(final String moduleName, final FlexBuildConfiguration bc) throws ConfigurationException {
    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        final FlexBuildConfiguration dependencyBC = bcEntry.findBuildConfiguration();
        final LinkageType linkageType = bcEntry.getDependencyType().getLinkageType();

        if (dependencyBC == null) {
          throw new ConfigurationException(
            FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(), bc.getName(), moduleName));
        }

        if (!FlexCommonUtils.checkDependencyType(bc.getOutputType(), dependencyBC.getOutputType(), linkageType)) {
          throw new ConfigurationException(
            FlexBundle.message("bc.dependency.problem",
                               bc.getName(), moduleName, bc.getOutputType().getPresentableText(),
                               dependencyBC.getName(), bcEntry.getModuleName(), dependencyBC.getOutputType().getPresentableText(),
                               linkageType.getShortText()));
        }
      }
    }
  }

  private static boolean checkWrapperFolderClash(final FlexBuildConfiguration bc,
                                                 final String templateFolderPath,
                                                 final String otherFolderPath,
                                                 final String otherFolderDescription,
                                                 final Consumer<FlashProjectStructureProblem> errorConsumer) {
    if (FileUtil.isAncestor(templateFolderPath, otherFolderPath, false)) {
      errorConsumer.consume(FlashProjectStructureProblem.createGeneralOptionProblem(bc.getName(), FlexBundle.message(
        "html.wrapper.folder.clash", otherFolderDescription, FileUtil.toSystemDependentName(templateFolderPath)),
                                                                                    FlexBCConfigurable.Location.HtmlTemplatePath));
      return false;
    }
    return true;
  }

  public static void checkPackagingOptions(final FlexBuildConfiguration bc, final Consumer<FlashProjectStructureProblem> errorConsumer) {
    if (bc.getOutputType() != OutputType.Application) return;

    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      checkPackagingOptions(bc.getAirDesktopPackagingOptions(), PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        checkPackagingOptions(bc.getAndroidPackagingOptions(), PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
      if (bc.getIosPackagingOptions().isEnabled()) {
        checkPackagingOptions(bc.getIosPackagingOptions(), PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
    }
  }

  private static void checkPackagingOptions(final AirPackagingOptions packagingOptions,
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
                                .createPackagingOptionsProblem(packagingOptions, FlexBundle.message("custom.descriptor.not.set", device),
                                                               AirPackagingConfigurableBase.Location.CustomDescriptor));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(packagingOptions, FlexBundle
                                    .message("custom.descriptor.not.found", device,
                                             FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())),
                                                                 AirPackagingConfigurableBase.Location.CustomDescriptor));
        }
      }
    }

    if (packagingOptions.getPackageFileName().isEmpty()) {
      errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
        .message("package.file.name.not.set", device), AirPackagingConfigurableBase.Location.PackageFileName));
    }

    for (FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = entry.FILE_PATH;
      String relPathInPackage = entry.PATH_IN_PACKAGE;
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
          .message("packaging.options.empty.file.name", device), AirPackagingConfigurableBase.Location.FilesToPackage));
      }
      else {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (file == null) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(packagingOptions, FlexBundle
                                    .message("packaging.options.file.not.found", device, FileUtil.toSystemDependentName(fullPath)),
                                                                 AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (relPathInPackage.isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
            .message("packaging.options.empty.relative.path", device), AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (file != null && file.isDirectory()) {
          if (FileUtil.isAncestor(file.getPath(), outputFolderPath, false)) {
            errorConsumer.consume(FlashProjectStructureProblem
                                    .createPackagingOptionsProblem(packagingOptions, FlexBundle
                                      .message("folder.to.package.includes.output", device, file.getPresentableUrl()),
                                                                   AirPackagingConfigurableBase.Location.FilesToPackage));
          }
          else if (!relPathInPackage.isEmpty() && !fullPath.endsWith("/" + relPathInPackage)) {
            errorConsumer.consume(
              FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
                .message("packaging.options.relative.path.not.matches", device, FileUtil.toSystemDependentName(relPathInPackage)),
                                                                         AirPackagingConfigurableBase.Location.FilesToPackage));
          }
        }
      }
    }

    if (packagingOptions instanceof IosPackagingOptions) {
      final String path = packagingOptions.getSigningOptions().getIOSSdkPath();
      if (!path.isEmpty() && !new File(path).isDirectory()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
          .message("packaging.options.bad.ios.sdk.path", device, FileUtil.toSystemDependentName(path)),
                                                                                         AirPackagingConfigurableBase.Location.IosSdkPath));
      }
    }

    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    if (packagingOptions instanceof IosPackagingOptions) {
      final String provisioningProfilePath = signingOptions.getProvisioningProfilePath();
      if (provisioningProfilePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions, FlexBundle
          .message("ios.provisioning.profile.not.set"), AirPackagingConfigurableBase.Location.ProvisioningProfile));
      }
      else {
        final VirtualFile provisioningProfile = LocalFileSystem.getInstance().findFileByPath(provisioningProfilePath);
        if (provisioningProfile == null || provisioningProfile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(packagingOptions, FlexBundle
                                    .message("ios.provisioning.profile.not.found", FileUtil.toSystemDependentName(provisioningProfilePath)),
                                                                 AirPackagingConfigurableBase.Location.ProvisioningProfile));
        }
      }
    }

    final boolean tempCertificate = !(packagingOptions instanceof IosPackagingOptions) && signingOptions.isUseTempCertificate();
    if (!tempCertificate) {
      final String keystorePath = signingOptions.getKeystorePath();
      if (keystorePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(packagingOptions,
                                                                                         FlexBundle.message("keystore.not.set", device),
                                                                                         AirPackagingConfigurableBase.Location.Keystore));
      }
      else {
        final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
        if (keystore == null || keystore.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(packagingOptions, FlexBundle
                                    .message("keystore.not.found", device, FileUtil.toSystemDependentName(keystorePath)),
                                                                 AirPackagingConfigurableBase.Location.Keystore));
        }
      }
    }
  }

  public ValidityState createValidityState(final DataInput in) throws IOException {
    return new EmptyValidityState();
  }

  private static class MyProcessingItem implements ProcessingItem {
    private final Module myModule;
    private final FlexBuildConfiguration myBC;

    private MyProcessingItem(final Module module, final FlexBuildConfiguration bc) {
      myModule = module;
      myBC = bc;
    }

    @NotNull
    public VirtualFile getFile() {
      return myModule.getModuleFile();
    }

    public ValidityState getValidityState() {
      return new EmptyValidityState();
    }
  }
}
