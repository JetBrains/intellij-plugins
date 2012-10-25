package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlashRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

  private static final String WINDOWED_APPLICATION_CLASS_NAME_1 = "mx.core.WindowedApplication";
  private static final String WINDOWED_APPLICATION_CLASS_NAME_2 = "spark.components.WindowedApplication";

  private PsiElement mySourceElement;

  public FlashRunConfigurationProducer() {
    super(FlashRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
    if (!(location instanceof PsiLocation)) return null;
    final Module module = context.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return null;

    mySourceElement = location.getPsiElement();

    final JSClass jsClass = getJSClass(mySourceElement);
    if (jsClass != null && isAcceptedMainClass(jsClass, module)) {
      final RunnerAndConfigurationSettings settings =
        RunManagerEx.getInstanceEx(location.getProject()).createConfiguration("", FlashRunConfigurationType.getFactory());
      final FlashRunConfiguration runConfig = (FlashRunConfiguration)settings.getConfiguration();
      final FlashRunnerParameters params = runConfig.getRunnerParameters();

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

      runConfig.setName(params.suggestUniqueName(context.getRunManager().getConfigurations(FlashRunConfigurationType.getInstance())));
      return settings;
    }

    return null;
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

  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 final @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 final ConfigurationContext context) {
    if (existingConfigurations.length == 0) return null;
    if (!(location instanceof PsiLocation)) return null;
    final Module module = location.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return null;

    final PsiElement psiElement = location.getPsiElement();

    final JSClass jsClass = getJSClass(psiElement);

    if (jsClass != null && isAcceptedMainClass(jsClass, module)) {
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
      final FlashRunnerParameters params = ((FlashRunConfiguration)runConfig.getConfiguration()).getRunnerParameters();
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

  public int compareTo(Object o) {
    return PREFERED;
  }

  @Nullable
  private static JSClass getJSClass(final PsiElement sourceElement) {
    PsiElement element = PsiTreeUtil.getNonStrictParentOfType(sourceElement, JSClass.class, JSFile.class, XmlFile.class);
    if (element instanceof JSFile) {
      element = JSPsiImplUtils.findClass((JSFile)element);
    }
    else if (element instanceof XmlFile) {
      element = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
    }
    return element instanceof JSClass ? (JSClass)element : null;
  }

  public static boolean isAcceptedMainClass(@Nullable final JSClass jsClass, @Nullable final Module module) {
    return jsClass != null && module != null && BCUtils.isValidMainClass(module, null, jsClass, true);
  }
}
