package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewFlexUnitRunnerParameters extends BCBasedRunnerParameters implements FlexUnitCommonParameters {

  private @NotNull String myPackageName = "";
  private @NotNull String myClassName = "";
  private @NotNull String myMethodName = "";

  private static final Scope DEFAULT_SCOPE = Scope.Class;

  private @NotNull Scope myScope = DEFAULT_SCOPE;

  private int myPort;

  private int mySocketPolicyPort;

  private static final OutputLogLevel DEFAULT_LEVEL = null;

  private @Nullable OutputLogLevel myOutputLogLevel = null;
  private String myLauncherFileName;

  public NewFlexUnitRunnerParameters() {
  }

  @Attribute("scope")
  public String getScopeRaw() {
    return myScope.name();
  }

  public void setScopeRaw(String scopeRaw) {
    try {
      myScope = Scope.valueOf(scopeRaw);
    }
    catch (IllegalArgumentException e) {
      myScope = DEFAULT_SCOPE;
    }
  }

  @NotNull
  @Transient
  public Scope getScope() {
    return myScope;
  }

  public void setScope(@NotNull Scope scope) {
    myScope = scope;
  }

  @NotNull
  @Attribute("package_name")
  public String getPackageName() {
    return myPackageName;
  }

  public void setPackageName(@NotNull String packageName) {
    myPackageName = packageName;
  }

  @NotNull
  @Attribute("class_name")
  public String getClassName() {
    return myClassName;
  }

  public void setClassName(@NotNull String className) {
    myClassName = className;
  }

  @NotNull
  @Attribute("method_name")
  public String getMethodName() {
    return myMethodName;
  }

  public void setMethodName(@NotNull String methodName) {
    myMethodName = methodName;
  }

  @Transient
  public int getPort() {
    return myPort;
  }

  public void setPort(int port) {
    myPort = port;
  }

  @Transient
  public int getSocketPolicyPort() {
    return mySocketPolicyPort;
  }

  public void setSocketPolicyPort(int port) {
    mySocketPolicyPort = port;
  }

  @Attribute("output_log_level")
  public String getOutputLogLevelRaw() {
    return myOutputLogLevel != null ? myOutputLogLevel.name() : "";
  }

  public void setOutputLogLevelRaw(String outputLogLevel) {
    if (StringUtil.isNotEmpty(outputLogLevel)) {
      try {
        myOutputLogLevel = OutputLogLevel.valueOf(outputLogLevel);
        return;
      }
      catch (IllegalArgumentException e) {
        // ignore
      }
    }
    myOutputLogLevel = DEFAULT_LEVEL;
  }

  @Nullable
  @Transient
  public OutputLogLevel getOutputLogLevel() {
    return myOutputLogLevel;
  }

  public void setOutputLogLevel(@Nullable OutputLogLevel outputLogLevel) {
    myOutputLogLevel = outputLogLevel;
  }

  @Override
  public NewFlexUnitRunnerParameters clone() {
    return (NewFlexUnitRunnerParameters)super.clone();
  }

  public void check(final Project project) throws RuntimeConfigurationError {
    doCheck(project, super.checkAndGetModuleAndBC(project));
  }

  public Pair<Module, FlexIdeBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
    doCheck(project, moduleAndBC);

    final ModifiableFlexIdeBuildConfiguration overriddenBC = Factory.getCopy(moduleAndBC.second);
    overriddenBC.setSkipCompile(false);
    overriddenBC.setMainClass(FlexUnitPrecompileTask.getFlexUnitLauncherName(getModuleName()));
    overriddenBC.setOutputFileName("_flexunit.swf");
    overriddenBC.getAndroidPackagingOptions().setPackageFileName("_flexunit.apk");
    overriddenBC.getIosPackagingOptions().setPackageFileName("_flexunit.ipa");

    //if (overriddenBC.getOutputType() != OutputType.Application) {
    overriddenBC.setOutputType(OutputType.Application);
    overriddenBC.setUseHtmlWrapper(false);

    overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

    for (ModifiableDependencyEntry entry : overriddenBC.getDependencies().getModifiableEntries()) {
      if (entry.getDependencyType().getLinkageType() == LinkageType.External) {
        entry.getDependencyType().setLinkageType(LinkageType.Merged);
      }
    }

    /*
    overriddenBC.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);

    final ModifiableAndroidPackagingOptions androidOptions = overriddenBC.getAndroidPackagingOptions();
    androidOptions.setEnabled(true);
    androidOptions.setUseGeneratedDescriptor(true);
    androidOptions.getSigningOptions().setUseTempCertificate(true);

    overriddenBC.getIosPackagingOptions().setEnabled(false); // impossible without extra user input: app id, provisioning, etc.
    */
    //}

    return Pair.create(moduleAndBC.first, ((FlexIdeBuildConfiguration)overriddenBC));
  }

  private void doCheck(final Project project, final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC) throws RuntimeConfigurationError {
    if (DumbService.getInstance(project).isDumb()) return;

    final FlexIdeBuildConfiguration bc = moduleAndBC.second;

    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      throw new RuntimeConfigurationError("FlexUnit tests for Desktop target platform are not supported yet");
    }
    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      throw new RuntimeConfigurationError("FlexUnit tests are not supported on mobile devices");
    }

    final FlexUnitSupport support = FlexUnitSupport.getSupport(bc, moduleAndBC.first);
    if (support == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("flexunit.not.found.for.bc", bc.getName()));
    }

    // todo check further
    /*final GlobalSearchScope scope = ;
    switch (params.getScope()) {
      case Class:
        getClassToTest(params.getClassName(), scope, support, true);
        break;

      case Method:
        final JSClass classToTest = getClassToTest(params.getClassName(), scope, support, false);
        if (StringUtil.isEmpty(params.getMethodName())) {
          throw new RuntimeConfigurationError(FlexBundle.message("no.test.method.specified"));
        }

        final JSFunction methodToTest = classToTest.findFunctionByNameAndKind(params.getMethodName(), JSFunction.FunctionKind.SIMPLE);

        if (methodToTest == null || !support.isTestMethod(methodToTest)) {
          throw new RuntimeConfigurationError(FlexBundle.message("method.not.valid", params.getMethodName()));
        }
        break;

      case Package:
        if (!JSUtils.packageExists(params.getPackageName(), scope)) {
          throw new RuntimeConfigurationError(FlexBundle.message("package.not.valid", params.getPackageName()));
        }
        break;

      default:
        assert false : "Unknown scope: " + params.getScope();
    }*/
  }
}
