package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class FlexIdeBuildConfigurationImpl implements ModifiableFlexIdeBuildConfiguration {

  @NotNull
  private String myName = "Unnamed";

  @NotNull
  private TargetPlatform myTargetPlatform = BuildConfigurationNature.DEFAULT.targetPlatform;

  private boolean myPureAs = BuildConfigurationNature.DEFAULT.pureAS;

  @NotNull
  private OutputType myOutputType = BuildConfigurationNature.DEFAULT.outputType;

  @NotNull
  private String myOptimizeFor = "";

  @NotNull
  private String myMainClass = "";

  @NotNull
  private String myOutputFileName = "";

  @NotNull
  private String myOutputFolder = "";

  private boolean myUseHtmlWrapper = false;

  @NotNull
  private String myWrapperTemplatePath = "";

  private boolean mySkipCompile = false;

  private final DependenciesImpl myDependencies = new DependenciesImpl();
  private final CompilerOptionsImpl myCompilerOptions = new CompilerOptionsImpl();
  private final AirDesktopPackagingOptionsImpl myAirDesktopPackagingOptions = new AirDesktopPackagingOptionsImpl();
  private final AndroidPackagingOptionsImpl myAndroidPackagingOptions = new AndroidPackagingOptionsImpl();
  private final IosPackagingOptionsImpl myIosPackagingOptions = new IosPackagingOptionsImpl();

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  @NotNull
  public TargetPlatform getTargetPlatform() {
    return myTargetPlatform;
  }

  @Override
  public boolean isPureAs() {
    return myPureAs;
  }

  @Override
  @NotNull
  public OutputType getOutputType() {
    return myOutputType;
  }

  @Override
  @NotNull
  public String getOptimizeFor() {
    return myOptimizeFor;
  }

  @Override
  @NotNull
  public String getMainClass() {
    return myMainClass;
  }

  @Override
  @NotNull
  public String getOutputFileName() {
    return myOutputFileName;
  }

  @Override
  @NotNull
  public String getOutputFolder() {
    return myOutputFolder;
  }

  @Override
  public boolean isUseHtmlWrapper() {
    return myUseHtmlWrapper;
  }

  @Override
  @NotNull
  public String getWrapperTemplatePath() {
    return myWrapperTemplatePath;
  }

  @Override
  public boolean isSkipCompile() {
    return mySkipCompile;
  }

  @Override
  public void setName(@NotNull String name) {
    myName = name;
  }

  @Override
  public void setNature(BuildConfigurationNature nature) {
    myTargetPlatform = nature.targetPlatform;
    myPureAs = nature.pureAS;
    myOutputType = nature.outputType;
  }

  @Override
  public void setTargetPlatform(@NotNull TargetPlatform targetPlatform) {
    myTargetPlatform = targetPlatform;
  }

  @Override
  public void setPureAs(boolean pureAs) {
    myPureAs = pureAs;
  }

  @Override
  public void setOutputType(@NotNull OutputType outputType) {
    myOutputType = outputType;
  }

  @Override
  public void setOptimizeFor(@NotNull String optimizeFor) {
    myOptimizeFor = optimizeFor;
  }

  @Override
  public void setMainClass(@NotNull String mainClass) {
    myMainClass = mainClass;
  }

  @Override
  public void setOutputFileName(@NotNull String outputFileName) {
    myOutputFileName = outputFileName;
  }

  @Override
  public void setOutputFolder(@NotNull String outputFolder) {
    myOutputFolder = outputFolder;
  }

  @Override
  public void setUseHtmlWrapper(boolean useHtmlWrapper) {
    myUseHtmlWrapper = useHtmlWrapper;
  }

  @Override
  public void setWrapperTemplatePath(@NotNull String wrapperTemplatePath) {
    myWrapperTemplatePath = wrapperTemplatePath;
  }

  @Override
  public void setSkipCompile(boolean skipCompile) {
    mySkipCompile = skipCompile;
  }

  @NotNull
  @Override
  public ModifiableDependencies getDependencies() {
    return myDependencies;
  }

  @NotNull
  @Override
  public ModifiableCompilerOptions getCompilerOptions() {
    return myCompilerOptions;
  }

  @NotNull
  @Override
  public ModifiableAirDesktopPackagingOptions getAirDesktopPackagingOptions() {
    return myAirDesktopPackagingOptions;
  }

  @NotNull
  @Override
  public ModifiableAndroidPackagingOptions getAndroidPackagingOptions() {
    return myAndroidPackagingOptions;
  }

  @NotNull
  @Override
  public ModifiableIosPackagingOptions getIosPackagingOptions() {
    return myIosPackagingOptions;
  }

  @Override
  public Icon getIcon() {
    switch (myTargetPlatform) {
      case Web:
        return FlexFacetType.ourFlexIcon;
      case Desktop:
        return AirSdkType.airIcon;
      case Mobile:
        return AirMobileSdkType.airMobileIcon;
      default:
        assert false;
        return FlexFacetType.ourFlexIcon;
    }
  }

  @Override
  public String getOutputFilePath() {
    return myOutputFolder + (myOutputFolder.isEmpty() ? "" : "/") + myOutputFileName;
  }

  public FlexIdeBuildConfigurationImpl getCopy() {
    FlexIdeBuildConfigurationImpl copy = new FlexIdeBuildConfigurationImpl();
    applyTo(copy);
    return copy;
  }

  void applyTo(FlexIdeBuildConfigurationImpl copy) {
    myAirDesktopPackagingOptions.applyTo(copy.myAirDesktopPackagingOptions);
    myAndroidPackagingOptions.applyTo(copy.myAndroidPackagingOptions);
    myCompilerOptions.applyTo(copy.myCompilerOptions);
    myDependencies.applyTo(copy.myDependencies);
    myIosPackagingOptions.applyTo(copy.myIosPackagingOptions);
    copy.myMainClass = myMainClass;
    copy.myName = myName;
    copy.myOptimizeFor = myOptimizeFor;
    copy.myOutputFileName = myOutputFileName;
    copy.myOutputFolder = myOutputFolder;
    copy.myOutputType = myOutputType;
    copy.myPureAs = myPureAs;
    copy.mySkipCompile = mySkipCompile;
    copy.myTargetPlatform = myTargetPlatform;
    copy.myUseHtmlWrapper = myUseHtmlWrapper;
    copy.myWrapperTemplatePath = myWrapperTemplatePath;
  }

  boolean isEqual(FlexIdeBuildConfigurationImpl other) {
    if (!myAirDesktopPackagingOptions.isEqual(other.myAirDesktopPackagingOptions)) return false;
    if (!myAndroidPackagingOptions.isEqual(other.myAndroidPackagingOptions)) return false;
    if (!myCompilerOptions.isEqual(other.myCompilerOptions)) return false;
    if (!myDependencies.isEqual(other.myDependencies)) return false;
    if (!myIosPackagingOptions.isEqual(other.myIosPackagingOptions)) return false;
    if (!other.myMainClass.equals(myMainClass)) return false;
    if (!other.myName.equals(myName)) return false;
    if (!other.myOptimizeFor.equals(myOptimizeFor)) return false;
    if (!other.myOutputFileName.equals(myOutputFileName)) return false;
    if (!other.myOutputFolder.equals(myOutputFolder)) return false;
    if (other.myOutputType != myOutputType) return false;
    if (other.myPureAs != myPureAs) return false;
    if (other.mySkipCompile != mySkipCompile) return false;
    if (other.myTargetPlatform != myTargetPlatform) return false;
    if (other.myUseHtmlWrapper != myUseHtmlWrapper) return false;
    if (!other.myWrapperTemplatePath.equals(myWrapperTemplatePath)) return false;
    return true;
  }

  @Override
  public BuildConfigurationNature getNature() {
    return new BuildConfigurationNature(myTargetPlatform, myPureAs, myOutputType);
  }

  public State getState() {
    State state = new State();
    state.AIR_DESKTOP_PACKAGING_OPTIONS = myAirDesktopPackagingOptions.getState();
    state.ANDROID_PACKAGING_OPTIONS = myAndroidPackagingOptions.getState();
    state.COMPILER_OPTIONS = myCompilerOptions.getState();
    state.DEPENDENCIES = myDependencies.getState();
    state.IOS_PACKAGING_OPTIONS = myIosPackagingOptions.getState();
    state.MAIN_CLASS = myMainClass;
    state.NAME = myName;
    state.OPTIMIZE_FOR = myOptimizeFor;
    state.OUTPUT_FILE_NAME = myOutputFileName;
    state.OUTPUT_FOLDER = myOutputFolder;
    state.OUTPUT_TYPE = myOutputType;
    state.PURE_ACTION_SCRIPT = myPureAs;
    state.SKIP_COMPILE = mySkipCompile;
    state.TARGET_PLATFORM = myTargetPlatform;
    state.USE_HTML_WRAPPER = myUseHtmlWrapper;
    state.WRAPPER_TEMPLATE_PATH = myWrapperTemplatePath;
    return state;
  }

  public void loadState(State state, Project project) {
    myAirDesktopPackagingOptions.loadState(state.AIR_DESKTOP_PACKAGING_OPTIONS);
    myAndroidPackagingOptions.loadState(state.ANDROID_PACKAGING_OPTIONS);
    myCompilerOptions.loadState(state.COMPILER_OPTIONS);
    myDependencies.loadState(state.DEPENDENCIES, project);
    myIosPackagingOptions.loadState(state.IOS_PACKAGING_OPTIONS);
    myMainClass = state.MAIN_CLASS;
    myName = state.NAME;
    myOptimizeFor = state.OPTIMIZE_FOR;
    myOutputFileName = state.OUTPUT_FILE_NAME;
    myOutputFolder = state.OUTPUT_FOLDER;
    myOutputType = state.OUTPUT_TYPE;
    myPureAs = state.PURE_ACTION_SCRIPT;
    mySkipCompile = state.SKIP_COMPILE;
    myTargetPlatform = state.TARGET_PLATFORM;
    myUseHtmlWrapper = state.USE_HTML_WRAPPER;
    myWrapperTemplatePath = state.WRAPPER_TEMPLATE_PATH;
  }

  @Tag("configuration")
  public static class State {
    @Attribute(value = "name")
    public String NAME = "Unnamed";

    @Attribute(value = "target-platform")
    public TargetPlatform TARGET_PLATFORM = TargetPlatform.Web;

    @Attribute(value = "pure-as")
    public boolean PURE_ACTION_SCRIPT = false;

    @Attribute(value = "output-type")
    public OutputType OUTPUT_TYPE = OutputType.Application;

    @Attribute(value = "optimize-for")
    public String OPTIMIZE_FOR = "";

    @Attribute(value = "main-class")
    public String MAIN_CLASS = "";

    @Attribute(value = "output-file")
    public String OUTPUT_FILE_NAME = "";

    @Attribute(value = "output-folder")
    public String OUTPUT_FOLDER = "";

    @Attribute(value = "use-html-wrapper")
    public boolean USE_HTML_WRAPPER = false;

    @Attribute(value = "wrapper-template-path")
    public String WRAPPER_TEMPLATE_PATH = "";

    @Attribute(value = "skip-build")
    public boolean SKIP_COMPILE = false;

    @Property(surroundWithTag = false)
    public DependenciesImpl.State DEPENDENCIES;

    @Property(surroundWithTag = false)
    public CompilerOptionsImpl.State COMPILER_OPTIONS;

    @Property(surroundWithTag = false)
    public AirDesktopPackagingOptionsImpl.State AIR_DESKTOP_PACKAGING_OPTIONS;

    @Property(surroundWithTag = false)
    public AndroidPackagingOptionsImpl.State ANDROID_PACKAGING_OPTIONS;

    @Property(surroundWithTag = false)
    public IosPackagingOptionsImpl.State IOS_PACKAGING_OPTIONS;
  }
}
