package com.intellij.flex.model.bc.impl;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.*;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.ex.JpsElementCollectionRole;
import org.jetbrains.jps.model.ex.JpsNamedCompositeElementBase;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsTypedModule;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class JpsFlexBuildConfigurationImpl extends JpsNamedCompositeElementBase<JpsFlexBuildConfigurationImpl>
  implements JpsFlexBuildConfiguration {

  private static final JpsElementChildRoleBase<JpsFlexBuildConfiguration> ROLE = JpsElementChildRoleBase.create("flex build configuration");
  static final JpsElementCollectionRole<JpsFlexBuildConfiguration> COLLECTION_ROLE = JpsElementCollectionRole.create(ROLE);

  //private @NotNull String myName = UNNAMED;
  private @NotNull TargetPlatform myTargetPlatform = BuildConfigurationNature.DEFAULT.targetPlatform;
  private boolean myPureAs = BuildConfigurationNature.DEFAULT.pureAS;
  private @NotNull OutputType myOutputType = BuildConfigurationNature.DEFAULT.outputType;
  private @NotNull String myOptimizeFor = "";
  private @NotNull String myMainClass = "";
  private @NotNull String myOutputFileName = "";
  private @NotNull String myOutputFolder = "";
  private boolean myUseHtmlWrapper = false;
  private @NotNull String myWrapperTemplatePath = "";
  private @NotNull String myRLMs = "";
  private @NotNull String myCssFilesToCompile = "";
  private boolean mySkipCompile = false;
  private boolean myTempBCForCompilation = false;

  JpsFlexBuildConfigurationImpl(@NotNull final String name) {
    super(name);
    myContainer.setChild(JpsFlexDependenciesImpl.ROLE);
    myContainer.setChild(JpsFlexCompilerOptionsRole.INSTANCE);
    myContainer.setChild(JpsAirDesktopPackagingOptionsImpl.ROLE);
    myContainer.setChild(JpsAndroidPackagingOptionsImpl.ROLE);
    myContainer.setChild(JpsIosPackagingOptionsImpl.ROLE);
  }

  private JpsFlexBuildConfigurationImpl(final JpsFlexBuildConfigurationImpl original) {
    super(original);
    myTargetPlatform = original.myTargetPlatform;
    myPureAs = original.myPureAs;
    myOutputType = original.myOutputType;
    myOptimizeFor = original.myOptimizeFor;
    myMainClass = original.myMainClass;
    myOutputFileName = original.myOutputFileName;
    myOutputFolder = original.myOutputFolder;
    myUseHtmlWrapper = original.myUseHtmlWrapper;
    myWrapperTemplatePath = original.myWrapperTemplatePath;
    myRLMs = original.myRLMs;
    myCssFilesToCompile = original.myCssFilesToCompile;
    mySkipCompile = original.mySkipCompile;
    myTempBCForCompilation = original.myTempBCForCompilation;
  }

  @Override
  @NotNull
  public JpsFlexBuildConfigurationImpl createCopy() {
    return new JpsFlexBuildConfigurationImpl(this);
  }

  @Override
  public void applyChanges(@NotNull final JpsFlexBuildConfigurationImpl modified) {
    super.applyChanges(modified);
    // todo use setters & dispatch events
    myTargetPlatform = modified.myTargetPlatform;
    myPureAs = modified.myPureAs;
    myOutputType = modified.myOutputType;
    myOptimizeFor = modified.myOptimizeFor;
    myMainClass = modified.myMainClass;
    myOutputFileName = modified.myOutputFileName;
    myOutputFolder = modified.myOutputFolder;
    myUseHtmlWrapper = modified.myUseHtmlWrapper;
    myWrapperTemplatePath = modified.myWrapperTemplatePath;
    myRLMs = modified.myRLMs;
    myCssFilesToCompile = modified.myCssFilesToCompile;
    mySkipCompile = modified.mySkipCompile;
    myTempBCForCompilation = modified.myTempBCForCompilation;
  }

// -----------------------------------------

  @Override
  public JpsTypedModule<JpsFlexBuildConfigurationManager> getModule() {
    return (JpsTypedModule<JpsFlexBuildConfigurationManager>)myParent.getParent().getParent();
  }

  @Override
  @NotNull
  public JpsFlexBCReference createReference() {
    return new JpsFlexBCReferenceImpl(getName(), getModule().createReference());
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
  public void setOutputType(@NotNull final OutputType outputType) {
    myOutputType = outputType;
  }

  @Override
  @NotNull
  public String getOptimizeFor() {
    return myOptimizeFor;
  }

  @Override
  public void setOptimizeFor(@NotNull final String optimizeFor) {
    myOptimizeFor = optimizeFor;
  }

  @Override
  @NotNull
  public String getMainClass() {
    return myMainClass;
  }

  @Override
  public void setMainClass(@NotNull final String mainClass) {
    myMainClass = mainClass;
  }

  @Override
  @NotNull
  public String getOutputFileName() {
    return myOutputFileName;
  }

  @Override
  public void setOutputFileName(@NotNull final String outputFileName) {
    myOutputFileName = outputFileName;
  }

  @Override
  @NotNull
  public String getOutputFolder() {
    return myOutputFolder;
  }

  @Override
  public void setOutputFolder(@NotNull final String outputFolder) {
    myOutputFolder = outputFolder;
  }

  @Override
  public boolean isUseHtmlWrapper() {
    return myUseHtmlWrapper;
  }

  @Override
  public void setUseHtmlWrapper(final boolean useHtmlWrapper) {
    myUseHtmlWrapper = useHtmlWrapper;
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
      result.add(new RLMInfo(parts.get(0), parts.get(1), Boolean.parseBoolean(parts.get(2))));
    }
    return result;
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
  @NotNull
  public Collection<String> getCssFilesToCompile() {
    if (myCssFilesToCompile.isEmpty()) return Collections.emptyList();
    return StringUtil.split(myCssFilesToCompile, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setCssFilesToCompile(@NotNull Collection<String> cssFilesToCompile) {
    myCssFilesToCompile = cssFilesToCompile.isEmpty() ? "" : StringUtil.join(cssFilesToCompile, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public boolean isSkipCompile() {
    return mySkipCompile;
  }

  @Override
  public void setSkipCompile(final boolean skipCompile) {
    mySkipCompile = skipCompile;
  }

  @Override
  @NotNull
  public JpsFlexDependencies getDependencies() {
    return myContainer.getChild(JpsFlexDependenciesImpl.ROLE);
  }

  @Override
  @NotNull
  public JpsFlexCompilerOptions getCompilerOptions() {
    return myContainer.getChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  @NotNull
  @Override
  public JpsAirDesktopPackagingOptions getAirDesktopPackagingOptions() {
    return myContainer.getChild(JpsAirDesktopPackagingOptionsImpl.ROLE);
  }

  @NotNull
  @Override
  public JpsAndroidPackagingOptions getAndroidPackagingOptions() {
    return myContainer.getChild(JpsAndroidPackagingOptionsImpl.ROLE);
  }

  @NotNull
  @Override
  public JpsIosPackagingOptions getIosPackagingOptions() {
    return myContainer.getChild(JpsIosPackagingOptionsImpl.ROLE);
  }

  @Override
  public Icon getIcon() {
    return getNature().getIcon();
  }

  @Override
  public String getShortText() {
    return getName();
  }

  @Override
  public String getDescription() {
    return myOutputType.getShortText();
  }

  @Override
  public String getActualOutputFilePath() {
    final InfoFromConfigFile info = InfoFromConfigFile.getInfoFromConfigFile(getCompilerOptions().getAdditionalConfigFilePath());
    final String outputFolderPath = FlexCommonUtils.isFlexUnitBC(this) ? myOutputFolder
                                                                       : StringUtil.notNullize(info.getOutputFolderPath(), myOutputFolder);
    final String outputFileName = myTempBCForCompilation ? myOutputFileName
                                                         : StringUtil.notNullize(info.getOutputFileName(), myOutputFileName);
    return outputFolderPath + (outputFolderPath.isEmpty() ? "" : "/") + outputFileName;
  }

  /*
  public boolean isEqual(FlexIdeBuildConfiguration bc) {
    final FlexIdeBuildConfigurationImpl other = (FlexIdeBuildConfigurationImpl)bc;
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
  */

  @Override
  public BuildConfigurationNature getNature() {
    return new BuildConfigurationNature(myTargetPlatform, myPureAs, myOutputType);
  }

  @Override
  @Nullable
  public JpsSdk<?> getSdk() {
    return getDependencies().getSdk();
  }

  @Override
  public boolean isTempBCForCompilation() {
    return myTempBCForCompilation;
  }

  void setTempBCForCompilation(final boolean tempBCForCompilation) {
    myTempBCForCompilation = tempBCForCompilation;
  }

    /*
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
  public void setRLMs(@NotNull Collection<RLMInfo> rlms) {
    if (rlms.isEmpty()) myRLMs = "";
    myRLMs = StringUtil.join(rlms, new Function<RLMInfo, String>() {
      public String fun(final RLMInfo info) {
        return info.MAIN_CLASS +
               JpsCompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR +
               info.OUTPUT_FILE +
               JpsCompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR +
               info.OPTIMIZE;
      }
    }, JpsCompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setCssFilesToCompile(@NotNull Collection<String> cssFilesToCompile) {
    myCssFilesToCompile = cssFilesToCompile.isEmpty() ? "" : StringUtil.join(cssFilesToCompile, JpsCompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  public void setSkipCompile(boolean skipCompile) {
    mySkipCompile = skipCompile;
  }
  */

  @Override
  public String toString() {
    return getName() + ": " + getNature().toString();
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
        if (getAndroidPackagingOptions().isEnabled() && getIosPackagingOptions().isEnabled()) {
          s.append("(a+i)");
        }
        else if (getAndroidPackagingOptions().isEnabled()) {
          s.append("(a)");
        }
        else if (getIosPackagingOptions().isEnabled()) {
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
    final JpsSdk<?> sdk = getSdk();
    if (sdk != null && sdk.getSdkType() == JpsFlexmojosSdkType.INSTANCE) {
      s.append(" (mvn)");
    }
    return s.toString();
  }

  JpsFlexBCState getState() {
    final JpsFlexBCState state = new JpsFlexBCState();

    state.DEPENDENCIES = ((JpsFlexDependenciesImpl)getContainer().getChild(JpsFlexDependenciesImpl.ROLE)).getState();
    state.COMPILER_OPTIONS = ((JpsFlexCompilerOptionsImpl)getContainer().getChild(JpsFlexCompilerOptionsRole.INSTANCE)).getState();
    state.AIR_DESKTOP_PACKAGING_OPTIONS =
      ((JpsAirDesktopPackagingOptionsImpl)getContainer().getChild(JpsAirDesktopPackagingOptionsImpl.ROLE)).getState();
    state.ANDROID_PACKAGING_OPTIONS =
      ((JpsAndroidPackagingOptionsImpl)getContainer().getChild(JpsAndroidPackagingOptionsImpl.ROLE)).getState();
    state.IOS_PACKAGING_OPTIONS = ((JpsIosPackagingOptionsImpl)getContainer().getChild(JpsIosPackagingOptionsImpl.ROLE)).getState();

    state.NAME = getName();
    state.TARGET_PLATFORM = myTargetPlatform;
    state.PURE_ACTION_SCRIPT = myPureAs;
    state.OUTPUT_TYPE = myOutputType;
    //state.OPTIMIZE_FOR = myOptimizeFor;
    state.MAIN_CLASS = myMainClass;
    state.OUTPUT_FILE_NAME = myOutputFileName;
    state.OUTPUT_FOLDER = myOutputFolder;
    state.USE_HTML_WRAPPER = myUseHtmlWrapper;
    state.WRAPPER_TEMPLATE_PATH = myWrapperTemplatePath;
    state.RLMS = myRLMs;
    state.CSS_FILES_TO_COMPILE = myCssFilesToCompile; //collapsePaths(componentManager, myCssFilesToCompile);
    state.SKIP_COMPILE = mySkipCompile;

    return state;
  }

  void loadState(final JpsFlexBCState state) {
    ((JpsFlexDependenciesImpl)getContainer().getChild(JpsFlexDependenciesImpl.ROLE)).loadState(state.DEPENDENCIES);
    ((JpsFlexCompilerOptionsImpl)getContainer().getChild(JpsFlexCompilerOptionsRole.INSTANCE)).loadState(state.COMPILER_OPTIONS);
    ((JpsAirDesktopPackagingOptionsImpl)getContainer().getChild(JpsAirDesktopPackagingOptionsImpl.ROLE))
      .loadState(state.AIR_DESKTOP_PACKAGING_OPTIONS);
    ((JpsAndroidPackagingOptionsImpl)getContainer().getChild(JpsAndroidPackagingOptionsImpl.ROLE))
      .loadState(state.ANDROID_PACKAGING_OPTIONS);
    ((JpsIosPackagingOptionsImpl)getContainer().getChild(JpsIosPackagingOptionsImpl.ROLE)).loadState(state.IOS_PACKAGING_OPTIONS);

    //setName(state.NAME); already set via constructor
    myTargetPlatform = state.TARGET_PLATFORM;
    myPureAs = state.PURE_ACTION_SCRIPT;
    myOutputType = state.OUTPUT_TYPE;
    //myOptimizeFor = state.OPTIMIZE_FOR;
    myMainClass = state.MAIN_CLASS;
    myOutputFileName = state.OUTPUT_FILE_NAME;
    myOutputFolder = state.OUTPUT_FOLDER;
    myUseHtmlWrapper = state.USE_HTML_WRAPPER;
    myWrapperTemplatePath = state.WRAPPER_TEMPLATE_PATH;
    myRLMs = state.RLMS;
    // no need in expanding paths, it is done automatically even if macros is not in the beginning of the string
    myCssFilesToCompile = state.CSS_FILES_TO_COMPILE;
    mySkipCompile = state.SKIP_COMPILE;
  }

  /*
  static String collapsePaths(final @Nullable ComponentManager componentManager, final String value) {
    if (componentManager == null) return value;
    if (!value.contains(JpsCompilerOptionInfo.LIST_ENTRIES_SEPARATOR) && !value.contains(JpsCompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)) {
      return value;
    }

    final StringBuilder result = new StringBuilder();
    final PathMacroManager pathMacroManager = PathMacroManager.getInstance(componentManager);
    final String delimiters = JpsCompilerOptionInfo.LIST_ENTRIES_SEPARATOR + JpsCompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR;
    for (StringTokenizer tokenizer = new StringTokenizer(value, delimiters, true); tokenizer.hasMoreTokens(); ) {
      String token = tokenizer.nextToken();
      if (token.length() > 1) {
        token = pathMacroManager.collapsePath(token);
      }
      result.append(token);
    }

    return result.toString();
  }
  */
}
