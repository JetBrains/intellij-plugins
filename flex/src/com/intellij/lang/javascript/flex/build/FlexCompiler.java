package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.RunMainClassPrecompileTask;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;

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
  private static final Key<Collection<Module>> MODULES_TO_SKIP_FLEX_FACET_COMPILATION =
    Key.create("MODULES_TO_SKIP_FLEX_FACET_COMPILATION");

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    final List<ProcessingItem> myItems = new ArrayList<ProcessingItem>();
    boolean doneSave = false;

    for (final Module module : context.getCompileScope().getAffectedModules()) {
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
        if (config.DO_BUILD) {
          // currently one MyProcessingItem(module) is added if module contains several Flex facets. Better solution could potentially exist.
          myItems.add(new MyProcessingItem(module));
          doneSave = ensureDocumentsSaved(doneSave);
          break;
        }
      }
    }

    final ProcessingItem[] items = myItems.toArray(new ProcessingItem[myItems.size()]);
    if (items.length > 0 && context.isRebuild()) {
      final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(context.getProject());
      flexCompilerHandler.quitCompilerShell();
      flexCompilerHandler.getCompilerDependenciesCache().clear();
    }
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        Arrays.sort(items, new Comparator<ProcessingItem>() {
          final Comparator<Module> moduleComparator = ModuleManager.getInstance(context.getProject()).moduleDependencyComparator();

          public int compare(final ProcessingItem o1, final ProcessingItem o2) {
            return moduleComparator.compare(((MyProcessingItem)o1).myModule, ((MyProcessingItem)o2).myModule);
          }
        });
      }
    });

    return items;
  }

  private static boolean ensureDocumentsSaved(boolean doneSave) {
    if (!doneSave) {
      Runnable runnable = new Runnable() {
        public void run() {
          ApplicationManager.getApplication().saveAll();
        }
      };

      if (ApplicationManager.getApplication().isDispatchThread()) {
        runnable.run();
      }
      else {
        ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.defaultModalityState());
      }

      doneSave = true;
    }
    return doneSave;
  }

  public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items) {
    // todo switch to mxmlc/compc processes if different SDKs used.
    final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(context.getProject());
    final FlexCompilerProjectConfiguration flexCompilerConfiguration = FlexCompilerProjectConfiguration.getInstance(context.getProject());

    final Collection<Module> modulesToSkip = context.getCompileScope().getUserData(MODULES_TO_SKIP_FLEX_FACET_COMPILATION);

    if (flexCompilerConfiguration.USE_FCSH) {
      for (ProcessingItem item : items) {
        try {
          final Module module = ((MyProcessingItem)item).myModule;

          if (modulesToSkip != null && modulesToSkip.contains(module)) {
            continue;
          }

          context.addMessage(CompilerMessageCategory.INFORMATION, FlexBundle.message("using.fcsh"), null, -1, -1);

          flexCompilerHandler.compileFlexModuleOrAllFlexFacets(module, context);
        }
        catch (IOException ex) {
          context.addMessage(CompilerMessageCategory.ERROR, ex.toString(), null, -1, -1);
        }
      }
    }
    else {
      final boolean builtIn = flexCompilerConfiguration.USE_BUILT_IN_COMPILER;

      final FlexBuildConfiguration overriddenConfig = context.getUserData(FlexCompilerHandler.OVERRIDE_BUILD_CONFIG);

      final Collection<FlexCompilationTask> compilationTasks = new ArrayList<FlexCompilationTask>();
      for (ProcessingItem item : items) {
        final Module module = ((MyProcessingItem)item).myModule;

        if (modulesToSkip != null && modulesToSkip.contains(module)) {
          continue;
        }

        if (PlatformUtils.isFlexIde() && FlexIdeUtils.isNewUI()) {
          // not enabled in IDEA yet
          appendFlexIdeBCCompilations(compilationTasks, module, builtIn);
        }
        else {
          if (overriddenConfig != null && module == overriddenConfig.getModule()) {
            final Pair<Boolean, String> validationResultWithMessage =
              validateConfiguration(overriddenConfig, module, FlexBundle.message("module.name", module.getName()), false);

            if (!validationResultWithMessage.first) {
              if (validationResultWithMessage.second != null) {
                context.addMessage(CompilerMessageCategory.ERROR, validationResultWithMessage.second, null, -1, -1);
              }
              return ProcessingItem.EMPTY_ARRAY;
            }
            compilationTasks.add(builtIn ? new BuiltInCompilationTask(module, null, overriddenConfig)
                                         : new MxmlcCompcCompilationTask(module, null, overriddenConfig));
          }
          else {
            if (ModuleType.get(module) instanceof FlexModuleType) {
              final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(module);
              if (config.DO_BUILD) {
                compilationTasks.add(builtIn ? new BuiltInCompilationTask(module, null, config)
                                             : new MxmlcCompcCompilationTask(module, null, config));
              }
            }
            else {
              final Collection<FlexFacet> flexFacets = FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID);
              for (FlexFacet flexFacet : flexFacets) {
                final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(flexFacet);
                if (config.DO_BUILD) {
                  compilationTasks.add(builtIn ? new BuiltInCompilationTask(module, flexFacet, config)
                                               : new MxmlcCompcCompilationTask(module, flexFacet, config));
                }
              }
            }
          }
          appendCssCompilationTasks(compilationTasks, module, builtIn);
        }
      }

      if (!compilationTasks.isEmpty()) {
        context.addMessage(CompilerMessageCategory.INFORMATION,
                           FlexBundle.message(builtIn ? "using.builtin.compiler" : "using.mxmlc.compc",
                                              flexCompilerConfiguration.MAX_PARALLEL_COMPILATIONS),
                           null, -1, -1);

        if (builtIn) {
          try {
            // todo take correct SDK from myFlexIdeConfig.DEPENDENCIES...
            final Sdk someSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(compilationTasks.iterator().next().getModule());
            flexCompilerHandler.getBuiltInFlexCompilerHandler().startCompilerIfNeeded(someSdk, context);
          }
          catch (IOException e) {
            context.addMessage(CompilerMessageCategory.ERROR, e.toString(), null, -1, -1);
            return ProcessingItem.EMPTY_ARRAY;
          }
        }

        new FlexCompilationManager(context, compilationTasks).compile();

        final int activeCompilationsNumber = flexCompilerHandler.getBuiltInFlexCompilerHandler().getActiveCompilationsNumber();
        if (activeCompilationsNumber != 0) {
          Logger.getInstance(getClass().getName()).error(activeCompilationsNumber + " Flex compilation(s) are not finished!");
        }
      }
    }

    FlexCompilerHandler.deleteTempFlexUnitFiles(context);
    return items;
  }

  private static void appendCssCompilationTasks(final Collection<FlexCompilationTask> compilationTasks,
                                                final Module module,
                                                final boolean builtInCompiler) {
    if (ModuleType.get(module) instanceof FlexModuleType) {
      final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(module);
      if (config.DO_BUILD) {
        for (String cssFilePath : config.CSS_FILES_LIST) {
          compilationTasks.add(builtInCompiler
                               ? new BuiltInCompilationTask.BuiltInCSSCompilationTask(module, null, config, cssFilePath)
                               : new MxmlcCompcCompilationTask.MxmlcCompcCssCompilationTask(module, null, config, cssFilePath));
        }
      }
    }
    else {
      final Collection<FlexFacet> flexFacets = FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID);
      for (FlexFacet flexFacet : flexFacets) {
        final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(flexFacet);
        if (config.DO_BUILD) {
          for (String cssFilePath : config.CSS_FILES_LIST) {
            compilationTasks.add(builtInCompiler
                                 ? new BuiltInCompilationTask.BuiltInCSSCompilationTask(module, flexFacet, config, cssFilePath)
                                 : new MxmlcCompcCompilationTask.MxmlcCompcCssCompilationTask(module, flexFacet, config, cssFilePath));
          }
        }
      }
    }
  }

  private static void appendFlexIdeBCCompilations(final Collection<FlexCompilationTask> compilationTasks,
                                                  final Module module,
                                                  final boolean builtInCompiler) {
    if (ModuleType.get(module) instanceof FlexModuleType) {
      for (final FlexIdeBuildConfiguration config : FlexIdeBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
        compilationTasks.add(builtInCompiler ? new BuiltInCompilationTask(module, config)
                                             : new MxmlcCompcCompilationTask(module, config));
      }
    }
  }

  @NotNull
  public String getDescription() {
    return "ActionScript Compiler";
  }

  public boolean validateConfiguration(final CompileScope scope) {
    if (PlatformUtils.isFlexIde() && FlexIdeUtils.isNewUI()) {
      // todo implement
      return true;
    }
    
    Module moduleToSkipValidation = null; // will be validated later in FlexUnitPrecompilerTask or RunMainClassPrecompileTask
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(scope);
    if (runConfiguration instanceof FlexUnitRunConfiguration ||
        RunMainClassPrecompileTask.isMainClassBasedFlexRunConfiguration(runConfiguration)) {
      try {
        moduleToSkipValidation =
          FlexRunConfiguration.getAndValidateModule(runConfiguration.getProject(),
                                                    ((FlexRunConfiguration)runConfiguration).getRunnerParameters().getModuleName());
      }
      catch (RuntimeConfigurationError e) {/*ignore, error will be reported later*/}
    }

    for (final Module module : scope.getAffectedModules()) {
      if (module.equals(moduleToSkipValidation)) {
        continue;
      }

      if (ModuleType.get(module) instanceof FlexModuleType) {
        final Pair<Boolean, String> validationResultWithMessage =
          validateConfiguration(FlexBuildConfiguration.getInstance(module), module, FlexUtils.getPresentableName(module, null), true);
        if (!validationResultWithMessage.first) {
          if (validationResultWithMessage.second != null) {
            Messages.showErrorDialog(module.getProject(), validationResultWithMessage.second, FlexBundle.message("flex.compiler.problem"));
          }
          return false;
        }
      }
      else {
        final Collection<FlexFacet> flexFacets = FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID);
        for (final FlexFacet flexFacet : flexFacets) {
          final Pair<Boolean, String> validationResultWithMessage =
            validateConfiguration(FlexBuildConfiguration.getInstance(flexFacet), module, FlexUtils.getPresentableName(module, flexFacet),
                                  true);
          if (!validationResultWithMessage.first) {
            if (validationResultWithMessage.second != null) {
              Messages
                .showErrorDialog(module.getProject(), validationResultWithMessage.second, FlexBundle.message("flex.compiler.problem"));
            }

            if (runConfiguration != null && !(runConfiguration instanceof FlexRunConfiguration)) {
              // if the compilation triggered by 'Make before run' step of some non-Flex run configuration, let it proceed even with misconfigured Flex facet.
              Collection<Module> modulesToSkip = scope.getUserData(MODULES_TO_SKIP_FLEX_FACET_COMPILATION);
              if (modulesToSkip == null) {
                modulesToSkip = new ArrayList<Module>();
              }
              modulesToSkip.add(module);
              scope.putUserData(MODULES_TO_SKIP_FLEX_FACET_COMPILATION, modulesToSkip);
              return true;
            }
            return false;
          }
        }
      }
    }
    return true;
  }

  @NotNull
  public static Pair<Boolean, String> validateConfiguration(final FlexBuildConfiguration config,
                                                            final Module module,
                                                            final String presentableModuleOrFacetName,
                                                            final boolean chooseMainClassIfNeeded) {
    if (!config.DO_BUILD) return Pair.create(true, null);

    final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    if (flexSdk == null) {
      return Pair.create(false, FlexBundle.message("flex.sdk.not.set.for", presentableModuleOrFacetName));
    }

    if (flexSdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = flexSdk.getSdkAdditionalData();
      if (data == null ||
          !(data instanceof FlexmojosSdkAdditionalData) ||
          ((FlexmojosSdkAdditionalData)data).getFlexCompilerClasspath().isEmpty()) {
        return Pair.create(false, FlexBundle.message("sdk.flex.compiler.classpath.not.set", flexSdk.getName()));
      }
    }
    else {
      final VirtualFile sdkRoot = flexSdk.getHomeDirectory();
      if (sdkRoot == null || !sdkRoot.isValid()) {
        return Pair.create(false, FlexBundle.message("sdk.home.directory.not.found.for", flexSdk.getName()));
      }
    }

    if (config.USE_CUSTOM_CONFIG_FILE) {
      if (config.getType() == FlexBuildConfiguration.Type.FlexUnit && config.USE_CUSTOM_CONFIG_FILE_FOR_TESTS) {
        if (StringUtil.isEmptyOrSpaces(config.CUSTOM_CONFIG_FILE_FOR_TESTS)) {
          return Pair.create(false, FlexBundle.message("flex.compiler.config.file.for.tests.not.specified", presentableModuleOrFacetName));
        }
        final VirtualFile configFileForTests =
          VfsUtil.findRelativeFile(config.CUSTOM_CONFIG_FILE_FOR_TESTS, FlexUtils.getFlexCompilerWorkDir(module.getProject(), null));
        if (configFileForTests == null || !configFileForTests.isValid() || configFileForTests.isDirectory()) {
          return Pair.create(false, FlexBundle.message("flex.compiler.config.file.for.tests.not.found", config.CUSTOM_CONFIG_FILE_FOR_TESTS,
                                                       presentableModuleOrFacetName));
        }
      }
      else {
        if (StringUtil.isEmptyOrSpaces(config.CUSTOM_CONFIG_FILE)) {
          return Pair.create(false, FlexBundle.message("flex.compiler.config.file.not.specified", presentableModuleOrFacetName));
        }
        final VirtualFile configFile =
          VfsUtil.findRelativeFile(config.CUSTOM_CONFIG_FILE, FlexUtils.getFlexCompilerWorkDir(module.getProject(), null));
        if (configFile == null || !configFile.isValid() || configFile.isDirectory()) {
          return Pair.create(false,
                             FlexBundle.message("flex.compiler.config.file.not.found", config.CUSTOM_CONFIG_FILE,
                                                presentableModuleOrFacetName));
        }
      }
    }
    else {
      if (chooseMainClassIfNeeded &&
          config.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION) &&
          StringUtil.isEmptyOrSpaces(FlexUtils.getPathToMainClassFile(config))) {
        final ChooseMainClassDialog dialog =
          new ChooseMainClassDialog(module, presentableModuleOrFacetName, config.MAIN_CLASS, FlexBundle.message("flex.compiler.problem"));
        dialog.show();
        if (dialog.isOK()) {
          config.MAIN_CLASS = dialog.getMainClassName();
        }
        else {
          return Pair.create(false, null);
        }
      }

      if (config.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION)) {
        if (StringUtil.isEmptyOrSpaces(config.MAIN_CLASS)) {
          return Pair.create(false, FlexBundle.message("main.class.not.set.for", presentableModuleOrFacetName));
        }
        else if (StringUtil.isEmptyOrSpaces(FlexUtils.getPathToMainClassFile(config))) {
          return Pair.create(false, FlexBundle.message("main.class.not.found", config.MAIN_CLASS, presentableModuleOrFacetName));
        }
      }

      if (StringUtil.isEmptyOrSpaces(config.OUTPUT_FILE_NAME)) {
        return Pair.create(false, FlexBundle.message("output.file.name.not.specified.for", presentableModuleOrFacetName));
      }

      if (TargetPlayerUtils.isTargetPlayerApplicable(flexSdk) &&
          !TargetPlayerUtils.isTargetPlayerValid(config.TARGET_PLAYER_VERSION)) {
        return Pair.create(false, FlexBundle.message("invalid.target.player.version.for", config.TARGET_PLAYER_VERSION,
                                                     presentableModuleOrFacetName));
      }

      for (FlexBuildConfiguration.NamespaceAndManifestFileInfo info : config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST) {
        if (StringUtil.isEmptyOrSpaces(info.MANIFEST_FILE_PATH)) {
          return Pair.create(false, FlexBundle.message("flex.compiler.config.manifest.file.not.specified", presentableModuleOrFacetName));
        }
        final VirtualFile manifestFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(info.MANIFEST_FILE_PATH);
        if (manifestFile == null || manifestFile.isDirectory()) {
          return Pair.create(false, FlexBundle.message("flex.compiler.config.manifest.file.not.found", info.MANIFEST_FILE_PATH,
                                                       presentableModuleOrFacetName));
        }
      }

      for (FlexBuildConfiguration.ConditionalCompilationDefinition definition : config.CONDITIONAL_COMPILATION_DEFINITION_LIST) {
        if (!definition.NAME.matches(CONDITIONAL_COMPILATION_VARIABLE_PATTERN)) {
          return Pair.create(false, FlexBundle.message("incorrect.conditional.compilation.definition", definition.NAME));
        }
      }

      if (!StringUtil.isEmpty(config.PATH_TO_SERVICES_CONFIG_XML)) {
        final VirtualFile servicesConfigXml =
          VfsUtil.findRelativeFile(config.PATH_TO_SERVICES_CONFIG_XML, FlexUtils.getFlexCompilerWorkDir(module.getProject(), null));
        if (servicesConfigXml == null || servicesConfigXml.isDirectory()) {
          return Pair.create(false, FlexBundle.message("flex.services-config.xml.file.is.not.valid", config.PATH_TO_SERVICES_CONFIG_XML,
                                                       presentableModuleOrFacetName));
        }
      }
    }
    return Pair.create(true, null);
  }

  public ValidityState createValidityState(final DataInput in) throws IOException {
    return new EmptyValidityState();
  }

  private static class MyProcessingItem implements ProcessingItem {
    private final Module myModule;

    public MyProcessingItem(final Module module) {
      myModule = module;
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
