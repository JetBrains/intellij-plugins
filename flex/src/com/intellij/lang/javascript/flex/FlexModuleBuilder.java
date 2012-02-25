package com.intellij.lang.javascript.flex;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationType;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;

public class FlexModuleBuilder extends ModuleBuilder {

  private TargetPlatform myTargetPlatform = TargetPlatform.Web;
  private boolean isPureActionScript = false;
  private OutputType myOutputType = OutputType.Application;
  private Sdk myFlexSdk;
  private String myTargetPlayer;
  private boolean myCreateSampleApp;
  private String mySampleAppName;
  private boolean myCreateHtmlWrapperTemplate;
  private boolean myEnableHistory;
  private boolean myCheckPlayerVersion;
  private boolean myExpressInstall;

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

  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
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

    final ModifiableFlexIdeBuildConfiguration[] configurations = flexConfigEditor.getConfigurations(module);
    assert configurations.length == 1;
    final ModifiableFlexIdeBuildConfiguration bc = configurations[0];

    setupBC(module, bc);

    if (bc.getOutputType() == OutputType.Application) {
      createRunConfiguration(module, bc.getName());
    }

    if (sourceRoot != null && myCreateSampleApp && myFlexSdk != null) {
      try {
        final boolean flex4 = myFlexSdk.getVersionString().startsWith("4");
        FlexUtils.createSampleApp(module.getProject(), sourceRoot, mySampleAppName, myTargetPlatform, flex4);
      }
      catch (IOException ex) {
        throw new ConfigurationException(ex.getMessage());
      }
    }

    if (myCreateHtmlWrapperTemplate && myFlexSdk != null) {
      final String path = VfsUtil.urlToPath(contentEntry.getUrl()) + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME;
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

  private void setupBC(final Module module, final ModifiableFlexIdeBuildConfiguration bc) {
    bc.setName(module.getName());
    bc.setTargetPlatform(myTargetPlatform);
    bc.setPureAs(isPureActionScript);
    bc.setOutputType(myOutputType);

    final String className = FileUtil.getNameWithoutExtension(mySampleAppName);

    if (myCreateSampleApp) {
      bc.setMainClass(className);
      bc.setOutputFileName(className + (myOutputType == OutputType.Library ? ".swc" : ".swf"));
    }
    else {
      bc.setOutputFileName(module.getName() + (myOutputType == OutputType.Library ? ".swc" : ".swf"));
    }
    bc.setOutputFolder(VfsUtil.urlToPath(CompilerModuleExtension.getInstance(module).getCompilerOutputUrl()));

    bc.getDependencies().setSdkEntry(myFlexSdk != null ? Factory.createSdkEntry(myFlexSdk.getName()) : null);
    if (myTargetPlayer != null) {
      bc.getDependencies().setTargetPlayer(myTargetPlayer);
    }

    if (myTargetPlatform == TargetPlatform.Mobile && myOutputType == OutputType.Application) {
      bc.getAndroidPackagingOptions().setEnabled(true);
      bc.getAndroidPackagingOptions().setPackageFileName(className);
      bc.getIosPackagingOptions().setPackageFileName(className);
    }
  }

  public static void createRunConfiguration(final Module module, final String bcName) {
    final RunManagerEx runManager = RunManagerEx.getInstanceEx(module.getProject());

    final RunConfiguration[] existingConfigurations = runManager.getConfigurations(FlashRunConfigurationType.getInstance());
    for (RunConfiguration configuration : existingConfigurations) {
      final FlashRunnerParameters parameters = ((FlashRunConfiguration)configuration).getRunnerParameters();
      if (module.getName().equals(parameters.getModuleName()) && bcName.equals(parameters.getBCName())) {
        //already exists
        return;
      }
    }

    final RunnerAndConfigurationSettings settings = runManager.createConfiguration("", FlashRunConfigurationType.getFactory());
    final FlashRunConfiguration runConfiguration = (FlashRunConfiguration)settings.getConfiguration();
    final FlashRunnerParameters params = runConfiguration.getRunnerParameters();
    params.setModuleName(module.getName());
    params.setBCName(bcName);

    settings.setName(params.suggestUniqueName(existingConfigurations));
    settings.setTemporary(false);
    runManager.addConfiguration(settings, false);
    runManager.setSelectedConfiguration(settings);
  }

  @Nullable
  private VirtualFile createSourceRoot(final ContentEntry contentEntry) {
    final VirtualFile contentRoot = contentEntry.getFile();
    if (contentRoot == null) return null;

    VirtualFile sourceRoot = VfsUtil.findRelativeFile(contentRoot, "src");

    if (sourceRoot == null) {
      sourceRoot = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
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
}
