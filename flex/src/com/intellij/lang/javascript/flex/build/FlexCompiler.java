package com.intellij.lang.javascript.flex.build;

import com.intellij.CommonBundle;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
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
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;
import static com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions.ResourceFilesMode;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

public class FlexCompiler implements SourceProcessingCompiler {
  private static final Logger LOG = Logger.getInstance(FlexCompiler.class.getName());
  public static final Key<Collection<Pair<Module, FlexIdeBuildConfiguration>>> MODULES_AND_BCS_TO_COMPILE =
    Key.create("modules.and.bcs.to.compile");

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    saveProject(context.getProject());
    final List<ProcessingItem> itemList = new ArrayList<ProcessingItem>();

    try {
      for (final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC : getModulesAndBCsToCompile(context.getCompileScope())) {
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
      boolean builtIn = flexCompilerConfiguration.USE_BUILT_IN_COMPILER;
      final Sdk commonSdk = getSdkIfSame(items);

      if (builtIn && commonSdk == null) {
        builtIn = false;
        flexCompilerHandler.getBuiltInFlexCompilerHandler().stopCompilerProcess();
        context.addMessage(CompilerMessageCategory.INFORMATION, FlexBundle.message("can.not.use.built.in.compiler.shell"), null, -1, -1);
      }

      context.addMessage(CompilerMessageCategory.INFORMATION,
                         FlexBundle.message(builtIn ? "using.builtin.compiler" : "using.mxmlc.compc",
                                            flexCompilerConfiguration.MAX_PARALLEL_COMPILATIONS), null, -1, -1);
      final Collection<FlexCompilationTask> compilationTasks = new ArrayList<FlexCompilationTask>();
      for (final ProcessingItem item : items) {
        final Collection<FlexIdeBuildConfiguration> dependencies = new HashSet<FlexIdeBuildConfiguration>();
        // todo add 'optimize for' dependencies
        final FlexIdeBuildConfiguration bc = ((MyProcessingItem)item).myBC;
        for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
          if (entry instanceof BuildConfigurationEntry) {
            final FlexIdeBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
            if (dependencyBC != null && !dependencyBC.isSkipCompile() &&
                entry.getDependencyType().getLinkageType() != LinkageType.LoadInRuntime) {
              dependencies.add(dependencyBC);
            }
          }
        }

        compilationTasks.add(builtIn ? new BuiltInCompilationTask(((MyProcessingItem)item).myModule, bc, dependencies)
                                     : new MxmlcCompcCompilationTask(((MyProcessingItem)item).myModule, bc, dependencies));

        if (BCUtils.canHaveRuntimeStylesheets(bc)) {
          for (String cssPath : bc.getCssFilesToCompile()) {
            final VirtualFile cssFile = LocalFileSystem.getInstance().findFileByPath(cssPath);
            if (cssFile == null) continue;

            final ModifiableFlexIdeBuildConfiguration cssBC = Factory.getTemporaryCopyForCompilation(bc);
            cssBC.setMainClass(cssPath);
            cssBC.setOutputFileName(FileUtil.getNameWithoutExtension(PathUtil.getFileName(cssPath)) + ".swf");
            cssBC.setCssFilesToCompile(Collections.<String>emptyList());
            cssBC.getCompilerOptions().setResourceFilesMode(ResourceFilesMode.None);

            VirtualFile root = ProjectRootManager.getInstance(context.getProject()).getFileIndex().getSourceRootForFile(cssFile);
            if (root == null) root = ProjectRootManager.getInstance(context.getProject()).getFileIndex().getContentRootForFile(cssFile);
            final String relativePath = root == null ? null : VfsUtilCore.getRelativePath(cssFile.getParent(), root, '/');
            if (!StringUtil.isEmpty(relativePath)) {
              final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
              cssBC.setOutputFolder(outputFolder + "/" + relativePath);
            }

            compilationTasks.add(builtIn ? new BuiltInCompilationTask(((MyProcessingItem)item).myModule, cssBC, dependencies)
                                         : new MxmlcCompcCompilationTask(((MyProcessingItem)item).myModule, cssBC, dependencies));
          }
        }
      }

      if (builtIn) {
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

      FlexCompilerHandler.deleteTempFlexUnitFiles(context);
      return items;
    }
  }

  private static Map<Module, Collection<FlexIdeBuildConfiguration>> mapModuleToBCsWithResourceFiles(final ProcessingItem[] items) {
    final Map<Module, Collection<FlexIdeBuildConfiguration>> result = new THashMap<Module, Collection<FlexIdeBuildConfiguration>>();

    for (ProcessingItem item : items) {
      final Module module = ((MyProcessingItem)item).myModule;
      final FlexIdeBuildConfiguration bc = ((MyProcessingItem)item).myBC;
      if (!bc.isSkipCompile() && BCUtils.canHaveResourceFiles(bc.getNature()) &&
          bc.getCompilerOptions().getResourceFilesMode() != ResourceFilesMode.None) {
        Collection<FlexIdeBuildConfiguration> bcs = result.get(module);
        if (bcs == null) {
          bcs = new ArrayList<FlexIdeBuildConfiguration>();
          result.put(module, bcs);
        }
        bcs.add(bc);
      }
    }

    return result;
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
    final StringBuilder errors = new StringBuilder();

    try {
      final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCsToCompile = getModulesAndBCsToCompile(scope);
      for (final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
        final StringBuilder s = new StringBuilder();

        final Consumer<String> errorConsumer = new Consumer<String>() {
          @Override
          public void consume(final String message) {
            if (s.length() > 0) {
              s.append("\n");
            }
            s.append("- ").append(message);
          }
        };

        validateConfiguration(moduleAndBC.first, moduleAndBC.second, false, errorConsumer);

        if (moduleAndBC.second.getNature().isMobilePlatform() && moduleAndBC.second.getNature().isApp()) {
          final RunConfiguration runConfig = CompileStepBeforeRun.getRunConfiguration(scope);
          if (runConfig instanceof FlashRunConfiguration) {
            final FlashRunnerParameters params = ((FlashRunConfiguration)runConfig).getRunnerParameters();
            if (moduleAndBC.first.getName().equals(params.getModuleName()) &&
                moduleAndBC.second.getName().equals(params.getBCName()) &&
                params.getMobileRunTarget() == AirMobileRunTarget.AndroidDevice) {
              checkPackagingOptions(moduleAndBC.second.getAndroidPackagingOptions(), errorConsumer);
            }
          }
        }

        if (s.length() > 0) {
          if (errors.length() > 0) errors.append("\n\n");

          errors.append(FlexBundle.message("bc.0.of.module.1", moduleAndBC.second.getName(), moduleAndBC.first.getName()))
            .append(":\n").append(s);
        }
      }
      checkSimilarOutputFiles(modulesAndBCsToCompile);
    }
    catch (ConfigurationException e) {
      if (errors.length() > 0) errors.append("\n\n");
      errors.append("- ").append(e.getMessage());
    }

    if (errors.length() > 0) {
      final int choice =
        Messages.showDialog(errors.toString(), FlexBundle.message("project.setup.problem.title"),
                            new String[]{FlexBundle.message("open.project.structure"), CommonBundle.message("button.cancel")},
                            0, Messages.getErrorIcon());
      if (choice == 0) {
        final Module[] modules = scope.getAffectedModules();
        if (modules.length > 0) {
          ShowSettingsUtil.getInstance().editConfigurable(modules[0].getProject(),
                                                          ProjectStructureConfigurable.getInstance(modules[0].getProject()));
        }
      }

      return false;
    }

    return true;
  }

  private static boolean checkSimilarOutputFiles(final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCsToCompile)
    throws ConfigurationException {

    final Map<String, Pair<Module, FlexIdeBuildConfiguration>> outputPathToModuleAndBC =
      new THashMap<String, Pair<Module, FlexIdeBuildConfiguration>>();
    for (Pair<Module, FlexIdeBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      final FlexIdeBuildConfiguration bc = moduleAndBC.second;
      final String outputFilePath = bc.getActualOutputFilePath();
      checkOutputPathUnique(outputFilePath, moduleAndBC, outputPathToModuleAndBC);

      /*
      final String outputFolderPath = PathUtil.getParentPath(outputFilePath);
      if (bc.getTargetPlatform() == TargetPlatform.Mobile && bc.getOutputType() == OutputType.Application) {
        if (bc.getAndroidPackagingOptions().isEnabled()) {
          final String outputPath = outputFolderPath + "/" + bc.getAndroidPackagingOptions().getPackageFileName() + ".apk";
          checkOutputPathUnique(outputPath, moduleAndBC, outputPathToModuleAndBC);
        }
        if (bc.getIosPackagingOptions().isEnabled()) {
          final String outputPath = outputFolderPath + "/" + bc.getIosPackagingOptions().getPackageFileName() + ".ipa";
          checkOutputPathUnique(outputPath, moduleAndBC, outputPathToModuleAndBC);
        }
      }
      */
    }
    return true;
  }

  private static void checkOutputPathUnique(final String outputPath,
                                            final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC,
                                            final Map<String, Pair<Module, FlexIdeBuildConfiguration>> outputPathToModuleAndBC)
    throws ConfigurationException {
    final String caseAwarePath = SystemInfo.isFileSystemCaseSensitive ? outputPath : outputPath.toLowerCase();

    final Pair<Module, FlexIdeBuildConfiguration> existing = outputPathToModuleAndBC.put(caseAwarePath, moduleAndBC);
    if (existing != null) {
      throw new ConfigurationException(FlexBundle
                                         .message("same.output.files", moduleAndBC.second.getName(), moduleAndBC.first.getName(),
                                                  existing.second.getName(), existing.first.getName(),
                                                  FileUtil.toSystemDependentName(outputPath)));
    }
  }

  static Collection<Pair<Module, FlexIdeBuildConfiguration>> getModulesAndBCsToCompile(final CompileScope scope)
    throws ConfigurationException {

    final Collection<Pair<Module, FlexIdeBuildConfiguration>> result = new HashSet<Pair<Module, FlexIdeBuildConfiguration>>();
    final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCsToCompile = scope.getUserData(MODULES_AND_BCS_TO_COMPILE);
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(scope);

    if (modulesAndBCsToCompile != null) {
      for (Pair<Module, FlexIdeBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
        if (!moduleAndBC.second.isSkipCompile()) {
          final FlexIdeBuildConfiguration bcWithForcedDebugStatus = forceDebugStatus(moduleAndBC.first.getProject(), moduleAndBC.second);
          result.add(Pair.create(moduleAndBC.first, bcWithForcedDebugStatus));
          appendBCDependencies(result, moduleAndBC.first, moduleAndBC.second);
        }
      }
    }
    else if (runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof FlexUnitRunConfiguration) {
      final BCBasedRunnerParameters params = runConfiguration instanceof FlashRunConfiguration
                                             ? ((FlashRunConfiguration)runConfiguration).getRunnerParameters()
                                             : ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC;

      final Ref<RuntimeConfigurationError> exceptionRef = new Ref<RuntimeConfigurationError>();
      moduleAndBC = ApplicationManager.getApplication().runReadAction(new NullableComputable<Pair<Module, FlexIdeBuildConfiguration>>() {
        public Pair<Module, FlexIdeBuildConfiguration> compute() {
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
        for (final FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
          if (!bc.isSkipCompile()) {
            result.add(Pair.create(module, bc));
          }
        }
      }
    }

    return result;
  }

  private static FlexIdeBuildConfiguration forceDebugStatus(final Project project, final FlexIdeBuildConfiguration bc) {
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

    // must not use getTemporaryCopyForCompilation() here because additional config file must not be merged with the generated one when compiling swf for release or AIR package
    final ModifiableFlexIdeBuildConfiguration result = Factory.getCopy(bc);
    final String additionalOptions = FlexUtils.removeOptions(bc.getCompilerOptions().getAdditionalOptions(), "debug", "compiler.debug");
    result.getCompilerOptions().setAdditionalOptions(additionalOptions + " -debug=" + String.valueOf(debug));

    return result;
  }

  private static void appendBCDependencies(final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCs,
                                           final Module module,
                                           final FlexIdeBuildConfiguration bc) throws ConfigurationException {
    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;

        final Module dependencyModule = bcEntry.findModule();
        final FlexIdeBuildConfiguration dependencyBC = dependencyModule == null ? null : bcEntry.findBuildConfiguration();

        if (dependencyModule == null || dependencyBC == null) {
          throw new ConfigurationException(FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(),
                                                              bc.getName(), module.getName()));
        }

        final Pair<Module, FlexIdeBuildConfiguration> dependencyModuleAndBC = Pair.create(dependencyModule, dependencyBC);
        if (!dependencyBC.isSkipCompile()) {
          if (modulesAndBCs.add(dependencyModuleAndBC)) {
            appendBCDependencies(modulesAndBCs, dependencyModule, dependencyBC);
          }
        }
      }
    }
  }

  private static void validateConfiguration(final Module module,
                                            final FlexIdeBuildConfiguration bc,
                                            final boolean checkPackaging,
                                            final Consumer<String> errorConsumer) {
    assert !bc.isSkipCompile();
    final String moduleName = module.getName();

    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      errorConsumer.consume(FlexBundle.message("sdk.not.set"));
    }

    InfoFromConfigFile info = InfoFromConfigFile.DEFAULT;

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null || additionalConfigFile.isDirectory()) {
        errorConsumer.consume(FlexBundle.message("additional.config.file.not.found", additionalConfigFilePath));
      }
      if (!bc.isTempBCForCompilation()) {
        info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(additionalConfigFilePath);
      }
    }

    final BuildConfigurationNature nature = bc.getNature();

    if (!nature.isLib() && info.getMainClass(module) == null && bc.getMainClass().isEmpty()) {
      errorConsumer.consume(FlexBundle.message("main.class.not.set"));
      // real main class validation is done later in CompilerConfigGenerator
    }

    if (info.getOutputFileName() == null && info.getOutputFolderPath() == null) {
      if (bc.getOutputFileName().isEmpty()) {
        errorConsumer.consume(FlexBundle.message("output.file.name.not.set"));
      }

      if (!nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swf")) {
        errorConsumer.consume(FlexBundle.message("output.file.wrong.extension", "swf"));
      }

      if (nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swc")) {
        errorConsumer.consume(FlexBundle.message("output.file.wrong.extension", "swc"));
      }

      if (bc.getOutputFolder().isEmpty()) {
        errorConsumer.consume(FlexBundle.message("output.folder.not.set"));
      }
    }

    if (nature.isWebPlatform() && nature.isApp() && bc.isUseHtmlWrapper()) {
      if (bc.getWrapperTemplatePath().isEmpty()) {
        errorConsumer.consume(FlexBundle.message("html.template.folder.not.set"));
      }
      final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
      if (templateDir == null || !templateDir.isDirectory()) {
        errorConsumer.consume(FlexBundle.message("html.template.folder.not.found", bc.getWrapperTemplatePath()));
      }
      else {
        final VirtualFile templateFile = templateDir.findChild(CreateHtmlWrapperTemplateDialog.HTML_WRAPPER_TEMPLATE_FILE_NAME);
        if (templateFile == null) {
          errorConsumer.consume(FlexBundle.message("no.index.template.html.file", bc.getWrapperTemplatePath()));
        }
        else {
          try {
            if (!VfsUtilCore.loadText(templateFile).contains(FlexCompilationUtils.SWF_MACRO)) {
              errorConsumer.consume(FlexBundle.message("no.swf.macro.in.template", FileUtil.toSystemDependentName(templateFile.getPath())));
            }
          }
          catch (IOException e) {
            errorConsumer.consume(
              FlexBundle.message("failed.to.load.template.file", FileUtil.toSystemDependentName(templateFile.getPath()), e.getMessage()));
          }
        }
      }
    }

    if (BCUtils.canHaveRuntimeStylesheets(bc)) {
      for (String cssPath : bc.getCssFilesToCompile()) {
        if (!cssPath.toLowerCase().endsWith(".css")) {
          errorConsumer.consume(FlexBundle.message("not.a.css.runtime.stylesheet", FileUtil.toSystemDependentName(cssPath)));
        }
        else if (LocalFileSystem.getInstance().findFileByPath(cssPath) == null) {
          errorConsumer.consume(FlexBundle.message("css.not.found", bc.getName(), moduleName, FileUtil.toSystemDependentName(cssPath)));
        }
      }
    }

    if (nature.isLib()) {
      for (String path : bc.getCompilerOptions().getFilesToIncludeInSWC()) {
        if (LocalFileSystem.getInstance().findFileByPath(path) == null) {
          errorConsumer.consume(FlexBundle.message("file.to.include.in.swc.not.found", FileUtil.toSystemDependentName(path)));
        }
      }
    }

    if (checkPackaging) {
      checkPackagingOptions(bc, errorConsumer);
    }

    // This verification is disabled because Vladimir Krivosheev has app on app dependency because he needs predictable compilation order.
    // So we do not check dependencies and ignore incompatible ones when doing highlighting and compilation.
    //checkDependencies(moduleName, bc);
  }

  private static void checkDependencies(final String moduleName, final FlexIdeBuildConfiguration bc) throws ConfigurationException {
    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        final FlexIdeBuildConfiguration dependencyBC = bcEntry.findBuildConfiguration();
        final LinkageType linkageType = bcEntry.getDependencyType().getLinkageType();

        if (dependencyBC == null) {
          throw new ConfigurationException(
            FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(), bc.getName(), moduleName));
        }

        if (!checkDependencyType(bc, dependencyBC, linkageType)) {
          throw new ConfigurationException(
            FlexBundle.message("bc.dependency.problem",
                               bc.getName(), moduleName, bc.getOutputType().getPresentableText(),
                               dependencyBC.getName(), bcEntry.getModuleName(), dependencyBC.getOutputType().getPresentableText(),
                               linkageType.getShortText()));
        }
      }
    }
  }

  public static void checkPackagingOptions(final FlexIdeBuildConfiguration bc, final Consumer<String> errorConsumer) {
    if (bc.getOutputType() != OutputType.Application) return;

    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      checkPackagingOptions(bc.getAirDesktopPackagingOptions(), errorConsumer);
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        checkPackagingOptions(bc.getAndroidPackagingOptions(), errorConsumer);
      }
      if (bc.getIosPackagingOptions().isEnabled()) {
        checkPackagingOptions(bc.getIosPackagingOptions(), errorConsumer);
      }
    }
  }

  private static void checkPackagingOptions(final AirPackagingOptions packagingOptions, final Consumer<String> errorConsumer) {
    final String device = packagingOptions instanceof AndroidPackagingOptions
                          ? "Android"
                          : packagingOptions instanceof IosPackagingOptions
                            ? "iOS"
                            : "";
    if (!packagingOptions.isUseGeneratedDescriptor()) {
      if (packagingOptions.getCustomDescriptorPath().isEmpty()) {
        errorConsumer.consume(FlexBundle.message("custom.descriptor.not.set", device));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          errorConsumer.consume(FlexBundle.message("custom.descriptor.not.found", device, FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())));
        }
      }
    }

    if (packagingOptions.getPackageFileName().isEmpty()) {
      errorConsumer.consume(FlexBundle.message("package.file.name.not.set", device));
    }

    for (FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = entry.FILE_PATH;
      String relPathInPackage = entry.PATH_IN_PACKAGE;
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.isEmpty()) {
        errorConsumer.consume(FlexBundle.message("packaging.options.empty.file.name", device));
      }
      else {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (file == null) {
          errorConsumer.consume(FlexBundle.message("packaging.options.file.not.found", device, FileUtil.toSystemDependentName(fullPath)));
        }

        if (relPathInPackage.length() == 0) {
          errorConsumer.consume(FlexBundle.message("packaging.options.empty.relative.path", device));
        }

        if (file != null && file.isDirectory() && !fullPath.endsWith("/" + relPathInPackage)) {
          errorConsumer.consume(
            FlexBundle.message("packaging.options.relative.path.not.matches", device, FileUtil.toSystemDependentName(relPathInPackage)));
        }
      }
    }

    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    if (packagingOptions instanceof IosPackagingOptions) {
      final String provisioningProfilePath = signingOptions.getProvisioningProfilePath();
      if (provisioningProfilePath.isEmpty()) {
        errorConsumer.consume(FlexBundle.message("ios.provisioning.profile.not.set"));
      }
      else {
        final VirtualFile provisioningProfile = LocalFileSystem.getInstance().findFileByPath(provisioningProfilePath);
        if (provisioningProfile == null || provisioningProfile.isDirectory()) {
          errorConsumer
            .consume(FlexBundle.message("ios.provisioning.profile.not.found", FileUtil.toSystemDependentName(provisioningProfilePath)));
        }
      }
    }

    final boolean tempCertificate = !(packagingOptions instanceof IosPackagingOptions) && signingOptions.isUseTempCertificate();
    if (!tempCertificate) {
      final String keystorePath = signingOptions.getKeystorePath();
      if (keystorePath.isEmpty()) {
        errorConsumer.consume(FlexBundle.message("keystore.not.set", device));
      }
      else {
        final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
        if (keystore == null || keystore.isDirectory()) {
          errorConsumer.consume(FlexBundle.message("keystore.not.found", device, FileUtil.toSystemDependentName(keystorePath)));
        }
      }
    }
  }

  public static boolean checkDependencyType(final FlexIdeBuildConfiguration bc,
                                            final FlexIdeBuildConfiguration dependencyBC,
                                            final LinkageType linkageType) {
    final boolean ok;

    switch (dependencyBC.getOutputType()) {
      case Application:
        ok = false;
        break;
      case RuntimeLoadedModule:
        ok = bc.getOutputType() == OutputType.Application && linkageType == LinkageType.LoadInRuntime;
        break;
      case Library:
        ok = ArrayUtil.contains(linkageType, LinkageType.getSwcLinkageValues());
        break;
      default:
        assert false;
        ok = false;
    }

    return ok;
  }

  public ValidityState createValidityState(final DataInput in) throws IOException {
    return new EmptyValidityState();
  }

  static boolean isSourceFile(final VirtualFile file) {
    final String ext = file.getExtension();
    return ext != null && (ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg"));
  }

  private static class MyProcessingItem implements ProcessingItem {
    private final Module myModule;
    private final FlexIdeBuildConfiguration myBC;

    private MyProcessingItem(final Module module, final FlexIdeBuildConfiguration bc) {
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
