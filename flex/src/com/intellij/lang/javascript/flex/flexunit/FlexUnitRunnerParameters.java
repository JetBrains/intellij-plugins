package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableDependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.run.LauncherParameters;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AppDescriptorForEmulator;

public class FlexUnitRunnerParameters extends BCBasedRunnerParameters {

  public enum Scope {
    Method, Class, Package
  }

  public enum OutputLogLevel {
    Fatal("1000"), Error("8"), Warn("6"), Info("4"), Debug("2"), All("0");

    private final String myFlexConstant;

    OutputLogLevel(String flexConstant) {
      myFlexConstant = flexConstant;
    }

    public String getFlexConstant() {
      return myFlexConstant;
    }
  }

  private @NotNull Scope myScope = Scope.Class;

  private @NotNull String myPackageName = "";
  private @NotNull String myClassName = "";
  private @NotNull String myMethodName = "";

  private @Nullable OutputLogLevel myOutputLogLevel = null;
  private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  private boolean myTrusted = true;

  private String myEmulatorAdlOptions = "";
  private @NotNull AppDescriptorForEmulator myAppDescriptorForEmulator = AppDescriptorForEmulator.Android;

  private int myPort;
  private int mySocketPolicyPort;

  public FlexUnitRunnerParameters() {
  }

  @NotNull
  @Attribute("scope")
  public Scope getScope() {
    return myScope;
  }

  public void setScope(@NotNull Scope scope) {
    myScope = scope;
  }

  @NotNull
  @Attribute("package_name")
  public @NlsSafe String getPackageName() {
    return myPackageName;
  }

  public void setPackageName(@NotNull @NlsSafe String packageName) {
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
  public @NlsSafe String getMethodName() {
    return myMethodName;
  }

  public void setMethodName(@NotNull @NlsSafe String methodName) {
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

  @Nullable
  @Attribute("output_log_level")
  public OutputLogLevel getOutputLogLevel() {
    return myOutputLogLevel;
  }

  public void setOutputLogLevel(@Nullable OutputLogLevel outputLogLevel) {
    myOutputLogLevel = outputLogLevel;
  }

  @NotNull
  public LauncherParameters getLauncherParameters() {
    return myLauncherParameters;
  }

  public void setLauncherParameters(@NotNull final LauncherParameters launcherParameters) {
    myLauncherParameters = launcherParameters;
  }

  public boolean isTrusted() {
    return myTrusted;
  }

  public void setTrusted(final boolean trusted) {
    myTrusted = trusted;
  }

  @NotNull
  @Attribute("emulatorAdlOptions")
  public String getEmulatorAdlOptions() {
    return myEmulatorAdlOptions;
  }

  public void setEmulatorAdlOptions(final String emulatorAdlOptions) {
    myEmulatorAdlOptions = emulatorAdlOptions;
  }

  @NotNull
  @Attribute("appDescriptorForEmulator")
  public AppDescriptorForEmulator getAppDescriptorForEmulator() {
    return myAppDescriptorForEmulator;
  }

  public void setAppDescriptorForEmulator(@NotNull final AppDescriptorForEmulator appDescriptorForEmulator) {
    myAppDescriptorForEmulator = appDescriptorForEmulator;
  }

  public void check(final Project project) throws RuntimeConfigurationError {
    doCheck(project, super.checkAndGetModuleAndBC(project));
  }

  @Override
  public Pair<Module, FlexBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    final Pair<Module, FlexBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
    doCheck(project, moduleAndBC);

    final ModifiableFlexBuildConfiguration overriddenBC = Factory.getTemporaryCopyForCompilation(moduleAndBC.second);
    overriddenBC.setOutputType(OutputType.Application);

    overriddenBC.setMainClass(FlexCommonUtils.FLEX_UNIT_LAUNCHER);
    overriddenBC.setOutputFileName("_flexunit.swf");
    overriddenBC
      .setOutputFolder(VfsUtilCore.urlToPath(CompilerModuleExtension.getInstance(moduleAndBC.first).getCompilerOutputUrlForTests()));

    overriddenBC.setUseHtmlWrapper(false);

    overriddenBC.setRLMs(Collections.emptyList());
    overriddenBC.setCssFilesToCompile(Collections.emptyList());
    overriddenBC.setSkipCompile(false);

    overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

    for (ModifiableDependencyEntry entry : overriddenBC.getDependencies().getModifiableEntries()) {
      if (entry.getDependencyType().getLinkageType() == LinkageType.External) {
        entry.getDependencyType().setLinkageType(LinkageType.Merged);
      }
    }

    //overriddenBC.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);

    /*
    overriddenBC.getAndroidPackagingOptions().setPackageFileName("_flexunit.apk");

    final ModifiableAndroidPackagingOptions androidOptions = overriddenBC.getAndroidPackagingOptions();
    androidOptions.setEnabled(true);
    androidOptions.setUseGeneratedDescriptor(true);
    androidOptions.getSigningOptions().setUseTempCertificate(true);

    overriddenBC.getIosPackagingOptions().setEnabled(false);
    overriddenBC.getIosPackagingOptions().setPackageFileName("_flexunit.ipa");
    */

    return Pair.create(moduleAndBC.first, overriddenBC);
  }

  private void doCheck(final Project project, final Pair<Module, FlexBuildConfiguration> moduleAndBC) throws RuntimeConfigurationError {
    if (DumbService.getInstance(project).isDumb()) return;

    final FlexBuildConfiguration bc = moduleAndBC.second;
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new RuntimeConfigurationError(
        FlexCommonBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), moduleAndBC.first.getName()));
    }

    switch (bc.getTargetPlatform()) {
      case Web:
        break;

      case Desktop:
        FlashRunnerParameters.checkAdlAndAirRuntime(sdk);
        if (bc.getOutputType() == OutputType.Application) {
          FlashRunnerParameters.checkCustomDescriptor(bc.getAirDesktopPackagingOptions(), getBCName(), getModuleName());
        }
        break;

      case Mobile:
        FlashRunnerParameters.checkAdlAndAirRuntime(sdk);

        switch (myAppDescriptorForEmulator) {
          case Android:
            if (bc.getOutputType() == OutputType.Application) {
              FlashRunnerParameters.checkCustomDescriptor(bc.getAndroidPackagingOptions(), getBCName(), getModuleName());
            }
            break;
          case IOS:
            if (bc.getOutputType() == OutputType.Application) {
              FlashRunnerParameters.checkCustomDescriptor(bc.getIosPackagingOptions(), getBCName(), getModuleName());
            }
            break;
        }
        break;
    }

    final FlexUnitSupport support = FlexUnitSupport.getSupport(bc, moduleAndBC.first);
    if (support == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("flexunit.not.found.for.bc", bc.getName()));
    }

    if (!support.flexUnit4Present && bc.isPureAs()) {
      throw new RuntimeConfigurationError(FlexBundle.message("cant.execute.flexunit1.for.pure.as.bc"));
    }

    final GlobalSearchScope searchScope = FlexUtils.getModuleWithDependenciesAndLibrariesScope(moduleAndBC.first, bc, true);
    switch (getScope()) {
      case Class:
        getClassToTest(getClassName(), searchScope, support, true);
        break;

      case Method:
        final JSClass classToTest = getClassToTest(getClassName(), searchScope, support, false);
        if (StringUtil.isEmpty(getMethodName())) {
          throw new RuntimeConfigurationError(FlexBundle.message("no.test.method.specified"));
        }

        final JSFunction methodToTest = classToTest.findFunctionByNameAndKind(getMethodName(), JSFunction.FunctionKind.SIMPLE);

        if (methodToTest == null || !support.isTestMethod(methodToTest)) {
          throw new RuntimeConfigurationError(FlexBundle.message("method.not.valid", getMethodName()));
        }
        break;

      case Package:
        if (!FlexUtils.packageExists(getPackageName(), searchScope)) {
          throw new RuntimeConfigurationError(FlexBundle.message("package.not.valid", getPackageName()));
        }
        break;

      default:
        assert false : "Unknown scope: " + getScope();
    }
  }

  private static JSClass getClassToTest(String className,
                                        GlobalSearchScope searchScope,
                                        @NotNull FlexUnitSupport flexUnitSupport,
                                        boolean allowSuite) throws RuntimeConfigurationError {
    if (StringUtil.isEmpty(className)) {
      throw new RuntimeConfigurationError(FlexBundle.message("test.class.not.specified"));
    }
    final PsiElement classToTest = ActionScriptClassResolver.findClassByQNameStatic(className, searchScope);
    if (!(classToTest instanceof JSClass)) {
      throw new RuntimeConfigurationError(FlexBundle.message("class.not.found", className));
    }

    if (!flexUnitSupport.isTestClass((JSClass)classToTest, allowSuite)) {
      throw new RuntimeConfigurationError(FlexBundle.message("class.contains.no.tests", className));
    }
    return (JSClass)classToTest;
  }

  @Override
  public FlexUnitRunnerParameters clone() {
    final FlexUnitRunnerParameters clone = (FlexUnitRunnerParameters)super.clone();
    clone.myLauncherParameters = myLauncherParameters.clone();
    return clone;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final FlexUnitRunnerParameters that = (FlexUnitRunnerParameters)o;

    if (myTrusted != that.myTrusted) return false;
    if (!myClassName.equals(that.myClassName)) return false;
    if (!myLauncherParameters.equals(that.myLauncherParameters)) return false;
    if (!myMethodName.equals(that.myMethodName)) return false;
    if (myOutputLogLevel != that.myOutputLogLevel) return false;
    if (!myPackageName.equals(that.myPackageName)) return false;
    if (myScope != that.myScope) return false;

    return true;
  }
}
