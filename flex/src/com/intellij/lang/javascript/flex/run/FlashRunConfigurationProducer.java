// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.RunManager;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class FlashRunConfigurationProducer extends LazyRunConfigurationProducer<FlashRunConfiguration> {
  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return ConfigurationTypeUtil.findConfigurationType(FlashRunConfigurationType.class);
  }

  @Override
  protected boolean setupConfigurationFromContext(final FlashRunConfiguration configuration,
                                                  final ConfigurationContext context,
                                                  final Ref<PsiElement> sourceElement) {
    final Module module = context.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return false;

    final JSClass jsClass = getJSClass(context.getPsiLocation());
    if (jsClass == null || !isAcceptedMainClass(jsClass, module)) return false;

    final FlashRunnerParameters params = configuration.getRunnerParameters();

    params.setModuleName(module.getName());

    final FlexBuildConfiguration bc = getBCToBaseOn(module, jsClass.getQualifiedName());
    params.setBCName(bc.getName());

    if (bc.getOutputType() == OutputType.Application && jsClass.getQualifiedName().equals(bc.getMainClass())) {
      params.setOverrideMainClass(false);
    }
    else {
      params.setOverrideMainClass(true);
      params.setOverriddenMainClass(jsClass.getQualifiedName());
      params.setOverriddenOutputFileName(jsClass.getName() + ".swf");
    }

    if (bc.getTargetPlatform() == TargetPlatform.Mobile &&
        bc.getOutputType() == OutputType.Application &&
        params.getMobileRunTarget() == FlashRunnerParameters.AirMobileRunTarget.Emulator) {

      if (params.getAppDescriptorForEmulator() == FlashRunnerParameters.AppDescriptorForEmulator.Android &&
          !bc.getAndroidPackagingOptions().isEnabled() &&
          bc.getIosPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.IOS);
      }

      if (params.getAppDescriptorForEmulator() == FlashRunnerParameters.AppDescriptorForEmulator.IOS &&
          bc.getAndroidPackagingOptions().isEnabled() &&
          !bc.getIosPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.Android);
      }
    }

    configuration.setGeneratedName();

    return true;
  }

  @Override
  public boolean isConfigurationFromContext(final FlashRunConfiguration configuration, final ConfigurationContext context) {
    final Module module = context.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return false;
    if (!module.getName().equals(configuration.getRunnerParameters().getModuleName())) return false;

    final JSClass jsClass = getJSClass(context.getPsiLocation());
    if (jsClass == null || !isAcceptedMainClass(jsClass, module)) return false;

    final List<RunConfiguration> existing = RunManager.getInstance(module.getProject()).getConfigurationsList(getConfigurationType());
    final RunConfiguration suitable = findSuitableRunConfig(module, jsClass.getQualifiedName(), existing);

    return suitable == configuration;
  }

  private static FlexBuildConfiguration getBCToBaseOn(final Module module, final String fqn) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);

    FlexBuildConfiguration appWithSuitableMainClass = null;

    for (FlexBuildConfiguration bc : manager.getBuildConfigurations()) {
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

  @Nullable
  private static RunConfiguration findSuitableRunConfig(final Module module,
                                                        final String fqn,
                                                        final List<RunConfiguration> existing) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);

    RunConfiguration basedOnBC = null;
    RunConfiguration basedOnMainClass = null;
    RunConfiguration basedOnMainClassAndActiveBC = null;

    for (final RunConfiguration runConfig : existing) {
      final FlashRunnerParameters params = ((FlashRunConfiguration)runConfig).getRunnerParameters();
      if (module.getName().equals(params.getModuleName())) {
        final FlexBuildConfiguration bc = manager.findConfigurationByName(params.getBCName());
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

  @Nullable
  private static JSClass getJSClass(final @Nullable PsiElement sourceElement) {
    PsiElement element = PsiTreeUtil.getNonStrictParentOfType(sourceElement, JSClass.class, JSFile.class, XmlFile.class);
    if (element instanceof JSFile) {
      element = JSPsiImplUtils.findClass((JSFile)element);
    }
    else if (element instanceof XmlFile) {
      element = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
    }
    return element instanceof JSClass ? (JSClass)element : null;
  }

  public static boolean isAcceptedMainClass(@Nullable final JSClass jsClass, @Nullable final Module module) {
    return jsClass != null && module != null && BCUtils.isValidMainClass(module, null, jsClass, true);
  }
}
