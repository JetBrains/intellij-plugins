package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;

/**
* User: ksafonov
*/
@Tag("configuration")
public class FlexBuildConfigurationState {
  @Attribute(value = "name")
  public String NAME = FlexBuildConfiguration.UNNAMED;

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

  @Attribute(value = "runtime-loaded-modules")
  public String RLMS = "";

  @Attribute(value = "css-to-compile")
  public String CSS_FILES_TO_COMPILE = "";

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
