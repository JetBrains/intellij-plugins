package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class LinkageType {
  public static final LinkageType Default = new LinkageType("Default");
  public static final LinkageType Merged = new LinkageType("Merged into code");
  public static final LinkageType RSL = new LinkageType("Runtime shared library");
  public static final LinkageType External = new LinkageType("External");
  public static final LinkageType Include = new LinkageType("Include");
  public static final LinkageType LoadInRuntime = new LinkageType("Loaded in runtime");

  public final String PRESENTABLE_TEXT;
  private static final LinkageType[] FRAMEWORK_LINKAGE_VALUES = new LinkageType[]{Default, Merged, RSL, External, Include};
  private static final LinkageType[] SWC_LINKAGE_VALUES = new LinkageType[]{Merged, RSL, External, Include, LoadInRuntime};

  private LinkageType(final String presentableText) {
    PRESENTABLE_TEXT = presentableText;
  }

  public static LinkageType[] frameworkLinkageValues() {
    return FRAMEWORK_LINKAGE_VALUES;
  }

  public static LinkageType[] getSwcLinkageValues() {
    return SWC_LINKAGE_VALUES;
  }

}
