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

public class AirRuntimeConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

  private PsiElement mySourceElement;

  public AirRuntimeConfigurationProducer() {
    super(AirRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
    if (!(location instanceof PsiLocation)) return null;

    mySourceElement = location.getPsiElement();
    final Module module = context.getModule();
    if (module == null || !FlexSdkUtils.hasDependencyOnAir(module)) {
      return null;
    }

    final PsiFile psiFile = mySourceElement.getContainingFile();
    final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();
    if (virtualFile != null && FlexUtils.isAirDescriptorFile(virtualFile)) {
      final FlexBuildConfiguration config = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
      final RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(location.getProject())
        .createConfiguration(virtualFile.getNameWithoutExtension(), AirRunConfigurationType.getFactory());
      final AirRunConfiguration runConfiguration = (AirRunConfiguration)settings.getConfiguration();
      final AirRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

      runnerParameters.setModuleName(module.getName());
      runnerParameters.setAirRunMode(AirRunnerParameters.AirRunMode.AppDescriptor);
      runnerParameters.setAirDescriptorPath(virtualFile.getPath());
      runnerParameters.setAirRootDirPath(config.getCompileOutputPath());
      return settings;
    }
    else {
      final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(mySourceElement);
      if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, true)) {
        final RunnerAndConfigurationSettings settings =
          RunManagerEx.getInstanceEx(location.getProject()).createConfiguration(jsClass.getName(), AirRunConfigurationType.getFactory());
        final AirRunConfiguration runConfiguration = (AirRunConfiguration)settings.getConfiguration();
        final AirRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();

        runnerParameters.setModuleName(module.getName());
        runnerParameters.setAirRunMode(AirRunnerParameters.AirRunMode.MainClass);
        runnerParameters.setMainClassName(jsClass.getQualifiedName());
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
    if (module == null || !FlexSdkUtils.hasDependencyOnAir(module)) {
      return null;
    }

    final PsiFile psiFile = psiElement.getContainingFile();
    final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();
    if (virtualFile != null && FlexUtils.isAirDescriptorFile(virtualFile)) {
      final FlexBuildConfiguration config = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
      for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
        final AirRunnerParameters runnerParameters = ((AirRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
        if (module.getName().equals(runnerParameters.getModuleName()) &&
            AirRunnerParameters.AirRunMode.AppDescriptor == runnerParameters.getAirRunMode() &&
            virtualFile.getPath().equals(runnerParameters.getAirDescriptorPath()) &&
            config.getCompileOutputPath().equals(runnerParameters.getAirRootDirPath())) {
          return existingConfiguration;
        }
      }
    }
    else {
      final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(psiElement);
      if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, true)) {
        for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
          final AirRunnerParameters runnerParameters =
            ((AirRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
          if (module.getName().equals(runnerParameters.getModuleName()) &&
              AirRunnerParameters.AirRunMode.MainClass == runnerParameters.getAirRunMode() &&
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