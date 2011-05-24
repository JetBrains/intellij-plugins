package com.intellij.lang.javascript.flex;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.lang.javascript.flex.actions.airdescriptor.AirDescriptorParameters;
import com.intellij.lang.javascript.flex.actions.airdescriptor.CreateAirDescriptorAction;
import com.intellij.lang.javascript.flex.actions.htmlwrapper.CreateHtmlWrapperAction;
import com.intellij.lang.javascript.flex.actions.htmlwrapper.CreateHtmlWrapperDialog;
import com.intellij.lang.javascript.flex.actions.htmlwrapper.HTMLWrapperParameters;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.run.*;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Dec 2, 2007
 * Time: 12:07:16 AM
 */
public class FlexModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
  private List<Pair<String, String>> mySourcePaths;
  private Sdk mySdk;
  private String myTargetPlayerVersion;
  private boolean myCreateSampleApp;
  private String mySampleAppFileName;
  private boolean myCreateCustomCompilerConfig;
  private String myCustomCompilerConfigFileName;
  private boolean myCreateHtmlWrapper;
  private boolean myCreateAirDescriptor;
  private String myAirDescriptorFileName;
  private boolean myCreateRunConfiguration;
  private String myPathToServicesConfigXml;
  private String myContextRoot;
  private String myFlexOutputType;

  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    final Module module = modifiableRootModel.getModule();
    setupResourceFilePatterns(module.getProject());

    if (TargetPlayerUtils.needToChangeSdk(mySdk, myTargetPlayerVersion)) {
      final Sdk sdk = TargetPlayerUtils.findOrCreateProperSdk(module.getProject(), mySdk, myTargetPlayerVersion);
      if (sdk != null) {
        mySdk = sdk;
      }
    }
    modifiableRootModel.setSdk(mySdk);

    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    if (contentEntry == null) return;
    if (mySourcePaths == null) return;

    final FlexBuildConfiguration config = setupFlexBuildConfiguration(module);
    final VirtualFile sourceRootForFlexSetup = setupSourceRoots(contentEntry);

    if (sourceRootForFlexSetup != null) {
      try {
        FlexUtils
          .setupFlexConfigFileAndSampleCode(module, config, mySdk, myCreateCustomCompilerConfig ? myCustomCompilerConfigFileName : null,
                                            contentEntry.getFile(), myCreateSampleApp ? mySampleAppFileName : null, sourceRootForFlexSetup);
      }
      catch (IOException ex) {
        throw new ConfigurationException(ex.getMessage());
      }

      if (myCreateHtmlWrapper) {
        scheduleHtmlWrapperCreation(module, sourceRootForFlexSetup, myCreateRunConfiguration);
      }
      else if (myCreateRunConfiguration) {
        createRunConfiguration(module, sourceRootForFlexSetup);
      }

      if (myCreateAirDescriptor) {
        createAirDescriptor(sourceRootForFlexSetup.getPath(), config);
      }
    }
  }

  private void scheduleHtmlWrapperCreation(final Module module, final VirtualFile sourceRoot, final boolean createRunConfiguration) {
    ApplicationManager.getApplication().invokeLater(new Runnable() { // should not be in write action
      boolean runConfigurationCreated = false;

      public void run() {
        final CreateHtmlWrapperDialog dialog = new CreateHtmlWrapperDialog(module.getProject());
        dialog.setModuleAndSdkAndWrapperLocation(module, mySdk, sourceRoot.getPath());
        dialog.suggestToCreateRunConfiguration(false);
        dialog.show();
        if (dialog.isOK()) {
          try {
            final HTMLWrapperParameters htmlWrapperParameters = dialog.getHTMLWrapperParameters();
            final VirtualFile htmlFile = CreateHtmlWrapperAction.createHtmlWrapper(htmlWrapperParameters);

            if (createRunConfiguration && htmlFile != null) {
              createFlexRunConfiguration(module.getProject(), htmlFile);
              runConfigurationCreated = true;
            }
          }
          catch (IOException ex) {
            Messages
              .showErrorDialog(module.getProject(), MessageFormat.format("Failed to create HTML wrapper\n{0}", ex.getMessage()), "Error");
          }
        }

        if (myCreateRunConfiguration && !runConfigurationCreated) {
          createFlexRunConfiguration(module, FlexBuildConfiguration.getInstance(module).OUTPUT_FILE_NAME);
        }
      }
    });
  }

  public static void createFlexRunConfiguration(final Project project, final VirtualFile htmlWrapperFile) {
    final Module module = ModuleUtil.findModuleForFile(htmlWrapperFile, project);
    final VirtualFile sourceRoot =
      module == null ? null : ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(htmlWrapperFile);
    if (sourceRoot == null) {
      createFlexRunConfiguration(project, module, htmlWrapperFile.getPath());
    }
    else {
      createFlexRunConfiguration(module, VfsUtil.getRelativePath(htmlWrapperFile, sourceRoot, '/'));
    }
  }

  public static void createFlexRunConfiguration(final Module module, final String filePathRelativeToOutputFolder) {
    final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
    final String absolutePath =
      (extension == null ? "" : VfsUtil.urlToPath(extension.getCompilerOutputUrl()) + "/") + filePathRelativeToOutputFolder;
    createFlexRunConfiguration(module.getProject(), module, absolutePath);
  }

  public static void createFlexRunConfiguration(final Project project, final @Nullable Module module, final String _absoluteFilePath) {
    final String absoluteFilePath = FileUtil.toSystemIndependentName(_absoluteFilePath);

    final RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
    final RunnerAndConfigurationSettings settings = runManager.createConfiguration("", FlexRunConfigurationType.getFactory());
    settings.setTemporary(false);
    runManager.addConfiguration(settings, false);
    runManager.setActiveConfiguration(settings);

    final FlexRunConfiguration runConfiguration = (FlexRunConfiguration)settings.getConfiguration();
    final FlexRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();
    runnerParameters.setModuleName(module == null ? "" : module.getName());
    runnerParameters.setRunMode(FlexRunnerParameters.RunMode.HtmlOrSwfFile);
    runnerParameters.setHtmlOrSwfFilePath(absoluteFilePath);
    settings.setName(runConfiguration.suggestedName());
  }

  private void createAirDescriptor(final String airDescriptorFolderPath, final FlexBuildConfiguration config)
    throws ConfigurationException {
    try {
      final String name = FileUtil.getNameWithoutExtension(config.OUTPUT_FILE_NAME);
      CreateAirDescriptorAction.createAirDescriptor(
        new AirDescriptorParameters(myAirDescriptorFileName, airDescriptorFolderPath, FlexSdkUtils.getAirVersion(mySdk), name, name, name,
                                    "0.0", config.OUTPUT_FILE_NAME, name, 400, 300,
                                    mySdk != null && mySdk.getSdkType() instanceof AirMobileSdkType));
    }
    catch (IOException e) {
      throw new ConfigurationException("Failed to create AIR application descriptor\n" + e.getMessage());
    }
  }

  private void createRunConfiguration(final Module module, final VirtualFile sourceRoot) {
    final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(module);

    if (mySdk != null && (mySdk.getSdkType() instanceof AirMobileSdkType || mySdk.getSdkType() instanceof AirSdkType)) {
      final IFlexSdkType.Subtype subtype = ((IFlexSdkType)mySdk.getSdkType()).getSubtype();
      final RunManagerEx runManager = RunManagerEx.getInstanceEx(module.getProject());

      final CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
      final String outputDirPath = moduleExtension == null ? "" : VfsUtil.urlToPath(moduleExtension.getCompilerOutputUrl());

      final RunnerAndConfigurationSettings settings = runManager.createConfiguration("", subtype == IFlexSdkType.Subtype.AIRMobile
                                                                                         ? AirMobileRunConfigurationType.getFactory()
                                                                                         : AirRunConfigurationType.getFactory());
      settings.setTemporary(false);
      runManager.addConfiguration(settings, false);
      runManager.setActiveConfiguration(settings);

      final AirRunConfiguration runConfiguration = (AirRunConfiguration)settings.getConfiguration();
      final AirRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();
      runnerParameters.setModuleName(module.getName());

      if (myCreateAirDescriptor) {
        if (runnerParameters instanceof AirMobileRunnerParameters) {
          final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)runnerParameters;
          mobileParams.setAirMobileRunMode(AirMobileRunnerParameters.AirMobileRunMode.AppDescriptor);
          mobileParams.setMobilePackageFileName(FileUtil.getNameWithoutExtension(config.OUTPUT_FILE_NAME) + ".apk");
        }
        else {
          runnerParameters.setAirRunMode(AirRunnerParameters.AirRunMode.AppDescriptor);
        }
        runnerParameters.setAirDescriptorPath(sourceRoot.getPath() + "/" + myAirDescriptorFileName);
        runnerParameters.setAirRootDirPath(outputDirPath);
      }
      else {
        if (runnerParameters instanceof AirMobileRunnerParameters) {
          final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)runnerParameters;
          mobileParams.setAirMobileRunMode(AirMobileRunnerParameters.AirMobileRunMode.MainClass);
          mobileParams.setMobilePackageFileName(config.MAIN_CLASS + ".apk");
        }
        else {
          runnerParameters.setAirRunMode(AirRunnerParameters.AirRunMode.MainClass);
        }
        runnerParameters.setMainClassName(config.MAIN_CLASS);
      }

      settings.setName(runConfiguration.suggestedName());
    }
    else {
      createFlexRunConfiguration(module, config.OUTPUT_FILE_NAME);
    }
  }

  @Nullable
  private VirtualFile setupSourceRoots(final ContentEntry contentEntry) {
    VirtualFile sourceRootForFlexSetup = null;
    for (Pair<String, String> p : mySourcePaths) {
      final VirtualFile sourceRoot = VfsUtil.findRelativeFile(p.first, null);
      if (sourceRoot != null) {
        contentEntry.addSourceFolder(sourceRoot, false);
        if (sourceRootForFlexSetup == null) {
          sourceRootForFlexSetup = sourceRoot;
        }
      }
    }
    return sourceRootForFlexSetup;
  }

  public static void setupResourceFilePatterns(final Project project) {
    final CompilerConfiguration configuration = CompilerConfiguration.getInstance(project);
    if (!configuration.isResourceFile("A.js")) configuration.addResourceFilePattern("?*.js");
    if (!configuration.isResourceFile("A.css")) configuration.addResourceFilePattern("?*.css");
    if (!configuration.isResourceFile("A.swf")) configuration.addResourceFilePattern("?*.swf");
  }

  private FlexBuildConfiguration setupFlexBuildConfiguration(final Module module) {
    final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(module);
    config.OUTPUT_FILE_NAME = suggestOutputFileName(module);
    config.DO_BUILD = true;
    config.OUTPUT_TYPE = myFlexOutputType;
    config.TARGET_PLAYER_VERSION = myTargetPlayerVersion;
    config.PATH_TO_SERVICES_CONFIG_XML = myPathToServicesConfigXml;
    config.CONTEXT_ROOT = myContextRoot;
    return config;
  }

  private String suggestOutputFileName(final Module module) {
    final String extension = FlexBuildConfiguration.APPLICATION.equals(myFlexOutputType) ? ".swf" : ".swc";
    if (myCreateSampleApp && mySampleAppFileName.indexOf(".") > 0) {
      return mySampleAppFileName.substring(0, mySampleAppFileName.indexOf(".")) + extension;
    }
    else {
      return module.getName().replaceAll("[^\\p{Alnum}]", "_") + extension;
    }
  }

  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public List<Pair<String, String>> getSourcePaths() {
    return mySourcePaths;
  }

  public void setSourcePaths(final List<Pair<String, String>> sourcePaths) {
    mySourcePaths = sourcePaths;
  }

  public void addSourcePath(final Pair<String, String> sourcePathInfo) {
    if (mySourcePaths == null) {
      mySourcePaths = new ArrayList<Pair<String, String>>();
    }
    mySourcePaths.add(sourcePathInfo);
  }

  public void setSdk(final Sdk sdk) {
    mySdk = sdk;
  }

  public void setTargetPlayerVersion(final String playerVersion) {
    myTargetPlayerVersion = playerVersion;
  }

  public void setCreateSampleApp(final boolean createSampleApp) {
    myCreateSampleApp = createSampleApp;
  }

  public void setSampleAppFileName(final String sampleAppFileName) {
    mySampleAppFileName = sampleAppFileName;
  }

  public void setCreateCustomCompilerConfig(final boolean createCustomCompilerConfig) {
    myCreateCustomCompilerConfig = createCustomCompilerConfig;
  }

  public void setCustomCompilerConfigFileName(final String createCompilerConfigurationFileName) {
    myCustomCompilerConfigFileName = createCompilerConfigurationFileName;
  }

  public void setCreateHtmlWrapper(final boolean selected) {
    myCreateHtmlWrapper = selected;
  }

  public void setCreateAirDescriptor(final boolean createAirDescriptor) {
    myCreateAirDescriptor = createAirDescriptor;
  }

  public void setAirDescriptorFileName(final String airDescriptorFileName) {
    myAirDescriptorFileName = airDescriptorFileName;
  }

  public void setCreateRunConfiguration(boolean createRunConfiguration) {
    myCreateRunConfiguration = createRunConfiguration;
  }

  public void setPathToServicesConfigXml(final String pathToServicesConfigXml) {
    myPathToServicesConfigXml = pathToServicesConfigXml;
  }

  public void setContextRoot(final String contextRoot) {
    myContextRoot = contextRoot;
  }

  public void setFlexOutputType(final String flexOutputType) {
    myFlexOutputType = flexOutputType;
  }
}
