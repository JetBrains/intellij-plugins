// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.bc;

import java.util.ArrayList;
import java.util.Collection;

public final class LinkageType {
  private static final Collection<LinkageType> all = new ArrayList<>();

  public static final LinkageType Default = new LinkageType("Default", "Default", "");
  public static final LinkageType Merged = new LinkageType("Merged", "Merged into code", "Merged");
  public static final LinkageType RSL = new LinkageType("Runtime", "Runtime shared library", "RSL");
  public static final LinkageType External = new LinkageType("External", "External", "External");
  public static final LinkageType Include = new LinkageType("Include", "Include", "Include");
  public static final LinkageType LoadInRuntime = new LinkageType("Loaded", "Loaded at runtime", "Loaded");
  public static final LinkageType Test = new LinkageType("Test", "Test", "Test");

  private static final LinkageType[] SWC_LINKAGE_VALUES = new LinkageType[]{Merged, /*RSL,*/ External, Include, Test};

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

  private LinkageType(final String serializedText, final String longText, final String shortText) {
    myLongText = longText;
    mySerializedText = serializedText;
    myShortText = shortText;
    all.add(this);
  }

  @Override
  public String toString() {
    return getShortText();
  }

  public static LinkageType valueOf(final String linkageType, final LinkageType defaultValue) {
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
