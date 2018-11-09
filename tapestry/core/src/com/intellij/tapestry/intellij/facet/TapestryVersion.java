package com.intellij.tapestry.intellij.facet;

public enum TapestryVersion {

  TAPESTRY_5_3_6("5.3.6");

  private final String _name;

  TapestryVersion(String name) {
    _name = name;
  }

   public String toString() {
    return _name;
  }

  public static TapestryVersion fromString(String name) {
    for (TapestryVersion version : TapestryVersion.values()) {
      if (version.toString().equals(name)) return version;
    }
    return null;
  }


}
