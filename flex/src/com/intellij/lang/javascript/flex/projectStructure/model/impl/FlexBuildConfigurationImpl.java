package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil;
import com.intellij.lang.javascript.flex.build.InfoFromConfigFile;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

class FlexBuildConfigurationImpl implements ModifiableFlexBuildConfiguration {

  @NotNull
  private String myName = UNNAMED;

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

  @NotNull
  private String myRLMs = "";

  @NotNull
  private String myCssFilesToCompile = "";

  private boolean mySkipCompile = false;

  private final DependenciesImpl myDependencies = new DependenciesImpl();
  private final CompilerOptionsImpl myCompilerOptions = new CompilerOptionsImpl();
  private final AirDesktopPackagingOptionsImpl myAirDesktopPackagingOptions = new AirDesktopPackagingOptionsImpl();
  private final AndroidPackagingOptionsImpl myAndroidPackagingOptions = new AndroidPackagingOptionsImpl();
  private final IosPackagingOptionsImpl myIosPackagingOptions = new IosPackagingOptionsImpl();
  private boolean myTempBCForCompilation = false;

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
  @NotNull
  public Collection<RLMInfo> getRLMs() {
    if (myRLMs.isEmpty()) return Collections.emptyList();

    final List<String> entries = StringUtil.split(myRLMs, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
    final ArrayList<RLMInfo> result = new ArrayList<>(entries.size());
    for (String entry : entries) {
      final List<String> parts = StringUtil.split(entry, CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false);
      assert parts.size() == 3 : entry;
      result.add(new FlexBuildConfiguration.RLMInfo(parts.get(0), parts.get(1), Boolean.parseBoolean(parts.get(2))));
    }
    return result;
  }

  @Override
  @NotNull
  public Collection<String> getCssFilesToCompile() {
    if (myCssFilesToCompile.isEmpty()) return Collections.emptyList();
    return StringUtil.split(myCssFilesToCompile, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
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
    myOutputFolder = StringUtil.trimEnd(outputFolder, "/");
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
  public void setRLMs(@NotNull Collection<RLMInfo> rlms) {
    if (rlms.isEmpty()) myRLMs = "";
    myRLMs = StringUtil.join(rlms, info -> info.MAIN_CLASS +
                                       CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR +
                                       info.OUTPUT_FILE +
                                       CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR +
                                       info.OPTIMIZE, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setCssFilesToCompile(@NotNull Collection<String> cssFilesToCompile) {
    myCssFilesToCompile = cssFilesToCompile.isEmpty() ? "" : StringUtil.join(cssFilesToCompile, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
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
  public CompilerOptionsImpl getCompilerOptions() {
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

  @NotNull
  @Override
  public Icon getIcon() {
    return getNature().getIcon();
  }

  @Override
  public String getShortText() {
    return myName;
  }

  @Override
  public String getDescription() {
    return myOutputType.getShortText();
  }

  @Override
  public String getActualOutputFilePath() {
    final InfoFromConfigFile info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(myCompilerOptions.getAdditionalConfigFilePath());
    final String outputFolderPath = BCUtils.isFlexUnitBC(this) ? myOutputFolder
                                                               : StringUtil.notNullize(info.getOutputFolderPath(), myOutputFolder);
    final String outputFileName = myTempBCForCompilation ? myOutputFileName
                                                         : StringUtil.notNullize(info.getOutputFileName(), myOutputFileName);
    return outputFolderPath + (outputFolderPath.isEmpty() ? "" : "/") + outputFileName;
  }

  public FlexBuildConfigurationImpl getCopy() {
    FlexBuildConfigurationImpl copy = new FlexBuildConfigurationImpl();
    applyTo(copy);
    return copy;
  }

  void applyTo(FlexBuildConfigurationImpl copy) {
    myAirDesktopPackagingOptions.applyTo(copy.myAirDesktopPackagingOptions);
    myAndroidPackagingOptions.applyTo(copy.myAndroidPackagingOptions);
    myCompilerOptions.applyTo(copy.myCompilerOptions);
    myDependencies.applyTo(copy.myDependencies);
    myIosPackagingOptions.applyTo(copy.myIosPackagingOptions);
    copy.myCssFilesToCompile = myCssFilesToCompile;
    copy.myMainClass = myMainClass;
    copy.myName = myName;
    copy.myOptimizeFor = myOptimizeFor;
    copy.myOutputFileName = myOutputFileName;
    copy.myOutputFolder = myOutputFolder;
    copy.myOutputType = myOutputType;
    copy.myPureAs = myPureAs;
    copy.myRLMs = myRLMs;
    copy.mySkipCompile = mySkipCompile;
    copy.myTargetPlatform = myTargetPlatform;
    copy.myUseHtmlWrapper = myUseHtmlWrapper;
    copy.myWrapperTemplatePath = myWrapperTemplatePath;
  }

  @Override
  public boolean isEqual(FlexBuildConfiguration bc) {
    final FlexBuildConfigurationImpl other = (FlexBuildConfigurationImpl)bc;
    if (!myAirDesktopPackagingOptions.isEqual(other.myAirDesktopPackagingOptions)) return false;
    if (!myAndroidPackagingOptions.isEqual(other.myAndroidPackagingOptions)) return false;
    if (!myCompilerOptions.isEqual(other.myCompilerOptions)) return false;
    if (!myDependencies.isEqual(other.myDependencies)) return false;
    if (!myIosPackagingOptions.isEqual(other.myIosPackagingOptions)) return false;
    if (!other.myCssFilesToCompile.equals(myCssFilesToCompile)) return false;
    if (!other.myMainClass.equals(myMainClass)) return false;
    if (!other.myName.equals(myName)) return false;
    if (!other.myOptimizeFor.equals(myOptimizeFor)) return false;
    if (!other.myOutputFileName.equals(myOutputFileName)) return false;
    if (!other.myOutputFolder.equals(myOutputFolder)) return false;
    if (other.myOutputType != myOutputType) return false;
    if (other.myPureAs != myPureAs) return false;
    if (!other.myRLMs.equals(myRLMs)) return false;
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

  @Override
  @Nullable
  public Sdk getSdk() {
    final SdkEntry sdkEntry = myDependencies.getSdkEntry();
    return sdkEntry == null ? null : ContainerUtil.find(FlexSdkUtils.getFlexAndFlexmojosSdks(),
                                                        sdk -> sdkEntry.getName().equals(sdk.getName()));
  }

  @Override
  public boolean isTempBCForCompilation() {
    return myTempBCForCompilation;
  }

  void setTempBCForCompilation(final boolean tempBCForCompilation) {
    myTempBCForCompilation = tempBCForCompilation;
  }

  @Override
  public String toString() {
    return myName + ": " + getNature().toString();
  }

  @Override
  public String getStatisticsEntry() {
    StringBuilder s = new StringBuilder();
    switch (myTargetPlatform) {
      case Web:
        s.append("Web");
        break;
      case Desktop:
        s.append("Desktop");
        break;
      case Mobile:
        s.append("Mobile");
        if (myAndroidPackagingOptions.isEnabled() && myIosPackagingOptions.isEnabled()) {
          s.append("(a+i)");
        }
        else if (myAndroidPackagingOptions.isEnabled()) {
          s.append("(a)");
        }
        else if (myIosPackagingOptions.isEnabled()) {
          s.append("(i)");
        }
        break;
      default:
        assert false : myTargetPlatform;
    }
    s.append(" ");
    s.append(myPureAs ? "AS" : "Flex");
    s.append(" ");
    switch (myOutputType) {
      case Application:
        s.append("app");
        break;
      case Library:
        s.append("lib");
        break;
      case RuntimeLoadedModule:
        s.append("rlm");
        break;
      default:
        assert false : myOutputType;
    }
    Sdk sdk = getSdk();
    if (sdk != null && sdk.getSdkType() == FlexmojosSdkType.getInstance()) {
      s.append(" (mvn)");
    }
    return s.toString();
  }

  public FlexBuildConfigurationState getState(final @Nullable ComponentManager componentManager) {
    FlexBuildConfigurationState state = new FlexBuildConfigurationState();
    state.AIR_DESKTOP_PACKAGING_OPTIONS = myAirDesktopPackagingOptions.getState();
    state.ANDROID_PACKAGING_OPTIONS = myAndroidPackagingOptions.getState();
    state.COMPILER_OPTIONS = myCompilerOptions.getState(componentManager);
    state.DEPENDENCIES = myDependencies.getState();
    state.IOS_PACKAGING_OPTIONS = myIosPackagingOptions.getState();
    state.CSS_FILES_TO_COMPILE = collapsePaths(componentManager, myCssFilesToCompile);
    state.MAIN_CLASS = myMainClass;
    state.NAME = myName;
    state.OPTIMIZE_FOR = myOptimizeFor;
    state.OUTPUT_FILE_NAME = myOutputFileName;
    state.OUTPUT_FOLDER = myOutputFolder;
    state.OUTPUT_TYPE = myOutputType;
    state.PURE_ACTION_SCRIPT = myPureAs;
    state.RLMS = myRLMs;
    state.SKIP_COMPILE = mySkipCompile;
    state.TARGET_PLATFORM = myTargetPlatform;
    state.USE_HTML_WRAPPER = myUseHtmlWrapper;
    state.WRAPPER_TEMPLATE_PATH = myWrapperTemplatePath;
    return state;
  }

  public void loadState(final FlexBuildConfigurationState state, final Project project) {
    myAirDesktopPackagingOptions.loadState(state.AIR_DESKTOP_PACKAGING_OPTIONS);
    myAndroidPackagingOptions.loadState(state.ANDROID_PACKAGING_OPTIONS);
    myCompilerOptions.loadState(state.COMPILER_OPTIONS);
    myDependencies.loadState(state.DEPENDENCIES, project);
    myIosPackagingOptions.loadState(state.IOS_PACKAGING_OPTIONS);
    // no need in expanding paths, it is done automatically even if macros is not in the beginning of the string
    myCssFilesToCompile = state.CSS_FILES_TO_COMPILE;
    myMainClass = state.MAIN_CLASS;
    myName = state.NAME;
    myOptimizeFor = state.OPTIMIZE_FOR;
    myOutputFileName = state.OUTPUT_FILE_NAME;
    myOutputFolder = StringUtil.trimEnd(state.OUTPUT_FOLDER, "/");
    myOutputType = state.OUTPUT_TYPE;
    myPureAs = state.PURE_ACTION_SCRIPT;
    myRLMs = state.RLMS;
    mySkipCompile = state.SKIP_COMPILE;
    myTargetPlatform = state.TARGET_PLATFORM;
    myUseHtmlWrapper = state.USE_HTML_WRAPPER;
    myWrapperTemplatePath = state.WRAPPER_TEMPLATE_PATH;
  }

  static String collapsePaths(final @Nullable ComponentManager componentManager, final String value) {
    if (componentManager == null) return value;
    if (!value.contains(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR) && !value.contains(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)) {
      return value;
    }

    final StringBuilder result = new StringBuilder();
    final PathMacroManager pathMacroManager = PathMacroManager.getInstance(componentManager);
    final String delimiters = CompilerOptionInfo.LIST_ENTRIES_SEPARATOR + CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR;
    for (StringTokenizer tokenizer = new StringTokenizer(value, delimiters, true); tokenizer.hasMoreTokens(); ) {
      String token = tokenizer.nextToken();
      if (token.length() > 1) {
        token = pathMacroManager.collapsePath(token);
      }
      result.append(token);
    }

    return result.toString();
  }
}
