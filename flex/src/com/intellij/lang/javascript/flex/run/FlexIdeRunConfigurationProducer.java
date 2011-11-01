package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlexIdeRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

  private PsiElement mySourceElement;

  public FlexIdeRunConfigurationProducer() {
    super(FlexIdeRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
    final Module module = context.getModule();
    if (!(location instanceof PsiLocation) || module == null) return null;
    mySourceElement = location.getPsiElement();

    final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(mySourceElement);
    if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, true)) {
      final RunnerAndConfigurationSettings settings =
        RunManagerEx.getInstanceEx(location.getProject()).createConfiguration("", FlexIdeRunConfigurationType.getFactory());
      final FlexIdeRunConfiguration runConfig = (FlexIdeRunConfiguration)settings.getConfiguration();
      final FlexIdeRunnerParameters params = runConfig.getRunnerParameters();

      params.setModuleName(module.getName());

      final FlexIdeBuildConfiguration bc = getBCToBaseOn(module, jsClass.getQualifiedName());
      params.setBCName(bc.getName());

      if (bc.getOutputType() == OutputType.Application && jsClass.getQualifiedName().equals(bc.getMainClass())) {
        params.setOverrideMainClass(false);
      }
      else {
        params.setOverrideMainClass(true);
        params.setOverriddenMainClass(jsClass.getQualifiedName());
        params.setOverriddenOutputFileName(jsClass.getName() + ".swf");
      }

      runConfig.setName(params.suggestUniqueName(context.getRunManager().getConfigurations(FlexIdeRunConfigurationType.getInstance())));
      return settings;
    }

    return null;
  }

  private static FlexIdeBuildConfiguration getBCToBaseOn(final Module module, final String fqn) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);

    FlexIdeBuildConfiguration appWithSuitableMainClass = null;

    for (FlexIdeBuildConfiguration bc : manager.getBuildConfigurations()) {
      if (bc.getOutputType() == OutputType.Application && fqn.equals(bc.getMainClass())) {
        if (manager.getActiveConfiguration() == bc) {
          return bc; // the best choice
        }
        appWithSuitableMainClass = bc;
      }
    }

    // 2nd good choice - any app with matching main class (though not active at this moment)
    // if no apps with matching main class - take active bc and override main class
    return appWithSuitableMainClass != null ? appWithSuitableMainClass : manager.getActiveConfiguration();
  }

  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 final @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 final ConfigurationContext context) {
    if (existingConfigurations.length == 0) return null;
    final Module module = context.getModule();
    if (!(location instanceof PsiLocation) || module == null) return null;
    final PsiElement psiElement = location.getPsiElement();

    final JSClass jsClass = FlexRuntimeConfigurationProducer.getJSClass(psiElement);

    if (jsClass != null && FlexRuntimeConfigurationProducer.isAcceptedMainClass(jsClass, module, true)) {
      return findSuitableRunConfig(module, jsClass.getQualifiedName(), existingConfigurations);
    }

    return null;
  }

  @Nullable
  private static RunnerAndConfigurationSettings findSuitableRunConfig(final Module module,
                                                                      final String fqn,
                                                                      final RunnerAndConfigurationSettings[] existing) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);

    RunnerAndConfigurationSettings basedOnBC = null;
    RunnerAndConfigurationSettings basedOnMainClass = null;
    RunnerAndConfigurationSettings basedOnMainClassAndActiveBC = null;

    for (final RunnerAndConfigurationSettings runConfig : existing) {
      final FlexIdeRunnerParameters params = ((FlexIdeRunConfiguration)runConfig.getConfiguration()).getRunnerParameters();
      if (module.getName().equals(params.getModuleName())) {
        final FlexIdeBuildConfiguration bc = manager.findConfigurationByName(params.getBCName());
        if (bc == null) continue;

        if (params.isOverrideMainClass()) {
          if (fqn.equals(params.getOverriddenMainClass())) {
            if (manager.getActiveConfiguration() == bc) {
              basedOnMainClassAndActiveBC = runConfig;
            }
            basedOnMainClass = runConfig;
          }
        }
        else {
          if (bc.getOutputType() == OutputType.Application && fqn.equals(bc.getMainClass())) {
            if (manager.getActiveConfiguration() == bc) {
              return runConfig; // the best choice
            }
            basedOnBC = runConfig;
          }
        }
      }
    }

    // second good choice - based on app bc with matching main class (though not active at this moment)
    // third - based on overridden main class and active bc
    // forth - based on overridden main class and any bc
    return basedOnBC != null ? basedOnBC : basedOnMainClassAndActiveBC != null ? basedOnMainClassAndActiveBC : basedOnMainClass;
  }

  public int compareTo(Object o) {
    return PREFERED;
  }
}
