package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileRunMode;

public class AirMobileRuntimeConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

  private PsiElement mySourceElement;

  public AirMobileRuntimeConfigurationProducer() {
    super(AirMobileRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
    if (!(location instanceof PsiLocation)) return null;

    mySourceElement = location.getPsiElement();
    final Module module = context.getModule();
    if (module == null || !FlexSdkUtils.hasDependencyOnAirMobile(module)) {
      return null;
    }

    final PsiFile psiFile = mySourceElement.getContainingFile();
    final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();
    if (virtualFile != null && FlexUtils.isAirDescriptorFile(virtualFile) && FlexUtils.isAirMobileDescriptorFile(virtualFile)) {
      final FlexBuildConfiguration config = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
      final RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(location.getProject())
        .createConfiguration("", AirMobileRunConfigurationType.getFactory());
      final AirMobileRunConfiguration runConfiguration = (AirMobileRunConfiguration)settings.getConfiguration();
      final AirMobileRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

      runnerParameters.setModuleName(module.getName());
      runnerParameters.setAirMobileRunMode(AirMobileRunMode.AppDescriptor);
      runnerParameters.setAirDescriptorPath(virtualFile.getPath());
      runnerParameters.setAirRootDirPath(config.getCompileOutputPath());
      settings.setName(runConfiguration.suggestedName());
      return settings;
    }
    else {
      final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(mySourceElement);
      if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, false)) {
        final RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(location.getProject())
          .createConfiguration("", AirMobileRunConfigurationType.getFactory());
        final AirMobileRunConfiguration runConfiguration = (AirMobileRunConfiguration)settings.getConfiguration();
        final AirMobileRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

        runnerParameters.setModuleName(module.getName());
        runnerParameters.setAirMobileRunMode(AirMobileRunMode.MainClass);
        runnerParameters.setMainClassName(jsClass.getQualifiedName());
        settings.setName(runConfiguration.suggestedName());
        return settings;
      }
    }

    return null;
  }

  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 final @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 final ConfigurationContext context) {
    if (existingConfigurations.length == 0) return null;
    if (!(location instanceof PsiLocation)) return null;
    final PsiElement psiElement = location.getPsiElement();
    final Module module = ModuleUtil.findModuleForPsiElement(psiElement);
    if (module == null || !FlexSdkUtils.hasDependencyOnAirMobile(module)) {
      return null;
    }

    final PsiFile psiFile = psiElement.getContainingFile();
    final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();
    if (virtualFile != null && FlexUtils.isAirDescriptorFile(virtualFile) && FlexUtils.isAirMobileDescriptorFile(virtualFile)) {
      final FlexBuildConfiguration config = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
      for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
        final AirMobileRunnerParameters runnerParameters =
          ((AirMobileRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
        if (module.getName().equals(runnerParameters.getModuleName()) &&
            AirMobileRunMode.AppDescriptor == runnerParameters.getAirMobileRunMode() &&
            virtualFile.getPath().equals(runnerParameters.getAirDescriptorPath()) &&
            config.getCompileOutputPath().equals(runnerParameters.getAirRootDirPath())) {
          return existingConfiguration;
        }
      }
    }
    else {
      final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(psiElement);
      if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, false)) {
        for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
          final AirMobileRunnerParameters runnerParameters =
            ((AirMobileRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
          if (module.getName().equals(runnerParameters.getModuleName()) &&
              AirMobileRunMode.MainClass == runnerParameters.getAirMobileRunMode() &&
              runnerParameters.getMainClassName().equals(jsClass.getQualifiedName())) {
            return existingConfiguration;
          }
        }
      }
    }

    return null;
  }

  public int compareTo(Object o) {
    return PREFERED;
  }
}