package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsOutputType;
import com.intellij.flex.model.bc.JpsTargetPlatform;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("configuration")
public class JpsFlexBCState {
  @Attribute(value = "name")
  public String NAME = JpsFlexBuildConfiguration.UNNAMED;

  @Attribute(value = "target-platform")
  public JpsTargetPlatform TARGET_PLATFORM = JpsTargetPlatform.Web;

  @Attribute(value = "pure-as")
  public boolean PURE_ACTION_SCRIPT = false;

  @Attribute(value = "output-type")
  public JpsOutputType OUTPUT_TYPE = JpsOutputType.Application;

  //@Attribute(value = "optimize-for")
  //public String OPTIMIZE_FOR = "";

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

  @Attribute(value = "runtime-loaded-modules")
  public String RLMS = "";

  @Attribute(value = "css-to-compile")
  public String CSS_FILES_TO_COMPILE = "";

  @Attribute(value = "skip-build")
  public boolean SKIP_COMPILE = false;

  @Property(surroundWithTag = false)
  public JpsFlexDependenciesImpl.State DEPENDENCIES;

  @Property(surroundWithTag = false)
  public JpsFlexCompilerOptionsImpl.State COMPILER_OPTIONS;

  @Property(surroundWithTag = false)
  public JpsAirDesktopPackagingOptionsImpl.State AIR_DESKTOP_PACKAGING_OPTIONS;

  @Property(surroundWithTag = false)
  public JpsAndroidPackagingOptionsImpl.State ANDROID_PACKAGING_OPTIONS;

  @Property(surroundWithTag = false)
  public JpsIosPackagingOptionsImpl.State IOS_PACKAGING_OPTIONS;

  public static JpsFlexBCState getState(final JpsFlexBuildConfiguration bc) {
    return ((JpsFlexBuildConfigurationImpl)bc).getState();
  }
}
