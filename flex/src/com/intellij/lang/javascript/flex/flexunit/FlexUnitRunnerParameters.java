package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.LauncherParameters;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
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

  private static final Scope DEFAULT_SCOPE = Scope.Class;
  private static final OutputLogLevel DEFAULT_LEVEL = null;

  private @NotNull Scope myScope = DEFAULT_SCOPE;

  private @NotNull String myPackageName = "";
  private @NotNull String myClassName = "";
  private @NotNull String myMethodName = "";

  private @Nullable OutputLogLevel myOutputLogLevel = null;
  private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  private boolean myTrusted = true;

  private int myPort;
  private int mySocketPolicyPort;

  public FlexUnitRunnerParameters() {
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

  public void check(final Project project) throws RuntimeConfigurationError {
    doCheck(project, super.checkAndGetModuleAndBC(project));
  }

  public Pair<Module, FlexIdeBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
    doCheck(project, moduleAndBC);

    final ModifiableFlexIdeBuildConfiguration overriddenBC = Factory.getTemporaryCopyForCompilation(moduleAndBC.second);
    overriddenBC.setSkipCompile(false);
    overriddenBC.setRLMs(Collections.<FlexIdeBuildConfiguration.RLMInfo>emptyList());
    overriddenBC.setCssFilesToCompile(Collections.<String>emptyList());
    overriddenBC.setMainClass(FlexUnitPrecompileTask.FLEX_UNIT_LAUNCHER);
    overriddenBC.setOutputFileName("_flexunit.swf");
    overriddenBC
      .setOutputFolder(VfsUtilCore.urlToPath(CompilerModuleExtension.getInstance(moduleAndBC.first).getCompilerOutputUrlForTests()));
    //overriddenBC.getAndroidPackagingOptions().setPackageFileName("_flexunit.apk");
    //overriddenBC.getIosPackagingOptions().setPackageFileName("_flexunit.ipa");

    overriddenBC.setOutputType(OutputType.Application);
    overriddenBC.setUseHtmlWrapper(false);

    overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

    for (ModifiableDependencyEntry entry : overriddenBC.getDependencies().getModifiableEntries()) {
      if (entry.getDependencyType().getLinkageType() == LinkageType.External) {
        entry.getDependencyType().setLinkageType(LinkageType.Merged);
      }
    }

    overriddenBC.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);

    /*
    final ModifiableAndroidPackagingOptions androidOptions = overriddenBC.getAndroidPackagingOptions();
    androidOptions.setEnabled(true);
    androidOptions.setUseGeneratedDescriptor(true);
    androidOptions.getSigningOptions().setUseTempCertificate(true);

    overriddenBC.getIosPackagingOptions().setEnabled(false); // impossible without extra user input: app id, provisioning, etc.
    */

    return Pair.create(moduleAndBC.first, ((FlexIdeBuildConfiguration)overriddenBC));
  }

  private void doCheck(final Project project, final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC) throws RuntimeConfigurationError {
    if (DumbService.getInstance(project).isDumb()) return;

    final FlexIdeBuildConfiguration bc = moduleAndBC.second;

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      throw new RuntimeConfigurationError("FlexUnit tests are not supported on mobile devices");
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
        if (!JSUtils.packageExists(getPackageName(), searchScope)) {
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
    final PsiElement classToTest = JSResolveUtil.findClassByQName(className, searchScope);
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
