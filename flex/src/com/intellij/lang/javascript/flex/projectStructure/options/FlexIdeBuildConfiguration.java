package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;

import javax.swing.*;

public class FlexIdeBuildConfiguration implements Cloneable {

  private static final int CURRENT_VERSION = 1;

  public int VERSION = CURRENT_VERSION;
  public String NAME = "Unnamed";
  public TargetPlatform TARGET_PLATFORM = TargetPlatform.Web;
  public boolean PURE_ACTION_SCRIPT = false;
  public OutputType OUTPUT_TYPE = OutputType.Application;
  public String OPTIMIZE_FOR = "";
  public String TARGET_PLAYER = "";
  public ComponentSet COMPONENT_SET = ComponentSet.SparkAndMx;
  public FrameworkLinkage FRAMEWORK_LINKAGE = FrameworkLinkage.Default;
  public String MAIN_CLASS = "";
  public String OUTPUT_FILE_NAME = "";
  public String OUTPUT_FOLDER = "";

  public Dependencies DEPENDENCIES = new Dependencies();
  public CompilerOptions COMPILER_OPTIONS = new CompilerOptions();
  public HtmlWrapperOptions HTML_WRAPPER_OPTIONS = new HtmlWrapperOptions();
  public AirDescriptorOptions AIR_DESCRIPTOR_OPTIONS = new AirDescriptorOptions();
  public AirDesktopPackagingOptions AIR_DESKTOP_PACKAGING_OPTIONS = new AirDesktopPackagingOptions();
  public AndroidPackagingOptions ANDROID_PACKAGING_OPTIONS = new AndroidPackagingOptions();
  public IOSPackagingOptions IOS_PACKAGING_OPTIONS = new IOSPackagingOptions();

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

  public FlexIdeBuildConfiguration clone() {
    try {
      final FlexIdeBuildConfiguration clone = (FlexIdeBuildConfiguration)super.clone();

      clone.DEPENDENCIES = DEPENDENCIES.clone();
      clone.COMPILER_OPTIONS = COMPILER_OPTIONS.clone();
      clone.HTML_WRAPPER_OPTIONS = HTML_WRAPPER_OPTIONS.clone();
      clone.AIR_DESCRIPTOR_OPTIONS = AIR_DESCRIPTOR_OPTIONS.clone();
      clone.AIR_DESKTOP_PACKAGING_OPTIONS = AIR_DESKTOP_PACKAGING_OPTIONS.clone();
      clone.ANDROID_PACKAGING_OPTIONS = ANDROID_PACKAGING_OPTIONS.clone();
      clone.IOS_PACKAGING_OPTIONS = IOS_PACKAGING_OPTIONS.clone();

      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }

  public static enum TargetPlatform {
    Web("Web"),
    Desktop("Desktop"),
    Mobile("Mobile");

    public final String PRESENTABLE_TEXT;

    TargetPlatform(final String presentableText) {
      PRESENTABLE_TEXT = presentableText;
    }
  }

  public static enum OutputType {
    Application("Application (*.swf)"),
    RuntimeLoadedModule("Runtime loaded module (*.swf)"),
    Library("Library (*.swc)");

    public final String PRESENTABLE_TEXT;

    OutputType(final String presentableText) {
      PRESENTABLE_TEXT = presentableText;
    }
  }

  public static enum ComponentSet {
    SparkAndMx("Spark + MX"),
    SparkOnly("Spark only"),
    MxOnly("MX only");

    public final String PRESENTABLE_TEXT;

    ComponentSet(final String presentableText) {
      PRESENTABLE_TEXT = presentableText;
    }
  }

  public static enum FrameworkLinkage {
    Default("Default"),
    Merged("Merged into code"),
    RSL("Runtime shared library"),
    External("External");

    public final String PRESENTABLE_TEXT;

    FrameworkLinkage(final String presentableText) {
      PRESENTABLE_TEXT = presentableText;
    }
  }
}
