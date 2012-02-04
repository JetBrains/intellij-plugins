package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 9, 2008
 *         Time: 11:22:58 PM
 */
public class FlexCompiler implements SourceProcessingCompiler {
  public static final String CONDITIONAL_COMPILATION_VARIABLE_PATTERN = "[a-zA-Z_$][a-zA-Z0-9_&]*::[a-zA-Z_$][a-zA-Z0-9_&]*";
  private static final Logger LOG = Logger.getInstance(FlexCompiler.class.getName());

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    saveProject(context.getProject());
    final List<ProcessingItem> itemList = new ArrayList<ProcessingItem>();

    try {
      for (final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig : getModulesAndConfigsToCompile(context.getCompileScope())) {
        itemList.add(new MyProcessingItem(moduleAndConfig.first, moduleAndConfig.second));
      }
    }
    catch (ConfigurationException e) {
      // can't happen because already validated
      throw new RuntimeException(e);
    }

    if (!itemList.isEmpty() && context.isRebuild()) {
      final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(context.getProject());
      flexCompilerHandler.quitCompilerShell();
      flexCompilerHandler.getCompilerDependenciesCache().clear();
    }

    if (!itemList.isEmpty() && context.isRebuild()) {
      final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(context.getProject());
      flexCompilerHandler.quitCompilerShell();
      flexCompilerHandler.getCompilerDependenciesCache().clear();
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

    if (flexCompilerConfiguration.USE_FCSH) {
      context.addMessage(CompilerMessageCategory.INFORMATION,
                         "FCSH tool is not supported yet. Please choose another compiler at File | Settings | Compiler | Flex Compiler",
                         null, -1, -1);
      return ProcessingItem.EMPTY_ARRAY;
    }
    else {
      boolean builtIn = flexCompilerConfiguration.USE_BUILT_IN_COMPILER;

      final Pair<String, String> sdkHomeAndVersion = getSdkHomeAndVersionIfSame(items);
      if (builtIn && sdkHomeAndVersion == null) {
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
        for (final DependencyEntry entry : ((MyProcessingItem)item).myBC.getDependencies().getEntries()) {
          if (entry instanceof BuildConfigurationEntry) {
            final FlexIdeBuildConfiguration dependencyConfig = ((BuildConfigurationEntry)entry).findBuildConfiguration();
            if (dependencyConfig != null && !dependencyConfig.isSkipCompile()) {
              dependencies.add(dependencyConfig);
            }
          }
        }

        compilationTasks
          .add(builtIn ? new BuiltInCompilationTask(((MyProcessingItem)item).myModule, ((MyProcessingItem)item).myBC, dependencies)
                       : new MxmlcCompcCompilationTask(((MyProcessingItem)item).myModule, ((MyProcessingItem)item).myBC, dependencies));
      }

      if (builtIn) {
        try {
          flexCompilerHandler.getBuiltInFlexCompilerHandler()
            .startCompilerIfNeeded(sdkHomeAndVersion.first, sdkHomeAndVersion.second, null, context);
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

  @SuppressWarnings("ConstantConditions") // already checked in validateConfiguration()
  @Nullable
  private static Pair<String, String> getSdkHomeAndVersionIfSame(final ProcessingItem[] items) {
    final Sdk sdk = ((MyProcessingItem)items[0]).myBC.getSdk();
    final String sdkHome = sdk.getHomePath();

    for (int i = 1; i < items.length; i++) {
      if (!sdkHome.equals(((MyProcessingItem)items[i]).myBC.getSdk().getHomePath())) {
        return null;
      }
    }

    return Pair.create(sdkHome, sdk.getVersionString());
  }

  @NotNull
  public String getDescription() {
    return "ActionScript Compiler";
  }

  public boolean validateConfiguration(final CompileScope scope) {
    try {
      // todo add quick fixes to ConfigurationException
      final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndConfigsToCompile = getModulesAndConfigsToCompile(scope);

      for (final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig : modulesAndConfigsToCompile) {
        validateConfiguration(moduleAndConfig.first.getName(), moduleAndConfig.second);
      }

      checkSimilarOutputFiles(modulesAndConfigsToCompile);
    }
    catch (ConfigurationException e) {
      final String title =
        ConfigurationException.DEFAULT_TITLE.equals(e.getTitle()) ? FlexBundle.message("project.setup.problem.title") : e.getTitle();
      Messages.showErrorDialog(e.getMessage(), title);
      return false;
    }

    return true;
  }

  private static boolean checkSimilarOutputFiles(final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndConfigsToCompile)
    throws ConfigurationException {

    final Map<String, Pair<Module, FlexIdeBuildConfiguration>> outputPathToModuleAndConfig =
      new THashMap<String, Pair<Module, FlexIdeBuildConfiguration>>();
    for (Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig : modulesAndConfigsToCompile) {
      final FlexIdeBuildConfiguration config = moduleAndConfig.second;
      checkOutputPathUnique(config.getOutputFilePath(), moduleAndConfig, outputPathToModuleAndConfig);

      if (config.getTargetPlatform() == TargetPlatform.Mobile && config.getOutputType() == OutputType.Application) {
        if (config.getAndroidPackagingOptions().isEnabled()) {
          final String outputPath = config.getOutputFolder() + "/" + config.getAndroidPackagingOptions().getPackageFileName();
          checkOutputPathUnique(outputPath, moduleAndConfig, outputPathToModuleAndConfig);
        }
        if (config.getIosPackagingOptions().isEnabled()) {
          final String outputPath = config.getOutputFolder() + "/" + config.getIosPackagingOptions().getPackageFileName();
          checkOutputPathUnique(outputPath, moduleAndConfig, outputPathToModuleAndConfig);
        }
      }
    }
    return true;
  }

  private static void checkOutputPathUnique(final String outputPath,
                                            final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig,
                                            final Map<String, Pair<Module, FlexIdeBuildConfiguration>> outputPathToModuleAndConfig)
    throws ConfigurationException {
    final String caseAwarePath = SystemInfo.isFileSystemCaseSensitive ? outputPath : outputPath.toLowerCase();

    final Pair<Module, FlexIdeBuildConfiguration> existing = outputPathToModuleAndConfig.put(caseAwarePath, moduleAndConfig);
    if (existing != null) {
      throw new ConfigurationException(FlexBundle
                                         .message("same.output.files", moduleAndConfig.second.getName(), moduleAndConfig.first.getName(),
                                                  existing.second.getName(), existing.first.getName(),
                                                  FileUtil.toSystemDependentName(outputPath)));
    }
  }

  private static Collection<Pair<Module, FlexIdeBuildConfiguration>> getModulesAndConfigsToCompile(final CompileScope scope)
    throws ConfigurationException {

    final Collection<Pair<Module, FlexIdeBuildConfiguration>> result = new HashSet<Pair<Module, FlexIdeBuildConfiguration>>();
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(scope);

    if (runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof NewFlexUnitRunConfiguration) {
      final BCBasedRunnerParameters params = runConfiguration instanceof FlashRunConfiguration
                                             ? ((FlashRunConfiguration)runConfiguration).getRunnerParameters()
                                             : ((NewFlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig;

      final Ref<RuntimeConfigurationError> exceptionRef = new Ref<RuntimeConfigurationError>();
      moduleAndConfig =
        ApplicationManager.getApplication().runReadAction(new NullableComputable<Pair<Module, FlexIdeBuildConfiguration>>() {
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

      if (!moduleAndConfig.second.isSkipCompile()) {
        result.add(moduleAndConfig);
        appendBCDependencies(result, moduleAndConfig.first, moduleAndConfig.second);
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

  private static void appendBCDependencies(final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndConfigs,
                                           final Module module,
                                           final FlexIdeBuildConfiguration config) throws ConfigurationException {
    for (final DependencyEntry entry : config.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;

        final Module otherModule = bcEntry.findModule();
        final FlexIdeBuildConfiguration otherConfig = otherModule == null ? null : bcEntry.findBuildConfiguration();

        if (otherModule == null || otherConfig == null) {
          throw new ConfigurationException(FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(),
                                                              config.getName(), module.getName()));
        }

        final Pair<Module, FlexIdeBuildConfiguration> otherModuleAndConfig = Pair.create(otherModule, otherConfig);
        if (!otherConfig.isSkipCompile()) {
          if (modulesAndConfigs.add(otherModuleAndConfig)) {
            appendBCDependencies(modulesAndConfigs, otherModule, otherConfig);
          }
        }
      }
    }
  }

  private static void validateConfiguration(final String moduleName, final FlexIdeBuildConfiguration bc) throws ConfigurationException {
    assert !bc.isSkipCompile();
    final BuildConfigurationNature nature = bc.getNature();

    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new ConfigurationException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
    }

    if (!nature.isLib() && bc.getMainClass().isEmpty()) {
      throw new ConfigurationException(FlexBundle.message("main.class.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
      // real main class validation is done later in CompilerConfigGenerator
    }

    if (bc.getOutputFileName().isEmpty()) {
      throw new ConfigurationException(FlexBundle.message("output.file.name.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
    }

    if (!nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swf")) {
      throw new ConfigurationException(
        FlexBundle.message("output.file.name.must.have.2.extension.for.bc.0.of.module.1", bc.getName(), moduleName, "swf"));
    }

    if (nature.isLib() && !bc.getOutputFileName().toLowerCase().endsWith(".swc")) {
      throw new ConfigurationException(
        FlexBundle.message("output.file.name.must.have.2.extension.for.bc.0.of.module.1", bc.getName(), moduleName, "swc"));
    }

    if (bc.getOutputFolder().isEmpty()) {
      throw new ConfigurationException(FlexBundle.message("output.folder.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
    }

    if (nature.isWebPlatform() && nature.isApp() && bc.isUseHtmlWrapper()) {
      if (bc.getWrapperTemplatePath().isEmpty()) {
        throw new ConfigurationException(FlexBundle.message("html.template.folder.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
      }
      final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
      if (templateDir == null || !templateDir.isDirectory()) {
        throw new ConfigurationException(FlexBundle.message("html.template.folder.not.found.for.bc.0.of.module.1.2",
                                                            bc.getName(), moduleName, bc.getWrapperTemplatePath()));
      }
      final VirtualFile templateFile = templateDir.findChild(CreateHtmlWrapperTemplateDialog.HTML_WRAPPER_TEMPLATE_FILE_NAME);
      if (templateFile == null) {
        throw new ConfigurationException(
          FlexBundle.message("no.index.template.html.file.bc.0.of.module.1.2", bc.getName(), moduleName, bc.getWrapperTemplatePath()));
      }

      try {
        if (!VfsUtil.loadText(templateFile).contains(FlexCompilationUtils.SWF_MACRO)) {
          throw new ConfigurationException(FlexBundle.message("no.swf.macro.in.template.bc.0.of.module.1.2", bc.getName(), moduleName,
                                                              FileUtil.toSystemDependentName(templateFile.getPath())));
        }
      }
      catch (IOException e) {
        throw new ConfigurationException(FlexBundle.message("failed.to.load.file", templateFile.getPath(), e.getMessage()));
      }
    }

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null || additionalConfigFile.isDirectory()) {
        throw new ConfigurationException(
          FlexBundle.message("additional.config.file.not.found", additionalConfigFilePath, bc.getName(), moduleName));
      }
    }

    if (nature.isMobilePlatform() && nature.isApp()) {
      if (bc.getAndroidPackagingOptions().isEnabled() && bc.getAndroidPackagingOptions().getPackageFileName().isEmpty()) {
        throw new ConfigurationException(FlexBundle.message("android.package.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
      }
      if (bc.getIosPackagingOptions().isEnabled() && bc.getIosPackagingOptions().getPackageFileName().isEmpty()) {
        throw new ConfigurationException(
          FlexBundle.message("ios.package.not.set.for.bc.0.of.module.1", bc.getName(), moduleName));
      }
    }

    checkDependencies(moduleName, bc);
  }

  private static void checkDependencies(final String moduleName, final FlexIdeBuildConfiguration config) throws ConfigurationException {
    for (final DependencyEntry entry : config.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        checkDependencyType(moduleName, config, bcEntry.getModuleName(), bcEntry.findBuildConfiguration(),
                            bcEntry.getDependencyType().getLinkageType());
      }
    }
  }

  private static void checkDependencyType(final String moduleName,
                                          final FlexIdeBuildConfiguration config,
                                          final String dependencyModuleName,
                                          final FlexIdeBuildConfiguration dependencyConfig,
                                          final LinkageType linkageType) throws ConfigurationException {
    final BuildConfigurationNature nature = config.getNature();
    final boolean ok;

    switch (dependencyConfig.getOutputType()) {
      case Application:
        ok = false;
        break;
      case RuntimeLoadedModule:
        ok = nature.isApp() && linkageType == LinkageType.LoadInRuntime;
        break;
      case Library:
        ok = ArrayUtil.contains(linkageType, LinkageType.getSwcLinkageValues());
        break;
      default:
        assert false;
        ok = false;
    }

    if (!ok) {
      throw new ConfigurationException(
        FlexBundle.message("bc.dependency.problem",
                           config.getName(), moduleName, config.getOutputType().getPresentableText(),
                           dependencyConfig.getName(), dependencyModuleName, dependencyConfig.getOutputType().getPresentableText(),
                           linkageType.getShortText()));
    }
  }

  public ValidityState createValidityState(final DataInput in) throws IOException {
    return new EmptyValidityState();
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
