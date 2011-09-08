package com.intellij.lang.javascript.flex.projectStructure.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ksafonov
 */
public class LinkageType {
  private static final Collection<LinkageType> all = new ArrayList<LinkageType>();

  public static final LinkageType Default = new LinkageType("Default", "Default", "");
  public static final LinkageType Merged = new LinkageType("Merged", "Merged into code", "Merged");
  public static final LinkageType RSL = new LinkageType("Runtime", "Runtime shared library", "RSL");
  public static final LinkageType External = new LinkageType("External", "External", "External");
  public static final LinkageType Include = new LinkageType("Include", "Include", "Include");
  public static final LinkageType LoadInRuntime = new LinkageType("Loaded", "Loaded in runtime", "Loaded");

  private static final LinkageType[] SWC_LINKAGE_VALUES = new LinkageType[]{Merged, RSL, External, Include};

  private final String myLongText;
  private final String myShortText;
  private final String mySerializedText;

  public String getSerializedText() {
    return mySerializedText;
  }

  public String getLongText() {
    return myLongText;
  }

  public String getShortText() {
    return myShortText;
  }

  private LinkageType(String serializedText, String longText, String shortText) {
    myLongText = longText;
    mySerializedText = serializedText;
    myShortText = shortText;
    all.add(this);
  }

  @Override
  public String toString() {
    return getShortText();
  }

  public static LinkageType valueOf(String linkageType, LinkageType defaultValue) {
    for (LinkageType type : all) {
      if (type.getSerializedText().equals(linkageType)) {
        return type;
      }
    }
    return defaultValue;
  }

  public static LinkageType[] getSwcLinkageValues() {
    return SWC_LINKAGE_VALUES;
  }
}
