package com.intellij.flex.model.bc;

import java.util.ArrayList;
import java.util.Collection;

public class JpsLinkageType {
  private static final Collection<JpsLinkageType> all = new ArrayList<JpsLinkageType>();

  public static final JpsLinkageType Default = new JpsLinkageType("Default", "Default", "");
  public static final JpsLinkageType Merged = new JpsLinkageType("Merged", "Merged into code", "Merged");
  public static final JpsLinkageType RSL = new JpsLinkageType("Runtime", "Runtime shared library", "RSL");
  public static final JpsLinkageType External = new JpsLinkageType("External", "External", "External");
  public static final JpsLinkageType Include = new JpsLinkageType("Include", "Include", "Include");
  public static final JpsLinkageType LoadInRuntime = new JpsLinkageType("Loaded", "Loaded at runtime", "Loaded");
  public static final JpsLinkageType Test = new JpsLinkageType("Test", "Test", "Test");

  private static final JpsLinkageType[] SWC_LINKAGE_VALUES = new JpsLinkageType[]{Merged, /*RSL,*/ External, Include, Test};

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

  private JpsLinkageType(final String serializedText, final String longText, final String shortText) {
    myLongText = longText;
    mySerializedText = serializedText;
    myShortText = shortText;
    all.add(this);
  }

  @Override
  public String toString() {
    return getShortText();
  }

  public static JpsLinkageType valueOf(final String linkageType, final JpsLinkageType defaultValue) {
    for (JpsLinkageType type : all) {
      if (type.getSerializedText().equals(linkageType)) {
        return type;
      }
    }
    return defaultValue;
  }

  public static JpsLinkageType[] getSwcLinkageValues() {
    return SWC_LINKAGE_VALUES;
  }
}
