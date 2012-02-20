package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AirRunConfiguration extends FlexRunConfiguration {

  public AirRunConfiguration(final Project project, final ConfigurationFactory configurationFactory, final String name) {
    super(project, configurationFactory, name);
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new AirRunConfigurationForm(getProject());
  }

  public AirRunConfiguration clone() {
    return (AirRunConfiguration)super.clone();
  }

  protected FlexRunnerParameters createRunnerParametersInstance() {
    return new AirRunnerParameters();
  }

  @NotNull
  public AirRunnerParameters getRunnerParameters() {
    return (AirRunnerParameters)super.getRunnerParameters();
  }

  public String suggestedName() {
    final String descriptorPath = getRunnerParameters().getAirDescriptorPath();
    return getRunnerParameters().getAirRunMode() == AirRunnerParameters.AirRunMode.MainClass
           ? StringUtil.getShortName(getRunnerParameters().getMainClassName())
           : getRunnerParameters().getAirRunMode() == AirRunnerParameters.AirRunMode.AppDescriptor
             ? descriptorPath.substring(descriptorPath.lastIndexOf('/') + 1)
             : "unnamed";
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    final AirRunnerParameters params = getRunnerParameters();
    final Module module = getAndValidateModule(getProject(), params.getModuleName());

    switch (params.getAirRunMode()) {
      case AppDescriptor:
        checkAirDescriptorBasedConfiguration(module, params);
        break;
      case MainClass:
        checkMainClassBasedConfiguration(module, params);
        break;
    }

    //checkAdlAndAirRuntime(module);

    checkDebuggerSdk(params);
  }

  protected static void checkAirDescriptorBasedConfiguration(Module module, AirRunnerParameters params) throws RuntimeConfigurationError {
    final String airDescriptorPath = params.getAirDescriptorPath().trim();
    if (airDescriptorPath.length() == 0) {
      throw new RuntimeConfigurationError(FlexBundle.message("air.descriptor.not.set"));
    }
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
    if (file == null || file.isDirectory()) {
      throw new RuntimeConfigurationError(FlexBundle.message("air.descriptor.not.found", airDescriptorPath));
    }

    final String rootDirPath = params.getAirRootDirPath().trim();
    if (rootDirPath.length() == 0) {
      throw new RuntimeConfigurationError(FlexBundle.message("root.directory.not.set"));
    }
    final CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
    if (moduleExtension == null ||
        !FileUtil.toSystemIndependentName(rootDirPath).equals(VfsUtil.urlToPath(moduleExtension.getCompilerOutputUrl()))) {
      final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(rootDirPath);
      if (dir == null || !dir.isDirectory()) {
        throw new RuntimeConfigurationError(FlexBundle.message("root.directory.not.found", rootDirPath));
      }
    }
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    CommandLineState state = new AirRunState(env);
    state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
    return state;
  }

  protected class AirRunState extends CommandLineState {

    private boolean myNeedToRemoveAirRuntimeDir;
    private VirtualFile myAirRuntimeDirForFlexmojosSdk;

    public AirRunState(ExecutionEnvironment env) {
      super(env);
    }

    GeneralCommandLine createCommandLine() throws ExecutionException {
      final AirRunnerParameters params = getRunnerParameters();
      final GeneralCommandLine commandLine = new GeneralCommandLine();
      final VirtualFile airDescriptorFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(params.getAirDescriptorPath());
      if (airDescriptorFile == null) throw new ExecutionException(FlexBundle.message("file.not.found", params.getAirDescriptorPath()));

      final VirtualFile airRootDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(params.getAirRootDirPath());
      if (airRootDir == null) throw new ExecutionException(FlexBundle.message("folder.does.not.exist", params.getAirRootDirPath()));

      final Module module = ModuleManager.getInstance(getProject()).findModuleByName(params.getModuleName());
      if (module == null) throw new ExecutionException(FlexBundle.message("module.not.found", params.getModuleName()));

      final Sdk sdk = FlexUtils.getSdkForActiveBC(module);
      if (sdk == null) {
        final String s = (ModuleType.get(module) instanceof FlexModuleType ? "module " : "Flex facet(s) of module ") + module.getName();
        throw new ExecutionException(FlexBundle.message("flex.sdk.not.set.for", s));
      }

      final String adlPath = FlexSdkUtils.getAdlPath(sdk);
      commandLine.setExePath(adlPath);

      if (sdk.getSdkType() instanceof FlexmojosSdkType) {
        final Pair<VirtualFile, Boolean> airRuntimeDirInfo;
        try {
          airRuntimeDirInfo = FlexSdkUtils.getAirRuntimeDirInfoForFlexmojosSdk(sdk);
        }
        catch (IOException e) {
          throw new ExecutionException(e.getMessage());
        }
        myNeedToRemoveAirRuntimeDir = airRuntimeDirInfo.second;
        myAirRuntimeDirForFlexmojosSdk = airRuntimeDirInfo.first;

        commandLine.addParameter("-runtime");
        commandLine.addParameter(myAirRuntimeDirForFlexmojosSdk.getPath());
      }
      else {
        myNeedToRemoveAirRuntimeDir = false;
        myAirRuntimeDirForFlexmojosSdk = null;
      }

      /*
      if (params instanceof AirMobileRunnerParameters) {
        final AirMobileRunnerParameters p = (AirMobileRunnerParameters)params;
        switch (p.getAirMobileRunTarget()) {
          case Emulator:
            commandLine.addParameter("-profile");
            commandLine.addParameter("mobileDevice");

            commandLine.addParameter("-screensize");
            final String adlAlias = p.getEmulator().adlAlias;
            if (adlAlias != null) {
              commandLine.addParameter(adlAlias);
            }
            else {
              commandLine.addParameter(
                p.getScreenWidth() + "x" + p.getScreenHeight() + ":" + p.getFullScreenWidth() + "x" + p.getFullScreenHeight());
            }
            break;
          case AndroidDevice:
            assert false;
            break;
        }
      }
      */

      final String adlOptions = params.getAdlOptions();
      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        commandLine.addParameters(StringUtil.split(adlOptions, " "));
      }
      commandLine.addParameter(params.getAirDescriptorPath());
      commandLine.addParameter(params.getAirRootDirPath());
      final String programParameters = params.getAirProgramParameters();
      if (!StringUtil.isEmptyOrSpaces(programParameters)) {
        commandLine.addParameter("--");
        commandLine.addParameters(StringUtil.split(programParameters, " "));
      }

      return commandLine;
    }

    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
      final OSProcessHandler processHandler = JavaCommandLineStateUtil.startProcess(createCommandLine());
      processHandler.addProcessListener(new ProcessAdapter() {
        public void processTerminated(final ProcessEvent event) {
          if (myNeedToRemoveAirRuntimeDir) {
            FlexUtils.removeFileLater(myAirRuntimeDirForFlexmojosSdk);
          }
        }
      });
      return processHandler;
    }
  }
}
