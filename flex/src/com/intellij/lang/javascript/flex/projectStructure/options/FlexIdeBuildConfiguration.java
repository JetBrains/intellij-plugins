package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;

import javax.swing.*;

public class FlexIdeBuildConfiguration implements Cloneable {

  private static final int CURRENT_VERSION = 1;

  public int VERSION = CURRENT_VERSION;
  public String NAME = "Unnamed";
  public TargetPlatform TARGET_PLATFORM = TargetPlatform.Web;
  public boolean PURE_ACTION_SCRIPT = false;
  public OutputType OUTPUT_TYPE = OutputType.Application;
  public String OPTIMIZE_FOR = "";
  public String MAIN_CLASS = "";
  public String OUTPUT_FILE_NAME = "";
  public String OUTPUT_FOLDER = "";
  public boolean USE_HTML_WRAPPER = false;
  public String WRAPPER_TEMPLATE_PATH = "";
  public boolean SKIP_COMPILE = false;

  public Dependencies DEPENDENCIES = new Dependencies();
  public CompilerOptions COMPILER_OPTIONS = new CompilerOptions();
  @Transient
  public AirDesktopPackagingOptionsImpl AIR_DESKTOP_PACKAGING_OPTIONS = new AirDesktopPackagingOptionsImpl();
  @Transient
  public AndroidPackagingOptionsImpl ANDROID_PACKAGING_OPTIONS = new AndroidPackagingOptionsImpl();
  @Transient
  public IosPackagingOptionsImpl IOS_PACKAGING_OPTIONS = new IosPackagingOptionsImpl();

  public Icon getIcon() {
    switch (TARGET_PLATFORM) {
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

  @Property(surroundWithTag = false)
  public AirDesktopPackagingOptionsImpl.State getAirDesktopPackagingOptionsState() {
    return AIR_DESKTOP_PACKAGING_OPTIONS.getState();
  }

  public void setAirDesktopPackagingOptionsState(AirDesktopPackagingOptionsImpl.State state) {
    AIR_DESKTOP_PACKAGING_OPTIONS.loadState(state);
  }

  @Property(surroundWithTag = false)
  public AndroidPackagingOptionsImpl.State getAndroidPackagingOptionsState() {
    return ANDROID_PACKAGING_OPTIONS.getState();
  }

  public void setAndroidPackagingOptionsState(AndroidPackagingOptionsImpl.State state) {
    ANDROID_PACKAGING_OPTIONS.loadState(state);
  }

  @Property(surroundWithTag = false)
  public IosPackagingOptionsImpl.State getIosPackagingOptionsState() {
    return IOS_PACKAGING_OPTIONS.getState();
  }

  public void setIosPackagingOptionsState(IosPackagingOptionsImpl.State state) {
    IOS_PACKAGING_OPTIONS.loadState(state);
  }

  public String getOutputFilePath() {
    return OUTPUT_FOLDER + (OUTPUT_FOLDER.isEmpty() ? "" : "/") + OUTPUT_FILE_NAME;
  }

  public FlexIdeBuildConfiguration clone() {
    try {
      final FlexIdeBuildConfiguration clone = (FlexIdeBuildConfiguration)super.clone();

      clone.DEPENDENCIES = DEPENDENCIES.clone();
      clone.COMPILER_OPTIONS = COMPILER_OPTIONS.clone();
      clone.AIR_DESKTOP_PACKAGING_OPTIONS = AIR_DESKTOP_PACKAGING_OPTIONS.getCopy();
      clone.ANDROID_PACKAGING_OPTIONS = ANDROID_PACKAGING_OPTIONS.getCopy();
      clone.IOS_PACKAGING_OPTIONS = IOS_PACKAGING_OPTIONS.getCopy();

      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }

  public void initialize(Project project) {
    DEPENDENCIES.initialize(project);
  }

  public BuildConfigurationNature getNature() {
    return new BuildConfigurationNature(TARGET_PLATFORM, PURE_ACTION_SCRIPT, OUTPUT_TYPE);
  }
}
