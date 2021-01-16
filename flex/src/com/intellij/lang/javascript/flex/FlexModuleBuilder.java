// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationType;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FlexModuleBuilder extends ModuleBuilder {

  private TargetPlatform myTargetPlatform = TargetPlatform.Web;
  private boolean isPureActionScript = false;
  private OutputType myOutputType = OutputType.Application;
  private boolean myAndroidEnabled;
  private boolean myIOSEnabled;
  private Sdk myFlexSdk;
  private String myTargetPlayer;
  private boolean myCreateSampleApp;
  private String mySampleAppName;
  private boolean myCreateHtmlWrapperTemplate;
  private boolean myEnableHistory;
  private boolean myCheckPlayerVersion;
  private boolean myExpressInstall;

  @Override
  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void setTargetPlatform(final TargetPlatform targetPlatform) {
    myTargetPlatform = targetPlatform;
  }

  public void setPureActionScript(final boolean pureActionScript) {
    isPureActionScript = pureActionScript;
  }

  public void setOutputType(final OutputType outputType) {
    myOutputType = outputType;
  }

  public void setAndroidEnabled(final boolean enabled) {
    myAndroidEnabled = enabled;
  }

  public void setIOSEnabled(final boolean enabled) {
    myIOSEnabled = enabled;
  }

  public void setFlexSdk(final Sdk flexSdk) {
    myFlexSdk = flexSdk;
  }

  public void setTargetPlayer(final String targetPlayer) {
    myTargetPlayer = targetPlayer;
  }

  public void setCreateSampleApp(final boolean createSampleApp) {
    myCreateSampleApp = createSampleApp;
  }

  public void setSampleAppName(final String sampleAppName) {
    mySampleAppName = sampleAppName;
  }

  public void setCreateHtmlWrapperTemplate(final boolean createHtmlWrapperTemplate) {
    myCreateHtmlWrapperTemplate = createHtmlWrapperTemplate;
  }

  public void setHtmlWrapperTemplateParameters(final boolean enableHistory,
                                               final boolean checkPlayerVersion,
                                               final boolean expressInstall) {
    myEnableHistory = enableHistory;
    myCheckPlayerVersion = checkPlayerVersion;
    myExpressInstall = expressInstall;
  }

  @Override
  public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    if (contentEntry == null) return;

    final VirtualFile sourceRoot = createSourceRoot(contentEntry);

    final Module module = modifiableRootModel.getModule();

    final FlexProjectConfigurationEditor currentFlexEditor =
      FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    final boolean needToCommitFlexEditor = currentFlexEditor == null;

    final FlexProjectConfigurationEditor flexConfigEditor;

    flexConfigEditor = currentFlexEditor != null
                       ? currentFlexEditor
                       : FlexProjectConfigurationEditor
                         .createEditor(module.getProject(), Collections.singletonMap(module, modifiableRootModel), null, null);

    final ModifiableFlexBuildConfiguration[] configurations = flexConfigEditor.getConfigurations(module);
    assert configurations.length == 1;
    final ModifiableFlexBuildConfiguration bc = configurations[0];

    setupBC(modifiableRootModel, bc);

    if (bc.getOutputType() == OutputType.Application) {
      createRunConfiguration(module, bc);
    }

    if (sourceRoot != null && myCreateSampleApp && myFlexSdk != null) {
      try {
        final boolean flex4 = !FlexSdkUtils.isAirSdkWithoutFlex(myFlexSdk) &&
                              StringUtil.compareVersionNumbers(myFlexSdk.getVersionString(), "4") >= 0;
        FlexUtils.createSampleApp(module.getProject(), sourceRoot, mySampleAppName, myTargetPlatform, flex4);
      }
      catch (IOException ex) {
        throw new ConfigurationException(ex.getMessage());
      }
    }

    if (myCreateHtmlWrapperTemplate && myFlexSdk != null) {
      final String path = VfsUtilCore.urlToPath(contentEntry.getUrl()) + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME;
      if (CreateHtmlWrapperTemplateDialog.createHtmlWrapperTemplate(module.getProject(), myFlexSdk, path,
                                                                    myEnableHistory, myCheckPlayerVersion, myExpressInstall)) {
        bc.setUseHtmlWrapper(true);
        bc.setWrapperTemplatePath(path);
      }
    }

    if (needToCommitFlexEditor) {
      flexConfigEditor.commit();
    }
  }

  private void setupBC(final ModifiableRootModel modifiableRootModel, final ModifiableFlexBuildConfiguration bc) {
    final Module module = modifiableRootModel.getModule();
    bc.setName(module.getName());
    bc.setTargetPlatform(myTargetPlatform);
    bc.setPureAs(isPureActionScript);
    bc.setOutputType(myOutputType);
    final BuildConfigurationNature nature = bc.getNature();

    if (myCreateSampleApp) {
      final String className = FileUtilRt.getNameWithoutExtension(mySampleAppName);

      bc.setMainClass(className);
      bc.setOutputFileName(className + (myOutputType == OutputType.Library ? ".swc" : ".swf"));

      if (nature.isApp()) {
        if (nature.isDesktopPlatform()) {
          bc.getAirDesktopPackagingOptions().setPackageFileName(className);
        }
        else if (nature.isMobilePlatform()) {
          bc.getAndroidPackagingOptions().setEnabled(myAndroidEnabled);
          bc.getAndroidPackagingOptions().setPackageFileName(className);

          bc.getIosPackagingOptions().setEnabled(myIOSEnabled);
          bc.getIosPackagingOptions().setPackageFileName(className);
        }
      }
    }
    else {
      final String fileName = PathUtil.suggestFileName(module.getName());
      bc.setOutputFileName(fileName + (myOutputType == OutputType.Library ? ".swc" : ".swf"));

      if (nature.isApp()) {
        if (nature.isDesktopPlatform()) {
          bc.getAirDesktopPackagingOptions().setPackageFileName(fileName);
        }
        else if (nature.isMobilePlatform()) {
          bc.getAndroidPackagingOptions().setEnabled(myAndroidEnabled);
          bc.getAndroidPackagingOptions().setPackageFileName(fileName);

          bc.getIosPackagingOptions().setEnabled(myIOSEnabled);
          bc.getIosPackagingOptions().setPackageFileName(fileName);
        }
      }
    }

    bc.setOutputFolder(VfsUtilCore.urlToPath(modifiableRootModel.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputUrl()));

    bc.getDependencies().setSdkEntry(myFlexSdk != null ? Factory.createSdkEntry(myFlexSdk.getName()) : null);
    if (myTargetPlayer != null) {
      bc.getDependencies().setTargetPlayer(myTargetPlayer);
    }
  }

  public static void createRunConfiguration(final Module module, final FlexBuildConfiguration bc) {
    final RunManager runManager = RunManager.getInstance(module.getProject());

    final List<RunConfiguration> existingConfigurations = runManager.getConfigurationsList(FlashRunConfigurationType.getInstance());
    for (RunConfiguration configuration : existingConfigurations) {
      final FlashRunnerParameters parameters = ((FlashRunConfiguration)configuration).getRunnerParameters();
      if (module.getName().equals(parameters.getModuleName()) && bc.getName().equals(parameters.getBCName())) {
        //already exists
        return;
      }
    }

    final RunnerAndConfigurationSettings settings = runManager.createConfiguration("", FlashRunConfigurationType.class);
    final FlashRunConfiguration runConfiguration = (FlashRunConfiguration)settings.getConfiguration();
    final FlashRunnerParameters params = runConfiguration.getRunnerParameters();
    params.setModuleName(module.getName());
    params.setBCName(bc.getName());

    if (bc.getNature().isMobilePlatform()) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.Android);
      }
      else if (bc.getIosPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.IOS);
      }
    }

    settings.setName(params.suggestUniqueName(existingConfigurations));
    settings.setTemporary(false);
    runManager.addConfiguration(settings);
    runManager.setSelectedConfiguration(settings);
  }

  @Nullable
  private VirtualFile createSourceRoot(final ContentEntry contentEntry) {
    final VirtualFile contentRoot = contentEntry.getFile();
    if (contentRoot == null) return null;

    VirtualFile sourceRoot = VfsUtil.findRelativeFile(contentRoot, "src");

    if (sourceRoot == null) {
      sourceRoot = ApplicationManager.getApplication().runWriteAction(new NullableComputable<>() {
        @Override
        public VirtualFile compute() {
          try {
            return contentRoot.createChildDirectory(this, "src");
          }
          catch (IOException e) {
            return null;
          }
        }
      });
    }

    if (sourceRoot != null) {
      contentEntry.addSourceFolder(sourceRoot, false);
      return sourceRoot;
    }

    return null;
  }

  @Nullable
  @Override
  public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
    return new SetupFlexModuleWizardStep(this);
  }

  @Override
  public String getParentGroup() {
    return WebModuleBuilder.GROUP_NAME;
  }
}
